package de.skawronek.audiolib;

import org.eclipse.jdt.annotation.NonNull;

public abstract class FeatureKey<F extends Feature> {
	public abstract @NonNull Class<F> getFeatureClass();

	@Override
	public abstract boolean equals(final Object obj);

	@Override
	public abstract int hashCode();
}