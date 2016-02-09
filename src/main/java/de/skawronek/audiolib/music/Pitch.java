package de.skawronek.audiolib.music;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;

public final class Pitch implements Comparable<Pitch> {
	public static enum Step {
		C("C"), D("D"), E("E"), F("F"), G("G"), A("A"), B("H");

		private final String germanName;

		private Step(final String name) {
			this.germanName = name;
		}

		public String germanName() {
			return germanName;
		}
	}

	private final Step step;
	private final Accidental alter;
	/**
	 * Octaves are represented by the numbers where 4 indicates the octave
	 * started by middle C.
	 */
	private final int octave;

	public Pitch(final @NonNull Step step, final @NonNull Accidental alter,
			final int octave) {
		if (octave < 0) {
			throw new IllegalArgumentException("octave " + octave + " < 0");
		}

		this.step = step;
		this.alter = alter;
		this.octave = octave;
	}

	public Pitch(final @NonNull Pitch origin) {
		this.step = origin.step;
		this.alter = origin.alter;
		this.octave = origin.octave;
	}

	public @NonNull Step getStep() {
		return step;
	}

	public @NonNull Accidental getAlter() {
		return alter;
	}

	/**
	 * Octaves are represented by the numbers 0 to 9, where 4 indicates the
	 * octave started by middle C.
	 * 
	 * @return octave number, where 4 is the octave of middle C
	 */
	public int getOctave() {
		return octave;
	}

	public Pitch getInOctave(final int octave) {
		if (getOctave() == octave) {
			return this;
		} else {
			return new Pitch(this.step, this.alter, octave);
		}
	}

	@Override
	public @NonNull String toString() {
		//@formatter:off
		final String message = String.format("pitch [step: %s, alter: %s, octave: %d]", 
				step, alter, octave);
		//@formatter:on
		return message;
	}

	public @NonNull String toGermanNotation() {
		assert octave >= 2;
		final String base;
		if (alter == Accidental.NONE) {
			base = step.germanName();
		} else if (alter == Accidental.SHARP) {
			switch (step) {
			case C:
				base = "Cis";
				break;
			case D:
				base = "Dis";
				break;
			case E:
				base = "Eis";
				break;
			case F:
				base = "Fis";
				break;
			case G:
				base = "Gis";
				break;
			case A:
				base = "Ais";
				break;
			case B:
				base = "His";
				break;
			default:
				throw new AssertionError();
			}
		} else if (alter == Accidental.FLAT) {
			switch (step) {
			case C:
				base = "Ces";
				break;
			case D:
				base = "Des";
				break;
			case E:
				base = "Es";
				break;
			case F:
				base = "Fes";
				break;
			case G:
				base = "Ges";
				break;
			case A:
				base = "As";
				break;
			case B:
				base = "B";
				break;
			default:
				throw new AssertionError();
			}
		} else {
			throw new AssertionError();
		}

		if (octave == 2) {
			return base;
		} else if (octave == 3) {
			return base.toLowerCase();
		} else if (octave >= 4) {
			StringBuilder sb = new StringBuilder(base.toLowerCase());
			for (int i = 0; i < octave - 3; i++) {
				sb.append('\'');
			}
			return sb.toString();
		} else {
			throw new AssertionError();
		}
	}

	public @NonNull String toEnglishNotation() {
		final String alterString;
		switch (alter) {
		case NONE:
			alterString = "";
			break;
		case FLAT:
			alterString = "b";
			break;
		case SHARP:
			alterString = "#";
			break;
		default:
			throw new AssertionError();
		}

		final String message = String.format("%s%s%d", step.name(),
				alterString, octave);
		return message;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		} else if (obj == this) {
			return true;
		} else if (obj instanceof Pitch) {
			final Pitch other = (Pitch) obj;
			return this.step == other.step && this.alter == other.alter
					&& this.octave == other.octave;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return (step.ordinal() << 16) | (alter.ordinal() << 8) | (octave);
	}

	@Override
	public int compareTo(final Pitch other) {
		return this.getSemitoneIndex() - other.getSemitoneIndex();
	}

