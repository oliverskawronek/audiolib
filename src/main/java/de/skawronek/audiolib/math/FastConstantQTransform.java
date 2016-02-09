package de.skawronek.audiolib.math;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.math.WindowFunctions.IWindowFunction;
import de.skawronek.audiolib.util.AbstractFactory;
import de.skawronek.audiolib.util.Util;

public final class FastConstantQTransform {
	// "An Efficient Algorithm for the Calculation of a constant Q transform",
	// Judith C. Brown, Page 3
	private static final float MINVAL = 0.15f;

	private static final class Index {
		final double minFreq;
		final double maxFreq;
		final int binsPerOctave;
		final double sampleRate;

		public Index(final double minFreq, final double maxFreq,
				final int binsPerOctave, final double sampleRate) {
			checkArguments(minFreq, maxFreq, binsPerOctave, sampleRate);

			this.minFreq = minFreq;
			this.maxFreq = maxFreq;
			this.binsPerOctave = binsPerOctave;
			this.sampleRate = sampleRate;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null) {
				return false;
			} else if (obj == this) {
				return true;
			} else if (obj instanceof Index) {
				final Index other = (Index) obj;
				//@formatter:off
				return Double.compare(other.minFreq, this.minFreq) == 0
						&& Double.compare(other.maxFreq, this.maxFreq) == 0
						&& other.binsPerOctave == this.binsPerOctave
						&& Double.compare(other.sampleRate, this.sampleRate) == 0;
				//@formatter:on
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			//@formatter:off
			return Double.hashCode(minFreq)
					^ Double.hashCode(maxFreq)
					^ binsPerOctave
					^ Double.hashCode(sampleRate);
			//@formatter:on
		}
	}

	public static final class Factory extends
			AbstractFactory<Index, FastConstantQTransform> {
		private static final Factory INSTANCE = new Factory();

		private Factory() {
		}

		public static Factory getInstance() {
			return INSTANCE;
		}

		public @NonNull FastConstantQTransform get(double minFreq,
				double maxFreq, int binsPerOctave, double sampleRate) {
			final Index index = new Index(minFreq, maxFreq, binsPerOctave,
					sampleRate);
			return super.get(index);
		}

		@Override
		protected @NonNull FastConstantQTransform create(@NonNull Index index) {
			return new FastConstantQTransform(index.minFreq, index.maxFreq,
					index.binsPerOctave, index.sampleRate);
		}
	}

	private final Kernel kernel;
	private final int binsPerOctave;
	private final double minFreq;
	private final double sampleRate;

	private final float[] real;
	private final float[] imaginary;

	public static final class Kernel {
		private final FastFourierTransform fft;
		private final ComplexSparseVector[] rows;

		private final float[] transformedRe;
		private final float[] transformedIm;

		public Kernel(final int binsPerOctave, final double minFreq,
				final double maxFreq, final double sampleRate) {
			final double q = calcQ(binsPerOctave);
			final int numBins = calcNumBins(binsPerOctave, minFreq, maxFreq);
			final int n0 = calcWindowSize(q, sampleRate, minFreq);
			final int fftLen = Util.getNextPowerOfTwo(n0);
			// Sample-Rate spielt bei der FFT keine Rolle
			fft = FastFourierTransform.Factory.getInstance().get(fftLen);
			// Eine Zeile des temporären Kernels
			final float[] tmpKernelRe = new float[fftLen];
			final float[] tmpKernelIm = new float[fftLen];
			// Fensterfunktion
			final IWindowFunction hamming = WindowFunctions.getHammingWindow();

			this.rows = new ComplexSparseVector[numBins];

			this.transformedRe = new float[fftLen];
			this.transformedIm = new float[fftLen];

			// Berechne alle Zeilen/Bins des Kernels
			// Fange bei der Zeile an, bei der das Fenster am größten ist.
			// Das erspart das Füllen mit Nullen des temporären Kernels.
			final float[] kernelRe = new float[fftLen];
			final float[] kernelIm = new float[fftLen];
			for (int k = numBins - 1; k >= 0; k--) {
				// Center-Frequenz des k-ten Bins in Hz
				final double fk = calcFrequency(k, minFreq, binsPerOctave);
				// Fenstergröße des k-ten Bins
				final int nk = calcWindowSize(q, sampleRate, fk);
				// Fenster des k-ten Bins
				final double[] wk = createCoefficients(nk, hamming);
				// Generiere alle Spalten der k-ten Zeile des temporären Kernels
				for (int i = 0; i < nk; i++) {
					// i-ter Exponent der e-Funktion (Imaginärteil)
					final double exponent = (2 * Math.PI * q / nk) * i;
					// Eulersche-Identität: r*e^(j*phi) = r*cos(phi) +
					// r*sin(phi)j
					tmpKernelRe[i] = (float) ((wk[i] / nk) * Math.cos(exponent));
					tmpKernelIm[i] = (float) ((wk[i] / nk) * Math.sin(exponent));
				}

				// Kernel ist zeilenweise fourier-transformierte des temporären
				// Kernels.
				fft.forward(tmpKernelRe, tmpKernelIm);
				fft.copyReal(kernelRe);
				fft.copyImaginary(kernelIm);

				rows[k] = ComplexSparseVector.fromArray(kernelRe, kernelIm,
						MINVAL);
			}
		}

