package de.skawronek.audiolib.math;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.util.AbstractFactory;

public final class HarmonicProductSpectrum {
	private static final class Index {
		private final int size;
		private final int numHarmonics;

		Index(int size, int numHarmonics) {
			this.size = size;
			this.numHarmonics = numHarmonics;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null) {
				return false;
			} else if (obj == this) {
				return true;
			} else if (obj instanceof Index) {
				final Index other = (Index) obj;
				return this.size == other.size
						&& this.numHarmonics == other.numHarmonics;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return size << 16 | numHarmonics;
		}
	}

	public static final class Factory extends
			AbstractFactory<Index, HarmonicProductSpectrum> {
		private static final Factory INSTANCE = new Factory();

		private Factory() {
		}

		public static Factory getInstance() {
			return INSTANCE;
		}

		public @NonNull HarmonicProductSpectrum get(final int size,
				final int numHarmonics) {
			final Index index = new Index(size, numHarmonics);
			return super.get(index);
		}

		@Override
		protected @NonNull HarmonicProductSpectrum create(@NonNull Index index) {
			return new HarmonicProductSpectrum(index.size, index.numHarmonics);
		}
	}

	private final int size;
	private final int numHarmonics;
	// Für die Bins 0..maxHpsBin kann das HPS berechnet werden.
	private final int maxHpsBin; // inkl.
	private final float[] hps;

	HarmonicProductSpectrum(final int size, final int numHarmonics) {
		if (size <= 0) {
			throw new IllegalArgumentException("size " + size + " <= 0");
		} else if (numHarmonics < 1) {
			throw new IllegalArgumentException("numHarmonics " + numHarmonics
					+ " < 1");
		}

		this.size = size;
		this.numHarmonics = numHarmonics;
		this.maxHpsBin = (size - 1) / numHarmonics;
		assert 0 >= maxHpsBin && (maxHpsBin * numHarmonics) < size;
		this.hps = new float[size];
	}

	/**
	 * Gibt die Größe des HPS' zurück. Die Größe entspricht der Anzahl an Bins
	 * des HPS'.
	 * 
	 * @return Größe des HPS' bzw. Anzahl der Bins
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Berechnet das Harmonic Product Spectrum aus dem übergebenen
	 * Betrags-/Magnitudenspektrum.
	 * 
	 * @param spectrum
	 *            Betragsspektrum
	 */
	public void compute(final float @NonNull [] spectrum) {
		if (spectrum.length != size) {
			throw new IllegalArgumentException("spectrum length "
					+ spectrum.length + " != size " + size);
		}

		final int hpsSize = maxHpsBin + 1;
		System.arraycopy(spectrum, 0, hps, 0, hpsSize);
		for (int i = 2; i <= numHarmonics; i++) {
			for (int k = 0; k < hpsSize; k++) {
				// Bin 0 ist der DC-Offset mit 0 Hz (unabhängig von allen
				// Parametern der STFT). Er hat damit keine Harmonischen.
				final int bin = i * k;
				hps[k] *= spectrum[bin];
			}
		}
	}

	public void copyHps(final float @NonNull [] buffer) {
		if (buffer.length < size) {
			throw new IllegalArgumentException("buffer length " + buffer.length
					+ " < size " + size);
		}

		System.arraycopy(hps, 0, buffer, 0, size);
	}
}