	// formatter:off
	/**
	 * Gibt den Ganztonindex zurück. Dabei wird <code>getAlter</code> ignoriert.
	 * Zwischen zwei benachbarten Oktaven liegen 7 Ganztöne. Beispiele:
	 * <ul>
	 * <li><code>p = "C4"; p.getToneIndex() == 35</code></li>
	 * <li><code>p = "D4"; p.getToneIndex() == 36</code></li>
	 * <li><code>p = "D#4"; p.getToneIndex() == 36</code></li>
	 * </ul>
	 * 
	 * @return Ganztonindex, beginnent ab 7 für C0
	 */
	// @formatter:on
	public int getToneIndex() {
		// Zwischen zwei Oktaven liegen 7 Ganztöne
		final int octaveOffset = (octave + 1) * 7;

		final int stepOffset;
		switch (step) {
		case C:
			stepOffset = 0;
			break;
		case D:
			stepOffset = 1;
			break;
		case E:
			stepOffset = 2;
			break;
		case F:
			stepOffset = 3;
			break;
		case G:
			stepOffset = 4;
			break;
		case A:
			stepOffset = 5;
			break;
		case B: // deutsch H
			stepOffset = 6;
			break;
		default:
			throw new AssertionError("Expected step is one of A..G, but was "
					+ step);
		}

		final int index = octaveOffset + stepOffset;
		return index;
	}

	// formatter:off
	/**
	 * Gibt den Halbtonindex auf der chromatischen Skala zurück. Zwischen zwei
	 * benachbarten Oktaven liegen 12 Halbtöne. Beispiele:
	 * <ul>
	 * <li><code>p = "C4"; p.getSemitoneIndex() == 60</code></li>
	 * <li><code>p = "D4"; p.getSemitoneIndex() == 62</code></li>
	 * <li><code>p = "D#4"; p.getSemitoneIndex() == 63</code></li>
	 * </ul>
	 * 
	 * @return Halbtonindex, beginnent ab 12 für C0
	 */
	// @formatter:on
	public int getSemitoneIndex() {
		// Zwischen zwei Oktaven liegen 12 Halbtöne
		final int octaveOffset = (octave + 1) * 12;

		final int stepOffset;
		switch (step) {
		case C:
			stepOffset = 0;
			break;
		case D:
			stepOffset = 2;
			break;
		case E:
			stepOffset = 4;
			break;
		case F:
			stepOffset = 5;
			break;
		case G:
			stepOffset = 7;
			break;
		case A:
			stepOffset = 9;
			break;
		case B:
			stepOffset = 11;
			break;
		default:
			throw new AssertionError("Expected step is one of A..G, but was "
					+ step);
		}

		final int alterOffset;
		switch (alter) {
		case FLAT:
			alterOffset = -1;
			break;
		case SHARP:
			alterOffset = 1;
			break;
		case NONE:
			alterOffset = 0;
			break;
		default:
			throw new AssertionError(
					"Expected one of FLAT, SHARP and NONE, but was " + alter);
		}

		final int index = octaveOffset + stepOffset + alterOffset;
		return index;
	}

	/**
	 * Gibt den Vorgänger-Pitch auf der chromatischen Skala zurück, der einen
	 * Halbton davor liegt.
	 * 
	 * @returns Vorgänger-Pitch auf der chromatischen Skala
	 */
	public @NonNull Pitch getPredecessor() {
		switch (alter) {
		case FLAT:
			switch (step) {
			case C:
				// Es muss in die Oktave darunter gewechselt werden.
				// Beispiel ces'' hat den Vorgänger h', engl. pred(Cb5) = B4
				return new Pitch(Step.B, Accidental.NONE, octave - 1);
			case D:
				// pred(Des) = C, engl. pred(Db) = C
				return new Pitch(Step.C, Accidental.NONE, octave);
			case E:
				// pred(Es) = D, engl. pred(Eb) = D
				return new Pitch(Step.D, Accidental.NONE, octave);
			case F:
				// pred(Fes) = Es, engl. pred(Fb) = Eb
				return new Pitch(Step.E, Accidental.FLAT, octave);
			case G:
				// pred(Ges) = Fm, engl. pred(Gb) = F
				return new Pitch(Step.F, Accidental.NONE, octave);
			case A:
				// pred(As) = G, engl. pred(Ab) = G
				return new Pitch(Step.G, Accidental.NONE, octave);
			case B:
				// pred(B) = A, engl. pred(Bb) = A
				return new Pitch(Step.A, Accidental.NONE, octave);
			default:
				throw new AssertionError();
			}
		case NONE:
			return new Pitch(step, Accidental.FLAT, octave);
		case SHARP:
			return new Pitch(step, Accidental.NONE, octave);
		default:
			throw new AssertionError();
		}
	}

