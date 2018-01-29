/**
 * 
 */
package edu.monash.vanted.boot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import javax.swing.JOptionPane;

import org.vanted.bootstrap.update.InstallUpdate;
import org.vanted.bootstrap.update.PrivilegeRunner;
import org.vanted.bootstrap.update.ReleaseInfo;

/**
 * @author matthiak
 */
public class VantedBootstrap {
	
	public static Boolean DEBUG = false;
	
	/**
	 * 
	 */
	public VantedBootstrap(String[] args) {
		log("Getting bootstrap information:");
		
		ClassLoader bootstraploader;
		
		try {
			bootstraploader = loadLibraries(Thread.currentThread().getContextClassLoader());
			Class<?> loadClass = bootstraploader.loadClass("de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.Main");
			
			Constructor<?>[] constructors = loadClass.getConstructors();
			if (DEBUG) {
				for (Constructor<?> constr : constructors) {
					System.out.print("Constructor name " + constr.getName() + "(");
					for (Class<?> paramname : constr.getParameterTypes())
						System.out.print(paramname.getName() + ",");
					System.out.println(")");
				}
				Method[] methods = loadClass.getMethods();
				for (Method curmethod : methods) {
					System.out.print("Method name: " + curmethod.getName() + "(");
					for (Class<?> paramname : curmethod.getParameterTypes())
						System.out.print(paramname.getName() + ",");
					System.out.println(")");
				}
			}
			
			Thread.currentThread().setContextClassLoader(bootstraploader);
			
			Method startmethod = loadClass.getMethod("startVanted", String[].class, String.class);
			
			startmethod.invoke(null, args, null);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static ClassLoader loadLibraries(ClassLoader cl) throws IOException {
		Enumeration<URL> urls = getBaseURL(cl);
		log("Printing all URLs for current thread's context Classloader...");
		while (urls.hasMoreElements()) {
			URL curURL = urls.nextElement();
			log(curURL.getPath() + " " + curURL.getFile());
		}
		
		String executionpath = getExectutionPath(urls);
		
		if (executionpath == null) {
			System.err.println("CANNOT figure out correct classpath!");
			System.exit(1);
		}
		
		log("Execution path: " + executionpath);
		File f = new File(executionpath + "/core-libs/");
		log("Corelibs path:" + f.getPath());
		File c = new File(executionpath + "/vanted-core/");
		log("Core path:" + c.getPath());
		FilenameFilter filter = new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.toString().endsWith(".jar");
			}
		};
		
		File[] listLibFiles = f.listFiles(filter);
		log("Number of core-lib files:" + listLibFiles.length);
		File[] listCoreFiles = c.listFiles(filter);
		log("Number of core files:" + listCoreFiles.length);
		
		URL urllist[] = new URL[listLibFiles.length + listCoreFiles.length];
		
		try {
			int i = 0;
			
			for (File curFile : listLibFiles) {
				URL url = curFile.toURI().toURL();
				urllist[i++] = url;
			}
			for (File curFile : listCoreFiles) {
				URL url = curFile.toURI().toURL();
				urllist[i++] = url;
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		if (DEBUG) {
			log("==================");
			for (URL url : urllist)
				log(url.toString());
		}
		return new BootStrapClassloader(urllist);
	}

	/**
	 * Returns the base URL for further use. Previously used: URLClassLoader,
	 * which is incompatible with the default ClassLoader starting from Java 9.
	 * The reason for that was an undocumented in the Java APIs intermediate 
	 * AppClassLoader, which has been internalized and cannot be accessed.
	 * 
	 * @param cl the classLoader
	 * @return
	 * @throws IOException
	 * @since Vanted 2.6.5
	 */
	public static Enumeration<URL> getBaseURL(ClassLoader cl) throws IOException {
		return cl.getResources("");
	}
	
	/**
	 * @param urls
	 * @param executionpath
	 * @return
	 */
	public static String getExectutionPath(Enumeration<URL> urls) {
		String executionpath = null;
		while (urls.hasMoreElements()) {
			URL curURL = urls.nextElement();
			if (curURL.getPath().endsWith(".jar")) {
				executionpath = curURL.getPath().substring(0, curURL.getPath().lastIndexOf(File.separator));
				executionpath = executionpath.replace("%20", " ");
				break;
			}
		}
		return executionpath;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (System.getProperty("vanted.debug") != null && System.getProperty("vanted.debug").equals("true")) {
			DEBUG = true;
		} else
			DEBUG = false;
		
		log("Starting....");

		if (PrivilegeRunner.isPrivilegedMode() && InstallUpdate.isUpdateAvailable()) {
			/*
			 * this part only is privileged
			 */
			try {
				log("Starting with privileged mode AND update is available");
				log("Updating...");
//				System.out.println("is privileged mode AND update is available...doing update");
				InstallUpdate.doUpdate();
				InstallUpdate.waitUpdateFinished();
				log("Exiting update-install mode");
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
//			return; //return from privileged mode
		} else
			if (!PrivilegeRunner.isPrivilegedMode() && InstallUpdate.isUpdateAvailable() && !InstallUpdate.isUpdateInstalled()) {
				try {
					JOptionPane.showMessageDialog(null, "Updating VANTED");
					if (ReleaseInfo.windowsRunning()) {
						log("Update is available: Windows: relaunching with elevated rights");
						new PrivilegeRunner().relaunchWithElevatedRights(DEBUG);
					} else {
						log("Update is available: MAC / Linux: relaunching normally");
						new PrivilegeRunner().relaunchWithNormalRights(DEBUG); // on Linux and Mac
					}
					log("Waiting until update is finished");
					InstallUpdate.waitUpdateFinished();
					log("Update finished!");
					log("Bootstrapping VANTED after update");
					
					new PrivilegeRunner().launchAfterUpdate(DEBUG);
					
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				log("Bootstrapping VANTED");
				log("----------------------------");
				new VantedBootstrap(args);
			}
	}
	
	static void log(String text) {
		System.out.println("logger: " + text);
		File file = new File(ReleaseInfo.getAppFolderWithFinalSep() + "bootlog");
		try (BufferedWriter logfile = new BufferedWriter(new FileWriter(file, true))) {
			logfile.write(text);
			logfile.newLine();
			logfile.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
