package de.skawronek.audiolib.math;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;

/**
 * <pre>
 * m: Order
 * x_pred[i] = -a[1]*x[i-1] -a[2]*x[i-2] - ... -a[M]*x[i-m]
 * </pre>
 *
 */
public final class LinearPredictor {
	// Autokorrelation
	private final float[] r;
	// Koeffzienten der Linear Prediction mit a[0] = 1
	private final float[] a;

	public LinearPredictor(final int order) {
		if (order < 1) {
			throw new IllegalArgumentException("order " + order + " < 1");
		}

		r = new float[order + 1];
		a = new float[order + 1];
	}

	public int getOrder() {
		return a.length - 1;
	}

	/**
	 * Berechnung der Koeffizienten mittels Levinson-Durbin-Rekursion unter
	 * Berücksichtigung der vorberechneten Autkorrelation r[]. Dabei muss r[]
	 * mindestens (m + 1) mit m: Order sein.
	 * 
	 * @param x
	 *            Eingangssignal
	 * @param r
	 *            Autokorrelation mit r[i] = r_xx(i)
	 */
	public void computeCoefficients(final float @NonNull [] x,
			final float @NonNull [] r) {
		final int m = getOrder();
		if (r.length < m + 1) {
			throw new IllegalArgumentException("r length " + r.length
					+ " < (Order + 1 = " + (m + 1) + ")");
		}

		System.arraycopy(r, 0, this.r, 0, m);

		Arrays.fill(a, 0f);
		a[0] = 1.0f;

		float e = r[0];

		// Levinson-Durbin Rekursion
		for (int k = 0; k < m; k++) {
			float lambda = 0f;
			for (int j = 0; j <= k; j++) {
				lambda -= a[j] * r[k + 1 - j];
			}
			lambda /= e;

			// Update a
			for (int j = 0; j <= (k + 1) / 2; j++) {
				float temp = a[k + 1 - j] + lambda * a[j];
				a[j] = a[j] + lambda * a[k + 1 - j];
				a[k + 1 - j] = temp;
			}

			// Update e
			e *= 1f - lambda * lambda;
		}
	}

	/**
	 * Berechnung der Koeffizienten mittels Levinson-Durbin-Rekursion
	 */
	public void computeCoefficients(final float @NonNull [] x) {
		computeAutocorrelation(x);
		computeCoefficients(x, r);
	}

	private void computeAutocorrelation(final float @NonNull [] x) {
		final int m = getOrder();
		final int n = x.length - 1;
		Arrays.fill(r, 0f);
		for (int i = 0; i <= m; i++) {
			for (int j = 0; j <= n - i; j++) {
				r[i] += x[j] * x[j + i];
			}
		}
	}

	/**
	 * Prediction an der Stelle i, order <= i <= x.length - 1.
	 * 
	 * @param x
	 * @param i
	 * @return Prediction für die Stelle i
	 */
	public float predict(final float @NonNull [] x, final int i) {
		final int order = getOrder(); // inkl.
		// inkl.
		if (i < order || i > x.length - 1) {
			throw new IndexOutOfBoundsException("i " + i + " is out of range "
					+ order + ".." + (x.length - 1));
		}

		float prediction = 0f;
		for (int j = 1; j <= order; j++) {
			prediction -= a[j] * x[i - j];
		}

		return prediction;
	}

	/**
	 * Gibt die Kopie a[] der Koeffizienten zurück. Das Array hat folgende
	 * Eigenschaften:
	 * <ul>
	 * <li>a[] hat die Länge m+1 mit m: Order</li>
	 * <li>a[0] = 1</li>
	 * <li>-a[j] wird mit dem Term x[i-j] multipliziert</li>
	 * </ul>
	 * 
	 * @return Kopie der Koeffizienten
	 */
	public float @NonNull [] getCoefficients() {
		final float[] copy = Arrays.copyOf(a, a.length);
		return copy;
	}
}
