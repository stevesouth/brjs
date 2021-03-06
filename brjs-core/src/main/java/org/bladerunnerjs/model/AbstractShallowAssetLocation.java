package org.bladerunnerjs.model;

import java.io.File;
import java.util.List;

import org.bladerunnerjs.model.engine.RootNode;

public abstract class AbstractShallowAssetLocation extends TheAbstractAssetLocation {
	public AbstractShallowAssetLocation(RootNode rootNode, AssetContainer assetContainer, File dir, AssetLocation parentAssetLocation, AssetLocation... dependentAssetLocations) {
		super(rootNode, assetContainer, dir, parentAssetLocation, dependentAssetLocations);
	}
	
	@Override
	protected List<File> getCandidateFiles() {
		return getDirInfo().files();
	}
}
