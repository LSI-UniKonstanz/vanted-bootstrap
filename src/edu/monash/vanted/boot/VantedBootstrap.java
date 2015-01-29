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

/**
 * @author matthiak
 *
 */
public class VantedBootstrap {

	private static final Boolean DEBUG = false;


	/**
	 * 
	 */
	public VantedBootstrap(String[] args) {
		ClassLoader bootstraploader = loadLibraries(Thread.currentThread().getContextClassLoader());

		
		try {
			Class<?> loadClass = bootstraploader.loadClass("de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.Main");
			Constructor<?>[] constructors = loadClass.getConstructors();
			if(DEBUG) {
				for(Constructor<?> constr : constructors){
					System.out.print("constructor name " + constr.getName()+"(");
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
		String executionpath = Thread.currentThread().getContextClassLoader().getResource(".").getPath();
		if(DEBUG) {
			System.out.println("execution path: "+ executionpath);
		}
		File f = new File(executionpath + "\\core-libs\\");
		File core = new File(executionpath + "\\vanted-core\\vanted-core.jar");
		FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.toString().endsWith(".jar");
			}
		};
		File[] listFiles = f.listFiles(filter);

		URL urllist[] = new URL[listFiles.length + 1];


		try {
			int i = 0;

			urllist[i++] = core.toURI().toURL();

			for(File curFile : listFiles) {
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

		new VantedBootstrap(args);
	}

}
