package de.skawronek.audiolib.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

public final class AdaptivePeakPicking {
	private final int windowSize;
	private final float[] window;
	private final float baseThreshold;
	private final float weight;
	private final List<Integer> peaks = new ArrayList<>();
	private final Statistics statistics = new Statistics();

	public AdaptivePeakPicking(final int windowSize, final float baseThreshold,
			final float weight) {
		if (windowSize < 1) {
			throw new IllegalArgumentException("windowSize " + windowSize
					+ " < 1");
		}

		this.windowSize = windowSize;
		this.baseThreshold = baseThreshold;
		this.weight = weight;
		this.window = new float[windowSize];
	}

	public int getWindowSize() {
		return windowSize;
	}

	public float getBaseThreshold() {
		return baseThreshold;
	}

	public float getWeight() {
		return weight;
	}

	public void compute(final float @NonNull [] x) {
		peaks.clear();

		// Wähle Start- und End-Index so, dass
		// 1) die komplette Fenstergröße um x[i] und
		// 2) mindestens die direkten Nachbarn x[i-1], x[i+1]
		// betrachtet werden können.
		final int startIndex = Math.max(windowSize / 2, 1); // inkl.
		final int endIndex = Math.min(x.length - (windowSize - 1) / 2,
				x.length - 1); // exkl.

		for (int i = startIndex; i < endIndex; i++) {
			// Ist x[i] größer als sein linker und rechter Nachbar?
			final boolean localMaxima = x[i - 1] < x[i] && x[i] > x[i + 1];

			if (localMaxima) {
				// Fülle Fenster mit der Umgebung von x[i]
				final int windowStart = i - windowSize / 2;
				System.arraycopy(x, windowStart, window, 0, windowSize);

				// Adaptive Threshold
				final float threshold = calcThreshold(window);

				if (x[i] > threshold) {
					// x[i] ist lokales Maximum und größer als der Threshold
					peaks.add(i);
				}
			}
		}
	}

	private float calcThreshold(final float @NonNull [] window) {
		statistics.setInput(window);
		final float median = statistics.getMedian();
		return baseThreshold + weight * median;
	}

	@NonNull
	public List<Integer> getPeaks() {
		return Collections.unmodifiableList(peaks);
	}
}
