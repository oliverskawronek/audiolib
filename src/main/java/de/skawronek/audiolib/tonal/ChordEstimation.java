package de.skawronek.audiolib.tonal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.Feature;
import de.skawronek.audiolib.FeatureFactory;
import de.skawronek.audiolib.FeatureKey;
import de.skawronek.audiolib.FeatureProcessor;
import de.skawronek.audiolib.Frame;
import de.skawronek.audiolib.music.Chord;
import de.skawronek.audiolib.music.Chord.Type;
import de.skawronek.audiolib.music.Pitch;

public final class ChordEstimation extends Feature {
	private final Chord chord;

	private static final class ChordProfile {
		final Chord chord;
		final float[] mask;
		final int numNotes;

		ChordProfile(final @NonNull Chord chord) {
			this.chord = chord;

			final Pitch[] pitches = chord.getPitches();
			numNotes = pitches.length;
			mask = new float[12];
			for (final Pitch pitch : pitches) {
				final int index = pitch.getSemitoneIndex() % 12;
				mask[index] = 1.0f;
			}
		}

		private static final double DEFAULT_BIAS = 1.0;

		double getDistance(final float @NonNull [] chroma) {
			return getDistance(chroma, DEFAULT_BIAS);
		}

		double getDistance(final float @NonNull [] chroma, final double beta) {
			double sum = 0.0;
			for (int i = 0; i < 12; i++) {
				final float complement = 1.0f - mask[i];
				sum += complement * (chroma[i] * chroma[i]);
			}
			return sum / ((12 - numNotes) * beta);
		}

		static ChordProfile fromChord(final @NonNull Chord chord) {
			return new ChordProfile(chord);
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null) {
				return false;
			} else if (obj == this) {
				return true;
			} else if (obj instanceof ChordProfile) {
				final ChordProfile other = (ChordProfile) obj;
				return this.numNotes == other.numNotes
						&& Arrays.equals(this.mask, other.mask);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(mask) ^ numNotes;
		}
	}

	public final static class Processor extends
			FeatureProcessor<ChordEstimation> {
		private final Set<ChordProfile> profiles = new HashSet<>();

		private Processor() {
			insertMajorChords();
			insertMinorChords();
		}

		private void insertMajorChords() {
			Pitch root = Pitch.fromString("C4");
			for (int i = 0; i < 12; i++) {
				final Chord chord = new Chord(root, Type.MAJOR);
				final ChordProfile profile = ChordProfile.fromChord(chord);
				profiles.add(profile);
				root = root.getSucessor();
			}
		}

		private void insertMinorChords() {
			Pitch root;
			root = Pitch.fromString("C4");
			for (int i = 0; i < 12; i++) {
				final Chord chord = new Chord(root, Type.MINOR);
				final ChordProfile profile = ChordProfile.fromChord(chord);
				profiles.add(profile);
				root = root.getSucessor();
			}
		}

		@Override
		public @NonNull Class<ChordEstimation> getFeatureClass() {
			return ChordEstimation.class;
		}

		@Override
		public ChordEstimation process(final @NonNull Frame frame,
				@NonNull final FeatureKey<ChordEstimation> featureKey) {
			final float[] chroma = frame.getFeature(ChromaVector.getKey())
					.getChroma();
			ChordProfile minProfile = null;
			double minDistance = Double.MAX_VALUE;
			for (final ChordProfile profile : profiles) {
				final double distance = profile.getDistance(chroma);
				if (distance < minDistance) {
					minDistance = distance;
					minProfile = profile;
				}
			}
			return new ChordEstimation(minProfile.chord);
		}
	}

	private ChordEstimation(final @NonNull Chord chord) {
		this.chord = chord;
	}

	public Chord getChord() {
		return chord;
	}

	public final static class Key extends FeatureKey<ChordEstimation> {
		private Key() {
		}

		@Override
		public @NonNull Class<ChordEstimation> getFeatureClass() {
			return ChordEstimation.class;
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
