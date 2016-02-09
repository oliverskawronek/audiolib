package de.skawronek.audiolib.signal;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.Feature;
import de.skawronek.audiolib.FeatureFactory;
import de.skawronek.audiolib.FeatureKey;
import de.skawronek.audiolib.FeatureProcessor;
import de.skawronek.audiolib.Frame;
import de.skawronek.audiolib.math.FastConstantQTransform;

public final class ConstantQTransform extends Feature {
	private final float[] real;
	private final float[] imaginary;
	private float[] spectrum;

	public final static class Processor extends
			FeatureProcessor<ConstantQTransform> {
		@Override
		public @NonNull Class<ConstantQTransform> getFeatureClass() {
			return ConstantQTransform.class;
		}

		@Override
		public ConstantQTransform process(@NonNull final Frame frame,
				@NonNull final FeatureKey<ConstantQTransform> featureKey) {
			final float[] window = frame.getMonoSamples();
			final Key key = (Key) featureKey;

			//@formatter:off
			final de.skawronek.audiolib.math.FastConstantQTransform cqt =
					de.skawronek.audiolib.math.FastConstantQTransform.Factory
					.getInstance().get(
							key.minFreq,
							key.maxFreq,
							key.numBinsPerOctave,
							frame.getSampleRate());
			//@formatter:on

			cqt.forward(window);
			final float[] real = new float[cqt.getSpectrumSize()];
			final float[] imaginary = new float[cqt.getSpectrumSize()];
			cqt.copyReal(real);
			cqt.copyImaginary(imaginary);
			return new ConstantQTransform(real, imaginary);
		}
	}

	private ConstantQTransform(final float @NonNull [] real,
			final float @NonNull [] imaginary) {
		this.real = real;
		this.imaginary = imaginary;
	}

	public float @NonNull [] getReal() {
		return real;
	}

	public float @NonNull [] getImaginary() {
		return imaginary;
	}

	public float @NonNull [] getMagnitudeSpectrum() {
		if (spectrum == null) {
			spectrum = new float[real.length];
			FastConstantQTransform.computeMagnitudeSpectrum(real, imaginary,
					spectrum);
		}

		return spectrum;
	}

	public final static class Key extends FeatureKey<ConstantQTransform> {
		private final double minFreq;
		private final double maxFreq;
		private final int numBinsPerOctave;

		private Key(final double minFreq, final double maxFreq,
				final int numBinsPerOctave) {
			if (minFreq <= 0) {
				throw new IllegalArgumentException("minFreq " + minFreq
						+ " <= 0");
			} else if (minFreq >= maxFreq) {
				throw new IllegalArgumentException("minFreq " + minFreq
						+ " >= maxFreq " + maxFreq);
			} else if (numBinsPerOctave <= 0) {
				throw new IllegalArgumentException("numBinsPerOctave "
						+ numBinsPerOctave + " <= 0");
			}

			this.minFreq = minFreq;
			this.maxFreq = maxFreq;
			this.numBinsPerOctave = numBinsPerOctave;
		}

		@Override
		public @NonNull Class<ConstantQTransform> getFeatureClass() {
			return ConstantQTransform.class;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null) {
				return false;
			} else if (obj == this) {
				return true;
			} else if (obj instanceof Key) {
				final Key other = (Key) obj;
				return Double.compare(this.minFreq, other.minFreq) == 0
						&& Double.compare(this.maxFreq, other.maxFreq) == 0
						&& this.numBinsPerOctave == other.numBinsPerOctave;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return Double.hashCode(minFreq) ^ Double.hashCode(maxFreq)
					^ numBinsPerOctave;
		}
	}

	public static @NonNull Key getKey(final double minFreq,
			final double maxFreq, final int numBinPerOctave) {
		return new Key(minFreq, maxFreq, numBinPerOctave);
	}

	public static void register() {
		final Processor processor = new Processor();
		FeatureFactory.getInstance().registerProcessor(processor);
	}
}
