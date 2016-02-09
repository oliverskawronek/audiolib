package de.skawronek.audiolib;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;

public final class FrameManagerTest {
	private static final float COMPARISION_DELTA = 0.00001f;

	@Test(expected = IllegalArgumentException.class)
	public void testForMonoAudioSourceChecksNegativeSampleRate() {
		FrameManager.forMonoAudioSource(-1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testForMonoAudioSourceChecksZeroSampleRate() {
		FrameManager.forMonoAudioSource(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testForStereoAudioSourceChecksNegativeSampleRate() {
		FrameManager.forStereoAudioSource(-1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testForStereoAudioSourceChecksZeroSampleRate() {
		FrameManager.forStereoAudioSource(0);
	}

	@Test
	public void testIsStereo() {
		assertFalse(FrameManager.forMonoAudioSource(44100).isStereo());
		assertTrue(FrameManager.forStereoAudioSource(44100).isStereo());
	}

	@Test
	public void testIsMono() {
		assertTrue(FrameManager.forMonoAudioSource(44100).isMono());
		assertFalse(FrameManager.forStereoAudioSource(44100).isMono());
	}

	@Test(expected = IllegalStateException.class)
	public void testPutMonoSamplesChecksIfMono() {
		final FrameManager fm = FrameManager.forMonoAudioSource(44100);
		fm.putStereoSample(0.5f, 0.5f);
	}

	@Test(expected = IllegalStateException.class)
	public void testPutMonoSamplesChecksIfStereo() {
		final FrameManager fm = FrameManager.forStereoAudioSource(44100);
		fm.putMonoSample(0.5f);
	}

	@Test
	public void testNumberOfCreatedFramesMono() {
		testNumberOfCreatedFrames(FrameManager.forMonoAudioSource(44100));
	}

	@Test
	public void testNumberOfCreatedFramesStereo() {
		testNumberOfCreatedFrames(FrameManager.forStereoAudioSource(44100));
	}

	private void testNumberOfCreatedFrames(final @NonNull FrameManager fm) {
		final AtomicInteger counter = new AtomicInteger(0);
		final FrameSpecification spec = new FrameSpecification(4, 2);
		fm.addListener(new IFrameListener() {
			@Override
			public void onFrameAvailable(final @NonNull Frame frame) {
				counter.incrementAndGet();
			}
		}, spec);

		// Füge 8 Samples hinzu.
		for (int i = 0; i < 8; i++) {
			if (fm.isMono()) {
				fm.putMonoSample(0.5f);
			} else if (fm.isStereo()) {
				fm.putStereoSample(0.5f, 0.5f);
			} else {
				throw new AssertionError();
			}
		}
		fm.processFrames();

		// Die zu erstellenden Frames sind:
		// [0, 3], [2, 5], [4, 7]. Frame [6, 9] ist nicht vollständig.
		// Insgesamt sind es also 3 Frames.
		assertEquals(3, counter.get());
	}

	@Test
	public void testAllFrameAreCreatedMono() {
		testAllFrameAreCreated(FrameManager.forMonoAudioSource(44100));
	}

	@Test
	public void testAllFrameAreCreatedStereo() {
		testAllFrameAreCreated(FrameManager.forStereoAudioSource(44100));
	}

	private void testAllFrameAreCreated(final @NonNull FrameManager fm) {
		final List<Frame> frames = new ArrayList<>();

		final Consumer<Frame> frameAdder = (f -> frames.add(f));
		addListener(fm, new FrameSpecification(16, 8), frameAdder);
		addListener(fm, new FrameSpecification(32, 16), frameAdder);
		addListener(fm, new FrameSpecification(32, 32), frameAdder);

		final int numSamples = 2000;
		for (int i = 0; i < numSamples; i++) {
			if (fm.isMono()) {
				fm.putMonoSample(0.5f);
			} else if (fm.isStereo()) {
				fm.putStereoSample(0.5f, 0.5f);
			} else {
				throw new AssertionError();
			}
		}
		fm.processFrames();

		assertTrue(containsAllFrames(frames, numSamples, 16, 8));
		assertTrue(containsAllFrames(frames, numSamples, 32, 16));
		assertTrue(containsAllFrames(frames, numSamples, 32, 32));
	}

	private static boolean containsAllFrames(
			final @NonNull Collection<Frame> frames, final int numSamples,
			final int frameSize, final int hopSize) {
		int frame = 0;
		while (frame * hopSize + frameSize <= numSamples) {
			final long begin = frame * 8L;
			final long end = begin + 16 - 1;
			if (!containsFrame(frames, begin, end)) {
				return false;
			}
			frame++;
		}
		return true;
	}

	private static boolean containsFrame(
			final @NonNull Collection<Frame> frames, final long begin,
			final long end) {
		return frames.stream()
				.filter(f -> f.getBegin() == begin && f.getEnd() == end)
				.findAny().isPresent();
	}

	@Test
	public void testNoDuplicatedFramesAreCreatedMono() {
		testNoDuplicatedFramesAreCreated(FrameManager.forMonoAudioSource(44100));
	}

	@Test
	public void testNoDuplicatedFramesAreCreatedStereo() {
		testNoDuplicatedFramesAreCreated(FrameManager
				.forStereoAudioSource(44100));
	}

	private void testNoDuplicatedFramesAreCreated(final @NonNull FrameManager fm) {
		final List<Frame> frames = new ArrayList<>();

		final Consumer<Frame> frameAdder = (f -> frames.add(f));
		addListener(fm, new FrameSpecification(16, 8), frameAdder);
		addListener(fm, new FrameSpecification(8, 8), frameAdder);
		addListener(fm, new FrameSpecification(16, 16), frameAdder);
		addListener(fm, new FrameSpecification(7, 4), frameAdder);
		// Ein anderer Listener mit der selben FrameSpecification.
		addListener(fm, new FrameSpecification(7, 4), frameAdder);

		final int numSamples = 20;
		for (int i = 0; i < numSamples; i++) {
			if (fm.isMono()) {
				fm.putMonoSample(0.5f);
			} else if (fm.isStereo()) {
				fm.putStereoSample(0.5f, 0.5f);
			} else {
				throw new AssertionError();
			}
		}
		fm.processFrames();

		for (final Frame frame : frames) {
			final long begin = frame.getBegin();
			final long end = frame.getEnd();
			assertFalse(containsFrameDuplicates(frames, begin, end));
		}
	}

	@Test
	public void testSamplesCreatedCorrectMono() {
		testSamplesCreatedCorrect(FrameManager.forMonoAudioSource(44100));
	}

	@Test
	public void testSamplesCreatedCorrectStereo() {
		testSamplesCreatedCorrect(FrameManager.forStereoAudioSource(44100));
	}

	private void testSamplesCreatedCorrect(final @NonNull FrameManager fm) {
		final List<Frame> frames = new ArrayList<>();

		final Consumer<Frame> frameAdder = (f -> frames.add(f));
		addListener(fm, new FrameSpecification(16, 8), frameAdder);
		addListener(fm, new FrameSpecification(8, 8), frameAdder);
		addListener(fm, new FrameSpecification(7, 4), frameAdder);

		// Fülle RingBuffer wie folgt: x[i] = i / 100 für i = 0..99 (mono).
		// Analog bei Stereo: linker Kanal x[i] = i / 100 und x[i] = -i / 100
		// rechter Kanal.
		for (int i = 0; i < 100; i++) {
			if (fm.isMono()) {
				fm.putMonoSample(i / 100f);
			} else if (fm.isStereo()) {
				fm.putStereoSample(i / 100f, -i / 100f);
			} else {
				throw new AssertionError();
			}
		}
		fm.processFrames();

		// Prüfe Samples in allen Frames
		for (final Frame frame : frames) {
			if (fm.isMono()) {
				final float[] actualSamples = frame.getMonoSamples();
				for (int i = (int) frame.getBegin(); i <= (int) frame.getEnd(); i++) {
					final float expectedSample = i / 100f;
					final float actualSample = actualSamples[i
							- ((int) frame.getBegin())];
					assertEquals(expectedSample, actualSample,
							COMPARISION_DELTA);
				}
			} else if (fm.isStereo()) {
				final float[] actualLeftSamples = frame.getLeftSamples();
				final float[] actualRightSamples = frame.getRightSamples();
				for (int i = (int) frame.getBegin(); i <= (int) frame.getEnd(); i++) {
					final float expectedLeftSample = i / 100f;
					final float expectedRightSample = -i / 100f;
					final float actualLeftSample = actualLeftSamples[i
							- ((int) frame.getBegin())];
					final float actualRightSample = actualRightSamples[i
							- ((int) frame.getBegin())];
					assertEquals(expectedLeftSample, actualLeftSample,
							COMPARISION_DELTA);
					assertEquals(expectedRightSample, actualRightSample,
							COMPARISION_DELTA);
				}
			} else {
				throw new AssertionError();
			}
		}
	}

	@Test
	public void testRemovedListenerDontGetNotified() {
		final FrameManager fm = FrameManager.forMonoAudioSource(44100);
		final IFrameListener listener = new IFrameListener() {
			@Override
			public void onFrameAvailable(@NonNull Frame frame) {
				fail("Unexpected listener notification");
			}
		};
		final int frameSize = 10;
		fm.addListener(listener, new FrameSpecification(frameSize, 10));
		fm.removeListener(listener);

		// Erzeuge soviele Samples, sodass Frame mit einer Größe von 10 erstellt
		// werden würde.
		for (int i = 0; i < frameSize; i++) {
			fm.putMonoSample(0.1234f);
		}
		// Listener ist nicht registriert, d. h. er dürfte nicht über Frame
		// [0, 9] benachrichtigt werden.
		fm.processFrames();
	}

	private static void addListener(final @NonNull FrameManager fm,
			final @NonNull FrameSpecification spec,
			final @NonNull Consumer<Frame> operation) {
		fm.addListener(new IFrameListener() {
			@Override
			public void onFrameAvailable(@NonNull Frame frame) {
				operation.accept(frame);
			}
		}, spec);
	}

	private static boolean containsFrameDuplicates(
			final @NonNull List<Frame> frames, final long begin, final long end) {
		final List<Frame> framesWithSameTiming = frames.stream()
				.filter(f -> f.getBegin() == begin && f.getEnd() == end)
				.collect(Collectors.toList());

		if (framesWithSameTiming.size() <= 1) {
			// Es wurde maximal ein Frame mit dem selbem Timing gefunden.
			return false;
		}

		// In frameWithSameTiming sollten sich, wenn count > 0, nur die selben
		// Instanzen befinden.
		for (final Frame f1 : framesWithSameTiming) {
			for (final Frame f2 : framesWithSameTiming) {
				if (f1 != f2) {
					assert f1.getBegin() == f2.getBegin()
							&& f1.getEnd() == f2.getEnd();
					assert f1.getBegin() == begin && f2.getEnd() == end;
					// Unterschiedliche Instanzen für das selbe Timing gefunden.
					return true;
				}
			}
		}
		// Alle Instanzen in framesWithSameTiming sind identisch.
		return false;
	}

	@Test
	public void testCreatedFramesHaveCorrectSizeMono() {
		testCreatedFramesHaveCorrectSize(FrameManager.forMonoAudioSource(44100));
	}

	@Test
	public void testCreatedFramesHaveCorrectSizeStereo() {
		testCreatedFramesHaveCorrectSize(FrameManager
				.forStereoAudioSource(44100));
	}

	private void testCreatedFramesHaveCorrectSize(final @NonNull FrameManager fm) {
		final FrameSpecification spec = new FrameSpecification(200, 100);
		fm.addListener(new IFrameListener() {
			@Override
			public void onFrameAvailable(final @NonNull Frame frame) {
				assertEquals(spec.getSize(), frame.getSize());
				// Prüfe zusätzlich, ob Frame-Begin ganzahlig Vielfaches der
				// Hop-Size ist.
				assertTrue(frame.getBegin() % spec.getHopSize() == 0);
			}
		}, spec);

		final int numSamples = 2000;
		for (int i = 0; i < numSamples; i++) {
			if (fm.isMono()) {
				fm.putMonoSample(0.5f);
			} else if (fm.isStereo()) {
				fm.putStereoSample(0.5f, 0.5f);
			} else {
				throw new AssertionError();
			}
		}
		fm.processFrames();
	}
}
