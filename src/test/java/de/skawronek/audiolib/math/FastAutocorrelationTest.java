package de.skawronek.audiolib.math;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;

import de.skawronek.audiolib.TestUtil;
import de.skawronek.audiolib.math.FastAutocorrelation;

public final class FastAutocorrelationTest {
	private static final float COMPARISON_DELTA = 0.0001f;

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorCheckForNegativeSize() {
		new FastAutocorrelation(-1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorCheckForZeroSize() {
		new FastAutocorrelation(0);
	}

	@Test
	public void testGetMaxInputSizeIsGreaterThanSize() {
		for (int size = 1; size < 1024; size++) {
			final FastAutocorrelation acf = new FastAutocorrelation(size);
			assertTrue(acf.getMaxInputSize() >= size);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testComputeChecksSize() {
		final FastAutocorrelation acf = new FastAutocorrelation(1024);
		final int maxInputSize = acf.getMaxInputSize();
		final float[] x = new float[maxInputSize + 1];
		acf.compute(x);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCopyCoefficientsChecksSize() {
		final int size = 1024;
		final FastAutocorrelation acf = new FastAutocorrelation(size);
		final float[] x = new float[size];
		final float[] rxx = new float[size - 1];
		acf.compute(x);
		acf.copyCoefficients(rxx);
	}

	@Test
	public void testCompute() {
		// Teste mit einer gerade Länge
		float[] x = new float[] { 5, 2, 3, -7 };
		float[] expectedRxx = new float[] { 87, -5, 1, -35 };
		float[] actualRxx = computeFastAutocorrelation(x, false);
		assertEquals(0f, TestUtil.computeRMSE(expectedRxx, actualRxx),
				COMPARISON_DELTA);

		// Teste mit einer ungeraden Länge
		x = new float[] { 5, 2, 3, -7, 9 };
		expectedRxx = new float[] { 168, -68, 28, -17, 45 };
		actualRxx = computeFastAutocorrelation(x, false);
		assertEquals(0f, TestUtil.computeRMSE(expectedRxx, actualRxx),
				COMPARISON_DELTA);

		// Teste mit einemm Einzelwert
		x = new float[] { 7 };
		expectedRxx = new float[] { 49 };
		actualRxx = computeFastAutocorrelation(x, false);
		assertEquals(0f, TestUtil.computeRMSE(expectedRxx, actualRxx),
				COMPARISON_DELTA);
	}

	@Test
	public void testNormalize() {
		final int size = 500;
		final int numSamples = 100;
		final Random random = new Random(12345);
		for (int i = 0; i < numSamples; i++) {
			final float[] x = TestUtil.generateRandomWindow(random, size);
			final float[] rxx = computeFastAutocorrelation(x, true);
			testNormalizeProperties(rxx);
		}
	}

	private void testNormalizeProperties(final float @NonNull [] rxx) {
		// Erste Koeffizient ist Eins
		assertEquals(1, rxx[0], COMPARISON_DELTA);
		// Erste Koeffizient ist Maximum
		for (int j = 1; j < rxx.length; j++) {
			assertTrue(rxx[0] >= rxx[j]);
		}
	}

	@Test
	public void testComputeAgainstNaiveCompution() {
		final int size = 500;
		final int numSamples = 100;
		final Random random = new Random(12345);
		for (int i = 0; i < numSamples; i++) {
			final float[] x = TestUtil.generateRandomWindow(random, size);
			final float[] expectedRxx = computeNaiveAutocorrelation(x, false);
			final float[] actualRxx = computeFastAutocorrelation(x, false);
			assertEquals(0, TestUtil.computeRMSE(actualRxx, expectedRxx),
					COMPARISON_DELTA);
		}
	}

	private static float[] computeFastAutocorrelation(
			final float @NonNull [] x, final boolean normalize) {
		final FastAutocorrelation acf = new FastAutocorrelation(x.length);
		final float[] rxx = new float[x.length];
		acf.compute(x);
		if (normalize) {
			acf.normalize();
		}
		acf.copyCoefficients(rxx);
		return rxx;
	}

	private static float[] computeNaiveAutocorrelation(
			final float @NonNull [] x, final boolean normalize) {
		final int size = x.length;
		final float[] rxx = new float[size];
		for (int lag = 0; lag < size; lag++) {
			rxx[lag] = 0f;
			for (int i = 0; i < size - lag; i++) {
				rxx[lag] += x[i] * x[i + lag];
			}
		}

		if (normalize) {
			final float max = rxx[0];
			if (max == 0) {
				rxx[0] = 1;
				Arrays.fill(rxx, 1, size, 0f);
			} else {
				final float normalizationFactor = 1 / max;
				for (int lag = 0; lag < size; lag++) {
					rxx[lag] *= normalizationFactor;
				}
			}
		}

		return rxx;
	}
}
