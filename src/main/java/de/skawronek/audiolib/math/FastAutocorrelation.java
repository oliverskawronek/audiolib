package de.skawronek.audiolib.math;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.util.AbstractFactory;
import de.skawronek.audiolib.util.Util;

// Basierend auf dem Wiener–Khinchin-Theorem kann die Autokorrelation
// mittels FFT in O(n*log(n)) mit berechnet werden.
// Autokorrelation eines Signals x[i] ist R_xx[i] = Sum_j=0^N-1 x[j]*x[j - i].
// Das Wiener–Khinchin-Theorem besagt, dass das Power-Spektrum S[] der
// Fourier-Transformierten von R_xx[] gleich ist: S[] = FFT(R_xx[]).
// Daher gilt: R_xx[] = IFFT(FFT(S[])) = IFFT(|FFT(x[])|^2)
public final class FastAutocorrelation {
	public static final class Factory extends
			AbstractFactory<Integer, FastAutocorrelation> {
		private static final Factory INSTANCE = new Factory();

		private Factory() {
		}

		public static Factory getInstance() {
			return INSTANCE;
		}

		public @NonNull FastAutocorrelation get(final int size) {
			return super.get(size);
		}

		@Override
		protected @NonNull FastAutocorrelation create(@NonNull Integer size) {
			return new FastAutocorrelation(size);
		}
	}

	private final int maxInputSize;
	private int lastInputSize;
	private final int zeroPaddedSize;
	private final float[] zeroPadded;
	private final FastFourierTransform fft;
	// Zum Speichern von: Realteil der FFT, Powerspektrum S[] und letztlich der
	// Autokorrelation R_xx[]
	private final float[] buffer1;
	// Zum Speichern von: Imaginärteil der FFT
	private final float[] buffer2;

	FastAutocorrelation(final int size) {
		if (size <= 0) {
			throw new IllegalArgumentException("size " + size + " <= 0");
		}

		this.maxInputSize = Util.getNextPowerOfTwo(size);
		this.zeroPaddedSize = 2 * Util.getNextPowerOfTwo(size);
		this.zeroPadded = new float[zeroPaddedSize];
		// Samplerate ist egal
		this.fft = FastFourierTransform.Factory.getInstance().get(
				zeroPaddedSize);
		this.buffer1 = new float[zeroPaddedSize];
		this.buffer2 = new float[zeroPaddedSize];
	}

	public int getMaxInputSize() {
		return maxInputSize;
	}

	public void compute(final float @NonNull [] samples) {
		if (samples.length > maxInputSize) {
			throw new IllegalArgumentException("samples length "
					+ samples.length + " > maxInputSize " + maxInputSize);
		}

		lastInputSize = samples.length;

		// 1. Zero-Padding: zeroPadded[] = [samples; 0...0]
		System.arraycopy(samples, 0, zeroPadded, 0, samples.length);
		final int startIndex = maxInputSize; // inkl.
		final int endIndex = zeroPaddedSize; // exkl.
		Arrays.fill(zeroPadded, startIndex, endIndex, 0.0f);

		// 2. buffer1/2[] = FFT(zeroPadded[])
		fft.forward(zeroPadded);
		fft.copyReal(buffer1);
		fft.copyImaginary(buffer2);

		// 3. buffer1[] = S[] (Powerspektrum)
		for (int i = 0; i < zeroPaddedSize; i++) {
			final float re = buffer1[i];
			final float im = buffer2[i];
			buffer1[i] = re * re + im * im;
		}

		// 4. buffer1[] = IFFT(buffer1[])
		fft.backward(buffer1);
		fft.copyReal(buffer1);
	}

	public void copyCoefficients(final float @NonNull [] dest) {
		if (dest.length < lastInputSize) {
			throw new IllegalArgumentException("dest length " + dest.length
					+ " < last input size " + lastInputSize);
		}

		// In buffer1 ist das Ergebnis der letzten Berechnung gespeichert
		System.arraycopy(buffer1, 0, dest, 0, lastInputSize);
	}

	public void normalize() {
		final float max = buffer1[0];
		if (max == 0) {
			// Sondefall, bei dem das Eingangssignal überall Null ist. In dem
			// Fall wird der erste Koeffizient zu Eins und der Rest zu Null
			// gesetzt.
			buffer1[0] = 1f;
			Arrays.fill(buffer1, 1, lastInputSize, 0f);
		} else {
			// Skaliert die Koeffizienten so, dass der erste Koeffizient Eins
			// wird.
			final float normalizationFactor = 1f / max;
			for (int i = 0; i < lastInputSize; i++) {
				buffer1[i] *= normalizationFactor;
			}
		}
	}
}
