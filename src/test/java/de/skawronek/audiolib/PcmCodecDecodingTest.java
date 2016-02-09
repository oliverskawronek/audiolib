package de.skawronek.audiolib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import org.junit.Test;

import de.skawronek.audiolib.PcmCodec.UnsuportedFormatException;

public final class PcmCodecDecodingTest {
	private static final float COMPARISION_DELTA = 0.1f;
	private static final float AMPLITUDE = 0.8f;

	private static final String SIGNED_8BIT = "/codec/Sin440Hz_Signed8Bit.raw";
	private static final String UNSIGNED_8BIT = "/codec/Sin440Hz_Unsigned8Bit.raw";
	private static final String SIGNED_16BIT_BIG_ENDIAN = "/codec/Sin440Hz_Signed16Bit_BigEndian.raw";
	private static final String SIGNED_16BIT_LITTLE_ENDIAN = "/codec/Sin440Hz_Signed16Bit_LittleEndian.raw";
	private static final String UNSIGNED_16BIT_BIG_ENDIAN = "/codec/Sin440Hz_Unsigned16Bit_BigEndian.raw";
	private static final String UNSIGNED_16BIT_LITTLE_ENDIAN = "/codec/Sin440Hz_Unsigned16Bit_LittleEndian.raw";
	private static final String SIGNED_16BIT_BIG_ENDIAN_STEREO = "/codec/Sin440Hz_Signed16Bit_BigEndian_Stereo.raw";
	private static final String SIGNED_16BIT_LITTLE_ENDIAN_STEREO = "/codec/Sin440Hz_Signed16Bit_LittleEndian_Stereo.raw";
	private static final String UNSIGNED_8BIT_STEREO = "/codec/Sin440Hz_Unsigned8Bit_Stereo.raw";

	@Test
	public void testSigned8BitMono() throws UnsuportedFormatException,
			IOException {
		final float sampleRate = 44100.0f;
		final int sampleSizeInBits = 8;
		final int numChannels = 1;
		final int frameSize = 1;
		final float frameRate = 44100f;
		final boolean bigEndian = false;
		final AudioFormat format = new AudioFormat(Encoding.PCM_SIGNED,
				sampleRate, sampleSizeInBits, numChannels, frameSize,
				frameRate, bigEndian);
		final float[] samples = readMonoSamples(SIGNED_8BIT, format);
		testMonoSamples(samples);
	}

	@Test
	public void testUnsigned8BitMono() throws UnsuportedFormatException,
			IOException {
		final float sampleRate = 44100.0f;
		final int sampleSizeInBits = 8;
		final int numChannels = 1;
		final int frameSize = 1;
		final float frameRate = 44100f;
		final boolean bigEndian = false;
		final AudioFormat format = new AudioFormat(Encoding.PCM_UNSIGNED,
				sampleRate, sampleSizeInBits, numChannels, frameSize,
				frameRate, bigEndian);
		final float[] samples = readMonoSamples(UNSIGNED_8BIT, format);
		testMonoSamples(samples);
	}

	@Test
	public void testSigned16BitBigEndianMono()
			throws UnsuportedFormatException, IOException {
		final float sampleRate = 44100.0f;
		final int sampleSizeInBits = 16;
		final int numChannels = 1;
		final int frameSize = 2;
		final float frameRate = 44100f;
		final boolean bigEndian = true;
		final AudioFormat format = new AudioFormat(Encoding.PCM_SIGNED,
				sampleRate, sampleSizeInBits, numChannels, frameSize,
				frameRate, bigEndian);
		final float[] samples = readMonoSamples(SIGNED_16BIT_BIG_ENDIAN, format);
		testMonoSamples(samples);
	}

