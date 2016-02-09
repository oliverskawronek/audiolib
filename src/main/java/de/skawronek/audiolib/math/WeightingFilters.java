package de.skawronek.audiolib.math;

import static java.lang.Math.sqrt;

import org.eclipse.jdt.annotation.NonNull;

//Quelle: http://en.wikipedia.org/wiki/A-weighting,
//http://www.webcitation.org/5xZXWNqVh
public final class WeightingFilters {
	@FunctionalInterface
	public interface IWeightingFilter {
		public double evaluate(final double f);
	}

	private WeightingFilters() {
	}

	private static IWeightingFilter aWeighting = null;

	// To be used for sounds at low level
	public static @NonNull IWeightingFilter getAWeighting() {
		if (aWeighting == null) {
			final double k = 12200 * 12200;
			aWeighting = f -> {
				final double fSquared = f * f;
				return (k * fSquared * fSquared)
						/ ((fSquared + 20.6 * 20.6) * (fSquared + k) * (sqrt(fSquared + 107.7 * 107.7) * sqrt(fSquared + 737.9 * 737.9)));
			};
		}
		return aWeighting;
	}

	private static IWeightingFilter bWeighting = null;

	public static @NonNull IWeightingFilter getBWeighting() {
		if (bWeighting == null) {
			final double k = 12200 * 12200;
			bWeighting = f -> {
				final double fSquared = f * f;
				return (k * fSquared * f)
						/ ((fSquared + 20.6 * 20.6) * (fSquared + k) * sqrt((fSquared + 158.5 * 158.5)));
			};
		}
		return bWeighting;
	}

	private static IWeightingFilter cWeighting = null;

	// To be used for sounds at medium level
	public static @NonNull IWeightingFilter getCWeighting() {
		if (cWeighting == null) {
			final double k = 12200 * 12200;
			cWeighting = f -> {
				final double fSquared = f * f;
				return (k * fSquared)
						/ ((fSquared + 20.6 * 20.6) * (fSquared + k));
			};
		}
		return cWeighting;
	}

	private static IWeightingFilter zWeighting;

	// No weighting
	public static @NonNull IWeightingFilter getZWeighting() {
		if (zWeighting == null) {
			zWeighting = (f -> 1);
		}
		return zWeighting;
	}
}
