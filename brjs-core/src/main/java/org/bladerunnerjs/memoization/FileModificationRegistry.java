package org.bladerunnerjs.memoization;

import java.io.File;
import java.util.TreeMap;

import org.bladerunnerjs.utility.FileUtility;


public class FileModificationRegistry
{
	
	private TreeMap<String,Integer> lastModifiedMap = new TreeMap<>();
	
	public int getLastModified(File file) {
		String canonicalFilePath = createKeyAndInitEmptyValueIfRequired(file);
		return lastModifiedMap.get(canonicalFilePath);
	}

	public void updateLastModified(File file) {
		String canonicalFilePath = createKeyAndInitEmptyValueIfRequired(file);
		int currentLastModified = lastModifiedMap.get(canonicalFilePath);
		lastModifiedMap.put(canonicalFilePath, ++currentLastModified);
	}
	
	
	private String createKeyAndInitEmptyValueIfRequired(File file)
	{
		String canonicalFilePath = FileUtility.getCanonicalFileWhenPossible(file).getAbsolutePath();
		if (!lastModifiedMap.containsKey(canonicalFilePath)) {
			lastModifiedMap.put(canonicalFilePath, 0);
		}
		return canonicalFilePath;
	}
	
}