	@Test
	public void testSigned16BitLittleEndianMono()
			throws UnsuportedFormatException, IOException {
		final float sampleRate = 44100.0f;
		final int sampleSizeInBits = 16;
		final int numChannels = 1;
		final int frameSize = 2;
		final float frameRate = 44100f;
		final boolean bigEndian = false;
		final AudioFormat format = new AudioFormat(Encoding.PCM_SIGNED,
				sampleRate, sampleSizeInBits, numChannels, frameSize,
				frameRate, bigEndian);
		final float[] samples = readMonoSamples(SIGNED_16BIT_LITTLE_ENDIAN,
				format);
		testMonoSamples(samples);
	}

	@Test
	public void testUnsigned16BitBigEndianMono()
			throws UnsuportedFormatException, IOException {
		final float sampleRate = 44100.0f;
		final int sampleSizeInBits = 16;
		final int numChannels = 1;
		final int frameSize = 2;
		final float frameRate = 44100f;
		final boolean bigEndian = true;
		final AudioFormat format = new AudioFormat(Encoding.PCM_UNSIGNED,
				sampleRate, sampleSizeInBits, numChannels, frameSize,
				frameRate, bigEndian);
		final float[] samples = readMonoSamples(UNSIGNED_16BIT_BIG_ENDIAN,
				format);
		testMonoSamples(samples);
	}

	@Test
	public void testUnsigned16BitLittleEndianMono()
			throws UnsuportedFormatException, IOException {
		final float sampleRate = 44100.0f;
		final int sampleSizeInBits = 16;
		final int numChannels = 1;
		final int frameSize = 2;
		final float frameRate = 44100f;
		final boolean bigEndian = false;
		final AudioFormat format = new AudioFormat(Encoding.PCM_UNSIGNED,
				sampleRate, sampleSizeInBits, numChannels, frameSize,
				frameRate, bigEndian);
		final float[] samples = readMonoSamples(UNSIGNED_16BIT_LITTLE_ENDIAN,
				format);
		testMonoSamples(samples);
	}

	@Test
	public void testUnsigned8Stereo() throws UnsuportedFormatException,
			IOException {
		final float sampleRate = 44100.0f;
		final int sampleSizeInBits = 8;
		final int numChannels = 2;
		final int frameSize = 2;
		final float frameRate = 44100f;
		final boolean bigEndian = false;
		final AudioFormat format = new AudioFormat(Encoding.PCM_UNSIGNED,
				sampleRate, sampleSizeInBits, numChannels, frameSize,
				frameRate, bigEndian);
		final float[][] samples = readStereoSamples(UNSIGNED_8BIT_STEREO,
				format);
		testStereoSamples(samples);
	}

	@Test
	public void testSigned16BitBigEndianStereo()
			throws UnsuportedFormatException, IOException {
		final float sampleRate = 44100.0f;
		final int sampleSizeInBits = 16;
		final int numChannels = 2;
		final int frameSize = 4;
		final float frameRate = 44100f;
		final boolean bigEndian = true;
		final AudioFormat format = new AudioFormat(Encoding.PCM_SIGNED,
				sampleRate, sampleSizeInBits, numChannels, frameSize,
				frameRate, bigEndian);
		final float[][] samples = readStereoSamples(
				SIGNED_16BIT_BIG_ENDIAN_STEREO, format);
		testStereoSamples(samples);
	}

	@Test
	public void testSigned16BitLittleEndianStereo()
			throws UnsuportedFormatException, IOException {
		final float sampleRate = 44100.0f;
		final int sampleSizeInBits = 16;
		final int numChannels = 2;
		final int frameSize = 4;
		final float frameRate = 44100f;
		final boolean bigEndian = false;
		final AudioFormat format = new AudioFormat(Encoding.PCM_SIGNED,
				sampleRate, sampleSizeInBits, numChannels, frameSize,
				frameRate, bigEndian);
		final float[][] samples = readStereoSamples(
				SIGNED_16BIT_LITTLE_ENDIAN_STEREO, format);
		testStereoSamples(samples);
	}

