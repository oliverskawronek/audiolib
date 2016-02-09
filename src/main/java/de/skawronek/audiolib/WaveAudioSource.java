package de.skawronek.audiolib;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.EnumSet;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.PcmCodec.UnsuportedFormatException;
import de.skawronek.audiolib.util.Util;

public final class WaveAudioSource extends AudioSource {
	private static final Duration BUFFER_LENGTH = Duration.ofSeconds(1);

	private final InputStream inputStream;
	private final PcmCodec codec;
	private final byte[] buffer;
	private boolean started = false;
	private boolean stopped = false;

	private WaveAudioSource(final @NonNull String name,
			final @NonNull InputStream inputStream,
			final @NonNull EnumSet<Channel> channels, final double sampleRate,
			final @NonNull PcmCodec codec) {
		super(name, channels, sampleRate);
		this.inputStream = inputStream;
		this.codec = codec;

		final int numSamples = (int) Util.durationToSamples(BUFFER_LENGTH,
				sampleRate);
		final int bufferSize = numSamples * codec.getSampleSizeInBytes();
		this.buffer = new byte[bufferSize];
	}

	@Override
	public boolean isAvailable() {
		// Der InputStream konnte bereits geöffnet und Header-Informationen
		// gelesen werden. Ein weitere Prüfung auf Verfügbarkeit ist daher nicht
		// nötig.
		return true;
	}

	@Override
	public void start() {
		if (stopped) {
			throw new IllegalStateException("Stopped");
		}

		started = true;
	}

	@Override
	public void stop() {
		try {
			inputStream.close();
		} catch (final IOException e) {
			// Ignoriere IOException beim Schließen.
		}

		started = false;
		stopped = true;
	}

	public boolean isStarted() {
		return started;
	}

	public boolean isStopped() {
		return stopped;
	}

	// Wird für PcmCodec#decodeStereo genutzt.
	// Anlegen auf Klassenebene verhindert Mehrfachallokation.
	private static final float[] STEREO_SAMPLE_BUFFER = new float[2];

	@Override
	public void process() throws ReadException {
		ensureStarted();

		final int sampleSize = codec.getSampleSizeInBytes();
		try {
			final int numAvailableBytes = inputStream.available();
			final int numBytesToRead = Math.min(numAvailableBytes,
					buffer.length);
			final int numBytesRead = inputStream
					.read(buffer, 0, numBytesToRead);

			final boolean eof = (numAvailableBytes == 0 || numBytesRead == -1);
			if (eof) {
				stop();
				return;
			}

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
		} catch (final IOException e) {
			throw new ReadException(e);
		}

		frameManager.processFrames();
	}

	private void ensureStarted() {
		if (!started) {
			throw new IllegalStateException("Not started");
		}
	}

	@NonNull
	public static WaveAudioSource fromFile(final @NonNull File file)
			throws UnsuportedFormatException, IOException {
		final InputStream inputStream = new FileInputStream(file);
		final String name = file.getName();
		return fromInputStream(inputStream, name);
	}

	@NonNull
	public static WaveAudioSource fromInputStream(
			final @NonNull InputStream inputStream)
			throws UnsuportedFormatException, IOException {
		return fromInputStream(inputStream, "Wave");
	}

	@NonNull
	private static WaveAudioSource fromInputStream(
			final @NonNull InputStream inputStream, final @NonNull String name)
			throws UnsuportedFormatException, IOException {
		// Der WAVE-File-Reader von Java muss an Marks reseten können
		final InputStream markSupportedInputStream;
		if (inputStream.markSupported()) {
			markSupportedInputStream = inputStream;
		} else {
			// BufferedInputStream wrapt InputStream, buffered das Gelesene
			// und kann damit an Marks resetten.

			// Standard-WAVE-Dateien haben mit RIFF-Header, Format-Chunk und
			// Data-Chunk eine Gesamtheadergröße von 44 Bytes. 8192 Bytes
			// sollten daher ausreichen. Unberücksichtigt bleiben untypische
			// Chuncks, wie Cue-Chunk, Playlist-Chunk, etc.
			final int bufferSize = 8192;
			markSupportedInputStream = new BufferedInputStream(inputStream,
					bufferSize);
		}

		final AudioInputStream stream;
		try {
			stream = AudioSystem.getAudioInputStream(markSupportedInputStream);
		} catch (final UnsupportedAudioFileException e) {
			throw new UnsuportedFormatException();
		}

		final AudioFormat format = stream.getFormat();

		final int numChannels = format.getChannels();
		final EnumSet<Channel> channels;
		if (numChannels == 1) {
			channels = EnumSet.of(Channel.MONO);
		} else if (numChannels > 1) {
			// Bei mehr als zwei Channels wird nur Stereo angeboten
			channels = EnumSet.of(Channel.LEFT, Channel.RIGHT, Channel.MONO);
		} else {
			throw new AssertionError("Expected one or more channels, but was "
					+ numChannels);
		}

		final PcmCodec codec = PcmCodec.fromAudioFormat(format);
		final float sampleRate = format.getSampleRate();
		return new WaveAudioSource(name, markSupportedInputStream, channels,
				sampleRate, codec);
	}
}
