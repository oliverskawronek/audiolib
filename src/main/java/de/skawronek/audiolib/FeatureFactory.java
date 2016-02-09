package de.skawronek.audiolib;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

public final class FeatureFactory {
	private static final FeatureFactory INSTANCE = new FeatureFactory();

	private final Map<Class<?>, FeatureProcessor<?>> processors = new HashMap<>();

	public static FeatureFactory getInstance() {
		return INSTANCE;
	}

	public <F extends Feature> F createFeature(final @NonNull Frame frame,
			final @NonNull FeatureKey<F> key) {
		@SuppressWarnings("unchecked")
		FeatureProcessor<F> processor = (FeatureProcessor<F>) processors
				.get(key.getFeatureClass());
		if (processor == null) {
			throw new IllegalStateException(
					"Processor not registered for feature "
							+ key.getFeatureClass().getSimpleName());
		}
		return processor.process(frame, key);
	}

	public <F extends Feature> void registerProcessor(
			final @NonNull FeatureProcessor<F> processor) {
		final Class<F> featureClass = processor.getFeatureClass();
		processors.put(featureClass, processor);
	}

	public <F extends Feature> void removeProcessor(
			final @NonNull Class<F> featureClass) {
		processors.remove(featureClass);
	}
}
