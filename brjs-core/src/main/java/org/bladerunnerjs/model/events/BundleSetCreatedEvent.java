package org.bladerunnerjs.model.events;

import org.bladerunnerjs.model.BundlableNode;
import org.bladerunnerjs.model.BundleSet;
import org.bladerunnerjs.plugin.Event;


public class BundleSetCreatedEvent implements Event
{
	private BundleSet bundleSet;

	public BundleSetCreatedEvent(BundleSet bundleSet) {
		this.bundleSet = bundleSet;
	}
	
	public BundleSet getBundleSet() {
		return bundleSet;
	}
}
