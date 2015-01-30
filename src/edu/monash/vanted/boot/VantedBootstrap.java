/**
 * 
 */
package edu.monash.vanted.boot;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author matthiak
 *
 */
public class VantedBootstrap {

	private static Boolean DEBUG = false;


	/**
	 * 
	 */
	public VantedBootstrap(String[] args) {
		if(DEBUG){
			System.out.println("Getting bootstrap information:");
			System.out.println();
		}
		
		ClassLoader bootstraploader = loadLibraries(Thread.currentThread().getContextClassLoader());

		
		try {
			Class<?> loadClass = bootstraploader.loadClass("de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.Main");
			
			Constructor<?>[] constructors = loadClass.getConstructors();
			if(DEBUG) {
				for(Constructor<?> constr : constructors){
					System.out.print("Constructor name " + constr.getName()+"(");
					for(Class<?> paramname : constr.getParameterTypes())
						System.out.print(paramname.getName()+",");
					System.out.println(")");
				}
				Method[] methods = loadClass.getMethods();
				for(Method curmethod : methods) {
					System.out.print("Method name: " + curmethod.getName()+"(");
					for(Class<?> paramname : curmethod.getParameterTypes())
						System.out.print(paramname.getName()+",");
					System.out.println(")");
				}
			}
			
			Thread.currentThread().setContextClassLoader(bootstraploader);
			
			Method startmethod = loadClass.getMethod("startVanted", String[].class, String.class);

			startmethod.invoke(null,args, null);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} 
	}

	static ClassLoader loadLibraries(ClassLoader cl) {
		String executionpath = ((URLClassLoader)Thread.currentThread().getContextClassLoader()).getURLs()[0].getPath();
		
		if(executionpath.endsWith(".jar"))
			executionpath = executionpath.substring(0, executionpath.lastIndexOf("/"));
		executionpath = executionpath.replace("%20", " ");
		
		if(DEBUG) {
			System.out.println("Execution path: "+ executionpath);
		}
		File f = new File(executionpath + "/core-libs/");
		if(DEBUG) {
			System.out.println("corelibs path:"+f.getPath());
		}
		File c = new File(executionpath + "/vanted-core/");
		if(DEBUG) {
			System.out.println("    core path:"+c.getPath());
		}
		FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.toString().endsWith(".jar");
			}
		};

		File[] listLibFiles = f.listFiles(filter);
		if(DEBUG) {
			System.out.println("number of core-lib files:"+listLibFiles.length);
		}
		File[] listCoreFiles= c.listFiles(filter);
		if(DEBUG) {
			System.out.println("    number of core-files:"+listCoreFiles.length);
		}
		
		
		URL urllist[] = new URL[listLibFiles.length + listCoreFiles.length];


		try {
			int i = 0;

			for(File curFile : listLibFiles) {
				URL url = curFile.toURI().toURL();
				urllist[i++] = url;
			}
			for(File curFile : listCoreFiles) {
				URL url = curFile.toURI().toURL();
				urllist[i++] = url;
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		
		
		
		if(DEBUG) {
			System.out.println("==================");
			for(URL url : urllist)
				System.out.println(url.toString());
		}
		return new BootStrapClassloader(urllist);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if( System.getProperty("vanted.debug") != null && System.getProperty("vanted.debug").equals("true")) {
			DEBUG=true;
		} else
			DEBUG=false;
		new VantedBootstrap(args);
	}

}
