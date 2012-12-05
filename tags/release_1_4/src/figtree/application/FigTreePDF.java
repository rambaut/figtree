/*
 * FigTreePDF.java
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

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import figtree.treeviewer.ExtendedTreeViewer;
import jam.controlpalettes.BasicControlPalette;
import jam.controlpalettes.ControlPalette;

import java.io.*;
import java.util.*;
import java.util.List;
import java.awt.*;

import jebl.evolution.io.ImportException;
import jebl.evolution.io.NewickImporter;
import jebl.evolution.trees.Tree;

public class FigTreePDF {

    public static final String VERSION = "1.4";
    public static final String DATES = "2006-2012";

    static public void createGraphic(int width, int height, String treeFileName, String graphicFileName) {

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

            OutputStream stream;
            if (graphicFileName != null) {
                stream = new FileOutputStream(graphicFileName);
            } else {
                stream = System.out;
            }

            Document document = new Document();
            document.setPageSize(new com.itextpdf.text.Rectangle(width, height));
            try {
                PdfWriter writer = PdfWriter.getInstance(document, stream);
                document.open();
                PdfContentByte cb = writer.getDirectContent();
                PdfTemplate tp = cb.createTemplate(width, height);
                Graphics2D g2 = tp.createGraphics(width, height);
                tp.setWidth(width);
                tp.setHeight(height);
                treeViewer.getContentPane().print(g2);
                g2.dispose();
                tp.sanityCheck(); // all the g2 content is written to tp, not cb
                cb.addTemplate(tp, 0, 0);
                cb.sanityCheck();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
            document.close();

        } catch(ImportException ie) {
            throw new RuntimeException("Error writing graphic file: " + ie);
        } catch(IOException ioe) {
            throw new RuntimeException("Error writing graphic file: " + ioe);
        }

    }

    // Main entry point
    static public void main(String[] args) {

        Arguments arguments = new Arguments(
                new Arguments.Option[] {
                        new Arguments.IntegerOption("width", "the width of the graphic in pixels"),
                        new Arguments.IntegerOption("height", "the height of the graphic in pixels")
                });

        try {
            arguments.parseArguments(args);
        } catch (Arguments.ArgumentException ae) {
            System.out.println();
            System.out.println(ae.getMessage());
            System.out.println();
            System.exit(1);
        }


        int width = 800;
        int height = 600;

        if (arguments.hasOption("width")) {
            width = arguments.getIntegerOption("width");
        }

        if (arguments.hasOption("height")) {
            height = arguments.getIntegerOption("height");
        }

        // command line version...
        String[] args2 = arguments.getLeftoverArguments();

        if (args2.length == 0) {
            // no tree file specified
            System.exit(0);
        } else if (args2.length == 1) {
            // no graphic file specified - write to stdout
            createGraphic(width, height, args2[0], (args2.length > 1 ? args2[1] : null));
            System.exit(0);
        } else {
            createGraphic(width, height, args2[0], (args2.length > 1 ? args2[1] : null));
            System.exit(0);
        }
    }
}

