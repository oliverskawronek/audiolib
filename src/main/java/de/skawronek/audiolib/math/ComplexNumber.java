package de.skawronek.audiolib.math;

import org.eclipse.jdt.annotation.NonNull;

public final class ComplexNumber {
	private ComplexNumber() {
	}

	public static double magnitude(final double @NonNull [] z) {
		checkLength(z, "z");
		final double re = z[0];
		final double im = z[1];
		return Math.sqrt(re * re + im * im);
	}

	public static void conjugate(final double @NonNull [] z,
			final double @NonNull [] result) {
		checkLength(z, "z");
		checkLength(result, "result");
		result[0] = z[0];
		result[1] = -z[1];
	}

	public static void fromEuler(final double r, final double phi,
			final double @NonNull [] result) {
		checkLength(result, "result");
		result[0] = r * Math.cos(phi);
		result[1] = r * Math.sin(phi);
	}

	public static double toEulerR(final double[] z) {
		return magnitude(z);
	}

	public static double toEulerPhi(final double[] z) {
		checkLength(z, "z");
		final double re = z[0];
		final double im = z[1];
		return Math.atan2(im, re);
	}

	public static void add(final double @NonNull [] a,
			final double @NonNull [] b, final double @NonNull [] result) {
		checkLength(a, "a");
		checkLength(b, "b");
		checkLength(result, "result");
		// (a+bi)+(c+di) = (a+c)+(b+d)i
		result[0] = a[0] + b[0];
		result[1] = a[1] + b[1];
	}

	public static void substract(final double @NonNull [] a,
			final double @NonNull [] b, final double @NonNull [] result) {
		checkLength(a, "a");
		checkLength(b, "b");
		checkLength(result, "result");
		// (a+bi)-(c+di) = (a-c) + (b-d)i
		result[0] = a[0] - b[0];
		result[1] = a[1] - b[1];
	}

	public static void multiply(final double @NonNull [] a,
			final double @NonNull [] b, final double @NonNull [] result) {
		checkLength(a, "a");
		checkLength(b, "b");
		checkLength(result, "result");
		// (a+bi)*(c+di) = (ac-bd) + (ad+bc)i
		result[0] = a[0] * b[0] - a[1] * b[1];
		result[1] = a[0] * b[1] + a[1] * b[0];
	}

	public static void multiply(final double c, final double @NonNull [] z,
			final double @NonNull [] result) {
		checkLength(z, "z");
		checkLength(result, "result");
		// c*(a+bi) = ca + cbi
		result[0] = c * z[0];
		result[1] = c * z[1];
	}

	public static void divide(final double @NonNull [] a,
			final double @NonNull [] b, final double @NonNull [] result) {
		checkLength(a, "a");
		checkLength(b, "b");
		checkLength(result, "result");
		//@formatter:off
		/*
		 * a+bi   ac+bd    bc-ad 
		 * ---- = ------ + -----i 
		 * c+di   c²+d²    c²+d²
		 */
		//@formatter:on
		final double denom = b[0] * b[0] + b[1] * b[1];
		result[0] = (a[0] * b[0] + a[1] * b[1]) / denom;
		result[1] = (a[1] * b[0] - a[0] * b[1]) / denom;
	}

	public static void power(final double[] z, final double k,
			final double @NonNull [] result) {
		checkLength(z, "z");
		checkLength(result, "result");

		final double r = toEulerR(z);
		final double phi = toEulerPhi(z);

		// (a + bi)^k = (r * exp(i*phi))^k = r^k * exp(i*k*phi)
		final double r2 = Math.pow(r, k);
		result[0] = r2 * Math.cos(k * phi);
		result[1] = r2 * Math.sin(k * phi);
	}

	private static void checkLength(final double @NonNull [] z,
			final @NonNull String name) {
		if (z.length != 2) {
			throw new IllegalArgumentException("Length of " + name
					+ " must be 2, but was " + z.length);
		}
	}
}
