package de.skawronek.audiolib.math;

import static java.lang.Math.*;
import static org.junit.Assert.*;

import java.util.Random;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;

import de.skawronek.audiolib.TestUtil;

public class DctMatrixGeneratorTest {
	private static final float COMPARISON_DELTA = 0.000001f;

	private final Random random = new Random(12345);

	@Test(expected = IllegalArgumentException.class)
	public void testGenerateTestForZeroSize() {
		DctMatrixGenerator.generate(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGenerateTestForNegativeSize() {
		DctMatrixGenerator.generate(-1);
	}

	@Test
	public void testGeneratedMatrixIsSquare() {
		float[][] t;

		t = DctMatrixGenerator.generate(2);
		assertTrue(t.length == t[0].length);

		t = DctMatrixGenerator.generate(11);
		assertTrue(t.length == t[0].length);

		t = DctMatrixGenerator.generate(256);
		assertTrue(t.length == t[0].length);
	}

	@Test
	public void testInverseTransform() {
		final int numSamples = 100;
		for (int i = 0; i < numSamples; i++) {
			testInverseTransformSingle(64);
		}
	}

	private void testInverseTransformSingle(final int size) {
		final float[] x = TestUtil.generateRandomWindow(random, size);

		// final float[] x = TestUtil.generateRandomWindow(random, size);
		final float[][] dctMat = DctMatrixGenerator.generate(size);

		// Vorwärtstransformation y = DCT(x) (DCT-II)
		final float[] y = forwardDct(x, dctMat);

		// Rückwärtstransformation z = IDFT(y) (entspricht DCT-III)
		final float[] z = inverseDct(size, y);

		assertArrayEquals(x, z, COMPARISON_DELTA);
	}

	private float @NonNull [] forwardDct(final float @NonNull [] x,
			final float @NonNull [][] t) {
		final int size = x.length;
		final float[] y = new float[size];

		for (int k = 0; k < size; k++) {
			y[k] = 0f;
			for (int i = 0; i < size; i++) {
				y[k] += x[i] * t[i][k];
			}
		}

		return y;
	}

	private float @NonNull [] inverseDct(int size, final float @NonNull [] y) {
		// DCT-III mit Orthogonal-Norm entspricht IDCT der DCT-II mit
		// Orthogonal-Norm
		final float[] z = new float[size];
		for (int k = 0; k < size; k++) {
			z[k] = (float) (y[0] / sqrt(size));
			float sum = 0;
			for (int n = 1; n < size; n++) {
				sum += (float) (y[n] * cos(PI / size * n * (k + 0.5f)));
			}
			z[k] += sqrt(2f / size) * sum;
		}
		return z;
	}

	@Test
	public void testGeneratedMatrixIsOrthogonal() {
		float[][] t;

		t = DctMatrixGenerator.generate(2);
		assertTrue(isMatrixOrthogonal(t));

		t = DctMatrixGenerator.generate(11);
		assertTrue(isMatrixOrthogonal(t));

		t = DctMatrixGenerator.generate(256);
		assertTrue(isMatrixOrthogonal(t));
	}

	private static boolean isMatrixOrthogonal(final float @NonNull [][] m) {
		if (!isSquareMatrix(m)) {
			return false;
		}

		final float[][] mTransposed = transposeMatrix(m);
		return isIdentityMatrix(multiplyMatrices(m, mTransposed));
	}

	private static float @NonNull [][] transposeMatrix(
			final float @NonNull [][] m) {
		if (!isSquareMatrix(m)) {
			throw new IllegalArgumentException("m is not a square matrix");
		}

		final int size = m.length;
		final float[][] transposed = new float[size][size];
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				transposed[row][col] = m[col][row];
			}
		}
		return transposed;
	}

	private static float @NonNull [][] multiplyMatrices(
			final float @NonNull [][] a, final float @NonNull [][] b) {
		final int numRows = a.length;
		final int numCols = a[0].length;

		final float[][] result = new float[numRows][numCols];
		for (int row = 0; row < numRows; row++) {
			for (int col = 0; col < numCols; col++) {
				result[row][col] = 0;
				for (int i = 0; i < b.length; i++) {
					result[row][col] += a[row][i] * b[i][col];
				}
			}
		}
		return result;
	}

	private static boolean isIdentityMatrix(final float[][] m) {
		if (!isSquareMatrix(m)) {
			return false;
		}

		for (int row = 0; row < m.length; row++) {
			for (int col = 0; col < m.length; col++) {
				final boolean onDiagonal = (row == col);
				if (onDiagonal) {
					boolean isOne = Math.abs(1f - m[row][col]) <= COMPARISON_DELTA;
					if (!isOne) {
						return false;
					}
				} else {
					boolean isZero = Math.abs(m[row][col]) <= COMPARISON_DELTA;
					if (!isZero) {
						return false;
					}
				}
			}
		}

		return true;
	}

	private static boolean isSquareMatrix(final float @NonNull [][] m) {
		return m.length == m[0].length;
	}
}
