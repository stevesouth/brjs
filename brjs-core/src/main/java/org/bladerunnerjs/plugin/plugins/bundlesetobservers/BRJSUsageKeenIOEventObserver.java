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


public class BRJSUsageKeenIOEventObserver extends AbstractModelObserverPlugin implements EventObserver
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
			
			String jsonBlob = UsageTrackingRestPayloadBuilder.bundlesetPayload(lastCreationStartTime, bundleSetCreatedEvent.getBundleSet());
			
			HttpPost firebasePost = new HttpPost("https://api.keen.io/3.0/projects/5452adc733e406748303ecb4/events/bundlesets?api_key=2788245138ec539bd1e5fbb7fbb9d015b700d0a1687eaca22cb60abf8ce9bcd3515d1a81b7400b0b3459ab796357dc8629192380b1fd9286667eb958eb68a2e3ae58ecaff480a7eca3a49f9bdad43c3f86cc6636ce5c43ac83038b867f91f7e4426c9006809a26113e2709d1f7fb524a");
			firebasePost.setHeader("Content-Type", "application/json");
			try
			{
				firebasePost.setEntity( new StringEntity(jsonBlob) );
				DefaultHttpClient client = new DefaultHttpClient();
				client.execute(firebasePost);
			}
			catch (Exception e)
			{
				brjs.logger(this.getClass()).error(e.toString());
				throw new RuntimeException(e);
			}
		} else if (event instanceof CommandExecutedEvent) {
			CommandExecutedEvent commandExecutedEvent = (CommandExecutedEvent) event;
			
			String jsonBlob = UsageTrackingRestPayloadBuilder.commandPayload(brjs, commandExecutedEvent.getCommand());
			
			HttpPost firebasePost = new HttpPost("https://api.keen.io/3.0/projects/5452adc733e406748303ecb4/events/commands?api_key=2788245138ec539bd1e5fbb7fbb9d015b700d0a1687eaca22cb60abf8ce9bcd3515d1a81b7400b0b3459ab796357dc8629192380b1fd9286667eb958eb68a2e3ae58ecaff480a7eca3a49f9bdad43c3f86cc6636ce5c43ac83038b867f91f7e4426c9006809a26113e2709d1f7fb524a");
			firebasePost.setHeader("Content-Type", "application/json");
			try
			{
				firebasePost.setEntity( new StringEntity(jsonBlob) );
				DefaultHttpClient client = new DefaultHttpClient();
				client.execute(firebasePost);
			}
			catch (Exception e)
			{
				brjs.logger(this.getClass()).error(e.toString());
				throw new RuntimeException(e);
			}
        } else if (event instanceof NewInstallEvent) {        	
        	String jsonBlob = UsageTrackingRestPayloadBuilder.newInstallPayload(brjs);
        	
        	HttpPost firebasePost = new HttpPost("https://api.keen.io/3.0/projects/5452adc733e406748303ecb4/events/installs?api_key=2788245138ec539bd1e5fbb7fbb9d015b700d0a1687eaca22cb60abf8ce9bcd3515d1a81b7400b0b3459ab796357dc8629192380b1fd9286667eb958eb68a2e3ae58ecaff480a7eca3a49f9bdad43c3f86cc6636ce5c43ac83038b867f91f7e4426c9006809a26113e2709d1f7fb524a");
        	firebasePost.setHeader("Content-Type", "application/json");
        	try
        	{
        		firebasePost.setEntity( new StringEntity(jsonBlob) );
        		DefaultHttpClient client = new DefaultHttpClient();
        		client.execute(firebasePost);
        	}
        	catch (Exception e)
        	{
        		brjs.logger(this.getClass()).error(e.toString());
        		throw new RuntimeException(e);
        	}
    	}
	}


}