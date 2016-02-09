package de.skawronek.audiolib.math;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.math.WeightingFilters.IWeightingFilter;

public final class RootMeanSquare {
	private RootMeanSquare() {
	}

	public static float compute(final float @NonNull [] x) {
		if (x.length == 0) {
			return 0;
		}

		float sum = 0;
		for (int i = 0; i < x.length; i++) {
			sum += (x[i] * x[i]);
		}
		final float mean = sum / (float) x.length;
		final float rms = (float) Math.sqrt(mean);
		return rms;
	}

	public static float computeWithWeightingFilter(
			final float @NonNull [] magSpectrum, final int size,
			final double sampleRate, final boolean realValued,
			IWeightingFilter filter) {
		checkSpectrumArguments(magSpectrum, size, sampleRate, realValued);

		//@formatter:off
		/*
		 * Parsevals Theorem:
		 *    Integrate_(-Inf)^(+Inf){x²[t]*dt}
		 *  = (1/2Pi)*Integrate_(-Inf)^(+Inf){|X(jw)|²*jw}
		 */
		//@formatter:on

		final int n = (realValued ? magSpectrum.length / 2 : magSpectrum.length);
		double energy = 0;
		for (int k = 0; k < n; k++) {
			final double frequency = FastFourierTransform.binToFrequency(k,
					size, sampleRate);
			final double weighted = filter.evaluate(frequency) * magSpectrum[k];
			energy += weighted * weighted;
		}
		energy /= (n * n);
		if (realValued) {
			energy /= 2;
		}
		final double intensity = Math.sqrt(energy);
		return (float) intensity;
	}

	private static void checkSpectrumArguments(
			final float @NonNull [] magSpectrum, final int size,
			final double sampleRate, final boolean realValued) {
		if (sampleRate < 0) {
			throw new IllegalArgumentException("sampleRate " + sampleRate
					+ " < 0");
		} else if (size < 0) {
			throw new IllegalArgumentException("size " + size + " < 0");
		} else if (!realValued && magSpectrum.length < size) {
			throw new IllegalArgumentException("spectrum length "
					+ magSpectrum.length + " < " + size);
		} else if (magSpectrum.length < size / 2) {
			final int minLength = (realValued ? size / 2 : size);
			throw new IllegalArgumentException("spectrum length "
					+ magSpectrum.length + " < " + minLength);
		}
	}
}
