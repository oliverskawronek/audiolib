package de.skawronek.audiolib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import org.junit.Test;

import de.skawronek.audiolib.PcmCodec.UnsuportedFormatException;

public final class PcmCodecEncodingTest {
	private static final int COMPARISON_DELTA = 2;

	@Test
	public void testSigned8BitMono() throws IOException,
			UnsuportedFormatException {
		final AudioFormat format = new AudioFormat(Encoding.PCM_SIGNED,
				44100.0f, 8, 1, 1, 44100f, false);
		final PcmCodec codec = PcmCodec.fromAudioFormat(format);
		final byte[] buffer = new byte[1];

		codec.encodeMono(-1.0f, buffer, 0);
		assertEqualsWithDelta(0x80, buffer[0] & 0xff);
		codec.encodeMono(-0.5f, buffer, 0);
		assertEqualsWithDelta(0xc0, buffer[0] & 0xff);
		codec.encodeMono(0.0f, buffer, 0);
		assertEquals(0x00, buffer[0] & 0xff);
		codec.encodeMono(0.5f, buffer, 0);
		assertEqualsWithDelta(0x40, buffer[0] & 0xff);
		codec.encodeMono(1.0f, buffer, 0);
		assertEqualsWithDelta(0x80, buffer[0] & 0xff);
	}

	@Test
	public void testUnsigned8BitMono() throws IOException,
			UnsuportedFormatException {
		final AudioFormat format = new AudioFormat(Encoding.PCM_UNSIGNED,
				44100.0f, 8, 1, 1, 44100f, false);
		final PcmCodec codec = PcmCodec.fromAudioFormat(format);
		final byte[] buffer = new byte[1];

		codec.encodeMono(-1.0f, buffer, 0);
		assertEqualsWithDelta(0x00, buffer[0] & 0xff);
		codec.encodeMono(-0.5f, buffer, 0);
		assertEqualsWithDelta(0x40, buffer[0] & 0xff);
		codec.encodeMono(0.0f, buffer, 0);
		assertEquals(0x80, buffer[0] & 0xff);
		codec.encodeMono(0.5f, buffer, 0);
		assertEqualsWithDelta(0xc0, buffer[0] & 0xff);
		codec.encodeMono(1.0f, buffer, 0);
		assertEqualsWithDelta(0xff, buffer[0] & 0xff);
	}

	@Test
	public void testSigned16BitBigEndianMono() throws IOException,
			UnsuportedFormatException {
		final AudioFormat format = new AudioFormat(Encoding.PCM_SIGNED,
				44100.0f, 16, 1, 2, 44100f, true);
		final PcmCodec codec = PcmCodec.fromAudioFormat(format);
		final byte[] buffer = new byte[2];

		//@formatter:off
		codec.encodeMono(-1.0f, buffer, 0);
		assertEqualsWithDelta(0x8000,
				((buffer[0] & 0xff) << 8) | (buffer[1] & 0xff));
		codec.encodeMono(-0.5f, buffer, 0);
		assertEqualsWithDelta(0xc000,
				((buffer[0] & 0xff) << 8) | (buffer[1] & 0xff));
		codec.encodeMono(0.0f, buffer, 0);
		assertEquals(0x0000,
				((buffer[0] & 0xff) << 8) | (buffer[1] & 0xff));
		codec.encodeMono(0.5f, buffer, 0);
		assertEqualsWithDelta(0x4000,
				((buffer[0] & 0xff) << 8) | (buffer[1] & 0xff));
		codec.encodeMono(1.0f, buffer, 0);
		assertEqualsWithDelta(0x8000,
				((buffer[0] & 0xff) << 8) | (buffer[1] & 0xff));
		//@formatter:on
	}

