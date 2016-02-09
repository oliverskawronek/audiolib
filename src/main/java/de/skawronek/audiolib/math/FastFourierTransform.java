package de.skawronek.audiolib.math;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.util.AbstractFactory;
import de.skawronek.audiolib.util.Util;

public final class FastFourierTransform {
	public static final class Factory extends
			AbstractFactory<Integer, FastFourierTransform> {
		private static final Factory INSTANCE = new Factory();

		private Factory() {
		}

		public static Factory getInstance() {
			return INSTANCE;
		}

		public @NonNull FastFourierTransform get(final int size) {
			return super.get(size);
		}

		@Override
		protected @NonNull FastFourierTransform create(@NonNull Integer size) {
			return new FastFourierTransform(size);
		}
	}

	final int size;

	final float[] imaginary;
	final float[] real;

	private final int[] reverse;

	// Lookup Tabellen
	final float[] sinLookup;
	final float[] cosLookup;

	FastFourierTransform(final int size) {
		if (!Util.isPowerOfTwo(size)) {
			throw new IllegalArgumentException("size " + size
					+ " is not a power of two");
		}

		this.size = size;

		imaginary = new float[size];
		real = new float[size];

		reverse = new int[size];
		reverse[0] = 0;
		for (int limit = 1, bit = size / 2; limit < size; limit <<= 1, bit >>= 1) {
			for (int i = 0; i < limit; i++)
				reverse[i + limit] = reverse[i] + bit;
		}

		sinLookup = new float[size];
		cosLookup = new float[size];
		fillLookupTables();
	}

	private void fillLookupTables() {
		for (int i = 0; i < size; i++) {
			final float rad = (float) (-Math.PI / i);
			sinLookup[i] = (float) Math.sin(rad);
			cosLookup[i] = (float) Math.cos(rad);
		}
	}

	// Vorwärtstransformation für reellwertige Signale
	public void forward(final float @NonNull [] samples) {
		if (samples.length != size) {
			throw new IllegalArgumentException("samples length "
					+ samples.length + " must be " + size);
		}

		bitReverseSamples(samples);
		fft();
	}

	// Forward transform of complex valued signal
	public void forward(final float @NonNull [] real,
			final float @NonNull [] imaginary) {
		if (real.length != size || imaginary.length != size) {
			throw new IllegalArgumentException(
					"Length of real and imaginary must be " + size);
		}

		bitReverseSamples(real, imaginary);
		fft();
	}

	public void backward(final float @NonNull [] samples) {
		if (samples.length != size) {
			throw new IllegalArgumentException("samples length "
					+ samples.length + " must be " + size);
		}

		// 1. Take conjugate. Nothing to do on real valued input.
		// 2. Compute forward FFT
		bitReverseSamples(samples);
		fft();
		// 3. Take conjugate again
		// 4. Divide by size
		for (int i = 0; i < size; i++) {
			this.real[i] = this.real[i] / size;
			this.imaginary[i] = -this.imaginary[i] / size;
		}
	}

	public void backward(final float @NonNull [] real,
			final float @NonNull [] imaginary) {
		if (real.length != size || imaginary.length != size) {
			throw new IllegalArgumentException(
					"Length of real and imaginary must be " + size);
		}

		// 1. Take conjugate
		// 2. Compute forward FFT
		bitReverseAndConjugateSamples(real, imaginary);
		fft();
		// 3. Take conjugate again
		// 4. Divide by size
		for (int i = 0; i < size; i++) {
			this.real[i] = this.real[i] / size;
			this.imaginary[i] = -this.imaginary[i] / size;
		}
	}

	public void copyReal(final float @NonNull [] buffer) {
		if (buffer.length < real.length) {
			throw new IllegalArgumentException("buffer length " + buffer.length
					+ " < size " + real.length);
		}

		System.arraycopy(real, 0, buffer, 0, real.length);
	}

	public void copyImaginary(final float @NonNull [] buffer) {
		if (buffer.length < real.length) {
			throw new IllegalArgumentException("buffer length " + buffer.length
					+ " < size " + imaginary.length);
		}

		System.arraycopy(imaginary, 0, buffer, 0, imaginary.length);
	}

	public int getSize() {
		return real.length;
	}

	// Copies the values in the samples array into the real array
	// in bit reversed order. the imaginary array is filled with zeros.
	// Used for real transform.
	private void bitReverseSamples(final float @NonNull [] samples) {
		for (int i = 0; i < size; i++) {
			real[i] = samples[reverse[i]];
			imaginary[i] = 0.0f;
		}
	}

	// Copies the values of real and imaginary in reversed order.
	// Used for complex transform.
	private void bitReverseSamples(final float @NonNull [] real,
			final float @NonNull [] imaginary) {
		for (int i = 0; i < size; i++) {
			this.real[i] = real[reverse[i]];
			this.imaginary[i] = imaginary[reverse[i]];
		}
	}

