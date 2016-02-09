package de.skawronek.audiolib.signal;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.Feature;
import de.skawronek.audiolib.FeatureFactory;
import de.skawronek.audiolib.FeatureKey;
import de.skawronek.audiolib.FeatureProcessor;
import de.skawronek.audiolib.Frame;

public final class Statistics extends Feature {
	private final de.skawronek.audiolib.math.Statistics statistics = new de.skawronek.audiolib.math.Statistics();

	public final static class Processor extends FeatureProcessor<Statistics> {
		@Override
		public @NonNull Class<Statistics> getFeatureClass() {
			return Statistics.class;
		}

		@Override
		public Statistics process(@NonNull final Frame frame,
				@NonNull final FeatureKey<Statistics> key) {
			final float[] window = frame.getMonoSamples();
			return new Statistics(window);
		}
	}

	private Statistics(final float @NonNull [] window) {
		statistics.setInput(window);
	}

	public float getMinimum() {
		return statistics.getMininum();
	}

	public float getMaximum() {
		return statistics.getMaximum();
	}

	public float getAverage() {
		return statistics.getAverage();
	}

	public float getMedian() {
		return statistics.getMedian();
	}

	public final static class Key extends FeatureKey<Statistics> {
		@Override
		public @NonNull Class<Statistics> getFeatureClass() {
			return Statistics.class;
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