		static double[] createCoefficients(final int size,
				final IWindowFunction wf) {
			final double[] w = new double[size];
			for (int i = 0; i < size; i++) {
				// Bilde [0, size-1] auf [-0.5, 0.5] ab
				final double t = ((double) i / (double) size) - 0.5;
				w[i] = wf.evaluate(t);
			}
			return w;
		}

		void forward(final float @NonNull [] samples,
				final float @NonNull [] outRe, final float @NonNull [] outIm) {
			final int fftLen = this.fft.getSize();
			final int size = samples.length;
			if (size > fftLen) {
				throw new IllegalArgumentException("samples length "
						+ samples.length + " > fft length " + fftLen);
			}

			final float[] inRe, inIm;
			if (size == fftLen) {
				inRe = samples;
			} else {
				System.arraycopy(samples, 0, transformedRe, 0, size);
				Arrays.fill(transformedRe, size, fftLen, 0f);
				inRe = transformedRe;
			}
			Arrays.fill(transformedIm, 0, fftLen, 0f);
			inIm = transformedIm;

			internalForward(inRe, inIm, outRe, outIm);
		}

		void forward(float @NonNull [] inRe, float @NonNull [] inIm,
				final float[] outRe, final float[] outIm) {
			final int fftLen = this.fft.getSize();
			if (inRe.length != inIm.length) {
				throw new IllegalArgumentException("real length " + inRe.length
						+ " != imaginary length " + inIm.length);
			} else if (inRe.length > fftLen) {
				throw new IllegalArgumentException("input size " + inRe.length
						+ " > fft length " + fftLen);
			}

			// Zero Padding des Eingangssignal auf FFT-Länge
			final int size = inRe.length;
			if (size < fft.getSize()) {
				System.arraycopy(inRe, 0, transformedRe, 0, size);
				Arrays.fill(transformedRe, size, fftLen, 0f);
				inRe = transformedRe;

				System.arraycopy(inIm, 0, transformedIm, 0, size);
				Arrays.fill(transformedIm, size, fftLen, 0f);
				inIm = transformedIm;
			}

			internalForward(inRe, inIm, outRe, outIm);
		}

		private void internalForward(final float @NonNull [] inRe,
				final float @NonNull [] inIm, final float @NonNull [] outRe,
				final float @NonNull [] outIm) {
			final int fftLen = this.fft.getSize();
			assert inRe.length == fftLen && inIm.length == fftLen;
			assert outRe.length == fftLen && outIm.length == fftLen;

			final int numBins = rows.length;

			fft.forward(inRe, inIm);
			fft.copyReal(transformedRe);
			fft.copyImaginary(transformedIm);

			for (int k = 0; k < numBins; k++) {
				final float[] dotProduct = rows[k].dotProduct(transformedRe,
						transformedIm);
				outRe[k] = dotProduct[0];
				outRe[k] = dotProduct[1];

				// Normalisiere
				outRe[k] /= fftLen;
				outIm[k] /= fftLen;
			}
		}
	}

