package de.skawronek.audiolib.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

import de.skawronek.audiolib.TestUtil;
import de.skawronek.audiolib.math.FastConstantQTransform;
import de.skawronek.audiolib.math.WindowFunctions;
import de.skawronek.audiolib.math.WindowFunctions.IWindowFunction;
import de.skawronek.audiolib.util.Util;

public final class FastConstantQTransformTest {
	// klein gewählt
	private static final float COMPARISON_DELTA = 0.015f;

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorChecksForZeroSampleRate() {
		new FastConstantQTransform(65.4064, 523.251, 12, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorChecksForNegativeSampleRate() {
		new FastConstantQTransform(65.4064, 523.251, 12, -1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorChecksMinFreqIsLowerThanMaxFreq() {
		final double minFreq = 523.251;
		final double maxFreq = 65.4064;
		new FastConstantQTransform(minFreq, maxFreq, 12, 44100);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorChecksForZeroMinFreq() {
		new FastConstantQTransform(0, 523.251, 12, 44100);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorChecksForNegativeMinFreq() {
		new FastConstantQTransform(-1, 523.251, 12, 44100);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorChecksForZeroBinCount() {
		new FastConstantQTransform(65.4064, 523.251, 0, 44100);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorChecksForNegativeZeroBinCount() {
		new FastConstantQTransform(65.4064, 523.251, -1, 44100);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testForwardRealChecksMaxSize() {
		final int size = 1000;
		final int maxSize = Util.getNextPowerOfTwo(size);
		final FastConstantQTransform cqt = FastConstantQTransform
				.createFullSpectrum(size, 12, 44100);
		final float[] x = new float[maxSize + 1];
		cqt.forward(x);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testForwardComplexChecksForSameSize() {
		final FastConstantQTransform cqt = FastConstantQTransform
				.createFullSpectrum(1024, 12, 44100);
		final float[] xReal = new float[1024];
		final float[] xImaginary = new float[1024 + 1];
		cqt.forward(xReal, xImaginary);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIndexToFrequencyChecksForNegativeIndex() {
		final FastConstantQTransform cqt = FastConstantQTransform
				.createFullSpectrum(2048, 12, 44100);
		cqt.binToFrequency(-1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIndexToFrequencyChecksForMaximumIndex() {
		final FastConstantQTransform cqt = FastConstantQTransform
				.createFullSpectrum(2048, 12, 44100);
		final int maxIndex = cqt.getSpectrumSize() - 1; // inkl.
		cqt.binToFrequency(maxIndex + 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFrequencyToIndexChecksForNegativeFrequency() {
		final FastConstantQTransform cqt = FastConstantQTransform
				.createFullSpectrum(2048, 12, 44100);
		cqt.frequencyToBin(-1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFrequencyToIndexChecksForMaximumFrequency() {
		final FastConstantQTransform cqt = FastConstantQTransform
				.createFullSpectrum(2048, 12, 44100);
		// Shannon Theorem max freq = sample rate / 2
		final double maxFreq = cqt.getSampleRate() / 2;
		cqt.frequencyToBin(maxFreq + 1);
	}

	@Test
	public void testFrequencyToIndex_IsInverseTo_IndexToFrequency() {
		final FastConstantQTransform cqt = FastConstantQTransform
				.createFullSpectrum(2048, 12, 44100);
		final int maxIndex = cqt.getSpectrumSize() - 1; // inkl.
		for (int i = 0; i <= maxIndex; i++) {
			final int expectedIndex = i;
			final double freq = cqt.binToFrequency(i);
			final int actualIndex = cqt.frequencyToBin(freq);
			assertEquals(expectedIndex, actualIndex);
		}
	}

	@Test
	public void testIndexToFrequencyIsStrictlyMonotonousIncreasing() {
		final FastConstantQTransform cqt = FastConstantQTransform
				.createFullSpectrum(2048, 12, 44100);
		final int maxIndex = cqt.getSpectrumSize() - 1; // inkl.
		for (int i = 1; i <= maxIndex; i++) {
			final double freqBefore = cqt.binToFrequency(i - 1);
			final double freq = cqt.binToFrequency(i);
			assertTrue(freqBefore < freq);
		}
	}

	@Test
	public void testAgainstNonFastForwardTransform() {
		// Testet, ob die selben Ergebnisse bei der CQT herauskommen wie bei der
		// naiven Implementierung mit zwei For-Schleifen (Komplexität O(n²)).

		final Random random = new Random(12345);

		final int numSamples = 100;
		for (int j = 0; j < numSamples; j++) {
			testSingleAgainstNonFastForwardTransform(random);
		}
	}

	private void testSingleAgainstNonFastForwardTransform(final Random random) {
		final int size = 1024;
		final double sampleRate = 22050;
		final int binsPerOctave = 12;
		final double q = 1 / (Math.pow(2, 1d / binsPerOctave) - 1);
		final double minFreq = (q * sampleRate) / size;
		final double maxFreq = sampleRate / 2;

		// Generiere Zuffals-Signal
		final int numSines = 5;
		final float minAmplitude = 0.1f;
		final float maxAmplitude = 0.3f;
		final float[] x = TestUtil.generateRandomSound(random, size, numSines,
				minAmplitude, maxAmplitude);

		// Naive Implementierung
		final IWindowFunction w = WindowFunctions.getHammingWindow();
		final int numBins = (int) Math.ceil(binsPerOctave
				* Util.log2(maxFreq / minFreq));
		final float[] expectedMag = new float[numBins];

		for (int k = 0; k < numBins; k++) {
			final double freq = minFreq
					* Math.pow(2, (double) k / (double) binsPerOctave);
			final int n = (int) Math.ceil(q * sampleRate / freq);
			float re = 0f;
			float im = 0f;
			for (int i = 0; i < n; i++) {
				final double t = (1d / n) * i - 0.5;
				final double exponent = -2 * Math.PI * q * i / n;
				re += w.evaluate(t) / n * x[i] * Math.cos(exponent);
				im += w.evaluate(t) / n * x[i] * Math.sin(exponent);
			}
			expectedMag[k] = (float) Math.sqrt(re * re + im * im);
		}

		// Fast Implementierung
		final FastConstantQTransform fcqt = new FastConstantQTransform(minFreq,
				maxFreq, binsPerOctave, sampleRate);
		fcqt.forward(x);
		final float[] actualMag = new float[fcqt.getSpectrumSize()];
		fcqt.fillMagnitudeSpectrum(actualMag);

		// Teste über RMSE, ob naive Implementierung mit der
		// Fast-Implementierung übereinstimmt
		final float magRMSE = TestUtil.computeRMSE(expectedMag, actualMag);
		assertEquals(0.0, magRMSE, COMPARISON_DELTA);
	}
}
