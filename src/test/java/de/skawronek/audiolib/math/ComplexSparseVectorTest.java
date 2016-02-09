package de.skawronek.audiolib.math;

import static org.junit.Assert.*;

import java.util.Random;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;

import de.skawronek.audiolib.math.ComplexSparseVector;
import de.skawronek.audiolib.TestUtil;

public final class ComplexSparseVectorTest {
	private static final float COMPARISON_DELTA = 0.0000001f;

	private final Random random = new Random(12345);

	@Test(expected = IllegalArgumentException.class)
	public void testFromArrayChecksVectorSize() {
		final float[] vecRe = new float[4];
		final float[] vecIm = new float[5];
		ComplexSparseVector.fromArray(vecRe, vecIm, 123f);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFromArrayChecksNegativeThreshold() {
		final float[] vecRe = new float[5];
		final float[] vecIm = new float[5];
		ComplexSparseVector.fromArray(vecRe, vecIm, -1f);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFromArrayChecksMinGapSize() {
		final float[] vecRe = new float[5];
		final float[] vecIm = new float[5];
		ComplexSparseVector.fromArray(vecRe, vecIm, 1f, 0);
	}

	@Test
	public void testGetThreshold() {
		final float[] vecRe = new float[5];
		final float[] vecIm = new float[5];
		final ComplexSparseVector nonEmpty = ComplexSparseVector.fromArray(
				vecRe, vecIm, 123f);
		assertEquals(123f, nonEmpty.getThreshold(), COMPARISON_DELTA);
	}

	@Test
	public void testSize() {
		// Teste mit leerem Vektor
		final float[] emptyRe = new float[0];
		final float[] emptyIm = new float[0];
		final ComplexSparseVector emptyVec = ComplexSparseVector.fromArray(
				emptyRe, emptyIm, 123f);
		assertEquals(0, emptyVec.size());

		// Test mit nicht leerem Vektor
		final float[] vecRe = new float[5];
		final float[] vecIm = new float[5];
		final ComplexSparseVector nonEmpty = ComplexSparseVector.fromArray(
				vecRe, vecIm, 123f);
		assertEquals(5, nonEmpty.size());
	}

	@Test
	public void testIsEmpty() {
		// Teste mit leerem Vektor
		final float[] emptyRe = new float[0];
		final float[] emptyIm = new float[0];
		final ComplexSparseVector emptyVec = ComplexSparseVector.fromArray(
				emptyRe, emptyIm, 123f);
		assertTrue(emptyVec.isEmpty());

		// Test mit nicht leerem Vektor
		final float[] vecRe = new float[5];
		final float[] vecIm = new float[5];
		final ComplexSparseVector nonEmpty = ComplexSparseVector.fromArray(
				vecRe, vecIm, 123f);
		assertFalse(nonEmpty.isEmpty());
	}

	@Test
	public void testToArray() {
		final int numSample = 100;
		for (int i = 0; i < numSample; i++) {
			testToArraySingle();
		}
	}

	private void testToArraySingle() {
		final float[] expectedRe = TestUtil.generateRandomWindow(random, 2048,
				-5, 5);
		final float[] expectedIm = TestUtil.generateRandomWindow(random, 2048,
				-5, 5);
		final float threshold = random.nextFloat() * 4;
		assert threshold >= 0f && threshold < 4f;

		final ComplexSparseVector sparse = ComplexSparseVector.fromArray(
				expectedRe, expectedIm, threshold);
		threshold(expectedRe, expectedIm, threshold);

		final float[][] a = sparse.toArray();
		final float[] actualRe = a[0];
		final float[] actualIm = a[1];

		final float reRMSE = TestUtil.computeRMSE(expectedRe, actualRe);
		final float imRMSE = TestUtil.computeRMSE(expectedIm, actualIm);
		assertEquals(0f, reRMSE, COMPARISON_DELTA);
		assertEquals(0f, imRMSE, COMPARISON_DELTA);
	}

	@Test
	public void testToArrayWithExplicitGap() {
		final float[] expectedRe = new float[] { 2, 3, 4, 5, 0, 0, 0, 6, 7, 8 };
		final float[] expectedIm = new float[] { 2, 3, 4, 5, 0, 0, 0, 6, 7, 8 };
		final float threshold = 1f;
		final ComplexSparseVector sparse = ComplexSparseVector.fromArray(
				expectedRe, expectedIm, threshold, 1);
		threshold(expectedRe, expectedIm, threshold);

		final float[][] a = sparse.toArray();
		final float[] actualRe = a[0];
		final float[] actualIm = a[1];

		final float reRMSE = TestUtil.computeRMSE(expectedRe, actualRe);
		final float imRMSE = TestUtil.computeRMSE(expectedIm, actualIm);
		assertEquals(0f, reRMSE, COMPARISON_DELTA);
		assertEquals(0f, imRMSE, COMPARISON_DELTA);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDotProductChecksSize() {
		// Vektor A der Größe 4
		final float[] vecARe = new float[4];
		final float[] vecAIm = new float[4];
		final ComplexSparseVector vecA = ComplexSparseVector.fromArray(vecARe,
				vecAIm, 123f);

		// Vektor B der Größe 5
		final float[] vecBRe = new float[5];
		final float[] vecBIm = new float[5];

		vecA.dotProduct(vecBRe, vecBIm);
	}

	@Test
	public void testDotProduct() {
		final int numSamples = 100;
		for (int i = 0; i < numSamples; i++) {
			testDotProductSingle();
		}
	}

	private void testDotProductSingle() {
		final int size = 2048;

		// Generiere komplexen Vektor A
		final float[] vecARe = TestUtil.generateRandomWindow(random, size, -5f,
				5f);
		final float[] vecAIm = TestUtil.generateRandomWindow(random, size, -5f,
				5f);
		final float threshold = random.nextFloat() * 4;
		assert threshold >= 0f && threshold < 4f;
		final ComplexSparseVector vecA = ComplexSparseVector.fromArray(vecARe,
				vecAIm, threshold);
		threshold(vecARe, vecAIm, threshold);

		// Generiere komplexen Vektor B
		final float[] vecBRe = TestUtil.generateRandomWindow(random, size, -5f,
				5f);
		final float[] vecBIm = TestUtil.generateRandomWindow(random, size, -5f,
				5f);

		// Berechne Punktprodukt zwischen vecA und vecB manuell
		float expectedRe = 0f;
		float expectedIm = 0f;
		for (int i = 0; i < size; i++) {
			//@formatter:off
			// Komplexe Multiplikation:
			// (a + bi) * (c + di) = (ac - bd) + (ad + bc)i
			//@formatter:on

			expectedRe += (vecARe[i] * vecBRe[i] - vecAIm[i] * vecBIm[i]);
			expectedIm += (vecARe[i] * vecBIm[i] + vecAIm[i] * vecBRe[i]);
		}

		// Berechne Punktprodukt mit eingebauter Methode
		final float[] dotProduct = vecA.dotProduct(vecBRe, vecBIm);
		final float actualRe = dotProduct[0];
		final float actualIm = dotProduct[1];

		assertEquals(expectedRe, actualRe, COMPARISON_DELTA);
		assertEquals(expectedIm, actualIm, COMPARISON_DELTA);
	}

	private static void threshold(final float @NonNull [] vecRe,
			final float @NonNull [] vecIm, final float threshold) {
		assert vecIm.length == vecRe.length;
		assert threshold >= 0f;
		final int size = vecRe.length;
		for (int i = 0; i < size; i++) {
			final float mag = (float) Math.sqrt(vecRe[i] * vecRe[i] + vecIm[i]
					* vecIm[i]);
			if (mag < threshold) {
				vecRe[i] = 0f;
				vecIm[i] = 0f;
			}
		}
	}
}