	// Copies the conjugate values of real and imaginary in reversed order.
	// Used for complex transform.
	private void bitReverseAndConjugateSamples(final float @NonNull [] real,
			final float @NonNull [] imaginary) {
		for (int i = 0; i < size; i++) {
			this.real[i] = real[reverse[i]];
			this.imaginary[i] = -imaginary[reverse[i]];
		}
	}

	// performs an in-place fft on the data in the real and imag arrays
	// bit reversing is not necessary as the data will already be bit reversed
	private void fft() {
		for (int halfSize = 1; halfSize < real.length; halfSize *= 2) {
			// float k = -(float)Math.PI/halfSize;
			// phase shift step
			// float phaseShiftStepR = (float)Math.cos(k);
			// float phaseShiftStepI = (float)Math.sin(k);
			// using lookup table
			float phaseShiftStepR = cos(halfSize);
			float phaseShiftStepI = sin(halfSize);
			// current phase shift
			float currentPhaseShiftR = 1.0f;
			float currentPhaseShiftI = 0.0f;
			for (int fftStep = 0; fftStep < halfSize; fftStep++) {
				for (int i = fftStep; i < real.length; i += 2 * halfSize) {
					int off = i + halfSize;
					float tr = (currentPhaseShiftR * real[off])
							- (currentPhaseShiftI * imaginary[off]);
					float ti = (currentPhaseShiftR * imaginary[off])
							+ (currentPhaseShiftI * real[off]);
					real[off] = real[i] - tr;
					imaginary[off] = imaginary[i] - ti;
					real[i] += tr;
					imaginary[i] += ti;
				}
				float tmpR = currentPhaseShiftR;
				currentPhaseShiftR = (tmpR * phaseShiftStepR)
						- (currentPhaseShiftI * phaseShiftStepI);
				currentPhaseShiftI = (tmpR * phaseShiftStepI)
						+ (currentPhaseShiftI * phaseShiftStepR);
			}
		}
	}

	private float sin(final int i) {
		// = sin(-Pi / i)
		return sinLookup[i];
	}

	private float cos(final int i) {
		// = cos(-Pi / i)
		return cosLookup[i];
	}

	// Berechnet das Betragsspektrum
	public void fillMagnitudeSpectrum(final float @NonNull [] spectrum,
			final boolean firstHalfOnly) {
		computeMagnitudeSpectrum(real, imaginary, spectrum, firstHalfOnly);
	}

	/**
	 * Berechnet das Betragsspektrum aus der Fourier-Transformierten:
	 * 
	 * <pre>
	 * spectrum[k] = sqrt(re[k]² + im[k]²)
	 * </pre>
	 * 
	 * Für reellwertige Eingangssignale der Fourier-Transformation gilt, dass
	 * die zweite Hälfte der Fourier-Transformierten konjugiert symmetrisch zur
	 * ersten Hälfte ist. Die zweite Hälfte des Betragsspektrums ist demzufolge
	 * die gespiegelte erste Hälfte.
	 * 
	 * <pre>
	 * spectrum[k] == spectrum[n - k] // für reellwertige Eingangssignale
	 * </pre>
	 * 
	 * Wenn firstHalfOnly = true ist, dann wird nur die erste Hälfte des
	 * Betragsspektrums berechnet.
	 * 
	 * @param re
	 *            Ergebnis von {@link #copyReal(float[])}
	 * @param im
	 *            Ergebnis von {@link #copyImaginary(float[])}
	 * @param spectrum
	 *            Betragsspektrum, das gefüllt wird
	 * @param firstHalfOnly
	 *            nur die erste Hälfte des Betragsspektrums wird berechnet.
	 */
	public static void computeMagnitudeSpectrum(final float @NonNull [] re,
			final float @NonNull [] im, final float @NonNull [] spectrum,
			final boolean firstHalfOnly) {
		checkSpectrumArguments(re, im, spectrum, firstHalfOnly, "spectrum");

		final int size = re.length;
		final int expectedSize = firstHalfOnly ? size / 2 : size;
		for (int k = 0; k < expectedSize; k++) {
			spectrum[k] = (float) Math.sqrt(re[k] * re[k] + im[k] * im[k]);
		}
	}

	public void normalizeMagnitudeSpectrum(final float @NonNull [] spectrum,
			final boolean realValuedInput) {
		normalizeMagnitudeSpectrum(spectrum, getSize(), realValuedInput);
	}

