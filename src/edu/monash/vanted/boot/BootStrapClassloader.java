/**
 * 
 */
package edu.monash.vanted.boot;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author matthiak
 *
 */
public class BootStrapClassloader extends URLClassLoader {


	
	/**
	 * @param urls
	 */
	public BootStrapClassloader(URL[] urls) {
		super(urls);
		
	}

	@Override
	protected void addURL(URL url) {
		super.addURL(url);
	}

}
