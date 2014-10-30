package org.bladerunnerjs.plugin;

import org.bladerunnerjs.model.BundleSet;

public interface BundlesetObserverPlugin extends Plugin
{
	public void onBundlesetCreated(BundleSet bundleset);
}
