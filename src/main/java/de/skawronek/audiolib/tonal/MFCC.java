package de.skawronek.audiolib.tonal;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.Feature;
import de.skawronek.audiolib.FeatureFactory;
import de.skawronek.audiolib.FeatureKey;
import de.skawronek.audiolib.FeatureProcessor;
import de.skawronek.audiolib.Frame;
import de.skawronek.audiolib.signal.FastFourierTransform;

public final class MFCC extends Feature {
	final float[] coeffs;

	public final static class Processor extends FeatureProcessor<MFCC> {
		@Override
		public @NonNull Class<MFCC> getFeatureClass() {
			return MFCC.class;
		}

		@Override
		public MFCC process(@NonNull final Frame frame,
				@NonNull final FeatureKey<MFCC> featureKey) {
			final FastFourierTransform fft = frame
					.getFeature(FastFourierTransform.getKey());
			final float[] magSpec = fft.getMagnitudeSpectrum();

			final Key key = (Key) featureKey;
			//@formatter:off
			final de.skawronek.audiolib.math.MFCC mfcc = 
					de.skawronek.audiolib.math.MFCC.Factory.getInstance().get(
							frame.getSize(),
							frame.getSampleRate(),
							key.minFreq,
							key.maxFreq,
							key.numFilterbanks,
							key.numCepstralCoefficients);
			//@formatter:on
			mfcc.compute(magSpec);
			final float[] coeffs = new float[key.numCepstralCoefficients];
			mfcc.copyCoefficients(coeffs);

			return new MFCC(coeffs);
		}
	}

	private MFCC(final float @NonNull [] coeffs) {
		this.coeffs = coeffs;
	}

	public float @NonNull [] getCoefficents() {
		return coeffs;
	}

	public final static class Key extends FeatureKey<MFCC> {
		private final double minFreq;
		private final double maxFreq;
		private final int numFilterbanks;
		private final int numCepstralCoefficients;

		private Key(double minFreq, double maxFreq, int numFilterbanks,
				int numCepstralCoefficients) {
			if (minFreq <= 0) {
				throw new IllegalArgumentException("minFreq " + minFreq
						+ " <= 0");
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

			this.minFreq = minFreq;
			this.maxFreq = maxFreq;
			this.numFilterbanks = numFilterbanks;
			this.numCepstralCoefficients = numCepstralCoefficients;
		}

		@Override
		public @NonNull Class<MFCC> getFeatureClass() {
			return MFCC.class;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null) {
				return false;
			} else if (obj == this) {
				return true;
			} else if (obj instanceof Key) {
				final Key other = (Key) obj;
				//@formatter:off
				return Double.compare(this.minFreq, other.minFreq) == 0
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
			return Double.hashCode(minFreq)
					^ Double.hashCode(maxFreq)
					^ numFilterbanks
					^ numCepstralCoefficients;
			//@formatter:on
		}
	}

	private static final Key DEFAULT_KEY = new Key(
			de.skawronek.audiolib.math.MFCC.DEFAULT_MIN_FREQ,
			de.skawronek.audiolib.math.MFCC.DEFAULT_MAX_FREQ,
			de.skawronek.audiolib.math.MFCC.DEFAULT_NUM_FILTERBANKS,
			de.skawronek.audiolib.math.MFCC.DEFAULT_NUM_CEPSTRUM_COEFFICIENTS);

	public static Key getDefaultKey() {
		return DEFAULT_KEY;
	}

	public static Key getKey(double minFreq, double maxFreq,
			int numFilterbanks, int numCepstralCoefficients) {
		return new Key(minFreq, maxFreq, numFilterbanks,
				numCepstralCoefficients);
	}

	public static void register() {
		final Processor processor = new Processor();
		FeatureFactory.getInstance().registerProcessor(processor);
	}
}
