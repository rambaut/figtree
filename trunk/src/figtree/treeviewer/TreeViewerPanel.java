package figtree.treeviewer;

import jebl.evolution.io.TreeImporter;
import jebl.evolution.io.NexusImporter;
import jebl.evolution.trees.Tree;
import jam.controlpalettes.BasicControlPalette;
import jam.controlpalettes.ControlPalette;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.BufferedReader;

import figtree.treeviewer.painters.LabelPainterController;
import figtree.treeviewer.painters.NodeBarController;
import figtree.treeviewer.painters.NodeBarPainter;
import figtree.treeviewer.painters.BasicLabelPainter;

/**
 * This is a panel that has a TreeViewer and a BasicControlPalette with
 * the default Controllers and Painters.
 *
 * @author Andrew Rambaut
 * @version $Id: TreeViewerPanel.java 760 2007-08-21 00:05:45Z rambaut $
 */
public class TreeViewerPanel extends JPanel {

    public TreeViewerPanel(JFrame frame, TreeViewer treeViewer, ControlPalette controlPalette) {

        this.treeViewer = treeViewer;
        this.controlPalette = controlPalette;

        controlPalette.getPanel().setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        controlPalette.getPanel().setBackground(new Color(231, 237, 246));
        controlPalette.getPanel().setOpaque(true);

        controlPalette.addController(new TreeViewerController(treeViewer));

        // This controller handles reading/writing of attribute colour schemes but doesn't presently
        // show any UI
        AttributeColourController attributeColourController = new AttributeColourController(treeViewer, frame);
        controlPalette.addController(attributeColourController);

        controlPalette.addController(new TreeAppearanceController(treeViewer, frame, attributeColourController));

        controlPalette.addController(new TreesController(treeViewer));

        // Create a tip label painter and its controller
        BasicLabelPainter tipLabelPainter = new BasicLabelPainter(BasicLabelPainter.PainterIntent.TIP);
        controlPalette.addController(new LabelPainterController("Tip Labels", "tipLabels", tipLabelPainter, frame, attributeColourController, treeViewer));
        treeViewer.setTipLabelPainter(tipLabelPainter);

        // Create a node label painter and its controller
        BasicLabelPainter nodeLabelPainter = new BasicLabelPainter(BasicLabelPainter.PainterIntent.NODE);
        nodeLabelPainter.setVisible(false);
        controlPalette.addController(new LabelPainterController("Node Labels", "nodeLabels", nodeLabelPainter, frame, attributeColourController, treeViewer));
        treeViewer.setNodeLabelPainter(nodeLabelPainter);

        // Create a node shape painter and its controller
        NodeBarPainter nodeBarPainter = new NodeBarPainter();
        nodeBarPainter.setForeground(new Color(24, 32, 228, 128));
        nodeBarPainter.setVisible(false);
        controlPalette.addController(new NodeBarController("Node Bars", nodeBarPainter, treeViewer));
        treeViewer.setNodeBarPainter(nodeBarPainter);

        // Create a branch label painter and its controller
        BasicLabelPainter branchLabelPainter = new BasicLabelPainter(BasicLabelPainter.PainterIntent.BRANCH);
        branchLabelPainter.setVisible(false);
        controlPalette.addController(new LabelPainterController("Branch Labels", "branchLabels", branchLabelPainter, frame, attributeColourController, treeViewer));
        treeViewer.setBranchLabelPainter(branchLabelPainter);

        // Create a scale bar painter and its controller
        controlPalette.addController(new TimeScaleController(treeViewer));

        setLayout(new BorderLayout());

        add(treeViewer, BorderLayout.CENTER);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(controlPalette.getPanel(), BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(leftPanel);
        add(scrollPane, BorderLayout.WEST);

    }

    public TreeViewer getTreeViewer() {
        return treeViewer;
    }

    public ControlPalette getControlPalette() {
        return controlPalette;
    }

    private final TreeViewer treeViewer;
    private final ControlPalette controlPalette;

    static public void main(String[] args) {

        JFrame frame = new JFrame("TreeViewer Test");

        TreeViewer treeViewer = new DefaultTreeViewer(frame);
        ControlPalette controlPalette = new BasicControlPalette(200, BasicControlPalette.DisplayMode.ONLY_ONE_OPEN);

        frame.getContentPane().add(new TreeViewerPanel(frame, treeViewer, controlPalette), BorderLayout.CENTER);

        try {
            File inputFile = null;

            if (args.length > 0) {
                inputFile = new File(args[0]);
            }

            if (inputFile == null) {
                // No input file name was given so throw up a dialog box...
                java.awt.FileDialog chooser = new java.awt.FileDialog(frame, "Select NEXUS Tree File",
                        java.awt.FileDialog.LOAD);
                chooser.setVisible(true);
                inputFile = new java.io.File(chooser.getDirectory(), chooser.getFile());
                chooser.dispose();
            }

            if (inputFile == null) {
                throw new RuntimeException("No file specified");
            }

//        TreeImporter importer = new NewickImporter(new FileReader(inputFile));
            Reader reader = new BufferedReader(new FileReader(inputFile));
            TreeImporter importer = new NexusImporter(reader);
            java.util.List<Tree> trees = importer.importTrees();
            reader.close();
            treeViewer.setTrees(trees);
        } catch (Exception ie) {
            ie.printStackTrace();
            System.exit(1);
        }

        frame.setSize(640, 480);
        frame.setVisible(true);
    }
}
