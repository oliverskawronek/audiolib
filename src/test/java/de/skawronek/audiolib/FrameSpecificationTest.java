package de.skawronek.audiolib;

import org.junit.Test;

import static org.junit.Assert.*;

public final class FrameSpecificationTest {
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorChecksForNegativeSize() {
		new FrameSpecification(-1, 1024);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorChecksForZeroSize() {
		new FrameSpecification(0, 1024);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorChecksForNegativeHopSize() {
		new FrameSpecification(1024, -1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorChecksForZeroHopSize() {
		new FrameSpecification(1024, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFromRatioChecksForNegativeRatio() {
		FrameSpecification.fromRatio(4096, -1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFromRatioChecksForZeroRatio() {
		FrameSpecification.fromRatio(4096, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFromRatioChecksForRatioGreatherThanOne() {
		FrameSpecification.fromRatio(4096, 2);
	}

	@Test
	public void testFromRatio() {
		final int frameSize = 4096;
		final double ratio = 0.5;
		final int expectedHopSize = 2048;

		final FrameSpecification spec = FrameSpecification.fromRatio(frameSize,
				ratio);
		assertNotNull(spec);
		assertEquals(expectedHopSize, spec.getHopSize());
	}

	@Test
	public void testEquals() {
		//@formatter:off
		final FrameSpecification spec4096_2048A = new FrameSpecification(
				4096, 2048);
		final FrameSpecification spec4096_2048B = new FrameSpecification(
				4096, 2048);
		final FrameSpecification spec4096_1024 = new FrameSpecification(
				4096, 1024);
		final FrameSpecification spec2048_2048 = new FrameSpecification(
				2048, 2048);
		//@formatter:on

		assertEquals(spec4096_2048A, spec4096_2048A);
		assertEquals(spec4096_2048A, spec4096_2048B);
		assertEquals(spec4096_2048B, spec4096_2048A);
		assertNotEquals(spec4096_2048A, spec4096_1024);
		assertNotEquals(spec4096_2048A, spec2048_2048);
		assertNotEquals(spec4096_1024, spec2048_2048);

		assertNotEquals(spec4096_2048A, "A String");
		assertNotEquals(spec4096_2048A, null);
	}

	@Test
	public void testEqualFrameSpecificationsHaveEqualHashCodes() {
		//@formatter:off
		final FrameSpecification spec4096_2048A = new FrameSpecification(
				4096, 2048);
		final FrameSpecification spec4096_2048B = new FrameSpecification(
				4096, 2048);
		
		final FrameSpecification spec64_64A = new FrameSpecification(
				64, 64);
		final FrameSpecification spec64_64B = new FrameSpecification(
				64, 64);
		//@formatter:on

		assertEquals(spec4096_2048A.hashCode(), spec4096_2048B.hashCode());
		assertEquals(spec64_64A.hashCode(), spec64_64B.hashCode());
	}
}
