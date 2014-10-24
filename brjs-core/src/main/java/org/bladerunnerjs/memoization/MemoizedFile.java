package org.bladerunnerjs.memoization;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.bladerunnerjs.model.engine.RootNode;
import org.bladerunnerjs.utility.FileUtility;


public class MemoizedFile extends File
{
	private static final long serialVersionUID = 7406703034536312889L;
	private MemoizedValue<ComputedValue> computedValue;
	private RootNode rootNode;
	private MemoizedFile canonicalFile;
	private File superFile;
	private String name;
	private MemoizedFile parentFile;
	
	MemoizedFile(RootNode rootNode, String file) {
		super( FileUtility.getCanonicalFileWhenPossible( new File(file) ).getAbsolutePath() );
		this.rootNode = rootNode;
		superFile = FileUtility.getCanonicalFileWhenPossible( new File(file) );
			// ^^ use composition so we don't have a chicken and egg problem when trying to read memoized files but we're forced to extend java.io.File since its not an interface
		
		String className = this.getClass().getSimpleName();
		computedValue = new MemoizedValue<>(className+"."+superFile.getAbsolutePath(), rootNode, this);
	}	
	
	// ---- Methods Using Memoized Values ----
	
	@Override
	public String getName()
	{
		if (name == null) {
			name = superFile.getName();
		}
		return name;
	}
	
//	@Override
//	public boolean exists() {
//		return getComputedValue().exists;
//	}
//	
//	@Override
//	public boolean isDirectory() {
//		return getComputedValue().isDirectory;
//	}
//	
//	@Override
//	public boolean isFile() {
//		return getComputedValue().isFile;
//	}
	
	public List<MemoizedFile> filesAndDirs() {
		List<MemoizedFile> filesAndDirs = new ArrayList<>();
		filesAndDirs.addAll( getComputedValue().filesAndDirs );
		return filesAndDirs; // return a copy so multiple callers dont have the same object by reference
	}
	
	// ---- End Methods Using Memoized Values ----
	
	@Override
	public MemoizedFile getCanonicalFile()
	{
		if (canonicalFile == null) {
			try {
				canonicalFile = rootNode.getMemoizedFile(superFile.getCanonicalFile());
			} catch (IOException e) {
				rootNode.logger(this.getClass()).warn("Unable to calculate canonical path for path '%s'.", getPath());
				canonicalFile = rootNode.getMemoizedFile(super.getAbsoluteFile());
			}
		}
		return canonicalFile;
	}
	
	@Override
	public String getCanonicalPath()
	{
		return getCanonicalFile().getAbsolutePath();
	}
	
	@Override
	public MemoizedFile getParentFile()
	{
		if (parentFile == null) {
			parentFile = rootNode.getMemoizedFile( superFile.getParentFile() );
		}
		return parentFile;
	}
	
	@Override
	public MemoizedFile[] listFiles(FileFilter filter) {
		List<MemoizedFile> listedFiles = new ArrayList<>();
		for (MemoizedFile file : filesAndDirs()) {
			if (filter.accept(file)) {
				listedFiles.add(file);
			}
		}
		return listedFiles.toArray(new MemoizedFile[0]);
	}
	
	@Override
	public MemoizedFile[] listFiles(FilenameFilter filter) {
		return listFiles( (FileFilter) FileFilterUtils.asFileFilter(filter) );
	}
	
	@Override
	public MemoizedFile[] listFiles() {
		return listFiles( (FileFilter) TrueFileFilter.INSTANCE);
	}
	
	@Override
	public String[] list(FilenameFilter filter) {
		List<String> listedNames = new ArrayList<>();
		for (MemoizedFile file : filesAndDirs()) {
			if (filter.accept(file.getParentFile(), file.getName())) {
				listedNames.add(file.getName());
			}
		}
		return listedNames.toArray(new String[0]);
	}
	
	@Override
	public String[] list() {
		return list( TrueFileFilter.INSTANCE );
	}
	
	public List<MemoizedFile> filesAndDirs(IOFileFilter fileFilter) {
		List<MemoizedFile> returnedFilesAndDirsCopy = new ArrayList<>();
		for (MemoizedFile file : filesAndDirs()) {
			if (fileFilter.accept(file)) {
				returnedFilesAndDirsCopy.add(file);
			}
		}
		return returnedFilesAndDirsCopy;
	}
	
	public List<MemoizedFile> files() {
		return filesAndDirs(FileFileFilter.FILE);
	}
	
	public List<MemoizedFile> dirs() {
		return filesAndDirs(DirectoryFileFilter.DIRECTORY);
	}
	
	public List<MemoizedFile> nestedFilesAndDirs() {
		List<MemoizedFile> nestedFilesAndDirs = new ArrayList<>();
		populateNestedFilesAndDirs(this, nestedFilesAndDirs);
		return nestedFilesAndDirs;
	}
	
	public List<MemoizedFile> nestedFiles() {
		List<MemoizedFile> nestedFiles = new ArrayList<>();
		for(MemoizedFile file : nestedFilesAndDirs()) {
			if(file.isFile()) {
				nestedFiles.add(file);
			}
		}
		return nestedFiles;
	}
	
	public List<MemoizedFile> nestedDirs() {
		List<MemoizedFile> nestedDirs = new ArrayList<>();
		for(MemoizedFile file : nestedFilesAndDirs()) {
			if(file.isDirectory()) {
				nestedDirs.add(file);
			}
		}
		return nestedDirs;
	}	
	
	
	private void populateNestedFilesAndDirs(MemoizedFile file, List<MemoizedFile> nestedFilesAndDirs) {
		nestedFilesAndDirs.addAll(file.filesAndDirs());
		
		for(MemoizedFile dir : file.dirs()) {
			MemoizedFile memoizedFile = rootNode.getMemoizedFile(dir);
			populateNestedFilesAndDirs(memoizedFile, nestedFilesAndDirs);
		}
	}
	
	private ComputedValue getComputedValue() {		
		return computedValue.value(() -> {
			ComputedValue value = new ComputedValue();
			value.exists = superFile.exists();
			value.isFile = superFile.isFile();
			value.isDirectory = superFile.isDirectory();			
			if (value.isDirectory) {
				for (File file : superFile.listFiles()) {
					value.filesAndDirs.add( rootNode.getMemoizedFile(file) );
				}				
			}
			return value;
		});
	}
	
	private class ComputedValue {
		List<MemoizedFile> filesAndDirs = new ArrayList<>();
		boolean exists;
		boolean isFile;
		boolean isDirectory;
	}
	
	
}