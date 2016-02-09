package de.skawronek.audiolib;

import java.util.Random;

import org.eclipse.jdt.annotation.NonNull;

public final class TestUtil {
	private TestUtil() {
	}

	// Root Mean Squared Error
	public static float computeRMSE(final float[] a, final float[] b) {
		if (a.length != b.length) {
			throw new IllegalArgumentException("Size of a " + a.length
					+ " != size of b " + b.length);
		}

		final int size = a.length;
		if (size == 0) {
			return 0.0f;
		}

		float rmse = 0.0f;
		for (int i = 0; i < size; i++) {
			final float error = a[i] - b[i];
			rmse += error * error;
		}
		rmse = (float) Math.sqrt(rmse / size);
		return rmse;
	}

	public static float[] generateRandomWindow(final @NonNull Random random,
			final int size) {
		return generateRandomWindow(random, size, -1, 1);
	}

	public static float[] generateRandomWindow(final @NonNull Random random,
			final int size, final float min, final float max) {
		if (min > max) {
			throw new IllegalArgumentException("min " + min + " > max " + max);
		}

		final float w[] = new float[size];
		for (int i = 0; i < size; i++) {
			w[i] = (max - min) * random.nextFloat() + min;
			assert w[i] >= -1 && w[i] <= 1;
		}
		return w;
	}

	public static float[] generateRandomSound(final @NonNull Random random,
			final int size, final int numSines, final float minAmplitude,
			final float maxAmplitude) {
		final float[] frequencies = generateRandomWindow(random, numSines,
				size / 200f, size / 50f);
		final float[] amplitudes = generateRandomWindow(random, numSines,
				minAmplitude, maxAmplitude);

		final float samples[] = new float[size];
		for (int i = 0; i < size; i++) {
			samples[i] = 0f;
			for (int k = 0; k < numSines; k++) {
				samples[i] += (float) (amplitudes[k] * Math.sin(2 * Math.PI
						* frequencies[k] / size * i));
			}
		}

		return samples;
	}

	public static int random(final Random random, final int minIncl, final int maxExcl) {
		if (minIncl > maxExcl) {
			throw new IllegalArgumentException("minIncl " + minIncl
					+ " > maxExcl " + maxExcl);
		}

		return minIncl + random.nextInt(maxExcl - minIncl);
	}

	public static float random(final Random random, final float minIncl,
			final float maxExcl) {
		if (minIncl > maxExcl) {
			throw new IllegalArgumentException("minIncl " + minIncl
					+ " > maxExcl " + maxExcl);
		}

		return minIncl + (maxExcl - minIncl) * random.nextFloat();
	}
}
