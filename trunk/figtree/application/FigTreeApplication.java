/**
 * TracerApp.java
 *
 * Title:			Tracer
 * Description:		An application for analysing MCMC trace files.
 * @author			Andrew Rambaut
 * @author			Alexei Drummond
 * @version			$Id: FigTreeApplication.java,v 1.15 2007/09/10 14:52:02 rambaut Exp $
 */

package figtree.application;

import org.virion.jam.framework.*;
import figtree.application.preferences.*;

import javax.swing.*;
import java.awt.*;

public class FigTreeApplication extends MultiDocApplication {

	public static FigTreeApplication application;

	public FigTreeApplication(MenuBarFactory menuBarFactory, String nameString, String aboutString, Icon icon,
                              String websiteURLString,
                              String helpURLString) {
		super(menuBarFactory, nameString, aboutString, icon, websiteURLString, helpURLString);

		addPreferencesSection(new GeneralPreferencesSection());
		addPreferencesSection(new AppearancePreferencesSection());
		addPreferencesSection(new AdvancedPreferencesSection());
	}

	public void doPaste() {

	}

	// Main entry point
	static public void main(String[] args) {

		System.setProperty("apple.awt.brushMetalLook","true");
		System.setProperty("apple.laf.useScreenMenuBar","true");
		System.setProperty("apple.awt.showGrowBox","true");
		System.setProperty("apple.awt.antialiasing","on");
		System.setProperty("apple.awt.graphics.UseQuartz","true");
		System.setProperty("apple.awt.textantialiasing","on");
		System.setProperty("apple.awt.rendering","VALUE_RENDER_SPEED");

		// set the Quaqua Look and Feel in the UIManager
		try {
			//System.setProperty("Quaqua.Debug.showClipBounds","true");
			//System.setProperty("Quaqua.Debug.showVisualBounds","true");
			UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");
			// set UI manager properties here that affect Quaqua
			UIManager.put("SystemFont", new Font("Lucida Grande", Font.PLAIN, 13));
			UIManager.put("SmallSystemFont", new Font("Lucida Grande", Font.PLAIN, 11));

		} catch (Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		java.net.URL url = FigTreeApplication.class.getResource("images/figtree.png");
		Icon icon = null;

		if (url != null) {
			icon = new ImageIcon(url);
		}

		final String nameString = "FigTree";
        final String versionString = "1.1";
		String aboutString = "<html><center>Tree Figure Drawing Tool<br>Version " + versionString + "<br>2006-2007, Andrew Rambaut<br>" +
				"Institute of Evolutionary Biology, University of Edinburgh.<br><br>" +
				"<a href=\"http://tree.bio.ed.ac.uk/\">http://tree.bio.ed.ac.uk/</a><br><br>" +
				"Uses the Java Evolutionary Biology Library (JEBL)<br>" +
				"<a href=\"http://sourceforge.net/projects/jebl/\">http://jebl.sourceforge.net/</a><br><br>" +
				"Thanks to Alexei Drummond, Joseph Heled, Philippe Lemey, <br>Tulio de Oliveira & Beth Shapiro</center></html>";

        String websiteURLString = "http://tree.bio.ed.ac.uk/software/figtree/";
        String helpURLString = "http://tree.bio.ed.ac.uk/software/figtree/";

		FigTreeApplication.application = new FigTreeApplication(new FigTreeMenuBarFactory(), nameString, aboutString, icon,
                websiteURLString, helpURLString);

		application.setDocumentFrameFactory(new DocumentFrameFactory() {
			public DocumentFrame createDocumentFrame(Application app, MenuBarFactory menuBarFactory) {
				return new FigTreeFrame(nameString + " v" + versionString);
			}
		});

		application.initialize();

        if (args.length > 0) {
			for (String arg : args) {
				application.doOpen(arg);
			}
		}

		if (!org.virion.jam.mac.Utils.isMacOSX() && application.getUpperDocumentFrame() == null) {
            // If we haven't opened any files by now, prompt for one...
            application.doOpen();
		}

	}

}