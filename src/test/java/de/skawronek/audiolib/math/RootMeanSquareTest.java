package de.skawronek.audiolib.math;

import java.util.Random;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;

import de.skawronek.audiolib.TestUtil;
import de.skawronek.audiolib.math.WeightingFilters.IWeightingFilter;
import static org.junit.Assert.*;

public final class RootMeanSquareTest {
	private static final float COMPARISON_DELTA = 0.01f;

	private final Random random = new Random(12345);

	@Test
	public void testComputeWithEmpty() {
		final float[] empty = new float[0];
		assertEquals(0, RootMeanSquare.compute(empty), COMPARISON_DELTA);
	}

	@Test
	public void testCompute() {
		assertEquals(1, RootMeanSquare.compute(new float[] { 1 }),
				COMPARISON_DELTA);
		assertEquals(1, RootMeanSquare.compute(new float[] { -1 }),
				COMPARISON_DELTA);
		assertEquals(6.137318f, RootMeanSquare.compute(new float[] { 2f, -4f,
				5f, 6f, 8f, -9f }), COMPARISON_DELTA);
	}

	@Test
	public void testComputationInFrequencyDomainIsEqualToComputationInTimeDomain() {
		final int numSamples = 100;
		for (int i = 0; i < numSamples; i++) {
			final float[] x = TestUtil.generateRandomWindow(random, 2048);
			singleTestComputationInFrequencyDomainIsEqualToComputationInTimeDomain(x);
		}
	}
	
	private void singleTestComputationInFrequencyDomainIsEqualToComputationInTimeDomain(
			final float @NonNull [] x) {
		final float rmsTime = RootMeanSquare.compute(x);

		final int size = x.length;
		final FastFourierTransform fft = new FastFourierTransform(size);
		final float[] spectrum = new float[size];
		fft.forward(x);
		fft.fillMagnitudeSpectrum(spectrum, true);
		IWeightingFilter noWeighting = WeightingFilters.getZWeighting();
		final float rmsFrequency = RootMeanSquare.computeWithWeightingFilter(
				spectrum, size, 44100, true, noWeighting);

		assertEquals(rmsTime, rmsFrequency, COMPARISON_DELTA);
	}
}
