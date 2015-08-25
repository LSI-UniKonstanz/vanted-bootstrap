/**
 * 
 */
package org.vanted.bootstrap.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import edu.monash.vanted.boot.VantedBootstrap;

/**
 * @author matthiak
 */
public class InstallUpdate {
	
	private static final String VERSIONSTRING = "version";
	private static final String CORESTRING = "core";
	private static final String LIBSTRING = "lib";
	
	private static final String DESTPATHUPDATEDIR = ReleaseInfo.getAppFolderWithFinalSep() + "update/";
	private static final String DESTUPDATEFILE = DESTPATHUPDATEDIR + "do-vanted-update";
	private static final String VANTEDUPDATEOKFILE = DESTPATHUPDATEDIR + "vanted-update-ok";
	
	public static boolean isUpdateAvailable() {
		return new File(DESTUPDATEFILE).exists();
	}
	
	public static void waitUpdateFinished() {
		File fileUpdateOK = new File(VANTEDUPDATEOKFILE);
		while (!fileUpdateOK.exists()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 */
	public static void doUpdate() throws IOException {
		File fileUpdateOK = new File(VANTEDUPDATEOKFILE);
		
		//directory was not deleted but update was done (since this check-file is here
		if (fileUpdateOK.exists()) {
			System.out.println("found previous update file");
			return;
		}
		System.out.println("getting execution path");
		String executionpath = VantedBootstrap.getExectutionPath(((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs());
		
		String version;
		
		BufferedReader reader = new BufferedReader(new FileReader(new File(DESTUPDATEFILE)));
		System.out.println("reading update file:");
		String line;
		String strCoreProgramRelativePath = null;
		List<String> listStrCoreLibsRelativePaths = new ArrayList<>();
		while ((line = reader.readLine()) != null) {
			if (line.equals("//"))
				break;
			if (line.startsWith("#"))
				continue;
			if (line.toLowerCase().startsWith(VERSIONSTRING)) {
				version = line.substring(VERSIONSTRING.length() + 1).trim();
			}
			if (line.toLowerCase().startsWith(CORESTRING)) {
				strCoreProgramRelativePath = line.substring(CORESTRING.length() + 1).trim();
			}
			if (line.toLowerCase().startsWith(LIBSTRING)) {
				listStrCoreLibsRelativePaths.add(line.substring(LIBSTRING.length() + 1).trim());
			}
			
		}
		
		reader.close();
		
		System.out.println("installing update to :" + executionpath);
		System.out.println("Core: " + strCoreProgramRelativePath);
		for (String libPath : listStrCoreLibsRelativePaths) {
			System.out.println(" lib: " + libPath);
		}
		
		if (strCoreProgramRelativePath != null) {
			Path source = new File(DESTPATHUPDATEDIR + strCoreProgramRelativePath).toPath();
			System.out.println(source);
			Path target = new File(executionpath + "/vanted-core/" + strCoreProgramRelativePath).toPath();
			System.out.println(target);
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
		}
		for (String libname : listStrCoreLibsRelativePaths) {
			Path source = new File(DESTPATHUPDATEDIR + libname).toPath();
			Path target = new File(executionpath + "/core-libs/" + libname).toPath();
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
			
		}
		
		fileUpdateOK.createNewFile();
		JOptionPane.showMessageDialog(null, "Update finished");
	}
	
	private static String extractFileName(String path) {
		int idx = path.lastIndexOf("/");
		return path.substring(idx + 1);
	}
	
}
