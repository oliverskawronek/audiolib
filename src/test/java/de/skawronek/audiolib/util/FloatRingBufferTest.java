package de.skawronek.audiolib.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

public final class FloatRingBufferTest {
	private static final float COMPARISON_DELTA = 0.000001f;

	private static final int CAPACITY = 5;

	private FloatRingBuffer ringBuffer;

	@Before
	public void init() {
		ringBuffer = new FloatRingBuffer(CAPACITY);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorChecksForEmpty() {
		new FloatRingBuffer(new float[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorChecksNegativeCapacity() {
		new FloatRingBuffer(-1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorChecksZeroCapacity() {
		new FloatRingBuffer(0);
	}

	@Test
	public void testConstructor() {
		final int capacity = 5;
		final FloatRingBuffer ringBuffer = new FloatRingBuffer(
				new float[capacity]);
		assertEquals(capacity, ringBuffer.getCapacity());
	}

	@Test
	public void testIsEmpty() {
		assert ringBuffer.getSize() == 0;
		assertTrue(ringBuffer.isEmpty());
		final int numPuts = 100;
		for (int i = 0; i < numPuts; i++) {
			ringBuffer.put(1.2345f);
			assertFalse(ringBuffer.isEmpty());
		}
	}

	@Test
	public void testClear() {
		// Kapazität ändert sich nie
		final int capacity = ringBuffer.getCapacity();

		ringBuffer.clear();
		assertEquals(0, ringBuffer.getCount());
		assertEquals(0, ringBuffer.getSize());
		assertEquals(capacity, ringBuffer.getCapacity());

		fillRingBuffer(100);

		ringBuffer.clear();
		assertEquals(0, ringBuffer.getCount());
		assertEquals(0, ringBuffer.getSize());
		assertEquals(capacity, ringBuffer.getCapacity());
	}

	@Test
	public void testPutIncrementsCount() {
		final int numPuts = 100;
		for (int i = 0; i < numPuts; i++) {
			final long countBefore = ringBuffer.getCount();
			ringBuffer.put(1.2345f);
			final long countAfter = ringBuffer.getCount();
			assertEquals(countBefore + 1, countAfter);
		}
	}

	@Test
	public void testPutIncrementsSize() {
		final int numPuts = 100;
		final int maxSize = ringBuffer.getCapacity();
		for (int i = 0; i < numPuts; i++) {
			final int sizeBefore = ringBuffer.getSize();
			ringBuffer.put(1.2345f);
			final int sizeAfter = ringBuffer.getSize();

			final int expectedSize = Math.min(sizeBefore + 1, maxSize);
			assertEquals(expectedSize, sizeAfter);
		}
	}

	@Test
	public void testSizeIsLowerOrEqualsCapacity() {
		final int numPuts = 1000;
		for (int i = 0; i < numPuts; i++) {
			ringBuffer.put(1.2345f);

			// Lösche RingBuffer zu einem beliebigen Zeitpunkt
			if (i == 123) {
				ringBuffer.clear();
			}

			assertTrue(ringBuffer.getSize() <= ringBuffer.getCapacity());
		}
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testPeakChecksBufferSize() {
		fillRingBuffer(5);

		final int bufferSize = 3;
		final float[] buffer = new float[bufferSize];
		final int offset = 2;
		final int length = 2;

		// Es soll buffer ab offset = 2 (inkl) bis (offset + length) = 4 (exkl.)
		// gefüllt werden. Buffer kann jedoch bis max. Index 2 gefüllt werden.
		ringBuffer.peak(0, buffer, offset, length);
	}

	@Test(expected = NoSuchElementException.class)
	public void testPeakCantReadUnavilableStart() {
		fillRingBuffer(100);
		final float[] buffer = new float[1];
		// Es kann nur von Index 95 (inkl) bis 100 (exkl.) gelesen werden.
		// Zugriff auf Index 100 muss scheitern.
		ringBuffer.peak(100, buffer, 0, 1);
	}

	@Test(expected = NoSuchElementException.class)
	public void testPeakDetermineOverflow() {
		fillRingBuffer(100);
		final float[] buffer = new float[1];
		// Der aktuelle Count ist 100. Mit einer Kapazität können nur die
		// Elemente von 95 (inkl.) bis 100 (exkl.) gelesen werden. Der Zugriff
		// auf Index 94 muss scheitern.
		ringBuffer.peak(94, buffer, 0, 1);
	}

	@Test(expected = NoSuchElementException.class)
	public void testPeakChecksForTooMuchElementsToRead() {
		fillRingBuffer(100);
		final float[] buffer = new float[6];
		// Es kann nur von Index 95 (inkl) bis 100 (exkl.) gelesen werden.
		// Der Zugriff auf 95 (inkl.) bis 101 (exkl.) muss scheitern.
		ringBuffer.peak(95, buffer, 0, 6);
	}

	@Test
	public void testPeakDontTouchTheBufferWithZeroLength() {
		final float[] buffer = { 123 };
		final int length = 0;
		ringBuffer.peak(0, buffer, 0, length);
		assertEquals(123, buffer[0], COMPARISON_DELTA);
	}

	@Test
	public void testPeakReadsTheRightElements() {
		for (int i = 0; i < 10; i++) {
			ringBuffer.put(i);
		}
		// Im RingBuffer stehen die Elemente 5, 6, 7, 8, 9

		// Lese von 6 (inkl.) bis 9 (exkl.)
		final float[] buffer = new float[5];
		final long start = 6;
		ringBuffer.peak(start, buffer, 0, 3);
		assertEquals(6f, buffer[0], COMPARISON_DELTA);
		assertEquals(7f, buffer[1], COMPARISON_DELTA);
		assertEquals(8f, buffer[2], COMPARISON_DELTA);
		// die restlichen Elemente von buffer wurden nicht überschrieben
		assertEquals(0f, buffer[3], COMPARISON_DELTA);
		assertEquals(0f, buffer[4], COMPARISON_DELTA);
	}

	@Test
	public void testPeakReadsTheRightElementsWithOffset() {
		for (int i = 0; i < 10; i++) {
			ringBuffer.put(i);
		}
		// Im RingBuffer stehen die Elemente 5, 6, 7, 8, 9

		// Lese von 6 (inkl.) bis 9 (exkl.) in Buffer ab offset 2
		final float[] buffer = new float[6];
		final long start = 6;
		ringBuffer.peak(start, buffer, 2, 3);
		assertEquals(6f, buffer[2], COMPARISON_DELTA);
		assertEquals(7f, buffer[3], COMPARISON_DELTA);
		assertEquals(8f, buffer[4], COMPARISON_DELTA);
		// die restlichen Elemente von buffer wurden nicht überschrieben
		assertEquals(0f, buffer[0], COMPARISON_DELTA);
		assertEquals(0f, buffer[1], COMPARISON_DELTA);
		assertEquals(0f, buffer[5], COMPARISON_DELTA);
	}

	@Test(expected = NoSuchElementException.class)
	public void testPeakLastCantReadMoreThanSizeElements() {
		fillRingBuffer(3);
		// Versuche die letzten 4 Elemente zu lesen, obwohl nur 3 vorhanden
		// sind.
		final float[] buffer = new float[5];
		ringBuffer.peakLast(buffer, 0, 4);
	}

	@Test(expected = NoSuchElementException.class)
	public void testPeakLastCantReadMoreThanCapacityElements() {
		fillRingBuffer(100);
		// Versuche die letzten 6 Elemente zu lesen, obwohl nur 5 vorhanden
		// sind.
		final float[] buffer = new float[6];
		ringBuffer.peakLast(buffer, 0, 6);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testPeakLastChecksBufferSize() {
		fillRingBuffer(5);

		final int bufferSize = 3;
		final float[] buffer = new float[bufferSize];
		final int offset = 2;
		final int length = 2;

		// Es soll buffer ab offset = 2 (inkl) bis (offset + length) = 4 (exkl.)
		// gefüllt werden. Buffer kann jedoch bis max. Index 2 gefüllt werden.
		ringBuffer.peakLast(buffer, offset, length);
	}

	@Test
	public void testPeakLastReadsTheRightElements() {
		for (int i = 0; i < 10; i++) {
			ringBuffer.put(i);
		}
		// Im RingBuffer stehen die Elemente 5, 6, 7, 8, 9

		// Lese die letzten 3 Elemente: 7, 8, 9
		final float[] buffer = new float[5];
		ringBuffer.peakLast(buffer, 0, 3);
		assertEquals(7f, buffer[0], COMPARISON_DELTA);
		assertEquals(8f, buffer[1], COMPARISON_DELTA);
		assertEquals(9f, buffer[2], COMPARISON_DELTA);
		// die restlichen Elemente von buffer wurden nicht überschrieben
		assertEquals(0f, buffer[3], COMPARISON_DELTA);
		assertEquals(0f, buffer[4], COMPARISON_DELTA);
	}

	@Test
	public void testPeakLastReadsTheRightElementsWithOffset() {
		for (int i = 0; i < 10; i++) {
			ringBuffer.put(i);
		}
		// Im RingBuffer stehen die Elemente 5, 6, 7, 8, 9

		// Lese die letzten 3 Elemente in buffer ab offset 2: 7, 8, 9
		final float[] buffer = new float[6];
		ringBuffer.peakLast(buffer, 2, 3);
		assertEquals(7f, buffer[2], COMPARISON_DELTA);
		assertEquals(8f, buffer[3], COMPARISON_DELTA);
		assertEquals(9f, buffer[4], COMPARISON_DELTA);
		// die restlichen Elemente von buffer wurden nicht überschrieben
		assertEquals(0f, buffer[0], COMPARISON_DELTA);
		assertEquals(0f, buffer[1], COMPARISON_DELTA);
		assertEquals(0f, buffer[5], COMPARISON_DELTA);
	}

	@Test(expected = NoSuchElementException.class)
	public void testPeakLastSingleChecksForEmptyness() {
		ringBuffer.peakLast();
	}

	@Test
	public void testPeakLastSingleReadsTheRightElement() {
		for (int i = 0; i < 5; i++) {
			ringBuffer.put(i);
		}
		// Im RingBuffer stehen die Elemente 0, 1, 2, 3, 4
		assertEquals(4f, ringBuffer.peakLast(), COMPARISON_DELTA);

		// Erzwinge Überlauf
		for (int i = 5; i < 100; i++) {
			ringBuffer.put(i);
		}
		// Im RingBuffer stehen die Elemente 95, 96, 97, 98, 99
		assertEquals(99f, ringBuffer.peakLast(), COMPARISON_DELTA);
	}

	@Test(expected = NoSuchElementException.class)
	public void testGetCantReadOlderElements() {
		fillRingBuffer(100);
		// RingBuffer kann von Index 95 (inkl.) bis 100 (exkl.) gelesen werden.
		// Der Zugriff auf Index 94 muss scheitern.
		ringBuffer.get(94);
	}

	@Test(expected = NoSuchElementException.class)
	public void testGetCantReadNewerElements() {
		fillRingBuffer(100);
		// RingBuffer kann von Index 95 (inkl.) bis 100 (exkl.) gelesen werden.
		// Der Zugriff auf Index 100 muss scheitern.
		ringBuffer.get(100);
	}

	@Test
	public void testGetReadsTheRightElement() {
		for (int i = 0; i < 5; i++) {
			ringBuffer.put(i);
		}
		// Im RingBuffer stehen die Elemente 0, 1, 2, 3, 4

		// Teste Element mit Index 2
		assertEquals(2, ringBuffer.get(2), COMPARISON_DELTA);

		// Erzeuge Überlauf
		ringBuffer.put(5);
		ringBuffer.put(6);
		// Im RingBuffer stehen die Elemente 2, 3, 4, 5, 6

		// Teste Element mit Index 5
		assertEquals(5, ringBuffer.get(5), COMPARISON_DELTA);
	}

	private void fillRingBuffer(final int numElements) {
		for (int i = 0; i < numElements; i++) {
			ringBuffer.put(1.2345f);
		}
	}
}