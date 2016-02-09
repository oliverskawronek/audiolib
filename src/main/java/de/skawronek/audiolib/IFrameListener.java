package de.skawronek.audiolib;

import org.eclipse.jdt.annotation.NonNull;

public interface IFrameListener {
	public void onFrameAvailable(final @NonNull Frame frame);
}
