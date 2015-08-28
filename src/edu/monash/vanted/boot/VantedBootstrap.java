/**
 * 
 */
package edu.monash.vanted.boot;

import java.awt.BorderLayout;
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
import java.net.URLClassLoader;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
		log("printing all urls for currents threads context classloader");
		for (URL curURL : urls)
			log(curURL.getPath() + " " + curURL.getFile());
		
		String executionpath = getExectutionPath(urls);
		
		if (executionpath == null) {
			System.err.println("cannot figure out correct classpath");
			System.exit(1);
		}
		
		log("Execution path: " + executionpath);
		File f = new File(executionpath + "/core-libs/");
		log("corelibs path:" + f.getPath());
		File c = new File(executionpath + "/vanted-core/");
		log("    core path:" + c.getPath());
		FilenameFilter filter = new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.toString().endsWith(".jar");
			}
		};
		
		File[] listLibFiles = f.listFiles(filter);
		log("number of core-lib files:" + listLibFiles.length);
		File[] listCoreFiles = c.listFiles(filter);
		log("    number of core-files:" + listCoreFiles.length);
		
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
		
		log("starting....");
		
		JFrame frame = new JFrame("Update");
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(new JLabel("updating..."), BorderLayout.CENTER);
		frame.setSize(100, 50);
		frame.getContentPane().add(panel);
		if (PrivilegeRunner.isPrivilegedMode() && InstallUpdate.isUpdateAvailable()) {
			/*
			 * this part only is privileged
			 */
			try {
				log("Starting with privileged mode AND update is available");
				log("doing update");
//				System.out.println("is privileged mode AND update is available...doing update");
				InstallUpdate.doUpdate();
			} catch (IOException e) {
				e.printStackTrace();
			}
			log("exiting from update-install mode");
			return; //return from privileged mode
		} else
			if (!PrivilegeRunner.isPrivilegedMode() && InstallUpdate.isUpdateAvailable()) {
				try {
					JOptionPane.showMessageDialog(null, "Updating VANTED");
					if (ReleaseInfo.windowsRunning()) {
						log("update is available: Windows: relaunching with elevated rights");
						new PrivilegeRunner().relaunchWithElevatedRights(DEBUG);
					} else {
						log("update is available: MAC / Linux: relaunching normal");
						new PrivilegeRunner().relaunchWithNormalRights(DEBUG); // on Linux and Mac
					}
					log("waiting until update is finished");
					InstallUpdate.waitUpdateFinished();
					log("update finished");
//					JOptionPane.showMessageDialog(null, "Update finished");
					if (frame.isVisible())
						frame.setVisible(false);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		log("bootstrapping VANTED");
		log("----------------------------");
		new VantedBootstrap(args);
	}
	
	static void log(String text) {
		
//		if (DEBUG && text != null) {
		System.out.println("logger: " + text);
		File file = new File(ReleaseInfo.getAppFolderWithFinalSep() + "bootlog");
		try (BufferedWriter logfile = new BufferedWriter(new FileWriter(file, true))) {
			logfile.write(text);
			logfile.newLine();
			logfile.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
//		}
	}
}
