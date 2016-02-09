package de.skawronek.audiolib;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;

import de.skawronek.audiolib.AudioSource.ReadException;
import de.skawronek.audiolib.PcmCodec.UnsuportedFormatException;

public final class WaveAudioSourceTest {
	private static final float COMPARISION_DELTA = 0.000001f;

	private static final File RESOURCES_DIR = new File("src/test/resources");

	@Test(expected = IOException.class)
	public void testFromFileRejectsNonExistingFile()
			throws UnsuportedFormatException, IOException {
		final File nonExisting = new File(RESOURCES_DIR,
				"wave/File_Does_Not_Exists.wav");
		WaveAudioSource.fromFile(nonExisting);
	}

	@Test(expected = UnsuportedFormatException.class)
	public void testFromFileRejectsTextFile() throws UnsuportedFormatException,
			IOException {
		final File file = new File(RESOURCES_DIR, "wave/Non_Wave.txt");
		WaveAudioSource.fromFile(file);
	}

	@Test
	public void testFromFileReturnsNonNull() throws UnsuportedFormatException,
			IOException {
		final File file = new File(RESOURCES_DIR, "wave/Sin440Hz_Mono.wav");
		assertNotNull(WaveAudioSource.fromFile(file));
	}

	@Test(expected = UnsuportedFormatException.class)
	public void testFromInputStreamRejectsTextFile()
			throws UnsuportedFormatException, IOException {
		final InputStream stream = WaveAudioSourceTest.class
				.getResourceAsStream("/wave/Non_Wave.txt");
		final WaveAudioSource source = WaveAudioSource.fromInputStream(stream);
		source.stop();
	}

	@Test
	public void testFromInputStreamsReturnsNonNull()
			throws UnsuportedFormatException, IOException {
		final InputStream stream = WaveAudioSourceTest.class
				.getResourceAsStream("/wave/Sin440Hz_Mono.wav");
		final WaveAudioSource source = WaveAudioSource.fromInputStream(stream);
		assertNotNull(source);
		source.stop();
	}

	@Test
	public void testAvailableChannels() throws UnsuportedFormatException,
			IOException {
		final WaveAudioSource monoSource = WaveAudioSource.fromFile(new File(
				RESOURCES_DIR, "wave/Sin440Hz_Mono.wav"));
		assertTrue(monoSource.getAvailableChannels().contains(Channel.MONO));
		assertFalse(monoSource.getAvailableChannels().contains(Channel.LEFT));
		assertFalse(monoSource.getAvailableChannels().contains(Channel.RIGHT));

		final WaveAudioSource stereoSource = WaveAudioSource.fromFile(new File(
				RESOURCES_DIR, "wave/Sin440Hz_Stereo.wav"));
		assertTrue(stereoSource.getAvailableChannels().contains(Channel.MONO));
		assertTrue(stereoSource.getAvailableChannels().contains(Channel.LEFT));
		assertTrue(stereoSource.getAvailableChannels().contains(Channel.RIGHT));
	}

	@Test
	public void testGetSampleRate() throws UnsuportedFormatException,
			IOException {
		final WaveAudioSource monoSource = WaveAudioSource.fromFile(new File(
				RESOURCES_DIR, "wave/Sin440Hz_Mono.wav"));
		assertEquals(44100, monoSource.getSampleRate(), COMPARISION_DELTA);

		final WaveAudioSource stereoSource = WaveAudioSource.fromFile(new File(
				RESOURCES_DIR, "wave/Sin440Hz_Stereo.wav"));
		assertEquals(44100, stereoSource.getSampleRate(), COMPARISION_DELTA);
	}

	@Test(expected = IllegalStateException.class)
	public void testStartFailsWhenStopped() throws UnsuportedFormatException,
			IOException {
		final WaveAudioSource source = WaveAudioSource.fromFile(new File(
				RESOURCES_DIR, "wave/Sin440Hz_Mono.wav"));
		try {
			source.start();
		} catch (final IllegalStateException e) {
			fail("Unexpected IllegalStateException on first start");
		}
		source.stop();
		source.start();
	}

	@Test
	public void testStopDontFailsWhenNotStarted()
			throws UnsuportedFormatException, IOException {
		final WaveAudioSource source = WaveAudioSource.fromFile(new File(
				RESOURCES_DIR, "wave/Sin440Hz_Mono.wav"));
		// Stop kann mehrmals aufgerufen werden.
		source.stop();
		source.stop();
		source.stop();
	}

