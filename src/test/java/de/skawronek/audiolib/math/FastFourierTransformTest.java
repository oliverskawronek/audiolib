package de.skawronek.audiolib.math;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import de.skawronek.audiolib.TestUtil;
import de.skawronek.audiolib.math.FastFourierTransform;
import de.skawronek.audiolib.util.Util;

public final class FastFourierTransformTest {
	private static final float COMPARISON_DELTA = 0.001f;

	private final Random random = new Random(12345);

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorChecksForPowerOfTwoSize() {
		final int size = 123;
		assert !Util.isPowerOfTwo(size);
		new FastFourierTransform(size);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testForwardChecksForSize() {
		final int size = 1024;
		final int wrongSize = 512;
		final FastFourierTransform fft = new FastFourierTransform(size);
		fft.forward(new float[wrongSize]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testForwarComplexdChecksForSize() {
		final int size = 1024;
		final int wrongSize = 512;
		final FastFourierTransform fft = new FastFourierTransform(size);
		fft.forward(new float[wrongSize], new float[wrongSize]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBackwardChecksForSize() {
		final int size = 1024;
		final int wrongSize = 512;
		final FastFourierTransform ifft = new FastFourierTransform(size);
		ifft.backward(new float[wrongSize]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBackwardComplexChecksForSize() {
		final int size = 1024;
		final int wrongSize = 512;
		final FastFourierTransform ifft = new FastFourierTransform(size);
		ifft.backward(new float[wrongSize], new float[wrongSize]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCopyRealChecksForSize() {
		final int size = 1024;
		final int wrongSize = 512;
		final FastFourierTransform fft = new FastFourierTransform(size);
		fft.forward(new float[size]);
		fft.copyReal(new float[wrongSize]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCopyImaginaryChecksForSize() {
		final int size = 1024;
		final int wrongSize = 512;
		final FastFourierTransform fft = new FastFourierTransform(size);
		fft.forward(new float[size]);
		fft.copyImaginary(new float[wrongSize]);
	}

	@Test
	public void testBackwardIsInverseOperationOfForward() {
		// Für alle x[] gitl: x[] = IFFT(FFT(x[]))

		final int size = 1024;
		// Zwei unabhängige Instanzen stellen sicher, dass
		// die inverse Transformation nichts von der Vorwärtstransformation weiß
		final FastFourierTransform fft = new FastFourierTransform(size);
		final FastFourierTransform ifft = new FastFourierTransform(size);

		// Ergebnis von FFT(x[])
		final float[] realForward = new float[size];
		final float[] imaginaryForward = new float[size];

		// Ergebnis von IFFT(FFT(x[]))
		final float[] realBackward = new float[size];
		final float[] imaginaryBackward = new float[size];

		final int numSamples = 100;
		for (int i = 0; i < numSamples; i++) {
			// Generiere x[]
			final float[] realSamples = TestUtil.generateRandomWindow(random,
					size);
			final float[] imaginarySamples = TestUtil.generateRandomWindow(
					random, size);

			// Berechne FFT(x[])
			fft.forward(realSamples, imaginarySamples);
			fft.copyReal(realForward);
			fft.copyImaginary(imaginaryForward);

			// Berechne IFFT(FFT(x[]))
			ifft.backward(realForward, imaginaryForward);
			ifft.copyReal(realBackward);
			ifft.copyImaginary(imaginaryBackward);

			// Teste über RMSE, ob x[] = IFFT(FFT(x[])) gilt
			final float realRMSE = TestUtil.computeRMSE(realSamples,
					realBackward);
			final float imaginaryRMSE = TestUtil.computeRMSE(imaginarySamples,
					imaginaryBackward);
			assertEquals(0.0, realRMSE, COMPARISON_DELTA);
			assertEquals(0.0, imaginaryRMSE, COMPARISON_DELTA);
		}
	}

	@Test
	public void testLinearity() {
		// Für alle a, b, und x[], y[] gilt:
		// FFT(a*x[] + b*y[]) = a*FFT(x[]) + b*FFT(y[])

		final int size = 1024;
		final FastFourierTransform fft = new FastFourierTransform(size);

		// Ergbnis der linken Seite: FFT(a*x[] + b*y[])
		final float[] realLeft = new float[size];
		final float[] imaginaryLeft = new float[size];

		// Ergbnis der rechten Seite: a*FFT(x[]) + b*FFT(y[])
		final float[] realRight = new float[size];
		final float[] imaginaryRight = new float[size];

		// Ergbnis der Fourier-Transformierten von a*FFT(x[])
		final float[] realX = new float[size];
		final float[] imaginaryX = new float[size];

		// Ergbnis der Fourier-Transformierten von b*FFT(x[])
		final float[] realY = new float[size];
		final float[] imaginaryY = new float[size];

		// Ergebnis von a*x[] + b*y[]
		final float[] z = new float[size];

		final int numSamples = 100;
		for (int i = 0; i < numSamples; i++) {
			final float a = 10 * random.nextFloat() - 5;
			assert a >= -5 && a <= 5;
			final float b = 10 * random.nextFloat() - 5;
			assert b >= -5 && b <= 5;
			final float[] x = TestUtil.generateRandomWindow(random, size);
			final float[] y = TestUtil.generateRandomWindow(random, size);

			// Berechne z[] = a*x[] + b*y[]
			for (int j = 0; j < size; j++) {
				z[j] = a * x[j] + b * y[j];
			}

			// Berechne linke Seite: FFT(a*x[] + b*y[]) = FFT(z[])
			fft.forward(z);
			fft.copyReal(realLeft);
			fft.copyImaginary(imaginaryLeft);

			// Berechne FFT(x[])
			fft.forward(x);
			fft.copyReal(realX);
			fft.copyImaginary(imaginaryX);

			// Berechne FFT(y[])
			fft.forward(y);
			fft.copyReal(realY);
			fft.copyImaginary(imaginaryY);

			// Fasse rechte Seite zusammen: a*FFT(x[]) + b*FFT(y[])
			for (int j = 0; j < size; j++) {
				realRight[j] = a * realX[j] + b * realY[j];
				imaginaryRight[j] = a * imaginaryX[j] + b * imaginaryY[j];
			}

			// Teste über RMSE, ob linke mit rechter Seite übereinstimmt
			final float realRMSE = TestUtil.computeRMSE(realLeft, realRight);
			final float imaginaryRMSE = TestUtil.computeRMSE(imaginaryLeft,
					imaginaryRight);
			assertEquals(0.0, realRMSE, COMPARISON_DELTA);
			assertEquals(0.0, imaginaryRMSE, COMPARISON_DELTA);
		}
	}

	@Test
	public void testAgainstNonFastForwardTransform() {
		// Testet, ob die selben Ergebnisse bei der FFT herauskommen wie bei der
		// naiven Implementierung mit zwei For-Schleifen (Komplexität O(n²)).

		// Generiere Zufalls-Signal
		final int size = 1024;
		final float[] x = TestUtil.generateRandomWindow(random, size);

		// Naive Implementierung
		final float[] expectedReal = new float[size];
		final float[] expectedImaginary = new float[size];
		for (int k = 0; k < size; k++) {
			expectedReal[k] = 0f;
			expectedImaginary[k] = 0f;
			for (int i = 0; i < size; i++) {
				expectedReal[k] += x[i]
						* Math.cos(-k * i * (2 * Math.PI / size));
				expectedImaginary[k] += x[i]
						* Math.sin(-k * i * (2 * Math.PI / size));
			}
		}

		// FFT Implementierung
		final FastFourierTransform fft = new FastFourierTransform(size);
		fft.forward(x);
		final float[] actualReal = new float[size];
		final float[] actualImaginary = new float[size];
		fft.copyReal(actualReal);
		fft.copyImaginary(actualImaginary);

		// Teste über RMSE, ob naive Implementierung mit der FFT übereinstimmt
		final float realRMSE = TestUtil.computeRMSE(expectedReal, actualReal);
		final float imaginaryRMSE = TestUtil.computeRMSE(expectedImaginary,
				actualImaginary);
		assertEquals(0.0, realRMSE, COMPARISON_DELTA);
		assertEquals(0.0, imaginaryRMSE, COMPARISON_DELTA);
	}

	@Test
	public void testAgainstNonFastBackwardTransform() {
		// Testet, ob die selben Ergebnisse bei der IFFT herauskommen wie bei
		// der
		// naiven Implementierung mit zwei For-Schleifen (Komplexität O(n²)).

		// Generiere Zufalls-Signal
		final int size = 1024;
		final float[] realX = TestUtil.generateRandomWindow(random, size);
		final float[] imaginaryX = TestUtil.generateRandomWindow(random, size);

		// Naive Implementierung
		final float[] expectedReal = new float[size];
		final float[] expectedImaginary = new float[size];
		for (int k = 0; k < size; k++) {
			expectedReal[k] = 0f;
			expectedImaginary[k] = 0f;
			for (int i = 0; i < size; i++) {
				final float cos = (float) Math
						.cos((2 * Math.PI * i * k) / size);
				final float sin = (float) Math
						.sin((2 * Math.PI * i * k) / size);
				expectedReal[k] += realX[i] * cos - imaginaryX[i] * sin;
				expectedImaginary[k] += realX[i] * sin + imaginaryX[i] * cos;
			}
			expectedReal[k] /= size;
			expectedImaginary[k] /= size;
		}

		// FFT Implementierung
		final FastFourierTransform fft = new FastFourierTransform(size);
		fft.backward(realX, imaginaryX);
		final float[] actualReal = new float[size];
		final float[] actualImaginary = new float[size];
		fft.copyReal(actualReal);
		fft.copyImaginary(actualImaginary);

		// Teste über RMSE, ob naive Implementierung mit der FFT übereinstimmt
		final float realRMSE = TestUtil.computeRMSE(expectedReal, actualReal);
		final float imaginaryRMSE = TestUtil.computeRMSE(expectedImaginary,
				actualImaginary);
		assertEquals(0.0, realRMSE, COMPARISON_DELTA);
		assertEquals(0.0, imaginaryRMSE, COMPARISON_DELTA);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFillMagnitudeSpectrumChecksSize() {
		final int size = 1024;
		final int wrongSize = 512;
		final FastFourierTransform fft = new FastFourierTransform(size);
		fft.forward(new float[size]);
		fft.fillMagnitudeSpectrum(new float[wrongSize], false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFillMagnitudeSpectrumChecksSize2() {
		final int size = 1024;
		final int wrongSize = 256;
		final FastFourierTransform fft = new FastFourierTransform(size);
		fft.forward(new float[size]);
		fft.fillMagnitudeSpectrum(new float[wrongSize], true);
	}

	@Test
	public void testNormalizeMagnitudeSpectrumReal() {
		// Durch Normalisieren des Betragsspektrums werden die Bins so skaliert,
		// dass die ursprünglichen Amplituden abgelesen werden können.

		// Erstelle reellwertiges Eingangssignal
		final int size = 1024;
		final float amplitude = 4.0f;
		final int freq = 100;
		final float[] x = new float[size];
		for (int i = 0; i < size; i++) {
			x[i] = (float) (amplitude * Math.cos(freq * 2 * Math.PI * i / size));
		}

		final FastFourierTransform fft = new FastFourierTransform(size);
		// Vorwärtstransformation mit reellwertiger Eingabe
		fft.forward(x);
		final float[] spectrum = new float[size];
		fft.fillMagnitudeSpectrum(spectrum, true);
		fft.normalizeMagnitudeSpectrum(spectrum, true);

		assertEquals(amplitude, spectrum[freq], COMPARISON_DELTA);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFillPhaseSpectrumChecksSize() {
		final int size = 1024;
		final int wrongSize = 512;
		final FastFourierTransform fft = new FastFourierTransform(size);
		fft.forward(new float[size]);
		fft.fillPhaseSpectrum(new float[wrongSize], false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFillPhaseSpectrumChecksSize2() {
		final int size = 1024;
		final int wrongSize = 256;
		final FastFourierTransform fft = new FastFourierTransform(size);
		fft.forward(new float[size]);
		fft.fillPhaseSpectrum(new float[wrongSize], true);
	}

	@Test
	public void testPhaseSpectrumIsBetweenNegativeAndPositivePi() {
		final int numSamples = 100;
		for (int i = 0; i < numSamples; i++) {
			testPhaseSpectrumIsBetweenNegativeAndPositivePiSingle();
		}
	}

	private void testPhaseSpectrumIsBetweenNegativeAndPositivePiSingle() {
		final int size = 1024;
		final float[] x = TestUtil
				.generateRandomWindow(random, size, -10f, 10f);

		final FastFourierTransform fft = new FastFourierTransform(size);
		fft.forward(x);
		final float[] phase = new float[size];
		fft.fillPhaseSpectrum(phase, false);

		for (int k = 0; k < size; k++) {
			assertTrue(-2 * Math.PI <= phase[k] && phase[k] <= 2 * Math.PI);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFillPowerSpectrumChecksSize() {
		final int size = 1024;
		final int wrongSize = 512;
		final FastFourierTransform fft = new FastFourierTransform(size);
		fft.forward(new float[size]);
		fft.fillPowerSpectrum(new float[wrongSize], false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFillPowerSpectrumChecksSize2() {
		final int size = 1024;
		final int wrongSize = 256;
		final FastFourierTransform fft = new FastFourierTransform(size);
		fft.forward(new float[size]);
		fft.fillPowerSpectrum(new float[wrongSize], true);
	}

	@Test
	public void testPowerSpectrumIsSquaredMagnitudeSpectrum() {
		final int numSamples = 100;
		for (int i = 0; i < numSamples; i++) {
			testPowerSpectrumIsSquaredMagnitudeSpectrumSingle();
		}
	}

	private void testPowerSpectrumIsSquaredMagnitudeSpectrumSingle() {
		final int size = 1024;
		final float[] x = TestUtil.generateRandomWindow(random, size);
		final FastFourierTransform fft = new FastFourierTransform(size);
		fft.forward(x);
		final float[] expected = new float[size];
		fft.fillMagnitudeSpectrum(expected, false);
		for (int k = 0; k < size; k++) {
			expected[k] = expected[k] * expected[k];
		}
		final float[] actual = new float[size];
		fft.fillPowerSpectrum(actual, false);

		final float rmse = TestUtil.computeRMSE(expected, actual);
		assertEquals(0f, rmse, COMPARISON_DELTA);
	}

	@Test
	public void testFrequencyToIndex_IsInverseTo_IndexToFrequency() {
		final int size = 4096;
		final double sampleRate = 44100;
		for (int k = 0; k <= size / 2; k++) {
			final int expectedIndex = k;
			final double freq = FastFourierTransform.binToFrequency(k, size,
					sampleRate);
			final int actualIndex = FastFourierTransform.frequencyToBin(freq,
					size, sampleRate);
			assertEquals(expectedIndex, actualIndex);
		}
	}

	@Test
	public void testIndexToFrequencyIsStrictlyMonotonousIncreasing() {
		final int size = 4096;
		final double sampleRate = 44100;
		for (int k = 1; k < size / 2; k++) {
			final double freqBefore = FastFourierTransform.binToFrequency(
					k - 1, size, sampleRate);
			final double freq = FastFourierTransform.binToFrequency(k, size,
					sampleRate);
			assertTrue(freqBefore < freq);
		}
	}

	@Test
	public void testNormalizeMagnitudeSpectrumComplex() {
		// Durch Normalisieren des Betragsspektrums werden die Bins so skaliert,
		// dass die ursprünglichen Amplituden abgelesen werden können.

		// Erstelle komplexwertiges Eingangssignal
		final int size = 1024;
		final float amplitude = 4.0f;
		final int freq = 100;
		final float[] realX = new float[size];
		final float[] imaginaryX = new float[size];
		// x[i] = A*exp((j*f*2*Pi*i)/N) mit j = sqrt(-1)
		for (int i = 0; i < size; i++) {
			realX[i] = (float) (amplitude * Math.cos(freq * 2 * Math.PI * i
					/ size));
			imaginaryX[i] = (float) (amplitude * Math.sin(freq * 2 * Math.PI
					* i / size));
		}

		final FastFourierTransform fft = new FastFourierTransform(size);
		// Vorwärtstransformation mit komplexwertiger Eingabe
		fft.forward(realX, imaginaryX);
		final float[] spectrum = new float[size];
		fft.fillMagnitudeSpectrum(spectrum, false);
		fft.normalizeMagnitudeSpectrum(spectrum, false);

		assertEquals(amplitude, spectrum[freq], COMPARISON_DELTA);
	}
}
