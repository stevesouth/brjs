package org.bladerunnerjs.model.events;

import org.bladerunnerjs.model.BundlableNode;
import org.bladerunnerjs.plugin.Event;


public class BundleSetCreationStartedEvent implements Event
{
	private BundlableNode bundlableNode;
	private long startTime;

	public BundleSetCreationStartedEvent(BundlableNode bundlableNode) {
		this.bundlableNode = bundlableNode;
	}
	
	public BundlableNode getBundlableNode() {
		return bundlableNode;
	}
}
