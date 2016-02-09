package de.skawronek.audiolib.music;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import de.skawronek.audiolib.music.Pitch.Step;

public final class PitchTest {
	@Test
	public void testFromStringGermanPitches() {
		assertEquals(new Pitch(Step.C, Accidental.NONE, 2),
				Pitch.fromString("C"));
		assertEquals(new Pitch(Step.C, Accidental.SHARP, 2),
				Pitch.fromString("Cis"));
		assertEquals(new Pitch(Step.B, Accidental.FLAT, 2),
				Pitch.fromString("B"));
		assertEquals(new Pitch(Step.D, Accidental.NONE, 3),
				Pitch.fromString("d"));
		assertEquals(new Pitch(Step.A, Accidental.SHARP, 5),
				Pitch.fromString("ais''"));
		assertEquals(new Pitch(Step.A, Accidental.FLAT, 5),
				Pitch.fromString("as''"));
	}

	@Test
	public void testFromStringOfEnglishPitches() {
		assertEquals(new Pitch(Step.C, Accidental.NONE, 2),
				Pitch.fromString("C2"));
		assertEquals(new Pitch(Step.C, Accidental.SHARP, 2),
				Pitch.fromString("C#2"));
		assertEquals(new Pitch(Step.B, Accidental.FLAT, 2),
				Pitch.fromString("Bb2"));
		assertEquals(new Pitch(Step.D, Accidental.NONE, 3),
				Pitch.fromString("D3"));
		assertEquals(new Pitch(Step.A, Accidental.SHARP, 5),
				Pitch.fromString("A#5"));
		assertEquals(new Pitch(Step.A, Accidental.FLAT, 5),
				Pitch.fromString("Ab5"));
	}

	@Test
	public void testEqualsAndHashCode() {
		final Pitch a1 = new Pitch(Step.D, Accidental.NONE, 2);
		final Pitch b1 = new Pitch(Step.D, Accidental.NONE, 2);
		assertTrue(a1.equals(b1) && b1.equals(a1));
		assertEquals(a1.hashCode(), b1.hashCode());

		final Pitch a2 = new Pitch(Step.D, Accidental.NONE, 2);
		final Pitch b2 = new Pitch(Step.D, Accidental.SHARP, 2);
		assertFalse(a2.equals(b2));
		assertFalse(b2.equals(a2));
		assertFalse(a2.hashCode() == b2.hashCode());

		final Pitch a3 = new Pitch(Step.D, Accidental.NONE, 2);
		final Pitch b3 = new Pitch(Step.D, Accidental.FLAT, 3);
		assertFalse(a3.equals(b3));
		assertFalse(b3.equals(a3));
		assertFalse(a3.hashCode() == b3.hashCode());
	}

	@Test
	public void testToGermanNotation() {
		assertEquals("D",
				new Pitch(Step.D, Accidental.NONE, 2).toGermanNotation());
		assertEquals("Des",
				new Pitch(Step.D, Accidental.FLAT, 2).toGermanNotation());
		assertEquals("Dis",
				new Pitch(Step.D, Accidental.SHARP, 2).toGermanNotation());

		assertEquals("d",
				new Pitch(Step.D, Accidental.NONE, 3).toGermanNotation());
		assertEquals("des",
				new Pitch(Step.D, Accidental.FLAT, 3).toGermanNotation());
		assertEquals("dis",
				new Pitch(Step.D, Accidental.SHARP, 3).toGermanNotation());

		assertEquals("d''",
				new Pitch(Step.D, Accidental.NONE, 5).toGermanNotation());
		assertEquals("des''",
				new Pitch(Step.D, Accidental.FLAT, 5).toGermanNotation());
		assertEquals("dis''",
				new Pitch(Step.D, Accidental.SHARP, 5).toGermanNotation());
	}

	@Test
	public void testToEnglishNotation() {
		assertEquals("D2",
				new Pitch(Step.D, Accidental.NONE, 2).toEnglishNotation());
		assertEquals("Db2",
				new Pitch(Step.D, Accidental.FLAT, 2).toEnglishNotation());
		assertEquals("D#2",
				new Pitch(Step.D, Accidental.SHARP, 2).toEnglishNotation());

		assertEquals("D3",
				new Pitch(Step.D, Accidental.NONE, 3).toEnglishNotation());
		assertEquals("Db3",
				new Pitch(Step.D, Accidental.FLAT, 3).toEnglishNotation());
		assertEquals("D#3",
				new Pitch(Step.D, Accidental.SHARP, 3).toEnglishNotation());

		assertEquals("D5",
				new Pitch(Step.D, Accidental.NONE, 5).toEnglishNotation());
		assertEquals("Db5",
				new Pitch(Step.D, Accidental.FLAT, 5).toEnglishNotation());
		assertEquals("D#5",
				new Pitch(Step.D, Accidental.SHARP, 5).toEnglishNotation());
	}

	@Test
	public void testGetToneIndex() {
		assertEquals(21, Pitch.fromString("C2").getToneIndex());
		assertEquals(28, Pitch.fromString("C3").getToneIndex());
		assertEquals(35, Pitch.fromString("C4").getToneIndex());
		assertEquals(36, Pitch.fromString("D4").getToneIndex());
		assertEquals(36, Pitch.fromString("D#4").getToneIndex());
		assertEquals(36, Pitch.fromString("Db4").getToneIndex());
	}

	@Test
	public void testGetSemitoneIndex() {
		assertEquals(36, Pitch.fromString("C2").getSemitoneIndex());
		assertEquals(48, Pitch.fromString("C3").getSemitoneIndex());
		assertEquals(60, Pitch.fromString("C4").getSemitoneIndex());
		assertEquals(62, Pitch.fromString("D4").getSemitoneIndex());
		assertEquals(63, Pitch.fromString("D#4").getSemitoneIndex());
		assertEquals(61, Pitch.fromString("Db4").getSemitoneIndex());
		assertEquals(61, Pitch.fromString("C#4").getSemitoneIndex());
	}

	@Test
	public void testTranspose() {
		assertEquals(Pitch.fromString("C4"),
				Pitch.fromString("C3").transpose(12));
		assertEquals(Pitch.fromString("C2"),
				Pitch.fromString("C3").transpose(-12));
		assertEquals(Pitch.fromString("C#3"),
				Pitch.fromString("C3").transpose(1));
		assertEquals(Pitch.fromString("B2"),
				Pitch.fromString("C3").transpose(-1));
		assertEquals(Pitch.fromString("F3"), Pitch.fromString("E3")
				.transpose(1));
		assertEquals(Pitch.fromString("E3"),
				Pitch.fromString("F3").transpose(-1));
	}
}
