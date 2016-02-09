package de.skawronek.audiolib.tonal;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.Feature;
import de.skawronek.audiolib.FeatureFactory;
import de.skawronek.audiolib.FeatureKey;
import de.skawronek.audiolib.FeatureProcessor;
import de.skawronek.audiolib.Frame;
import de.skawronek.audiolib.math.LinearPredictor;
import de.skawronek.audiolib.signal.Autocorrelation;

public final class Tonalness extends Feature {
	private final float tonalness;

	public final static class Processor extends FeatureProcessor<Tonalness> {
		@Override
		public @NonNull Class<Tonalness> getFeatureClass() {
			return Tonalness.class;
		}

		@Override
		public Tonalness process(final @NonNull Frame frame,
				@NonNull final FeatureKey<Tonalness> featureKey) {
			// 1/4 einer 65 Hz Schwingung
			final int order = (int) (frame.getSampleRate() / 441d);
			final float[] x = frame.getMonoSamples();

			final LinearPredictor predictor = new LinearPredictor(order);
			// Vorberechnete Autokorrelation vorhanden?
			final de.skawronek.audiolib.signal.Autocorrelation.Key acfKey = Autocorrelation
					.getKey(false);
			if (frame.containsFeature(acfKey)) {
				final Autocorrelation acf = frame.getFeature(acfKey);
				final float[] r = acf.getCoefficients();
				predictor.computeCoefficients(x, r);
			} else {
				// Autkorrelation berechnet LinearPredictor selber
				predictor.computeCoefficients(x);
			}

			float predictionError = 0f;
			for (int i = order, n = frame.getSize(); i < n; i++) {
				final float diff = x[i] - predictor.predict(x, i);
				predictionError += (diff * diff);
			}
			float energy = 0f;
			for (int i = order, n = frame.getSize(); i < n; i++) {
				energy += (x[i] * x[i]);
			}

			final double tonalness = Math.sqrt(predictionError / energy);
			return new Tonalness((float) tonalness);
		}
	}

	private Tonalness(final float tonalness) {
		this.tonalness = tonalness;
	}

	public float getTonalness() {
		return tonalness;
	}

	public final static class Key extends FeatureKey<Tonalness> {
		private Key() {
		}

		@Override
		public @NonNull Class<Tonalness> getFeatureClass() {
			return Tonalness.class;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null) {
				return false;
			} else if (obj == this) {
				return true;
			} else if (obj instanceof Key) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return 0;
		}
	}

	private static final Key DEFAULT_KEY = new Key();

	public static Key getKey() {
		return DEFAULT_KEY;
	}

	public static void register() {
		final Processor processor = new Processor();
		FeatureFactory.getInstance().registerProcessor(processor);
	}
}