	@Test
	public void testSigned16BitLittleEndianMono() throws IOException,
			UnsuportedFormatException {
		final AudioFormat format = new AudioFormat(Encoding.PCM_SIGNED,
				44100.0f, 16, 1, 2, 44100f, false);
		final PcmCodec codec = PcmCodec.fromAudioFormat(format);
		final byte[] buffer = new byte[2];

		//@formatter:off
		codec.encodeMono(-1.0f, buffer, 0);
		assertEqualsWithDelta(0x8000,
				((buffer[1] & 0xff) << 8) | (buffer[0] & 0xff));
		codec.encodeMono(-0.5f, buffer, 0);
		assertEqualsWithDelta(0xc000,
				((buffer[1] & 0xff) << 8) | (buffer[0] & 0xff));
		codec.encodeMono(0.0f, buffer, 0);
		assertEquals(0x0000,
				((buffer[1] & 0xff) << 8) | (buffer[0] & 0xff));
		codec.encodeMono(0.5f, buffer, 0);
		assertEqualsWithDelta(0x4000,
				((buffer[1] & 0xff) << 8) | (buffer[0] & 0xff));
		codec.encodeMono(1.0f, buffer, 0);
		assertEqualsWithDelta(0x8000,
				((buffer[1] & 0xff) << 8) | (buffer[0] & 0xff));
		//@formatter:on
	}

	@Test
	public void testUnsigned16BitBigEndianMono() throws IOException,
			UnsuportedFormatException {
		final AudioFormat format = new AudioFormat(Encoding.PCM_UNSIGNED,
				44100.0f, 16, 1, 2, 44100f, true);
		final PcmCodec codec = PcmCodec.fromAudioFormat(format);
		final byte[] buffer = new byte[2];

		//@formatter:off
		codec.encodeMono(-1.0f, buffer, 0);
		assertEqualsWithDelta(0x0000,
				((buffer[0] & 0xff) << 8) | (buffer[1] & 0xff));
		codec.encodeMono(-0.5f, buffer, 0);
		assertEqualsWithDelta(0x4000,
				((buffer[0] & 0xff) << 8) | (buffer[1] & 0xff));
		codec.encodeMono(0.0f, buffer, 0);
		assertEqualsWithDelta(0x8000,
				((buffer[0] & 0xff) << 8) | (buffer[1] & 0xff));
		codec.encodeMono(0.5f, buffer, 0);
		assertEqualsWithDelta(0xc000,
				((buffer[0] & 0xff) << 8) | (buffer[1] & 0xff));
		codec.encodeMono(1.0f, buffer, 0);
		assertEqualsWithDelta(0xffff,
				((buffer[0] & 0xff) << 8) | (buffer[1] & 0xff));
		//@formatter:on
	}

	@Test
	public void testUnsigned16BitLittleEndianMono() throws IOException,
			UnsuportedFormatException {
		final AudioFormat format = new AudioFormat(Encoding.PCM_UNSIGNED,
				44100.0f, 16, 1, 2, 44100f, false);
		final PcmCodec codec = PcmCodec.fromAudioFormat(format);
		final byte[] buffer = new byte[2];

		//@formatter:off
		codec.encodeMono(-1.0f, buffer, 0);
		assertEqualsWithDelta(0x0000,
				((buffer[1] & 0xff) << 8) | (buffer[0] & 0xff));
		codec.encodeMono(-0.5f, buffer, 0);
		assertEqualsWithDelta(0x4000,
				((buffer[1] & 0xff) << 8) | (buffer[0] & 0xff));
		codec.encodeMono(0.0f, buffer, 0);
		assertEqualsWithDelta(0x8000,
				((buffer[1] & 0xff) << 8) | (buffer[0] & 0xff));
		codec.encodeMono(0.5f, buffer, 0);
		assertEqualsWithDelta(0xc000,
				((buffer[1] & 0xff) << 8) | (buffer[0] & 0xff));
		codec.encodeMono(1.0f, buffer, 0);
		assertEqualsWithDelta(0xffff,
				((buffer[1] & 0xff) << 8) | (buffer[0] & 0xff));
		//@formatter:on
	}

	private void assertEqualsWithDelta(final int a, final int b) {
		final int diff = Math.abs(a - b);
		if (diff >= COMPARISON_DELTA) {
			final String message = String.format("expected:<%d> but was:<%d>",
					a, b);
			fail(message);
		}
	}
}