	FastConstantQTransform(final double minFreq, final double maxFreq,
			final int binsPerOctave, final double sampleRate) {
		checkArguments(minFreq, maxFreq, binsPerOctave, sampleRate);

		kernel = new Kernel(binsPerOctave, minFreq, maxFreq, sampleRate);
		this.binsPerOctave = binsPerOctave;
		this.minFreq = minFreq;
		final int numBins = calcNumBins(binsPerOctave, minFreq, maxFreq);
		this.sampleRate = sampleRate;

		real = new float[numBins];
		imaginary = new float[numBins];
	}

	private static void checkArguments(final double minFreq,
			final double maxFreq, final int binsPerOctave,
			final double sampleRate) {
		checkFreq(minFreq, "minFreq");
		if (minFreq > maxFreq) {
			throw new IllegalArgumentException("minFreq " + minFreq
					+ " > maxFreq " + maxFreq);
		}
		checkBinPerOctave(binsPerOctave);
		checkSampleRate(sampleRate);
	}

	private static void checkFreq(final double freq, final @NonNull String name) {
		if (freq <= 0) {
			throw new IllegalArgumentException(name + " " + freq + " <= 0");
		}
	}

	private static void checkBinPerOctave(final int binsPerOcatve) {
		if (binsPerOcatve <= 1) {
			throw new IllegalArgumentException("binPerOcatve " + binsPerOcatve
					+ " <= 0");
		}
	}

	private static void checkSampleRate(final double sampleRate) {
		checkFreq(sampleRate, "sampleRate");
	}

	public void forward(final float @NonNull [] samples) {
		kernel.forward(samples, real, imaginary);
	}

	public void forward(final float @NonNull [] real,
			final float @NonNull [] imaginary) {
		kernel.forward(real, imaginary, this.real, this.imaginary);
	}

	public double getSampleRate() {
		return sampleRate;
	}

	public int getSpectrumSize() {
		return real.length;
	}

	public double binToFrequency(final int index) {
		if (index < 0 || index >= real.length) {
			throw new IllegalArgumentException("index " + index
					+ " is out of range 0.." + (real.length - 1));
		}

		return calcFrequency(index, minFreq, binsPerOctave);
	}

	public int frequencyToBin(final double frequency) {
		final double maxFreq = binToFrequency(real.length - 1);
		if (frequency < minFreq || frequency > maxFreq) {
			throw new IllegalArgumentException("frequency " + frequency
					+ " is out of range " + minFreq + ".." + maxFreq);
		}

		return (int) Math.round(binsPerOctave * Util.log2(frequency / minFreq));
	}

	// Berechnet das Betragsspektrum
	public void fillMagnitudeSpectrum(final float @NonNull [] spectrum) {
		computeMagnitudeSpectrum(real, imaginary, spectrum);
	}

	public static void computeMagnitudeSpectrum(final float @NonNull [] re,
			final float @NonNull [] im, final float @NonNull [] spectrum) {
		for (int k = 0; k < re.length; k++) {
			spectrum[k] = (float) Math.sqrt(re[k] * re[k] + im[k] * im[k]);
		}
	}

	public void copyReal(final float[] buffer) {
		if (buffer.length < imaginary.length) {
			throw new IllegalArgumentException("buffer length " + buffer.length
					+ " < spectrum size " + real.length);
		}
		System.arraycopy(real, 0, buffer, 0, real.length);
	}

	public void copyImaginary(final float[] buffer) {
		if (buffer.length < imaginary.length) {
			throw new IllegalArgumentException("buffer length " + buffer.length
					+ " < spectrum size " + imaginary.length);
		}
		System.arraycopy(imaginary, 0, buffer, 0, imaginary.length);
	}

