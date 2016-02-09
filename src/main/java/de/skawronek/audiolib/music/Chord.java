package de.skawronek.audiolib.music;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;

public final class Chord {
	// Implementiert nach "The Complete Guide to JFugue --
	// Programming Music in Java",
	// Kapitel 2 "Using the JFugue MusicString", S. 4
	// http://www.jfugue.org/jfugue-chapter2.pdf
	public enum Type {
		//@formatter:off
		MAJOR("maj", new byte[] {0, 4, 7}),
		MINOR("min", new byte[] {0, 3, 7}),
		AUGMENTED("aug", new byte[] {0, 4, 8}),
		DIMINISHED("dim", new byte[] {0, 3, 6}),
		DOMINANT_7TH("dom7", new byte[] {0, 4, 7, 10}),
		MAJOR_7TH("maj7", new byte[] {0, 4, 7, 11}),
		MINOR_7TH("min7", new byte[] {0, 3, 7, 10});
		//@formatter:on

		private final String shortName;
		// Halbtonschritte ausgehend von der Stammnote
		private final byte[] intervals;

		private Type(final @NonNull String shortName,
				final byte @NonNull [] intervals) {
			this.shortName = shortName;
			this.intervals = intervals;
		}

		public @NonNull String getShortName() {
			return shortName;
		}

		public byte @NonNull [] getIntervals() {
			return Arrays.copyOf(intervals, intervals.length);
		}

		public byte @NonNull [] innvertIntervals(final int numInversions) {
			if (numInversions <= 0 || numInversions > intervals.length - 1) {
				throw new IllegalArgumentException("numInversions "
						+ numInversions + " is out of range 1.."
						+ (intervals.length - 1));
			}

			final byte[] inverted = getIntervals();
			// Es wird um eine Oktave (= 12 Halbtonschritte) erhöht
			for (int i = 0; i < numInversions; i++) {
				inverted[i] += 12;
			}
			// damit müssen die Halbtonschritte neu sortiert werden
			Arrays.sort(inverted);

			return inverted;
		}
	}

	final Pitch root;
	final Type type;
	final int inversions;
	final Pitch[] pitches;

	public Chord(final @NonNull Pitch root, final @NonNull Type type) {
		this(root, type, 0);
	}

	public Chord(final @NonNull Pitch root, final @NonNull Type type,
			final int inversions) {
		this.root = root;
		this.type = type;
		this.inversions = inversions;
		final byte[] intervals;
		if (inversions > 0) {
			intervals = type.innvertIntervals(inversions);
		} else {
			intervals = type.getIntervals();
		}
		this.pitches = new Pitch[intervals.length];
		for (int i = 0; i < intervals.length; i++) {
			final byte halfSteps = intervals[i];
			final Pitch pitch = root.transpose(halfSteps);
			this.pitches[i] = pitch;
		}
	}

	public @NonNull Pitch getRoot() {
		return root;
	}

	public @NonNull Type getType() {
		return type;
	}

	public int getInversions() {
		return inversions;
	}

	public boolean isInverted() {
		return inversions > 0;
	}

	public Pitch @NonNull [] getPitches() {
		return Arrays.copyOf(pitches, pitches.length);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		} else if (obj == this) {
			return true;
		} else if (obj instanceof Chord) {
			final Chord other = (Chord) obj;
			return this.root == other.root && this.type == other.type
					&& this.inversions == other.inversions;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int hashCode = root.hashCode();
		hashCode ^= type.hashCode();
		hashCode ^= inversions;
		return hashCode;
	}

	@Override
	public @NonNull String toString() {
		// Aufbau: Root + Type + Inversions
		// z. B. C7maj^
		String result = root.toEnglishNotation();
		result += type.getShortName();
		if (inversions > 0) {
			final StringBuilder carets = new StringBuilder(inversions);
			for (int i = 0; i < inversions; i++) {
				carets.append('^');
			}
			result += carets.toString();
		}
		return result;
	}
}
