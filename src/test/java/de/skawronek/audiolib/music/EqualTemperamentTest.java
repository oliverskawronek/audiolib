package de.skawronek.audiolib.music;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * 
 * @author Oliver Skawronek
 * 
 */
public final class EqualTemperamentTest {
	private static final double COMPARISON_DELTA = 1e-3f;

	private final EqualTemperament temperament = EqualTemperament.getInstance();

	@Test(expected = IllegalArgumentException.class)
	public void testSetTuningFrequencyWithZero() {
		temperament.setTuningFrequency(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetTuningFrequencyWithNegativeFrequency() {
		temperament.setTuningFrequency(-1);
	}

	@Test
	public void testGetFrequencyWith440Hertz() {
		// Standardkammerton bei 440 Hz
		temperament.setTuningFrequency(440f);
		//@formatter:off
		assertEquals(220f, temperament.getFrequencyOf(Pitch.fromString("A3")), COMPARISON_DELTA);
		// Kammerton a' bzw. A4
		assertEquals(440f, temperament.getFrequencyOf(Pitch.fromString("A4")), COMPARISON_DELTA);
		assertEquals(880f, temperament.getFrequencyOf(Pitch.fromString("A5")), COMPARISON_DELTA);
		// Schloss-C c' bzw. C4
		assertEquals(261.626f, temperament.getFrequencyOf(Pitch.fromString("C4")), COMPARISON_DELTA);
		assertEquals(65.4064f, temperament.getFrequencyOf(Pitch.fromString("C2")), COMPARISON_DELTA);
		assertEquals(184.997f, temperament.getFrequencyOf(Pitch.fromString("F#3")), COMPARISON_DELTA);		
		//@formatter:on
	}

	@Test
	public void testGetFrequencyWith443Hertz() {
		/*
		 * Einstimmung des Kammertons bei deutschen und österreichigen
		 * Sinfonieorchestern auf 443 Hz
		 */
		temperament.setTuningFrequency(443f);
		//@formatter:off
		assertEquals(221.5f, temperament.getFrequencyOf(Pitch.fromString("A3")), COMPARISON_DELTA);
		// Kammerton a' bzw. A4
		assertEquals(443f, temperament.getFrequencyOf(Pitch.fromString("A4")), COMPARISON_DELTA);
		assertEquals(886f, temperament.getFrequencyOf(Pitch.fromString("A5")), COMPARISON_DELTA);
		// Schloss-C c' bzw. C4
		assertEquals(263.409f, temperament.getFrequencyOf(Pitch.fromString("C4")), COMPARISON_DELTA);
		assertEquals(65.8523f, temperament.getFrequencyOf(Pitch.fromString("C2")), COMPARISON_DELTA);
		assertEquals(186.2585f, temperament.getFrequencyOf(Pitch.fromString("F#3")), COMPARISON_DELTA);
		//@formatter:on
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetClosestPitchWithZero() {
		temperament.getClosestPitchTo(0, Accidental.SHARP);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetClosestPitchWithNegativeFrequency() {
		temperament.getClosestPitchTo(-1, Accidental.SHARP);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetClosestPitchWithNone() {
		temperament.getClosestPitchTo(440, Accidental.NONE);
	}

	@Test
	public void testGetClosestPitchTo() {
		assertEquals(Pitch.fromString("C4"),
				temperament.getClosestPitchTo(261.626, Accidental.FLAT));
		assertEquals(Pitch.fromString("C4"),
				temperament.getClosestPitchTo(261.626, Accidental.SHARP));
		assertEquals(Pitch.fromString("E4"),
				temperament.getClosestPitchTo(329.628, Accidental.FLAT));
		assertEquals(Pitch.fromString("E4"),
				temperament.getClosestPitchTo(329.628, Accidental.SHARP));
		assertEquals(Pitch.fromString("G#4"),
				temperament.getClosestPitchTo(415.305, Accidental.SHARP));
		assertEquals(Pitch.fromString("Ab4"),
				temperament.getClosestPitchTo(415.305, Accidental.FLAT));
	}

	@Test
	public void testAreEnharmonicTogether() {
		//@formatter:off
		assertTrue(EqualTemperament.areEnharmonicTogether(
				Pitch.fromString("B#3"), Pitch.fromString("C4")));
		assertTrue(EqualTemperament.areEnharmonicTogether(
				Pitch.fromString("C#4"), Pitch.fromString("Db4")));
		assertTrue(EqualTemperament.areEnharmonicTogether(
				Pitch.fromString("D#4"), Pitch.fromString("Eb4")));
		assertTrue(EqualTemperament.areEnharmonicTogether(
				Pitch.fromString("E4"), Pitch.fromString("Fb4")));
		assertTrue(EqualTemperament.areEnharmonicTogether(
				Pitch.fromString("F4"), Pitch.fromString("E#4")));
		assertTrue(EqualTemperament.areEnharmonicTogether(
				Pitch.fromString("F#4"), Pitch.fromString("Gb4")));
		assertTrue(EqualTemperament.areEnharmonicTogether(
				Pitch.fromString("G#4"), Pitch.fromString("Ab4")));
		assertTrue(EqualTemperament.areEnharmonicTogether(
				Pitch.fromString("A#4"), Pitch.fromString("Bb4")));
		assertTrue(EqualTemperament.areEnharmonicTogether(
				Pitch.fromString("B4"), Pitch.fromString("Cb5")));
		assertTrue(EqualTemperament.areEnharmonicTogether(
				Pitch.fromString("B#4"), Pitch.fromString("C5")));
		//@formatter:on
	}

	@Test
	public void getCentInterval() {
		final double fC4 = temperament.getFrequencyOf(Pitch.fromString("C4"));
		final double fCSharp4 = temperament.getFrequencyOf(Pitch
				.fromString("C#4"));
		final double fD4 = temperament.getFrequencyOf(Pitch.fromString("D4"));
		final double fC5 = temperament.getFrequencyOf(Pitch.fromString("C5"));

		assertEquals(100, EqualTemperament.getCentInterval(fC4, fCSharp4),
				COMPARISON_DELTA);
		assertEquals(-100, EqualTemperament.getCentInterval(fCSharp4, fC4),
				COMPARISON_DELTA);
		assertEquals(200, EqualTemperament.getCentInterval(fC4, fD4),
				COMPARISON_DELTA);
		assertEquals(1200, EqualTemperament.getCentInterval(fC4, fC5),
				COMPARISON_DELTA);
		assertEquals(-1200, EqualTemperament.getCentInterval(fC5, fC4),
				COMPARISON_DELTA);
	}

	@Test
	public void testTranspose() {
		final double fC4 = temperament.getFrequencyOf(Pitch.fromString("C4"));
		final double fCSharp4 = temperament.getFrequencyOf(Pitch
				.fromString("C#4"));
		final double fD4 = temperament.getFrequencyOf(Pitch.fromString("D4"));
		final double fC5 = temperament.getFrequencyOf(Pitch.fromString("C5"));

		assertEquals(fCSharp4, EqualTemperament.transpose(fC4, 100),
				COMPARISON_DELTA);
		assertEquals(fC4, EqualTemperament.transpose(fCSharp4, -100),
				COMPARISON_DELTA);
		assertEquals(fD4, EqualTemperament.transpose(fC4, 200),
				COMPARISON_DELTA);
		assertEquals(fC5, EqualTemperament.transpose(fC4, 1200),
				COMPARISON_DELTA);
		assertEquals(fC4, EqualTemperament.transpose(fC5, -1200),
				COMPARISON_DELTA);
	}
}
