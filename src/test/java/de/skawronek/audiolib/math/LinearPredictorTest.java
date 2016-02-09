package de.skawronek.audiolib.math;

import java.util.Random;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;

import de.skawronek.audiolib.TestUtil;
import static org.junit.Assert.*;

public final class LinearPredictorTest {
	private static final float COMPARISON_DELTA = 0.0001f;

	private final Random random = new Random(12345);
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorChecksOrder() {
		final int minOrder = 1;
		new LinearPredictor(minOrder - 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testComputeCoefficientsChecksRLength() {
		final int size = 2048;
		final float[] x = TestUtil.generateRandomWindow(random, size);
		final int order = 5;

		final LinearPredictor predictor = new LinearPredictor(order);
		final int minLength = order + 1;
		final float[] r = new float[minLength - 1];
		predictor.computeCoefficients(x, r);
	}

	@Test
	public void testComputeCoefficientsComputesTheSameWithAndWithoutAutocorrelation() {
		final int size = 2048;
		final float[] x = TestUtil.generateRandomWindow(random, size);
		final int order = 5;

		// Berechne Autokorrelation manuell
		FastAutocorrelation acf = new FastAutocorrelation(size);
		acf.compute(x);
		final float[] r = new float[size];
		acf.copyCoefficients(r);

		final LinearPredictor predictor1 = new LinearPredictor(order);
		predictor1.computeCoefficients(x, r);
		final float[] expectedA = predictor1.getCoefficients();

		final LinearPredictor predictor2 = new LinearPredictor(order);
		predictor2.computeCoefficients(x);
		final float[] actualA = predictor2.getCoefficients();

		assertArrayEquals(expectedA, actualA, COMPARISON_DELTA);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testPredictChecksMinimumI() {
		final int size = 10;
		final float[] x = TestUtil.generateRandomWindow(random, size);
		final int order = 5;

		final LinearPredictor predictor = new LinearPredictor(order);
		predictor.computeCoefficients(x);
		final int minI = order;
		final int i = minI - 1;
		predictor.predict(x, i);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testPredictChecksMaximumI() {
		final int size = 10;
		final float[] x = TestUtil.generateRandomWindow(random, size);
		final int order = 5;

		final LinearPredictor predictor = new LinearPredictor(order);
		predictor.computeCoefficients(x);
		final int maxI = size - 1;
		final int i = maxI + 1;
		predictor.predict(x, i);
	}

	@Test
	public void testPredict() {
		final int size = 2048;
		final float[] x = TestUtil.generateRandomWindow(random, size);
		final int order = 5;

		final LinearPredictor predictor = new LinearPredictor(order);
		predictor.computeCoefficients(x);
		final float @NonNull [] a = predictor.getCoefficients();

		for (int i = order; i < size; i++) {
			// Manuelle Berechner der Prediction
			float expectedPrediction = 0f;
			for (int j = 1; j <= order; j++) {
				expectedPrediction -= a[j] * x[i - j];
			}

			final float actualPrediction = predictor.predict(x, i);
			assertEquals(expectedPrediction, actualPrediction, COMPARISON_DELTA);
		}
	}

	@Test
	public void testComputeCoefficients() {
		final int order = 3;
		// Quadratische Kurve
		final float[] x = new float[] { 0, 1, 2, 4, 8, 16, 32 };

		//@formatter:off
		/* r = [1365, 682, 340, 168]
		 * 
		 *     |1365  682  340|      |a1|              |682|
		 * M = | 682 1365  682|, a = |a2|, Löse M.a = -|340|
		 *     | 340  682 1365|      |a3|              |168|
		 *     
		 * Lösung: a = [1, -174762/349525, 0, 512/349525]
		 */
		//@formatter:on
		final float[] expectedA = new float[] { 1f, -174762f / 349525f, 0f,
				512f / 349525f };
		final LinearPredictor predictor = new LinearPredictor(order);
		predictor.computeCoefficients(x);
		final float[] actualA = predictor.getCoefficients();
		assertArrayEquals(expectedA, actualA, COMPARISON_DELTA);
	}
}
