package de.skawronek.audiolib.math;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.music.EqualTemperament;

public final class ChromaVector {
	private final int numOctaves;
	private final int numHarmonics;
	private final int numSearchBins;
	private final float[] chroma;

	public ChromaVector(final int numOctaves, final int numHarmonics,
			final int numSearchBins) {
		this.numOctaves = numOctaves;
		this.numHarmonics = numHarmonics;
		this.numSearchBins = numSearchBins;
		this.chroma = new float[12];
	}

	/**
	 * 
	 * @param spectrum
	 *            Betragsspektrum von FFT
	 * @param minFreq
	 * @param bandWidth
	 *            Samplerate / Framesize
	 */
	public void compute(float @NonNull [] spectrum, final double minFreq,
			final double bandWidth) {
		for (int i = 0; i < 12; i++) {
			chroma[i] = 0f;
			for (int phi = 1; phi <= numOctaves; phi++) {
				for (int h = 1; h <= numHarmonics; h++) {
					final double f = EqualTemperament.transpose(minFreq,
							100 * i);
					final int km = (int) Math.round(f * phi * h / bandWidth);
					final int k0 = km - numSearchBins * h;
					final int k1 = km + numSearchBins * h;

					float max = -Float.MAX_VALUE;
					for (int k = k0; k <= k1; k++) {
						if (spectrum[k] > max) {
							max = spectrum[k];
						}
					}

					chroma[i] += (max / h);
				}
			}
		}
	}

	public void copyChroma(final float @NonNull [] buffer) {
		checkChromaLength(buffer, "buffer");
		System.arraycopy(chroma, 0, buffer, 0, 12);
	}

	public static void normalizeToVectorLength(final float @NonNull [] chroma) {
		checkChromaLength(chroma, "chroma");

		float sum = 0f;
		for (int i = 0; i < 12; i++) {
			sum += chroma[i];
		}
		for (int i = 0; i < 12; i++) {
			chroma[i] /= sum;
		}
	}

	public static void normalizeToSum(final float @NonNull [] chroma) {
		checkChromaLength(chroma, "chroma");

		float sum = 0f;
		for (int i = 0; i < 12; i++) {
			sum += (chroma[i] * chroma[i]);
		}
		final float scale = (float) (1 / Math.sqrt(sum));
		for (int i = 0; i < 12; i++) {
			chroma[i] *= scale;
		}
	}

	private static void checkChromaLength(final float @NonNull [] buffer,
			final @NonNull String name) {
		if (buffer.length != 12) {
			throw new IllegalArgumentException(name + " length "
					+ buffer.length + " != 12");
		}
	}
}
