/*
 * Utils.java
 *
 * Copyright (c) 2009 JAM Development Team
 *
 * This package is distributed under the Lesser Gnu Public Licence (LGPL)
 *
 */

/*
 * Utils.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package figtree.application;

import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author Alexei Drummond
 * @author Andrew Rambaut
 *
 * @version $Id: Utils.java 965 2009-01-03 00:39:29Z rambaut $
 */
public class Utils {


	private static javax.swing.JFileChooser SAVE_FILE_CHOOSER = null;

	public static String getLoadFileName(String message) {
		java.io.File file = getLoadFile(message);
		if (file == null) return null;
		return file.getAbsolutePath();
	}

	public static String getSaveFileName(String message) {
		java.io.File file = getSaveFile(message);
		if (file == null) return null;
		return file.getAbsolutePath();
	}

    @SuppressWarnings({"deprecation"})
    public static File getLoadFile(String message) {
		// No file name in the arguments so throw up a dialog box...
		java.awt.Frame frame = new java.awt.Frame();
		java.awt.FileDialog chooser = new java.awt.FileDialog(frame, message,
															java.awt.FileDialog.LOAD);
        chooser.show();
		if (chooser.getFile() == null) return null;
		java.io.File file = new java.io.File(chooser.getDirectory(), chooser.getFile());
		chooser.dispose();
		frame.dispose();

		return file;
	}

	public static File getSaveFile(String message) {
		// No file name in the arguments so throw up a dialog box...
		java.awt.Frame frame = new java.awt.Frame();
		java.awt.FileDialog chooser = new java.awt.FileDialog(frame, message,
															java.awt.FileDialog.SAVE);
        chooser.setVisible(true);
        java.io.File file = null;
        if(chooser.getDirectory() != null && chooser.getFile() != null)
            file = new java.io.File(chooser.getDirectory(), chooser.getFile());

        chooser.dispose();
		frame.dispose();

		return file;
	}

	/**
	 * This function takes a file name and an array of extensions (specified
	 * without the leading '.'). If the file name ends with one of the extensions
	 * then it is returned with this trimmed off. Otherwise the file name is
	 * return as it is.
	 * @return the trimmed filename
	 */
	public static String trimExtensions(String fileName, String[] extensions) {

		String newName = null;

		for (int i = 0; i < extensions.length; i++) {
			String ext = "." + extensions[i];
			if (fileName.endsWith(ext)) {
				newName = fileName.substring(0, fileName.length() - ext.length());
			}
		}

		if (newName == null) newName = fileName;

		return newName;
	}

	/**
	 * @return a named image from file or resource bundle.
	 */
	public static Image getImage(Object caller, String name) {

		java.net.URL url = caller.getClass().getResource(name);
		if (url != null) {
			return Toolkit.getDefaultToolkit().createImage(url);
		} else {
			if (caller instanceof Component) {
				Component c = (Component)caller;
				Image i = c.createImage(100,20);
				Graphics g = c.getGraphics();
				g.drawString("Not found!", 1, 15);
				return i;
			} else return null;
		}
	}


	/*public static void showExceptionDialog(JFrame parent, Throwable e, String title) {

		JOptionPane pane = new JOptionPane(e.getMessage(), JOptionPane.ERROR_MESSAGE);
		JDialog dialog = pane.createDialog(parent, title);
		dialog.show();
	}

	public static void showExceptionDialog(JFrame parent, Throwable e) {

		JExceptionDialog d = new JExceptionDialog(parent, true, "Exception", getStackTrace(e));
	}*/

	/**
	 * Return the stack trace of an exception as a string.
	 */
	private static String getStackTrace(Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

}
