package org.bladerunnerjs.plugin.plugins.bundlers.commonjs;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.bladerunnerjs.memoization.Getter;
import org.bladerunnerjs.memoization.MemoizedValue;
import org.bladerunnerjs.model.Asset;
import org.bladerunnerjs.model.AssetFileInstantationException;
import org.bladerunnerjs.model.AssetLocation;
import org.bladerunnerjs.model.AssetLocationUtility;
import org.bladerunnerjs.model.AugmentedContentSourceModule;
import org.bladerunnerjs.model.BladerunnerConf;
import org.bladerunnerjs.model.BundlableNode;
import org.bladerunnerjs.model.SourceModulePatch;
import org.bladerunnerjs.model.exception.AmbiguousRequirePathException;
import org.bladerunnerjs.model.exception.ConfigException;
import org.bladerunnerjs.model.exception.ModelOperationException;
import org.bladerunnerjs.model.exception.RequirePathException;
import org.bladerunnerjs.model.exception.UnresolvableRequirePathException;
import org.bladerunnerjs.utility.PrimaryRequirePathUtility;
import org.bladerunnerjs.utility.RelativePathUtility;
import org.bladerunnerjs.utility.UnicodeReader;

import com.Ostermiller.util.ConcatReader;

public class CommonJsSourceModule implements AugmentedContentSourceModule {

	public static final String COMMONJS_DEFINE_BLOCK_HEADER = "define('%s', function(require, exports, module) {\n";
	public static final String COMMONJS_DEFINE_BLOCK_FOOTER = "\n});\n";

	private static final Pattern matcherPattern = Pattern.compile("(require|br\\.Core\\.alias|caplin\\.alias|getAlias|getService)\\([ ]*[\"']([^)]+)[\"'][ ]*\\)");
	
	private File assetFile;
	private AssetLocation assetLocation;
	
	private SourceModulePatch patch;
	
	private MemoizedValue<ComputedValue> computedValue;
	private List<String> requirePaths = new ArrayList<>();
	public static final String JS_STYLE = "common-js";
	
	public CommonJsSourceModule(File assetFile, AssetLocation assetLocation) throws AssetFileInstantationException {
		this.assetLocation = assetLocation;
		this.assetFile = assetFile;
		
		String requirePath = assetLocation.requirePrefix() + "/" + RelativePathUtility.get(assetLocation.root().getFileInfoAccessor(), assetLocation.dir(), assetFile).replaceAll("\\.js$", "");
		requirePaths.add(requirePath);
		
		patch = SourceModulePatch.getPatchForRequirePath(assetLocation, getPrimaryRequirePath());
		computedValue = new MemoizedValue<>(getAssetPath()+" - computedValue", assetLocation.root(), assetFile, patch.getPatchFile(), BladerunnerConf.getConfigFilePath(assetLocation.root()));
	}
	
	@Override
	public List<Asset> getDependentAssets(BundlableNode bundlableNode) throws ModelOperationException {
		List<Asset> dependendAssets = new ArrayList<>();
		dependendAssets.addAll( getPreExportDefineTimeDependentAssets(bundlableNode) );
		dependendAssets.addAll( getPostExportDefineTimeDependentAssets(bundlableNode) );
		dependendAssets.addAll( getUseTimeDependentAssets(bundlableNode) );
		return dependendAssets;
	}
	
	@Override
	public List<String> getRequirePaths() {
		return requirePaths;
	}
	
	
	@Override
	public List<String> getAliasNames() throws ModelOperationException {
		return getComputedValue().aliases;
	}
	
