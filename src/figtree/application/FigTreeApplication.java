/*
 * FigTreeApplication.java
 *
 * Copyright (C) 2012 Andrew Rambaut
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

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
import jam.framework.*;
import jam.controlpalettes.BasicControlPalette;
import jam.controlpalettes.ControlPalette;
import jam.mac.Utils;
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

import jebl.evolution.io.ImportException;
import jebl.evolution.io.NewickImporter;
import jebl.evolution.trees.Tree;

import javax.swing.*;

import ch.randelshofer.quaqua.QuaquaManager;

public class FigTreeApplication extends MultiDocApplication {

    public static final String VERSION = "1.4.1";
    public static final String DATES = "2006-2013";

    public static FigTreeApplication application;

    public FigTreeApplication(MenuBarFactory menuBarFactory, String nameString, String aboutString, Icon icon,
                              String websiteURLString,
                              String helpURLString) {
        super(menuBarFactory, nameString, aboutString, icon, websiteURLString, helpURLString);

//        addPreferencesSection(new GeneralPreferencesSection());
        addPreferencesSection(new AppearancePreferencesSection());
        addPreferencesSection(new FontsPreferencesSection());
//        addPreferencesSection(new AdvancedPreferencesSection());
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
            ControlPalette controlPalette = new BasicControlPalette(FigTreePanel.CONTROL_PALETTE_WIDTH, BasicControlPalette.DisplayMode.ONLY_ONE_OPEN);
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

            OutputStream stream;
            if (graphicFileName != null) {
                stream = new FileOutputStream(graphicFileName);
            } else {
                stream = System.out;
            }

            Properties p = new Properties();
//	        p.setProperty("PageSize","A5");
            VectorGraphics g;

            if (graphicFormat.equals("PDF")) {
                if (graphicFileName != null) {
                    System.out.println("Creating PDF graphic: " + graphicFileName);
                }
                g = new PDFGraphics2D(stream, new Dimension(width, height));
            } else if (graphicFormat.equals("PS")) {
                if (graphicFileName != null) {
                    System.out.println("Creating PS graphic: " + graphicFileName);
                }
                g = new PSGraphics2D(stream, new Dimension(width, height));
            } else if (graphicFormat.equals("EMF")) {
                if (graphicFileName != null) {
                    System.out.println("Creating EMF graphic: " + graphicFileName);
                }
                g = new EMFGraphics2D(stream, new Dimension(width, height));
            } else if (graphicFormat.equals("SVG")) {
                if (graphicFileName != null) {
                    System.out.println("Creating SVG graphic: " + graphicFileName);
                }
                g = new SVGGraphics2D(stream, new Dimension(width, height));
            } else if (graphicFormat.equals("SWF")) {
                if (graphicFileName != null) {
                    System.out.println("Creating SWF graphic: " + graphicFileName);
                }
                g = new SWFGraphics2D(stream, new Dimension(width, height));
            } else if (graphicFormat.equals("GIF")) {
                if (graphicFileName != null) {
                    System.out.println("Creating GIF graphic: " + graphicFileName);
                }
                g = new GIFGraphics2D(stream, new Dimension(width, height));
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
        centreLine("FigTree v" + VERSION + ", " + DATES, 60);
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
        centreLine("Tulio de Oliveira, Oliver Pybus, Beth Shapiro & Marc Suchard", 60);
        System.out.println();
    }

    public static void printUsage(Arguments arguments) {

        arguments.printUsage("figtree", "[<tree-file-name>] [<graphic-file-name>]");
        System.out.println();
        System.out.println("  Example: figtree test.tree");
        System.out.println("  Example: figtree -graphic PDF test.tree test.pdf");
        System.out.println("  Example: figtree -graphic GIF -width 320 -height 320 test.tree test.gif");
        System.out.println();
    }

    private static boolean lafLoaded = false;

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

            if (args2.length == 0) {
                // no tree file specified
                printTitle();
                printUsage(arguments);
                System.exit(0);
            } else if (args2.length == 1) {
                // no graphic file specified - write to stdout
                createGraphic(graphicFormat, width, height, args2[0], (args2.length > 1 ? args2[1] : null));
                System.exit(0);
            } else {
                printTitle();
                createGraphic(graphicFormat, width, height, args2[0], (args2.length > 1 ? args2[1] : null));
                System.exit(0);
            }
        }

        if (Utils.isMacOSX()) {
            if (Utils.getMacOSXMajorVersionNumber() >= 5) {
                System.setProperty("apple.awt.brushMetalLook","true");
            }

            System.setProperty("apple.laf.useScreenMenuBar","true");
            System.setProperty("apple.awt.draggableWindowBackground","true");
            System.setProperty("apple.awt.showGrowBox","true");
            System.setProperty("apple.awt.graphics.UseQuartz","true");

            try {
                // set the Quaqua Look and Feel in the UIManager
                javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        try {
                            // Only override the UI's necessary for ColorChooser and
                            // FileChooser:
                            Set includes = new HashSet();
                            includes.add("ColorChooser");
                            includes.add("FileChooser");
                            includes.add("Component");
                            includes.add("Browser");
                            includes.add("Tree");
                            includes.add("SplitPane");
                            includes.add("TitledBorder");

                            try {
                                QuaquaManager.setIncludedUIs(includes);
                            } catch (java.lang.NoClassDefFoundError ncdfe) {
                                // this is to protect against the figtree.jar being
                                // run on Mac OS without Quaqua on the classpath
                            }

                            UIManager.setLookAndFeel(
                                    "ch.randelshofer.quaqua.QuaquaLookAndFeel"
                            );

                            lafLoaded = true;
                        } catch (Exception e) {
                        }
                    }
                });
            } catch (Exception e) {
            }

            UIManager.put("SystemFont", new Font("Lucida Grande", Font.PLAIN, 13));
            UIManager.put("SmallSystemFont", new Font("Lucida Grande", Font.PLAIN, 11));
        }

        if (!lafLoaded) {
            try {
                // set the System Look and Feel in the UIManager
                javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        try {
                            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        java.net.URL url = FigTreeApplication.class.getResource("images/figtreeLogo.png");
        Icon icon = null;

        if (url != null) {
            icon = new ImageIcon(url);
        }

        final String nameString = "FigTree";
        String aboutString = "<html><center>Tree Figure Drawing Tool<br>Version " + VERSION + "<br>" + DATES + ", Andrew Rambaut<br>" +
                "Institute of Evolutionary Biology, University of Edinburgh.<br><br>" +
                "<a href=\"http://tree.bio.ed.ac.uk/\">http://tree.bio.ed.ac.uk/</a><br><br>" +
                "Uses the Java Evolutionary Biology Library (JEBL)<br>" +
                "<a href=\"http://sourceforge.net/projects/jebl/\">http://jebl.sourceforge.net/</a><br><br>" +
                "Thanks to Alexei Drummond, Joseph Heled, Philippe Lemey, <br>Tulio de Oliveira, Oliver Pybus, Beth Shapiro & Marc Suchard</center></html>";

        String websiteURLString = "http://tree.bio.ed.ac.uk/software/figtree/";
        String helpURLString = "http://tree.bio.ed.ac.uk/software/figtree/";

        FigTreeApplication.application = new FigTreeApplication(new FigTreeMenuBarFactory(), nameString, aboutString, icon,
                websiteURLString, helpURLString);

        application.setDocumentFrameFactory(new DocumentFrameFactory() {
            public DocumentFrame createDocumentFrame(Application app, MenuBarFactory menuBarFactory) {
                return new FigTreeFrame(nameString + " v" + VERSION);
            }
        });

        application.initialize();

        if (args.length > 0) {
            for (String arg : args) {
                application.doOpen(arg);
            }
        }

//		if (!jam.mac.Utils.isMacOSX() && application.getUpperDocumentFrame() == null) {
//			// If we haven't opened any files by now, prompt for one...
//			application.doOpen();
//		}

        if (application.getUpperDocumentFrame() == null) {
            // If we haven't opened any files by now, open a blank window...
            application.doNew();
        }
    }

}