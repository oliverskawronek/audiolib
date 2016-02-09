package de.skawronek.audiolib.signal;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.Feature;
import de.skawronek.audiolib.FeatureFactory;
import de.skawronek.audiolib.FeatureKey;
import de.skawronek.audiolib.FeatureProcessor;
import de.skawronek.audiolib.Frame;

public final class FastFourierTransform extends Feature {
	private final float[] real;
	private final float[] imaginary;
	private float[] spectrum;
	private float[] power;
	private float[] phase;

	public final static class Processor extends
			FeatureProcessor<FastFourierTransform> {
		@Override
		public @NonNull Class<FastFourierTransform> getFeatureClass() {
			return FastFourierTransform.class;
		}

		@Override
		public FastFourierTransform process(@NonNull final Frame frame,
				@NonNull final FeatureKey<FastFourierTransform> key) {
			final float[] window = frame.getMonoSamples();
			//@formatter:off
			final de.skawronek.audiolib.math.FastFourierTransform fft
					= de.skawronek.audiolib.math.FastFourierTransform.Factory.getInstance().get(window.length);
			//@formatter:on
			fft.forward(window);
			final float[] real = new float[window.length];
			final float[] imaginary = new float[window.length];
			fft.copyReal(real);
			fft.copyImaginary(imaginary);

			return new FastFourierTransform(real, imaginary);
		}
	}

	public FastFourierTransform(final float @NonNull [] real,
			final float @NonNull [] imaginary) {
		this.real = real;
		this.imaginary = imaginary;
	}
	
	public int getSize() {
		return real.length;
	}

	public float @NonNull [] getReal() {
		return real;
	}

	public float @NonNull [] getImaginary() {
		return imaginary;
	}

	public float @NonNull [] getMagnitudeSpectrum() {
		if (spectrum == null) {
			spectrum = new float[real.length];
			de.skawronek.audiolib.math.FastFourierTransform
					.computeMagnitudeSpectrum(real, imaginary, spectrum, false);
		}

		return spectrum;
	}

	public float @NonNull [] getPowerSpectrum() {
		if (power == null) {
			power = new float[real.length];
			de.skawronek.audiolib.math.FastFourierTransform
					.computePowerSpectrum(real, imaginary, power, false);
		}

		return power;
	}

	public float @NonNull [] getPhaseSpectrum() {
		if (phase == null) {
			phase = new float[real.length];
			de.skawronek.audiolib.math.FastFourierTransform
					.computePhaseSpectrum(real, imaginary, phase, false);
		}

		return phase;
	}

	public final static class Key extends FeatureKey<FastFourierTransform> {
		@Override
		public @NonNull Class<FastFourierTransform> getFeatureClass() {
			return FastFourierTransform.class;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null) {
				return false;
			} else if (obj == this) {
				return true;
			} else if (obj instanceof Key) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return 0;
		}
	}

	private static final Key DEFAULT_KEY = new Key();

	public static Key getKey() {
		return DEFAULT_KEY;
	}

	public static void register() {
		final Processor processor = new Processor();
		FeatureFactory.getInstance().registerProcessor(processor);
	}
}
