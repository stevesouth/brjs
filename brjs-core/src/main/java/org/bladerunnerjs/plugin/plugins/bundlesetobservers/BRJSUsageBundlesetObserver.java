package org.bladerunnerjs.plugin.plugins.bundlesetobservers;

import org.bladerunnerjs.model.Asset;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.BundleSet;
import org.bladerunnerjs.plugin.BundlesetObserverPlugin;
import org.bladerunnerjs.plugin.base.AbstractPlugin;


public class BRJSUsageBundlesetObserver extends AbstractPlugin implements BundlesetObserverPlugin
{
	
	private BRJS brjs;

	@Override
	public void setBRJS(BRJS brjs)
	{
		this.brjs = brjs;		
	}

	@Override
	public void onBundlesetCreated(BundleSet bundleset)
	{
		int fileCount = bundleset.getResourceFiles().size();
		System.err.println(fileCount);
	}

}