	/**
	 * Erstellt die Constant-Q-Transformation so, dass das volle Spektrum
	 * berechnet wird. Das Spektrum hat folgende Eigenschaften:
	 * <ul>
	 * <li>Min. Frequenz f0 = Q*fs/n0</li>
	 * <li>Max. Frequenz fmax = fs/2 (Nyquist-Frequenz)</li>
	 * <li>Anzahl Bins N = ceil(b*log2(n0/2Q))
	 * </ul>
	 * mit
	 * <ul>
	 * <li>Quality-Factor Q = 1/(2^(1/b) - 1)</li>
	 * <li>Anzahl Bins pro Oktave b</li>
	 * <li>Samplerate fs</li>
	 * <li>Fenstergröße des nullten Bins n0</li>
	 * </ul>
	 * 
	 * @param size
	 *            Blocklänge
	 * @param binsPerOctave
	 *            Anzahl der Bins pro Oktave, z. B. 12 für die chromatische
	 *            Tonleiter
	 * @param sampleRate
	 *            Abtastrate in Hz mit der die Samples des Blocks abgetastet
	 *            wurden, z. B. 44100 Hz.
	 * @return
	 */
	public static FastConstantQTransform createFullSpectrum(final int size,
			final int binsPerOctave, final double sampleRate) {
		if (size <= 0) {
			throw new IllegalArgumentException("size " + size + " <= 0");
		}

		final double q = calcQ(binsPerOctave);
		final double minFreq = calcMinFreq(size, sampleRate, q);

		// Nyquist-Frequenz
		final double maxFreq = calcMaxFreq(sampleRate);

		return new FastConstantQTransform(minFreq, maxFreq, binsPerOctave,
				sampleRate);
	}

	public static double binToFrequency(final int bin, final double minFreq,
			final int binsPerOctave) {
		if (bin < 0) {
			throw new IllegalArgumentException("bin " + bin + " < 0");
		}
		checkFreq(minFreq, "minFreq");
		checkBinPerOctave(binsPerOctave);

		return calcFrequency(bin, minFreq, binsPerOctave);
	}

	protected static double calcMinFreq(final int size,
			final double sampleRate, final double q) {
		// Gegegeben: Fenstergröße für das größte Fenster n0 = ceil(Q*fs/f0).
		// Gesucht: Kleinste Frequenz f0
		// Lösung: f0 \in [Q*fs/n0, Q*fs/(n0-1)).
		// Beweis:
		// 1) Ungleichung für ceil: n = ceil(x) <==> n -1 < x <= n
		// 2) Eingesetzt: n0 - 1 < Q*fs/f0 <= n0
		// 3) Ist f streng-monoton-fallend, dann gilt: a <= b <==> f(a) >= f(b)
		// 3.1) Kehrwert ist für positive Zahlen streng-monoton fallend
		// 3.2) n0 - 1 und n0 sind positiv, wegen Exception bei size <= 0
		// 3.3) Q*fs/f0 ist positiv, weil Q, fs und f0 positiv sind
		// 4) Kehrwert: 1/(n0 - 1) > f0/(Q*fs) >= 1/n0
		// 5) Mit Q*fs multipliziert: Q*fs/(n0 - 1) > f0 >= Q*fs/n0
		final double minFreq = (q * sampleRate) / size;
		return minFreq;
	}

	protected static double calcMaxFreq(final double sampleRate) {
		return sampleRate / 2;
	}

	protected static double calcQ(final int binsPerOctave) {
		final double q = 1 / (Math.pow(2, 1d / binsPerOctave) - 1);
		return q;
	}

	protected static int calcNumBins(final int binsPerOctave,
			final double minFreq, final double maxFreq) {
		final int numBins = (int) Math.ceil(binsPerOctave
				* Util.log2(maxFreq / minFreq));
		return numBins;
	}

	protected static double calcFrequency(final int k, final double minFreq,
			final int binsPerOctave) {
		final double freq = minFreq
				* Math.pow(2, (double) k / (double) binsPerOctave);
		return freq;
	}

	protected static int calcWindowSize(final double q,
			final double sampleRate, final double freq) {
		final int n = (int) Math.ceil(q * sampleRate / freq);
		return n;
	}
}
