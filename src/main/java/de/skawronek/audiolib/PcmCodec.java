package de.skawronek.audiolib;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioSystem;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.util.Util;

/**
 * PCM Codec
 * 
 * @author Oliver Skawronek
 * 
 */
public final class PcmCodec {
	public final static class UnsuportedFormatException extends Exception {
		private static final long serialVersionUID = 1L;

	}

	private enum Format {
		//@formatter:off
		SIGNED_8_BIT,
		UNSIGNED_8_BIT,
		SIGNED_16_BIT_LITTLE_ENDIAN,
		SIGNED_16_BIT_BIG_ENDIAN,
		UNSIGNED_16_BIT_LITTLE_ENDIAN,
		UNSIGNED_16_BIT_BIG_ENDIAN;
		//@formatter:on
	}

	private final Format format;
	private final int numChannels;

	private PcmCodec(final @NonNull Format internalFormat, final int numChannels) {
		this.format = internalFormat;
		this.numChannels = numChannels;
	}

	public int getSampleSizeInBytes() {
		final int sizePerChannel; // in Bytes
		switch (format) {
		case SIGNED_8_BIT:
		case UNSIGNED_8_BIT:
			sizePerChannel = 1;
			break;
		case SIGNED_16_BIT_LITTLE_ENDIAN:
		case SIGNED_16_BIT_BIG_ENDIAN:
		case UNSIGNED_16_BIT_LITTLE_ENDIAN:
		case UNSIGNED_16_BIT_BIG_ENDIAN:
			sizePerChannel = 2;
			break;
		default:
			throw new AssertionError("Unexpected format: " + format);
		}

		return numChannels * sizePerChannel;
	}

	public boolean isStereo() {
		return numChannels == 2;
	}

	public boolean isMono() {
		return numChannels == 1;
	}

	public float decodeMono(final byte @NonNull [] buffer, final int offset) {
		final float sample;
		switch (format) {
		case SIGNED_8_BIT:
			sample = decodeSigned8Bit(buffer, offset);
			break;
		case UNSIGNED_8_BIT:
			sample = decodeUnsigned8Bit(buffer, offset);
			break;
		case SIGNED_16_BIT_BIG_ENDIAN:
			sample = decodeSigned16BitBigEndian(buffer, offset);
			break;
		case SIGNED_16_BIT_LITTLE_ENDIAN:
			sample = decodeSigned16BitLittleEndian(buffer, offset);
			break;
		case UNSIGNED_16_BIT_BIG_ENDIAN:
			sample = decodeUnsigned16BitBigEndian(buffer, offset);
			break;
		case UNSIGNED_16_BIT_LITTLE_ENDIAN:
			sample = decodeUnsigned16BitLittleEndian(buffer, offset);
			break;
		default:
			throw new AssertionError();
		}

		return Util.clamp(sample, -1f, 1f);
	}

	/**
	 * 
	 * @param buffer
	 * @param offset
	 * @param samples
	 *            Left channel has index 0, right channel has index 1
	 */
	public void decodeStereo(final byte @NonNull [] buffer, int offset,
			final float @NonNull [] samples) {
		float left = 0.0f, right = 0.0f;
		for (int i = 0; i < 2; i++) {
			final float sample;
			switch (format) {
			case SIGNED_8_BIT:
				sample = decodeSigned8Bit(buffer, offset);
				offset += 1;
				break;
			case UNSIGNED_8_BIT:
				sample = decodeUnsigned8Bit(buffer, offset);
				offset += 1;
				break;
			case SIGNED_16_BIT_BIG_ENDIAN:
				sample = decodeSigned16BitBigEndian(buffer, offset);
				offset += 2;
				break;
			case SIGNED_16_BIT_LITTLE_ENDIAN:
				sample = decodeSigned16BitLittleEndian(buffer, offset);
				offset += 2;
				break;
			case UNSIGNED_16_BIT_BIG_ENDIAN:
				sample = decodeUnsigned16BitBigEndian(buffer, offset);
				offset += 2;
				break;
			case UNSIGNED_16_BIT_LITTLE_ENDIAN:
				sample = decodeUnsigned16BitLittleEndian(buffer, offset);
				offset += 2;
				break;
			default:
				throw new AssertionError();
			}

			if (i == 0) {
				left = sample;
			} else if (i == 1) {
				right = sample;
			}
		}

		samples[0] = left;
		samples[1] = right;
	}

