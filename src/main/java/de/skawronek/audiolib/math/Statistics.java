package de.skawronek.audiolib.math;

import org.eclipse.jdt.annotation.NonNull;

public final class Statistics {
	private static final float NOT_SET = Float.NaN;

	private float[] x = null;
	private float sum;
	private float min;
	private float max;
	private float avg;
	private float geometricMean;
	private float median;
	private float variance;
	private float stdDeviation;

	public Statistics() {
		reset();
	}

	public void setInput(final float @NonNull [] x) {
		if (x.length == 0) {
			throw new IllegalArgumentException("x cannot be empty");
		}
		reset();
		this.x = x;
	}

	private void reset() {
		sum = NOT_SET;
		min = NOT_SET;
		max = NOT_SET;
		avg = NOT_SET;
		geometricMean = NOT_SET;
		median = NOT_SET;
		variance = NOT_SET;
		stdDeviation = NOT_SET;
	}

	private void calcSumMinMaxAvg() {
		ensureInputIsSet();

		sum = x[0];
		min = x[0];
		max = x[0];
		avg = x[0];
		for (int i = 1; i < x.length; i++) {
			sum += x[i];

			if (x[i] < min) {
				min = x[i];
			}

			if (x[i] > max) {
				max = x[i];
			}

			avg += x[i];
		}
		avg /= x.length;
	}

	public float getSum() {
		if (!isSet(sum)) {
			calcSumMinMaxAvg();
		}
		return sum;
	}

	public float getMininum() {
		if (!isSet(min)) {
			calcSumMinMaxAvg();
		}
		return min;
	}

	public float getMaximum() {
		if (!isSet(max)) {
			calcSumMinMaxAvg();
		}
		return max;
	}

	public float getAverage() {
		if (!isSet(avg)) {
			calcSumMinMaxAvg();
		}
		return avg;
	}

	public float getGeometricMean() {
		if (isSet(geometricMean)) {
			return geometricMean;
		}

		final int n = x.length;
		geometricMean = 1;
		for (int i = 0; i < n; i++) {
			geometricMean *= x[i];
		}
		geometricMean = (float) Math.pow(geometricMean, 1d / n);

		return geometricMean;
	}

	public float getMedian() {
		if (!isSet(median)) {
			calcMedian();
		}
		return median;
	}

	public float getVariance() {
		if (!isSet(variance)) {
			calcVariance();
		}
		return variance;
	}

	public float getStandardDeviation() {
		if (!isSet(stdDeviation)) {
			final float var = getVariance();
			stdDeviation = (float) Math.sqrt(var);
		}
		return stdDeviation;
	}

	private static boolean isSet(final float x) {
		return !Float.isNaN(x);
	}

	// Algorithmus von Torben Mogensen.
	// Vorteil: Verändert nicht das Eingabe-Array
	// Laufzeit: "Number of Iterations O(log(n))"
	// Literatur: http://ndevilla.free.fr/median/median.pdf
	// Quelle: http://ndevilla.free.fr/median/median/src/torben.c
	private void calcMedian() {
		ensureInputIsSet();

		float min = getMininum();
		float max = getMaximum();
		int lessCount;
		int equalCount;
		int greaterCount;
		float maxLtGuess;
		float minGtGuess;

		while (true) {
			final float guess = (min + max) / 2f;

			lessCount = 0;
			equalCount = 0;
			greaterCount = 0;

			maxLtGuess = min;
			minGtGuess = max;
			for (int i = 0; i < x.length; i++) {
				if (x[i] < guess) {
					lessCount++;
					if (x[i] > maxLtGuess) {
						maxLtGuess = x[i];
					}
				} else if (x[i] > guess) {
					greaterCount++;
					if (x[i] < minGtGuess) {
						minGtGuess = x[i];
					}
				} else {
					equalCount++;
				}
			}

			if (lessCount <= (x.length + 1) / 2
					&& greaterCount <= (x.length + 1) / 2) {
				// Ende
				if (lessCount >= (x.length + 1) / 2)
					median = maxLtGuess;
				else if (lessCount + equalCount >= (x.length + 1) / 2)
					median = guess;
				else
					median = minGtGuess;
				return;
			} else if (lessCount > greaterCount) {
				max = maxLtGuess;
			} else {
				min = minGtGuess;
			}
		}
	}

	private void calcVariance() {
		// korrigierte Stichprobenvarianz
		final float mean = getAverage();
		float sum = 0;
		for (int i = 0; i < x.length; i++) {
			final float diff = x[i] - mean;
			sum += diff * diff;
		}
		variance = (1f / (x.length - 1)) * sum;
	}

	private void ensureInputIsSet() {
		if (x == null) {
			throw new IllegalStateException(
					"Input was not set. Call setInput before");
		}
	}
}
