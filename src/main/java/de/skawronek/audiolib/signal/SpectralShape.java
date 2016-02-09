package de.skawronek.audiolib.signal;

import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.Feature;
import de.skawronek.audiolib.FeatureFactory;
import de.skawronek.audiolib.FeatureKey;
import de.skawronek.audiolib.FeatureProcessor;
import de.skawronek.audiolib.Frame;
import de.skawronek.audiolib.math.Statistics;
import de.skawronek.audiolib.util.Util;

public final class SpectralShape extends Feature {
	private static enum SingleFeature {
		SUM, SKEWNESS, ROLLOFF_85, ROLLOFF_95, CENTROID, SPREAD, DECREASE, FLATNESS
	}

	private final EnumSet<SingleFeature> computed = EnumSet
			.noneOf(SingleFeature.class);
	private final FastFourierTransform fft;
	private final Statistics stats = new Statistics();
	private float sum;
	private float skewness;
	private float rolloff_85;
	private float rolloff_95;
	private float centroid;
	private float spread;
	private float decrease;
	private float flatness;

	public final static class Processor extends FeatureProcessor<SpectralShape> {
		@Override
		public @NonNull Class<SpectralShape> getFeatureClass() {
			return SpectralShape.class;
		}

		@Override
		public @NonNull SpectralShape process(@NonNull final Frame frame,
				@NonNull final FeatureKey<SpectralShape> key) {
			FastFourierTransform fft = frame.getFeature(FastFourierTransform
					.getKey());
			return new SpectralShape(fft);
		}
	}

	public SpectralShape(final @NonNull FastFourierTransform fft) {
		this.fft = fft;
		this.stats.setInput(fft.getMagnitudeSpectrum());
	}

	private float getSum() {
		if (isComputed(SingleFeature.SUM)) {
			return sum;
		}

		sum = stats.getSum();
		setComputed(SingleFeature.SUM);

		return sum;
	}

	public float getSkewness() {
		if (isComputed(SingleFeature.SKEWNESS)) {
			return skewness;
		}

		final int numBins = fft.getSize() / 2;
		final float[] spectrum = fft.getMagnitudeSpectrum();
		final float mean = stats.getAverage();
		final float stdDev = stats.getStandardDeviation();
		float numerator = 0;
		for (int k = 0; k < numBins / 2; k++) {
			final float diff = spectrum[k] - mean;
			numerator += (diff * diff * diff);
		}
		numerator *= 2;
		final float denominator = numBins * (stdDev * stdDev * stdDev);
		skewness = numerator / denominator;
		setComputed(SingleFeature.SKEWNESS);

		return skewness;
	}

	public float getRolloff85() {
		if (isComputed(SingleFeature.ROLLOFF_85)) {
			return rolloff_85;
		}

		rolloff_85 = getRolloff(0.85f);
		setComputed(SingleFeature.ROLLOFF_85);

		return rolloff_85;
	}

	public float getRolloff95() {
		if (isComputed(SingleFeature.ROLLOFF_95)) {
			return rolloff_95;
		}

		rolloff_85 = getRolloff(0.95f);
		setComputed(SingleFeature.ROLLOFF_95);

		return rolloff_85;
	}

	private float getRolloff(final float cutoff) {
		assert 0 <= cutoff && cutoff <= 1;

		final float sum = getSum();
		final float threshold = sum * cutoff;
		final int numBins = fft.getSize() / 2;
		final float[] spectrum = fft.getMagnitudeSpectrum();
		float currSum = 0;
		int rolloffPoint = -1;
		for (int k = 0; k < numBins; k++) {
			currSum += spectrum[k];
			if (currSum >= threshold) {
				rolloffPoint = k;
				break;
			}
		}
		assert rolloffPoint >= 0;
		final float rolloff = (float) rolloffPoint / (float) numBins;
		assert 0 <= rolloff && rolloff <= 1;
		return rolloff;
	}

	public float getCentroid() {
		if (isComputed(SingleFeature.CENTROID)) {
			return centroid;
		}

		final int numBins = fft.getSize() / 2;
		final float[] powerSpec = fft.getPowerSpectrum();
		float numerator = 0;
		float denominator = 0;
		for (int k = 0; k < numBins; k++) {
			numerator += k * powerSpec[k];
			denominator += powerSpec[k];
		}
		centroid = numerator / denominator;
		setComputed(SingleFeature.CENTROID);

		assert 0 <= centroid && centroid <= numBins;
		return centroid;
	}

	public float getSpread() {
		if (isComputed(SingleFeature.SPREAD)) {
			return spread;
		}

		final int numBins = fft.getSize() / 2;
		final float[] powerSpec = fft.getPowerSpectrum();
		final int centroidBin = Util.clamp(Math.round(getCentroid() * numBins),
				0, numBins - 1);
		float numerator = 0;
		float denominator = 0;
		for (int k = 0; k < numBins; k++) {
			final int binDiff = k - centroidBin;
			numerator += (binDiff * binDiff + powerSpec[k]);
			denominator += powerSpec[k];
		}
		spread = (float) Math.sqrt(numerator / denominator);
		setComputed(SingleFeature.SPREAD);
		return spread;
	}

	public float getDecrease() {
		if (isComputed(SingleFeature.DECREASE)) {
			return decrease;
		}

		final int numBins = fft.getSize() / 2;
		final float[] spectrum = fft.getMagnitudeSpectrum();
		final float first = spectrum[0];
		float numerator = 0;
		float denominator = 0;
		for (int k = 1; k < numBins; k++) {
			numerator += ((1f / k) * (spectrum[k] - first));
			denominator += spectrum[k];
		}

		decrease = numerator / denominator;
		setComputed(SingleFeature.DECREASE);

		assert decrease <= 1;
		return decrease;
	}

	public float getFlatness() {
		if (isComputed(SingleFeature.FLATNESS)) {
			return flatness;
		}

		final int numBins = fft.getSize() / 2;
		final float[] powerSpec = fft.getPowerSpectrum();
		float geometricMean = 1;
		float arithmeticMean = 0;
		for (int k = 0; k < numBins; k++) {
			geometricMean *= powerSpec[k];
			arithmeticMean += powerSpec[k];
		}
		geometricMean = (float) Math.pow(geometricMean, 1d / numBins);
		arithmeticMean /= numBins;
		flatness = geometricMean / arithmeticMean;

		setComputed(SingleFeature.FLATNESS);
		return flatness;
	}

	private boolean isComputed(final @NonNull SingleFeature f) {
		return computed.contains(f);
	}

	private void setComputed(final @NonNull SingleFeature f) {
		computed.add(f);
	}

	public final static class Key extends FeatureKey<SpectralShape> {
		@Override
		public @NonNull Class<SpectralShape> getFeatureClass() {
			return SpectralShape.class;
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
