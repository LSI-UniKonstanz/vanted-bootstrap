/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 08.06.2005 by Christian Klukas
 */
package org.vanted.bootstrap.update;

import java.io.File;
import java.util.Properties;

public class ReleaseInfo {
	
	public static String WIN_MAC_HOMEFOLDER = "VANTED";
	public static String LINUX_HOMEFOLDER = ".vanted";
	
	public static boolean linuxRunning() {
		Properties p = System.getProperties();
		String os = (String) p.get("os.name");
		if (os != null && os.toUpperCase().contains("LINUX")) {
			return true;
		} else
			if (os != null && os.toUpperCase().contains("UNIX")) {
				return true;
			} else
				return false;
	}
	
	public static boolean windowsRunning() {
		Properties p = System.getProperties();
		String os = (String) p.get("os.name");
		if (os != null && os.toUpperCase().contains("WINDOWS")) {
			return true;
		} else
			return false;
	}
	
	public static boolean macOSrunning() {
		try {
			boolean ismac = System.getProperty("mrj.version") != null;
			
			ismac = System.getProperty("os.name").toLowerCase().contains("mac");
			return ismac;
			
		} catch (Exception ace) {
			return false;
		}
	}
	
	public static String getAppFolder() {
		String appFolder = getAppFolderName();
		try {
			if (!new File(appFolder).isDirectory()) {
				boolean success = (new File(appFolder)).mkdirs();
				if (!success) {
					appFolder = System.getenv("USERPROFILE");
					if (!new File(appFolder).isDirectory()) {
						success = (new File(appFolder)).mkdirs();
					}
				}
			}
		} catch (Exception e) {
			// empty
		}
		return appFolder;
	}
	
	private static String getAppFolderName() {
		String newStyle = getAppFolderNameNewStyle();
		
		return newStyle;
	}
	
	private static String getAppFolderNameNewStyle() {
		String home = System.getProperty("user.home");
		boolean windows = false;
		if (macOSrunning())
			home = home + getFileSeparator() + "Library" + getFileSeparator()
					+ "Preferences";
		else {
			if (new File(home + getFileSeparator() + "AppData"
					+ getFileSeparator() + "Roaming").isDirectory()) {
				home = home + getFileSeparator() + "AppData"
						+ getFileSeparator() + "Roaming";
				windows = true;
			} else {
				String hhh = System.getenv("APPDATA");
				if (hhh != null) {
					if (new File(hhh).isDirectory()) {
						home = hhh;
						windows = true;
					}
				}
			}
		}
		
		if (macOSrunning() || windows) {
			return home + getFileSeparator() + WIN_MAC_HOMEFOLDER;
		} else {
			return home + getFileSeparator() + LINUX_HOMEFOLDER;
		}
	}
	
	public static String getFileSeparator() {
		return System.getProperty("file.separator");
	}
	
	public static String getAppFolderWithFinalSep() {
		return getAppFolder() + getFileSeparator();
	}
	
	public static String getAppWebURL() {
		return "https://www.cls.uni-konstanz.de/software/vanted/";
	}
	
	private static String helpIntro = "";
	
	public static void setHelpIntroductionText(String statusMessage) {
		helpIntro = statusMessage;
	}
	
	public static String getHelpIntroductionText() {
		return helpIntro;
	}
	
	private static boolean firstRun = false;
	
	public static void setIsFirstRun(boolean b) {
		firstRun = b;
	}
	
	public static boolean isFirstRun() {
		return firstRun;
	}
	
	public static String getAppSubdirFolder(String folderName) {
		String folder = getAppFolderWithFinalSep() + folderName;
		File dir = new File(folder);
		if (!dir.exists())
			dir.mkdir();
		return folder;
	}
	
	public static String getAppSubdirFolder(String subDir1, String subDir2) {
		String folder1 = getAppFolderWithFinalSep() + subDir1;
		String folder2 = folder1 + getFileSeparator() + subDir2;
		File dir = new File(folder1);
		if (!dir.exists())
			dir.mkdir();
		File dir2 = new File(folder2);
		if (!dir2.exists())
			dir2.mkdir();
		return folder2;
	}
	
	public static String getAppSubdirFolderWithFinalSep(String folderName) {
		return getAppSubdirFolder(folderName) + getFileSeparator();
	}
	
	public static String getAppSubdirFolderWithFinalSep(String folderName,
			String folderName2) {
		return getAppSubdirFolder(folderName, folderName2) + getFileSeparator();
	}
}
