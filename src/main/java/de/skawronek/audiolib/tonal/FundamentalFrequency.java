package de.skawronek.audiolib.tonal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.Feature;
import de.skawronek.audiolib.FeatureFactory;
import de.skawronek.audiolib.FeatureKey;
import de.skawronek.audiolib.FeatureProcessor;
import de.skawronek.audiolib.Frame;
import de.skawronek.audiolib.math.AdaptivePeakPicking;
import de.skawronek.audiolib.math.FastConstantQTransform;
import de.skawronek.audiolib.math.FastFourierTransform;
import de.skawronek.audiolib.signal.Autocorrelation;
import de.skawronek.audiolib.signal.ConstantQTransform;
import de.skawronek.audiolib.signal.HarmonicProductSpectrum;
import de.skawronek.audiolib.signal.ZeroCrossingRate;

public final class FundamentalFrequency extends Feature {
	private final double frequency;

	public static enum Method {
		AUTOCORRELATION, HARMONIC_PRODUCT_SPECTRUM, ZERO_CROSSINGS, CONSTANT_Q_TRANSFORM
	}

	public final static class Processor extends
			FeatureProcessor<FundamentalFrequency> {
		@Override
		public @NonNull Class<FundamentalFrequency> getFeatureClass() {
			return FundamentalFrequency.class;
		}

		@Override
		public FundamentalFrequency process(final @NonNull Frame frame,
				@NonNull final FeatureKey<FundamentalFrequency> featureKey) {
			final double frequency;
			final Key key = (Key) featureKey;
			switch (key.method) {
			case AUTOCORRELATION:
				frequency = estimateByAutocorrelation(frame, key);
				break;
			case HARMONIC_PRODUCT_SPECTRUM:
				frequency = estimateByHarmonicProductSpectrum(frame, key);
				break;
			case ZERO_CROSSINGS:
				frequency = estimateByZeroCrossings(frame);
				break;
			case CONSTANT_Q_TRANSFORM:
				frequency = estimateByConstantQTransform(frame, key);
				break;
			default:
				throw new AssertionError();
			}
			return new FundamentalFrequency(frequency);
		}

		private double estimateByAutocorrelation(final @NonNull Frame frame,
				final @NonNull Key key) {
			final Autocorrelation autocorrelation = frame
					.getFeature(Autocorrelation.getDefaultKey());
			final float[] acf = autocorrelation.getCoefficients();
			final int startLag = (int) Math.ceil(frame.getSampleRate()
					/ key.maxFreq);
			// Finde Verschiebung/Lag mit der größten Ähnlichkeit
			float maxValue = -Float.MAX_VALUE;
			int maxLag = -1;
			for (int lag = startLag; lag < acf.length; lag++) {
				if (acf[lag] > maxValue) {
					maxValue = acf[lag];
					maxLag = lag;
				}
			}
			return frame.getSampleRate() / maxLag; // Hz
		}

		private double estimateByZeroCrossings(final @NonNull Frame frame) {
			final ZeroCrossingRate zcr = frame.getFeature(ZeroCrossingRate
					.getKey());
			return zcr.getFrequency();
		}

		private double estimateByHarmonicProductSpectrum(
				final @NonNull Frame frame, final @NonNull Key key) {
			final HarmonicProductSpectrum harmonicProductSpectrum = frame
					.getFeature(HarmonicProductSpectrum.getKey(key.numHarmonics));
			final float[] hps = harmonicProductSpectrum.getHps();

			final AdaptivePeakPicking picking = new AdaptivePeakPicking(11, 0f,
					1.5f);
			picking.compute(hps);
			final int startBin = Math.max(
					0,
					FastFourierTransform.frequencyToBin(key.minFreq,
							frame.getSize(), frame.getSampleRate())); // inkl.
			final int endBin = Math.min(
					frame.getSize() / 2,
					FastFourierTransform.frequencyToBin(key.maxFreq,
							frame.getSize(), frame.getSampleRate())); // exkl.
			final List<Integer> peaks = picking.getPeaks();

			float maxValue = -Float.MAX_VALUE;
			int maxPeak = -1;
			for (final int idx : peaks) {
				final boolean inBounds = (idx >= startBin && idx < endBin);
				if (inBounds && hps[idx] > maxValue) {
					maxValue = hps[idx];
					maxPeak = idx;
				}
			}
			final boolean peakInBounds = (maxPeak != -1);
			if (!peakInBounds) {
				// Finde Maximum in den Grenzen
				for (int k = startBin; k < endBin; k++) {
					if (hps[k] > maxValue) {
						maxValue = hps[k];
						maxPeak = k;
					}
				}
			}
			return FastFourierTransform.binToFrequency(maxPeak,
					frame.getSize(), frame.getSampleRate());
		}

		private double estimateByConstantQTransform(final @NonNull Frame frame,
				final @NonNull Key key) {
			final int binPerOctave = 12;
			ConstantQTransform cqt = frame.getFeature(ConstantQTransform
					.getKey(key.minFreq, key.maxFreq, binPerOctave));
			final float[] spectrum = cqt.getMagnitudeSpectrum();
			float maxValue = -Float.MAX_VALUE;
			int maxBin = -1;
			for (int k = 0; k < spectrum.length; k++) {
				if (spectrum[k] > maxValue) {
					maxValue = spectrum[k];
					maxBin = k;
				}
			}
			return FastConstantQTransform.binToFrequency(maxBin, key.minFreq,
					binPerOctave);
		}
	}