	private static float decodeSigned8Bit(final byte @NonNull [] buffer,
			final int offset) {
		final int sampleInt = buffer[offset];
		final float sample = (float) sampleInt / (float) 0x7f;
		return sample;
	}

	private static float decodeUnsigned8Bit(final byte @NonNull [] buffer,
			final int offset) {
		final int sampleInt = (buffer[offset] & 0xff) - 0x80;
		final float sample = (float) sampleInt / (float) 0x7f;
		return sample;
	}

	private static float decodeSigned16BitBigEndian(
			final byte @NonNull [] buffer, final int offset) {
		// Big-Endian: Byte mit höchstwertigsten Bits wird zuerst gespeichert
		final byte lower, higher;
		higher = buffer[offset];
		lower = buffer[offset + 1];
		final int sampleInt = higher << 8 | lower & 0xff;
		final float sample = (float) sampleInt / (float) 0x7fff;
		return sample;
	}

	private static float decodeSigned16BitLittleEndian(
			final byte @NonNull [] buffer, final int offset) {
		// Little-Endian: Byte mit niederstwertigen Bits wird zuerst gespeichert
		final byte lower, higher;
		higher = buffer[offset + 1];
		lower = buffer[offset];
		final int sampleInt = higher << 8 | lower & 0xff;
		final float sample = (float) sampleInt / (float) 0x7fff;
		return sample;
	}

	private static float decodeUnsigned16BitBigEndian(
			final byte @NonNull [] buffer, final int offset) {
		// Big-Endian: Byte mit höchstwertigsten Bits wird zuerst gespeichert
		final byte lower, higher;
		higher = buffer[offset];
		lower = buffer[offset + 1];
		final int sampleInt = ((higher & 0xff) << 8 | lower & 0xff) - 0x8000;
		final float sample = (float) sampleInt / (float) 0x7fff;
		return sample;
	}

	private static float decodeUnsigned16BitLittleEndian(
			final byte @NonNull [] buffer, final int offset) {
		// Little-Endian: Byte mit niederstwertigen Bits wird zuerst gespeichert
		final byte lower, higher;
		higher = buffer[offset + 1];
		lower = buffer[offset];
		final int sampleInt = ((higher & 0xff) << 8 | lower & 0xff) - 0x8000;
		final float sample = (float) sampleInt / (float) 0x7fff;
		return sample;
	}

	public void encodeMono(final float sample, final byte @NonNull [] buffer,
			final int offset) {
		if (Float.isNaN(sample)) {
			throw new IllegalArgumentException("sample is NaN");
		} else if (sample < -1.0 || sample > 1.0) {
			throw new IllegalArgumentException("sample " + sample
					+ " is out of range -1..1");
		}

		switch (format) {
		case SIGNED_8_BIT:
			encodeSigned8Bit(sample, buffer, offset);
			break;
		case UNSIGNED_8_BIT:
			encodeUnsigned8Bit(sample, buffer, offset);
			break;
		case SIGNED_16_BIT_BIG_ENDIAN:
			encodeSigned16BitBigEndian(sample, buffer, offset);
			break;
		case SIGNED_16_BIT_LITTLE_ENDIAN:
			encodeSigned16BitLittleEndian(sample, buffer, offset);
			break;
		case UNSIGNED_16_BIT_BIG_ENDIAN:
			encodeUnsigned16BitBigEndian(sample, buffer, offset);
			break;
		case UNSIGNED_16_BIT_LITTLE_ENDIAN:
			encodeUnsigned16BitLittleEndian(sample, buffer, offset);
			break;
		default:
			throw new AssertionError();
		}
	}

	private static void encodeSigned8Bit(final float sample,
			final byte @NonNull [] buffer, final int offset) {
		final int encoded = (int) (sample * 0x7f);
		final byte clamped = (byte) Util.clamp(encoded, -128, 127);
		buffer[offset] = clamped;
	}

	private static void encodeUnsigned8Bit(final float sample,
			final byte @NonNull [] buffer, final int offset) {
		final int encoded = (int) (sample * 0x7f) + 0x80;
		final int clamped = (int) Util.clamp(encoded, 0, 255);
		buffer[offset] = (byte) clamped;
	}

	private static void encodeSigned16BitBigEndian(final float sample,
			final byte @NonNull [] buffer, final int offset) {
		final int encoded = (int) (sample * 0x7fff);
		final int clamped = (int) Util.clamp(encoded, -32768, 32767);
		final byte higher = (byte) ((clamped & 0xff00) >> 8);
		final byte lower = (byte) (clamped & 0xff);
		buffer[offset] = higher;
		buffer[offset + 1] = lower;
	}

