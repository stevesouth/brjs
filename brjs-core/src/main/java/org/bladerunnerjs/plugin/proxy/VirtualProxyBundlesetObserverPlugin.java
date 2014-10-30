package org.bladerunnerjs.plugin.proxy;

import org.bladerunnerjs.model.BundleSet;
import org.bladerunnerjs.plugin.BundlesetObserverPlugin;


public class VirtualProxyBundlesetObserverPlugin extends VirtualProxyPlugin implements BundlesetObserverPlugin
{

	private BundlesetObserverPlugin wrappedPlugin;

	public VirtualProxyBundlesetObserverPlugin(BundlesetObserverPlugin plugin)
	{
		super(plugin);
		this.wrappedPlugin = plugin;
	}

	@Override
	public void onBundlesetCreated(BundleSet bundleset)
	{
		initializePlugin();
		wrappedPlugin.onBundlesetCreated(bundleset);
	}

}
