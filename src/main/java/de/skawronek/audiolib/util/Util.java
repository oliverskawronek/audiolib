package de.skawronek.audiolib.util;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNull;

public final class Util {
	private Util() {
	}

	public static double clamp(final double value, final double min,
			final double max) {
		if (min > max) {
			throw new IllegalArgumentException("min > max");
		}

		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		} else {
			return value;
		}
	}

	public static float clamp(final float value, final float min,
			final float max) {
		if (min > max) {
			throw new IllegalArgumentException("min > max");
		}

		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		} else {
			return value;
		}
	}

	public static int clamp(final int value, final int min, final int max) {
		if (min > max) {
			throw new IllegalArgumentException("min > max");
		}

		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		} else {
			return value;
		}
	}

	public static long clamp(final long value, final long min, final long max) {
		if (min > max) {
			throw new IllegalArgumentException("min > max");
		}

		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		} else {
			return value;
		}
	}

	public static boolean isPowerOfTwo(final int i) {
		return (i > 0) && (i & (i - 1)) == 0;
	}

	public static boolean isPowerOfTwo(final long i) {
		return (i > 0) && (i & (i - 1)) == 0;
	}

	public static int getExponentOfTwo(int i) {
		if (!isPowerOfTwo(i)) {
			throw new IllegalArgumentException("i " + i
					+ " is not a power of two");
		}

		// Exponent kann auch für negative Zahlen berechnet werden,
		// z. B. 1024 = -2^10.
		i = Math.abs(i);
		int exponent = 0;
		do {
			i = i >> 1;
			exponent++;
		} while (i != 0 && exponent < Integer.SIZE - 1);
		return exponent - 1;
	}

	/**
	 * Gibt die zu i nächste 2er-Potenz i* zurück. Es gilt i* >= i, d. h. ist i
	 * bereits eine 2er-Potenz, wird i zurück gegeben. Da 2^0 = 1 die kleinste
	 * ganzzahlige 2er-Potenz ist, wird i*=1 bei i <= 0 zurückgegeben.
	 * 
	 * @param i
	 *            Referenzwert
	 * @return zu i nächste 2er-Potenz
	 */
	public static int getNextPowerOfTwo(final int i) {
		if (isPowerOfTwo(i)) {
			return i;
		}

		if (i > 0) {
			final int exponent = 32 - Integer.numberOfLeadingZeros(i);
			return 1 << exponent; // = 2^exponent
		} else {
			return 1; // = 2^0
		}
	}

	/**
	 * Gibt die zu i nächste 2er-Potenz i* zurück. Es gilt i* >= i, d. h. ist i
	 * bereits eine 2er-Potenz, wird i zurück gegeben. Da 2^0 = 1 die kleinste
	 * ganzzahlige 2er-Potenz ist, wird i*=1 bei i <= 0 zurückgegeben.
	 * 
	 * @param i
	 *            Referenzwert
	 * @return zu i nächste 2er-Potenz
	 */
	public static long getNextPowerOfTwo(final long i) {
		if (isPowerOfTwo(i)) {
			return i;
		}

		if (i > 0) {
			final int exponent = 64 - Long.numberOfLeadingZeros(i);
			return 1 << exponent; // = 2^exponent
		} else {
			return 1; // = 2^0
		}
	}

	private static final double LOG2_DENOMINATOR = Math.log(2);

	public static double log2(final double x) {
		return Math.log(x) / LOG2_DENOMINATOR;
	}

	public static double log(final double b, final double x) {
		return Math.log(x) / Math.log(b);
	}

	@NonNull
	public static Duration samplesToDuration(final long numSamples,
			final double sampleRate) {
		checkSampleRate(sampleRate);
		final double SEC_TO_MILLIS = 1000.0;
		return Duration.ofMillis(Math.round(SEC_TO_MILLIS
				* (numSamples / sampleRate)));
	}

	public static long durationToSamples(final @NonNull Duration duration,
			final double sampleRate) {
		checkSampleRate(sampleRate);
		final double MILLIS_TO_SEC = 1.0 / 1000.0;
		final double secs = duration.toMillis() * MILLIS_TO_SEC;
		return Math.round(secs * sampleRate);
	}

	private static void checkSampleRate(final double sampleRate) {
		if (sampleRate <= 0) {
			throw new IllegalArgumentException("sampleRate " + sampleRate
					+ " <= 0");
		}
	}

	public static float interpolateLinear(final float a, final float b,
			final float t) {
		return a + t * (b - a);
	}

	public static double interpolateLinear(final double a, final double b,
			final double t) {
		return a + t * (b - a);
	}
}
