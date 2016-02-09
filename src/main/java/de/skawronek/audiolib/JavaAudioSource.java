package de.skawronek.audiolib;

import java.time.Duration;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import de.skawronek.audiolib.PcmCodec.UnsuportedFormatException;
import de.skawronek.audiolib.util.Util;

public final class JavaAudioSource extends AudioSource {
	private static final Duration BUFFER_LENGTH = Duration.ofSeconds(1);

	private final Device device;
	private Mixer mixer;
	private TargetDataLine line;
	private final PcmCodec codec;
	private final byte[] buffer;

	private boolean started = false;
	private boolean stopped = false;

	public static final class Device {
		private final Mixer.Info javaInfo;
		private final AudioFormat supportedAudioFormat;

		public @NonNull String getVendor() {
			return javaInfo.getVendor();
		}

		public @NonNull String getName() {
			return javaInfo.getName();
		}

		public @NonNull String getDescription() {
			return javaInfo.getDescription();
		}

		private Device(final Mixer.@NonNull Info javaInfo,
				final @NonNull AudioFormat targetAudioFormat) {
			this.javaInfo = javaInfo;
			this.supportedAudioFormat = targetAudioFormat;
		}

		@Override
		public String toString() {
			return getName();
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null) {
				return false;
			} else if (obj == this) {
				return true;
			} else if (obj instanceof Device) {
				final Device other = (Device) obj;
				return this.javaInfo.equals(other.javaInfo);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return javaInfo.hashCode();
		}
	}

	private JavaAudioSource(@NonNull Device device) {
		//@formatter:off
		super(device.getName(),
				toChannels(device.supportedAudioFormat.getChannels()),
				device.supportedAudioFormat.getSampleRate());
		//@formatter:on
		this.device = device;
		try {
			this.codec = PcmCodec.fromAudioFormat(device.supportedAudioFormat);
		} catch (final UnsuportedFormatException e) {
			throw new AssertionError(e);
		}
		final int numSamples = (int) Util.durationToSamples(BUFFER_LENGTH,
				sampleRate);
		final int bufferSize = numSamples * codec.getSampleSizeInBytes();
		this.buffer = new byte[bufferSize];
	}

	private static @NonNull EnumSet<Channel> toChannels(final int numChannels) {
		switch (numChannels) {
		case 1:
			return EnumSet.of(Channel.MONO);
		case 2:
			return EnumSet.of(Channel.LEFT, Channel.RIGHT, Channel.MONO);
		default:
			throw new AssertionError("Expected 1 or 2 channels, but was "
					+ numChannels);
		}
	}

	@Override
	public boolean isAvailable() {
		if (started) {
			return true;
		} else {
			try (final Mixer mixer = AudioSystem.getMixer(device.javaInfo)) {
				mixer.open();
				return true;
			} catch (final LineUnavailableException e) {
				return false;
			}
		}
	}

	@Override
	public void start() throws UnavailableException {
		if (stopped) {
			throw new IllegalStateException("Stopped");
		}

		mixer = AudioSystem.getMixer(device.javaInfo);
		try {
			mixer.open();
			final Line.Info supportedLineInfo = toLineInfo(device.supportedAudioFormat);
			line = (TargetDataLine) mixer.getLine(supportedLineInfo);
			line.open();
			line.start();
		} catch (final LineUnavailableException e) {
			if (line != null) {
				line.close();
				line = null;
			}
			mixer.close();
			mixer = null;
			throw new UnavailableException(e);
		}

		started = true;
	}

	@Override
	public void stop() {
		if (line != null) {
			line.stop();
			line.close();
			line = null;
		}
		if (mixer != null) {
			mixer.close();
			mixer = null;
		}

		started = false;
		stopped = true;
	}

	// Wird für PcmCodec#decodeStereo genutzt.
	// Anlegen auf Klassenebene verhindert Mehrfachallokation.
	private static final float[] STEREO_SAMPLE_BUFFER = new float[2];

