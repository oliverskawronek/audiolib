package de.skawronek.audiolib.math;

import org.junit.Test;
import static org.junit.Assert.*;
import static de.skawronek.audiolib.math.ComplexNumber.*;

public final class ComplexNumberTest {
	private static final double COMPARISON_DELTA = 0.00000000001;

	@Test
	public void testMagnitude() {
		assertEquals(3.6055512754639893, magnitude(new double[] { 2, 3 }),
				COMPARISON_DELTA);
		assertEquals(3.6055512754639893, magnitude(new double[] { -2, 3 }),
				COMPARISON_DELTA);
		assertEquals(3.6055512754639893, magnitude(new double[] { 2, -3 }),
				COMPARISON_DELTA);
		assertEquals(3.6055512754639893, magnitude(new double[] { -2, -3 }),
				COMPARISON_DELTA);
	}

	@Test
	public void testConjugate() {
		final double[] z = new double[] { 2, 3 };
		final double[] expected = new double[] { 2, -3 };
		final double[] actual = new double[2];
		conjugate(z, actual);
		assertArrayEquals(expected, actual, COMPARISON_DELTA);
	}

	@Test
	public void testFromEuler() {
		final double[] expected = new double[] { Math.sqrt(2), Math.sqrt(2) };
		final double[] actual = new double[2];
		fromEuler(2.0, Math.PI / 4.0, actual);
		assertArrayEquals(expected, actual, COMPARISON_DELTA);
	}

	@Test
	public void testToEulerR() {
		assertEquals(2.0,
				toEulerR(new double[] { Math.sqrt(2), Math.sqrt(2) }),
				COMPARISON_DELTA);
	}

	@Test
	public void testToEulerPhi() {
		assertEquals(Math.PI / 4,
				toEulerPhi(new double[] { Math.sqrt(2), Math.sqrt(2) }),
				COMPARISON_DELTA);
	}

	@Test
	public void testAdd() {
		final double[] r = new double[2];
		add(new double[] { 2, -3 }, new double[] { -5, 6 }, r);
		assertArrayEquals(new double[] { -3, 3 }, r, COMPARISON_DELTA);
	}

	@Test
	public void testSubstract() {
		final double[] r = new double[2];
		substract(new double[] { 2, -3 }, new double[] { -5, 6 }, r);
		assertArrayEquals(new double[] { 7, -9 }, r, COMPARISON_DELTA);
	}

	@Test
	public void testMultiplyComplex() {
		final double[] r = new double[2];
		multiply(new double[] { 2, -3 }, new double[] { -5, 6 }, r);
		assertArrayEquals(new double[] { 8, 27 }, r, COMPARISON_DELTA);
	}

	@Test
	public void testMultiplyFactor() {
		final double[] r = new double[2];
		multiply(-3, new double[] { 4, -5 }, r);
		assertArrayEquals(new double[] { -12, 15 }, r, COMPARISON_DELTA);
	}

	@Test
	public void testDivide() {
		final double[] r = new double[2];
		divide(new double[] { 2, -3 }, new double[] { -5, 6 }, r);
		assertArrayEquals(new double[] { -28.0 / 61.0, 3.0 / 61.0 }, r,
				COMPARISON_DELTA);
	}

	@Test
	public void testPower() {
		final double[] result = new double[2];
		power(new double[] { 1.0, 1.0 }, 8.0, result);
		assertEquals(16.0, result[0], COMPARISON_DELTA);
		assertEquals(0.0, result[1], COMPARISON_DELTA);
	}
}
