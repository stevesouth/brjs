package org.bladerunnerjs.model;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.bladerunnerjs.model.engine.Node;
import org.bladerunnerjs.model.engine.RootNode;
import org.bladerunnerjs.model.exception.ModelOperationException;
import org.bladerunnerjs.plugin.Locale;
import org.bladerunnerjs.utility.NoTagHandlerFoundException;
import org.bladerunnerjs.utility.TagPluginUtility;

public abstract class AbstractBrowsableNode extends AbstractBundlableNode implements BrowsableNode {
	public AbstractBrowsableNode(RootNode rootNode, Node parent, File dir) {
		super(rootNode, parent, dir);
	}
	
	@Override
	public void filterIndexPage(String indexPage, Locale locale, String version, Writer writer, RequestMode requestMode) throws ModelOperationException {
		try {
			TagPluginUtility.filterContent(indexPage, getBundleSet(), writer, requestMode, locale, version);
		}
		catch (IOException | NoTagHandlerFoundException e) {
			throw new ModelOperationException(e);
		}
	}
}
