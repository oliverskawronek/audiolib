package de.skawronek.audiolib.math;

import org.eclipse.jdt.annotation.NonNull;

// Quelle: https://code.google.com/p/jstk/source/browse/trunk/jstk/src/de/fau/cs/jstk/sampled/filters/Butterworth.java
public final class ButterworthFilterFactory {
	private ButterworthFilterFactory() {
	}

	public static double calcNormalizedFrequency(final double frequency,
			final double sampleRate) {
		if (sampleRate <= 0) {
			throw new IllegalArgumentException("sampleRate " + sampleRate
					+ " <= 0");
		}
		return 2 * frequency / sampleRate;
	}

	public static @NonNull IIRFilter createLowPass(final int order,
			final double cutoff) {
		return createLowPassOrHighPass(order, cutoff, true);
	}

	public static @NonNull IIRFilter createHighPass(final int order,
			final double cutoff) {
		return createLowPassOrHighPass(order, cutoff, false);
	}

	public static @NonNull IIRFilter createBandPass(final int order,
			final double cutoff1, final double cutoff2) {
		return createBandPassOrBandReject(order, cutoff1, cutoff2, true);
	}

	public static @NonNull IIRFilter createBandReject(final int order,
			final double cutoff1, final double cutoff2) {
		return createBandPassOrBandReject(order, cutoff1, cutoff2, false);
	}

	private static @NonNull IIRFilter createLowPassOrHighPass(final int order,
			final double cutoff, final boolean lowPass) {
		checkOrder(order);
		checkCutoff(cutoff, "cutoff");

		final double scale = computeScaleForLowPassAndHighPass(order, cutoff,
				lowPass);
		final double[] b = computeBForLowPassAndHighPass(order, lowPass);
		for (int i = 0; i < b.length; i++) {
			b[i] *= scale;
		}
		final double[] a = computeAForLowPassAndHighPass(order, cutoff);
		return new IIRFilter(b, a);
	}

	private static @NonNull IIRFilter createBandPassOrBandReject(
			final int order, final double cutoffLow, final double cutoffHigh,
			final boolean pass) {
		checkOrder(order);
		checkCutoff(cutoffLow, "cutoffLow");
		checkCutoff(cutoffHigh, "cutoffHigh");
		if (cutoffLow >= cutoffHigh) {
			throw new IllegalArgumentException("cutoffLow " + cutoffLow
					+ " > cutoffHigh " + cutoffHigh);
		}

		final double scale = computeScaleForBandPassAndBandReject(order,
				cutoffLow, cutoffHigh, pass);
		final double[] b = computeBForBandPassAndBandReject(order, cutoffLow,
				cutoffHigh, pass);
		for (int i = 0; i < b.length; ++i) {
			b[i] *= scale;
		}
		final double[] a = computeAForBandPassAndBandReject(order, cutoffLow,
				cutoffHigh, false);
		return new IIRFilter(b, a);
	}

	private static void checkOrder(final int order) {
		if (order < 1) {
			throw new IllegalArgumentException("order " + order + " < 1");
		}
	}

	private static void checkCutoff(final double cutoff,
			final @NonNull String name) {
		if (cutoff < 0 || cutoff > 1) {
			throw new IllegalArgumentException(name + " " + cutoff
					+ " is out of range 0..1");
		}
	}

	private static double computeScaleForLowPassAndHighPass(final int order,
			final double f, final boolean lowPass) {
		final double omega = Math.PI * f;
		double fomega = Math.sin(omega);
		final double parg0 = Math.PI / (double) (2 * order);

		double sf = 1;
		for (int k = 0; k < order / 2; k++) {
			sf *= 1 + fomega * Math.sin((double) (2 * k + 1) * parg0);
		}

		fomega = (lowPass ? Math.sin(omega / 2) : Math.cos(omega / 2));

		if (order % 2 != 0) {
			sf *= fomega
					+ (lowPass ? Math.cos(omega / 2) : Math.sin(omega / 2));
		}
		sf = Math.pow(fomega, order) / sf;

		return sf;
	}

	private static double computeScaleForBandPassAndBandReject(final int order,
			final double f1, final double f2, final boolean pass) {
		double parg; // pole angle
		double sparg; // sine of pole angle
		double cparg; // cosine of pole angle
		double a, b, c; // workspace variables

		double tt = Math.tan(Math.PI * (f2 - f1) / 2);
		if (pass) {
			tt = 1 / tt;
		}

		double sfr = 1;
		double sfi = 0;

		for (int k = 0; k < order; k++) {
			parg = Math.PI * (double) (2 * k + 1) / (double) (2 * order);
			sparg = tt + Math.sin(parg);
			cparg = Math.cos(parg);
			a = (sfr + sfi) * (sparg - cparg);
			b = sfr * sparg;
			c = -sfi * cparg;
			sfr = b - c;
			sfi = a - b - c;
		}

		return 1 / sfr;
	}

