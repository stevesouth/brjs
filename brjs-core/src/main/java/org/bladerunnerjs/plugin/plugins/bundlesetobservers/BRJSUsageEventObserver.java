package org.bladerunnerjs.plugin.plugins.bundlesetobservers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.bladerunnerjs.model.Asset;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.BundlableNode;
import org.bladerunnerjs.model.BundleSet;
import org.bladerunnerjs.model.engine.Node;
import org.bladerunnerjs.model.events.BundleSetCreatedEvent;
import org.bladerunnerjs.model.events.BundleSetCreationStartedEvent;
import org.bladerunnerjs.model.events.CommandExecutedEvent;
import org.bladerunnerjs.model.events.NewInstallEvent;
import org.bladerunnerjs.model.exception.ConfigException;
import org.bladerunnerjs.plugin.Event;
import org.bladerunnerjs.plugin.EventObserver;
import org.bladerunnerjs.plugin.ModelObserverPlugin;
import org.bladerunnerjs.plugin.base.AbstractModelObserverPlugin;
import org.bladerunnerjs.plugin.base.AbstractPlugin;
import org.eclipse.jetty.client.HttpClient;


public class BRJSUsageEventObserver extends AbstractModelObserverPlugin implements EventObserver
{
	
	private BRJS brjs;
	private long lastCreationStartTime;

	@Override
	public void setBRJS(BRJS brjs)
	{
		this.brjs = brjs;
		brjs.addObserver(this);
		lastCreationStartTime = 0;
	}
	
	
	@Override
	public void onEventEmitted(Event event, Node node)
	{
		if (node instanceof BRJS && node != null) {
			try
			{
				if (!((BRJS) node).bladerunnerConf().getAllowAnonymousStats()) {
					return;
				}
			}
			catch (Exception e)
			{
				return; // assume we dont want to track
			}
		}
		
		if (event instanceof BundleSetCreationStartedEvent) {
			lastCreationStartTime = System.currentTimeMillis();
		} else if (event instanceof BundleSetCreatedEvent) {
			BundleSetCreatedEvent bundleSetCreatedEvent = (BundleSetCreatedEvent) event;
			
			String jsonBlob = UsageTrackingFirebasePayloadBuilder.bundlesetPayload(lastCreationStartTime, bundleSetCreatedEvent.getBundleSet());
			
			HttpPost firebasePost = new HttpPost("https://brjs-usage-dashboard.firebaseio.com/bundlesets.json");
			try
			{
				firebasePost.setEntity( new StringEntity(jsonBlob) );
				DefaultHttpClient client = new DefaultHttpClient();
				client.execute(firebasePost);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		} else if (event instanceof CommandExecutedEvent) {
			CommandExecutedEvent commandExecutedEvent = (CommandExecutedEvent) event;
			
			String jsonBlob = UsageTrackingFirebasePayloadBuilder.commandPayload(brjs, commandExecutedEvent.getCommand());
			
			HttpPost firebasePost = new HttpPost("https://brjs-usage-dashboard.firebaseio.com/commands.json");
			try
			{
				firebasePost.setEntity( new StringEntity(jsonBlob) );
				DefaultHttpClient client = new DefaultHttpClient();
				client.execute(firebasePost);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
        } else if (event instanceof NewInstallEvent) {        	
        	String jsonBlob = UsageTrackingFirebasePayloadBuilder.newInstallPayload(brjs);
        	
        	HttpPost firebasePost = new HttpPost("https://brjs-usage-dashboard.firebaseio.com/installs.json");
        	try
        	{
        		firebasePost.setEntity( new StringEntity(jsonBlob) );
        		DefaultHttpClient client = new DefaultHttpClient();
        		client.execute(firebasePost);
        	}
        	catch (Exception e)
        	{
        		throw new RuntimeException(e);
        	}
    	} else if (event instanceof CommandExecutedEvent) {
    		CommandExecutedEvent commandExecutedEvent = (CommandExecutedEvent) event;
    		
    		String jsonBlob = UsageTrackingFirebasePayloadBuilder.commandPayload(brjs, commandExecutedEvent.getCommand());
    		
    		HttpPost firebasePost = new HttpPost("https://brjs-usage-dashboard.firebaseio.com/commands.json");
    		try
    		{
    			firebasePost.setEntity( new StringEntity(jsonBlob) );
    			DefaultHttpClient client = new DefaultHttpClient();
    			client.execute(firebasePost);
    		}
    		catch (Exception e)
    		{
    			throw new RuntimeException(e);
    		}
    	}
	}


}
