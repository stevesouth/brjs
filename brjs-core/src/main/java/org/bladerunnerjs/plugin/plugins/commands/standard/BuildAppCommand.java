package org.bladerunnerjs.plugin.plugins.commands.standard;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.bladerunnerjs.logging.Logger;
import org.bladerunnerjs.model.App;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.exception.ModelOperationException;
import org.bladerunnerjs.model.exception.command.CommandArgumentsException;
import org.bladerunnerjs.model.exception.command.CommandOperationException;
import org.bladerunnerjs.model.exception.command.DirectoryAlreadyExistsCommandException;
import org.bladerunnerjs.model.exception.command.DirectoryDoesNotExistCommandException;
import org.bladerunnerjs.model.exception.command.DirectoryNotEmptyCommandException;
import org.bladerunnerjs.model.exception.command.NodeDoesNotExistException;
import org.bladerunnerjs.plugin.utility.command.ArgsParsingCommandPlugin;
import org.bladerunnerjs.utility.RelativePathUtility;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;

public class BuildAppCommand extends ArgsParsingCommandPlugin {

	public class Messages {
		public static final String APP_BUILT_CONSOLE_MSG = "Built app '%s' available at '%s'";
		public static final String UNABLE_TO_DELETE_BULIT_APP_EXCEPTION = "Unable to automatically delete a previously built app at %s, possibly because its being used by another process. "+
						"The app will be exported to %s instead.";
	}
	
	private BRJS brjs;
	private Logger logger;
	
	private class Parameters {
		public static final String APP_NAME = "app-name";
		public static final String TARGET_DIR = "target-dir";
	}
	
	@Override
	protected void configureArgsParser(JSAP argsParser) throws JSAPException {
		argsParser.registerParameter(new UnflaggedOption(Parameters.APP_NAME).setRequired(true).setHelp("the application within which the new blade will be created"));
		argsParser.registerParameter(new UnflaggedOption(Parameters.TARGET_DIR).setHelp("the directory within which the exported app will be built"));
		argsParser.registerParameter(new Switch("war").setShortFlag('w').setLongFlag("war").setDefault("false").setHelp("whether the exported files should be placed into a war zip."));
	}
	
	@Override
	public void setBRJS(BRJS brjs) {
		this.brjs = brjs;
		this.logger = brjs.logger(this.getClass());
	}
	
	@Override
	public String getCommandName() {
		return "build-app";
	}
	
	@Override
	public String getCommandDescription() {
		return "Build an application so that it can be deployed on web server.";
	}
	
	@Override
	protected int doCommand(JSAPResult parsedArgs) throws CommandArgumentsException, CommandOperationException {
		
		String appName = parsedArgs.getString(Parameters.APP_NAME);
		String targetDirPath = parsedArgs.getString(Parameters.TARGET_DIR);
		boolean warExport = parsedArgs.getBoolean("war");
		boolean hasExplicitExportDirArg = (targetDirPath != null);
		
		App app = brjs.app(appName);
		
		File targetDir = brjs.storageDir("built-apps");
		File appExportDir;
		File warExportFile;
		
		if (hasExplicitExportDirArg) 
		{
			targetDir = new File(targetDirPath);
			if (!targetDir.isDirectory()) 
			{
				targetDir = brjs.file("sdk/" + targetDirPath);
			}
			appExportDir = targetDir;
			warExportFile = new File(targetDir, appName+".war");			
		} 
		else {
			appExportDir = new File(targetDir, appName);
			warExportFile = new File(targetDir, appName+".war");
			
			if (warExport && warExportFile.exists()) {
				boolean deleted = FileUtils.deleteQuietly(warExportFile);
				if (!deleted) {
					File oldWarExportFile = warExportFile;
					warExportFile = new File(targetDir, appName+"_"+getBuiltAppTimestamp()+".war");
					brjs.logger(this.getClass()).warn( Messages.UNABLE_TO_DELETE_BULIT_APP_EXCEPTION, RelativePathUtility.get(brjs.getFileInfoAccessor(), app.dir(), oldWarExportFile), RelativePathUtility.get(brjs.getFileInfoAccessor(), app.dir(), warExportFile)); 
				}
			} else if (!warExport && appExportDir.exists()){
				boolean deleted = FileUtils.deleteQuietly(appExportDir);			
				if (!deleted) {
					File oldAppExportDir = appExportDir;
					appExportDir = new File(targetDir, appName+"_"+getBuiltAppTimestamp());
					brjs.logger(this.getClass()).warn( Messages.UNABLE_TO_DELETE_BULIT_APP_EXCEPTION, RelativePathUtility.get(brjs.getFileInfoAccessor(), app.dir(), oldAppExportDir), RelativePathUtility.get(brjs.getFileInfoAccessor(), app.dir(), appExportDir));
				}
			}
			targetDir.mkdirs();
		}
		
		if(!app.dirExists()) throw new NodeDoesNotExistException(app, this);
		if(!targetDir.isDirectory()) throw new DirectoryDoesNotExistCommandException(targetDirPath, this);
		
		try {
			if (warExport) {
				if(warExportFile.exists()) throw new DirectoryAlreadyExistsCommandException(warExportFile.getPath(), this);
				app.buildWar(warExportFile);
				logger.println(Messages.APP_BUILT_CONSOLE_MSG, appName, warExportFile.getCanonicalPath());
			} else {
				if (hasExplicitExportDirArg) {
					if (appExportDir.listFiles().length > 0) throw new DirectoryNotEmptyCommandException(appExportDir.getPath(), this);								
				} else {
					appExportDir.mkdir();			
				}
				app.build(appExportDir);
				logger.println(Messages.APP_BUILT_CONSOLE_MSG, appName, appExportDir.getCanonicalPath());
			}
		}
		catch (ModelOperationException | IOException e) {
			throw new CommandOperationException(e);
		}
		
		return 0;
	}
	
	private String getBuiltAppTimestamp() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhmmss");
		return sdf.format(date);
	}
}
