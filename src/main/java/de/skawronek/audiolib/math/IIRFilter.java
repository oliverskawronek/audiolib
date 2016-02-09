package de.skawronek.audiolib.math;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;

/**
 * <pre>
 * y[n] = (1/a[0])*(b[0]*x[n] + b[1]*x[n-1] + ... + b[nb]*x[n-nb] - a[1]*y[n-1] - ... - a[na]*y[n-na])
 * </pre>
 *
 */
public class IIRFilter {
	// Koeffizienten des Eingangssignals
	private final double[] a;
	// Koeffizienten des Ausgangssignals
	private final double[] b;

	private final float[] x;
	private final float[] y;

	public IIRFilter(final double @NonNull [] b, final double @NonNull [] a) {
		if (a.length == 0) {
			throw new IllegalArgumentException("a is empty");
		}

		this.b = b.clone();
		this.a = a.clone();

		this.x = new float[b.length];
		this.y = new float[a.length];
	}

	/**
	 * Annahme:
	 * 
	 * <pre>
	 * x[i] = 0 für i < 0
	 * y[i] = 0 für i < 0
	 * </pre>
	 * 
	 * @param x
	 *            Eingangssignal
	 * @param y
	 *            Gefiltertes Ausgangssignal
	 */
	public void filter(final float @NonNull [] in, final float @NonNull [] out) {
		if (in.length != out.length) {
			throw new IllegalArgumentException("in length " + in.length
					+ " != out length " + out.length);
		}
		final int length = in.length;

		// Stellt x[i] = 0, y[i] = 0 für i < 0 sicher
		reset();

		for (int i = 0; i < length; i++) {
			append(x, in[i]);

			out[i] = 0f;
			for (int k = 0; k < b.length; k++) {
				out[i] += (float) (b[k] * x[x.length - k - 1]);
			}
			for (int k = 1; k < a.length; k++) {
				out[i] -= (float) (a[k] * y[y.length - k]);
			}
			out[i] /= a[0];

			append(y, out[i]);
		}
	}

	private void reset() {
		Arrays.fill(x, 0f);
		Arrays.fill(y, 0f);
	}

	private static void append(final float @NonNull [] arr, final float e) {
		assert arr.length > 0;
		shiftLeft(arr);
		arr[arr.length - 1] = e;
	}

	private static void shiftLeft(final float @NonNull [] arr) {
		for (int i = 0; i < arr.length - 1; i++) {
			arr[i] = arr[i + 1];
		}
	}

	/**
	 * Gibt die Koeffizienten a[k], k >= 0 der rückführenden Terme als Kopie
	 * zurück. Dabei ist
	 * <ul>
	 * <li>a[0] der Normalisierungskoeffizient, durch die restlichen
	 * Koeffzienten a[k], k > 0 und b[k] k >= 0 geteilt werden und</li>
	 * <li>a[k] der Koeffizient für den Term y[n-k] mit k >= 1.</li>
	 * </ul>
	 * 
	 * Wenn normalize auf true gesetzt wird, werden zuvor a[k], k >= 0 durch
	 * a[0] geteilt.
	 * 
	 * @param normalize
	 *            wenn true, dann werden alle Koeffizienten durch a[0] geteilt
	 * @return Koeffizienten der rückführenden Terme a[k], k >= 0
	 */
	public double[] getFeedbackCoefficients(final boolean normalize) {
		final double[] aCopy = Arrays.copyOf(this.a, this.a.length);
		if (normalize) {
			normalize(aCopy);
		}
		return aCopy;
	}

	/**
	 * Gibt die Koeffizienten b[k], k >= 0 der Eingangsterme als Kopie zurück.
	 * Dabei ist b[k] der Koeffizient für den Eingangsterm x[n - k].
	 * 
	 * Wenn normalize auf true gesetzt wird, werden zuvor b[k], k >= 0 durch
	 * a[0] geteilt.
	 * 
	 * @param normalize
	 *            wenn true, dann werden alle Koeffizienten durch a[0] geteilt
	 * @return Koeffizienten der Eingangsterme b[k], k >= 0
	 */
	public double[] getFeedforwardCoefficients(final boolean normalize) {
		final double[] bCopy = Arrays.copyOf(this.b, this.b.length);
		if (normalize) {
			normalize(bCopy);
		}
		return bCopy;
	}

	private void normalize(final double[] coeff) {
		for (int k = 0; k < coeff.length; k++) {
			coeff[k] /= a[0];
		}
	}

	final NumberFormat NUMBER_FORMAT = DecimalFormat.getInstance();

	@Override
	public @NonNull String toString() {
		final StringBuilder sb = new StringBuilder("IIR[");
		for (int k = 0; k < b.length; k++) {
			sb.append("b" + k + "=");
			sb.append(NUMBER_FORMAT.format(b[k]));
			sb.append(", ");
		}
		for (int k = 0; k < a.length; k++) {
			sb.append("a" + k + "=");
			sb.append(NUMBER_FORMAT.format(a[k]));

			final boolean last = (k == a.length - 1);
			if (!last) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	public double amplitudeResponse(final double omega) {
		return ComplexNumber.magnitude(frequencyResponse(omega));
	}

	public double phaseResponse(final double omega) {
		return ComplexNumber.toEulerPhi(frequencyResponse(omega));
	}

	private double[] frequencyResponse(final double omega) {
		// z = exp(-j*omega);
		final double[] z = new double[2];
		z[0] = Math.cos(omega);
		z[1] = Math.sin(omega);

		final double[] h = transferFunction(z);
		return h;
	}

	private double @NonNull[] transferFunction(final double @NonNull [] z) {
		final double[] tmp = new double[2];
		final double[] zp = new double[2];

		final double[] numerator = new double[2];
		numerator[0] = 0;
		numerator[1] = 0;
		for (int k = 0; k < b.length; k++) {
			ComplexNumber.power(z, -k, zp);
			ComplexNumber.multiply(b[k], zp, tmp);
			ComplexNumber.add(numerator, tmp, numerator);
		}

		final double[] denominator = new double[2];
		denominator[0] = 0;
		denominator[1] = 0;
		for (int k = 0; k < a.length; k++) {
			ComplexNumber.power(z, -k, zp);
			ComplexNumber.multiply(a[k], zp, tmp);
			ComplexNumber.add(denominator, tmp, denominator);
		}

		final double[] resesult = new double[2];
		ComplexNumber.divide(numerator, denominator, resesult);
		return resesult;
	}
}
