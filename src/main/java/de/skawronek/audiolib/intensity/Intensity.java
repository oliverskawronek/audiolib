package de.skawronek.audiolib.intensity;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.Feature;
import de.skawronek.audiolib.FeatureFactory;
import de.skawronek.audiolib.FeatureKey;
import de.skawronek.audiolib.FeatureProcessor;
import de.skawronek.audiolib.Frame;
import de.skawronek.audiolib.math.RootMeanSquare;
import de.skawronek.audiolib.math.WeightingFilters;
import de.skawronek.audiolib.math.WeightingFilters.IWeightingFilter;

public final class Intensity extends Feature {
	private final double intensity;

	public static enum Method {
		RMS, A_WEIGHTING, B_WEIGHTING, C_WEIGHTING, PEAK_ENVELOPE_MAX
	}

	public final static class Processor extends FeatureProcessor<Intensity> {
		@Override
		public @NonNull Class<Intensity> getFeatureClass() {
			return Intensity.class;
		}

		@Override
		public @NonNull Intensity process(@NonNull final Frame frame,
				@NonNull final FeatureKey<Intensity> featureKey) {

			final Key key = (Key) featureKey;

			final double intensity;
			switch (key.method) {
			case RMS:
				intensity = computeIntensityByRms(frame);
				break;
			case A_WEIGHTING:
			case B_WEIGHTING:
			case C_WEIGHTING:
				intensity = computeIntensityByWeighting(frame, key.method);
				break;
			case PEAK_ENVELOPE_MAX:
				intensity = computeIntensityByPeakEnvelope(frame);
				break;
			default:
				throw new AssertionError("Unexpected method " + key.method);
			}

			return new Intensity(intensity);
		}

		private double computeIntensityByRms(@NonNull final Frame frame) {
			final float[] window = frame.getMonoSamples();
			return RootMeanSquare.compute(window);
		}

		private double computeIntensityByWeighting(@NonNull final Frame frame,
				Method method) {
			final IWeightingFilter filter;

			switch (method) {
			case A_WEIGHTING:
				filter = WeightingFilters.getAWeighting();
				break;
			case B_WEIGHTING:
				filter = WeightingFilters.getBWeighting();
				break;
			case C_WEIGHTING:
				filter = WeightingFilters.getCWeighting();
				break;
			default:
				throw new AssertionError("Unexpected method " + method);
			}

			de.skawronek.audiolib.signal.FastFourierTransform fft = frame
					.getFeature(de.skawronek.audiolib.signal.FastFourierTransform
							.getKey());
			float[] spectrum = fft.getMagnitudeSpectrum();
			return RootMeanSquare.computeWithWeightingFilter(spectrum,
					frame.getSize(), frame.getSampleRate(), true, filter);
		}

		private static double computeIntensityByPeakEnvelope(
				final @NonNull Frame frame) {
			final float[] window = frame.getMonoSamples();
			float max = -Float.MAX_VALUE;
			for (int i = 0; i < window.length; i++) {
				float abs = Math.abs(window[i]);
				if (abs > max) {
					max = abs;
				}
			}
			return max;
		}
	}

	private Intensity(final double intensity) {
		this.intensity = intensity;
	}

	/**
	 * Gibt die Intensität I zurück. Für I gilt I \in [0, 1]
	 * 
	 * @return Intensität I
	 */
	public double getIntensity() {
		return intensity;
	}

	/**
	 * Decibels relativ zur FullScale (dB FS). FullScale bezieht sich auf die
	 * Intensität eines Rechteck-Singals (überall 1).
	 * 
	 * @return Decibel relativ zur FullScale.
	 */
	public double getDecibelFullScale() {
		final double fullScale = 1;
		return 20 * Math.log10(intensity / fullScale);
	}

	public final static class Key extends FeatureKey<Intensity> {
		private final Method method;

		private Key(final @NonNull Method method) {
			this.method = method;
		}

		@Override
		public @NonNull Class<Intensity> getFeatureClass() {
			return Intensity.class;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null) {
				return false;
			} else if (obj == this) {
				return true;
			} else if (obj instanceof Key) {
				final Key other = (Key) obj;
				return this.method == other.method;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			int hashCode = method.hashCode();
			return hashCode;
		}
	}

	private static final Key DEFAULT_KEY = new Key(Method.RMS);

	public static @NonNull Key getDefaultKey() {
		return DEFAULT_KEY;
	}

	public static @NonNull Key getKey(final @NonNull Method method) {
		return new Key(method);
	}

	public static void register() {
		final Processor processor = new Processor();
		FeatureFactory.getInstance().registerProcessor(processor);
	}
}
