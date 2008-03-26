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

import figtree.application.preferences.*;
import figtree.treeviewer.ExtendedTreeViewer;
import org.virion.jam.framework.*;
import org.virion.jam.controlpalettes.BasicControlPalette;
import org.virion.jam.controlpalettes.ControlPalette;
import org.virion.jam.app.Arguments;
import org.virion.jam.mac.Utils;
import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.ps.PSGraphics2D;
import org.freehep.graphicsio.pdf.PDFGraphics2D;
import org.freehep.graphicsio.emf.EMFGraphics2D;
import org.freehep.graphicsio.svg.SVGGraphics2D;
import org.freehep.graphicsio.gif.GIFGraphics2D;
import org.freehep.graphicsio.swf.SWFGraphics2D;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import ch.randelshofer.quaqua.QuaquaLookAndFeel;
import jebl.evolution.io.ImportException;
import jebl.evolution.io.NewickImporter;
import jebl.evolution.trees.Tree;

import javax.swing.*;

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

    public DocumentFrame doOpenFile(File file) {
        DocumentFrame documentFrame = getUpperDocumentFrame();
        if (documentFrame != null && documentFrame.getFile() == null) {
            documentFrame.openFile(file);
            return documentFrame;
        } else {
            return super.doOpenFile(file);
        }
    }

    public void doPaste() {

    }

    static public void createGraphic(String graphicFormat, int width, int height, String treeFileName, String graphicFileName) {

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
            FigTreePanel figTreePanel = new FigTreePanel(null, treeViewer, controlPalette);

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

	        treeViewer.getContentPane().setSize(width, height);

	        File file = new File(graphicFileName);

	        Properties p = new Properties();
	        p.setProperty("PageSize","A5");
	        VectorGraphics g;

	        if (graphicFormat.equals("PDF")) {
		        System.out.println("Creating PDF graphic: " + graphicFileName);
		        g = new PDFGraphics2D(file, new Dimension(width, height));
	        } else if (graphicFormat.equals("PS")) {
		        System.out.println("Creating PS graphic: " + graphicFileName);
		        g = new PSGraphics2D(file, new Dimension(width, height));
	        } else if (graphicFormat.equals("EMF")) {
		        System.out.println("Creating EMF graphic: " + graphicFileName);
		        g = new EMFGraphics2D(file, new Dimension(width, height));
	        } else if (graphicFormat.equals("SVG")) {
		        System.out.println("Creating SVG graphic: " + graphicFileName);
		        g = new SVGGraphics2D(file, new Dimension(width, height));
	        } else if (graphicFormat.equals("SWF")) {
		        System.out.println("Creating SWF graphic: " + graphicFileName);
		        g = new SWFGraphics2D(file, new Dimension(width, height));
	        } else if (graphicFormat.equals("GIF")) {
		        System.out.println("Creating GIF graphic: " + graphicFileName);
		        g = new GIFGraphics2D(file, new Dimension(width, height));
//	        } else if (graphicFormat.equals("PNG")) {
//		        g = new PNGGraphics2D(file, new Dimension(width, height));
//	        } else if (graphicFormat.equals("JPEG")) {
//		        g = new JPEGGraphics2D(file, new Dimension(width, height));
	        } else {
				throw new RuntimeException("Unknown graphic format");
	        }

	        g.setProperties(p);
	        g.startExport();
			treeViewer.getContentPane().print(g);
	        g.endExport();

        } catch(ImportException ie) {
            throw new RuntimeException("Error writing graphic file: " + ie);
        } catch(IOException ioe) {
            throw new RuntimeException("Error writing graphic file: " + ioe);
        }

    }

    public static void centreLine(String line, int pageWidth) {
        int n = pageWidth - line.length();
        int n1 = n / 2;
        for (int i = 0; i < n1; i++) { System.out.print(" "); }
        System.out.println(line);
    }

    public static void printTitle() {
        System.out.println();
        centreLine("FigTree v1.1.3, 2006-2008", 60);
        centreLine("Tree Figure Drawing Tool", 60);
        centreLine("Andrew Rambaut", 60);
        System.out.println();
        centreLine("Institute of Evolutionary Biology", 60);
        centreLine("University of Edinburgh", 60);
        centreLine("a.rambaut@ed.ac.uk", 60);
        System.out.println();
        centreLine("http://tree.bio.ed.ac.uk/", 60);
        centreLine("Uses the Java Evolutionary Biology Library (JEBL)", 60);
        centreLine("http://jebl.sourceforge.net/", 60);
        centreLine("Thanks to Alexei Drummond, Joseph Heled, Philippe Lemey, ", 60);
        centreLine("Tulio de Oliveira & Beth Shapiro", 60);
	    System.out.println();
    }

    public static void printUsage(Arguments arguments) {

        arguments.printUsage("figtree", "[<tree-file-name>] [<graphic-file-name>]");
        System.out.println();
        System.out.println("  Example: figtree test.tree");
        System.out.println("  Example: figtree -graphic PDF test.tree test.pdf");
	    System.out.println("  Example: figtree -graphic PNG -width 320 -height 320 test.tree test.png");
        System.out.println();
    }

    // Main entry point
    static public void main(String[] args) {

        Arguments arguments = new Arguments(
                new Arguments.Option[] {
                        new Arguments.StringOption("graphic", new String[] {
                                "PDF", "SVG", "SWF", "PS", "EMF",
		                        // "PNG",
		                        "GIF",
		                        // "JPEG"
                        }, false, "produce a graphic with the given format"),
		                new Arguments.IntegerOption("width", "the width of the graphic in pixels"),
		                new Arguments.IntegerOption("height", "the height of the graphic in pixels"),
                        new Arguments.Option("help", "option to print this message")
                });

        try {
            arguments.parseArguments(args);
        } catch (Arguments.ArgumentException ae) {
            System.out.println();
            System.out.println(ae.getMessage());
            System.out.println();
            printTitle();
            printUsage(arguments);
            System.exit(1);
        }

        if (arguments.hasOption("help")) {
            printTitle();
            printUsage(arguments);
            System.exit(0);
        }

        if (arguments.hasOption("graphic")) {

	        int width = 800;
	        int height = 600;

	        if (arguments.hasOption("width")) {
		        width = arguments.getIntegerOption("width");
	        }

	        if (arguments.hasOption("height")) {
		        height = arguments.getIntegerOption("height");
	        }

            // command line version...
            String graphicFormat = arguments.getStringOption("graphic");
            String[] args2 = arguments.getLeftoverArguments();

            printTitle();
            createGraphic(graphicFormat, width, height, args2[0], args2[1]);
            System.exit(0);
        }

        boolean lafLoaded = false;

        if (Utils.isMacOSX()) {
            if (Utils.getMacOSXVersion().startsWith("10.5")) {
                System.setProperty("apple.awt.brushMetalLook","true");
            }

            System.setProperty("apple.laf.useScreenMenuBar","true");
            System.setProperty("apple.awt.draggableWindowBackground","true");
            System.setProperty("apple.awt.showGrowBox","true");
            System.setProperty("apple.awt.graphics.UseQuartz","true");

            // set the Quaqua Look and Feel in the UIManager
            try {
                //System.setProperty("Quaqua.Debug.showClipBounds","true");
                //System.setProperty("Quaqua.Debug.showVisualBounds","true");
                LookAndFeel lafClass;

                if (Utils.getMacOSXVersion().startsWith("10.5")) {
                    lafClass = ch.randelshofer.quaqua.subset.Quaqua14ColorChooserLAF.class.newInstance();
                } else {
                    lafClass = QuaquaLookAndFeel.class.newInstance();
                }

                UIManager.setLookAndFeel(lafClass);

                lafLoaded = true;
            } catch (Exception e) {
            }

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

        java.net.URL url = FigTreeApplication.class.getResource("images/figtreeLogo.png");
        Icon icon = null;

        if (url != null) {
            icon = new ImageIcon(url);
        }

        final String nameString = "FigTree";
        final String versionString = "1.1.3";
        String aboutString = "<html><center>Tree Figure Drawing Tool<br>Version " + versionString + "<br>2006-2008, Andrew Rambaut<br>" +
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

//		if (!org.virion.jam.mac.Utils.isMacOSX() && application.getUpperDocumentFrame() == null) {
//			// If we haven't opened any files by now, prompt for one...
//			application.doOpen();
//		}

        if (application.getUpperDocumentFrame() == null) {
            // If we haven't opened any files by now, open a blank window...
            application.doNew();
        }
    }

}