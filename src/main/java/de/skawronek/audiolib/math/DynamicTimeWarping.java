package de.skawronek.audiolib.math;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javafx.util.Pair;

import org.eclipse.jdt.annotation.NonNull;

public final class DynamicTimeWarping implements
		Iterable<Pair<Integer, Integer>> {
	// Accumulated Distance Matrix: accuDistMatrix[n][m] = DTW(X(0:n), Y(0:m))
	// mit X(0:n) = x[0], x[1], ... , x[n] und Y(0:m) = y[0], y[1], ... , y[m]
	private double[][] accuDistMat = null;

	/**
	 * @param <F>
	 *            Feature Type
	 */
	@FunctionalInterface
	public static interface IDistanceFunction<F> {
		/**
		 * Die Distanz-Funktion, hier abgekürzt mit d, muss folgende
		 * Eigenschaften erfüllen:
		 * <ul>
		 * <li>Nicht-Negativität: d(x, y) >= 0</li>
		 * <li>Definitheit: d(x, y) = 0 <==> x = y</li>
		 * <li>Symmetrie: d(x, y) = d(x, y)</li>
		 * <li>Dreiecksungleichung: d(x, z) <= d(x, y) + d(y, z)
		 * </ul>
		 * für alle Features x, y, z
		 * 
		 * @param featureX
		 * @param featureY
		 * @return Distanz zwischen featureX und featureY
		 */
		double getDistance(final @NonNull F featureX, final @NonNull F featureY);
	}

	/**
	 * Iteriert einen Pfad Punkt für Punkt von hinten nach vorne, also in
	 * umgekehrter Reihenfolge. Punkte auf dem Pfad werden durch geordnete Paare
	 * (x, y) repräsentiert.
	 *
	 */
	public static class ReverseWarpingPath implements
			Iterator<Pair<Integer, Integer>> {
		private final double[][] accuDistMat;
		private final int numRows;
		private final int numCols;
		private int lastXIndex; // 0,...,numRows - 1
		private int lastYIndex; // 0,...,numCols - 1

		private ReverseWarpingPath(final double @NonNull [][] accuDistMat) {
			assert accuDistMat.length > 0;

			this.accuDistMat = accuDistMat;
			this.numRows = accuDistMat.length;
			this.numCols = accuDistMat[0].length;
			// Beginne beim letzten Punkt
			this.lastXIndex = numRows - 1;
			this.lastYIndex = numCols - 1;
		}

		@Override
		public boolean hasNext() {
			return lastXIndex >= 0 || lastYIndex >= 0;
		}

		@Override
		public Pair<Integer, Integer> next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			
			final Pair<Integer, Integer> curr = new Pair<>(lastXIndex,
					lastYIndex);

			final boolean firstPoint = lastXIndex == 0 && lastYIndex == 0;
			if (firstPoint) {
				// Beende Iteration
				lastXIndex = -1;
				lastYIndex = -1;

				return curr;
			}

			// Gehe einen Schritt weiter
			if (lastXIndex == 0) {
				lastXIndex = 0;
				lastYIndex = lastYIndex - 1;
			} else if (lastYIndex == 0) {
				lastXIndex = lastXIndex - 1;
				lastYIndex = 0;
			} else {
				final double a = accuDistMat[lastXIndex - 1][lastYIndex - 1];// Nord-Westen
				final double b = accuDistMat[lastXIndex - 1][lastYIndex];// Westen
				final double c = accuDistMat[lastXIndex][lastYIndex - 1];// Norden
				//@formatter:off
				/*
				 * Gesucht ist argmin{a, b, c}.
				 * Es gibt 8 Fälle:
				 * 1) a <= b && a <= c && b <= c
				 * 2) a <= b && a <= c && b >= c
				 * 3) a <= b && a >= c && b <= c
				 * 4) a <= b && a >= c && b >= c
				 * 5) a >= b && a <= c && b <= c
				 * 6) a >= b && a <= c && b >= c
				 * 7) a >= b && a >= c && b <= c
				 * 8) a >= b && a >= c && b >= c
				 * Aufsteigend sortiert:
				 * 1) a <= b <= c && a <= c
				 * 2) a <= c <= b && a <= b
				 * 3) a <= b <= c && c <= a --> a = b = c
				 * 4) c <= a <= b && c <= b
				 * 5) b <= a <= c && b <= c
				 * 6) b <= a <= c && c <= b --> a = b = c
				 * 7) b <= c <= a && b <= a
				 * 8) c <= b <= a && c <= a
				 * Für die uneindeutigen Fälle 3), 6) sollen die Argumente
				 * lexikografisch sortiert werden.
				 */
				//@formatter:on

				if (b <= c) {
					// Fall 1), 3), 5), 6), 7)
					if (a <= b) {
						// Fall 1), 3), 6): Gehe nord-west
						lastXIndex = lastXIndex - 1;
						lastYIndex = lastYIndex - 1;
					} else {
						// Fall 5), 7): Gehe west
						lastXIndex = lastXIndex - 1;
					}
				} else {
					// Fall 2), 4), 8)
					if (a <= c) {
						// Fall 2) : Gehe nord-west
						lastXIndex = lastXIndex - 1;
						lastYIndex = lastYIndex - 1;
					} else {
						// Fall 4), 8): Gehe nord
						lastYIndex = lastYIndex - 1;
					}
				}
			}

			return curr;
		}
	}

	public <F> void compute(final F @NonNull [] seriesX,
			final F @NonNull [] seriesY, final @NonNull IDistanceFunction<F> df) {
		compute(asList(seriesX), asList(seriesY), df);
	}

	@NonNull
	private static <F> List<F> asList(final F @NonNull [] fs) {
		return new AbstractList<F>() {
			@NonNull
			public F get(final int i) {
				return fs[i];
			}

			public int size() {
				return fs.length;
			}
		};
	}

	public <F> void compute(final @NonNull List<F> seriesX,
			final @NonNull List<F> seriesY,
			final @NonNull IDistanceFunction<F> df) {
		if (seriesX.isEmpty() || seriesY.isEmpty()) {
			throw new IllegalArgumentException("seriesX or seriesY is empty");
		}

		final int numRows = seriesX.size();
		final int numCols = seriesY.size();
		if (accuDistMat == null || (accuDistMat.length != numRows)
				|| (accuDistMat[0].length != numCols)) {
			accuDistMat = new double[numRows][numCols];
		}

		computeAccumulatedCostMatrix(seriesX, seriesY, df);
	}

	@Override
	@NonNull
	public Iterator<Pair<Integer, Integer>> iterator() {
		return getWarpingPath();
	}

	@NonNull
	public Iterator<Pair<Integer, Integer>> getWarpingPath() {
		return new ReverseWarpingPath(accuDistMat);
	}

	public double getTotallDistance() {
		final int numRows = accuDistMat.length;
		final int numCols = accuDistMat[0].length;
		return accuDistMat[numRows - 1][numCols - 1];
	}

	private <F> void computeAccumulatedCostMatrix(
			@NonNull final List<F> seriesX, @NonNull final List<F> seriesY,
			final @NonNull IDistanceFunction<F> df) {
		computeFirstRow(seriesX, seriesY, df);
		computeFirstColumn(seriesX, seriesY, df);
		computeRest(seriesX, seriesY, df);
	}

	private <F> void computeFirstRow(@NonNull final List<F> seriesX,
			@NonNull final List<F> seriesY,
			final @NonNull IDistanceFunction<F> df) {
		final int numCols = seriesY.size();

		final F featureX = seriesX.get(0);
		accuDistMat[0][0] = df.getDistance(featureX, seriesY.get(0));

		for (int col = 1; col < numCols; col++) {
			final F featureY = seriesY.get(col);
			accuDistMat[0][col] = accuDistMat[0][col - 1]
					+ df.getDistance(featureX, featureY);
		}
	}

	// computeFirstRow muss zuvor aufgerufen werden!
	private <F> void computeFirstColumn(@NonNull final List<F> seriesX,
			@NonNull final List<F> seriesY,
			final @NonNull IDistanceFunction<F> df) {
		final int numRows = seriesX.size();

		final F featureY = seriesY.get(0);
		for (int row = 1; row < numRows; row++) {
			final F featureX = seriesX.get(row);
			accuDistMat[row][0] = accuDistMat[row - 1][0]
					+ df.getDistance(featureX, featureY);
		}
	}

	// computeFirstRow und computeFirstColumn müssen zuvor aufgerufen werden!
	private <F> void computeRest(final @NonNull List<F> seriesX,
			final @NonNull List<F> seriesY,
			final @NonNull IDistanceFunction<F> df) {
		final int numRows = seriesX.size();
		final int numCols = seriesY.size();

		for (int row = 1; row < numRows; row++) {
			final F featureX = seriesX.get(row);
			for (int col = 1; col < numCols; col++) {
				final double topLeft = accuDistMat[row - 1][col - 1];
				final double top = accuDistMat[row - 1][col];
				final double left = accuDistMat[row][col - 1];
				// = min{topLeft, top, left}
				final double min = Math.min(topLeft, Math.min(top, left));
				final F featureY = seriesY.get(col);
				accuDistMat[row][col] = min
						+ df.getDistance(featureX, featureY);
			}
		}
	}
}