	/**
	 * Gibt den Nachfolger-Pitch auf der chromatischen Skala zurück, der einen
	 * Halbton danach liegt.
	 * 
	 * @returns Nachfolger-Pitch auf der chromatischen Skala
	 */
	public @NonNull Pitch getSucessor() {
		switch (alter) {
		case FLAT:
			return new Pitch(step, Accidental.NONE, octave);
		case NONE:
			return new Pitch(step, Accidental.SHARP, octave);
		case SHARP:
			switch (step) {
			case C:
				// succ(Cis) = D, engl. succ(C#) = D
				return new Pitch(Step.D, Accidental.NONE, octave);
			case D:
				// succ(Dis) = E, engl. succ(D#) = E
				return new Pitch(Step.E, Accidental.NONE, octave);
			case E:
				// succ(Eis) = Fis, engl. succ(E#) = F#
				return new Pitch(Step.F, Accidental.SHARP, octave);
			case F:
				// succ(Fis) = G, engl. succ(F#) = G
				return new Pitch(Step.G, Accidental.NONE, octave);
			case G:
				// succ(Gis) = A, engl. succ(G#) = A
				return new Pitch(Step.A, Accidental.NONE, octave);
			case A:
				// succ(Ais) = H, engl. succ(A#) = B
				return new Pitch(Step.B, Accidental.NONE, octave);
			case B:
				// Es muss in die Oktave darüber gewechselt werden.
				// Beispiel his'' hat den Nachfolger cis''',
				// engl. pred(B#5) = C#6
				return new Pitch(Step.C, Accidental.SHARP, octave + 1);
			default:
				throw new AssertionError();
			}
		default:
			throw new AssertionError();
		}
	}

	public @NonNull Pitch transpose(final int halfSteps) {
		if (halfSteps == 0) {
			return this;
		}

		final int octaveDelta = halfSteps / 12;
		Pitch result = new Pitch(this.step, this.alter, this.octave
				+ octaveDelta);
		while (true) {
			final int halfStepsDiff = result.getSemitoneIndex()
					- this.getSemitoneIndex();
			if (halfStepsDiff == halfSteps) {
				break;
			}
			if (halfSteps > 0) {
				result = result.getSucessor();
			} else {
				assert halfSteps < 0;
				result = result.getPredecessor();
			}
		}
		return result.toNaturalIfExists();
	}

	//@formatter:off
	/**
	 * <p>
	 * Versucht unnötige Versetzungszeichen aufzulösen.
	 * Beispiele: C3b wird zu B2 und E3# wird zu F3.
	 * </p>
	 * <p>
	 * Nachbedingung: <code>this</code> und <code>toNaturalIfExists</code>
	 * haben den selben <code>getSemitoneIndex</code>
	 * <pre>preIdx = postIdx
	 * where preIdx = this.getSemitoneIndex(),
	 * 	postIdx = this.toNaturalIfExists().getSemitoneIndex()</pre>
	 * </p>
	 * @return
	 */
	//@formatter:on
	public @NonNull Pitch toNaturalIfExists() {
		if (step == Step.C && alter == Accidental.FLAT) {
			return new Pitch(Step.B, Accidental.NONE, this.octave - 1);
		} else if (step == Step.E && alter == Accidental.SHARP) {
			return new Pitch(Step.F, Accidental.NONE, this.octave);
		} else if (step == Step.F && alter == Accidental.FLAT) {
			return new Pitch(Step.E, Accidental.NONE, this.octave);
		} else if (step == Step.B && alter == Accidental.SHARP) {
			return new Pitch(Step.C, Accidental.NONE, this.octave + 1);
		} else {
			return this;
		}
	}

	public static @NonNull Pitch fromSemitoneIndex(final int p,
			final @NonNull Accidental preferedAccidental) {
		if (p < 0) {
			throw new IllegalArgumentException("p " + p + " < 0");
		} else if (preferedAccidental == Accidental.NONE) {
			throw new IllegalArgumentException(
					"expected SHARP or FLAT, but was " + preferedAccidental);
		}

		final int octave = p / 12 - 1;
		final int rest = p % 12;

		final Step step;
		final Accidental alter;

		switch (rest) {
		case 0:
			step = Step.C;
			alter = Accidental.NONE;
			break;
		case 1:
			step = (preferedAccidental == Accidental.SHARP ? Step.C : Step.D);
			alter = preferedAccidental;
			break;
		case 2:
			step = Step.D;
			alter = Accidental.NONE;
			break;
		case 3:
			step = (preferedAccidental == Accidental.SHARP ? Step.D : Step.E);
			alter = preferedAccidental;
			break;
		case 4:
			step = Step.E;
			alter = Accidental.NONE;
			break;
		case 5:
			step = Step.F;
			alter = Accidental.NONE;
			break;
		case 6:
			step = (preferedAccidental == Accidental.SHARP ? Step.F : Step.G);
			alter = preferedAccidental;
			break;
		case 7:
			step = Step.G;
			alter = Accidental.NONE;
			break;
		case 8:
			step = (preferedAccidental == Accidental.SHARP ? Step.G : Step.A);
			alter = preferedAccidental;
			break;
		case 9:
			step = Step.A;
			alter = Accidental.NONE;
			break;
		case 10:
			step = (preferedAccidental == Accidental.SHARP ? Step.A : Step.B);
			alter = preferedAccidental;
			break;
		case 11:
			step = Step.B;
			alter = Accidental.NONE;
			break;
		default:
			throw new AssertionError("expected 0..11, but was " + rest);
		}

		final Pitch pitch = new Pitch(step, alter, octave);
		return pitch;
	}

