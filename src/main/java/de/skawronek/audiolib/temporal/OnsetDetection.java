package de.skawronek.audiolib.temporal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import de.skawronek.audiolib.Frame;
import de.skawronek.audiolib.math.AdaptivePeakPicking;
import de.skawronek.audiolib.signal.FastFourierTransform;
import de.skawronek.audiolib.signal.FastFourierTransform.Key;
import de.skawronek.audiolib.util.FloatRingBuffer;
import de.skawronek.audiolib.util.RingBuffer;

public final class OnsetDetection {
	private static final Key FFT_KEY = FastFourierTransform.getKey();

	private static final NoveltyFunction DEFAULT_NOVELTY_FUNCTION = NoveltyFunction.COMPLEX_DISTANCE;

	public static enum NoveltyFunction {
		HIGH_FREQUENCY_CONTENT, COMPLEX_DISTANCE
	}

	private final NoveltyFunction noveltyFunction;
	private final float[] noveltiesBuffer;
	private final FloatRingBuffer novelties;
	private final RingBuffer<Frame> frames;
	private final AdaptivePeakPicking peakPicking;
	private final Set<IListener> listeners = new HashSet<>();

	public static interface IListener {
		public void onOnset(final @NonNull Frame frame);
	}

	public OnsetDetection(final @NonNull NoveltyFunction nf,
			final int windowSize, final float baseThreshold, final float weight) {
		noveltyFunction = nf;
		noveltiesBuffer = new float[windowSize];
		novelties = new FloatRingBuffer(windowSize);
		frames = new RingBuffer<>(windowSize);
		peakPicking = new AdaptivePeakPicking(windowSize, baseThreshold, weight);
	}

	public OnsetDetection(final int windowSize, final float baseThreshold,
			final float weight) {
		this(DEFAULT_NOVELTY_FUNCTION, windowSize, baseThreshold, weight);
	}

	public void addListener(final @NonNull IListener listener) {
		listeners.add(listener);
	}

	public void removeListener(final @NonNull IListener listener) {
		listeners.remove(listener);
	}

	public void processFrame(final @NonNull Frame frame) {
		final Frame lastFrame = getLastFrame();
		frames.put(frame);
		final float novelty;
		switch (noveltyFunction) {
		case COMPLEX_DISTANCE:
			novelty = computeComplexDistance(lastFrame, frame);
			break;
		case HIGH_FREQUENCY_CONTENT:
			novelty = computeHighFrequencyContent(frame);
			break;
		default:
			throw new AssertionError("Unsupported novelty function: "
					+ noveltyFunction);
		}
		novelties.put(novelty);

		final int windowSize = novelties.getCapacity();
		final boolean enaughFrames = (novelties.getSize() == windowSize);
		if (enaughFrames) {
			novelties.peakLast(noveltiesBuffer, 0, windowSize);
			peakPicking.compute(noveltiesBuffer);
			final int mid = windowSize / 2;
			if (peakPicking.getPeaks().contains(mid)) {
				final Frame onset = frames.get(frames.getCount() - mid);
				notifyOnset(onset);
			}
		}
	}

	private Frame getLastFrame() {
		final Frame lastFrame;
		if (!frames.isEmpty()) {
			lastFrame = frames.peakLast();
		} else {
			lastFrame = null;
		}
		return lastFrame;
	}

	private static float computeHighFrequencyContent(final @NonNull Frame curr) {
		float sum = 0;
		final FastFourierTransform fft = curr.getFeature(FFT_KEY);
		final int numBins = fft.getSize();
		final float[] spectrum = fft.getMagnitudeSpectrum();
		for (int k = 0; k < numBins; k++) {
			sum += spectrum[k];
		}
		return sum;
	}

	private static float computeComplexDistance(final @Nullable Frame before,
			final @NonNull Frame curr) {
		if (before == null) {
			return 0f;
		}

		final FastFourierTransform beforeFft = before.getFeature(FFT_KEY);
		final FastFourierTransform currentFft = curr.getFeature(FFT_KEY);
		final int numBins = currentFft.getSize();

		final float[] beRe = beforeFft.getReal();
		final float[] beIm = beforeFft.getImaginary();
		final float[] cuRe = currentFft.getReal();
		final float[] cuIm = currentFft.getImaginary();

		float distance = 0f;
		for (int k = 0; k < numBins; k++) {
			final float diffRe = cuRe[k] - beRe[k];
			final float diffIm = cuIm[k] - beIm[k];
			distance += (float) Math.sqrt(diffRe * diffRe + diffIm * diffIm);
		}

		return distance;
	}

	private void notifyOnset(final @NonNull Frame onset) {
		for (final IListener listener : listeners) {
			listener.onOnset(onset);
		}
	}

	public void reset() {
		frames.clear();
		Arrays.fill(noveltiesBuffer, 0f);
		novelties.clear();
	}
}
