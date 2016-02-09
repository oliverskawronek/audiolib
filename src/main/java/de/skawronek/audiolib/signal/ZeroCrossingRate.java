package de.skawronek.audiolib.signal;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.Feature;
import de.skawronek.audiolib.FeatureFactory;
import de.skawronek.audiolib.FeatureKey;
import de.skawronek.audiolib.FeatureProcessor;
import de.skawronek.audiolib.Frame;

public final class ZeroCrossingRate extends Feature {
	private final int numCrossings;
	private final int frameSize;
	private final double sampleRate;

	public final static class Processor extends
			FeatureProcessor<ZeroCrossingRate> {
		@Override
		public @NonNull Class<ZeroCrossingRate> getFeatureClass() {
			return ZeroCrossingRate.class;
		}

		@Override
		public ZeroCrossingRate process(@NonNull final Frame frame,
				@NonNull final FeatureKey<ZeroCrossingRate> key) {
			final float[] window = frame.getMonoSamples();
			final int frameSize = frame.getSize();

			int crossingCount = 0;
			for (int i = 0; i < frameSize - 1; i++) {
				//@formatter:off
				/*
				 * Bedingung für ein Nulldurchgang im Fenster w zum
				 * Zeitpunkt i:
				 * 1. w[i] >= 0 UND w[i + 1] < 0 oder
				 * 2. w[i] < 0 UND w[i + 1] >= 0
				 */
				//@formatter:on
				if ((window[i] >= 0.0f && window[i + 1] < 0.0f)
						|| (window[i] < 0.0f && window[i + 1] >= 0.0f)) {
					crossingCount++;
				}
			}

			return new ZeroCrossingRate(crossingCount, frameSize,
					frame.getSampleRate());
		}
	}

	private ZeroCrossingRate(final int numCount, final int frameSize,
			final double sampleRate) {
		this.numCrossings = numCount;
		this.frameSize = frameSize;
		this.sampleRate = sampleRate;
	}

	public double getRate() {
		return (double) numCrossings / (double) frameSize;
	}

	public int getNumCrossings() {
		return numCrossings;
	}

	public double getFrequency() {
		// Periodenzeit der Grundfrequenz in Sekunden
		final double t0 = (2 * frameSize) / (sampleRate * numCrossings);

		return 1 / t0;
	}

	public final static class Key extends FeatureKey<ZeroCrossingRate> {
		@Override
		public @NonNull Class<ZeroCrossingRate> getFeatureClass() {
			return ZeroCrossingRate.class;
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
