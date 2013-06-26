/*
 * FigTreeFrame.java
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

package figtree.application;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.DefaultFontMapper;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import figtree.treeviewer.decorators.DiscreteColourDecorator;
import figtree.treeviewer.decorators.HSBDiscreteColourDecorator;
import figtree.treeviewer.painters.StatesPainter;
import jebl.evolution.alignments.Alignment;
import jebl.evolution.alignments.BasicAlignment;
import jebl.evolution.graphs.Node;
import jebl.evolution.io.*;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;
import jebl.util.Attributable;
import jam.controlpalettes.BasicControlPalette;
import jam.controlpalettes.ControlPalette;
import jam.framework.DocumentFrame;
import jam.panels.*;
import jam.toolbar.*;
import jam.util.IconUtils;
import figtree.application.menus.TreeMenuHandler;
import figtree.application.menus.FigTreeFileMenuHandler;
import figtree.treeviewer.*;
import figtree.treeviewer.TreeSelectionListener;
import figtree.treeviewer.annotations.*;
import org.freehep.util.export.ExportDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

public class FigTreeFrame extends DocumentFrame implements FigTreeFileMenuHandler, TreeMenuHandler {
    private final ExtendedTreeViewer treeViewer;
    private final ControlPalette controlPalette;
    private final FigTreePanel figTreePanel;

    private StatusBar statusBar;

    private SearchPanel filterPanel;
    private JPopupMenu filterPopup;

    public FigTreeFrame(String title) {
        super();

        setTitle(title);

        setImportAction(importAction);
        //       setImportAction(importCharactersAction);
        setExportAction(exportTreesAction);

        treeViewer = new ExtendedTreeViewer(this);
        controlPalette = new BasicControlPalette(200, BasicControlPalette.DisplayMode.ONLY_ONE_OPEN);

        figTreePanel = new FigTreePanel(this, treeViewer, controlPalette);

        this.addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                treeViewer.requestFocusInWindow();
            }
        });
    }

    public void initializeComponents() {

        setSize(new java.awt.Dimension(1024, 768));

        Toolbar toolBar = new Toolbar();
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.darkGray));

        toolBar.setRollover(true);
        toolBar.setFloatable(false);

        Icon rerootToolIcon = IconUtils.getIcon(this.getClass(), "images/rerootTool.png");
        Icon rotateToolIcon = IconUtils.getIcon(this.getClass(), "images/rotateTool.png");
        Icon cartoonNodeToolIcon = IconUtils.getIcon(this.getClass(), "images/cartoonNodeTool.png");
        Icon collapseNodeToolIcon = IconUtils.getIcon(this.getClass(), "images/collapseNodeTool.png");
        Icon hilightToolIcon = IconUtils.getIcon(this.getClass(), "images/hilightTool.png");
        Icon annotationToolIcon = IconUtils.getIcon(this.getClass(), "images/annotationTool.png");
        Icon findToolIcon = IconUtils.getIcon(this.getClass(), "images/findTool.png");
        Icon infoToolIcon = IconUtils.getIcon(this.getClass(), "images/infoTool.png");
        Icon statisticsToolIcon = IconUtils.getIcon(this.getClass(), "images/statisticsTool.png");
        Icon settingsToolIcon = IconUtils.getIcon(this.getClass(), "images/projectTool.png");
        Icon colourToolIcon = IconUtils.getIcon(this.getClass(), "images/coloursTool.png");

        Icon nextIcon = IconUtils.getIcon(this.getClass(), "images/next.png");
        Icon prevIcon = IconUtils.getIcon(this.getClass(), "images/prev.png");

        final ToolbarAction cartoonToolbarAction = new ToolbarAction("Cartoon", CARTOON_NODE, cartoonNodeToolIcon) {
            public void actionPerformed(ActionEvent e){
                cartoonAction.actionPerformed(e);
            }
        };

        ToolbarButton cartoonToolButton = new ToolbarButton(cartoonToolbarAction, true);
        cartoonToolButton.setFocusable(false);
        toolBar.addComponent(cartoonToolButton);

        final ToolbarAction collapseToolbarAction = new ToolbarAction("Collapse", COLLAPSE_NODE, collapseNodeToolIcon) {
            public void actionPerformed(ActionEvent e){
                collapseAction.actionPerformed(e);
            }
        };
        ToolbarButton collapseToolButton = new ToolbarButton(collapseToolbarAction, true);
        collapseToolButton.setFocusable(false);
        toolBar.addComponent(collapseToolButton);

        final ToolbarAction rerootToolbarAction = new ToolbarAction("Reroot", ROOT_ON_BRANCH, rerootToolIcon) {
            public void actionPerformed(ActionEvent e){
                rerootAction.actionPerformed(e);
            }
        };
        ToolbarButton rerootToolButton = new ToolbarButton(rerootToolbarAction, true);
        rerootToolButton.setFocusable(false);
        toolBar.addComponent(rerootToolButton);

        final ToolbarAction rotateToolbarAction = new ToolbarAction("Rotate", ROTATE_NODE, rotateToolIcon) {
            public void actionPerformed(ActionEvent e){
                rotateAction.actionPerformed(e);
            }
        };
        ToolbarButton rotateToolButton = new ToolbarButton(rotateToolbarAction, true);
        rotateToolButton.setFocusable(false);
        toolBar.addComponent(rotateToolButton);

        final ToolbarAction annotateToolbarAction =  new ToolbarAction("Annotate", ANNOTATE, annotationToolIcon) {
            public void actionPerformed(ActionEvent e){
                annotateAction.actionPerformed(e);
            }
        };
        ToolbarButton annotationToolButton = new ToolbarButton(annotateToolbarAction, true);
        annotationToolButton.setFocusable(false);
        toolBar.addComponent(annotationToolButton);

        final ToolbarAction colourToolbarAction = new ToolbarAction("Colour", COLOUR, colourToolIcon) {
            public void actionPerformed(ActionEvent e){
                colourAction.actionPerformed(e);
            }
        };
        ToolbarButton colourToolButton = new ToolbarButton(colourToolbarAction, true);
        colourToolButton.setFocusable(false);
        toolBar.addComponent(colourToolButton);

        final ToolbarAction hilightToolbarAction = new ToolbarAction("Hilight", HILIGHT, hilightToolIcon) {
            public void actionPerformed(ActionEvent e){
                hilightAction.actionPerformed(e);
            }
        };
        ToolbarButton hilightToolButton = new ToolbarButton(hilightToolbarAction, true);
        hilightToolButton.setFocusable(false);
        toolBar.addComponent(hilightToolButton);

        toolBar.addSeparator();

        final ToolbarAction findToolbarAction = new ToolbarAction("Find", "Find Taxa...", findToolIcon) {
            public void actionPerformed(ActionEvent e){
                findAction.actionPerformed(e);
            }
        };
        JButton findToolButton = new ToolbarButton(findToolbarAction);
        findToolButton.setFocusable(false);
        toolBar.addComponent(findToolButton);
        findToolButton.setEnabled(true);

//		final ToolbarAction infoToolbarAction = new ToolbarAction("Get Info", "Get Info...", infoToolIcon) {
//			public void actionPerformed(ActionEvent e){
//				getInfoAction.actionPerformed(e);
//			}
//		};
//		JButton infoToolButton = new ToolbarButton(infoToolbarAction);
//		toolBar.addComponent(infoToolButton);

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
        toggle1.setFocusable(false);
        toggle1.putClientProperty("JButton.buttonType", "segmentedTextured");
        toggle1.putClientProperty("JButton.segmentPosition", "first");
        toggle1.putClientProperty( "Quaqua.Button.style", "toggleWest");

        final JToggleButton toggle2 = new JToggleButton("Clade");
        toggle2.setFocusable(false);
        toggle2.putClientProperty("JButton.buttonType", "segmentedTextured");
        toggle2.putClientProperty("JButton.segmentPosition", "middle");
        toggle2.putClientProperty( "Quaqua.Button.style", "toggleCenter");

        final JToggleButton toggle3 = new JToggleButton("Taxa");
        toggle3.setFocusable(false);
        toggle3.putClientProperty("JButton.buttonType", "segmentedTextured");
        toggle3.putClientProperty("JButton.segmentPosition", "last");
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

        toolBar.addFlexibleSpace();

        final ToolbarAction prevTreeToolbarAction =
                new ToolbarAction(null, "Previous Tree...", prevIcon) {
                    public void actionPerformed(ActionEvent e){
                        if (treeViewer.isRootingOn() && treeViewer.getRootingType() == TreePane.RootingType.USER_ROOTING) {
                            JOptionPane.showMessageDialog(FigTreeFrame.this, "Cannot switch trees when user rooting option is on.\n" +
                                    "Turn this option off to switch trees",
                                    "Unable to switch trees",
                                    JOptionPane.ERROR_MESSAGE);

                        } else {
                            treeViewer.showPreviousTree();
                        }
                    }
                };
        JButton prevTreeToolButton = new ToolbarButton(prevTreeToolbarAction, true);
        prevTreeToolButton.setFocusable(false);
        prevTreeToolButton.putClientProperty("JButton.buttonType", "segmentedTextured");
        prevTreeToolButton.putClientProperty("JButton.segmentPosition", "first");
        prevTreeToolButton.putClientProperty( "Quaqua.Button.style", "toggleWest");

        final ToolbarAction nextTreeToolbarAction =
                new ToolbarAction(null, "Next Tree...", nextIcon) {
                    public void actionPerformed(ActionEvent e){
                        if (treeViewer.isRootingOn() && treeViewer.getRootingType() == TreePane.RootingType.USER_ROOTING) {
                            JOptionPane.showMessageDialog(FigTreeFrame.this, "Cannot switch trees when user rooting option is on.\n" +
                                    "Turn this option off to switch trees",
                                    "Unable to switch trees",
                                    JOptionPane.ERROR_MESSAGE);

                        } else {
                            treeViewer.showNextTree();
                        }
                    }
                };
        JButton nextTreeToolButton = new ToolbarButton(nextTreeToolbarAction, true);
        nextTreeToolButton.setFocusable(false);
        nextTreeToolButton.putClientProperty("JButton.buttonType", "segmentedTextured");
        nextTreeToolButton.putClientProperty("JButton.segmentPosition", "last");
        nextTreeToolButton.putClientProperty( "Quaqua.Button.style", "toggleEast");

        nextTreeToolbarAction.setEnabled(treeViewer.getCurrentTreeIndex() < treeViewer.getTreeCount() - 1);
        prevTreeToolbarAction.setEnabled(treeViewer.getCurrentTreeIndex() > 0);

        Box box2 = Box.createHorizontalBox();
        box2.add(Box.createVerticalStrut(annotationToolIcon.getIconHeight()));
        box2.add(prevTreeToolButton);
        box2.add(nextTreeToolButton);
        toolBar.addComponent(new GenericToolbarItem("Prev/Next", "Navigate through the trees", box2));

        TreeViewerListener l = new TreeViewerListener() {
            public void treeChanged() {
                boolean nextTreeEnabled = treeViewer.getCurrentTreeIndex() < treeViewer.getTreeCount() - 1;
                nextTreeAction.setEnabled(nextTreeEnabled);
                nextTreeToolbarAction.setEnabled(nextTreeEnabled);

                boolean prevTreeEnabled = treeViewer.getCurrentTreeIndex() > 0;
                previousTreeAction.setEnabled(prevTreeEnabled);
                prevTreeToolbarAction.setEnabled(prevTreeEnabled);
            }

            public void treeSettingsChanged() {
                // nothing to do
            }
        };
        treeViewer.addTreeViewerListener(l);
        l.treeChanged();

        toolBar.addFlexibleSpace();

        filterPopup = new JPopupMenu();

        final ButtonGroup bg = new ButtonGroup();
        boolean first = true;
        for (TreeViewer.TextSearchType searchType : TreeViewer.TextSearchType.values()) {
            final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(searchType.toString());
            if (first) {
                menuItem.setSelected(true);
                first = false;
            }
            filterPopup.add(menuItem);
            bg.add(menuItem);
        }
        filterPanel = new SearchPanel("Filter", filterPopup, true);
//        filterPanel.getSearchText().requestFocus();
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
                Enumeration e = bg.getElements();
                String value = null;
                while (e.hasMoreElements()) {
                    AbstractButton button = (AbstractButton)e.nextElement();
                    if (button.isSelected()) {
                        value = button.getText();
                    }
                }

                for (TreeViewer.TextSearchType searchType : TreeViewer.TextSearchType.values()) {
                    if (searchType.toString().equals(value)) {
                        treeViewer.selectTaxa("!name", searchType, searchString, false);
                    }
                }
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

        statusBar = new StatusBar("FigTree");
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 0, 0, 0, Color.darkGray),
                        BorderFactory.createMatteBorder(1, 0, 0, 0, Color.lightGray)),
                BorderFactory.createEmptyBorder(2, 12, 2, 12)));
        statusBar.setOpaque(false);
        statusBar.setStatusProvider(treeViewer.getStatusProvider());

        JPanel topPanel = new JPanel(new BorderLayout(0,0));
        topPanel.add(toolBar, BorderLayout.NORTH);

        getContentPane().setLayout(new java.awt.BorderLayout(0, 0));
        getContentPane().add(topPanel, BorderLayout.NORTH);

        getContentPane().add(figTreePanel, BorderLayout.CENTER);

        getContentPane().add(statusBar, BorderLayout.SOUTH);

        TreeSelectionListener l2 = new TreeSelectionListener() {
            public void selectionChanged() {
                boolean hasSelection = treeViewer.hasSelection();
                cartoonToolbarAction.setEnabled(hasSelection);
                cartoonAction.setEnabled(hasSelection);
                collapseToolbarAction.setEnabled(hasSelection);
                collapseAction.setEnabled(hasSelection);
                clearCollapsedAction.setEnabled(hasSelection);
                hilightToolbarAction.setEnabled(hasSelection);
                hilightAction.setEnabled(hasSelection);
                clearHilightingAction.setEnabled(hasSelection);
                rerootToolbarAction.setEnabled(hasSelection);
                rerootAction.setEnabled(hasSelection);
                clearRootingAction.setEnabled(hasSelection);
                rotateToolbarAction.setEnabled(hasSelection);
                rotateAction.setEnabled(hasSelection);
                clearRotationsAction.setEnabled(hasSelection);
                annotateToolbarAction.setEnabled(hasSelection);
                annotateAction.setEnabled(hasSelection);
                clearAnnotationsAction.setEnabled(hasSelection);
                colourToolbarAction.setEnabled(hasSelection);
                colourAction.setEnabled(hasSelection);
                clearColouringAction.setEnabled(hasSelection);
            }
        };
        treeViewer.addTreeSelectionListener(l2);
        l2.selectionChanged();

        getCutAction().setEnabled(false);
        getCopyAction().setEnabled(true);
        getDeleteAction().setEnabled(false);
        getSelectAllAction().setEnabled(true);
        getFindAction().setEnabled(true);

        getZoomWindowAction().setEnabled(false);
    }

    private void defineAnnotations() {

        Collection<AnnotationDefinition> definitions = treeViewer.getAnnotationDefinitions().values();

        if (annotationDefinitionsDialog == null) {
            annotationDefinitionsDialog = new AnnotationDefinitionsDialog(this);
        }
        annotationDefinitionsDialog.showDialog(definitions);

        treeViewer.setAnnotationDefinitions(annotationDefinitionsDialog.getAnnotations());

        treeViewer.fireAnnotationsChanged();
    }

    private void annotateNodesFromTips() {
        List<String> annotationNames = new ArrayList<String>();
        annotationNames.add("Colour");
        annotationNames.addAll(treeViewer.getAnnotationDefinitions().keySet());

        if (selectAnnotationDialog == null) {
            selectAnnotationDialog = new SelectAnnotationDialog(this);
        }

        int result = selectAnnotationDialog.showDialog(annotationNames);
        if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
            return;
        }

        String annotationName = selectAnnotationDialog.getAnnotationName();
        if (annotationName.equals("Colour")) {
            annotationName = "!color";
        }

        treeViewer.annotateNodesFromTips(annotationName);
        setDirty();
    }

    private void annotateTipsFromNodes() {
        List<String> annotationNames = new ArrayList<String>();
        annotationNames.add("Colour");
        annotationNames.addAll(treeViewer.getAnnotationDefinitions().keySet());

        if (selectAnnotationDialog == null) {
            selectAnnotationDialog = new SelectAnnotationDialog(this);
        }

        if (selectAnnotationDialog.showDialog(annotationNames) != JOptionPane.CANCEL_OPTION) {
            String annotationName = selectAnnotationDialog.getAnnotationName();
            if (annotationName.equals("Colour")) {
                annotationName = "!color";
            }

            treeViewer.annotateTipsFromNodes(annotationName);
            setDirty();
        }
    }

    private void cartoonSelected() {
        treeViewer.cartoonSelectedNodes();
    }

    private void collapseSelected() {
        treeViewer.collapseSelectedNodes();
    }

    private void hilightSelected() {
        Color color = JColorChooser.showDialog(this, "Select Colour", lastColor);
        if (color != null) {
            treeViewer.hilightSelectedNodes(color);
            setDirty();
            lastColor = color;
        }
    }

    private void rerootTree() {
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

    private void rotateTree() {
        treeViewer.rotateSelectedNode();
    }

    private void annotateSelected() {
        treeViewer.setToolMode(TreePaneSelector.ToolMode.SELECT);

        List<AnnotationDefinition> definitions = new ArrayList<AnnotationDefinition>();
        definitions.add(new AnnotationDefinition("Name", "!name", AnnotationDefinition.Type.STRING));
        definitions.addAll(treeViewer.getAnnotationDefinitions().values());

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
            int result = JOptionPane.showConfirmDialog(this,
                    "More than one node selected for annotation. This operation\n" +
                            "may overwrite existing annotations. Do you wish to continue?" ,
                    "Annotating Tree",
                    JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                return;
            }
        }
        if (annotationDialog.showDialog(definitions, item) != JOptionPane.CANCEL_OPTION) {
            AnnotationDefinition newDefinition = annotationDialog.getDefinition();

            List<AnnotationDefinition> defs = new ArrayList<AnnotationDefinition>(treeViewer.getAnnotationDefinitions().values());
            if (!defs.contains(newDefinition)) {
                defs.add(newDefinition);
                treeViewer.setAnnotationDefinitions(defs);
            }

            String code = newDefinition.getCode();
            Object value = annotationDialog.getValue();

            treeViewer.annotateSelected(code, value);
            setDirty();
        }
    }

    private void copySelectedAnnotations() {
        // todo This function is disabled as it is not completely implemented
//        treeViewer.setToolMode(TreePaneSelector.ToolMode.SELECT);
//
//        List<AnnotationDefinition> definitions = new ArrayList<AnnotationDefinition>();
//        definitions.add(new AnnotationDefinition("Name", "!name", AnnotationDefinition.Type.STRING));
//        definitions.addAll(treeViewer.getAnnotationDefinitions().values());
//
//        if (copyAnnotationDialog == null) {
//            copyAnnotationDialog = new AnnotationDialog(this, true);
//        }
//
//        Set<Node> nodes = treeViewer.getSelectedNodes();
//        Set<Node> tips = treeViewer.getSelectedTips();
//
//        Attributable item = null;
//        if (nodes.size() + tips.size() == 1 ) {
//            if (nodes.size() == 1) {
//                item = nodes.iterator().next();
//            }else if (tips.size() == 1) {
//                item = tips.iterator().next();
//            }
//        } else {
//            int result = JOptionPane.showConfirmDialog(this,
//                    "More than one node selected for annotation. This operation\n" +
//                            "may overwrite existing annotations. Do you wish to continue?" ,
//                    "Annotating Tree",
//                    JOptionPane.WARNING_MESSAGE);
//            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
//                return;
//            }
//        }
//        if (annotationDialog.showDialog(definitions, item) != JOptionPane.CANCEL_OPTION) {
//            String code = annotationDialog.getDefinition().getCode();
//            String code2 = annotationDialog.getDestinationDefinition().getCode();
//
//            treeViewer.copySelected(code, value);
//            setDirty();
//        }
    }

    private static Color lastColor = Color.GRAY;

    private void colourSelected() {
        treeViewer.setToolMode(TreePaneSelector.ToolMode.SELECT);

        Color color = JColorChooser.showDialog(this, "Select Colour", lastColor);
        if (color != null) {
            treeViewer.annotateSelected("!color", color);
            setDirty();
            lastColor = color;
        }
    }

    public boolean readFromFile(File file) throws IOException {
        Reader reader = null;
        try {
            reader = new FileReader(file);

            BufferedReader bufferedReader = new BufferedReader(reader);
            String line = bufferedReader.readLine();
            while (line != null && line.length() == 0) {
                line = bufferedReader.readLine();
            }

            boolean isNexus = (line != null && line.toUpperCase().contains("#NEXUS"));

            reader = new FileReader(file);

//			ProgressMonitorInputStream in = new ProgressMonitorInputStream(
//					this,
//					"Reading " + file.getName(),
//					new FileInputStream(file));
//			in.getProgressMonitor().setMillisToDecideToPopup(1000);
//			in.getProgressMonitor().setMillisToPopup(1000);
//
//	        reader = new InputStreamReader(in);

            boolean success = readData(reader, isNexus);

            reader.close();

            return success;

        } catch (IOException ioe) {
            if (reader != null) {
                reader.close();
            }
            throw ioe;
        }
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

            checkLabelAttribute(trees);

            treeViewer.setTrees(trees);
            controlPalette.setSettings(settings);
        } catch (ImportException ie) {
            JOptionPane.showMessageDialog(this, "Error reading tree file: \n" + ie.getMessage(),
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

    private void checkLabelAttribute(List<Tree> trees) {

        boolean hasLabel = false;

        for (Tree tree : trees) {
            for (Node node : tree.getNodes()) {
                if (node.getAttribute("label") != null) {
                    hasLabel = true;
                }
            }
        }

        if (hasLabel) {
            String labelName = null;

            do {
                labelName = JOptionPane.showInputDialog(
                        "The node/branches of the tree are labelled\n" +
                                "(i.e., with bootstrap values or posterior probabilities).\n\n" +
                                "Please select a name for these values.", "label");
                if (labelName == null) {
                    labelName = "label";
                }
                labelName = labelName.trim();

                if (labelName.length() == 0) {
                    Toolkit.getDefaultToolkit().beep();
                }
            } while (labelName == null || labelName.length() == 0);

            if (!labelName.equals("label")) {
                for (Tree tree : trees) {
                    for (Node node : tree.getNodes()) {
                        Object value = node.getAttribute("label");
                        if (value != null) {
                            node.removeAttribute("label");
                            node.setAttribute(labelName, value);
                        }
                    }
                }
            }
        }
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
        FigTreeNexusExporter exporter = new FigTreeNexusExporter(writer, true);
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

                // Hack to show tips states...
                String[] annotationNames = new String[annotations.keySet().size()];
                DiscreteColourDecorator[] decorators = new DiscreteColourDecorator[annotations.keySet().size()];

                int i = 0;
                for (AnnotationDefinition definition: annotations.keySet()) {
                    Map<Taxon, Object> annotation = annotations.get(definition);
                    annotationNames[i] = definition.getName();
                    decorators[i] = new HSBDiscreteColourDecorator(annotationNames[i], annotation.keySet());
                    i++;
                }
                treeViewer.setTipLabelPainter(new StatesPainter(annotationNames, decorators));


            } catch (FileNotFoundException fnfe) {
                JOptionPane.showMessageDialog(this, "Unable to open file: File not found",
                        "Unable to open file",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this, "Unable to read file: " + ioe.getMessage(),
                        "Unable to read file",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    protected Map<AnnotationDefinition, Map<Taxon, Object>> importAnnotationsFromFile(File file) throws IOException {

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

            if (values.length > 0) {
                taxa.add(values[0]);
                for (int i = 1; i < values.length; i++) {
                    if (i < labels.length) {
                        List<String> column = columns.get(labels[i]);
                        column.add(values[i]);
                    }
                }
            }
            line = reader.readLine();
        }

        Map<AnnotationDefinition, Map<Taxon, Object>> annotations = new TreeMap<AnnotationDefinition, Map<Taxon, Object>>();

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
                JOptionPane.showMessageDialog(this, "Unable to read file: " + ioe.getMessage(),
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
            JOptionPane.showMessageDialog(this, "Error reading characters file: " + ie.getMessage(),
                    "Import Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private void doImportColourScheme() {

        FileDialog dialog = new FileDialog(this,
                "Import Colour Scheme...",
                FileDialog.LOAD);

        dialog.setVisible(true);
        if (dialog.getFile() != null) {
            File file = new File(dialog.getDirectory(), dialog.getFile());

            try {
                importColourSchemeFromFile(file);
            } catch (FileNotFoundException fnfe) {
                JOptionPane.showMessageDialog(this, "Unable to open file: File not found",
                        "Unable to open file",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this, "Unable to read file: " + ioe.getMessage(),
                        "Unable to read file",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    protected boolean importColourSchemeFromFile(File file) throws FileNotFoundException, IOException {

        final String fileName = file.getName();
        SequenceImporter importer = new NexusImporter(new FileReader(file));
        try {
            Alignment alignment = new BasicAlignment(importer.importSequences());

            treeViewer.setCharacters(alignment);
        } catch (ImportException ie) {
            JOptionPane.showMessageDialog(this, "Error reading characters file: " + ie.getMessage(),
                    "Import Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    public final void doExport() {

        if (exportTreeDialog == null) {
            exportTreeDialog = new ExportTreeDialog(this);
        }

        if (exportTreeDialog.showDialog() == JOptionPane.OK_OPTION) {

            FileDialog dialog = new FileDialog(this,
                    "Export Tree File...",
                    FileDialog.SAVE);

            dialog.setVisible(true);
            if (dialog.getFile() != null) {
                File file = new File(dialog.getDirectory(), dialog.getFile());

                FileWriter writer = null;
                try {
                    writer = new FileWriter(file);
                    writeTreeFile(writer,
                            exportTreeDialog.getFormat(),
                            exportTreeDialog.allTrees(),
                            exportTreeDialog.asDisplayed(),
                            exportTreeDialog.includeFigTreeBlock(),
                            exportTreeDialog.includeAnnotations(),
                            false);
                    writer.close();
                } catch (IOException ioe) {
                    JOptionPane.showMessageDialog(this, "Error writing tree file: " + ioe.getMessage(),
                            "Export Error",
                            JOptionPane.ERROR_MESSAGE);
                }

            }
        }
    }

    public final void doExportGraphic() {
        ExportDialog export = new ExportDialog();
        export.showExportDialog(this, "Export view as ...", treeViewer.getContentPane(), "export");
    }


    public final void doExportPDF() {
        FileDialog dialog = new FileDialog(this,
                "Export PDF Image...",
                FileDialog.SAVE);

        dialog.setVisible(true);
        if (dialog.getFile() != null) {
            File file = new File(dialog.getDirectory(), dialog.getFile());

            Rectangle2D bounds = treeViewer.getContentPane().getBounds();
            Document document = new Document(new com.itextpdf
                    .text.Rectangle((float)bounds.getWidth(), (float)bounds.getHeight()));
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
        StringWriter writer = new StringWriter();
        try {
            writeTreeFile(writer, ExportTreeDialog.Format.NEXUS, true, false, false, true, true);
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
                JOptionPane.showMessageDialog(this, "Error reading trees on clipboard: " + ie.getMessage(),
                        "Import Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this, "Error reading trees on clipboard: " + ioe.getMessage(),
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

    protected void writeTreeFile(Writer writer, ExportTreeDialog.Format format,
                                 boolean writeAllTrees,
                                 boolean writeAsDisplayed,
                                 boolean writeFigTreeBlock,
                                 boolean writeAnnotations,
                                 boolean writeSelectedSubtree) throws IOException {

        Map<String, Object> settings = null;
        if (writeFigTreeBlock) {
            settings = new TreeMap<String, Object>();
            controlPalette.getSettings(settings);
        }

        List<Tree> trees = new ArrayList<Tree>();

        if (writeSelectedSubtree) {
            RootedTree tree = treeViewer.getSelectedSubtree();
            if (tree != null) {
                trees.add(tree);
            }
        } else {
            trees.addAll(treeViewer.getTreesAsViewed());
        }

        if (trees.size() > 0) {
            switch (format) {
                case NEWICK:
                    NewickExporter newickExporter = new NewickExporter(writer);
                    newickExporter.exportTrees(trees);
                    break;
                case NEXUS:
                    FigTreeNexusExporter nexusExporter = new FigTreeNexusExporter(writer, writeAnnotations);
                    nexusExporter.exportTrees(trees);
                    if (settings != null) {
                        nexusExporter.writeFigTreeBlock(settings);
                    }
                    break;
                case JSON:
                    JSONTreeExporter jsonExporter = new JSONTreeExporter(writer, writeAnnotations);
                    jsonExporter.exportTrees(trees);
                    break;
            }
        }

        writer.close();
    }

    public final void doFind() {
        if (findPanel == null) {
            findPanel = new FindPanel(findAllAction, null);
            findPanel.setOpaque(false);

            treeViewer.addAnnotationsListener(new AnnotationsListener() {
                public void annotationsChanged() {
                    List<AnnotationDefinition> definitions = new ArrayList<AnnotationDefinition>(
                            treeViewer.getAnnotationDefinitions().values());
                    findPanel.setup(definitions);
                }
            });

            List<AnnotationDefinition> definitions = new ArrayList<AnnotationDefinition>(
                    treeViewer.getAnnotationDefinitions().values());
            findPanel.setup(definitions);
        }

        if (figTreePanel.getUtilityPanel() != findPanel) {
            figTreePanel.showUtilityPanel(findPanel);
        } else {
            figTreePanel.hideUtilityPanel();
        }
    }

    public final void doFindAll() {

        FindPanel.Target target = findPanel.getSearchTarget();
        String targetString = findPanel.getSearchTargetString();

        if (findPanel.isNumericSearchType()) {
            TreeViewer.NumberSearchType searchType = findPanel.getNumberSearchType();
            Number searchValue = findPanel.getSearchValue();
            if (target == FindPanel.Target.TAXON_LABEL) {
                throw new IllegalArgumentException("Can't do numeric search on taxon labels");
            } else if (target == FindPanel.Target.BRANCH_LENGTH) {
                treeViewer.selectNodes("!length", searchType, searchValue);
            } else if (target == FindPanel.Target.NODE_AGE) {
                treeViewer.selectNodes("!height", searchType, searchValue);
            } else if (target == FindPanel.Target.ANY_ANNOTATION) {
                throw new IllegalArgumentException("Can't do numeric search on all annotations");
            } else {
                treeViewer.selectNodes(targetString, searchType, searchValue);
            }

        } else {
            TreeViewer.TextSearchType searchType = findPanel.getTextSearchType();
            String searchText = findPanel.getSearchText();
            boolean caseSensitive = findPanel.isCaseSensitive();

            if (target == FindPanel.Target.TAXON_LABEL) {
                treeViewer.selectTaxa("!name", searchType, searchText, caseSensitive);
            } else if (target == FindPanel.Target.BRANCH_LENGTH) {
                throw new IllegalArgumentException("Can't do text search on branch lengths");
            } else if (target == FindPanel.Target.NODE_AGE) {
                throw new IllegalArgumentException("Can't do text search on node ages");
            } else if (target == FindPanel.Target.ANY_ANNOTATION) {
                treeViewer.selectNodes(null, searchType, searchText, caseSensitive);
            } else {
                treeViewer.selectNodes(targetString, searchType, searchText, caseSensitive);
            }
        }
    }

    public final void doFindNext() {

    }

    public final void doGetInfo() {
//        List<AnnotationDefinition> definitions = treeViewer.getAnnotationDefinitions();
//        JPanel panel = new FindPanel(definitions);
//        panel.setOpaque(false);
//        figTreePanel.showUtilityPanel(panel);
    }

    public JComponent getExportableComponent() {
        return treeViewer.getContentPane();
    }

    @Override
    public Action getImportColourSchemeAction() {
        return importColourSchemeAction;
    }

    @Override
    public Action getExportColourSchemeAction() {
        return exportColourSchemeAction;
    }

    public Action getExportTreesAction() {
        return exportTreesAction;
    }

    public Action getExportGraphicAction() {
        return exportGraphicAction;
    }

    public Action getExportPDFAction() {
        return exportPDFAction;
    }

    public Action getNextTreeAction() {
        return nextTreeAction;
    }

    public Action getPreviousTreeAction() {
        return previousTreeAction;
    }

    public Action getCartoonAction() {
        return cartoonAction;
    }

    public Action getCollapseAction() {
        return collapseAction;
    }

    public Action getClearCollapsedAction() {
        return clearCollapsedAction;
    }

    public Action getMidpointRootAction() {
        return midpointRootAction;
    }

    public Action getRerootAction() {
        return rerootAction;
    }

    public Action getClearRootingAction() {
        return clearRootingAction;
    }

    public Action getIncreasingNodeOrderAction() {
        return increasingNodeOrderAction;
    }

    public Action getDecreasingNodeOrderAction() {
        return decreasingNodeOrderAction;
    }

    public Action getRotateAction() {
        return rotateAction;
    }

    public Action getClearRotationsAction() {
        return clearRotationsAction;
    }

    public Action getAnnotateAction() {
        return annotateAction;
    }

    public Action getCopyAnnotationsAction() {
        return copyAnnotationsAction;
    }

    public Action getAnnotateNodesFromTipsAction() {
        return annotateNodesFromTipsAction;
    }

    public Action getAnnotateTipsFromNodesAction() {
        return annotateTipsFromNodesAction;
    }

    public AbstractAction getClearAnnotationsAction() {
        return clearAnnotationsAction;
    }

    public AbstractAction getDefineAnnotationsAction() {
        return defineAnnotationsAction;
    }

    public Action getColourAction() {
        return colourAction;
    }

    public Action getClearColouringAction() {
        return clearColouringAction;
    }

    public Action getHilightAction() {
        return hilightAction;
    }

    public Action getClearHilightingAction() {
        return clearHilightingAction;
    }

    public Action getFindAction() {
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

    private AbstractAction importColourSchemeAction = new AbstractAction("Import Colour Scheme...") {
        public void actionPerformed(ActionEvent ae) {
            doImportColourScheme();
        }
    };

    private AbstractAction exportColourSchemeAction = new AbstractAction("Export Colour Scheme...") {
        public void actionPerformed(ActionEvent ae) {
//            doExportColourScheme();
        }
    };

    private AbstractAction exportTreesAction = new AbstractAction("Export Trees...") {
        public void actionPerformed(ActionEvent ae) {
            doExport();
        }
    };

    private AbstractAction exportGraphicAction = new AbstractAction("Export Graphic...") {
        public void actionPerformed(ActionEvent ae) {
            doExportGraphic();
        }
    };

    private AbstractAction exportPDFAction = new AbstractAction("Export PDF...") {
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

    private AbstractAction cartoonAction = new AbstractAction(CARTOON_NODE) {
        public void actionPerformed(ActionEvent e){
            cartoonSelected();
        }
    };
    private AbstractAction collapseAction = new AbstractAction(COLLAPSE_NODE) {
        public void actionPerformed(ActionEvent e){
            collapseSelected();
        }
    };
    private AbstractAction clearCollapsedAction = new AbstractAction(CLEAR_COLLAPSED) {
        public void actionPerformed(ActionEvent e){
            treeViewer.clearCollapsedNodes();
        }
    };

    private AbstractAction rerootAction = new AbstractAction(ROOT_ON_BRANCH) {
        public void actionPerformed(ActionEvent e){
            rerootTree();
        }
    };
    private AbstractAction midpointRootAction = new AbstractAction(MIDPOINT_ROOT) {
        public void actionPerformed(ActionEvent e){
            figTreePanel.toggleMidpointRoot();
        }
    };
    private AbstractAction clearRootingAction = new AbstractAction(CLEAR_ROOTING) {
        public void actionPerformed(ActionEvent e){
            treeViewer.clearRooting();
        }
    };


    private AbstractAction rotateAction = new AbstractAction(ROTATE_NODE) {
        public void actionPerformed(ActionEvent e){
            rotateTree();
        }
    };
    private AbstractAction clearRotationsAction = new AbstractAction(CLEAR_ROTATIONS) {
        public void actionPerformed(ActionEvent e){
            treeViewer.clearRotations();
        }
    };
    private AbstractAction increasingNodeOrderAction = new AbstractAction(INCREASING_NODE_ORDER) {
        public void actionPerformed(ActionEvent e){
            figTreePanel.toggleIncreasingNodeOrder();
        }
    };
    private AbstractAction decreasingNodeOrderAction = new AbstractAction(DECREASING_NODE_ORDER) {
        public void actionPerformed(ActionEvent e){
            figTreePanel.toggleDecreasingNodeOrder();
        }
    };

    private AbstractAction annotateAction = new AbstractAction(ANNOTATE) {
        public void actionPerformed(ActionEvent ae) {
            annotateSelected();
        }
    };

    private AbstractAction copyAnnotationsAction = new AbstractAction(COPY_ANNOTATION_VALUES) {
        public void actionPerformed(ActionEvent ae) {
            copySelectedAnnotations();
        }
    };

    private AbstractAction annotateNodesFromTipsAction = new AbstractAction(ANNOTATE_NODES_FROM_TIPS) {
        public void actionPerformed(ActionEvent ae) {
            annotateNodesFromTips();
        }
    };

    private AbstractAction annotateTipsFromNodesAction = new AbstractAction(ANNOTATE_TIPS_FROM_NODES) {
        public void actionPerformed(ActionEvent ae) {
            annotateTipsFromNodes();
        }
    };

    private AbstractAction clearAnnotationsAction = new AbstractAction(CLEAR_ANNOTATIONS) {
        public void actionPerformed(ActionEvent ae) {
            // treeViewer.clearAnnotation();
        }
    };

    private AbstractAction defineAnnotationsAction = new AbstractAction(DEFINE_ANNOTATIONS) {
        public void actionPerformed(ActionEvent ae) {
            defineAnnotations();
        }
    };

    private AbstractAction colourAction = new AbstractAction(COLOUR) {
        public void actionPerformed(ActionEvent ae) {
            colourSelected();
        }
    };

    private AbstractAction clearColouringAction = new AbstractAction(CLEAR_COLOURING) {
        public void actionPerformed(ActionEvent ae) {
            treeViewer.clearColouring();
        }
    };

    private AbstractAction hilightAction = new AbstractAction(HILIGHT) {
        public void actionPerformed(ActionEvent ae) {
            hilightSelected();
        }
    };

    private AbstractAction clearHilightingAction = new AbstractAction(CLEAR_HILIGHTING) {
        public void actionPerformed(ActionEvent ae) {
            treeViewer.clearHilighting();
        }
    };

    private AbstractAction findAction = new AbstractAction("Find...") {
        public void actionPerformed(ActionEvent ae) {
            doFind();
        }
    };

    private AbstractAction findAllAction = new AbstractAction("Find") {
        public void actionPerformed(ActionEvent ae) {
            doFindAll();
        }
    };

//	private AbstractAction findNextAction = new AbstractAction("Find Next") {
//		public void actionPerformed(ActionEvent ae) {
//			doFindNext();
//		}
//	};

    private AbstractAction getInfoAction = new AbstractAction("Get Info...") {
        public void actionPerformed(ActionEvent ae) {
            doGetInfo();
        }
    };

    private ExportTreeDialog exportTreeDialog = null;
    private FindPanel findPanel = null;
    private AnnotationDefinitionsDialog annotationDefinitionsDialog = null;
    private AnnotationDialog annotationDialog = null;
    private AnnotationDialog copyAnnotationDialog = null;
    private SelectAnnotationDialog selectAnnotationDialog = null;
}