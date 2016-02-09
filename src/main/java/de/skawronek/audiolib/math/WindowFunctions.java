package de.skawronek.audiolib.math;

import static java.lang.Math.*;

public final class WindowFunctions {
	@FunctionalInterface
	public interface IWindowFunction {
		public double evaluate(final double t);
	}

	private WindowFunctions() {
	}

	public static IWindowFunction getRectangularWindow() {
		return t -> 1;
	}

	public static IWindowFunction getBartlettWindow() {
		return t -> (t <= 0 ? 1 + 2 * t : 1 - 2 * t);
	}

	public static IWindowFunction getHammingWindow() {
		return t -> (0.54 - 0.46 * cos(2 * PI * (t + 0.5)));
	}

	public static IWindowFunction getHannWindow() {
		return t -> (0.5 * (1 - cos(2 * PI * (t + 0.5))));
	}

	public static IWindowFunction getBlackmanWindow() {
		return getBlackmanWindow(0.16);
	}

	public static IWindowFunction getBlackmanWindow(final double alpha) {
		final double a0 = (1 - alpha) / 2;
		final double a1 = 0.5;
		final double a2 = alpha / 2;
		return t -> (a0 - a1 * cos(2 * PI * (t + 0.5)) + a2
				* cos(4 * PI * (t + 0.5)));
	}

}