	private FundamentalFrequency(final double frequency) {
		this.frequency = frequency;
	}

	public double getFrequency() {
		return frequency;
	}

	public final static class Key extends FeatureKey<FundamentalFrequency> {
		private final Method method;
		private final double minFreq;
		private final double maxFreq;
		private final int numHarmonics; // Für Harmonic Product Spectrum

		private Key(final @NonNull Method method, final double minFreq,
				final double maxFreq) {
			this(method, minFreq, maxFreq, 1);
		}

		private Key(final @NonNull Method method, final double minFreq,
				final double maxFreq, final int numHarmonics) {
			this.method = method;
			this.minFreq = minFreq;
			this.maxFreq = maxFreq;
			this.numHarmonics = numHarmonics;
		}

		@Override
		public @NonNull Class<FundamentalFrequency> getFeatureClass() {
			return FundamentalFrequency.class;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null) {
				return false;
			} else if (obj == this) {
				return true;
			} else if (obj instanceof Key) {
				final Key other = (Key) obj;
				if (this.method == other.method) {
					switch (method) {
					case AUTOCORRELATION:
						return Double.compare(this.maxFreq, other.maxFreq) == 0;
					case HARMONIC_PRODUCT_SPECTRUM:
						return Double.compare(this.minFreq, other.minFreq) == 0
								&& Double.compare(this.maxFreq, other.maxFreq) == 0
								&& this.numHarmonics == other.numHarmonics;
					case CONSTANT_Q_TRANSFORM:
						return Double.compare(this.minFreq, other.minFreq) == 0
								&& Double.compare(this.maxFreq, other.maxFreq) == 0;
					case ZERO_CROSSINGS:
						return true;
					default:
						throw new AssertionError();
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			int hashCode = method.hashCode();

			switch (method) {
			case AUTOCORRELATION:
				hashCode ^= Double.hashCode(maxFreq);
				break;
			case HARMONIC_PRODUCT_SPECTRUM:
			case CONSTANT_Q_TRANSFORM:
				hashCode ^= Double.hashCode(minFreq);
				hashCode ^= Double.hashCode(maxFreq);
				break;
			case ZERO_CROSSINGS:
				break;
			default:
				throw new AssertionError();
			}

			return hashCode;
		}
	}

	private static final double C2_FREQ = 65.4064; // Hz
	private static final double C7_FREQ = 2093.00; // Hz
	private static final Key ACF_KEY = new Key(Method.AUTOCORRELATION, C2_FREQ,
			C7_FREQ);
	private static final Key HPS_KEY = new Key(
			Method.HARMONIC_PRODUCT_SPECTRUM, C2_FREQ, C7_FREQ);
	private static final Key ZCR_KEY = new Key(Method.ZERO_CROSSINGS, C2_FREQ,
			C7_FREQ);
	private static final Key CQT_KEY = new Key(Method.CONSTANT_Q_TRANSFORM,
			C2_FREQ, C7_FREQ);

	public static Key getKey(final @NonNull Method method) {
		switch (method) {
		case AUTOCORRELATION:
			return ACF_KEY;
		case HARMONIC_PRODUCT_SPECTRUM:
			return HPS_KEY;
		case ZERO_CROSSINGS:
			return ZCR_KEY;
		case CONSTANT_Q_TRANSFORM:
			return CQT_KEY;
		default:
			throw new AssertionError();
		}
	}

	public static Key getAutocorrelationKey(final double maxFreq) {
		return new Key(Method.AUTOCORRELATION, 0.0, maxFreq);
	}

	public static Key getHarmonicProductSpectrumKey(final double minFreq,
			final double maxFreq, int numHarmonics) {
		if (numHarmonics < 1) {
			throw new IllegalArgumentException("numHarmonics " + numHarmonics
					+ " < 1");
		}
		return new Key(Method.HARMONIC_PRODUCT_SPECTRUM, minFreq, maxFreq, numHarmonics);
	}

	public static Key getZeroCrossingsKey() {
		return ZCR_KEY;
	}

	public static Key getConstantQTransformKey(final double minFreq,
			final double maxFreq) {
		return new Key(Method.CONSTANT_Q_TRANSFORM, minFreq, maxFreq);
	}

	public static void register() {
		final Processor processor = new Processor();
		FeatureFactory.getInstance().registerProcessor(processor);
	}
}
