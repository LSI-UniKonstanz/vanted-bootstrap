/**
 * 
 */
package org.vanted.bootstrap.update;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

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
	
	public static boolean isUpdateInstalled() {
		return new File(VANTEDUPDATEOKFILE).exists();
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
	public static void doUpdate()
			throws IOException
	{
		File fileUpdateOK = new File(VANTEDUPDATEOKFILE);
		ProgressDialog progressDialog = new ProgressDialog(fileUpdateOK);
		try {
			
			//directory was not deleted but update was done (since this check-file is here
			if (fileUpdateOK.exists()) {
				System.out.println("found previous update file");
				return;
			}
			System.out.println("getting execution path");
			String executionpath = VantedBootstrap.getExectutionPath(((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs());
			
			List<String> listAddCoreJarRelativePath = new ArrayList<String>();
			List<String> listRemoveCoreJarRelativePath = new ArrayList<String>();
			List<String> listAddLibsJarRelativePaths = new ArrayList<String>();
			List<String> listRemoveLibsJarRelativePaths = new ArrayList<String>();
			String version;
			progressDialog.logLine("reading update file:");
			
			BufferedReader reader = new BufferedReader(new FileReader(new File(DESTUPDATEFILE)));
			System.out.println("reading update file:");
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.equals("//"))
					break;
				if (line.startsWith("#"))
					continue;
				if (line.toLowerCase().startsWith(VERSIONSTRING)) {
					version = line.substring(VERSIONSTRING.length() + 1).trim();
					progressDialog.logLine("updating to: " + version);
				}
				// look for jars to add
				if (line.toLowerCase().startsWith("+")) {
					line = line.substring(1);
					if (line.toLowerCase().startsWith(CORESTRING)) {
						listAddCoreJarRelativePath.add(line.substring(CORESTRING.length() + 1).trim());
					}
					if (line.toLowerCase().startsWith(LIBSTRING)) {
						listAddLibsJarRelativePaths.add(line.substring(LIBSTRING.length() + 1).trim());
					}
				} else
					if (line.toLowerCase().startsWith("-")) {
						line = line.substring(1);
						if (line.toLowerCase().startsWith(CORESTRING)) {
							listRemoveCoreJarRelativePath.add(line.substring(CORESTRING.length() + 1).trim());
						}
						if (line.toLowerCase().startsWith(LIBSTRING)) {
							listRemoveLibsJarRelativePaths.add(line.substring(LIBSTRING.length() + 1).trim());
						}
					}
			}
			
			reader.close();
			
			if (VantedBootstrap.DEBUG) {
				System.out.println("installing update to :" + executionpath);
				for (String libPath : listAddCoreJarRelativePath) {
					System.out.println(" adding core-jar: " + libPath);
				}
				for (String libPath : listAddLibsJarRelativePaths) {
					System.out.println("  adding lib-jar: " + libPath);
				}
				System.out.println("----------");
				for (String libPath : listRemoveCoreJarRelativePath) {
					System.out.println(" removing core-jar: " + libPath);
				}
				for (String libPath : listRemoveLibsJarRelativePaths) {
					System.out.println("  removing lib-jar: " + libPath);
				}
			}
			for (String corename : listAddCoreJarRelativePath) {
				Path source = new File(DESTPATHUPDATEDIR + corename).toPath();
				System.out.println(source);
				Path target = new File(executionpath + "/vanted-core/" + corename).toPath();
				System.out.println(target);
				Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
				progressDialog.logLine("copied new vanted core file: " + corename);
			}
			for (String libname : listAddLibsJarRelativePaths) {
				Path source = new File(DESTPATHUPDATEDIR + libname).toPath();
				Path target = new File(executionpath + "/core-libs/" + libname).toPath();
				Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
				progressDialog.logLine("copied new vanted library file: " + libname);
				
			}
			
			for (String corename : listRemoveCoreJarRelativePath) {
				File delFile = new File(executionpath + "/vanted-core/" + corename);
				if (delFile.exists()) {
					delFile.delete();
					progressDialog.logLine("deleted vanted core file: " + corename);
				}
			}
			
			for (String libname : listRemoveLibsJarRelativePaths) {
				File delFile = new File(executionpath + "/core-libs/" + libname);
				if (delFile.exists()) {
					delFile.delete();
					progressDialog.logLine("deleted vanted library file: " + libname);
				}
			}
			
			progressDialog.logLine("done... ");
		} catch (FileNotFoundException e) {
			progressDialog.logLine("ERROR: " + e.getMessage());
			progressDialog.logLine(null);
			progressDialog.logLine("If VANTED fails to start, please "
					+ "reinstall the latest version.");
			
			progressDialog.hasException();
			
			throw e;
		} catch (IOException e) {
			progressDialog.logLine("ERROR: " + e.getMessage());
			progressDialog.logLine(null);
			progressDialog.logLine("If VANTED fails to start, please "
					+ "reinstall the latest version.");
			
			progressDialog.hasException();
			throw e;
		} finally {
			progressDialog.enableClose();
		}
		
	}
	
	private static String extractFileName(String path) {
		int idx = path.lastIndexOf("/");
		return path.substring(idx + 1);
	}
	
	static class ProgressDialog extends JDialog {
		
		JLabel header;
		
		JButton okButton;
		
		JTextArea logscreen;
		
		ProgressDialog instance;
		
		File fileUpdateOK;
		
		private boolean hasException;
		
		/**
		 * 
		 */
		public ProgressDialog(File fileUpdateOK) {
			super();
			this.fileUpdateOK = fileUpdateOK;
			instance = this;
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			
			ImageIcon imageIcon = new ImageIcon(getClass().getResource("/org/vanted/bootstrap/update/vanted_logo_splash_16x16.png"));
			header = new JLabel("updating..");
			header.setBorder(new EmptyBorder(5, 5, 5, 5));
			header.setIcon(imageIcon);
			Font f = new Font("SansSerif", Font.PLAIN, 14);
			header.setFont(f);
			panel.add(header, BorderLayout.NORTH);
			
			logscreen = new JTextArea();
			logscreen.setEditable(false);
			logscreen.setBorder(new EmptyBorder(5, 5, 5, 5));
			logscreen.setLineWrap(true);
			logscreen.setPreferredSize(new Dimension(300, 200));
			JScrollPane scrollpane = new JScrollPane(logscreen);
			scrollpane.setPreferredSize(new Dimension(300, 200));
			panel.add(scrollpane, BorderLayout.CENTER);
			
			okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					instance.setVisible(false);
					if (hasException)
						return;
					try {
						getFileUpdateOK().createNewFile();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
			okButton.setEnabled(false);
			panel.add(okButton, BorderLayout.SOUTH);
			getContentPane().add(panel);
			
			setTitle("Update VANTED - Copying");
			setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			setResizable(false);
			setModal(false);
			setAlwaysOnTop(true);
			pack();
			setLocationRelativeTo(null);
			
			setVisible(true);
			toFront();
		}
		
		public File getFileUpdateOK() {
			return fileUpdateOK;
		}
		
		public void logLine(String text) {
			if (text == null)
				text = "";
			logscreen.append(text + "\n");
		}
		
		public void enableClose() {
			okButton.setEnabled(true);
		}
		
		public void hasException() {
			hasException = true;
		}
	}
}
