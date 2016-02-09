package de.skawronek.audiolib.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;

import org.junit.Test;

public final class UtilTest {
	private static final float COMPARISION_DELTA = 0.00001f;

	@Test(expected = IllegalArgumentException.class)
	public void testClampDoubleCheckForMinGreaterThanMax() {
		Util.clamp(5d, 10d, 1d);
	}

	@Test
	public void testClampDouble() {
		assertEquals(5d, Util.clamp(5d, 0d, 10d), COMPARISION_DELTA);
		assertEquals(0d, Util.clamp(0d, 0d, 10d), COMPARISION_DELTA);
		assertEquals(0d, Util.clamp(-2d, 0d, 10d), COMPARISION_DELTA);
		assertEquals(10d, Util.clamp(10d, 0d, 10d), COMPARISION_DELTA);
		assertEquals(10d, Util.clamp(11d, 0d, 10d), COMPARISION_DELTA);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testClampFloatCheckForMinGreaterThanMax() {
		Util.clamp(5f, 10f, 1f);
	}

	@Test
	public void testClampFloat() {
		assertEquals(5f, Util.clamp(5f, 0f, 10f), COMPARISION_DELTA);
		assertEquals(0f, Util.clamp(0f, 0f, 10f), COMPARISION_DELTA);
		assertEquals(0f, Util.clamp(-2f, 0f, 10f), COMPARISION_DELTA);
		assertEquals(10f, Util.clamp(10f, 0f, 10f), COMPARISION_DELTA);
		assertEquals(10f, Util.clamp(11f, 0f, 10f), COMPARISION_DELTA);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testClampIntCheckForMinGreaterThanMax() {
		Util.clamp(5, 10, 1);
	}

	@Test
	public void testClampInt() {
		assertEquals(5, Util.clamp(5, 0, 10));
		assertEquals(0, Util.clamp(0, 0, 10));
		assertEquals(0, Util.clamp(-2, 0, 10));
		assertEquals(10, Util.clamp(10, 0, 10));
		assertEquals(10, Util.clamp(11, 0, 10));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testClampLongCheckForMinGreaterThanMax() {
		Util.clamp(5L, 10L, 1L);
	}

	@Test
	public void testClampLong() {
		assertEquals(5L, Util.clamp(5L, 0L, 10L));
		assertEquals(0L, Util.clamp(0L, 0L, 10L));
		assertEquals(0L, Util.clamp(-2L, 0L, 10L));
		assertEquals(10L, Util.clamp(10L, 0L, 10L));
		assertEquals(10L, Util.clamp(11L, 0L, 10L));
	}

	@Test
	public void testPowerOfTwoInt() {
		for (int i = 0; i < 31; i++) {
			final int power = 1 << i; // = 2^i
			assertTrue(Util.isPowerOfTwo(power));
		}
		assertFalse(Util.isPowerOfTwo(Integer.MAX_VALUE));
		assertFalse(Util.isPowerOfTwo(12345));
		assertFalse(Util.isPowerOfTwo(0));
		assertFalse(Util.isPowerOfTwo(-1));
		assertFalse(Util.isPowerOfTwo(-2));
		assertFalse(Util.isPowerOfTwo(Integer.MIN_VALUE));
	}

	@Test
	public void testPowerOfTwoLong() {
		for (int i = 0; i < 63; i++) {
			final long power = 1l << i; // = 2^i
			assertTrue(Util.isPowerOfTwo(power));
		}
		assertFalse(Util.isPowerOfTwo(Long.MAX_VALUE));
		assertFalse(Util.isPowerOfTwo((long) Integer.MAX_VALUE));
		assertFalse(Util.isPowerOfTwo(12345l));
		assertFalse(Util.isPowerOfTwo(0l));
		assertFalse(Util.isPowerOfTwo(-1l));
		assertFalse(Util.isPowerOfTwo(-2l));
		assertFalse(Util.isPowerOfTwo((long) Integer.MIN_VALUE));
		assertFalse(Util.isPowerOfTwo(Long.MIN_VALUE));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetExponentOfTwoChecksForPowerOfTwo() {
		final int nonPowerOfTwo = 13;
		Util.getExponentOfTwo(nonPowerOfTwo);
	}

	@Test
	public void testGetExponentOfTwo() {
		for (int exp = 0; exp < 31; exp++) {
			final int power = 1 << exp; // = 2^exp
			// Teste positive 2er-Potenzen
			assertEquals("For 2^" + exp + "=" + power, exp,
					Util.getExponentOfTwo(power));
			// Teste negative 2er-Potenzen
			assertEquals("For -2^" + exp + "=" + -power, exp,
					Util.getExponentOfTwo(power));
		}
	}

	@Test
	public void testGetNextPowerOfTwo() {
		assertEquals(4, Util.getNextPowerOfTwo(3));
		assertEquals(4, Util.getNextPowerOfTwo(4));
		assertEquals(8, Util.getNextPowerOfTwo(5));
		assertEquals(128, Util.getNextPowerOfTwo(111));

		assertEquals(1, Util.getNextPowerOfTwo(-1));
		assertEquals(1, Util.getNextPowerOfTwo(0));
		assertEquals(1, Util.getNextPowerOfTwo(1));
		assertEquals(1, Util.getNextPowerOfTwo(Integer.MIN_VALUE));
		assertEquals(1 << 30, Util.getNextPowerOfTwo((1 << 30) - 1));
		assertEquals(Integer.MIN_VALUE,
				Util.getNextPowerOfTwo(Integer.MAX_VALUE));
	}

	@Test
	public void testLog2() {
		assertEquals(1.584962500721156, Util.log2(3), COMPARISION_DELTA);
		assertEquals(1, Util.log2(2), COMPARISION_DELTA);
		assertEquals(0, Util.log2(1), COMPARISION_DELTA);
		assertEquals(10, Util.log2(1024), COMPARISION_DELTA);
		assertTrue(Double.isInfinite(Util.log2(0)));
		assertTrue(Double.isNaN(Util.log2(-2)));
	}

	@Test
	public void testLog() {
		// Logarithmus zur Basis 2
		assertEquals(Util.log2(3), Util.log(2, 3), COMPARISION_DELTA);
		assertEquals(Util.log2(2), Util.log(2, 2), COMPARISION_DELTA);
		assertEquals(Util.log2(1), Util.log(2, 1), COMPARISION_DELTA);
		assertEquals(Util.log2(1024), Util.log(2, 1024), COMPARISION_DELTA);
		assertTrue(Double.isInfinite(Util.log(2, 0)));
		assertTrue(Double.isNaN(Util.log(2, -2)));

		// Logarithmus zur Basis 10
		assertEquals(0.3010299956639812, Util.log(10, 2), COMPARISION_DELTA);
		assertEquals(0, Util.log(10, 1), COMPARISION_DELTA);
		assertEquals(5, Util.log(10, 100000), COMPARISION_DELTA);
		assertTrue(Double.isNaN(Util.log(10, -2)));
		assertTrue(Double.isInfinite(Util.log(10, 0)));

		// Logarithmus zur Basis e
		assertEquals(2.302585092994046, Util.log(Math.E, 10), COMPARISION_DELTA);
		assertEquals(0, Util.log(Math.E, 1), COMPARISION_DELTA);
		assertTrue(Double.isNaN(Util.log(Math.E, -1)));
		assertTrue(Double.isInfinite(Util.log(Math.E, 0)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSamplesToDurationChecksForNegativeSampleRate() {
		Util.samplesToDuration(0, -1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSamplesToDurationChecksForZeroSampleRate() {
		Util.samplesToDuration(0, 0);
	}

	@Test
	public void testSamplesToDuration() {
		final double sampleRate = 44100;
		assertEquals(Duration.ZERO, Util.samplesToDuration(0, sampleRate));
		assertEquals(Duration.ofSeconds(1),
				Util.samplesToDuration(44100, sampleRate));
		assertEquals(Duration.ofMillis(500),
				Util.samplesToDuration(22050, sampleRate));
		assertEquals(Duration.ofMillis(-500),
				Util.samplesToDuration(-22050, sampleRate));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDurationToSamplesChecksForNegativeSampleRate() {
		Util.durationToSamples(Duration.ZERO, -1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDurationToSamplesForZeroSampleRate() {
		Util.durationToSamples(Duration.ZERO, 0);
	}

	@Test
	public void testDurationToSamples() {
		final double sampleRate = 44100;
		assertEquals(Util.durationToSamples(Duration.ZERO, sampleRate), 0);
		assertEquals(Util.durationToSamples(Duration.ofSeconds(1), sampleRate),
				44100);
		assertEquals(
				Util.durationToSamples(Duration.ofMillis(500), sampleRate),
				22050);
		assertEquals(
				Util.durationToSamples(Duration.ofMillis(-500), sampleRate),
				-22050);
	}

	@Test
	public void testInterpolateLinear() {
		assertEquals(2.5, Util.interpolateLinear(5, 10, -0.5),
				COMPARISION_DELTA);
		assertEquals(5, Util.interpolateLinear(5, 10, 0), COMPARISION_DELTA);
		assertEquals(7.5, Util.interpolateLinear(5, 10, 0.5), COMPARISION_DELTA);
		assertEquals(10, Util.interpolateLinear(5, 10, 1), COMPARISION_DELTA);
		assertEquals(12.5, Util.interpolateLinear(5, 10, 1.5),
				COMPARISION_DELTA);
		assertEquals(-1, Util.interpolateLinear(-5, 3, 0.5), COMPARISION_DELTA);
		assertEquals(10, Util.interpolateLinear(10, 5, 0.0), COMPARISION_DELTA);
		assertEquals(7.5, Util.interpolateLinear(10, 5, 0.5), COMPARISION_DELTA);
		assertEquals(5, Util.interpolateLinear(10, 5, 1), COMPARISION_DELTA);
	}
}
