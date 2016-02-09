package de.skawronek.audiolib.signal;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.Feature;
import de.skawronek.audiolib.FeatureFactory;
import de.skawronek.audiolib.FeatureKey;
import de.skawronek.audiolib.FeatureProcessor;
import de.skawronek.audiolib.Frame;
import de.skawronek.audiolib.math.FastAutocorrelation;

public final class Autocorrelation extends Feature {
	private final float[] coefficients;

	public final static class Processor extends
			FeatureProcessor<Autocorrelation> {
		@Override
		public @NonNull Class<Autocorrelation> getFeatureClass() {
			return Autocorrelation.class;
		}

		@Override
		public Autocorrelation process(@NonNull final Frame frame,
				@NonNull final FeatureKey<Autocorrelation> key) {
			
			final float[] window = frame.getMonoSamples();
			final Key featureKey = (Key) key;
			final FastAutocorrelation fa = FastAutocorrelation.Factory
					.getInstance().get(window.length);
			fa.compute(window);
			if (featureKey.normalize) {
				fa.normalize();
			}
			final float[] coeffs = new float[window.length];
			fa.copyCoefficients(coeffs);
			return new Autocorrelation(coeffs);
		}
	}

	private Autocorrelation(final float @NonNull [] coeffs) {
		this.coefficients = coeffs;
	}

	public float[] getCoefficients() {
		return coefficients;
	}

	public final static class Key extends FeatureKey<Autocorrelation> {
		private final boolean normalize;

		private Key(final boolean normalize) {
			this.normalize = normalize;
		}

		@Override
		public @NonNull Class<Autocorrelation> getFeatureClass() {
			return Autocorrelation.class;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null) {
				return false;
			} else if (obj == this) {
				return true;
			} else if (obj instanceof Key) {
				final Key other = (Key) obj;
				return this.normalize == other.normalize;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return 0;
		}
	}

	private static final Key NORMALIZE_KEY = new Key(true);
	private static final Key NOT_NORMALIZE_KEY = new Key(false);

	public static Key getDefaultKey() {
		return NORMALIZE_KEY;
	}

	public static Key getKey(final boolean normalize) {
		if (normalize) {
			return NORMALIZE_KEY;
		} else {
			return NOT_NORMALIZE_KEY;
		}
	}

	public static void register() {
		final Processor processor = new Processor();
		FeatureFactory.getInstance().registerProcessor(processor);
	}
}