	private void testMonoSamples(final float[] sin) {
		final int numSamples = sin.length;
		assertEquals(2048, numSamples);

		// Teste Nulldurchgänge und Wendepunkte einer Periode
		assertEquals(0.0f, sin[0], COMPARISION_DELTA);
		assertEquals(AMPLITUDE, sin[24], COMPARISION_DELTA);
		assertEquals(0.0f, sin[49], COMPARISION_DELTA);
		assertEquals(-AMPLITUDE, sin[74], COMPARISION_DELTA);
		assertEquals(0.0f, sin[99], COMPARISION_DELTA);
	}

	private void testStereoSamples(final float[][] sin) {
		final int numSamples = sin[0].length;
		assertEquals(2048, numSamples);

		// Mix down
		final float[] mono = new float[numSamples];
		for (int i = 0; i < numSamples; i++) {
			final float left = sin[0][i];
			final float right = sin[1][i];
			mono[i] = (left + right) / 2;
		}

		// Teste Nulldurchgänge und Wendepunkte einer Periode
		assertEquals(0.0f, mono[0], COMPARISION_DELTA);
		assertEquals(AMPLITUDE, mono[24], COMPARISION_DELTA);
		assertEquals(0.0f, mono[49], COMPARISION_DELTA);
		assertEquals(-AMPLITUDE, mono[74], COMPARISION_DELTA);
		assertEquals(0.0f, mono[99], COMPARISION_DELTA);
	}

	private static float[] readMonoSamples(final String fileName,
			final AudioFormat format) throws UnsuportedFormatException,
			IOException {
		final PcmCodec codec = PcmCodec.fromAudioFormat(format);
		final byte[] data = readWholeStream(fileName);
		final int numSamples = data.length / format.getFrameSize();
		final float[] samples = new float[numSamples];
		for (int i = 0; i < numSamples; i++) {
			final int offset = i * format.getFrameSize();
			samples[i] = codec.decodeMono(data, offset);
			assertFalse(Float.isNaN(samples[i]));
			assertFalse(Float.isInfinite(samples[i]));
			//@formatter:off
			assertTrue("samples[" + i + "] = " + samples[i]
					+ " is out of range -1..1",
					samples[i] >= -1.0f	&& samples[i] <= 1.0f);
			//@formatter:on
		}
		return samples;
	}

	private static float[][] readStereoSamples(final String fileName,
			final AudioFormat format) throws UnsuportedFormatException,
			IOException {
		final PcmCodec codec = PcmCodec.fromAudioFormat(format);
		final byte[] data = readWholeStream(fileName);
		final int numSamples = data.length / format.getFrameSize();
		final float[][] samples = new float[2][numSamples];
		final float[] stereoSample = new float[2];

		for (int i = 0; i < numSamples; i++) {
			final int offset = i * format.getFrameSize();
			codec.decodeStereo(data, offset, stereoSample);
			for (int channel = 0; channel < 2; channel++) {
				samples[channel][i] = stereoSample[channel];
				assertFalse(Float.isNaN(samples[channel][i]));
				assertFalse(Float.isInfinite(samples[channel][i]));
				//@formatter:off
				assertTrue("samples[" + channel + "][" + i + "] = "
						+ samples[channel][i] + " is out of range -1..1",
						samples[channel][i] >= -1.0f && samples[channel][i] <= 1.0f);
				//@formatter:on
			}
		}
		return samples;
	}

	private static byte[] readWholeStream(final String fileName)
			throws IOException {
		final byte[] buffer = new byte[8192];
		try (InputStream inStream = PcmCodecDecodingTest.class
				.getResourceAsStream(fileName);
				ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
			while (true) {
				final int numBytesRead = inStream.read(buffer);
				if (numBytesRead < 1) {
					break;
				}
				outStream.write(buffer, 0, numBytesRead);
			}
			return outStream.toByteArray();
		}
	}
}
