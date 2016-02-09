package de.skawronek.audiolib;

import static org.junit.Assert.*;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;

import de.skawronek.audiolib.FooFeature.FooKey;
import de.skawronek.audiolib.FooFeature.FooProcessor;

public final class FeatureFactoryTest {
	@Test(expected = IllegalStateException.class)
	public void testCreateFeatureThrowsIllegalStateExceptionIfProcessorIsNotRegistered() {
		final FooKey key = new FooKey();
		FeatureFactory.getInstance().createFeature(createFrame(), key);
	}

	@Test
	public void testCreateFeatureWithRegisteredProcessor() {
		FeatureFactory.getInstance().registerProcessor(new FooProcessor());
		final Frame frame = createFrame();
		final FooKey key = new FooKey();
		FeatureFactory.getInstance().createFeature(frame, key);
	}

	@Test
	public void testRemoveProcessor() {
		final FooProcessor processor = new FooProcessor();
		FeatureFactory.getInstance().registerProcessor(processor);
		final Frame frame = createFrame();
		final FooKey key = new FooKey();
		FeatureFactory.getInstance().removeProcessor(FooFeature.class);
		// Processor ist nicht mehr registriert
		try {
			FeatureFactory.getInstance().createFeature(frame, key);
			fail();
		} catch (final IllegalStateException e) {
			// Exception wird erwartet, weil Processor nicht mehr registriert
			// ist
		}
	}

	private static @NonNull Frame createFrame() {
		final float[] samples = new float[2048];
		return Frame.createMonoFrame(0, 44100.0, samples);
	}
}
