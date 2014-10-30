package org.bladerunnerjs.plugin.plugins.bundlesetobservers;

import org.bladerunnerjs.model.Asset;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.BundlableNode;
import org.bladerunnerjs.model.BundleSet;
import org.bladerunnerjs.plugin.BundlesetObserverPlugin;
import org.bladerunnerjs.plugin.base.AbstractPlugin;


public class BRJSUsageBundlesetObserver extends AbstractPlugin implements BundlesetObserverPlugin
{
	
	private BRJS brjs;
	private long lastCreationStartTime;

	@Override
	public void setBRJS(BRJS brjs)
	{
		this.brjs = brjs;
		lastCreationStartTime = 0;
	}

	@Override
	public void onBundlesetCreationStarted(BundlableNode bundlableNode)
	{
		lastCreationStartTime = System.currentTimeMillis();
	}
	
	@Override
	public void onBundlesetCreated(BundleSet bundleset)
	{
		System.err.println( UsageTrackingFirebasePayloadBuilder.bundlesetPayload(lastCreationStartTime, bundleset) );
	}

}
