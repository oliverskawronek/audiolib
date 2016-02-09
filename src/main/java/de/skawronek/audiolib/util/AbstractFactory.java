package de.skawronek.audiolib.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

/**
 *
 * @param <K>
 *            Index Type
 * @param <V>
 *            Instance Type
 */
public abstract class AbstractFactory<K, V> {
	private final Map<K, V> instances = new HashMap<>();

	protected @NonNull V get(final @NonNull K index) {
		final boolean alreadyExists = instances.containsKey(index);
		if (!alreadyExists) {
			final V instance = create(index);
			instances.put(index, instance);
			return instance;
		} else {
			return instances.get(index);
		}
	}

	protected abstract @NonNull V create(final @NonNull K index);
}
