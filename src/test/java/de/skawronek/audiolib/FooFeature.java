package de.skawronek.audiolib;

import org.eclipse.jdt.annotation.NonNull;

final class FooFeature extends Feature {
	static final class FooProcessor extends FeatureProcessor<FooFeature> {
		@Override
		public @NonNull Class<FooFeature> getFeatureClass() {
			return FooFeature.class;
		}

		@Override
		public FooFeature process(@NonNull final Frame frame,
				@NonNull final FeatureKey<FooFeature> key) {
			return new FooFeature();
		}
	}

	static final class FooKey extends FeatureKey<FooFeature> {
		@Override
		public @NonNull Class<FooFeature> getFeatureClass() {
			return FooFeature.class;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null) {
				return false;
			} else if (obj == this) {
				return true;
			} else if (obj instanceof FooKey) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return 0;
		}
	}
}