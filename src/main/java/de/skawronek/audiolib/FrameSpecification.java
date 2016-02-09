package de.skawronek.audiolib;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.util.Util;

public final class FrameSpecification {
	private final int size;
	private final int hopSize;

	public FrameSpecification(final int size, final int hopSize) {
		if (size <= 0) {
			throw new IllegalArgumentException("size " + size + " <= 0");
		} else if (hopSize <= 0) {
			throw new IllegalArgumentException("hopSize " + hopSize + " <= 0");
		}

		this.size = size;
		this.hopSize = hopSize;
	}

	public int getSize() {
		return size;
	}

	public int getHopSize() {
		return hopSize;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		} else if (obj == this) {
			return true;
		} else if (obj instanceof FrameSpecification) {
			final FrameSpecification other = (FrameSpecification) obj;
			return this.size == other.size && this.hopSize == other.hopSize;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return (size << 16) ^ (hopSize);
	}

	@NonNull
	public static FrameSpecification fromRatio(final int size,
			final double ratio) {
		if (ratio <= 0 || ratio > 1) {
			throw new IllegalArgumentException("ratio " + ratio
					+ " is out of range: 0 < ratio <= 1");
		}

		final int hopSize = Util.clamp((int) Math.round(size * ratio), 1, size);
		return new FrameSpecification(size, hopSize);
	}
}
