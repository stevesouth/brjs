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
import org.bladerunnerjs.plugin.BundlesetObserverPlugin;
import org.bladerunnerjs.plugin.base.AbstractPlugin;
import org.eclipse.jetty.client.HttpClient;


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
		String jsonBlob = UsageTrackingFirebasePayloadBuilder.bundlesetPayload(lastCreationStartTime, bundleset);
		
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
		
	}

}
