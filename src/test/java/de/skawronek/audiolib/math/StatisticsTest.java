package de.skawronek.audiolib.math;

import java.util.Random;

import org.junit.Test;

import de.skawronek.audiolib.TestUtil;
import de.skawronek.audiolib.math.Statistics;
import static org.junit.Assert.*;

public final class StatisticsTest {
	private static final float COMPARISON_DELTA = 0.02f;

	private final Statistics statistics = new Statistics();
	private final Random random = new Random(12345);

	@Test(expected = IllegalArgumentException.class)
	public void testSetInputCheckSize() {
		final float[] empty = new float[0];
		statistics.setInput(empty);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetMinimumChecksInputIsSet() {
		statistics.getMininum();
	}

	@Test
	public void testGetMinimum() {
		final int numSamples = 100;
		for (int i = 0; i < numSamples; i++) {
			testGetMinimumSingle();
		}
	}

	private void testGetMinimumSingle() {
		final int size = TestUtil.random(random, 1, 5000);
		final float[] x = TestUtil.generateRandomWindow(random, size);
		statistics.setInput(x);
		final float min = statistics.getMininum();

		// Ist das Minum kleiner oder gleich allen Elementen?
		for (int i = 0; i < x.length; i++) {
			if (x[i] < min) {
				fail();
			}
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testGetMaximumChecksInputIsSet() {
		statistics.getMaximum();
	}

	@Test
	public void testGetMaximum() {
		final int numSamples = 100;
		for (int i = 0; i < numSamples; i++) {
			testGetMaximumSingle();
		}
	}

	private void testGetMaximumSingle() {
		final int size = TestUtil.random(random, 1, 5000);
		final float[] x = TestUtil.generateRandomWindow(random, size);
		statistics.setInput(x);
		final float max = statistics.getMaximum();

		// Ist das Maximum größer oder gleich allen Elementen?
		for (int i = 0; i < x.length; i++) {
			if (x[i] > max) {
				fail();
			}
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testGetAverageChecksInputIsSet() {
		statistics.getAverage();
	}

	@Test
	public void testGetAverage() {
		final int numSamples = 100;
		for (int i = 0; i < numSamples; i++) {
			testGetAverageSingle();
		}
	}

	private void testGetAverageSingle() {
		final int size = TestUtil.random(random, 1, 5000);
		final float[] x = TestUtil.generateRandomWindow(random, size, -100f,
				100f);
		statistics.setInput(x);
		final float avg = statistics.getAverage();

		// Ist die Summe aller Abweichungen vom Mittelwert Null?
		float sum = 0f;
		for (int i = 0; i < x.length; i++) {
			sum += (avg - x[i]);
		}
		assertEquals(0f, sum, COMPARISON_DELTA);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetMedianChecksInputIsSet() {
		statistics.getMedian();
	}

	@Test
	public void testGetMedian() {
		final int numSamples = 100;
		for (int i = 0; i < numSamples; i++) {
			testGetmedianSingle();
		}
	}

	private void testGetmedianSingle() {
		final int size = TestUtil.random(random, 1, 5000);
		final float[] x = TestUtil.generateRandomWindow(random, size);
		statistics.setInput(x);
		final float median = statistics.getMedian();

		// Sei m der Median und X eine Zufallsgröße, dann gilt:
		// P(X <= m) >= 1/2 && P(X >= m) >= 1/2
		int lteCount = 0;
		int gteCount = 0;
		for (int i = 0; i < x.length; i++) {
			if (x[i] <= median) {
				lteCount++;
			}
			if (x[i] >= median) {
				gteCount++;
			}
		}
		assertTrue((double) lteCount / (double) size >= 0.5);
		assertTrue((double) gteCount / (double) size >= 0.5);
	}
}
