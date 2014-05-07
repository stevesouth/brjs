package org.bladerunnerjs.plugin.plugins.brjsconformant;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bladerunnerjs.model.AbstractResourcesAssetLocation;
import org.bladerunnerjs.model.AssetContainer;
import org.bladerunnerjs.model.AssetLocation;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.BundlableNode;
import org.bladerunnerjs.model.ChildSourceAssetLocation;
import org.bladerunnerjs.model.ChildTestSourceAssetLocation;
import org.bladerunnerjs.model.ResourcesAssetLocation;
import org.bladerunnerjs.model.SourceAssetLocation;
import org.bladerunnerjs.model.TestSourceAssetLocation;
import org.bladerunnerjs.model.ThemesAssetLocation;
import org.bladerunnerjs.model.Workbench;
import org.bladerunnerjs.model.WorkbenchResourcesAssetLocation;
import org.bladerunnerjs.plugin.base.AbstractAssetLocationPlugin;
import org.bladerunnerjs.utility.RelativePathUtility;

public class BRJSConformantAssetLocationPlugin extends AbstractAssetLocationPlugin {
	
	private BRJS brjs;

	public static List<String> getBundlableNodeThemes(BundlableNode bundlableNode) {
		Set<String> themeNames = new HashSet<>();
		
		for(AssetContainer assetContainer : bundlableNode.assetContainers()) {
			AbstractResourcesAssetLocation resourceAssetLocation = (AbstractResourcesAssetLocation) assetContainer.assetLocation("resources");
			
			if(resourceAssetLocation != null) {
				for(ThemesAssetLocation themeAssetLocation : resourceAssetLocation.themes()) {
					String themeName = themeAssetLocation.getThemeName();
					
					if(!themeName.equals("common")) {
						themeNames.add(themeName);
					}
					
				}
			}
		}
		
		List<String> themeNamesList = new ArrayList<>();
		themeNamesList.add("common");
		themeNamesList.addAll(themeNames);
		
		return themeNamesList;
	}
	
	@Override
	public void setBRJS(BRJS brjs) {
		this.brjs = brjs;
	}
	
	@Override
	public List<String> getPluginsThatMustAppearBeforeThisPlugin() {
		return new ArrayList<>();
	}
	
	@Override
	public List<String> getPluginsThatMustAppearAfterThisPlugin() {
		return new ArrayList<>();
	}
	
	public List<String> getAssetLocationDirectories(AssetContainer assetContainer) {
		List<String> assetLocationDirectories = new ArrayList<>();
		
		assetLocationDirectories.add("");
		assetLocationDirectories.add("resources");
		assetLocationDirectories.add("src");
		assetLocationDirectories.add("src-test");
		
		File sourceDir = assetContainer.file("src");
		if(sourceDir.exists()) {
			for(File dir : brjs.getFileInfo(sourceDir).nestedDirs()) {
				assetLocationDirectories.add(RelativePathUtility.get(assetContainer.dir(), dir));
			}
		}
		
		File sourceTestDir = assetContainer.file("src-test");
		if(sourceTestDir.exists()) {
			for(File dir : brjs.getFileInfo(sourceTestDir).nestedDirs()) {
				assetLocationDirectories.add(RelativePathUtility.get(assetContainer.dir(), dir));
			}
		}
		
		return assetLocationDirectories;
	}
	
	public List<String> getSeedAssetLocationDirectories(AssetContainer assetContainer) {
		return new ArrayList<>();
	}
	
	public AssetLocation createAssetLocation(AssetContainer assetContainer, String dirPath, Map<String, AssetLocation> assetLocationsMap) {
		AssetLocation assetLocation;
		File dir = assetContainer.file(dirPath);
		
		switch(dirPath) {
			case "":
				assetLocation = new BRJSConformantRootAssetLocation(assetContainer.root(), assetContainer, dir);
				break;
			
			case "resources":
				if (assetContainer instanceof Workbench) {
					assetLocation = new WorkbenchResourcesAssetLocation(assetContainer.root(), assetContainer, dir);
				}
				else {
					assetLocation = new ResourcesAssetLocation(assetContainer.root(), assetContainer, dir);
				}
				break;
			
			case "src":
				assetLocation = new SourceAssetLocation(assetContainer.root(), assetContainer, dir, assetLocationsMap.get("resources"));
				break;
			
			case "src-test":
				assetLocation = new TestSourceAssetLocation(assetContainer.root(), assetContainer, dir);
				break;
			
			default:
				String parentLocationPath = RelativePathUtility.get(assetContainer.dir(), dir.getParentFile());
				AssetLocation parentAssetLocation = assetLocationsMap.get(parentLocationPath);
				
				if((parentAssetLocation instanceof ChildSourceAssetLocation) || (parentAssetLocation instanceof SourceAssetLocation)) {
					assetLocation = new ChildSourceAssetLocation(assetContainer.root(), assetContainer, dir, parentAssetLocation);
				}
				else {
					assetLocation = new ChildTestSourceAssetLocation(assetContainer.root(), assetContainer, dir, parentAssetLocation);
				}
				break;
		}
		
		return assetLocation;
	}
	
	@Override
	public boolean allowFurtherProcessing() {
		return false;
	}
}
