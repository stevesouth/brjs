package org.bladerunnerjs.testing.utility;

import java.util.ArrayList;
import java.util.List;

import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.BundleSet;
import org.bladerunnerjs.model.UrlContentAccessor;
import org.bladerunnerjs.model.ParsedContentPath;
import org.bladerunnerjs.model.exception.request.ContentProcessingException;
import org.bladerunnerjs.plugin.CharResponseContent;
import org.bladerunnerjs.plugin.ResponseContent;
import org.bladerunnerjs.plugin.Locale;
import org.bladerunnerjs.plugin.base.AbstractContentPlugin;
import org.bladerunnerjs.utility.ContentPathParser;
import org.bladerunnerjs.utility.ContentPathParserBuilder;


public class ScriptedContentPlugin extends AbstractContentPlugin
{
	private ContentPathParser contentPathParser;
	private List<String> requestPaths = new ArrayList<>();
	private boolean outputAllBundles;
	
	public ScriptedContentPlugin(boolean outputAllBundles, String... urls) {
		this.outputAllBundles = outputAllBundles;
		ContentPathParserBuilder contentPathParserBuilder = new ContentPathParserBuilder();
		for (String url : urls) {
    		contentPathParserBuilder.accepts(url).as(url);
			requestPaths.add(url);
		}
		contentPathParser = contentPathParserBuilder.build();
	}
	
	@Override
	public void setBRJS(BRJS brjs)
	{
	}
	
	@Override
	public String getRequestPrefix() {
		return "ScriptedContentPlugin";
	}

	@Override
	public String getCompositeGroupName() {
		return null;
	}
	
	@Override
	public List<String> getPluginsThatMustAppearBeforeThisPlugin() {
		return new ArrayList<>();
	}
	
	@Override
	public List<String> getPluginsThatMustAppearAfterThisPlugin() {
		return new ArrayList<>();
	}
	
	@Override
	public ContentPathParser getContentPathParser()
	{
		return contentPathParser;
	}

	@Override
	public ResponseContent handleRequest(ParsedContentPath contentPath, BundleSet bundleSet, UrlContentAccessor contentAccessor, String version) throws ContentProcessingException
	{
		return new CharResponseContent( bundleSet.getBundlableNode().root(), this.getClass().getCanonicalName() );
	}

	@Override
	public List<String> getValidDevContentPaths(BundleSet bundleSet, Locale... locales) throws ContentProcessingException
	{
		return requestPaths;
	}

	@Override
	public List<String> getValidProdContentPaths(BundleSet bundleSet, Locale... locales) throws ContentProcessingException
	{
		return requestPaths;
	}
	
	@Override
	public boolean outputAllBundles()
	{
		return outputAllBundles;
	}

}
