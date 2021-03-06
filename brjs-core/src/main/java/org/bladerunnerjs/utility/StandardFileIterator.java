package org.bladerunnerjs.utility;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.bladerunnerjs.model.FileInfoAccessor;
import org.bladerunnerjs.utility.filemodification.FileModificationInfo;
import org.bladerunnerjs.utility.filemodification.FileModifiedChecker;
import org.bladerunnerjs.utility.filemodification.InfoFileModifiedChecker;

public class StandardFileIterator implements FileIterator {
	private final IOFileFilter fileFilter = FileFilterUtils.and(FileFileFilter.FILE, FileFilterUtils.notFileFilter(new PrefixFileFilter(".")));
	private final IOFileFilter dirFilter = FileFilterUtils.and(DirectoryFileFilter.DIRECTORY, FileFilterUtils.notFileFilter(new PrefixFileFilter(".")));
	private final FileModifiedChecker fileModificationChecker;
	private final FileInfoAccessor fileInfoAccessor;
	private final File dir;
	private List<File> filesAndDirs = Collections.emptyList();
	private List<File> files = Collections.emptyList();
	private List<File> dirs = Collections.emptyList();
	
	public StandardFileIterator(FileModificationInfo fileModificationInfo, File dir, FileInfoAccessor fileInfoAccessor) {
		this.dir = dir;
		this.fileInfoAccessor = fileInfoAccessor;
		fileModificationChecker = new InfoFileModifiedChecker(fileModificationInfo);
	}
	
	@Override
	public List<File> filesAndDirs() {
		updateIfChangeDetected();
		return filesAndDirs;
	}
	
	@Override
	public List<File> filesAndDirs(IOFileFilter fileFilter) {
		List<File> filteredFiles = new ArrayList<>();
		
		for(File file : filesAndDirs()) {
			if(fileFilter.accept(file)) {
				filteredFiles.add(file);
			}
		}
		
		return filteredFiles;
	}
	
	@Override
	public List<File> files() {
		updateIfChangeDetected();
		
		if(files == null) {
			files = filesAndDirs(fileFilter);
		}
		
		return files;
	}
	
	@Override
	public List<File> dirs() {
		updateIfChangeDetected();
		
		if(dirs == null) {
			dirs = filesAndDirs(dirFilter);
		}
		
		return dirs;
	}
	
	@Override
	public List<File> nestedFilesAndDirs() {
		List<File> nestedFilesAndDirs = new ArrayList<>();
		populateNestedFilesAndDirs(this, nestedFilesAndDirs);
		return nestedFilesAndDirs;
	}
	
	@Override
	public List<File> nestedFiles() {
		List<File> nestedFiles = new ArrayList<>();
		
		for(File file : nestedFilesAndDirs()) {
			if(!file.isDirectory()) {
				nestedFiles.add(file);
			}
		}
		
		return nestedFiles;
	}
	
	@Override
	public List<File> nestedDirs() {
		List<File> nestedDirs = new ArrayList<>();
		
		for(File file : nestedFilesAndDirs()) {
			if(file.isDirectory()) {
				nestedDirs.add(file);
			}
		}
		
		return nestedDirs;
	}
	
	private void updateIfChangeDetected() {
		if(fileModificationChecker.hasChangedSinceLastCheck()) {
			if (!dir.exists()) { return; }
			filesAndDirs = Arrays.asList(dir.listFiles());
			files = null;
			dirs = null;
			Collections.sort(filesAndDirs, NameFileComparator.NAME_COMPARATOR);
		}
	}
	
	private void populateNestedFilesAndDirs(FileIterator fileIterator, List<File> nestedFilesAndDirs) {
		nestedFilesAndDirs.addAll(fileIterator.filesAndDirs());
		
		for(File dir : fileIterator.dirs()) {
			populateNestedFilesAndDirs(fileInfoAccessor.getFileInfo(dir), nestedFilesAndDirs);
		}
	}
}
