package figtree.panel;

import jam.controlpalettes.ControlPalette;

import javax.swing.*;
import java.awt.*;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import figtree.treeviewer.painters.ScaleBarPainter;
import figtree.treeviewer.decorators.*;
import figtree.treeviewer.TreeViewer;
import figtree.treeviewer.TreePaneSelector;
import jebl.evolution.graphs.Node;
import jebl.evolution.trees.Tree;

/**
 * This is a panel that has a TreeViewer and a BasicControlPalette with
 * the default Controllers and Painters.
 *
 * @author Andrew Rambaut
 * @version $Id: FigTreeAppletPanel.java,v 1.4 2007/09/05 10:51:49 rambaut Exp $
 */
public class FigTreePanel extends JPanel {
    public enum Style {
        DEFAULT,
        SIMPLE,
        MINIMAL,
        ICARUS,
        ICARUS_SMALL
    }

    public FigTreePanel(Style style) {
        this(new SimpleTreeViewer(), new SimpleControlPalette(), null, style);
    }

    public FigTreePanel(final TreeViewer treeViewer,
                        ControlPalette controlPalette1,
                        ControlPalette controlPalette2,
                        Style style) {

        this.treeViewer = treeViewer;

        if (controlPalette1 != null) {
            controlPalette1.getPanel().setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
            controlPalette1.getPanel().setBackground(new Color(231, 237, 246));
            controlPalette1.getPanel().setOpaque(true);
        }

        if (controlPalette2 != null) {
            controlPalette2.getPanel().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
            controlPalette2.getPanel().setBackground(new Color(231, 237, 246));
            controlPalette2.getPanel().setOpaque(true);
        }

        // Create a tip label painter and its controller
        final SimpleLabelPainter tipLabelPainter = new SimpleLabelPainter(SimpleLabelPainter.PainterIntent.TIP);
        // Create a node label painter and its controller
        final SimpleLabelPainter nodeLabelPainter = new SimpleLabelPainter(SimpleLabelPainter.PainterIntent.NODE);
        nodeLabelPainter.setVisible(false);
        final SimpleLabelPainter branchLabelPainter = new SimpleLabelPainter(SimpleLabelPainter.PainterIntent.BRANCH);
        branchLabelPainter.setVisible(false);

        setLayout(new BorderLayout());

        switch (style) {

            case DEFAULT:
                if (controlPalette1 == null) {
                    throw new IllegalArgumentException("DEFAULT style requires a controlPalette");
                }
                controlPalette1.addController(new TreeViewerController(treeViewer));

                controlPalette1.addController(new TreeAppearanceController(
                        treeViewer,
                        "tipLabels", tipLabelPainter,
                        "nodeLabels", nodeLabelPainter,
                        "branchLabels", branchLabelPainter));

                controlPalette1.addController(new TreesController(treeViewer));

                controlPalette1.addController(new LabelPainterController(
                        "tipLabels", tipLabelPainter,
                        "nodeLabels", nodeLabelPainter,
                        "branchLabels", branchLabelPainter));
                break;
            case SIMPLE:
                if (controlPalette1 == null) {
                    throw new IllegalArgumentException("SIMPLE style requires a controlPalette");
                }
                controlPalette1.addController(new TreeViewerController(treeViewer));

                controlPalette1.addController(new TreeAppearanceController(
                        treeViewer,
                        "tipLabels", tipLabelPainter,
                        "nodeLabels", nodeLabelPainter,
                        "branchLabels", branchLabelPainter));

                controlPalette1.addController(new LabelPainterController(
                        "tipLabels", tipLabelPainter,
                        "nodeLabels", nodeLabelPainter,
                        "branchLabels", branchLabelPainter));
                break;
            case MINIMAL:
                break;
            case ICARUS:
                controlPalette1.addController(new TreeViewerController(treeViewer, true));

                controlPalette1.addController(new TreeAppearanceController(
                        treeViewer,
                        "tipLabels", tipLabelPainter,
                        "nodeLabels", nodeLabelPainter,
                        "branchLabels", branchLabelPainter,
                        true));

                controlPalette1.addController(new LabelPainterController(
                        "tipLabels", tipLabelPainter,
                        "nodeLabels", nodeLabelPainter,
                        "branchLabels", branchLabelPainter));

                controlPalette1.addController(new TreesController(treeViewer));

                controlPalette2.addController(new TreeColouringController(treeViewer, "Clustering:"));
                add(controlPalette2.getPanel(), BorderLayout.NORTH);
                break;
            case ICARUS_SMALL:
                controlPalette1.addController(new TreeViewerController(treeViewer, true));

                controlPalette1.addController(new TreeAppearanceController(
                        treeViewer,
                        "tipLabels", tipLabelPainter,
                        "nodeLabels", nodeLabelPainter,
                        "branchLabels", branchLabelPainter,
                        true));

                controlPalette2.addController(new TreeColouringController(treeViewer, "Clustering:"));
                add(controlPalette2.getPanel(), BorderLayout.NORTH);
                break;
        }

        treeViewer.setTipLabelPainter(tipLabelPainter);
        treeViewer.setNodeLabelPainter(nodeLabelPainter);
        treeViewer.setBranchLabelPainter(branchLabelPainter);

        AttributableDecorator tipDecorator = new AttributableDecorator();
        tipDecorator.setPaintAttributeName("!color");
        tipDecorator.setFontAttributeName("!font");
        tipLabelPainter.setTextDecorator(tipDecorator);

        // Create a scale bar painter and its controller
        final ScaleBarPainter scaleBarPainter = new ScaleBarPainter();
        // controlPalette.addController(new ScaleBarPainterController(scaleBarPainter));
        treeViewer.addScalePainter(scaleBarPainter);

        add(treeViewer, BorderLayout.CENTER);
        if (controlPalette1 != null) {
            add(controlPalette1.getPanel(), BorderLayout.SOUTH);
        }

    }

    public void setColourBy(String attribute) {
        if (attribute == null) {
            treeViewer.setBranchColouringDecorator(null, null);
            treeViewer.setBranchDecorator(null);
        } else {
            Set<Node> nodes = new HashSet<Node>();
            for (Tree tree : treeViewer.getTrees()) {
                for (Node node : tree.getNodes()) {
                    nodes.add(node);
                }
            }
            if (attribute != null && attribute.length() > 0) {
                if (attribute.endsWith("*")) {
                    Decorator decorator = new DiscreteColorDecorator();

                    treeViewer.setBranchColouringDecorator(attribute.substring(0, attribute.length() - 2), decorator);
                    treeViewer.setBranchDecorator(null);
                } else if (DiscreteColorDecorator.isDiscrete(attribute, nodes)) {
                    Decorator decorator = new DiscreteColorDecorator(attribute, nodes);

                    treeViewer.setBranchColouringDecorator(null, null);
                    treeViewer.setBranchDecorator(decorator);
                } else {

                    Decorator decorator = new ContinuousColorDecorator(
                            new ContinousScale(attribute, nodes),
                            new Color(192, 16, 0), new Color(0, 0, 0), new Color(0, 16, 192), false);

                    treeViewer.setBranchColouringDecorator(null, null);
                    treeViewer.setBranchDecorator(decorator);
                }
            }
        }
    }

    public TreeViewer getTreeViewer() {
        return treeViewer;
    }

    public void setTree(Tree tree) {
        java.util.List<Tree> trees = new ArrayList<Tree>();
        if (tree != null) {
            trees.add(tree);
        }
        treeViewer.setTrees(trees);
    }

    private final TreeViewer treeViewer;
}