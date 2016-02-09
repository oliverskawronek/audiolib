package de.skawronek.audiolib.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.Test;

import de.skawronek.audiolib.TestUtil;
import de.skawronek.audiolib.math.AdaptivePeakPicking;

public final class AdaptivePeakPickingTest {
	private final Random random = new Random(12345);

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorChecksWindowSize() {
		final int windowSize = 0;
		new AdaptivePeakPicking(windowSize, 0f, 1f);
	}

	@Test
	public void testGetPeaksIsEmptyAtStart() {
		final AdaptivePeakPicking app = new AdaptivePeakPicking(1, 0f, 1f);
		assertTrue(app.getPeaks().isEmpty());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetPeaksReturnsUnmodifiableList() {
		final AdaptivePeakPicking app = new AdaptivePeakPicking(1, 0f, 1f);
		final float[] x = new float[] { 0f };
		app.compute(x);
		final List<Integer> peaks = app.getPeaks();
		// Aufruf von add auf unmodifiable List löst
		// UnsupportedOperationException aus
		peaks.add(0);
	}

	@Test
	public void testCompute() {
		final int numSamples = 100;
		for (int i = 0; i < numSamples; i++) {
			testComputeSingle();
		}
	}

	private void testComputeSingle() {
		final int windowSize = TestUtil.random(random, 5, 25);
		final float baseThreshold = TestUtil.random(random, 0f, 5f);
		final float weight = TestUtil.random(random, 0f, 1f);
		final AdaptivePeakPicking app = new AdaptivePeakPicking(windowSize,
				baseThreshold, weight);

		final int size = TestUtil.random(random, 16, 2048);
		final float[] x = TestUtil.generateRandomWindow(random, size, -100f,
				100f);

		app.compute(x);
		final List<Integer> peaks = app.getPeaks();

		// Es kann nicht mehr Peaks geben, als x groß ist
		assertTrue(peaks.size() <= x.length);

		for (final int idx : peaks) {
			// Prüfe ob Peak-Index innerhalb von 0..x.length-1 liegt
			assertTrue(0 <= idx && idx < x.length);

			// Prüfe ob Peak lokales Maximum ist
			final float left = x[idx - 1];
			final float right = x[idx + 1];
			assertTrue(left < x[idx] && x[idx] > right);
		}

		// Prüfe ob Indizes aufsteigend geordnet sind und nicht doppelt
		// vorkommen
		int lastIndex = Integer.MIN_VALUE;
		for (int i = 0; i < peaks.size(); i++) {
			final int index = peaks.get(i);
			assertTrue(index > lastIndex);

			lastIndex = index;
		}
	}

	@Test
	public void testComputeConcrete() {
		final float[] x = new float[] { 0, 0, 1, 2, 5, 3, 0, 0, 0, 2, 3, 4, 3,
				0, 0, 0, 0 };
		AdaptivePeakPicking app = new AdaptivePeakPicking(5, 0, 1f);
		app.compute(x);
		final List<Integer> peaks = app.getPeaks();
		assertTrue(peaks.contains(4));
		assertTrue(peaks.contains(11));
	}

	@Test
	public void testBaseThreshold() {
		// Generiere x so, dass
		// 1) x[5*k + 2] = k + 2 ist und
		// 2) x[5*k + 2] lokales Maximum ist
		final int size = 500;
		final float[] x = new float[size];
		for (int k = 0; k < size / 5; k++) {
			final float peak = k + 2;
			x[5 * k] = peak - 2;
			x[5 * k + 1] = peak - 1;
			x[5 * k + 2] = peak;
			x[5 * k + 3] = peak - 1;
			x[5 * k + 4] = peak - 2;
		}

		// Setze weight zu Null so, dass der Median beim Threshold keine Rolle
		// spielt.
		final float weight = 0f;
		for (int threshold = 1; threshold <= 101; threshold++) {
			AdaptivePeakPicking app = new AdaptivePeakPicking(5, threshold,
					weight);
			app.compute(x);
			final List<Integer> peaks = app.getPeaks();

			assertEquals((101 - threshold), peaks.size());
			for (int k = threshold; k < size / 5; k++) {
				assertTrue(peaks.contains(5 * k + 2));
			}
		}
	}
}
