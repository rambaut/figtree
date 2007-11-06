package figtree.applet;

import figtree.treeviewer.DefaultTreeViewer;
import jebl.evolution.trees.Tree;
import jebl.evolution.io.NewickImporter;
import jebl.evolution.io.ImportException;
import org.virion.jam.controlpalettes.BasicControlPalette;
import org.virion.jam.controlpalettes.ControlPalette;
import org.virion.jam.panels.SearchPanel;
import org.virion.jam.panels.SearchPanelListener;
import org.virion.jam.panels.StatusBar;
import org.virion.jam.toolbar.Toolbar;
import figtree.application.FigTreeNexusImporter;
import figtree.treeviewer.ExtendedTreeViewer;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: FigTreeApplet.java,v 1.1 2007/08/14 15:50:41 rambaut Exp $
 */
public class FigTreeApplet extends JApplet {
    private ExtendedTreeViewer treeViewer;
    private ControlPalette controlPalette;
    private FigTreeAppletPanel figTreePanel;

    private StatusBar statusBar;

    private SearchPanel filterPanel;
    private JPopupMenu filterPopup;

    public void init() {
        treeViewer = new ExtendedTreeViewer();
        controlPalette = new BasicControlPalette(200, BasicControlPalette.DisplayMode.ONLY_ONE_OPEN);

        figTreePanel = new FigTreeAppletPanel(treeViewer, controlPalette);

        Toolbar toolBar = new Toolbar();
        toolBar.setRollover(true);
        toolBar.setFloatable(false);

        toolBar.addFlexibleSpace();

        filterPopup = new JPopupMenu();
        for (DefaultTreeViewer.SearchType searchType : DefaultTreeViewer.SearchType.values()) {
            filterPopup.add(searchType.toString());
        }
        filterPanel = new SearchPanel("Filter", filterPopup, true);

        filterPanel.addSearchPanelListener(new SearchPanelListener() {

            /**
             * Called when the user requests a search by pressing return having
             * typed a search string into the text field. If the continuousUpdate
             * flag is true then this method is called when the user types into
             * the text field.
             *
             * @param searchString the user's search string
             */
            public void searchStarted(String searchString) {
                int index = filterPopup.getSelectionModel().getSelectedIndex();
                if (index == -1) index = 0;
                DefaultTreeViewer.SearchType searchType = DefaultTreeViewer.SearchType.values()[index];
                treeViewer.selectTaxa(searchType, searchString, false);
            }

            /**
             * Called when the user presses the cancel search button or presses
             * escape while the search is in focus.
             */
            public void searchStopped() {
                treeViewer.clearSelectedTaxa();
            }
        });

        JPanel panel3 = new JPanel(new FlowLayout());

        panel3.add(filterPanel);

        toolBar.addComponent(panel3);

        statusBar = new StatusBar("");
        statusBar.setStatusProvider(treeViewer);

        JPanel topPanel = new JPanel(new BorderLayout(0,0));
        topPanel.add(toolBar, BorderLayout.NORTH);
        topPanel.add(statusBar, BorderLayout.CENTER);

        getContentPane().setLayout(new java.awt.BorderLayout(0, 0));
        getContentPane().add(topPanel, BorderLayout.NORTH);

        getContentPane().add(figTreePanel, BorderLayout.CENTER);

    }

    public void start() {
        String tree = getParameter("tree");

        if (tree != null && tree.length() > 0) {
            URL fileURL = null;
            try {
                fileURL = new URL(tree);
            } catch (MalformedURLException e) {
                treeViewer.setStatusText(e.getMessage());
            }
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileURL.openStream()));
                readData(bufferedReader, true);
            } catch (IOException e) {
                treeViewer.setStatusText(e.getMessage());
            }
        } else {
            treeViewer.setStatusText("Tree not specified");
        }
    }

    public void stop() {
    }

    protected boolean readData(Reader reader, boolean isNexus) throws IOException {

        java.util.List<Tree> trees = new ArrayList<Tree>();

        boolean hasSettings = false;

        try {
            Map<String, Object> settings = new HashMap<String, Object>();
            // First of all, fully populate the settings map so that
            // all the settings have defaults
            controlPalette.getSettings(settings);

            if (isNexus) {
                FigTreeNexusImporter importer = new FigTreeNexusImporter(reader);
                while (importer.hasTree()) {
                    Tree tree = importer.importNextTree();
                    trees.add(tree);
                }
                // Try to find a figtree block and if found, parse the settings
                while (true) {
                    try {
                        importer.findNextBlock();
                        if (importer.getNextBlockName().equalsIgnoreCase("FIGTREE")) {
                            importer.parseFigTreeBlock(settings);
                            hasSettings = true;
                        }
                    } catch (EOFException ex) {
                        break;
                    }
                }
            } else {
                NewickImporter importer = new NewickImporter(reader, true);
                while (importer.hasTree()) {
                    Tree tree = importer.importNextTree();
                    trees.add(tree);
                }
            }

            if (trees.size() == 0) {
                throw new ImportException("This file contained no trees.");
            }
            treeViewer.setTrees(trees);
            controlPalette.setSettings(settings);
        } catch (ImportException ie) {
            JOptionPane.showMessageDialog(this, "Error reading tree file: " + ie,
                    "Import Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }


}
