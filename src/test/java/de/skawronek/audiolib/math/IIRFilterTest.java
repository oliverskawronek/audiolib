package de.skawronek.audiolib.math;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

import de.skawronek.audiolib.TestUtil;
import static org.junit.Assert.*;

public final class IIRFilterTest {
	private static final float COMPARISON_DELTA = 0.00001f;

	private final Random random = new Random(12345);

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorCheckAForEmptyness() {
		new IIRFilter(new double[0], new double[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFilterChecksInAndOutLength() {
		final float[] in = new float[10];
		final float[] out = new float[11];
		final IIRFilter nop = new IIRFilter(new double[] { 1.0 }, new double[0]);
		nop.filter(in, out);
	}

	@Test
	public void testNopFilterDoNothing() {
		final int length = 2048;
		final float[] x = TestUtil
				.generateRandomWindow(random, length, -5f, 5f);
		final float[] y = new float[length];
		final IIRFilter nop = new IIRFilter(new double[] { 1.0 },
				new double[] { 1.0d });
		nop.filter(x, y);
		assertArrayEquals(x, y, COMPARISON_DELTA);
	}

	@Test
	public void testFilterWithOrderTwoFilter() {
		final float[] x = new float[] { 2f, -3f, 0f, 2f, 0f, 0f, 0f, 0f };
		final double[] b = new double[] { 0.6, 0.4 };
		final double[] a = new double[] { 1.0, 0.5, 0.8 };
		final float[] expectedY = new float[] { 1.2f, -1.6f, -1.36f, 3.16f,
				0.308f, -2.682f, 1.0946f, 1.5983f };

		final float[] actualY = new float[8];
		final IIRFilter filter = new IIRFilter(b, a);
		filter.filter(x, actualY);
		assertArrayEquals(expectedY, actualY, COMPARISON_DELTA);
	}

	@Test
	public void testFilterWorksInSitu() {
		// Testet, ob filter auch auf dem selben Array arbeiten kann.
		// Damit ist kein zweites Array nötig.
		final double[] b = new double[] { 0.6, 0.4 };
		final double[] a = new double[] { 1.0, 0.5, 0.8 };
		final IIRFilter filter = new IIRFilter(b, a);

		final int length = 2048;
		final float[] x = TestUtil
				.generateRandomWindow(random, length, -5f, 5f);
		final float[] expectedY = new float[length];
		filter.filter(x, expectedY);

		final float[] actualY = Arrays.copyOf(x, length);
		// Filter auf dem selben Array
		filter.filter(actualY, actualY);
		assertArrayEquals(expectedY, actualY, COMPARISON_DELTA);
	}

	@Test
	public void testGetFeedbackCoefficientsUnormalizedDontModifyCoefficients() {
		final double[] b = new double[] {};
		final double[] expectedA = new double[] { 2.0, 3.0, 4.0 };
		final IIRFilter filter = new IIRFilter(b, expectedA);
		final double[] actualA = filter.getFeedbackCoefficients(false);
		assertArrayEquals(expectedA, actualA, COMPARISON_DELTA);
	}

	@Test
	public void testGetFeedbackCoefficientsNormalized() {
		final double[] b = new double[] {};
		final double[] a = new double[] { 2.0, 3.0, 4.0 };
		final IIRFilter filter = new IIRFilter(b, a);
		final double[] expectedA = new double[] { 1.0, 1.5, 2.0 };
		final double[] actualA = filter.getFeedbackCoefficients(true);
		assertArrayEquals(expectedA, actualA, COMPARISON_DELTA);
	}

	@Test
	public void testGetFeedforwardCoefficientsUnormalizedDontModifyCoefficients() {
		final double[] expectedB = new double[] { 2.0, 3.0, 4.0 };
		final double[] a = new double[] { 2.0 };
		final IIRFilter filter = new IIRFilter(expectedB, a);
		final double[] actualB = filter.getFeedforwardCoefficients(false);
		assertArrayEquals(expectedB, actualB, COMPARISON_DELTA);
	}

	@Test
	public void testGetFeedforwardCoefficientsNormalized() {
		final double[] b = new double[] { 2.0, 3.0, 4.0 };
		final double[] a = new double[] { 2.0 };
		final IIRFilter filter = new IIRFilter(b, a);
		final double[] expectedB = new double[] { 1.0, 1.5, 2.0 };
		final double[] actualB = filter.getFeedforwardCoefficients(true);
		assertArrayEquals(expectedB, actualB, COMPARISON_DELTA);
	}

	@Test
	public void testAmplitudeResponse() {
		// Butterworth Low Pass (Order 2, Cutoff 0.2)
		final double[] b = new double[] { 0.067455, 0.134911, 0.067455 };
		final double[] a = new double[] { 1.00000, -1.14298, 0.41280 };
		final IIRFilter filter = new IIRFilter(b, a);

		assertEquals(1.0, filter.amplitudeResponse(0.0), COMPARISON_DELTA);
		assertEquals(1 / Math.sqrt(2), filter.amplitudeResponse(0.2 * Math.PI),
				COMPARISON_DELTA);
		assertEquals(0.0, filter.amplitudeResponse(Math.PI), COMPARISON_DELTA);
	}

	// TODO Teste phaseResponse
}
