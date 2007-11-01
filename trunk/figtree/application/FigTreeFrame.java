package figtree.application;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.*;
import jebl.evolution.alignments.Alignment;
import jebl.evolution.alignments.BasicAlignment;
import jebl.evolution.graphs.Node;
import jebl.evolution.io.*;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.Tree;
import jebl.gui.trees.treeviewer_dev.*;
import jebl.util.Attributable;
import org.virion.jam.controlpalettes.BasicControlPalette;
import org.virion.jam.controlpalettes.ControlPalette;
import org.virion.jam.framework.DocumentFrame;
import org.virion.jam.panels.*;
import org.virion.jam.toolbar.*;
import org.virion.jam.util.IconUtils;
import figtree.application.menus.TreeMenuHandler;
import figtree.treeviewer.ExtendedTreeViewer;
import figtree.treeviewer.FindDialog;
import figtree.treeviewer.annotations.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

public class FigTreeFrame extends DocumentFrame implements TreeMenuHandler {
	private final ExtendedTreeViewer treeViewer;
	private final ControlPalette controlPalette;
	private final FigTreePanel figTreePanel;

	private StatusBar statusBar;

	private SearchPanel filterPanel;
	private JPopupMenu filterPopup;

	private ToolbarToggleButton cartoonToolButton;
	private ToolbarToggleButton collapseToolButton;
	private ToolbarToggleButton rerootToolButton;
	private ToolbarToggleButton rotateToolButton;
	private ToolbarToggleButton annotationToolButton;
	private ToolbarToggleButton colourToolButton;

	public FigTreeFrame(String title) {
		super();

		setTitle(title);


		setImportAction(importAction);
		//       setImportAction(importCharactersAction);
		setExportAction(exportAction);

		treeViewer = new ExtendedTreeViewer();
		controlPalette = new BasicControlPalette(200, BasicControlPalette.DisplayMode.ONLY_ONE_OPEN);

		figTreePanel = new FigTreePanel(treeViewer, controlPalette);
	}