	@Test
	public void testIsStarted() throws UnsuportedFormatException, IOException {
		final WaveAudioSource source = WaveAudioSource.fromFile(new File(
				RESOURCES_DIR, "wave/Sin440Hz_Mono.wav"));
		assertFalse(source.isStarted());
		try {
			source.start();
			assertTrue(source.isStarted());
			source.stop();
			assertFalse(source.isStarted());
		} finally {
			source.stop();
		}
	}

	@Test
	public void testIsStopped() throws UnsuportedFormatException, IOException {
		final WaveAudioSource source = WaveAudioSource.fromFile(new File(
				RESOURCES_DIR, "wave/Sin440Hz_Mono.wav"));
		assertFalse(source.isStopped());
		source.start();
		assertFalse(source.isStopped());
		source.stop();
		assertTrue(source.isStopped());
	}

	@Test(expected = IllegalStateException.class)
	public void testProcessFailsWhenNotStarted()
			throws UnsuportedFormatException, IOException, ReadException {
		final WaveAudioSource source = WaveAudioSource.fromFile(new File(
				RESOURCES_DIR, "wave/Sin440Hz_Mono.wav"));
		try {
			source.process();
		} finally {
			source.stop();
		}
	}

	@Test
	public void testAllSamplesReadMono() throws IOException, ReadException,
			UnsuportedFormatException {
		final File monoFile = new File(RESOURCES_DIR, "wave/Sin440Hz_Mono.wav");
		testAllSamplesRead(WaveAudioSource.fromFile(monoFile));
	}

	@Test
	public void testAllSamplesReadStereo() throws IOException, ReadException,
			UnsuportedFormatException {
		final File monoFile = new File(RESOURCES_DIR,
				"wave/Sin440Hz_Stereo.wav");
		testAllSamplesRead(WaveAudioSource.fromFile(monoFile));
	}

	private void testAllSamplesRead(final @NonNull WaveAudioSource source)
			throws IOException, ReadException {

		// Die WAVE-Datei hat 44100 Samples. Fülle einen Buffer mit NaNs
		// und prüfe nach dem Lesen der Datei, ob alle NaNs überschrieben
		// wurden.
		final float[] overallSamples = new float[44100];
		for (int i = 0; i < overallSamples.length; i++) {
			overallSamples[i] = Float.NaN;
		}

		// Listener schreibt alle Frames in overallSamples.
		source.addListener(new IFrameListener() {
			@Override
			public void onFrameAvailable(final @NonNull Frame frame) {
				final int destPos = (int) frame.getBegin();
				final int length = frame.getSize();

				if (frame.isStereo()) {
					// Linker und rechter Channel sind gleich.
					final float[] leftSamples = frame.getLeftSamples();
					final float[] rightSamples = frame.getRightSamples();
					// Beim Dekodieren können größere Rundungsfehler auftauchen.
					final float comparisonDelta = 0.001f;
					assertArrayEquals(leftSamples, rightSamples,
							comparisonDelta);
					System.arraycopy(leftSamples, 0, overallSamples, destPos,
							length);
				} else {
					System.arraycopy(frame.getMonoSamples(), 0, overallSamples,
							destPos, length);
				}
			}
		}, new FrameSpecification(100, 100));

		// Lese komplette WAVE-Datei
		try {
			source.start();
			while (source.isStarted()) {
				source.process();
			}
		} finally {
			source.stop();
		}

		// Prüfe, ob alle NaNs überschrieben wurden.
		for (int i = 0; i < overallSamples.length; i++) {
			assertFalse("Sample " + i + " not read",
					Float.isNaN(overallSamples[i]));
		}
	}

	@Test
	public void testRemovedListenerDontGetNotified() throws ReadException,
			UnsuportedFormatException, IOException {
		final File file = new File(RESOURCES_DIR, "wave/Sin440Hz_Stereo.wav");
		final WaveAudioSource source = WaveAudioSource.fromFile(file);

		final IFrameListener listener = new IFrameListener() {
			@Override
			public void onFrameAvailable(@NonNull Frame frame) {
				fail("Unexpected listener notification");
			}
		};
		final int frameSize = 10;
		source.addListener(listener, new FrameSpecification(frameSize, 10));
		source.removeListener(listener);

		// Lese komplette WAVE-Datei
		try {
			source.start();
			while (source.isStarted()) {
				source.process();
			}
		} finally {
			source.stop();
		}
	}
}
