package de.skawronek.audiolib;

import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNull;

public abstract class AudioSource {
	protected final String name;
	protected final EnumSet<Channel> channels;
	protected final double sampleRate;
	protected final FrameManager frameManager;

	public static final class ReadException extends Exception {
		private static final long serialVersionUID = 1L;

		public ReadException() {
			super();
		}

		public ReadException(final String message, final Throwable cause) {
			super(message, cause);
		}

		public ReadException(final String message) {
			super(message);
		}

		public ReadException(final Throwable cause) {
			super(cause);
		}
	}

	public static final class UnavailableException extends Exception {
		private static final long serialVersionUID = 1L;

		public UnavailableException() {
			super();
		}

		public UnavailableException(final String message, final Throwable cause) {
			super(message, cause);
		}

		public UnavailableException(final String message) {
			super(message);
		}

		public UnavailableException(final Throwable cause) {
			super(cause);
		}
	}

	protected AudioSource(final @NonNull String name,
			final EnumSet<Channel> channels, final double sampleRate) {
		if (name.isEmpty()) {
			throw new IllegalArgumentException("name is empty");
		} else if (sampleRate <= 0.0) {
			throw new IllegalArgumentException("sampleRate " + sampleRate
					+ " <= 0");
		}

		this.name = name;
		this.channels = EnumSet.copyOf(channels);
		this.sampleRate = sampleRate;
		final boolean stereo = channels.contains(Channel.LEFT)
				&& channels.contains(Channel.RIGHT);
		if (stereo) {
			frameManager = FrameManager.forStereoAudioSource(sampleRate);
		} else {
			frameManager = FrameManager.forMonoAudioSource(sampleRate);
		}
	}

	public final @NonNull String getName() {
		return name;
	}

	public final @NonNull EnumSet<Channel> getAvailableChannels() {
		return EnumSet.copyOf(channels);
	}

	public final double getSampleRate() {
		return sampleRate;
	}

	public abstract boolean isAvailable();

	public abstract void start() throws UnavailableException;

	public abstract void stop();

	public abstract void process() throws ReadException;

	public void addListener(final @NonNull IFrameListener listener,
			final @NonNull FrameSpecification specification) {
		frameManager.addListener(listener, specification);
	}

	public void removeListener(final @NonNull IFrameListener listener) {
		frameManager.removeListener(listener);
	}
}
