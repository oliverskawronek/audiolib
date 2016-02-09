package de.skawronek.audiolib;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import org.junit.Test;

import de.skawronek.audiolib.PcmCodec.UnsuportedFormatException;

public class PcmCodecTest {
	@Test
	public void testIsAudioFormatSupported() {
		// Teste, ob bei unterstützten Formaten true zurückgegeben wird
		for (final AudioFormat format : generateSupportedFormats()) {
			assertTrue(PcmCodec.isAudioFormatSupported(format));
		}

		// Teste, ob bei nicht unterstützten Formaten false zurückgegeben wird
		for (final AudioFormat format : generateUnsupportedFormats()) {
			assertFalse(PcmCodec.isAudioFormatSupported(format));
		}
	}

	@Test
	public void testFromAudioFormat() {
		// Teste, ob bei nicht unterstützten Formaten UnsuportedFormatException
		// geworfen wird.
		for (final AudioFormat format : generateUnsupportedFormats()) {
			try {
				PcmCodec.fromAudioFormat(format);
				fail("Expected UnsuportedFormatException");
			} catch (final UnsuportedFormatException e) {
				// UnsuportedFormatException soll geworfen bei nicht
				// unterstützten Formaten geworfen werden.
			} catch (final Exception e) {
				fail("Expected UnsuportedFormatException, but was " + e);
			}
		}

		// Bei unterstützten Formaten darf keine Exception geschmissen werden.
		for (final AudioFormat format : generateSupportedFormats()) {
			try {
				assertNotNull(PcmCodec.fromAudioFormat(format));
			} catch (final UnsuportedFormatException e) {
				fail("Unexpected UnsuportedFormatException");
			}
		}
	}

	private static Set<AudioFormat> generateSupportedFormats() {
		// Unterstützt werden Kombinationen von (Mono|Stereo), (8 Bit|16 Bit),
		// (Signed|Unsigned) und (LittleEndian|BigEndian)

		// SampleRate ist für den Codec nicht relevant.
		final float sampleRate = 44100;

		// Generiere alle möglichen Kombinationen
		final Set<AudioFormat> supportedFormats = new HashSet<>();
		for (int numChannels = 1; numChannels <= 2; numChannels++) {
			for (int bit = 0; bit < 2; bit++) {
				final int numBits = (bit == 0 ? 8 : 16);
				for (int signedness = 0; signedness < 2; signedness++) {
					final boolean signed = (signedness == 0);
					// Bei 16 Bit ist die Endianess relevant
					if (numBits == 16) {
						for (int endian = 0; endian < 2; endian++) {
							final boolean bigEndian = (endian == 0);
							final AudioFormat format = new AudioFormat(
									sampleRate, numBits, numChannels, signed,
									bigEndian);
							supportedFormats.add(format);
						}
					} else {
						final AudioFormat format = new AudioFormat(sampleRate,
								numBits, numChannels, signed, false);
						supportedFormats.add(format);
					}
				}
			}
		}

		return supportedFormats;
	}

	private static Set<AudioFormat> generateUnsupportedFormats() {
		final Set<AudioFormat> unsupportedFormats = new HashSet<>();

		// SampleRate und FrameRate ist für den Codec nicht relevant.
		final float sampleRate = 44100;
		final float frameRate = 44100;

		int numBits = 16;
		boolean signed = false;
		boolean bigEndian = false;
		int numChannels;

		// Generiere nicht unterstützte Anzahl an Channels
		numChannels = 0;
		unsupportedFormats.add(new AudioFormat(sampleRate, numBits,
				numChannels, signed, bigEndian));
		numChannels = 3;
		unsupportedFormats.add(new AudioFormat(sampleRate, numBits,
				numChannels, signed, bigEndian));

		// Generiere nicht unterstützte SampleSizes
		numChannels = 2;
		numBits = 24;
		unsupportedFormats.add(new AudioFormat(sampleRate, numBits, 0, signed,
				false));
		numBits = 32;
		unsupportedFormats.add(new AudioFormat(sampleRate, numBits, 3, signed,
				false));

		// Generiere nicht unterstütztes Encoding
		numBits = 32;
		numChannels = 1;
		int frameSize = numBits / 8;
		unsupportedFormats.add(new AudioFormat(Encoding.PCM_FLOAT, sampleRate,
				numBits, numChannels, frameSize, frameRate, bigEndian));

		return unsupportedFormats;
	}

	@Test
	public void testIsMono() throws UnsuportedFormatException {
		int numChannels = 1;
		final PcmCodec monoCodec = PcmCodec.fromAudioFormat(new AudioFormat(
				44100, 16, numChannels, false, false));
		assertTrue(monoCodec.isMono());

		numChannels = 2;
		final PcmCodec stereoCodec = PcmCodec.fromAudioFormat(new AudioFormat(
				44100, 16, numChannels, false, false));
		assertFalse(stereoCodec.isMono());
	}

	@Test
	public void testIsStereo() throws UnsuportedFormatException {
		int numChannels = 1;
		final PcmCodec monoCodec = PcmCodec.fromAudioFormat(new AudioFormat(
				44100, 16, numChannels, false, false));
		assertFalse(monoCodec.isStereo());

		numChannels = 2;
		final PcmCodec stereoCodec = PcmCodec.fromAudioFormat(new AudioFormat(
				44100, 16, numChannels, false, false));
		assertTrue(stereoCodec.isStereo());
	}
}
