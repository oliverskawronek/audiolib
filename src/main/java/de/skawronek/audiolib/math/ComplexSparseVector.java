package de.skawronek.audiolib.math;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;

public final class ComplexSparseVector {
	private final Range first;
	private final int size;
	private final float threshold;

	private static final class Range {
		final int start;
		final int end;
		final float[] elementsRe;
		final float[] elementsIm;
		Range next;

		private Range(int start, int end, float[] elementsRe, float[] elementsIm) {
			this.start = start;
			this.end = end;
			assert (end - start) >= 1;
			this.elementsRe = elementsRe;
			this.elementsIm = elementsIm;
		}

		void setNext(final Range next) {
			this.next = next;
		}

		boolean isGap() {
			return elementsRe == null;
		}

		int size() {
			return end - start;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append(start + ".." + (end - 1) + ": [");
			if (isGap()) {
				for (int n = size(), i = 0; i < n; i++) {
					sb.append("0");
					final boolean last = (i == n - 1);
					if (!last) {
						sb.append(",");
					}
				}
			} else {
				for (int n = size(), i = 0; i < n; i++) {
					final float re = elementsRe[i];
					final float im = elementsIm[i];
					sb.append(formatComplexNumber(re, im));
					final boolean last = (i == n - 1);
					if (!last) {
						sb.append(",");
					}
				}
			}
			sb.append("]");
			return sb.toString();
		}

		@NonNull
		static Range createGap(final int start, final int end) {
			final Range gap = new Range(start, end, null, null);
			return gap;
		}

		@NonNull
		static Range createRange(final int start, final int end,
				final float @NonNull [] elemsRe, float @NonNull [] elemsIm) {
			return new Range(start, end, elemsRe, elemsIm);
		}
	}

	private ComplexSparseVector(final Range first, final int size,
			final float threshold) {
		if (threshold < 0) {
			throw new IllegalArgumentException("threshold " + threshold
					+ " < 0");
		}

		this.first = first;
		this.size = size;
		this.threshold = threshold;
	}

	public float getThreshold() {
		return threshold;
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Gibt den Vektor als 2dim. Array in voller Größe zurück. Werte kleiner als
	 * der Threshold (bezogen auf den komplexen Betrag) werden zu 0, d. h. Real-
	 * und Imaginärteil werden zu Null gesetzt. Das Array ist wie folgt
	 * aufgebaut:
	 * 
	 * <pre>
	 * float[][] a = vec.toArray();
	 * float[] real = a[0];
	 * float[] imaginary = a[1];
	 * assert real.length == vec.size() &amp;&amp; imaginary.length == vec.size();
	 * </pre>
	 * 
	 * @return Vektor als 2dim. Array in voller Größe
	 */
	public float @NonNull[][] toArray() {
		final float[][] a = new float[2][size];
		final float[] aRe = a[0];
		final float[] aIm = a[1];
		Range curr = first;
		while (curr != null) {
			if (curr.isGap()) {
				Arrays.fill(aRe, curr.start, curr.end, 0f);
				Arrays.fill(aIm, curr.start, curr.end, 0f);
			} else {
				System.arraycopy(curr.elementsRe, 0, aRe, curr.start, curr.end
						- curr.start);
				System.arraycopy(curr.elementsIm, 0, aIm, curr.start, curr.end
						- curr.start);
			}
			curr = curr.next;
		}
		return a;
	}

	/**
	 * Berechnet das Punktprodukt zum übergebenen Vektor vec. Das Ergebnis ist
	 * komplexwertig und wird als Array zurück gegeben.
	 * 
	 * <pre>
	 * dotProduct = vecA.dotProduct(vecB);
	 * real = dotProduct[0];
	 * imaginary = dotProduct[1];
	 * </pre>
	 * 
	 * @param vecRe
	 *            Realanteil des Vektors
	 * @param vecIm
	 *            Imaginäranteil des Vektors
	 * @return Punktprodukt als Array mit Real- (Index 0) und Imaginäranteil
	 *         (Index 1).
	 */
	public float @NonNull[] dotProduct(final float @NonNull [] vecRe,
			final float @NonNull [] vecIm) {
		if (vecRe.length != size || vecIm.length != size) {
			throw new IllegalArgumentException("vec length != this length "
					+ size);
		}

		Range curr = first;
		float sumRe = 0;
		float sumIm = 0;
		while (curr != null) {
			if (!curr.isGap()) {
				for (int j = curr.start; j < curr.end; j++) {
					final float aRe = curr.elementsRe[j - curr.start];
					final float aIm = curr.elementsIm[j - curr.start];

					final float bRe = vecRe[j];
					final float bIm = vecIm[j];

					//@formatter:off
					// Komplexe Multiplikation:
					// (a + bi) * (c + di) = (ac - bd) + (ad + bc)i
					//@formatter:on
					final float resRe = aRe * bRe - aIm * bIm;
					final float resIm = aRe * bIm + aIm * bRe;

					sumRe += resRe;
					sumIm += resIm;
				}
			}
			curr = curr.next;
		}

		return new float[] { sumRe, sumIm };
	}

	@Override
	@NonNull
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("[");
		Range curr = first;
		while (curr != null) {
			final int size = curr.end - curr.start;
			if (!curr.isGap()) {
				for (int i = 0; i < size; i++) {
					final float re = curr.elementsRe[i];
					final float im = curr.elementsIm[i];
					sb.append(formatComplexNumber(re, im));
					final boolean last = (i == size - 1);
					if (!last) {
						sb.append(",");
					}
				}
			} else {
				for (int i = 0; i < size; i++) {
					sb.append("0");
					final boolean last = (i == size - 1);
					if (!last) {
						sb.append(",");
					}
				}
			}
			final boolean lastRange = (curr.next == null);
			if (!lastRange) {
				sb.append("|");
			}

			curr = curr.next;
		}

		sb.append("]");
		return sb.toString();
	}