	//@formatter:off
	private static final String GERMAN_REGEX = "^(C|Ces|Cis|D|Des|Dis|E|Es|Eis|F|Fes|Fis|G|Ges|Gis|A|As|Ais|H|B|His)"
			+ "|((c|ces|cis|d|des|dis|e|es|eis|f|fes|fis|g|ges|gis|a|as|ais|h|b|his)(\'*))$";
	//@formatter:on
	private static final Pattern GERMAN_PATTERN = Pattern.compile(GERMAN_REGEX);

	private static final String ENGLISH_REGEX = "^([CDEFGAB])((#|b)?)([0-8])$";
	private static final Pattern ENGLISH_PATTERN = Pattern
			.compile(ENGLISH_REGEX);

	public static @NonNull Pitch fromString(final @NonNull String s) {
		final Step step;
		final Accidental alter;
		final int octave;

		final Matcher germanMatcher = GERMAN_PATTERN.matcher(s);
		if (germanMatcher.matches()) {
			if (germanMatcher.group(1) != null) {
				//@formatter:off
				/*
				 * 1. Alternative:
				 * (C|Ces|Cis|D|Des|Dis|E|Es|Eis|F|Fes|Fis|G|Ges|Gis|A|As|Ais|H|B|His)
				 */
				//@formatter:on
				final String base = germanMatcher.group(1);

				switch (base) {
				case "H":
				case "His":
					step = Step.B;
					break;
				default:
					step = Step.valueOf(base.substring(0, 1));
				}

				final boolean diatonic = base.length() == 1
						&& !"B".equals(base);
				if (diatonic) {
					alter = Accidental.NONE;
				} else {
					if (base.endsWith("is")) {
						alter = Accidental.SHARP;
					} else {
						alter = Accidental.FLAT;
					}
				}

				octave = 2;
			} else if (germanMatcher.group(2) != null) {
				//@formatter:off
				/*
				 * 2. Alternative:
				 * ((c|ces|cis|d|des|dis|e|es|eis|f|fes|fis|g|ges|gis|a|as|ais|h|b|his)(\'*))
				 */
				//@format:on
				final String base = germanMatcher.group(3);

				switch (base) {
				case "h":
				case "his":
					step = Step.B;
					break;
				default:
					step = Step.valueOf(base.substring(0, 1).toUpperCase());
				}
				
				final boolean diatonic = germanMatcher.group(3).length() == 1
						&& !"b".equals(germanMatcher.group(3));
				if (diatonic) {
					alter = Accidental.NONE;
				} else {
					if (germanMatcher.group(3).endsWith("is")) {
						alter = Accidental.SHARP;
					} else {
						alter = Accidental.FLAT;
					}
				}

				// Group 4: Apostrophs von (\'*)
				octave = 3 + germanMatcher.group(4).length();
			} else {
				throw new AssertionError("no alternatives matches");
			}
		} else {
			final Matcher englishMatcher = ENGLISH_PATTERN.matcher(s);
			if (englishMatcher.matches()) {
				step = Step.valueOf(englishMatcher.group(1));

				switch (englishMatcher.group(2)) {
				case "b":
					alter = Accidental.FLAT;
					break;
				case "#":
					alter = Accidental.SHARP;
					break;
				case "":
					alter = Accidental.NONE;
					break;
				default:
					throw new AssertionError();
				}

				octave = Integer.parseInt(englishMatcher.group(4));
			} else {
				throw new IllegalArgumentException("s " + s
						+ " has an unexpected format");
			}
		}

		return new Pitch(step, alter, octave);
	}
}
