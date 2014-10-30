package org.bladerunnerjs.plugin;

import org.bladerunnerjs.model.BundlableNode;
import org.bladerunnerjs.model.BundleSet;

public interface BundlesetObserverPlugin extends Plugin
{
	public void onBundlesetCreationStarted(BundlableNode bundlableNode);
	public void onBundlesetCreated(BundleSet bundleset);
}
