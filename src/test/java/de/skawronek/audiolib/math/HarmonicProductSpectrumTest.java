package de.skawronek.audiolib.math;

import java.util.Random;

import org.junit.Test;

import de.skawronek.audiolib.TestUtil;
import de.skawronek.audiolib.math.HarmonicProductSpectrum;
import static org.junit.Assert.*;

public final class HarmonicProductSpectrumTest {
	private static final float COMPARISON_DELTA = 0.0000001f;

	private final Random random = new Random(12345);

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorChecksForZeroSize() {
		new HarmonicProductSpectrum(0, 2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorChecksForNegativeSize() {
		new HarmonicProductSpectrum(-1, 2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorChecksNumHarmonics() {
		new HarmonicProductSpectrum(1024, 0);
	}

	@Test
	public void testGetSize() {
		final int expectedSize = 1024;
		final HarmonicProductSpectrum hps = new HarmonicProductSpectrum(
				expectedSize, 1);
		assertEquals(expectedSize, hps.getSize());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testComputeChecksSize() {
		final int size = 1024;
		final HarmonicProductSpectrum hps = new HarmonicProductSpectrum(size, 1);
		final float[] spectrum = new float[size + 1];
		hps.compute(spectrum);
	}

	@Test
	public void testHpsIsAlwaysPositive() {
		final int size = 1024;
		final int numSamples = 100;

		final float[] hpsResult = new float[size];
		for (int i = 0; i < numSamples; i++) {
			final int numHarmonics = (int) (random.nextFloat() * 4 + 1);
			assert 1 >= numHarmonics && numHarmonics <= 4;
			final HarmonicProductSpectrum hps = new HarmonicProductSpectrum(
					size, 3);
			final float[] positiveSpectrum = TestUtil.generateRandomWindow(
					random, size, 0f, 100f);
			hps.compute(positiveSpectrum);
			hps.copyHps(hpsResult);

			for (int j = 0; j < hpsResult.length; j++) {
				assertTrue(hpsResult[j] >= 0f);
			}
		}
	}

	@Test
	public void testComputeWithOneHarmonic() {
		final float[] expected = new float[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		final HarmonicProductSpectrum hps = new HarmonicProductSpectrum(
				expected.length, 1);
		hps.compute(expected);
		final float[] actual = new float[hps.getSize()];
		hps.copyHps(actual);

		// Alle Bins sind gleich
		final float rmse = TestUtil.computeRMSE(expected, actual);
		assertEquals(0f, rmse, COMPARISON_DELTA);
	}

	@Test
	public void testComputeWithTwoHarmonics() {
		final float[] spectrum = new float[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		final HarmonicProductSpectrum hps = new HarmonicProductSpectrum(
				spectrum.length, 2);
		hps.compute(spectrum);
		final float[] actual = new float[hps.getSize()];
		hps.copyHps(actual);

		assertEquals(0f * 0f, actual[0], COMPARISON_DELTA);
		assertEquals(1f * 2f, actual[1], COMPARISON_DELTA);
		assertEquals(2f * 4f, actual[2], COMPARISON_DELTA);
		assertEquals(3f * 6f, actual[3], COMPARISON_DELTA);
		assertEquals(4f * 8f, actual[4], COMPARISON_DELTA);
		assertEquals(0f, actual[5], COMPARISON_DELTA);
		assertEquals(0f, actual[6], COMPARISON_DELTA);
		assertEquals(0f, actual[7], COMPARISON_DELTA);
		assertEquals(0f, actual[8], COMPARISON_DELTA);
		assertEquals(0f, actual[9], COMPARISON_DELTA);
	}

	@Test
	public void testComputeWithThreeHarmonics() {
		final float[] spectrum = new float[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		final HarmonicProductSpectrum hps = new HarmonicProductSpectrum(
				spectrum.length, 3);
		hps.compute(spectrum);
		final float[] actual = new float[hps.getSize()];
		hps.copyHps(actual);

		assertEquals(0f * 0f * 0f, actual[0], COMPARISON_DELTA);
		assertEquals(1f * 2f * 3f, actual[1], COMPARISON_DELTA);
		assertEquals(2f * 4f * 6f, actual[2], COMPARISON_DELTA);
		assertEquals(3f * 6f * 9f, actual[3], COMPARISON_DELTA);
		assertEquals(0f, actual[4], COMPARISON_DELTA);
		assertEquals(0f, actual[5], COMPARISON_DELTA);
		assertEquals(0f, actual[6], COMPARISON_DELTA);
		assertEquals(0f, actual[7], COMPARISON_DELTA);
		assertEquals(0f, actual[8], COMPARISON_DELTA);
		assertEquals(0f, actual[9], COMPARISON_DELTA);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCopyHpsChecksSize() {
		final float[] spectrum = new float[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		final HarmonicProductSpectrum hps = new HarmonicProductSpectrum(
				spectrum.length, 3);
		hps.compute(spectrum);
		// Buffer ist zu klein
		final float[] buffer = new float[spectrum.length - 1];
		hps.copyHps(buffer);
	}
}
