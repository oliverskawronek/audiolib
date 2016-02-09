package de.skawronek.audiolib.math;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import javafx.util.Pair;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;

import de.skawronek.audiolib.TestUtil;
import de.skawronek.audiolib.math.DynamicTimeWarping.IDistanceFunction;
import static org.junit.Assert.*;

public final class DynamicTimeWarpingTest {
	private final DynamicTimeWarping dtw = new DynamicTimeWarping();
	private final Random random = new Random(12345);

	//@formatter:off
	private static final IDistanceFunction<Integer> intDistance =
			(Integer fx, Integer fy) -> Math.abs(fx - fy);
	private static final IDistanceFunction<Float> floatDistance =
			(Float fx, Float fy) -> Math.abs(fx - fy);
	//@formatter:on

	@Test(expected = IllegalArgumentException.class)
	public void testComputeChecksForEmptyXSeries() {
		final List<Integer> empty = new ArrayList<>();
		final List<Integer> seriesY = new ArrayList<>(1);
		dtw.compute(empty, seriesY, intDistance);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testComputeChecksForEmptyYSeries() {
		final List<Integer> seriesX = new ArrayList<>(1);
		final List<Integer> empty = new ArrayList<>();
		dtw.compute(seriesX, empty, intDistance);
	}

	@Test
	public void testPathConditions() {
		final int numSamples = 100;
		for (int i = 0; i < numSamples; i++) {
			final int xLength = random.nextInt(40) + 1;
			final int yLength = random.nextInt(40) + 1;
			testPathConditions(xLength, yLength);
		}
	}

	private void testPathConditions(final int xLength, final int yLength) {
		final Float[] seriesX = boxFloatArray(TestUtil.generateRandomWindow(
				random, xLength));
		final Float[] seriesY = boxFloatArray(TestUtil.generateRandomWindow(
				random, yLength));
		dtw.compute(seriesX, seriesY, floatDistance);

		// Boundary Condition
		Iterator<Pair<Integer, Integer>> path = dtw.getWarpingPath();
		Pair<Integer, Integer> oldPair = path.next();
		assertPairEquals(xLength - 1, yLength - 1, oldPair);

		// Step Size Condition
		while (path.hasNext()) {
			final Pair<Integer, Integer> curr = path.next();
			final int diffX = oldPair.getKey() - curr.getKey();
			final int diffY = oldPair.getValue() - curr.getValue();
			assertTrue(diffX <= 1 && diffY <= 1);
			oldPair = curr;
		}

		// Boundary Condition
		assertPairEquals(0, 0, oldPair);
	}

	@NonNull
	private Float[] boxFloatArray(final float @NonNull [] a) {
		final Float[] r = new Float[a.length];
		for (int i = 0; i < a.length; i++) {
			r[i] = a[i];
		}
		return r;
	}

	@Test
	public void testCompute() {
		final Integer[] xs = { 2, 4, 5, 5, 6 };
		final Integer[] ys = { 1, 2, 4, 4, 5, 6 };
		dtw.compute(xs, ys, intDistance);

		assertEquals(1, dtw.getTotallDistance(), 0.000001d);

		final Iterator<Pair<Integer, Integer>> path = dtw.getWarpingPath();
		assertPairEquals(4, 5, path.next());
		assertPairEquals(3, 4, path.next());
		assertPairEquals(2, 4, path.next());
		assertPairEquals(1, 3, path.next());
		assertPairEquals(1, 2, path.next());
		assertPairEquals(0, 1, path.next());
		assertPairEquals(0, 0, path.next());
		assertFalse(path.hasNext());
	}

	@Test
	public void testNextCheckIfHasNext() {
		final Integer[] xs = { 1 };
		final Integer[] ys = { 1 };
		dtw.compute(xs, ys, intDistance);

		final Iterator<Pair<Integer, Integer>> path = dtw.getWarpingPath();
		assertTrue(path.hasNext());
		path.next();
		assertFalse(path.hasNext());
		try {
			path.next();
			fail();
		} catch (final NoSuchElementException e) {
			// Alles ist gut
		}
	}

	private static void assertPairEquals(final int expectedKey,
			final int expectedValue,
			final @NonNull Pair<Integer, Integer> actual) {
		if (actual.getKey() != expectedKey
				|| actual.getValue() != expectedValue) {
			//@formatter:off
			final String message = String.format(
					"Expected (%d, %d), but was (%d, %d)",
					expectedKey, expectedValue,
					actual.getKey(), actual.getValue());
			//@formatter:on
			fail(message);
		}
	}
}