	public void initializeComponents() {

		setSize(new java.awt.Dimension(1024, 768));

		Toolbar toolBar = new Toolbar();
		toolBar.setRollover(true);
		toolBar.setFloatable(false);

		Icon rerootToolIcon = IconUtils.getIcon(this.getClass(), "images/rerootTool.png");
		Icon rotateToolIcon = IconUtils.getIcon(this.getClass(), "images/rotateTool.png");
		Icon cartoonNodeToolIcon = IconUtils.getIcon(this.getClass(), "images/cartoonNodeTool.png");
		Icon collapseNodeToolIcon = IconUtils.getIcon(this.getClass(), "images/collapseNodeTool.png");
		Icon annotationToolIcon = IconUtils.getIcon(this.getClass(), "images/annotationTool.png");
		Icon findToolIcon = IconUtils.getIcon(this.getClass(), "images/findTool.png");
		Icon infoToolIcon = IconUtils.getIcon(this.getClass(), "images/infoTool.png");
		Icon statisticsToolIcon = IconUtils.getIcon(this.getClass(), "images/statisticsTool.png");
		Icon settingsToolIcon = IconUtils.getIcon(this.getClass(), "images/projectTool.png");
		Icon colourToolIcon = IconUtils.getIcon(this.getClass(), "images/coloursTool.png");

		Icon nextButtonIcon = IconUtils.getIcon(this.getClass(), "images/nextButton.png");
		Icon nextButtonInactiveIcon = IconUtils.getIcon(this.getClass(), "images/nextButtonInactive.png");
		Icon nextButtonPressedIcon = IconUtils.getIcon(this.getClass(), "images/nextButtonPressed.png");

		Icon previousButtonIcon = IconUtils.getIcon(this.getClass(), "images/previousButton.png");
		Icon previousButtonInactiveIcon = IconUtils.getIcon(this.getClass(), "images/previousButtonInactive.png");
		Icon previousButtonPressedIcon = IconUtils.getIcon(this.getClass(), "images/previousButtonPressed.png");

		cartoonAction = new ToolbarAction("Cartoon", CARTOON_NODE, cartoonNodeToolIcon) {
			public void actionPerformed(ActionEvent e){
				cartoonSelected(cartoonToolButton.isSelected());
			}
		};

		cartoonToolButton = new ToolbarToggleButton(cartoonAction, true);
		toolBar.addComponent(cartoonToolButton);
		cartoonToolButton.setEnabled(false);

		collapseAction = new ToolbarAction("Collapse", COLLAPSE_NODE, collapseNodeToolIcon) {
			public void actionPerformed(ActionEvent e){
				collapseSelected(collapseToolButton.isSelected());
			}
		};
		collapseToolButton = new ToolbarToggleButton(collapseAction, true);
		toolBar.addComponent(collapseToolButton);
		collapseToolButton.setEnabled(false);

		rerootAction = new ToolbarAction("Reroot", ROOT_ON_BRANCH, rerootToolIcon) {
			public void actionPerformed(ActionEvent e){
				rerootTree(rerootToolButton.isSelected());
			}
		};
		rerootToolButton = new ToolbarToggleButton(rerootAction, true);
		toolBar.addComponent(rerootToolButton);
		rerootToolButton.setEnabled(false);

		rotateAction = new ToolbarAction("Rotate", ROTATE_NODE, rotateToolIcon) {
			public void actionPerformed(ActionEvent e){
				rotateTree(rotateToolButton.isSelected());
			}
		};
		rotateToolButton = new ToolbarToggleButton(rotateAction, true);
		toolBar.addComponent(rotateToolButton);
		rotateToolButton.setEnabled(false);

		annotateAction =  new ToolbarAction("Annotate", ANNOTATE, annotationToolIcon) {
			public void actionPerformed(ActionEvent e){
				annotateSelected(annotationToolButton.isSelected());
			}
		};
		annotationToolButton = new ToolbarToggleButton(annotateAction, true);
		toolBar.addComponent(annotationToolButton);
		annotationToolButton.setEnabled(false);

		colourAction = new ToolbarAction("Colour", COLOUR, colourToolIcon) {
			public void actionPerformed(ActionEvent e){
				colourSelected(colourToolButton.isSelected());
			}
		};
		colourToolButton = new ToolbarToggleButton(colourAction, true);
		toolBar.addComponent(colourToolButton);
		colourToolButton.setEnabled(false);

		ButtonGroup group = new ButtonGroup();
		group.add(cartoonToolButton);
		group.add(collapseToolButton);
		group.add(rerootToolButton);
		group.add(rotateToolButton);
		group.add(annotationToolButton);
		group.add(colourToolButton);

		toolBar.addSeparator();

		findAction = new ToolbarAction("Find", "Find Taxa...", findToolIcon) {
			public void actionPerformed(ActionEvent e){
				doFind();
			}
		};
		JButton findToolButton = new ToolbarButton(findAction);
		toolBar.addComponent(findToolButton);
		findToolButton.setEnabled(true);

//        final ToolbarAction infoToolbarAction = new ToolbarAction("Get Info", "Get Info...", infoToolIcon) {
//            public void actionPerformed(ActionEvent e){
//                treeViewer.showInfomation();
//            }
//        };
//        JButton infoToolButton = new ToolbarButton(infoToolbarAction);
//        infoToolButton.putClientProperty("Quaqua.Button.style", "toolBarRollover");
//        toolBar.addComponent(infoToolButton);
//        infoToolButton.setEnabled(false);
//
//        JButton settingsToolButton = new ToolbarButton(
//                new ToolbarAction("Statistics", "Statistics...", statisticsToolIcon) {
//                    public void actionPerformed(ActionEvent e){
//                        treeViewer.showStatistics();
//                    }
//                });
//        settingsToolButton.putClientProperty("Quaqua.Button.style", "toolBarRollover");
//        toolBar.addComponent(settingsToolButton);
//        settingsToolButton.setEnabled(false);

		toolBar.addSeparator();

		Box box1 = Box.createHorizontalBox();
		final JToggleButton toggle1 = new JToggleButton("Node");
		final JToggleButton toggle2 = new JToggleButton("Clade");
		final JToggleButton toggle3 = new JToggleButton("Taxa");
		toggle1.putClientProperty( "Quaqua.Button.style", "toggleWest");
		toggle2.putClientProperty( "Quaqua.Button.style", "toggleCenter");
		toggle3.putClientProperty( "Quaqua.Button.style", "toggleEast");
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(toggle1);
		buttonGroup.add(toggle2);
		buttonGroup.add(toggle3);
		toggle1.setSelected(true);
		toggle1.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					treeViewer.setSelectionMode(TreePaneSelector.SelectionMode.NODE);
				}
			}
		});
		toggle2.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					treeViewer.setSelectionMode(TreePaneSelector.SelectionMode.CLADE);
				}
			}
		});
		toggle3.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					treeViewer.setSelectionMode(TreePaneSelector.SelectionMode.TAXA);
				}
			}
		});
		box1.add(Box.createVerticalStrut(annotationToolIcon.getIconHeight()));
		box1.add(toggle1);
		box1.add(toggle2);
		box1.add(toggle3);
		toolBar.addComponent(new GenericToolbarItem("Selection Mode", "What aspect of the tree is selected when it is clicked", box1));

		toolBar.addSeparator();

