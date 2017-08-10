/*
 * FigTreeApplication.java
 *
 * Copyright (C) 2006-2014 Andrew Rambaut
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
 * @version			$Id$
 */

package figtree.application;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import figtree.application.preferences.*;
import figtree.treeviewer.ExtendedTreeViewer;
import jam.framework.*;
import jam.controlpalettes.BasicControlPalette;
import jam.controlpalettes.ControlPalette;
import jam.mac.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;

import jebl.evolution.io.ImportException;
import jebl.evolution.io.NewickImporter;
import jebl.evolution.trees.Tree;

import javax.imageio.ImageIO;
import javax.swing.*;

import ch.randelshofer.quaqua.QuaquaManager;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;

/**
 * Application class for FigTree including main() method for invoking it.
 * Uses JAM Application classes to create a MultiDoc Application.
 *
 * @author Andrew Rambaut
 * @version $Id$
 *
 * $HeadURL$
 *
 * $LastChangedBy$
 * $LastChangedDate$
 * $LastChangedRevision$
 */
public class FigTreeApplication extends MultiDocApplication {

    public static final String VERSION = "1.4.4pre";
    public static final String DATES = "2006-2017";

    public static FigTreeApplication application;

    public FigTreeApplication(MenuBarFactory menuBarFactory, String nameString,  String titleString, String aboutString, Icon icon,
                              String websiteURLString,
                              String helpURLString) {
        super(menuBarFactory, nameString, titleString, aboutString, icon, websiteURLString, helpURLString);

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

            GraphicFormat format = null;
            if (graphicFormat.equals("PDF")) {
                format = GraphicFormat.PDF;
            } else if (graphicFormat.equals("SVG")) {
                format = GraphicFormat.SVG;
            } else if (graphicFormat.equals("GIF")) {
                format = GraphicFormat.GIF;
            } else if (graphicFormat.equals("PNG")) {
                format = GraphicFormat.PNG;
            } else if (graphicFormat.equals("JPEG")) {
                format = GraphicFormat.JPEG;
            } else {
                throw new RuntimeException("Unknown graphic format");
            }

            if (graphicFileName != null) {
                System.out.println("Creating " + graphicFormat + " graphic: " + graphicFileName);
            }

            FigTreeFrame.exportGraphics(format, treeViewer.getContentPane(), stream);

        } catch(ImportException ie) {
            throw new RuntimeException("Error writing graphic file: " + ie.getMessage());
        } catch(IOException ioe) {
            throw new RuntimeException("Error writing graphic file: " + ioe.getMessage());
        } catch (DocumentException de) {
            throw new RuntimeException("Error writing graphic file: " + de.getMessage());
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
        centreLine("Uses the Java Evolutionary Biology 2 Library (JEBL2)", 60);
        centreLine("http://jebl2.googlecode.com/", 60);
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

        // There is a major issue with languages that use the comma as a decimal separator.
        // To ensure compatibility between programs in the package, enforce the US locale.
        //Locale.setDefault(Locale.US);

        Arguments arguments = new Arguments(
                new Arguments.Option[] {
                        new Arguments.StringOption("graphic", new String[] {
                                "PDF",
                                "SVG",
                                // "SWF", "PS", "EMF",
                                "PNG",
                                // "GIF",
                                "JPEG"
                        }, false, "produce a graphic with the given format"),
                        new Arguments.IntegerOption("width", "the width of the graphic in pixels"),
                        new Arguments.IntegerOption("height", "the height of the graphic in pixels"),
                        new Arguments.Option("url", "the input file is a URL"),
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
            UIManager.LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
            for (UIManager.LookAndFeelInfo laf : lafs) {
                System.out.println(laf);
            }

            try {
                // set the System Look and Feel in the UIManager
                javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        try {
                            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//                              UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
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
        String titleString = "<html>" +
                "<div style=\"font-family:'Helvetica Neue', Helvetica, Arial, 'Lucida Grande',sans-serif\">" +
                "<p style=\"font-weight: 100; font-size: 36px\">FigTree</p>" +
                "<p style=\"font-weight: 200; font-size: 14px\">Tree Figure Drawing Tool</p>" +
                "<p style=\"font-weight: 300; font-size: 12px\">Version " + VERSION + "</p>" +
                "</div></html>";
        String aboutString = "<html>" +
                "<div style=\"font-family:'Helvetica Neue', Helvetica, Arial, 'Lucida Grande',sans-serif\">" +
                "<center>"+ DATES + ", Andrew Rambaut<br>" +
                "Institute of Evolutionary Biology, University of Edinburgh.<br>" +
                "<a href=\"http://tree.bio.ed.ac.uk/\">http://tree.bio.ed.ac.uk/</a><br><br>" +
                "Source code available from:<br>" +
                "<a href=\"https://figtree.googlecode.com/\">http://figtree.googlecode.com/</a><br><br>" +
                "Uses the Java Evolutionary Biology 2 Library (JEBL2)<br>" +
                "<a href=\"https://jebl2.googlecode.com/\">http://jebl2.googlecode.com/</a><br><br>" +
                "Thanks to Alexei Drummond, Joseph Heled, Philippe Lemey, <br>Tulio de Oliveira, Oliver Pybus, Beth Shapiro & Marc Suchard</center>" +
                "</div></html>";

        String websiteURLString = "http://tree.bio.ed.ac.uk/software/figtree/";
        String helpURLString = "http://tree.bio.ed.ac.uk/software/figtree/";

        FigTreeApplication.application = new FigTreeApplication(new FigTreeMenuBarFactory(), nameString, titleString, aboutString, icon,
                websiteURLString, helpURLString);

        application.setDocumentFrameFactory(new DocumentFrameFactory() {
            public DocumentFrame createDocumentFrame(Application app, MenuBarFactory menuBarFactory) {
                return new FigTreeFrame(nameString + " v" + VERSION);
            }
        });

        application.initialize();

        boolean useURLs = arguments.hasOption("url");
        String[] leftoverArguments = arguments.getLeftoverArguments();
        if (leftoverArguments.length > 0) {
            for (String arg : leftoverArguments) {
                if (useURLs) {
                    FigTreeFrame frame = (FigTreeFrame)application.doNew();
                    try {
                        frame.readFromURL(new URL(arg));
                    } catch (IOException e) {
                    }
                } else {
                    application.doOpen(arg);
                }
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