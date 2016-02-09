package de.skawronek.audiolib;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;

import de.skawronek.audiolib.FooFeature.FooKey;
import de.skawronek.audiolib.FooFeature.FooProcessor;
import static org.junit.Assert.*;

public final class FrameTest {
	private static final float COMPARISION_DELTA = 0.00001f;

	@Test
	public void testCreateMonoFrame() {
		final int size = 22050;
		final long begin = 44100;
		final long end = begin + size - 1;
		final double sampleRate = 44100;
		final float[] samples = new float[size];

		final Frame frame = Frame.createMonoFrame(begin, sampleRate, samples);
		assertNotNull(frame);

		assertEquals(begin, frame.getBegin());
		assertEquals(end, frame.getEnd());
		assertEquals(Duration.ofMillis(1000), frame.getBeginTime());
		assertEquals(Duration.ofMillis(1250), frame.getTime());
		assertEquals(Duration.ofMillis(1500), frame.getEndTime());

		assertEquals(sampleRate, frame.getSampleRate(), COMPARISION_DELTA);
		assertEquals(size, frame.getSize());
		assertFalse(frame.isStereo());
	}

	@Test
	public void testCreateStereoFrame() {
		final int size = 22050;
		final long begin = 44100;
		final long end = begin + size - 1;
		final double sampleRate = 44100;
		final float[] leftSamples = new float[size];
		final float[] rightSamples = new float[size];

		final Frame frame = Frame.createStereoFrame(begin, sampleRate,
				leftSamples, rightSamples);
		assertNotNull(frame);

		assertEquals(begin, frame.getBegin());
		assertEquals(end, frame.getEnd());
		assertEquals(Duration.ofMillis(1000), frame.getBeginTime());
		assertEquals(Duration.ofMillis(1250), frame.getTime());
		assertEquals(Duration.ofMillis(1500), frame.getEndTime());

		assertEquals(sampleRate, frame.getSampleRate(), COMPARISION_DELTA);
		assertEquals(size, frame.getSize());
		assertTrue(frame.isStereo());
	}

	@Test
	public void testGetMonoSamplesOfStereoFrame() {
		final int size = 10;
		final float[] leftSamples = new float[size];
		final float[] rightSamples = new float[size];
		// leftSamples: -0.5, -0.4, -0.3, ..., 0.5
		// rightSamples: -0.25, -0.2, -0.15, ..., 0.25
		for (int i = 0; i < size; i++) {
			leftSamples[i] = (i - 5) * 0.1f;
			rightSamples[i] = (i - 5) * 0.05f;
		}

		final Frame frame = Frame.createStereoFrame(0, 44100, leftSamples,
				rightSamples);
		final float[] monoSamples = frame.getMonoSamples();
		assertNotNull(monoSamples);
		assertEquals(size, monoSamples.length);

		// Teste, ob beim Down-Mixen der Durchschnitt aus Left- und
		// Right-Channek gebildet wurde.
		for (int i = 0; i < size; i++) {
			final float mean = (leftSamples[i] + rightSamples[i]) / 2;
			assertEquals("monoSamples differs at index " + i, mean,
					monoSamples[i], COMPARISION_DELTA);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testGetLeftSamplesThrowsIllegalStateExceptionOnMonoFrame() {
		final Frame monoFrame = Frame
				.createMonoFrame(0, 44100, new float[1024]);
		monoFrame.getLeftSamples();
	}

	@Test
	public void testGetLeftSamples() {
		final float[] leftSamples = { 0.1f, 0.2f };
		final float[] rightSamples = { 0.3f, 0.4f };
		final Frame stereoFrame = Frame.createStereoFrame(0, 44100,
				leftSamples, rightSamples);
		final float[] actualLeftSamples = stereoFrame.getLeftSamples();
		assertArrayEquals(leftSamples, actualLeftSamples, COMPARISION_DELTA);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetRightSamplesThrowsIllegalStateExceptionOnMonoFrame() {
		final Frame monoFrame = Frame
				.createMonoFrame(0, 44100, new float[1024]);
		monoFrame.getRightSamples();
	}

	@Test
	public void testGetRightSamples() {
		final float[] leftSamples = { 0.1f, 0.2f };
		final float[] rightSamples = { 0.3f, 0.4f };
		final Frame stereoFrame = Frame.createStereoFrame(0, 44100,
				leftSamples, rightSamples);
		final float[] actualRightSamples = stereoFrame.getRightSamples();
		assertArrayEquals(rightSamples, actualRightSamples, COMPARISION_DELTA);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateMonoFrameChecksForNegativeBegin() {
		final long begin = -1;
		Frame.createMonoFrame(begin, 44100, new float[1024]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateMonoFrameChecksForZeroSamplerate() {
		final double sampleRate = 0;
		Frame.createMonoFrame(0, sampleRate, new float[1024]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateMonoFrameChecksEmptyness() {
		Frame.createMonoFrame(0, 44100, new float[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateStereoFrameChecksForNegativeBegin() {
		final long begin = -1;
		Frame.createStereoFrame(begin, 44100, new float[1024], new float[1024]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateStereoFrameChecksForZeroSamplerate() {
		final double sampleRate = 0;
		Frame.createStereoFrame(0, sampleRate, new float[1024], new float[1024]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateStereoFrameChecksEmptyness() {
		Frame.createStereoFrame(0, 44100, new float[0], new float[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateStereoFrameChecksForSameSize() {
		final float[] leftSamples = new float[1024];
		final float[] rightSamples = new float[2048];
		Frame.createStereoFrame(0, 44100, leftSamples, rightSamples);
	}

	@Test
	public void testGetFeatureReturnsTheSameFeatureForTheEqualKey() {
		final FooProcessor processor = new FooProcessor();
		FeatureFactory.getInstance().registerProcessor(processor);
		final Frame frame = createFrame();
		final FooKey key = new FooKey();
		final FooFeature feature1 = frame.getFeature(key);
		final FooFeature feature2 = frame.getFeature(key);
		assertSame(feature1, feature2);
	}

	@Test
	public void testContainsFeature() {
		final FooProcessor processor = new FooProcessor();
		FeatureFactory.getInstance().registerProcessor(processor);
		final Frame frame = createFrame();
		final FooKey key = new FooKey();
		assertFalse(frame.containsFeature(key));
		// Berechne Feature
		frame.getFeature(key);
		// Jetzt ist es vorhanden
		assertTrue(frame.containsFeature(key));
	}

	private static @NonNull Frame createFrame() {
		final float[] samples = new float[2048];
		return Frame.createMonoFrame(0, 44100.0, samples);
	}
}
