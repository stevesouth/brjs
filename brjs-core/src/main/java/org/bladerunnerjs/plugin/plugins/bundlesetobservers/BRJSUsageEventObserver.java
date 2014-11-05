package org.bladerunnerjs.plugin.plugins.bundlesetobservers;

import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.engine.Node;
import org.bladerunnerjs.model.events.BundleSetCreatedEvent;
import org.bladerunnerjs.model.events.BundleSetCreationStartedEvent;
import org.bladerunnerjs.model.events.CommandExecutedEvent;
import org.bladerunnerjs.model.events.NewInstallEvent;
import org.bladerunnerjs.plugin.Event;
import org.bladerunnerjs.plugin.EventObserver;
import org.bladerunnerjs.plugin.base.AbstractModelObserverPlugin;

import com.google.gson.Gson;
import com.pusher.rest.Pusher;


public class BRJSUsageEventObserver extends AbstractModelObserverPlugin implements EventObserver
{
	
	private BRJS brjs;
	private long lastCreationStartTime;
	private static String KEENIO_PROJECT_ID = "5452adc733e406748303ecb4";
	private static String KEENIO_APP_KEY = "2788245138ec539bd1e5fbb7fbb9d015b700d0a1687eaca22cb60abf8ce9bcd3515d1a81b7400b0b3459ab796357dc8629192380b1fd9286667eb958eb68a2e3ae58ecaff480a7eca3a49f9bdad43c3f86cc6636ce5c43ac83038b867f91f7e4426c9006809a26113e2709d1f7fb524a";

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
		
		Thread t = new Thread(new EventHandlerThread(event, node));
		t.start();
		try
		{
			t.join();
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private class EventHandlerThread implements Runnable {

		private Event event;
		// TODO: get Pusher credentials from some sort of encrypted config
		private final Pusher pusher = new Pusher("95122", "054cce0687e2e37c48df", "1851e96ce02c4d928bb0");

		EventHandlerThread(Event event, Node node) {
			this.event = event;
		}
		
		@Override
		public void run()
		{
			if (event instanceof BundleSetCreationStartedEvent) {
				lastCreationStartTime = System.currentTimeMillis();
			} else if (event instanceof BundleSetCreatedEvent) {
				BundleSetCreatedEvent bundleSetCreatedEvent = (BundleSetCreatedEvent) event;
				
				Map<String,Object> eventData = UsageTrackingRestPayloadBuilder.bundlesetPayload(lastCreationStartTime, bundleSetCreatedEvent.getBundleSet());
				
				trackEvent(eventData, "bundlesets");
			}
			else if (event instanceof CommandExecutedEvent) {
				CommandExecutedEvent commandExecutedEvent = (CommandExecutedEvent) event;
				
				Map<String,Object> eventData = UsageTrackingRestPayloadBuilder.commandPayload(brjs, commandExecutedEvent.getCommand());
				trackEvent(eventData, "commands");
	        }
			else if (event instanceof NewInstallEvent) {        	
	        	Map<String,Object> eventData = UsageTrackingRestPayloadBuilder.newInstallPayload(brjs);
	        	trackEvent(eventData, "installs");
	    	}
		}

		private void trackEvent(Map<String,Object> eventData, String eventType) {
			try
			{
				String jsonBlob = new Gson().toJson(eventData);
				HttpClient client = HttpClientBuilder.create().build();
				HttpPost keenIOPost = new HttpPost("https://api.keen.io/3.0/projects/" + KEENIO_PROJECT_ID + "/events/" + eventType + "?api_key=" + KEENIO_APP_KEY);
				keenIOPost.setHeader("Content-Type", "application/json");
				keenIOPost.setEntity( new StringEntity(jsonBlob) );
				client.execute(keenIOPost);
				
				pusher.trigger(eventType, "new-stat", eventData);
			}
			catch (Exception e)
			{
				brjs.logger(this.getClass()).error(e.toString());
			}
		}
		
	}
	

}