	private static double @NonNull [] computeBForLowPassAndHighPass(
			final int order, final boolean lowPass) {
		final double[] ccof = new double[order + 1];

		ccof[0] = 1;
		ccof[1] = order;

		for (int i = 2; i < order / 2 + 1; i++) {
			ccof[i] = (order - i + 1) * ccof[i - 1] / i;
			ccof[order - i] = ccof[i];
		}

		ccof[order - 1] = order;
		ccof[order] = 1;

		if (!lowPass) {
			for (int i = 1; i < order + 1; i += 2) {
				ccof[i] = -ccof[i];
			}
		}

		return ccof;

	}

	private static double @NonNull [] computeBForBandPassAndBandReject(
			final int order, final double f1, final double f2,
			final boolean pass) {
		final double[] ccof = new double[2 * order + 1];
		if (pass) {
			final double[] tcof = computeBForLowPassAndHighPass(order, false);

			for (int i = 0; i < order; i++) {
				ccof[2 * i] = tcof[i];
				ccof[2 * i + 1] = 0;
			}

			ccof[2 * order] = tcof[order];
		} else {
			final double alpha = -2 * Math.cos(Math.PI * (f2 + f1) / 2)
					/ Math.cos(Math.PI * (f2 - f1) / 2);

			ccof[0] = 1;
			ccof[1] = alpha;
			ccof[2] = 1;

			for (int i = 1; i < order; i++) {
				ccof[2 * i + 2] += ccof[2 * i];
				for (int j = 2 * i; j > 1; j--) {
					ccof[j + 1] += alpha * ccof[j] + ccof[j - 1];
				}

				ccof[2] += alpha * ccof[1] + 1;
				ccof[1] += alpha;
			}
		}

		return ccof;
	}

	private static double @NonNull [] computeAForLowPassAndHighPass(
			final int order, final double f) {
		double parg; // pole angle
		double sparg; // sine of the pole angle
		double cparg; // cosine of the pole angle
		double a; // workspace variable
		final double[] rcof = new double[2 * order]; // binomial coefficients

		final double theta = Math.PI * f;
		final double st = Math.sin(theta);
		final double ct = Math.cos(theta);

		for (int k = 0; k < order; k++) {
			parg = Math.PI * (double) (2 * k + 1) / (double) (2 * order);
			sparg = Math.sin(parg);
			cparg = Math.cos(parg);
			a = 1 + st * sparg;
			rcof[2 * k] = -ct / a;
			rcof[2 * k + 1] = -st * cparg / a;
		}

		// compute the binomial
		final double[] temp = binomialMult(rcof);

		// we only need the n+1 coefficients
		final double[] dcof = new double[order + 1];
		dcof[0] = 1;
		dcof[1] = temp[0];
		dcof[2] = temp[2];
		for (int k = 3; k < order + 1; k++) {
			dcof[k] = temp[2 * k - 2];
		}

		return dcof;
	}

	private static double @NonNull [] computeAForBandPassAndBandReject(
			final int order, final double f1, final double f2,
			final boolean pass) {
		double parg; // pole angle
		double sparg; // sine of pole angle
		double cparg; // cosine of pole angle
		double a; // workspace variables

		final double cp = Math.cos(Math.PI * (f2 + f1) / 2);
		final double theta = Math.PI * (f2 - f1) / 2;
		final double st = Math.sin(theta);
		final double ct = Math.cos(theta);
		final double s2t = 2 * st * ct; // sine of 2*theta
		final double c2t = 2 * ct * ct - 1; // cosine of 2*theta

		final double[] rcof = new double[2 * order]; // z^-2 coefficients
		final double[] tcof = new double[2 * order]; // z^-1 coefficients

		for (int k = 0; k < order; k++) {
			parg = Math.PI * (double) (2 * k + 1) / (double) (2 * order);
			sparg = Math.sin(parg);
			cparg = Math.cos(parg);
			a = 1 + s2t * sparg;
			rcof[2 * k] = c2t / a;
			rcof[2 * k + 1] = (pass ? 1 : -1) * s2t * cparg / a;
			tcof[2 * k] = -2.0 * cp * (ct + st * sparg) / a;
			tcof[2 * k + 1] = (pass ? -2 : 2) * cp * st * cparg / a;
		}

		// compute trinomial
		final double[] temp = trinomialMult(tcof, rcof);

		// we only need the 2n+1 coefficients
		final double[] dcof = new double[2 * order + 1];
		dcof[0] = 1;
		dcof[1] = temp[0];
		dcof[2] = temp[2];
		for (int k = 3; k < 2 * order + 1; k++) {
			dcof[k] = temp[2 * k - 2];
		}

		return dcof;
	}

