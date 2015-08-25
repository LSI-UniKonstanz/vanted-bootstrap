/**
 * 
 */
package edu.monash.vanted.boot;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.JOptionPane;

import org.vanted.bootstrap.update.InstallUpdate;
import org.vanted.bootstrap.update.PrivilegeRunner;
import org.vanted.bootstrap.update.ReleaseInfo;

/**
 * @author matthiak
 */
public class VantedBootstrap {
	
	private static Boolean DEBUG = false;
	
	/**
	 * 
	 */
	public VantedBootstrap(String[] args) {
		if (DEBUG) {
			System.out.println("Getting bootstrap information:");
			System.out.println();
		}
		
		ClassLoader bootstraploader = loadLibraries(Thread.currentThread().getContextClassLoader());
		
		try {
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
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	static ClassLoader loadLibraries(ClassLoader cl) {
		URL[] urls = ((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs();
		if (DEBUG) {
			System.out.println("printing all urls for currents threads context classloader");
			for (URL curURL : urls)
				System.out.println(curURL.getPath() + " " + curURL.getFile());
		}
		
		String executionpath = getExectutionPath(urls);
		
		if (executionpath == null) {
			System.err.println("cannot figure out correct classpath");
			System.exit(1);
		}
		
		if (DEBUG) {
			System.out.println("Execution path: " + executionpath);
		}
		File f = new File(executionpath + "/core-libs/");
		if (DEBUG) {
			System.out.println("corelibs path:" + f.getPath());
		}
		File c = new File(executionpath + "/vanted-core/");
		if (DEBUG) {
			System.out.println("    core path:" + c.getPath());
		}
		FilenameFilter filter = new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.toString().endsWith(".jar");
			}
		};
		
		File[] listLibFiles = f.listFiles(filter);
		if (DEBUG) {
			System.out.println("number of core-lib files:" + listLibFiles.length);
		}
		File[] listCoreFiles = c.listFiles(filter);
		if (DEBUG) {
			System.out.println("    number of core-files:" + listCoreFiles.length);
		}
		
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
			System.out.println("==================");
			for (URL url : urllist)
				System.out.println(url.toString());
		}
		return new BootStrapClassloader(urllist);
	}
	
	/**
	 * @param urls
	 * @param executionpath
	 * @return
	 */
	public static String getExectutionPath(URL[] urls) {
		String executionpath = null;
		for (URL curURL : urls) {
			if (curURL.getPath().endsWith(".jar")) {
				executionpath = curURL.getPath().substring(0, curURL.getPath().lastIndexOf("/"));
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
		System.out.println("starting");
		if (PrivilegeRunner.isPrivilegedMode() && InstallUpdate.isUpdateAvailable()) {
			/*
			 * this part only is privileged
			 */
			try {
				System.out.println("is privileged mode AND update is available...doing update");
				JOptionPane.showMessageDialog(null, "doing update");
				InstallUpdate.doUpdate();
				return; //return from privileged mode
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else
			if (!PrivilegeRunner.isPrivilegedMode() && InstallUpdate.isUpdateAvailable()) {
				try {
					System.out.println("update is available relaunching with elevated rights");
					if (ReleaseInfo.windowsRunning()) {
						new PrivilegeRunner().relaunchWithElevatedRights();
					} else {
						new PrivilegeRunner().relaunchWithNormalRights();
					}
					InstallUpdate.waitUpdateFinished();
					System.out.println("update finished");
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		System.out.println("bootstrapping VANTED");
		new VantedBootstrap(args);
	}
	
}