//        Box box2 = Box.createHorizontalBox();
//        final JToggleButton toggle4 = new JToggleButton("Select");
//        final JToggleButton toggle5 = new JToggleButton("Scroll");
//        toggle4.putClientProperty( "Quaqua.Button.style", "toggleWest");
//        toggle5.putClientProperty( "Quaqua.Button.style", "toggleEast");
//        ButtonGroup buttonGroup2 = new ButtonGroup();
//        buttonGroup2.add(toggle4);
//        buttonGroup2.add(toggle5);
//        toggle4.setSelected(true);
//        toggle4.addItemListener(new ItemListener() {
//            public void itemStateChanged(ItemEvent e) {
//                if (e.getStateChange() == ItemEvent.SELECTED) {
//                    treeViewer.setDragMode(TreePaneSelector.DragMode.SELECT);
//                }
//            }
//        });
//        toggle5.addItemListener(new ItemListener() {
//            public void itemStateChanged(ItemEvent e) {
//                if (e.getStateChange() == ItemEvent.SELECTED) {
//                    treeViewer.setDragMode(TreePaneSelector.DragMode.SCROLL);
//                }
//            }
//        });
//        box2.add(Box.createVerticalStrut(annotationToolIcon.getIconHeight()));
//        box2.add(toggle4);
//        box2.add(toggle5);
//        toolBar.addComponent(new GenericToolbarItem("Drag mode", "Mode for when the mouse is clicked and dragged", box2));

		toolBar.addFlexibleSpace();

		final ToolbarAction prevTreeToolbarAction =
				new ToolbarAction("Prev", "Previous Tree...",
						previousButtonIcon, previousButtonInactiveIcon, previousButtonPressedIcon) {
					public void actionPerformed(ActionEvent e){
						treeViewer.showPreviousTree();
					}
				};
		JButton prevTreeToolButton = new ToolbarButton(prevTreeToolbarAction);
		prevTreeToolButton.putClientProperty("Quaqua.Button.style", "toolBarRollover");
		toolBar.addComponent(prevTreeToolButton);

		final ToolbarAction nextTreeToolbarAction =
				new ToolbarAction("Next", "Next Tree...",
						nextButtonIcon, nextButtonInactiveIcon, nextButtonPressedIcon) {
					public void actionPerformed(ActionEvent e){
						treeViewer.showNextTree();
					}
				};
		JButton nextTreeToolButton = new ToolbarButton(nextTreeToolbarAction);
		nextTreeToolButton.putClientProperty("Quaqua.Button.style", "toolBarRollover");
		toolBar.addComponent(nextTreeToolButton);

		nextTreeToolbarAction.setEnabled(treeViewer.getCurrentTreeIndex() < treeViewer.getTreeCount() - 1);
		prevTreeToolbarAction.setEnabled(treeViewer.getCurrentTreeIndex() > 0);

		treeViewer.addTreeViewerListener(new TreeViewerListener() {
			public void treeChanged() {
				nextTreeToolbarAction.setEnabled(treeViewer.getCurrentTreeIndex() < treeViewer.getTreeCount() - 1);
				prevTreeToolbarAction.setEnabled(treeViewer.getCurrentTreeIndex() > 0);
			}

			public void treeSettingsChanged() {
				// nothing to do
			}
		});

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

		statusBar = new StatusBar(" ");
		statusBar.setStatusProvider(treeViewer.getStatusProvider());

		JPanel topPanel = new JPanel(new BorderLayout(0,0));
		topPanel.add(toolBar, BorderLayout.NORTH);
		topPanel.add(statusBar, BorderLayout.CENTER);

		getContentPane().setLayout(new java.awt.BorderLayout(0, 0));
		getContentPane().add(topPanel, BorderLayout.NORTH);

		getContentPane().add(figTreePanel, BorderLayout.CENTER);

		treeViewer.addTreeSelectionListener(new TreeSelectionListener() {
			public void selectionChanged() {
				boolean hasSelection = treeViewer.hasSelection();
				cartoonAction.setEnabled(hasSelection);
				collapseAction.setEnabled(hasSelection);
				clearCollapsedAction.setEnabled(hasSelection);
				rerootAction.setEnabled(hasSelection);
				clearRootingAction.setEnabled(hasSelection);
				rotateAction.setEnabled(hasSelection);
				clearRotationsAction.setEnabled(hasSelection);
				annotateAction.setEnabled(hasSelection);
				clearAnnotationsAction.setEnabled(hasSelection);
				colourAction.setEnabled(hasSelection);
				clearColouringAction.setEnabled(hasSelection);
			}
		});

		getCutAction().setEnabled(false);
		getCopyAction().setEnabled(true);
		getDeleteAction().setEnabled(false);
		getSelectAllAction().setEnabled(true);
		getFindAction().setEnabled(true);

		getZoomWindowAction().setEnabled(false);

	}

	private void defineAnnotations() {

		List<AnnotationDefinition> definitions = treeViewer.getAnnotationDefinitions();

		if (annotationDefinitionsDialog == null) {
			annotationDefinitionsDialog = new AnnotationDefinitionsDialog(this);
		}
		annotationDefinitionsDialog.showDialog(definitions);
		treeViewer.fireAnnotationsChanged();
	}

	private void cartoonSelected(boolean selected) {
		if (selected) {
			collapseToolButton.setSelected(false);
			rerootToolButton.setSelected(false);
			rotateToolButton.setSelected(false);
			annotationToolButton.setSelected(false);
			colourToolButton.setSelected(false);
			treeViewer.setToolMode(TreePaneSelector.ToolMode.CARTOONING);
		} else {
			treeViewer.setToolMode(TreePaneSelector.ToolMode.SELECT);
			treeViewer.cartoonSelectedNodes();
		}
	}

	private void collapseSelected(boolean selected) {
		if (selected) {
			cartoonToolButton.setSelected(false);
			rerootToolButton.setSelected(false);
			rotateToolButton.setSelected(false);
			annotationToolButton.setSelected(false);
			colourToolButton.setSelected(false);
			treeViewer.setToolMode(TreePaneSelector.ToolMode.COLLAPSING);
		} else {
			treeViewer.setToolMode(TreePaneSelector.ToolMode.SELECT);
			treeViewer.collapseSelectedNodes();
		}
	}

	private void rerootTree(boolean selected) {
		if (selected) {
			cartoonToolButton.setSelected(false);
			collapseToolButton.setSelected(false);
			rotateToolButton.setSelected(false);
			annotationToolButton.setSelected(false);
			colourToolButton.setSelected(false);
			treeViewer.setToolMode(TreePaneSelector.ToolMode.ROOTING);
		} else {
			treeViewer.setToolMode(TreePaneSelector.ToolMode.SELECT);
			Set<Node> nodes = treeViewer.getSelectedNodes();

			if (nodes.size() != 1 ) {
				JOptionPane.showMessageDialog(this,
						"Exactly one branch must be selected to re-root the tree." ,
						"Annotating Tree",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			treeViewer.rerootOnSelectedBranch();
		}
	}

	private void rotateTree(boolean selected) {
		if (selected) {
			cartoonToolButton.setSelected(false);
			collapseToolButton.setSelected(false);
			rerootToolButton.setSelected(false);
			annotationToolButton.setSelected(false);
			colourToolButton.setSelected(false);
			treeViewer.setToolMode(TreePaneSelector.ToolMode.ROTATING);
		} else {
			treeViewer.setToolMode(TreePaneSelector.ToolMode.SELECT);
			treeViewer.rotateSelectedNode();
		}
	}

	private void annotateSelected(boolean selected) {
		if (selected) {
			cartoonToolButton.setSelected(false);
			collapseToolButton.setSelected(false);
			rerootToolButton.setSelected(false);
			rotateToolButton.setSelected(false);
			colourToolButton.setSelected(false);
		} else {
			treeViewer.setToolMode(TreePaneSelector.ToolMode.SELECT);

			List<AnnotationDefinition> definitions = treeViewer.getAnnotationDefinitions();
			if (definitions.size() == 0) {
				return;
			}

			if (annotationDialog == null) {
				annotationDialog = new AnnotationDialog(this);
			}

			Set<Node> nodes = treeViewer.getSelectedNodes();
			Set<Node> tips = treeViewer.getSelectedTips();

			Attributable item = null;
			if (nodes.size() + tips.size() == 1 ) {
				if (nodes.size() == 1) {
					item = nodes.iterator().next();
				}else if (tips.size() == 1) {
					item = tips.iterator().next();
				}
			} else {
				if (JOptionPane.showConfirmDialog(this,
						"More than one node selected for annotation. This operation\r" +
								"may overwrite existing annotations. Do you wish to continue?" ,
						"Annotating Tree",
						JOptionPane.WARNING_MESSAGE) == JOptionPane.CANCEL_OPTION) {
					return;
				}
			}
			if (annotationDialog.showDialog(definitions, item) != JOptionPane.CANCEL_OPTION) {
				String name = annotationDialog.getDefinition().getName();
				Object value = annotationDialog.getValue();
				treeViewer.annotateSelected(name, value);
				setDirty();
			}
		}
	}

	private static Color lastColor = Color.GRAY;

	private void colourSelected(boolean selected) {
		if (selected) {
			cartoonToolButton.setSelected(false);
			collapseToolButton.setSelected(false);
			rerootToolButton.setSelected(false);
			rotateToolButton.setSelected(false);
			annotationToolButton.setSelected(false);
		} else {
			treeViewer.setToolMode(TreePaneSelector.ToolMode.SELECT);

			Color color = JColorChooser.showDialog(this, "Select Colour", lastColor);
			if (color != null) {
				treeViewer.annotateSelected("!color", color);
				setDirty();
				lastColor = color;
			}
		}
	}

	public boolean readFromFile(File file) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		String line = bufferedReader.readLine();
		while (line != null && line.length() == 0) {
			line = bufferedReader.readLine();
		}

		boolean isNexus = (line != null && line.toUpperCase().contains("#NEXUS"));

		return readData(new FileReader(file), isNexus);

		// This is for running the version for readData that uses a ProgressMonitor....
//        ProgressMonitorInputStream in = new ProgressMonitorInputStream(
//                this,
//                "Reading " + file.getName(),
//                new FileInputStream(file));
//
//        Reader reader = new InputStreamReader(in);
//
//        readData(reader, isNexus);
//        return true;
	}

	public boolean readFromString(String string) throws IOException {
		boolean isNexus = string.substring(0, 80).toUpperCase().contains("#NEXUS");
		return readData(new StringReader(string), isNexus);
	}

	protected boolean readData(Reader reader, boolean isNexus) throws IOException {

		List<Tree> trees = new ArrayList<Tree>();

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

		if (!hasSettings) {
			// If there weren't settings in the file then this wasn't a TreeDraw
			// created document so we don't want to be able to overwrite it without
			// explicit action of the user...
			setDirty();
			clearFile();
		}

		return true;
	}


	/**
	 * This version loads the trees in a thread but this needs more thought in order
	 * to tie in to the JAM framework correctly
	 *
	 protected boolean readData(final Reader reader, final boolean isNexus) {

	 final JFrame frame = this;
	 Thread readThread = new Thread () {
	 public void run() {
	 try {

	 final List<Tree> trees = new ArrayList<Tree>();

	 boolean hasSettings = false;

	 final Map<String, Object> settings = new HashMap<String, Object>();
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

	 final boolean hasSettings2 = hasSettings;

	 EventQueue.invokeLater (
	 new Runnable () {
	 public void run () {
	 treeViewer.setTrees(trees);
	 controlPalette.setSettings(settings);

	 if (!hasSettings2) {
	 // If there weren't settings in the file then this wasn't a TreeDraw
	 // created document so we don't want to be able to overwrite it without
	 // explicit action of the user...
	 setDirty();
	 clearFile();
	 }
	 }
	 });

	 } catch (final ImportException ie) {
	 EventQueue.invokeLater (
	 new Runnable () {
	 public void run () {
	 JOptionPane.showMessageDialog(frame, "Error reading tree file: " + ie.getMessage(),
	 "Import Error",
	 JOptionPane.ERROR_MESSAGE);
	 }
	 });
	 } catch (final InterruptedIOException iioex) {
	 // The cancel dialog button was pressed - do nothing
	 } catch (final IOException ioex) {
	 EventQueue.invokeLater (
	 new Runnable () {
	 public void run () {
	 JOptionPane.showMessageDialog(frame, "File I/O Error: " + ioex.getMessage(),
	 "File I/O Error",
	 JOptionPane.ERROR_MESSAGE);
	 }
	 });
	 }

	 }
	 };
	 readThread.start();

	 return true;
	 }
	 */

	protected boolean writeToFile(File file) throws IOException {

		Map<String, Object> settings = new TreeMap<String, Object>();
		controlPalette.getSettings(settings);

		FileWriter writer = new FileWriter(file);
		FigTreeNexusExporter exporter = new FigTreeNexusExporter(writer);
		exporter.exportTrees(treeViewer.getTrees());
		exporter.writeFigTreeBlock(settings);

		writer.close();

		return true;
	}

	public final void doImport() {

		FileDialog dialog = new FileDialog(this,
				"Import Annotations File...",
				FileDialog.LOAD);

		dialog.setVisible(true);
		if (dialog.getFile() != null) {
			File file = new File(dialog.getDirectory(), dialog.getFile());

			try {
				Map<AnnotationDefinition, Map<Taxon, Object>> annotations = importAnnotationsFromFile(file);

				treeViewer.setTaxonAnnotations(annotations);
			} catch (FileNotFoundException fnfe) {
				JOptionPane.showMessageDialog(this, "Unable to open file: File not found",
						"Unable to open file",
						JOptionPane.ERROR_MESSAGE);
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(this, "Unable to read file: " + ioe,
						"Unable to read file",
						JOptionPane.ERROR_MESSAGE);
			}
		}

	}

	protected Map<AnnotationDefinition, Map<Taxon, Object>> importAnnotationsFromFile(File file) throws FileNotFoundException, IOException {

		BufferedReader reader = new BufferedReader(new FileReader(file));

		List<String> taxa = new ArrayList<String>();

		String line = reader.readLine();
		String[] labels = line.split("\t");
		Map<String, List<String>> columns = new HashMap<String, List<String>>();
		for (int i = 1; i < labels.length; i++) {
			columns.put(labels[i], new ArrayList<String>());
		}

		line = reader.readLine();
		while (line != null) {
			String[] values = line.split("\t");

			taxa.add(values[0]);
			for (int i = 1; i < values.length; i++) {
				if (i < labels.length) {
					List<String> column = columns.get(labels[i]);
					column.add(values[i]);
				}
			}
			line = reader.readLine();
		}

		Map<AnnotationDefinition, Map<Taxon, Object>> annotations = new HashMap<AnnotationDefinition, Map<Taxon, Object>>();

		NumberFormat nf = NumberFormat.getInstance();

		for (int i = 1; i < labels.length; i++) {
			List<String> column = columns.get(labels[i]);

			boolean isInteger = true;
			boolean isNumber = true;
			boolean isBoolean = true;

			for (String valueString : column) {
				if (!valueString.equalsIgnoreCase("TRUE") && !valueString.equalsIgnoreCase("FALSE")) {
					isBoolean = false;
					try {
						double number = Double.parseDouble(valueString);
						if (Math.round(number) != number) {
							isInteger = false;
						}
					} catch (NumberFormatException pe) {
						isInteger = false;
						isNumber = false;
					}
				}
			}

			Map<Taxon, Object> values = new HashMap<Taxon, Object>();
			AnnotationDefinition ad;
			int j = 0;
			for (String valueString : column) {
				Taxon taxon = Taxon.getTaxon(taxa.get(j));
				if (isBoolean) {
					values.put(taxon, new Boolean(valueString));
				} else if (isInteger) {
					values.put(taxon, new Integer(valueString));
				} else if (isNumber) {
					values.put(taxon, new Double(valueString));
				} else {
					values.put(taxon, valueString);
				}
				j++;
			}

			Set<Object> valueSet = new HashSet<Object>(values.values());

			if (isBoolean) {
				ad = new AnnotationDefinition(labels[i], AnnotationDefinition.Type.BOOLEAN );
			} else if (isInteger) {
				ad = new AnnotationDefinition(labels[i], AnnotationDefinition.Type.INTEGER );
			} else if (isNumber) {
				ad = new AnnotationDefinition(labels[i], AnnotationDefinition.Type.REAL );
			} else {
				String[] valueArray = new String[valueSet.size()];
				valueSet.toArray(valueArray);
				ad = new AnnotationDefinition(labels[i], AnnotationDefinition.Type.STRING);
				//ad.setOptions(valueArray);
			}

			annotations.put(ad, values);
		}

		return annotations;
	}


	private void doImportCharacters() {

		FileDialog dialog = new FileDialog(this,
				"Import Characters File...",
				FileDialog.LOAD);

		dialog.setVisible(true);
		if (dialog.getFile() != null) {
			File file = new File(dialog.getDirectory(), dialog.getFile());

			try {
				importCharactersFromFile(file);
			} catch (FileNotFoundException fnfe) {
				JOptionPane.showMessageDialog(this, "Unable to open file: File not found",
						"Unable to open file",
						JOptionPane.ERROR_MESSAGE);
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(this, "Unable to read file: " + ioe,
						"Unable to read file",
						JOptionPane.ERROR_MESSAGE);
			}
		}

	}

	protected boolean importCharactersFromFile(File file) throws FileNotFoundException, IOException {

		final String fileName = file.getName();
		SequenceImporter importer = new NexusImporter(new FileReader(file));
		try {
			Alignment alignment = new BasicAlignment(importer.importSequences());

			treeViewer.setCharacters(alignment);
		} catch (ImportException ie) {
			JOptionPane.showMessageDialog(this, "Error reading characters file: " + ie,
					"Import Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}

	public final void doExport() {

		FileDialog dialog = new FileDialog(this,
				"Export Tree File...",
				FileDialog.SAVE);

		dialog.setVisible(true);
		if (dialog.getFile() != null) {
			File file = new File(dialog.getDirectory(), dialog.getFile());

			FileWriter writer = null;
			try {
				writer = new FileWriter(file);
				writeTreeFile(writer, TreeFileFormat.NEWICK, false);
				writer.close();
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(this, "Error writing tree file: " + ioe,
						"Export Error",
						JOptionPane.ERROR_MESSAGE);
			}

		}

	}

	public final void doExportPDF() {
		FileDialog dialog = new FileDialog(this,
				"Export PDF Image...",
				FileDialog.SAVE);

		dialog.setVisible(true);
		if (dialog.getFile() != null) {
			File file = new File(dialog.getDirectory(), dialog.getFile());

			Rectangle2D bounds = treeViewer.getContentPane().getBounds();
			Document document = new Document(new com.lowagie.text.Rectangle((float)bounds.getWidth(), (float)bounds.getHeight()));
			try {
				// step 2
				PdfWriter writer;
				writer = PdfWriter.getInstance(document, new FileOutputStream(file));
				// step 3
				document.open();
				// step 4
				PdfContentByte cb = writer.getDirectContent();
				PdfTemplate tp = cb.createTemplate((float)bounds.getWidth(), (float)bounds.getHeight());
				Graphics2D g2d = tp.createGraphics((float)bounds.getWidth(), (float)bounds.getHeight(), new DefaultFontMapper());
				treeViewer.getContentPane().print(g2d);
				g2d.dispose();
				cb.addTemplate(tp, 0, 0);
			}
			catch(DocumentException de) {
				JOptionPane.showMessageDialog(this, "Error writing PDF file: " + de,
						"Export PDF Error",
						JOptionPane.ERROR_MESSAGE);
			}
			catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(this, "Error writing PDF file: " + e,
						"Export PDF Error",
						JOptionPane.ERROR_MESSAGE);
			}
			document.close();
		}
	}


	public void doCopy() {
		List<Tree> trees = treeViewer.getTrees();

//	    Map<String, Object> settings = new TreeMap<String, Object>();
//	    controlPalette.getSettings(settings);

		StringWriter writer = new StringWriter();
		try {
			writeTreeFile(writer, TreeFileFormat.NEWICK, false);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		StringSelection selection = new StringSelection(writer.toString());

		clipboard.setContents(selection, selection);
	}

	public void doPaste() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		Transferable transfer = clipboard.getContents(null);
		if (transfer.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try {
				String clip = (String)transfer.getTransferData(DataFlavor.stringFlavor);
				boolean isNexus = clip.substring(0, Math.min(80, clip.length())).toUpperCase().contains("#NEXUS");
				Reader reader = new StringReader(clip);

				List<Tree> trees = new ArrayList<Tree>();

				TreeImporter importer;
				if (isNexus) {
					importer = new FigTreeNexusImporter(reader);
				} else {
					importer = new NewickImporter(reader, true);
				}
				while (importer.hasTree()) {
					Tree tree = importer.importNextTree();
					trees.add(tree);
				}

				if (trees.size() == 0) {
					throw new ImportException("This clipboard contained no trees.");
				}
				treeViewer.addTrees(trees);

				// Show the first of the new trees
				treeViewer.showNextTree();
				setDirty();

			} catch (ImportException ie) {
				JOptionPane.showMessageDialog(this, "Error reading trees on clipboard: " + ie,
						"Import Error",
						JOptionPane.ERROR_MESSAGE);
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(this, "Error reading trees on clipboard: " + ioe,
						"Import Error",
						JOptionPane.ERROR_MESSAGE);
			} catch (UnsupportedFlavorException e) {
				JOptionPane.showMessageDialog(this, "Clipboard data is not compatible",
						"Import Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void doSelectAll() {
		treeViewer.selectAll();
	}

	enum TreeFileFormat {
		NEXUS,
		NEWICK
	};

	protected void writeTreeFile(Writer writer, TreeFileFormat format, boolean treeDrawBlock) throws IOException {

		Map<String, Object> settings = null;
		if (treeDrawBlock) {
			settings = new TreeMap<String, Object>();
			controlPalette.getSettings(settings);
		}

		switch (format) {
			case NEWICK:
				NewickExporter newickExporter = new NewickExporter(writer);
				newickExporter.exportTrees(treeViewer.getTrees());
				break;
			case NEXUS:
				FigTreeNexusExporter nexusExporter = new FigTreeNexusExporter(writer);
				nexusExporter.exportTrees(treeViewer.getTrees());
				if (settings != null) {
					nexusExporter.writeFigTreeBlock(settings);
				}
				break;
		}

		writer.close();
	}

	public final void doFind() {
		if (findDialog == null) {
			findDialog = new FindDialog(this);
		}

		List<AnnotationDefinition> definitions = treeViewer.getAnnotationDefinitions();
		if (findDialog.showDialog(definitions) != JOptionPane.CANCEL_OPTION) {
			String target = findDialog.getSearchTarget();
			DefaultTreeViewer.SearchType searchType = findDialog.getSearchType();
			String searchText = findDialog.getSearchText();
			boolean caseSensitive = findDialog.isCaseSensitive();
			if (target.equals(FindDialog.TAXON_LABEL)) {
				treeViewer.selectTaxa(searchType, searchText, caseSensitive);
			} else if (target.equals(FindDialog.ANY_ANNOTATION)) {
				treeViewer.selectNodes(null, searchType, searchText, caseSensitive);
			} else {
				treeViewer.selectNodes(target, searchType, searchText, caseSensitive);
			}
		}
	}

	public final void doFindAgain() {
	}

	public JComponent getExportableComponent() {
		return treeViewer.getContentPane();
	}

	public Action getNextTreeAction() {
		return nextTreeAction;
	}

	public Action getPreviousTreeAction() {
		return previousTreeAction;
	}

	public ToolbarAction getCartoonAction() {
		return cartoonAction;
	}

	public ToolbarAction getCollapseAction() {
		return collapseAction;
	}

	public AbstractAction getClearCollapsedAction() {
		return clearCollapsedAction;
	}

	public ToolbarAction getRerootAction() {
		return rerootAction;
	}

	public AbstractAction getClearRootingAction() {
		return clearRootingAction;
	}

	public ToolbarAction getRotateAction() {
		return rotateAction;
	}

	public AbstractAction getClearRotationsAction() {
		return clearRotationsAction;
	}

	public ToolbarAction getAnnotateAction() {
		return annotateAction;
	}

	public AbstractAction getClearAnnotationsAction() {
		return clearAnnotationsAction;
	}

	public AbstractAction getDefineAnnotationsAction() {
		return defineAnnotationsAction;
	}

	public ToolbarAction getColourAction() {
		return colourAction;
	}

	public AbstractAction getClearColouringAction() {
		return clearColouringAction;
	}

	public ToolbarAction getFindAction() {
		return findAction;
	}

	private AbstractAction importAction = new AbstractAction("Import Annotations...") {
		public void actionPerformed(ActionEvent ae) {
			doImport();
		}
	};

	private AbstractAction importCharactersAction = new AbstractAction("Import Characters...") {
		public void actionPerformed(ActionEvent ae) {
			doImportCharacters();
		}
	};

	private AbstractAction exportAction = new AbstractAction("Export PDF...") {
		public void actionPerformed(ActionEvent ae) {
			doExportPDF();
		}
	};

	private AbstractAction nextTreeAction =
			new AbstractAction(NEXT_TREE) {
				public void actionPerformed(ActionEvent e){
					treeViewer.showNextTree();
				}
			};

	private AbstractAction previousTreeAction =
			new AbstractAction(PREVIOUS_TREE) {
				public void actionPerformed(ActionEvent e){
					treeViewer.showPreviousTree();
				}
			};

	private ToolbarAction cartoonAction;
	private ToolbarAction collapseAction;
	private AbstractAction clearCollapsedAction = new AbstractAction(CLEAR_COLLAPSED) {
				public void actionPerformed(ActionEvent e){
					//treeViewer.clearCollapsedNodes();
				}
			};

	private ToolbarAction rerootAction;
	private AbstractAction clearRootingAction = new AbstractAction(CLEAR_ROOTING) {
				public void actionPerformed(ActionEvent e){
					//treeViewer.clearRooting();
				}
			};

	private ToolbarAction rotateAction;
	private AbstractAction clearRotationsAction = new AbstractAction(CLEAR_ROTATIONS) {
				public void actionPerformed(ActionEvent e){
					//treeViewer.clearRotations();
				}
			};

	private ToolbarAction annotateAction;
	private AbstractAction clearAnnotationsAction = new AbstractAction(CLEAR_ANNOTATIONS) {
		public void actionPerformed(ActionEvent ae) {
			// treeViewer.clearAnnotations();
		}
	};
	private AbstractAction defineAnnotationsAction = new AbstractAction(DEFINE_ANNOTATIONS) {
		public void actionPerformed(ActionEvent ae) {
			defineAnnotations();
		}
	};

	private ToolbarAction colourAction;
	private AbstractAction clearColouringAction = new AbstractAction(CLEAR_COLOURING) {
		public void actionPerformed(ActionEvent ae) {
			// treeViewer.clearColouring();
		}
	};

	private ToolbarAction findAction;

	private FindDialog findDialog = null;
	private AnnotationDefinitionsDialog annotationDefinitionsDialog = null;
	private AnnotationDialog annotationDialog = null;
}