	/**
	 * Multiply a series of binomials and returns the coefficients of the
	 * resulting polynomial. The multiplication has the following form:<b/>
	 * 
	 * (x+p[0])*(x+p[1])*...*(x+p[n-1]) <b/>
	 * 
	 * The p[i] coefficients are assumed to be complex and are passed to the
	 * function as an array of doubles of length 2n.<b/>
	 * 
	 * The resulting polynomial has the following form:<b/>
	 * 
	 * x^n + a[0]*x^n-1 + a[1]*x^n-2 + ... +a[n-2]*x + a[n-1] <b/>
	 * 
	 * The a[i] coefficients can in general be complex but should in most cases
	 * turn out to be real. The a[i] coefficients are returned by the function
	 * as an array of doubles of length 2n.
	 * 
	 * @param p
	 *            array of doubles where p[2i], p[2i+1] (i=0...n-1) is assumed
	 *            to be the real, imaginary part of the i-th binomial.
	 * @return coefficients a: x^n + a[0]*x^n-1 + a[1]*x^n-2 + ... +a[n-2]*x +
	 *         a[n-1]
	 */
	private static double @NonNull [] binomialMult(final double @NonNull [] p) {
		final int n = p.length / 2;
		final double[] a = new double[2 * n];

		for (int i = 0; i < n; i++) {
			for (int j = i; j > 0; j--) {
				a[2 * j] += p[2 * i] * a[2 * (j - 1)] - p[2 * i + 1]
						* a[2 * (j - 1) + 1];
				a[2 * j + 1] += p[2 * i] * a[2 * (j - 1) + 1] + p[2 * i + 1]
						* a[2 * (j - 1)];
			}

			a[0] += p[2 * i];
			a[1] += p[2 * i + 1];
		}

		return a;
	}

	/**
	 * Multiply a series of trinomials and returns the coefficients of the
	 * resulting polynomial. The multiplication has the following form:<b/>
	 * 
	 * (x^2 + b[0]x + c[0])*(x^2 + b[1]x + c[1])*...*(x^2 + b[n-1]x + c[n-1])
	 * <b/>
	 * 
	 * The b[i], c[i] coefficients are assumed to be complex and are passed to
	 * the function as an array of doubles of length 2n.<b/>
	 * 
	 * The resulting polynomial has the following form:<b/>
	 * 
	 * x^2n + a[0]*x^2n-1 + a[1]*x^2n-2 + ... +a[2n-2]*x + a[2n-1] <b/>
	 * 
	 * The a[i] coefficients can in general be complex but should in most cases
	 * turn out to be real. The a[i] coefficients are returned by the function
	 * as an array of doubles of length 2n.
	 * 
	 * @param b
	 *            array of doubles where b[2i], b[2i+1] (i=0...n-1) is assumed
	 *            to be the real, imaginary part of the i-th binomial.
	 * @param c
	 * @return coefficients a: x^2n + a[0]*x^2n-1 + a[1]*x^2n-2 + ... +a[2n-2]*x
	 *         + a[2n-1]
	 */
	private static double @NonNull [] trinomialMult(final double @NonNull [] b,
			final double @NonNull [] c) {
		final int n = b.length / 2;
		final double[] a = new double[4 * n];

		a[0] = b[0];
		a[1] = b[1];
		a[2] = c[0];
		a[3] = c[1];

		for (int i = 1; i < n; i++) {
			a[2 * (2 * i + 1)] += c[2 * i] * a[2 * (2 * i - 1)] - c[2 * i + 1]
					* a[2 * (2 * i - 1) + 1];
			a[2 * (2 * i + 1) + 1] += c[2 * i] * a[2 * (2 * i - 1) + 1]
					+ c[2 * i + 1] * a[2 * (2 * i - 1)];

			for (int j = 2 * i; j > 1; --j) {
				a[2 * j] += b[2 * i] * a[2 * (j - 1)] - b[2 * i + 1]
						* a[2 * (j - 1) + 1] + c[2 * i] * a[2 * (j - 2)]
						- c[2 * i + 1] * a[2 * (j - 2) + 1];
				a[2 * j + 1] += b[2 * i] * a[2 * (j - 1) + 1] + b[2 * i + 1]
						* a[2 * (j - 1)] + c[2 * i] * a[2 * (j - 2) + 1]
						+ c[2 * i + 1] * a[2 * (j - 2)];
			}

			a[2] += b[2 * i] * a[0] - b[2 * i + 1] * a[1] + c[2 * i];
			a[3] += b[2 * i] * a[1] + b[2 * i + 1] * a[0] + c[2 * i + 1];
			a[0] += b[2 * i];
			a[1] += b[2 * i + 1];
		}

		return a;
	}
}
