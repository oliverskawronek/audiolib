package de.skawronek.audiolib.math;

import org.junit.Test;

import static de.skawronek.audiolib.math.ButterworthFilterFactory.*;
import static org.junit.Assert.*;

public final class ButterwortFilterFactoryTest {
	private static final double COMPARISON_DELTA = 0.00001;

	@Test(expected = IllegalArgumentException.class)
	public void testCalcNormalizedFrequencyChecksZeroSampleRate() {
		calcNormalizedFrequency(22050, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCalcNormalizedFrequencyChecksNegativeSampleRate() {
		calcNormalizedFrequency(22050, -1);
	}

	@Test
	public void testCalcNormalizedFrequency() {
		assertEquals(0.0, calcNormalizedFrequency(0, 44100), COMPARISON_DELTA);
		assertEquals(0.5, calcNormalizedFrequency(44100 / 4, 44100),
				COMPARISON_DELTA);
		// Nyquist
		assertEquals(1.0, calcNormalizedFrequency(44100 / 2, 44100),
				COMPARISON_DELTA);
	}

	//@formatter:off
	/*
	 * Alle Tests wurden mit dem Octave Signal Package erstellt.
	 * Low-Pass: [A, B] = butter(order, cutoff);
	 * High-Pass: [A, B] = butter(order, cutoff, "high");
	 * Band-Pass: [A, B] = butter(order, [low, high]);
	 * Band-Reject: [A, B] = butter(order, [low, high], "stop");
	 */
	//@formatter:on

	@Test(expected = IllegalArgumentException.class)
	public void testCreateLowPassChecksOrder() {
		createLowPass(0, 0.2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateLowPassChecksNegativeCutoff() {
		createLowPass(1, -0.5);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateLowPassChecksCutoffGreaterOne() {
		createLowPass(1, 1.5);
	}

	@Test
	public void testCreateLowPass() {
		// order 3, cutoff 0.2
		//@formatter:off
		final double[] bExpected = new double[] {
				0.018099, 0.054297,	0.054297, 0.018099 };
		final double[] aExpected = new double[] {
				1.00000, -1.76004, 1.18289,	-0.27806 };
		//@formatter:off
		final IIRFilter filter = createLowPass(3, 0.2);
		final double[] bActual = filter.getFeedforwardCoefficients(false);
		final double[] aActual = filter.getFeedbackCoefficients(false);
		assertArrayEquals(bExpected, bActual, COMPARISON_DELTA);
		assertArrayEquals(aExpected, aActual, COMPARISON_DELTA);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateHighPassChecksOrder() {
		createHighPass(0, 0.2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateHighPassChecksNegativeCutoff() {
		createHighPass(1, -0.5);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateHighPassChecksCutoffGreaterOne() {
		createHighPass(1, 1.5);
	}
	
	@Test
	public void testCreateHighPass() {
		// order 3, cutoff 0.2
		//@formatter:off
		final double[] bExpected = new double[] {
				0.52762, -1.58287, 1.58287, -0.52762 };
		final double[] aExpected = new double[] {
				1.00000, -1.76004, 1.18289, -0.27806 };
		//@formatter:off
		final IIRFilter filter = createHighPass(3, 0.2);
		final double[] bActual = filter.getFeedforwardCoefficients(false);
		final double[] aActual = filter.getFeedbackCoefficients(false);
		assertArrayEquals(bExpected, bActual, COMPARISON_DELTA);
		assertArrayEquals(aExpected, aActual, COMPARISON_DELTA);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateBandPassChecksOrder() {
		createBandPass(0, 0.2, 0.7);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateBandPassChecksNegativeCutoffLow() {
		createBandPass(1, -0.5, 0.7);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateBandPassChecksNegativeCutoffHigh() {
		createBandPass(1, 0.2, -0.7);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateBandPassChecksCutoffLowGreaterOne() {
		createBandPass(1, 1.5, 0.7);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateBandPassChecksCutoffHighGreaterOne() {
		createBandPass(1, 0.2, 1.5);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateBandPassChecksLowCutoffIsLowerThanHighCutoff() {
		createBandPass(1, 0.7, 0.2);
	}
	
	@Test
	public void testCreateBandPass() {
		// order 3, cutoffLow 0.2, cutoffHigh 0.7
		//@formatter:off
		final double[] bExpected = new double[] {
				0.16667, 0.00000, -0.50000, 0.00000, 0.50000, 0.00000, -0.16667};
		final double[] aExpected = new double[] {
				1.0000e+000, -6.6370e-001, 1.6314e-001, -1.6192e-001,
				3.6596e-001, -7.3744e-002, 1.6374e-017 };
		//@formatter:off
		final IIRFilter filter = createBandPass(3, 0.2, 0.7);
		final double[] bActual = filter.getFeedforwardCoefficients(false);
		final double[] aActual = filter.getFeedbackCoefficients(false);
		assertArrayEquals(bExpected, bActual, COMPARISON_DELTA);
		assertArrayEquals(aExpected, aActual, COMPARISON_DELTA);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateBandRejectChecksOrder() {
		createBandReject(0, 0.2, 0.7);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateBandRejectChecksNegativeCutoffLow() {
		createBandReject(1, -0.5, 0.7);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateBandRejectChecksNegativeCutoffHigh() {
		createBandReject(1, 0.2, -0.7);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateBandRejectChecksCutoffLowGreaterOne() {
		createBandReject(1, 1.5, 0.7);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateBandRejectChecksCutoffHighGreaterOne() {
		createBandReject(1, 0.2, 1.5);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateBandRejectChecksLowCutoffIsLowerThanHighCutoff() {
		createBandReject(1, 0.7, 0.2);
	}
	
	@Test
	public void testCreateBandReject() {
		// order 3, cutoffLow 0.2, cutoffHigh 0.7
		//@formatter:off
		final double[] bExpected = new double[] {
				0.16667, -0.22123, 0.59789, -0.45690, 0.59789, -0.22123, 0.16667 };
		final double[] aExpected = new double[] {
				1.0000e+000, -6.6370e-001, 1.6314e-001, -1.6192e-001,
				3.6596e-001, -7.3744e-002, 1.6374e-017 };
		//@formatter:off
		final IIRFilter filter = createBandReject(3, 0.2, 0.7);
		final double[] bActual = filter.getFeedforwardCoefficients(false);
		final double[] aActual = filter.getFeedbackCoefficients(false);
		assertArrayEquals(bExpected, bActual, COMPARISON_DELTA);
		assertArrayEquals(aExpected, aActual, COMPARISON_DELTA);
	}
}