	@Override
	public Reader getUnalteredContentReader() throws IOException {
		try
		{
			String defaultFileCharacterEncoding = assetLocation.root().bladerunnerConf().getDefaultFileCharacterEncoding();
			Reader assetReader = new UnicodeReader(assetFile, defaultFileCharacterEncoding);
			if (patch.patchAvailable()){
				return new ConcatReader( new Reader[] { assetReader, patch.getReader() });
			} else {
				return assetReader;
			}
		}
		catch (ConfigException ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	@Override
	public Reader getReader() throws IOException {
		return new ConcatReader(new Reader[] {
			new StringReader( String.format(COMMONJS_DEFINE_BLOCK_HEADER, getPrimaryRequirePath()) ),
			getUnalteredContentReader(),
			new StringReader( COMMONJS_DEFINE_BLOCK_FOOTER )
		});
	}
	
	@Override
	public String getPrimaryRequirePath() {
		return PrimaryRequirePathUtility.getPrimaryRequirePath(this);
	}
	
	@Override
	public boolean isEncapsulatedModule() {
		return true;
	}
	
	@Override
	public boolean isGlobalisedModule() {
		return false;
	}
	
	@Override
	public List<Asset> getPreExportDefineTimeDependentAssets(BundlableNode bundlableNode) throws ModelOperationException {
		return getSourceModulesForRequirePaths( bundlableNode, getComputedValue().preExportDefineTimeRequirePaths );
	}
	
	@Override
	public List<Asset> getPostExportDefineTimeDependentAssets(BundlableNode bundlableNode) throws ModelOperationException {
		return getSourceModulesForRequirePaths( bundlableNode, getComputedValue().postExportDefineTimeRequirePaths );
	}
	
	@Override
	public List<Asset> getUseTimeDependentAssets(BundlableNode bundlableNode) throws ModelOperationException {
		return getSourceModulesForRequirePaths( bundlableNode, getComputedValue().useTimeRequirePaths );
	}
	
	@Override
	public File dir() {
		return assetFile.getParentFile();
	}
	
	@Override
	public String getAssetName() {
		return assetFile.getName();
	}
	
	@Override
	public String getAssetPath() {
		return RelativePathUtility.get(assetLocation.root().getFileInfoAccessor(), assetLocation.assetContainer().app().dir(), assetFile);
	}
	
	@Override
	public AssetLocation assetLocation()
	{
		return assetLocation;
	}
	
	@Override
	public List<AssetLocation> assetLocations() {
		return AssetLocationUtility.getAllDependentAssetLocations(assetLocation);
	}
	
	private ComputedValue getComputedValue() throws ModelOperationException {
		CommonJsSourceModule sourceModule = this;
		return computedValue.value(new Getter<ModelOperationException>() {
			@Override
			public Object get() throws ModelOperationException {
				ComputedValue computedValue = new ComputedValue();
				
				try {
					try(Reader reader = new CommonJsPreExportDefineTimeDependenciesReader(sourceModule)) 
					{
						addRequirePathsFromReader(reader, computedValue.preExportDefineTimeRequirePaths, computedValue.aliases);
					}
					
					try(Reader reader = new CommonJsPostExportDefineTimeDependenciesReader(sourceModule)) 
					{
						addRequirePathsFromReader(reader, computedValue.postExportDefineTimeRequirePaths, computedValue.aliases);
					}

					try(Reader reader = new CommonJsUseTimeDependenciesReader(sourceModule)) 
					{
						addRequirePathsFromReader(reader, computedValue.useTimeRequirePaths, computedValue.aliases);
					}
				}
				catch(IOException e) {
					throw new ModelOperationException(e);
				}
				
				return computedValue;
			}
		});
	}
	
	private void addRequirePathsFromReader(Reader reader, Set<String> dependencies, List<String> aliases) throws IOException {
		StringWriter stringWriter = new StringWriter();
		IOUtils.copy(reader, stringWriter);
		
		Matcher m = matcherPattern.matcher(stringWriter.toString());
		while (m.find()) {
			String methodArgument = m.group(2);
			
			if (m.group(1).startsWith("require")) {
				String requirePath = methodArgument;
				dependencies.add(requirePath);
			}
			else if (m.group(1).startsWith("getService")){
				String serviceAliasName = methodArgument;
				//TODO: this is a big hack, remove the "SERVICE!" part and the same in BundleSetBuilder
				aliases.add("SERVICE!"+serviceAliasName);
			}
			else {
				aliases.add(methodArgument);
			}
		}
	}

	private List<Asset> getSourceModulesForRequirePaths(BundlableNode bundlableNode, Set<String> requirePaths) throws ModelOperationException {
		try {
			return bundlableNode.getLinkedAssets( assetLocation, new ArrayList<>(requirePaths) );
		}
		catch (AmbiguousRequirePathException | UnresolvableRequirePathException e) {
            e.setSourceRequirePath(getPrimaryRequirePath());
            throw new ModelOperationException(e);
        }
        catch (RequirePathException e) {
            throw new ModelOperationException(e);
        }
	}
	
	private class ComputedValue {
		public Set<String> preExportDefineTimeRequirePaths = new HashSet<>();
		public Set<String> postExportDefineTimeRequirePaths = new HashSet<>();
		public Set<String> useTimeRequirePaths = new HashSet<>();
		public List<String> aliases = new ArrayList<>();
	}
	
}