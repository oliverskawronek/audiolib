package de.skawronek.audiolib;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import de.skawronek.audiolib.util.FloatRingBuffer;
import de.skawronek.audiolib.util.Util;

final class FrameManager {
	private static final @NonNull Duration BUFFER_LENGTH = Duration
			.ofSeconds(3);

	private static final class ListenerInformation {
		final FrameSpecification specification;
		private long nextFrameBegin = 0;

		ListenerInformation(@NonNull FrameSpecification specification) {
			this.specification = specification;
		}

		long getNextFrameBegin() {
			return nextFrameBegin;
		}

		void nextFrame() {
			nextFrameBegin += specification.getHopSize();
		}
	}

	private final double sampleRate;
	// Entweder wird der Stereo-Buffer oder Mono-Buffer genutzt:
	// (leftBuffer != null && rightBuffer != null) || (monoBuffer != null)
	private final FloatRingBuffer leftBuffer;
	private final FloatRingBuffer rightBuffer;
	private final FloatRingBuffer monoBuffer;

	private final Map<IFrameListener, ListenerInformation> listenerInformations = new HashMap<>();

	private FrameManager(final double sampleRate,
			final @Nullable FloatRingBuffer leftBuffer,
			final @Nullable FloatRingBuffer rightBuffer,
			final @Nullable FloatRingBuffer monoBuffer) {
		this.sampleRate = sampleRate;
		this.leftBuffer = leftBuffer;
		this.rightBuffer = rightBuffer;
		this.monoBuffer = monoBuffer;
		assert (leftBuffer != null && rightBuffer != null)
				|| (monoBuffer != null);
	}

	public void addListener(final @NonNull IFrameListener listener,
			final @NonNull FrameSpecification specification) {
		final ListenerInformation information = new ListenerInformation(
				specification);
		listenerInformations.put(listener, information);
	}

	public void removeListener(final @NonNull IFrameListener listener) {
		listenerInformations.remove(listener);
	}

	public void putStereoSample(final float left, final float right) {
		ensureIsStereo();

		leftBuffer.put(left);
		rightBuffer.put(right);
		assert leftBuffer.getCount() == rightBuffer.getCount();
	}

	private void ensureIsStereo() {
		if (!isStereo()) {
			throw new IllegalStateException(
					"FrameManager was not created for stereo audio signals");
		}
	}

	public void putMonoSample(final float sample) {
		ensureIsMono();
		monoBuffer.put(sample);
	}

	private void ensureIsMono() {
		if (!isMono()) {
			throw new IllegalStateException(
					"FrameManager was not created for mono audio signals");
		}
	}

	public void processFrames() {
		final long bufferCount = getBufferCount();
		final int bufferSize = getBufferSize();
		final Set<Frame> createdFrames = new HashSet<>();
		for (final Map.Entry<IFrameListener, ListenerInformation> entry : listenerInformations
				.entrySet()) {
			final ListenerInformation information = entry.getValue();
			while (isFrameAvailable(bufferCount, bufferSize, information)) {
				final long frameBegin = information.getNextFrameBegin();
				final int frameSize = information.specification.getSize();

				//@formatter:off
				final Optional<Frame> existingFrame = createdFrames.stream()
						.filter(f -> f.getBegin() == frameBegin
						&& f.getSize() == frameSize).findAny();
				//@formatter:on
				final Frame frame;
				if (existingFrame.isPresent()) {
					frame = existingFrame.get();
				} else {
					if (isMono()) {
						final float[] samples = new float[frameSize];
						monoBuffer.peak(frameBegin, samples, 0, frameSize);
						frame = Frame.createMonoFrame(frameBegin, sampleRate,
								samples);
					} else if (isStereo()) {
						final float[] leftSamples = new float[frameSize];
						final float[] rightSamples = new float[frameSize];
						leftBuffer.peak(frameBegin, leftSamples, 0, frameSize);
						rightBuffer
								.peak(frameBegin, rightSamples, 0, frameSize);
						frame = Frame.createStereoFrame(frameBegin, sampleRate,
								leftSamples, rightSamples);
					} else {
						throw new AssertionError();
					}
					createdFrames.add(frame);
				}

				final IFrameListener listener = entry.getKey();
				listener.onFrameAvailable(frame);

				information.nextFrame();
			}
		}
	}

	private long getBufferCount() {
		if (isMono()) {
			return monoBuffer.getCount();
		} else {
			assert leftBuffer.getCount() == rightBuffer.getCount();
			return leftBuffer.getCount();
		}
	}

	private int getBufferSize() {
		if (isMono()) {
			return monoBuffer.getSize();
		} else {
			assert leftBuffer.getSize() == rightBuffer.getSize();
			return leftBuffer.getSize();
		}
	}

	public boolean isMono() {
		return monoBuffer != null;
	}

	public boolean isStereo() {
		return leftBuffer != null && rightBuffer != null;
	}

	private static boolean isFrameAvailable(final long bufferCount,
			final int bufferSize, final ListenerInformation information) {
		final long oldestSample = bufferCount - bufferSize;
		final long newestSample = bufferCount;
		final int frameSize = information.specification.getSize();
		if (information.nextFrameBegin >= oldestSample
				&& information.nextFrameBegin + frameSize <= newestSample) {
			return true;
		} else {
			return false;
		}
	}

	@NonNull
	public static FrameManager forMonoAudioSource(final double sampleRate) {
		checkSampleRate(sampleRate);
		final FloatRingBuffer leftBuffer = null;
		final FloatRingBuffer rightBuffer = null;
		final FloatRingBuffer monoBuffer = createBuffer(sampleRate);
		return new FrameManager(sampleRate, leftBuffer, rightBuffer, monoBuffer);
	}

	@NonNull
	public static FrameManager forStereoAudioSource(final double sampleRate) {
		checkSampleRate(sampleRate);
		final FloatRingBuffer leftBuffer = createBuffer(sampleRate);
		final FloatRingBuffer rightBuffer = createBuffer(sampleRate);
		final FloatRingBuffer monoBuffer = null;
		return new FrameManager(sampleRate, leftBuffer, rightBuffer, monoBuffer);
	}

	private static void checkSampleRate(final double sampleRate) {
		if (sampleRate <= 0) {
			throw new IllegalArgumentException("sampleRate " + sampleRate
					+ " <= 0");
		}
	}

	@NonNull
	private static FloatRingBuffer createBuffer(final double sampleRate) {
		final int capacity = (int) Util.durationToSamples(BUFFER_LENGTH,
				sampleRate);
		return new FloatRingBuffer(capacity);
	}
}
