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

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.*;
import figtree.application.preferences.*;
import figtree.treeviewer.ExtendedTreeViewer;
import figtree.treeviewer.TreeViewer;
import jebl.evolution.io.ImportException;
import jebl.evolution.io.NewickImporter;
import jebl.evolution.trees.Tree;
import org.virion.jam.controlpalettes.BasicControlPalette;
import org.virion.jam.controlpalettes.ControlPalette;
import org.virion.jam.framework.*;
import org.virion.jam.mac.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.*;
import java.util.List;

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

	static public void createPDF(String treeFileName, String pdfFileName) {

		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(treeFileName));
			String line = bufferedReader.readLine();
			while (line != null && line.length() == 0) {
				line = bufferedReader.readLine();
			}

			bufferedReader.close();

			boolean isNexus = (line != null && line.toUpperCase().contains("#NEXUS"));

			Reader reader = new FileReader(treeFileName);

			Map<String, Object> settings = new HashMap<String, Object>();

			ExtendedTreeViewer treeViewer = new ExtendedTreeViewer();
			ControlPalette controlPalette = new BasicControlPalette(200, BasicControlPalette.DisplayMode.ONLY_ONE_OPEN);
			FigTreePanel figTreePanel = new FigTreePanel(treeViewer, controlPalette);

			// First of all, fully populate the settings map so that
			// all the settings have defaults
			controlPalette.getSettings(settings);

			List<Tree> trees = new ArrayList<Tree>();

			if (isNexus) {
				FigTreeNexusImporter importer = new FigTreeNexusImporter(reader);
				trees.add(importer.importNextTree());

				// Try to find a figtree block and if found, parse the settings
				while (true) {
					try {
						importer.findNextBlock();
						if (importer.getNextBlockName().equalsIgnoreCase("FIGTREE")) {
							importer.parseFigTreeBlock(settings);
						}
					} catch (EOFException ex) {
						break;
					}
				}
			} else {
				NewickImporter importer = new NewickImporter(reader, true);
				trees.add(importer.importNextTree());
			}

			if (trees.size() == 0) {
				throw new ImportException("This file contained no trees.");
			}

			treeViewer.setTrees(trees);

			controlPalette.setSettings(settings);

			//Rectangle2D bounds = treeViewer.getContentPane().getBounds();
			Rectangle2D bounds = new Rectangle2D.Double(0.0, 0.0, 800, 800);
			treeViewer.getContentPane().setBounds(bounds.getBounds());
			Document document = new Document(new com.lowagie.text.Rectangle((float)bounds.getWidth(), (float)bounds.getHeight()));

			// step 2
			PdfWriter writer;
			writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFileName));
			// step 3
			document.open();
			// step 4
			PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tp = cb.createTemplate((float)bounds.getWidth(), (float)bounds.getHeight());
			Graphics2D g2d = tp.createGraphics((float)bounds.getWidth(), (float)bounds.getHeight(), new DefaultFontMapper());
			treeViewer.getContentPane().print(g2d);
			g2d.dispose();
			cb.addTemplate(tp, 0, 0);
			document.close();

		} catch(ImportException ie) {
			throw new RuntimeException("Error writing PDF file: " + ie);
		} catch(DocumentException de) {
			throw new RuntimeException("Error writing PDF file: " + de);
		} catch(IOException ioe) {
			throw new RuntimeException("Error writing PDF file: " + ioe);
		}

	}

	// Main entry point
	static public void main(String[] args) {

		if (args.length == 2) {
			// command line version...
			createPDF(args[0], args[1]);
			System.exit(0);
		}

		boolean lafLoaded = false;

		if (Utils.isMacOSX()) {
			if (Utils.getMacOSXVersion().startsWith("10.5")) {
				System.setProperty("apple.awt.brushMetalLook","true");
			} else {
				// set the Quaqua Look and Feel in the UIManager
				try {
					//System.setProperty("Quaqua.Debug.showClipBounds","true");
					//System.setProperty("Quaqua.Debug.showVisualBounds","true");
					UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");
					// set UI manager properties here that affect Quaqua
					UIManager.put("SystemFont", new Font("Lucida Grande", Font.PLAIN, 13));
					UIManager.put("SmallSystemFont", new Font("Lucida Grande", Font.PLAIN, 11));

					lafLoaded = true;
				} catch (Exception e) {
				}
			}

			System.setProperty("apple.laf.useScreenMenuBar","true");
			System.setProperty("apple.awt.showGrowBox","true");
			System.setProperty("apple.awt.graphics.UseQuartz","true");
			UIManager.put("SystemFont", new Font("Lucida Grande", Font.PLAIN, 13));
			UIManager.put("SmallSystemFont", new Font("Lucida Grande", Font.PLAIN, 11));
		}

		if (!lafLoaded) {
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