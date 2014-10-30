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
	private static Map<String,Object> getBasicVersionInfo(BRJS brjs) {
		Map<String,Object> versionInfo = new HashMap<>();
		versionInfo.put("brjs_version", brjs.versionInfo().getVersionNumber());
		versionInfo.put("toolkit_version", brjs.root().versionInfo().getVersionNumber());
		versionInfo.put("toolkit_name", "BladeRunnerJS");
		for (JsLib lib : brjs.sdkLibs()) {
			if (lib.getName().toLowerCase().startsWith("ct-")) {
				versionInfo.put("toolkit_name", "CT");
				break;
			}
		}
		versionInfo.put("timestamp", System.currentTimeMillis());
		return versionInfo;
	}
	
	public static String bundlesetPayload(long bundlesetStartTime, BundleSet bundleset)
	{
		Map<String,Object> jsonMap = getBasicVersionInfo(bundleset.getBundlableNode().root());
		
		Map<String,Object> fileCounts = new HashMap<>();
		fileCounts.put("total_count", bundleset.getResourceFiles().size());
		jsonMap.put("file_count", fileCounts);
		
		long currentTimeMillis = System.currentTimeMillis();
		jsonMap.put("execution_duration", currentTimeMillis - bundlesetStartTime);
		jsonMap.put("bundlable_node_type", bundleset.getBundlableNode().getClass().getSimpleName());
		
		return new Gson().toJson(jsonMap);
	}

	public static String commandPayload(BRJS brjs, String command)
	{
		Map<String,Object> jsonMap = getBasicVersionInfo(brjs);
		
		jsonMap.put("command_name", command);
		
//		long currentTimeMillis = System.currentTimeMillis();
//		jsonMap.put("execution_duration", currentTimeMillis - bundlesetStartTime);
		
		return new Gson().toJson(jsonMap);
	}

	public static String newInstallPayload(BRJS brjs)
	{
		Map<String,Object> jsonMap = getBasicVersionInfo(brjs);
		
		jsonMap.put("os_arch", System.getProperty("os.name"));
		jsonMap.put("os_name", System.getProperty("os.name"));
		jsonMap.put("os_version", System.getProperty("os.version"));
		jsonMap.put("java_vendor", System.getProperty("java.vendor"));
		jsonMap.put("java_version", System.getProperty("java.version"));
		
//		long currentTimeMillis = System.currentTimeMillis();
//		jsonMap.put("execution_duration", currentTimeMillis - bundlesetStartTime);
		
		return new Gson().toJson(jsonMap);
	}

}
