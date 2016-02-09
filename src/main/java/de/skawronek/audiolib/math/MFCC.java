package de.skawronek.audiolib.math;

import static de.skawronek.audiolib.util.Util.clamp;
import static java.lang.Math.exp;
import static java.lang.Math.log;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.util.AbstractFactory;

public final class MFCC {
	private static final class Index {
		private final int windowSize;
		private final double sampleRate;
		private final double minFreq;
		private final double maxFreq;
		private final int numFilterbanks;
		private final int numCepstralCoefficients;

		private Index(final int windowSize, final double sampleRate,
				final double minFreq, final double maxFreq,
				final int numFilterbanks, final int numCepstralCoefficients) {
			this.windowSize = windowSize;
			this.sampleRate = sampleRate;
			this.minFreq = minFreq;
			this.maxFreq = maxFreq;
			this.numFilterbanks = numFilterbanks;
			this.numCepstralCoefficients = numCepstralCoefficients;
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
				return this.windowSize == other.windowSize
						&& Double.compare(this.sampleRate, other.sampleRate) == 0
						&& Double.compare(this.minFreq, other.minFreq) == 0
						&& Double.compare(this.maxFreq, other.maxFreq) == 0
						&& this.numFilterbanks == other.numFilterbanks
						&& this.numCepstralCoefficients == other.numCepstralCoefficients;
				//@formatter:on
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			//@formatter:off
			return windowSize
					^ Double.hashCode(sampleRate)
					^ Double.hashCode(minFreq)
					^ Double.hashCode(maxFreq)
					^ numFilterbanks
					^ numCepstralCoefficients;
			//@formatter:on
		}
	}

	public static final double DEFAULT_MIN_FREQ = 300; // [Hz]
	public static final double DEFAULT_MAX_FREQ = 8000; // [Hz]
	public static final int DEFAULT_NUM_FILTERBANKS = 26; // Typisch 20--40
	public static final int DEFAULT_NUM_CEPSTRUM_COEFFICIENTS = 13; // Typisch
																	// 12--13

	public static final class Factory extends AbstractFactory<Index, MFCC> {
		private static final Factory INSTANCE = new Factory();

		private Factory() {
		}

		public static Factory getInstance() {
			return INSTANCE;
		}

		public @NonNull MFCC get(final int windowSize, final double sampleRate,
				final double minFreq, final double maxFreq,
				final int numFilterbanks, final int numCepstralCoefficients) {
			checkArguments(windowSize, sampleRate, minFreq, maxFreq,
					numFilterbanks, numCepstralCoefficients);
			final Index index = new Index(windowSize, sampleRate, minFreq,
					maxFreq, numFilterbanks, numCepstralCoefficients);
			return super.get(index);
		}

		public @NonNull MFCC get(final int windowSize, final double sampleRate) {
			return get(windowSize, sampleRate, DEFAULT_MIN_FREQ,
					DEFAULT_MAX_FREQ, DEFAULT_NUM_FILTERBANKS,
					DEFAULT_NUM_CEPSTRUM_COEFFICIENTS);
		}

		@Override
		protected @NonNull MFCC create(@NonNull Index index) {
			return new MFCC(index.windowSize, index.sampleRate, index.minFreq,
					index.maxFreq, index.numFilterbanks,
					index.numCepstralCoefficients);
		}
	}

	private final int windowSize;
	private final int[] filterbankIndices;
	private final float[] powerSpectrum;
	private final float[] logBankEnergies;
	private final float[][] dctMatrix;
	private final float[] coeffs;

	public MFCC(int windowSize, double sampleRate, double minFreq,
			double maxFreq, int numFilterbanks, int numCepstralCoefficients) {
		checkArguments(windowSize, sampleRate, minFreq, maxFreq,
				numFilterbanks, numCepstralCoefficients);

		this.windowSize = windowSize;
		this.filterbankIndices = computeFilterbankIndices(windowSize,
				sampleRate, numFilterbanks, minFreq, maxFreq);
		this.powerSpectrum = new float[windowSize / 2];
		this.logBankEnergies = new float[numFilterbanks];
		this.dctMatrix = DctMatrixGenerator.generate(numFilterbanks);
		this.coeffs = new float[numCepstralCoefficients];
	}

