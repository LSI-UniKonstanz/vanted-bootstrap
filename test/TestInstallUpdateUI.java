import java.io.IOException;

import org.vanted.bootstrap.update.InstallUpdate;

/**
 * 
 */

/**
 * @author matthiak
 */
public class TestInstallUpdateUI {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			InstallUpdate.doUpdate();
//			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			
		}
	}
	
}
