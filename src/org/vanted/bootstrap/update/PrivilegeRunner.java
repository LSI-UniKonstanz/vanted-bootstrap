/**
 * 
 */
package org.vanted.bootstrap.update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class PrivilegeRunner
{
	private boolean vetoed = false;
	
	public PrivilegeRunner() {
	}
	
	public PrivilegeRunner(boolean paramBoolean)
	{
		this.vetoed = paramBoolean;
	}
	
	public boolean isVetoed()
	{
		return this.vetoed;
	}
	
	public boolean canWriteToProgramFiles()
	{
		try
		{
			String str = System.getenv("ProgramFiles");
			if (str == null) {
				str = "C:\\Program Files";
			}
			File localFile = new File(str, "foo.txt");
			if (localFile.createNewFile())
			{
				localFile.delete();
				return true;
			}
			return false;
		} catch (IOException localIOException) {
		}
		return false;
	}
	
	public int relaunchWithElevatedRights(boolean enableDebug)
			throws IOException, InterruptedException
	{
		String str1 = getJavaExecutable();
		String str2 = getInstallerJar();
		ProcessBuilder localProcessBuilder = new ProcessBuilder(getElevator(str1, str2, enableDebug));
		localProcessBuilder.environment().put("vanted.update.mode", "privileged");
		return localProcessBuilder.start().waitFor();
	}
	
	public int relaunchWithNormalRights(boolean enableDebug)
			throws IOException, InterruptedException {
		String javaExecutable = "java";
		String locationJar = getInstallerJar();
		ArrayList<String> localArrayList = new ArrayList<>();
		localArrayList.add(javaExecutable);
		localArrayList.add("-Dvanted.update.mode=privileged");
		if (enableDebug)
			localArrayList.add("-Dvanted.debug=true");
		localArrayList.add("-jar");
		localArrayList.add(locationJar);
		
		ProcessBuilder localProcessBuilder = new ProcessBuilder(localArrayList);
		localProcessBuilder.environment().put("vanted.update.mode", "privileged");
		return localProcessBuilder.start().waitFor();
	}
	
	public int launchAfterUpdate(boolean enableDebug)
			throws IOException, InterruptedException {
		String javaExecutable = "java";
		String locationJar = getInstallerJar();
		ArrayList<String> localArrayList = new ArrayList<>();
		localArrayList.add(javaExecutable);
		if (enableDebug)
			localArrayList.add("-Dvanted.debug=true");
		localArrayList.add("-jar");
		localArrayList.add(locationJar);
		
		ProcessBuilder localProcessBuilder = new ProcessBuilder(localArrayList);
		return localProcessBuilder.start().waitFor();
	}
	
	private List<String> getElevator(String javaExecutable, String locationJar, boolean enableDebug)
			throws IOException, InterruptedException
	{
		ArrayList<String> localArrayList = new ArrayList<>();
		
		localArrayList.add("wscript");
		localArrayList.add(extractVistaElevator().getCanonicalPath());
		localArrayList.add(javaExecutable);
		localArrayList.add("-Dvanted.update.mode=privileged");
		if (enableDebug)
			localArrayList.add("-Dvanted.debug=true");
		
		localArrayList.add("-jar");
		localArrayList.add(locationJar);
		
		for (String param : localArrayList) {
			System.out.print(param + " ");
		}
		System.out.println();
		
		return localArrayList;
	}
	
	private File extractVistaElevator()
			throws IOException
	{
		String str = System.getProperty("java.io.tmpdir") + File.separator + "Installer.js";
		File localFile = new File(str);
		FileOutputStream localFileOutputStream = new FileOutputStream(localFile);
		InputStream localInputStream = getClass().getResourceAsStream("/org/vanted/bootstrap/update/elevate.js");
		copyStream(localFileOutputStream, localInputStream);
		localInputStream.close();
		localFileOutputStream.close();
		localFile.deleteOnExit();
		return localFile;
	}
	
	private void copyStream(OutputStream paramOutputStream, InputStream paramInputStream)
			throws IOException
	{
		byte[] arrayOfByte = new byte[1024];
		int i = 0;
		while ((i = paramInputStream.read(arrayOfByte)) >= 0) {
			paramOutputStream.write(arrayOfByte, 0, i);
		}
	}
	
	private String getInstallerJar()
	{
		try
		{
			URI localURI = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
			if (!"file".equals(localURI.getScheme())) {
				throw new Exception("Unexpected scheme in JAR file URI: " + localURI);
			}
			return new File(localURI.getSchemeSpecificPart()).getCanonicalPath();
		} catch (Exception localException)
		{
			localException.printStackTrace();
		}
		return null;
	}
	
	private String getJavaCommand()
	{
		return System.getProperty("java.home") + File.separator + "bin" + File.separator + getJavaExecutable();
	}
	
	private String getJavaExecutable()
	{
		return "javaw.exe";
	}
	
	public static boolean isPrivilegedMode()
	{
		return ("privileged".equals(System.getenv("vanted.update.mode"))) || ("privileged".equals(System.getProperty("vanted.update.mode")));
	}
}
