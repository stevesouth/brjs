package org.bladerunnerjs.model;

import java.io.File;
import java.util.List;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.bladerunnerjs.logging.LoggerFactory;
import org.bladerunnerjs.utility.filemodification.FileModificationInfo;
import org.bladerunnerjs.utility.filemodification.FileModificationService;

public class BRJSFileInfo implements FileInfo {

	private final File file;
	private final File primarySetFile;
	private final FileInfoAccessor fileInfoAccessor;
	private StandardFileInfo fileInfo;
	private LoggerFactory loggerFactory;
	
	public BRJSFileInfo(File file, FileModificationService fileModificationService, FileInfoAccessor fileInfoAccessor, LoggerFactory loggerFactory) {
		this(file, null, fileModificationService, fileInfoAccessor, loggerFactory);
	}

	public BRJSFileInfo(File file, File primarySetFile, FileModificationService fileModificationService, FileInfoAccessor fileInfoAccessor, LoggerFactory loggerFactory) {
		this.file = file;
		this.primarySetFile = primarySetFile;
		this.fileInfoAccessor = fileInfoAccessor;
		this.loggerFactory = loggerFactory;
		
		reset(fileModificationService);
	}

	public void reset(FileModificationService fileModificationService) {
		FileModificationInfo fileModificationInfo = (primarySetFile == null) ? fileModificationService.getFileModificationInfo(file) :
			fileModificationService.getFileSetModificationInfo(file, primarySetFile);
		fileInfo = new StandardFileInfo(file, fileModificationInfo, fileInfoAccessor, loggerFactory);
	}

	@Override
	public int hashCode() {
		return fileInfo.hashCode();
	}

	@Override
	public boolean isDirectory() {
		return fileInfo.isDirectory();
	}

	@Override
	public boolean exists() {
		return fileInfo.exists();
	}

	@Override
	public String canonicalPath() {
		return fileInfo.canonicalPath();
	}

	@Override
	public long getLastModified() {
		return fileInfo.getLastModified();
	}

	@Override
	public void resetLastModified() {
		fileInfo.resetLastModified();
	}

	@Override
	public List<File> filesAndDirs() {
		return fileInfo.filesAndDirs();
	}

	@Override
	public List<File> filesAndDirs(IOFileFilter fileFilter) {
		return fileInfo.filesAndDirs(fileFilter);
	}

	@Override
	public List<File> files() {
		return fileInfo.files();
	}

	@Override
	public List<File> dirs() {
		return fileInfo.dirs();
	}

	@Override
	public List<File> nestedFilesAndDirs() {
		return fileInfo.nestedFilesAndDirs();
	}

	@Override
	public List<File> nestedFiles() {
		return fileInfo.nestedFiles();
	}

	@Override
	public List<File> nestedDirs() {
		return fileInfo.nestedDirs();
	}

	@Override
	public boolean equals(Object obj) {
		return fileInfo.equals(obj);
	}

	@Override
	public String toString() {
		return fileInfo.toString();
	}
}