	/**
	 * Normalisiert das Betragsspektrum so, dass die Bin-Werte den Amplituden
	 * des Eingangssignals entsprechen. Das übergebene Array wird mit dem
	 * Ergebnis überschrieben (in situ Berechnung).
	 * <ul>
	 * <li>reellwertige Eingangssignale:
	 * <code>spectrum[k] = (2/n) * spectrum[k]</code></li>
	 * <li>complexwertige Eingangssignale:
	 * <code>spectrum[k] = (1/n) * spectrum[k]</code></li>
	 * </ul>
	 * 
	 * @param spectrum
	 *            Betragsspektrum berechnet mit
	 *            {@link #fillMagnitudeSpectrum(float[], boolean)}
	 * @param realValuedInput
	 *            true für reellwertiges Eingangssignal, false für
	 *            komplexwertiges Eingangssignal
	 */
	public static void normalizeMagnitudeSpectrum(
			final float @NonNull [] spectrum, final int size,
			final boolean realValuedInput) {
		// http://matteolandi.blogspot.de/2010/08/notes-about-fft-normalization.html
		// http://dsp.stackexchange.com/questions/11376/why-are-magnitudes-normalised-during-synthesis-idft-not-analysis-dft
		if (size <= 0) {
			throw new IllegalArgumentException("size " + size + " <= 0");
		} else if ((spectrum.length != size) && (spectrum.length != size / 2)) {
			throw new IllegalArgumentException("spectrum length "
					+ spectrum.length + " is neither size " + size
					+ " nor half of size " + (size / 2));
		}

		final float scale;
		if (realValuedInput) {
			// A magnitude spectrum X of a real valued input x is symmetric
			// because of the konjugate symmetrie X(-f) = X'(f). The magnitudes
			// are divide into two halfs. Where are only interested in first
			// half. So we multiply by 2 to get the full amplitude.
			scale = 2f / size;
		} else {
			scale = 1f / size;
		}

		for (int i = 0; i < spectrum.length; i++) {
			spectrum[i] *= scale;
		}
	}

	public void fillPhaseSpectrum(final float @NonNull [] phase,
			final boolean firstHalfOnly) {
		computePhaseSpectrum(real, imaginary, phase, firstHalfOnly);
	}

	/**
	 * <pre>
	 * phase[k] = atan2(im[k], re[k])
	 * </pre>
	 */
	public static void computePhaseSpectrum(final float @NonNull [] re,
			final float @NonNull [] im, final float @NonNull [] phase,
			final boolean firstHalfOnly) {
		checkSpectrumArguments(re, im, phase, firstHalfOnly, "phase");

		final int size = re.length;
		final int expectedSize = (firstHalfOnly ? size / 2 : size);
		for (int k = 0; k < expectedSize; k++) {
			phase[k] = (float) Math.atan2(im[k], re[k]);
		}
	}

	public void fillPowerSpectrum(final float @NonNull [] power,
			final boolean firstHalfOnly) {
		computePowerSpectrum(real, imaginary, power, firstHalfOnly);
	}

	/**
	 * <pre>
	 * power[k] = re[k]² + im[k]²
	 * </pre>
	 */
	public static void computePowerSpectrum(final float @NonNull [] re,
			final float @NonNull [] im, final float @NonNull [] power,
			final boolean firstHalfOnly) {
		checkSpectrumArguments(re, im, power, firstHalfOnly, "power");

		final int size = re.length;
		final int expectedSize = (firstHalfOnly ? size / 2 : size);
		for (int k = 0; k < expectedSize; k++) {
			power[k] = re[k] * re[k] + im[k] * im[k];
		}
	}

	private static void checkSpectrumArguments(final float @NonNull [] re,
			final float @NonNull [] im, final float @NonNull [] spectrum,
			final boolean firstHalfOnly, final @NonNull String spectrumName) {
		if (re.length != im.length) {
			throw new IllegalArgumentException("re length " + re.length
					+ " != im.length " + im.length);
		}
		final int size = re.length;
		final int expectedSize = firstHalfOnly ? size / 2 : size;
		if (spectrum.length < expectedSize) {
			throw new IllegalArgumentException(spectrumName + " length "
					+ spectrum.length + " < expected size " + expectedSize);
		}
	}

	public static double binToFrequency(final int bin, final int size,
			final double sampleRate) {
		final double bandWidth = getBandWidth(size, sampleRate);
		return bin * bandWidth;
	}

	public static int frequencyToBin(final double frequency, final int size,
			final double sampleRate) {
		if (frequency < 0) {
			throw new IllegalArgumentException("frequency " + frequency
					+ " < 0");
		} else if (frequency > sampleRate / 2) {
			throw new IllegalArgumentException("frequency " + frequency
					+ " > (sampleRate / 2 = " + (sampleRate / 2) + ")");
		}

		return (int) Math.round(size * (frequency / sampleRate));
	}

	public static double getBandWidth(final int size, final double sampleRate) {
		return (2.0 / size) * (sampleRate / 2.0);
	}
}