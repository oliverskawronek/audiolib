package de.skawronek.audiolib.signal;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.Feature;
import de.skawronek.audiolib.FeatureFactory;
import de.skawronek.audiolib.FeatureKey;
import de.skawronek.audiolib.FeatureProcessor;
import de.skawronek.audiolib.Frame;

public final class HarmonicProductSpectrum extends Feature {
	private final float[] hps;

	public final static class Processor extends
			FeatureProcessor<HarmonicProductSpectrum> {
		@Override
		public @NonNull Class<HarmonicProductSpectrum> getFeatureClass() {
			return HarmonicProductSpectrum.class;
		}

		@Override
		public HarmonicProductSpectrum process(@NonNull final Frame frame,
				@NonNull final FeatureKey<HarmonicProductSpectrum> key) {
			final float[] window = frame.getMonoSamples();
			final de.skawronek.audiolib.math.HarmonicProductSpectrum hps = de.skawronek.audiolib.math.HarmonicProductSpectrum.Factory
					.getInstance().get(window.length, ((Key) key).numHarmoncis);
			final FastFourierTransform fft = frame
					.getFeature(FastFourierTransform.getKey());
			hps.compute(fft.getMagnitudeSpectrum());
			final float[] buffer = new float[hps.getSize()];
			hps.copyHps(buffer);
			return new HarmonicProductSpectrum(buffer);
		}
	}

	private HarmonicProductSpectrum(final float @NonNull [] hps) {
		this.hps = hps;
	}

	public float @NonNull [] getHps() {
		return hps;
	}

	public final static class Key extends FeatureKey<HarmonicProductSpectrum> {
		private final int numHarmoncis;

		private Key(final int numHarmonics) {
			if (numHarmonics < 1) {
				throw new IllegalArgumentException("numHarmonics "
						+ numHarmonics + " < 1");
			}
			this.numHarmoncis = numHarmonics;
		}

		@Override
		public @NonNull Class<HarmonicProductSpectrum> getFeatureClass() {
			return HarmonicProductSpectrum.class;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null) {
				return false;
			} else if (obj == this) {
				return true;
			} else if (obj instanceof Key) {
				final Key other = (Key) obj;
				return this.numHarmoncis == other.numHarmoncis;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return numHarmoncis;
		}
	}

	private static final Key KEY_1_HARMONICS = new Key(1);
	private static final Key KEY_2_HARMONICS = new Key(2);
	private static final Key KEY_3_HARMONICS = new Key(3);
	private static final Key KEY_4_HARMONICS = new Key(4);

	public static Key getKey(final int numHarmonis) {
		switch (numHarmonis) {
		case 1:
			return KEY_1_HARMONICS;
		case 2:
			return KEY_2_HARMONICS;
		case 3:
			return KEY_3_HARMONICS;
		case 4:
			return KEY_4_HARMONICS;
		default:
			return new Key(numHarmonis);
		}
	}

	public static void register() {
		final Processor processor = new Processor();
		FeatureFactory.getInstance().registerProcessor(processor);
	}
}
