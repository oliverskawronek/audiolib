package de.skawronek.audiolib;

import org.eclipse.jdt.annotation.NonNull;

public abstract class FeatureProcessor<F extends Feature> {
	public abstract @NonNull Class<F> getFeatureClass();

	public abstract F process(final @NonNull Frame frame,
			final @NonNull FeatureKey<F> key);
}
