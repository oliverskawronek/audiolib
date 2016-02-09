package de.skawronek.audiolib.math;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;

import org.eclipse.jdt.annotation.NonNull;

public final class DctMatrixGenerator {
	private DctMatrixGenerator() {
	}

	/**
	 * Generiert die sizexsize große Transformations-Matrix T für die
	 * Diskrete-Kosinus-Transformation (DCT). Die Multiplikation mit T
	 * entspricht der DCT II-Transformation. T ist orthornomalisiert, d. h. T
	 * mit der Transponierten multipliziert ergibt die Einheitsmatrix.
	 * 
	 * @param size
	 * @return
	 */
	public static float @NonNull [][] generate(final int size) {
		if (size < 1) {
			throw new IllegalArgumentException("size " + size + "< 1");
		}

		// Gesamt Normalisierungs-Faktor
		final float normalizationFactor = (float) sqrt(1.0 / (size / 2.0));

		final float[][] dctMatrix = new float[size][size];
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				//@formatter:off
				dctMatrix[row][col] = (float) cos(col*(row + 0.5)*PI/size);
				//@formatter:on
				// Orthogonal Normalisierung
				dctMatrix[row][col] *= normalizationFactor;
			}
			// Zusätzlich erste Spalte extra normalisieren
			dctMatrix[row][0] *= (float) (sqrt(2.0) / 2.0);
		}

		return dctMatrix;
	}
}