	private static int[] computeFilterbankIndices(final int windowSize,
			final double sampleRate, final int numFilterbanks,
			final double minFreq, final double maxFreq) {
		if (maxFreq > sampleRate / 2) {
			throw new IllegalArgumentException();
		}

		// Jede Filterbbank hat einen Start-, Peak- und End-Punkt für den
		// dreieckigen Filter
		final int numPoints = numFilterbanks + 2;
		final double lowerMel = frequencyToMel(minFreq);
		final double upperMel = frequencyToMel(maxFreq);
		final double linSpace = (upperMel - lowerMel) / (numPoints - 1);
		final int indices[] = new int[numPoints];

		final int minBin = 0; // inkl.
		final int maxBin = windowSize / 2; // inkl.
		for (int i = 0; i < numPoints; i++) {
			final double mel = lowerMel + i * linSpace;
			final double freq = melToFrequency(mel);
			final int bin = clamp(FastFourierTransform.frequencyToBin(freq,
					windowSize, sampleRate), minBin, maxBin);
			indices[i] = bin;
		}

		return indices;
	}

	public void compute(final float @NonNull [] magSpectrum) {
		computePowerSpectrum(magSpectrum);
		computeLogBankEnergies();
		computeDiscreteCosineTransform();
	}

	private void computePowerSpectrum(final float @NonNull [] magSpectrum) {
		for (int k = 0; k < windowSize / 2; k++) {
			powerSpectrum[k] = (magSpectrum[k] * magSpectrum[k]) / windowSize;
		}
	}

	private void computeLogBankEnergies() {
		final int numFilterbanks = getNumFilterbanks();
		for (int i = 0; i < numFilterbanks; i++) {
			final int startIndex = filterbankIndices[i]; // inkl.
			final int midIndex = filterbankIndices[i + 1]; // inkl.
			final int endIndex = filterbankIndices[i + 2]; // inkl.

			// Linearer Dreiecks-Filter (startIndex, midIndex, endIndex)
			logBankEnergies[i] = 0;
			for (int k = startIndex; k < midIndex; k++) {
				final float scale = ((float) (k - startIndex))
						/ ((float) (midIndex - startIndex));
				logBankEnergies[i] += (scale * powerSpectrum[k]);
			}
			for (int k = midIndex; k <= endIndex; k++) {
				final float scale = 1 - ((float) (k - endIndex))
						/ ((float) (midIndex - startIndex));
				logBankEnergies[i] += (scale * powerSpectrum[k]);
			}

			// Logarithmus
			logBankEnergies[i] = (float) log(logBankEnergies[i]);
		}
	}

	private void computeDiscreteCosineTransform() {
		final int numCepstrumCoefficients = getNumCepstrumCoefficients();
		final int numFilterbanks = getNumFilterbanks();
		for (int k = 0; k < numCepstrumCoefficients; k++) {
			// Berechne Punktprdukt aus k-ten Spaltenvektor der DCT-Matrix
			// und dem Zeilenvektor mit den log. Engergien der Filterbänke.
			coeffs[k] = 0f;
			for (int n = 0; n < numFilterbanks; n++) {
				coeffs[k] += logBankEnergies[n] * dctMatrix[n][k];
			}
		}
	}

	public int getNumFilterbanks() {
		return filterbankIndices.length - 2;
	}

	public int getNumCepstrumCoefficients() {
		return coeffs.length;
	}

	public void copyCoefficients(final float @NonNull [] dest) {
		int numCoeffs = getNumCepstrumCoefficients();
		if (dest.length < numCoeffs) {
			throw new IllegalArgumentException("dest length " + dest.length
					+ " < num coeffs " + numCoeffs);
		}

		System.arraycopy(coeffs, 0, dest, 0, numCoeffs);
	}

	public static double frequencyToMel(final double freq) {
		return 1225 * log(1 + freq / 700);
	}

	public static double melToFrequency(final double mel) {
		return 700 * (exp(mel / 1125) - 1);
	}

	private static void checkArguments(final int windowSize,
			final double sampleRate, final double minFreq,
			final double maxFreq, final int numFilterbanks,
			final int numCepstralCoefficients) {
		if (windowSize <= 0) {
			throw new IllegalArgumentException("windowSize " + windowSize
					+ " <= 0");
		} else if (sampleRate <= 0) {
			throw new IllegalArgumentException("sampleRate " + sampleRate
					+ " <= 0");
		} else if (minFreq <= 0) {
			throw new IllegalArgumentException("minFreq " + minFreq + " <= 0");
		} else if (minFreq >= maxFreq) {
			throw new IllegalArgumentException("minFreq " + minFreq
					+ " >= maxFreq " + maxFreq);
		} else if (numFilterbanks < 1) {
			throw new IllegalArgumentException("numFilterBanks "
					+ numFilterbanks + " < 1");
		} else if (numCepstralCoefficients > numFilterbanks) {
			throw new IllegalArgumentException("numCepstralCoefficients "
					+ numCepstralCoefficients + " > numFilterbanks "
					+ numFilterbanks);
		}
	}
}