	@Override
	public void process() throws ReadException {
		ensureStarted();

		final int sampleSize = codec.getSampleSizeInBytes();
		final int numAvailableBytes = line.available();
		final int numBytesToRead = Math.min(numAvailableBytes, buffer.length);
		final int numBytesRead = line.read(buffer, 0, numBytesToRead);

		final int numSamplesRead = numBytesRead / sampleSize;
		for (int i = 0; i < numSamplesRead; i++) {
			final int offset = i * sampleSize;
			if (codec.isMono()) {
				final float sample = codec.decodeMono(buffer, offset);
				frameManager.putMonoSample(sample);
			} else if (codec.isStereo()) {
				codec.decodeStereo(buffer, offset, STEREO_SAMPLE_BUFFER);
				final float leftSample = STEREO_SAMPLE_BUFFER[0];
				final float rightSample = STEREO_SAMPLE_BUFFER[1];
				frameManager.putStereoSample(leftSample, rightSample);
			} else {
				throw new AssertionError();
			}
		}

		frameManager.processFrames();
	}

	private void ensureStarted() {
		if (!started) {
			throw new IllegalStateException("Not started");
		}
	}

	public boolean isStarted() {
		return started;
	}

	public boolean isStopped() {
		return stopped;
	}

	public static JavaAudioSource fromDevice(final @NonNull Device device) {
		return new JavaAudioSource(device);
	}

	public static @Nullable Device getDefaultDevice(final double sampleRate,
			final int numChannels, final int numBits) {
		final Set<Device> devices = getDevices(sampleRate, numChannels, numBits);
		final Optional<Device> first = devices.stream().findFirst();
		if (first.isPresent()) {
			return first.get();
		} else {
			return null;
		}
	}

	public static @NonNull Set<Device> getDevices(final double sampleRate,
			final int numChannels, final int numBits) {
		final Set<AudioFormat> formats = generateAudioFormats(
				(float) sampleRate, numChannels, numBits);

		final Set<Device> devices = new HashSet<>();
		for (final Mixer.Info javaInfo : AudioSystem.getMixerInfo()) {
			try (Mixer mixer = AudioSystem.getMixer(javaInfo)) {
				// Bei manchen Mixern muss zuvor open aufgerufen werden
				mixer.open();
				AudioFormat supportedAudioFormat = null;
				for (final AudioFormat format : formats) {
					final Line.Info lineInfo = toLineInfo(format);
					if (mixer.isLineSupported(lineInfo)) {
						supportedAudioFormat = format;
						break;
					}
				}
				if (supportedAudioFormat != null) {
					devices.add(new Device(javaInfo, supportedAudioFormat));
				}
				mixer.close();
			} catch (final LineUnavailableException e) {
			}
		}

		return devices;
	}

	private static Set<AudioFormat> generateAudioFormats(
			final float sampleRate, final int numChannels, final int numBits) {
		checkSampleRate(sampleRate);
		checkNumChannels(numChannels);
		checkNumBits(numBits);

		// Alle Kombinationen aus signed/unsigend und Big-/Little-Endian
		final Set<AudioFormat> formats = new HashSet<>();
		formats.add(new AudioFormat((float) sampleRate, numBits, numChannels,
				false, false));
		formats.add(new AudioFormat((float) sampleRate, numBits, numChannels,
				false, true));
		formats.add(new AudioFormat((float) sampleRate, numBits, numChannels,
				true, false));
		formats.add(new AudioFormat((float) sampleRate, numBits, numChannels,
				true, true));

		// Prüfe, ob PCM-Codec auch die Audioformate unterstützt
		final Set<AudioFormat> supportedFormats = formats.stream()
				.filter(af -> PcmCodec.isAudioFormatSupported(af))
				.collect(Collectors.toSet());

		return supportedFormats;
	}

	private static Line.Info toLineInfo(final @NonNull AudioFormat af) {
		return new DataLine.Info(TargetDataLine.class, af);
	}

	private static void checkSampleRate(final double sampleRate) {
		if (sampleRate <= 0) {
			throw new IllegalArgumentException("sampleRate " + sampleRate
					+ " <= 0");
		}
	}

	private static void checkNumChannels(final int numChannels) {
		if (!(numChannels == 1 || numChannels == 2)) {
			throw new IllegalArgumentException(
					"numChannels must be 1 or 2, but was " + numChannels);
		}
	}

	private static void checkNumBits(final int numBits) {
		if (!(numBits == 8 || numBits == 16)) {
			throw new IllegalArgumentException(
					"numBits must be 8 or 16, but was " + numBits);
		}
	}
}
