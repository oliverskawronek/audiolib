package de.skawronek.audiolib.util;

import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.NonNull;

public final class FloatRingBuffer {
	private final int capacity;
	private int size = 0;
	private long count = 0;
	private final float[] elements;
	private int next = 0;

	public FloatRingBuffer(final float @NonNull [] elements) {
		if (elements.length == 0) {
			throw new IllegalArgumentException("elements is empty");
		}
		this.capacity = elements.length;
		this.elements = elements;
	}

	public FloatRingBuffer(final int capacity) {
		if (capacity <= 0) {
			throw new IllegalArgumentException("capacity " + capacity + " <= 0");
		}
		this.capacity = capacity;
		this.elements = new float[capacity];
	}

	public void put(final float f) {
		elements[next] = f;
		next = (next + 1) % capacity;
		size = (size == capacity ? size : size + 1);
		count++;
	}

	public void peak(final long start, final float @NonNull [] buffer,
			final int offset, final int length) {
		if (length == 0) {
			return;
		} else if (buffer.length < offset + length) {
			throw new IndexOutOfBoundsException("try to read " + length
					+ " elements at " + offset + ", but buffer size is "
					+ buffer.length);
		} else if (start >= count) {
			throw new NoSuchElementException("try to read at " + start
					+ ", but count is " + count);
		} else if (start < count - size) {
			throw new NoSuchElementException("try to read at " + start
					+ ", but oldest available is at " + (count - size));
		} else if (start + length > count) {
			throw new NoSuchElementException("try to read " + length + " at "
					+ start + ", but count is " + count);
		}

		int startIdx = (int) (next - (count - start)) % capacity;
		if (startIdx < 0) {
			startIdx += capacity;
		}
		for (int i = 0; i < length; i++) {
			final int j = (startIdx + i) % capacity;
			buffer[i + offset] = this.elements[j];
		}
	}

	public void peakLast(final float @NonNull [] buffer, final int offset,
			final int length) {
		final long start = count - length;
		peak(start, buffer, offset, length);
	}

	public float peakLast() {
		if (size == 0) {
			throw new NoSuchElementException("size is 0");
		}

		int index = (next - 1) % capacity;
		if (index < 0) {
			index += capacity;
		}

		return elements[index];
	}

	public float get(final long start) {
		if (start >= count) {
			throw new NoSuchElementException("try to read at " + start
					+ ", but count is " + count);
		} else if (start < count - size) {
			throw new NoSuchElementException("try to read at " + start
					+ ", but oldest available is at " + (count - size));
		}

		int startIdx = (int) (next - (count - start)) % capacity;
		if (startIdx < 0) {
			startIdx += capacity;
		}
		return elements[startIdx];
	}

	public int getCapacity() {
		return capacity;
	}

	public int getSize() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public long getCount() {
		return count;
	}

	public void clear() {
		next = 0;
		size = 0;
		count = 0;
	}
}
