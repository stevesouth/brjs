package org.bladerunnerjs.plugin.proxy;

import java.util.List;

import org.bladerunnerjs.model.BundleSet;
import org.bladerunnerjs.model.UrlContentAccessor;
import org.bladerunnerjs.model.ParsedContentPath;
import org.bladerunnerjs.model.exception.request.ContentProcessingException;
import org.bladerunnerjs.plugin.ResponseContent;
import org.bladerunnerjs.plugin.ContentPlugin;
import org.bladerunnerjs.plugin.Locale;
import org.bladerunnerjs.utility.ContentPathParser;

public class VirtualProxyContentPlugin extends VirtualProxyPlugin implements ContentPlugin {
	private ContentPlugin contentPlugin;
	
	public VirtualProxyContentPlugin(ContentPlugin contentPlugin) {
		super(contentPlugin);
		this.contentPlugin = contentPlugin;
	}
	
	@Override
	public String getRequestPrefix() {
		return contentPlugin.getRequestPrefix();
	}
	
	@Override
	public String getCompositeGroupName() {
		return contentPlugin.getCompositeGroupName();
	}
	
	@Override
	public List<String> getPluginsThatMustAppearBeforeThisPlugin() {
		return contentPlugin.getPluginsThatMustAppearBeforeThisPlugin();
	}
	
	@Override
	public List<String> getPluginsThatMustAppearAfterThisPlugin() {
		return contentPlugin.getPluginsThatMustAppearAfterThisPlugin();
	}
	
	@Override
	public ContentPathParser getContentPathParser() {
		initializePlugin();
		return contentPlugin.getContentPathParser();
	}
	
	@Override
	public ResponseContent handleRequest(ParsedContentPath contentPath, BundleSet bundleSet, UrlContentAccessor contentAccessor, String version) throws ContentProcessingException {
		initializePlugin();
		return contentPlugin.handleRequest(contentPath, bundleSet, contentAccessor, version);
	}

	@Override
	public List<String> getValidDevContentPaths(BundleSet bundleSet, Locale... locales) throws ContentProcessingException
	{
		initializePlugin();
		return contentPlugin.getValidDevContentPaths(bundleSet, locales);
	}

	@Override
	public List<String> getValidProdContentPaths(BundleSet bundleSet, Locale... locales) throws ContentProcessingException
	{
		initializePlugin();
		return contentPlugin.getValidProdContentPaths(bundleSet, locales);
	}

	@Override
	public boolean outputAllBundles()
	{
		return contentPlugin.outputAllBundles();
	}
	
}
