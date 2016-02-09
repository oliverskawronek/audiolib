package de.skawronek.audiolib.tonal;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.Feature;
import de.skawronek.audiolib.FeatureFactory;
import de.skawronek.audiolib.FeatureKey;
import de.skawronek.audiolib.FeatureProcessor;
import de.skawronek.audiolib.Frame;
import de.skawronek.audiolib.music.EqualTemperament;
import de.skawronek.audiolib.music.Pitch;
import de.skawronek.audiolib.signal.FastFourierTransform;

public final class ChromaVector extends Feature {
	private final float[] chroma;

	public final static class Processor extends FeatureProcessor<ChromaVector> {
		private de.skawronek.audiolib.math.ChromaVector cv = new de.skawronek.audiolib.math.ChromaVector(
				2, 2, 2);

		@Override
		public @NonNull Class<ChromaVector> getFeatureClass() {
			return ChromaVector.class;
		}

		@Override
		public ChromaVector process(final @NonNull Frame frame,
				@NonNull final FeatureKey<ChromaVector> featureKey) {
			FastFourierTransform fft = frame.getFeature(FastFourierTransform
					.getKey());
			float[] spectrum = fft.getMagnitudeSpectrum();
			final Pitch c3 = Pitch.fromString("C3");
			final double minFreq = EqualTemperament.getInstance()
					.getFrequencyOf(c3);
			final double bandWidth = frame.getSampleRate() / frame.getSize();
			cv.compute(spectrum, minFreq, bandWidth);
			final float[] chroma = new float[12];
			cv.copyChroma(chroma);
			return new ChromaVector(chroma);
		}
	}

	private ChromaVector(final float @NonNull [] chroma) {
		this.chroma = chroma;
	}

	public float @NonNull [] getChroma() {
		return chroma;
	}

	public final static class Key extends FeatureKey<ChromaVector> {
		private Key() {
		}

		@Override
		public @NonNull Class<ChromaVector> getFeatureClass() {
			return ChromaVector.class;
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
