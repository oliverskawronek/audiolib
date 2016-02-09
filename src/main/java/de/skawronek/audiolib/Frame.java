package de.skawronek.audiolib;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import de.skawronek.audiolib.util.Util;

public final class Frame {
	private final long begin; // inkl.
	private final long end; // inkl.
	private final double sampleRate;
	private final float[] leftSamples;
	private final float[] rightSamples;
	private float[] monoSamples;
	private final Map<FeatureKey<?>, Feature> features = new HashMap<>();

	private Frame(final long begin, final double sampleRate,
			final float @Nullable[] leftSamples,
			final float @Nullable[] rightSamples,
			final float @Nullable[] monoSamples) {
		assert (leftSamples != null && rightSamples != null)
				|| (monoSamples != null);

		this.begin = begin;
		if (monoSamples != null) {
			this.end = begin + monoSamples.length - 1;
		} else {
			this.end = begin + leftSamples.length - 1;
		}
		this.sampleRate = sampleRate;
		this.leftSamples = leftSamples;
		this.rightSamples = rightSamples;
		this.monoSamples = monoSamples;
	}

	public long getBegin() {
		return begin;
	}

	public long getEnd() {
		return end;
	}

	public int getSize() {
		return (int) (end - begin + 1);
	}

	@NonNull
	public Duration getTime() {
		final long middle = begin + (end - begin) / 2;
		return Util.samplesToDuration(middle, sampleRate);
	}

	@NonNull
	public Duration getBeginTime() {
		return Util.samplesToDuration(begin, sampleRate);
	}

	@NonNull
	public Duration getEndTime() {
		return Util.samplesToDuration(end, sampleRate);
	}

	public double getSampleRate() {
		return sampleRate;
	}

	public boolean isStereo() {
		return leftSamples != null && rightSamples != null;
	}

	public float @Nullable[] getLeftSamples() {
		ensureStereo();
		return leftSamples;
	}

	public float @Nullable[] getRightSamples() {
		ensureStereo();
		return rightSamples;
	}

	private void ensureStereo() {
		if (!isStereo()) {
			throw new IllegalStateException("Frame has no stereo samples");
		}
	}

	public float @NonNull[] getMonoSamples() {
		final boolean monoAvailable = monoSamples != null;
		if (!monoAvailable) {
			mixDown();
		}

		return monoSamples;
	}

	// Linker und rechter Kanal werden zum Monokanal zusammengefasst.
	private void mixDown() {
		final int size = getSize();
		monoSamples = new float[size];
		for (int i = 0; i < size; i++) {
			// Berechne arithmetisches Mittel aus linken und rechten Kanal
			final float leftSample = leftSamples[i];
			final float rightSample = rightSamples[i];
			monoSamples[i] = (leftSample + rightSample) / 2;
		}
	}

	public boolean containsFeature(final @NonNull FeatureKey<?> key) {
		return features.containsKey(key);
	}

	public <F extends Feature> @NonNull F getFeature(
			final @NonNull FeatureKey<F> key) {
		if (features.containsKey(key)) {
			@SuppressWarnings("unchecked")
			final F feature = (F) features.get(key);
			return feature;
		} else {
			final F feature = FeatureFactory.getInstance().createFeature(this,
					key);
			features.put(key, feature);
			return feature;
		}
	}

	@NonNull
	public static Frame createStereoFrame(final long begin,
			final double sampleRate, final float @NonNull [] leftSamples,
			final float @NonNull [] rightSamples) {
		if (begin < 0) {
			throw new IllegalArgumentException("begin " + begin + " <= 0");
		} else if (sampleRate <= 0) {
			throw new IllegalArgumentException("sampleRate " + sampleRate
					+ " <= 0");
		} else if (leftSamples.length != rightSamples.length) {
			final String message = String.format(
					"Size of leftSamples %d differs from rightSamples %d",
					leftSamples.length, rightSamples.length);
			throw new IllegalArgumentException(message);
		} else if (leftSamples.length == 0) {
			throw new IllegalArgumentException("Empty arrays");
		}

		return new Frame(begin, sampleRate, leftSamples, rightSamples, null);
	}

	@NonNull
	public static Frame createMonoFrame(final long begin,
			final double sampleRate, final float @NonNull [] samples) {
		if (begin < 0) {
			throw new IllegalArgumentException("begin " + begin + " <= 0");
		} else if (sampleRate <= 0) {
			throw new IllegalArgumentException("sampleRate " + sampleRate
					+ " <= 0");
		} else if (samples.length == 0) {
			throw new IllegalArgumentException("samples is empty");
		}

		return new Frame(begin, sampleRate, null, null, samples);
	}
}
