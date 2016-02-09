package de.skawronek.audiolib.math;

import static org.junit.Assert.*;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;

import de.skawronek.audiolib.math.WindowFunctions;
import de.skawronek.audiolib.math.WindowFunctions.IWindowFunction;

public final class WindowFunctionsTest {
	private static final double COMPARISON_DELTA = 0.0000000001;

	@Test
	public void testRectangularWindow() {
		final IWindowFunction wf = WindowFunctions.getRectangularWindow();
		assertEquals(1, wf.evaluate(-0.5), COMPARISON_DELTA);
		assertEquals(1, wf.evaluate(-0.25), COMPARISON_DELTA);
		assertEquals(1, wf.evaluate(0), COMPARISON_DELTA);
		assertEquals(1, wf.evaluate(0.25), COMPARISON_DELTA);
		assertEquals(1, wf.evaluate(0.5), COMPARISON_DELTA);
	}

	@Test
	public void testRectangularWindowIsSymmetric() {
		testSymmetry(WindowFunctions.getRectangularWindow());
	}

	@Test
	public void testRectangularWindowIsEverywhereDefined() {
		testEverywhereDefined(WindowFunctions.getRectangularWindow());
	}

	@Test
	public void testBartlettWindow() {
		final IWindowFunction wf = WindowFunctions.getBartlettWindow();
		assertEquals(0, wf.evaluate(-0.5), COMPARISON_DELTA);
		assertEquals(0.5, wf.evaluate(-0.25), COMPARISON_DELTA);
		assertEquals(1, wf.evaluate(0), COMPARISON_DELTA);
		assertEquals(0.5, wf.evaluate(0.25), COMPARISON_DELTA);
		assertEquals(0, wf.evaluate(0.5), COMPARISON_DELTA);
	}

	@Test
	public void testBartlettWindowIsSymmetric() {
		testSymmetry(WindowFunctions.getBartlettWindow());
	}

	@Test
	public void testBartlettWindowIsEverywhereDefined() {
		testEverywhereDefined(WindowFunctions.getBartlettWindow());
	}

	@Test
	public void testHammingWindowIsSymmetric() {
		testSymmetry(WindowFunctions.getHammingWindow());
	}

	@Test
	public void testHammingWindowIsEverywhereDefined() {
		testEverywhereDefined(WindowFunctions.getHammingWindow());
	}

	@Test
	public void testHannWindowIsSymmetric() {
		testSymmetry(WindowFunctions.getHannWindow());
	}

	@Test
	public void testHannWindowIsEverywhereDefined() {
		testEverywhereDefined(WindowFunctions.getHannWindow());
	}

	@Test
	public void testBlackmanWindowIsSymmetric() {
		testSymmetry(WindowFunctions.getBlackmanWindow());
	}

	@Test
	public void testBlackmanWindowIsEverywhereDefined() {
		testEverywhereDefined(WindowFunctions.getBlackmanWindow());
	}

	private void testSymmetry(final @NonNull IWindowFunction wf) {
		final int numPoints = 100;
		for (int i = 0; i < numPoints; i++) {
			final double positiveT = 0.5 * ((double) i / (double) numPoints);
			final double negativeT = -positiveT;
			final double diff = Math.abs(wf.evaluate(negativeT)
					- wf.evaluate(positiveT));
			assertEquals(0, diff, COMPARISON_DELTA);
		}
	}

	private void testEverywhereDefined(final @NonNull IWindowFunction wf) {
		final int numPoints = 100;
		for (int i = 0; i < numPoints; i++) {
			final double t = (1d / numPoints) - 0.5;
			final double value = wf.evaluate(t);
			assertFalse(Double.isNaN(value));
			assertFalse(Double.isInfinite(value));
		}
	}
}
