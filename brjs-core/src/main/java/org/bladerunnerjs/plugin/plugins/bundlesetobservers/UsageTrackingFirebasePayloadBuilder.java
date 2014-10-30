package org.bladerunnerjs.plugin.plugins.bundlesetobservers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bladerunnerjs.model.App;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.BundleSet;
import org.bladerunnerjs.model.JsLib;

import com.google.gson.Gson;


public class UsageTrackingFirebasePayloadBuilder
{

	//TODO: calcuate toolkit version if we're using CT
	private static Map<String,Object> getBasicVersionInfo(App app) {
		Map<String,Object> versionInfo = new HashMap<>();
		versionInfo.put("brjs_version", app.root().versionInfo().getVersionNumber());
		versionInfo.put("toolkit_version", app.root().versionInfo().getVersionNumber());
		versionInfo.put("toolkit_name", "BladeRunnerJS");
		for (JsLib lib : app.jsLibs()) {
			if (lib.getName().toLowerCase().startsWith("ct-")) {
				versionInfo.put("toolkit_name", "CT");
			}
		}
		return versionInfo;
	}
	
	public static String bundlesetPayload(long bundlesetStartTime, BundleSet bundleset)
	{
		Map<String,Object> jsonMap = getBasicVersionInfo(bundleset.getBundlableNode().app());
		
		Map<String,Object> fileCounts = new HashMap<>();
		fileCounts.put("total_count", bundleset.getResourceFiles().size());
		jsonMap.put("file_count", fileCounts);
		
		long currentTimeMillis = System.currentTimeMillis();
		jsonMap.put("execution_duration", currentTimeMillis - bundlesetStartTime);
		jsonMap.put("timestamp", currentTimeMillis);
		
		return new Gson().toJson(jsonMap);
	}

}