	private static void encodeSigned16BitLittleEndian(final float sample,
			final byte @NonNull [] buffer, final int offset) {
		final int encoded = (int) (sample * 0x7fff);
		final int clamped = (int) Util.clamp(encoded, -32768, 32767);
		final byte higher = (byte) ((clamped & 0xff00) >> 8);
		final byte lower = (byte) (clamped & 0xff);
		buffer[offset] = lower;
		buffer[offset + 1] = higher;
	}

	private void encodeUnsigned16BitBigEndian(final float sample,
			final byte @NonNull [] buffer, final int offset) {
		final int encoded = (int) (sample * 0x7fff) + 0x8000;
		final int clamped = (int) Util.clamp(encoded, 0, 65535);
		final byte higher = (byte) ((clamped & 0xff00) >> 8);
		final byte lower = (byte) (clamped & 0xff);
		buffer[offset] = higher;
		buffer[offset + 1] = lower;
	}

	private static void encodeUnsigned16BitLittleEndian(final float sample,
			final byte @NonNull [] buffer, final int offset) {
		final int encoded = (int) (sample * 0x7fff) + 0x8000;
		final int clamped = (int) Util.clamp(encoded, 0, 65535);
		final byte higher = (byte) ((clamped & 0xff00) >> 8);
		final byte lower = (byte) (clamped & 0xff);
		buffer[offset] = lower;
		buffer[offset + 1] = higher;
	}

	@Override
	@NonNull
	public String toString() {
		return "Codec(format:" + format + ")";
	}

	public static boolean isAudioFormatSupported(
			final @NonNull AudioFormat format) {
		final Encoding encoding = format.getEncoding();
		final int numChannels = format.getChannels();
		final int sampleSizeBits = format.getSampleSizeInBits();

		final boolean encodingSupported = (encoding == Encoding.PCM_SIGNED || encoding == Encoding.PCM_UNSIGNED);
		final boolean channelsSupported = (numChannels == AudioSystem.NOT_SPECIFIED
				|| numChannels == 1 || numChannels == 2);
		final boolean sampleSizeSupported = (sampleSizeBits == AudioSystem.NOT_SPECIFIED
				|| sampleSizeBits == 8 || sampleSizeBits == 16);

		//@formatter:off
		return encodingSupported && channelsSupported && sampleSizeSupported;
		
	}
	
	@NonNull
	public static PcmCodec fromAudioFormat(final @NonNull AudioFormat format) throws UnsuportedFormatException {
		if (!isAudioFormatSupported(format)) {
			throw new UnsuportedFormatException();
		}
		
		final Format internalFormat = toInternalFormat(format);
		final int numChannels = format.getChannels();
		return new PcmCodec(internalFormat, numChannels);
	}
	
	@NonNull
	private static Format toInternalFormat(final @NonNull AudioFormat audioFormat) {
		final Format internalFormat;

		if (audioFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
			switch (audioFormat.getSampleSizeInBits()) {
			case 8:
				internalFormat = Format.SIGNED_8_BIT;
				break;
			case 16:
			case AudioSystem.NOT_SPECIFIED:
				if (audioFormat.isBigEndian()) {
					internalFormat = Format.SIGNED_16_BIT_BIG_ENDIAN;
				} else {
					internalFormat = Format.SIGNED_16_BIT_LITTLE_ENDIAN;
				}
				break;
			default:
				throw new AssertionError(audioFormat.getSampleSizeInBits()
						+ " Bit not supported");
			}
		} else if (audioFormat.getEncoding().equals(
				AudioFormat.Encoding.PCM_UNSIGNED)) {
			switch (audioFormat.getSampleSizeInBits()) {
			case 8:
				internalFormat = Format.UNSIGNED_8_BIT;
				break;
			case 16:
			case AudioSystem.NOT_SPECIFIED:
				if (audioFormat.isBigEndian()) {
					internalFormat = Format.UNSIGNED_16_BIT_BIG_ENDIAN;
				} else {
					internalFormat = Format.UNSIGNED_16_BIT_LITTLE_ENDIAN;
				}
				break;
			default:
				throw new AssertionError(audioFormat.getSampleSizeInBits()
						+ " Bit not supported");
			}
		} else {
			throw new AssertionError("Neither PCM_SIGNED nor PCM_UNSIGNED");
		}

		return internalFormat;
	}
}