	@NonNull
	public static ComplexSparseVector fromArray(final float @NonNull [] vecRe,
			final float @NonNull [] vecIm, final float threshold) {
		final int defaultMinGapSize = 16;
		return fromArray(vecRe, vecIm, threshold, defaultMinGapSize);
	}

	@NonNull
	public static ComplexSparseVector fromArray(final float @NonNull [] vecRe,
			final float @NonNull [] vecIm, final float threshold,
			final int minGapSize) {
		if (vecRe.length != vecIm.length) {
			throw new IllegalArgumentException("real vector length "
					+ vecRe.length + " != imaginary vector length "
					+ vecIm.length);
		} else if (minGapSize < 1) {
			throw new IllegalArgumentException("minGapSize " + minGapSize
					+ " < 1");
		}

		final int size = vecRe.length;
		if (size == 0) {
			return new ComplexSparseVector(null, size, threshold);
		}

		final Range firstUnmerged = createRanges(vecRe, vecIm, threshold, size);
		final Range first = mergeSmallGaps(vecRe, vecIm, firstUnmerged,
				minGapSize, threshold);
		return new ComplexSparseVector(first, size, threshold);
	}

	@NonNull
	private static Range createRanges(final float @NonNull [] vecRe,
			final float @NonNull [] vecIm, final float threshold, final int size) {
		int start = 0;
		int end = 0;
		Range first = null;
		Range curr = null;
		boolean gap = (getMagnitude(vecRe, vecIm, 0) < threshold);
		while (start < size) {
			final Range next;
			if (!gap) {
				while (end < size
						&& getMagnitude(vecRe, vecIm, end) >= threshold) {
					end++;
				}
				final float[] elemsRe = new float[end - start];
				final float[] elemsIm = new float[end - start];
				System.arraycopy(vecRe, start, elemsRe, 0, end - start);
				System.arraycopy(vecIm, start, elemsIm, 0, end - start);
				next = Range.createRange(start, end, elemsRe, elemsIm);
			} else {
				while (end < size
						&& getMagnitude(vecRe, vecIm, end) < threshold) {
					end++;
				}
				next = Range.createGap(start, end);
			}
			if (curr != null) {
				curr.setNext(next);
			} else {
				first = next;
			}
			curr = next;
			gap = !gap;
			start = end;
		}

		if (first == null) {
			throw new AssertionError();
		}
		return first;
	}

	private static float getMagnitude(final float @NonNull [] vecRe,
			final float @NonNull [] vecIm, final int i) {
		return calcMagnitude(vecRe[i], vecIm[i]);
	}

	private static float calcMagnitude(final float re, final float im) {
		return (float) Math.sqrt(re * re + im * im);
	}

	private static final NumberFormat NUMBER_FORMAT = DecimalFormat
			.getInstance();

	private static String formatComplexNumber(final float re, final float im) {
		if (Math.abs(im) > 0.0000001) {
			return String.format("%s %si", NUMBER_FORMAT.format(re),
					NUMBER_FORMAT.format(im));
		} else {
			return NUMBER_FORMAT.format(re);
		}
	}

	@NonNull
	private static Range mergeSmallGaps(final float @NonNull [] vecRe,
			final float @NonNull [] vecIm, @NonNull Range first,
			final int minGapSize, final float threshold) {
		Range curr = first;
		Range before = null;
		while (curr != null) {
			// Merge-Start gefunden
			if ((!curr.isGap() && curr.next != null && curr.next.size() < minGapSize)
					|| (curr.isGap() && curr.size() < minGapSize)) {
				final Range beforeStart = before;
				final Range mergeStart = curr;

				// Finde Merge-Ende
				while (curr != null
						&& (!curr.isGap() || (curr.isGap() && curr.size() < minGapSize))) {
					before = curr;
					curr = curr.next;
				}
				final Range mergeEnd = before;

				final Range merged = mergeRanges(vecRe, vecIm, mergeStart,
						mergeEnd, threshold);
				if (beforeStart != null) {
					beforeStart.setNext(merged);
				} else {
					first = merged;
				}

				curr = before;
			}

			before = curr;
			curr = curr.next;
		}

		return first;
	}

	@NonNull
	private static Range mergeRanges(final float @NonNull [] vecRe,
			final float @NonNull [] vecIm, final @NonNull Range mergeStart,
			final @NonNull Range mergeEnd, final float threshold) {
		final float[] elemsRe = new float[mergeEnd.end - mergeStart.start];
		final float[] elemsIm = new float[mergeEnd.end - mergeStart.start];
		System.arraycopy(vecRe, mergeStart.start, elemsRe, 0, elemsRe.length);
		System.arraycopy(vecIm, mergeStart.start, elemsIm, 0, elemsIm.length);

		final Range merged = Range.createRange(mergeStart.start, mergeEnd.end,
				elemsRe, elemsIm);
		threshold(merged.elementsRe, merged.elementsIm, threshold);
		merged.setNext(mergeEnd.next);
		return merged;
	}

	private static void threshold(final float @NonNull [] vecRe,
			final float @NonNull [] vecIm, final float threshold) {
		assert vecIm.length == vecRe.length;
		assert threshold >= 0f;
		final int size = vecRe.length;
		for (int i = 0; i < size; i++) {
			final float magnitude = calcMagnitude(vecRe[i], vecIm[i]);
			if (magnitude < threshold) {
				vecRe[i] = 0f;
				vecIm[i] = 0f;
			}
		}
	}
}
