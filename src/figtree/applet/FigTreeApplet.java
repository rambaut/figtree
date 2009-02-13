package figtree.applet;

import jebl.evolution.trees.Tree;
import jebl.evolution.io.NewickImporter;
import jebl.evolution.io.ImportException;
import jam.controlpalettes.ControlPalette;
import jam.panels.SearchPanel;
import jam.panels.SearchPanelListener;
import jam.panels.StatusBar;
import figtree.application.FigTreeNexusImporter;
import figtree.treeviewer.*;
import figtree.panel.SimpleTreeViewer;
import figtree.panel.FigTreePanel;
import figtree.panel.SimpleControlPalette;

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
	private SimpleTreeViewer treeViewer;
	private ControlPalette controlPalette1;
	private ControlPalette controlPalette2;
	private FigTreePanel figTreePanel;

	private StatusBar statusBar;

	private SearchPanel filterPanel;
	private JPopupMenu filterPopup;

	public void init() {
		FigTreePanel.Style style = FigTreePanel.Style.DEFAULT;

		String styleParam = getParameter("style");
		if (styleParam != null) {
			style = FigTreePanel.Style.valueOf(styleParam.trim().toUpperCase());
		}

		treeViewer = new SimpleTreeViewer();
		controlPalette1 = new SimpleControlPalette();
		controlPalette2 = new SimpleControlPalette();

		figTreePanel = new FigTreePanel(treeViewer, controlPalette1, controlPalette2, style);

		filterPopup = new JPopupMenu();
		for (TreeViewer.TextSearchType searchType : TreeViewer.TextSearchType.values()) {
			filterPopup.add(searchType.toString());
		}
		filterPanel = new SearchPanel("Filter", filterPopup, true);
		filterPanel.setOpaque(false);
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
				TreeViewer.TextSearchType searchType = TreeViewer.TextSearchType.values()[index];
				treeViewer.selectTaxa("!name", searchType, searchString, false);
			}

			/**
			 * Called when the user presses the cancel search button or presses
			 * escape while the search is in focus.
			 */
			public void searchStopped() {
				treeViewer.clearSelectedTaxa();
			}
		});

		statusBar = new StatusBar("");
		statusBar.setStatusProvider(treeViewer);
		statusBar.add(filterPanel, BorderLayout.EAST);

		if (style != FigTreePanel.Style.ICARUS_SMALL) {
			getContentPane().add(statusBar, BorderLayout.NORTH);
		}

		getContentPane().add(figTreePanel, BorderLayout.CENTER);

	}

	public void start() {
		String tree = getParameter("tree");
		String treefile = getParameter("treefile");

		if (tree != null && tree.length() > 0) {
			URL fileURL = null;
			try {
				fileURL = new URL(tree);

				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileURL.openStream()));
				readData(bufferedReader, true);
			} catch (MalformedURLException e) {
				treeViewer.setStatusText(e.getMessage());
			} catch (IOException e) {
				treeViewer.setStatusText(e.getMessage());
			}
		} else if (treefile != null && treefile.length() > 0) {
			try {
				BufferedReader bufferedReader = new BufferedReader(new StringReader(treefile));
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

		try {
			Map<String, Object> settings = new HashMap<String, Object>();
			// First of all, fully populate the settings map so that
			// all the settings have defaults
			controlPalette1.getSettings(settings);
			controlPalette2.getSettings(settings);

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
			controlPalette1.setSettings(settings);
			controlPalette2.setSettings(settings);
		} catch (ImportException ie) {
			JOptionPane.showMessageDialog(this, "Error reading tree file: " + ie,
					"Import Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}


}
