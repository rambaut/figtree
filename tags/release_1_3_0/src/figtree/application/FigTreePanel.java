package figtree.application;

import figtree.treeviewer.decorators.AttributableDecorator;
import jam.controlpalettes.ControlPalette;
import jam.disclosure.SlideOpenPanel;
import figtree.treeviewer.*;
import figtree.treeviewer.painters.*;

import javax.swing.*;
import java.awt.*;

/**
 * This is a panel that has a TreeViewer and a BasicControlPalette with
 * the default Controllers and Painters.
 *
 * @author Andrew Rambaut
 * @version $Id: FigTreePanel.java,v 1.13 2007/09/05 10:51:49 rambaut Exp $
 */
public class FigTreePanel extends JPanel {

    public FigTreePanel(JFrame frame, final ExtendedTreeViewer treeViewer, ControlPalette controlPalette) {

        this.treeViewer = treeViewer;
        this.controlPalette = controlPalette;

        controlPalette.getPanel().setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        controlPalette.getPanel().setBackground(new Color(231, 237, 246));
        controlPalette.getPanel().setOpaque(true);

        treeViewerController = new TreeViewerController(treeViewer);
        controlPalette.addController(treeViewerController);

        controlPalette.addController(new MultipleTreesController(treeViewer));

        controlPalette.addController(new TreeAppearanceController(treeViewer, frame));

        treesController = new TreesController(treeViewer);
        controlPalette.addController(treesController);

        controlPalette.addController(new TimeScaleController(treeViewer));

        // Create a tip label painter and its controller
        final BasicLabelPainter tipLabelPainter = new BasicLabelPainter(BasicLabelPainter.PainterIntent.TIP);
        controlPalette.addController(new LabelPainterController("Tip Labels", "tipLabels", tipLabelPainter, frame));
        treeViewer.setTipLabelPainter(tipLabelPainter);

        // Create a node label painter and its controller
        final BasicLabelPainter nodeLabelPainter = new BasicLabelPainter(BasicLabelPainter.PainterIntent.NODE);
        nodeLabelPainter.setVisible(false);
        controlPalette.addController(new LabelPainterController("Node Labels", "nodeLabels", nodeLabelPainter, frame));
        treeViewer.setNodeLabelPainter(nodeLabelPainter);

        // Create a node shape painter and its controller
        final NodeBarPainter nodeBarPainter = new NodeBarPainter();
        nodeBarPainter.setForeground(new Color(24, 32, 228, 128));
        nodeBarPainter.setVisible(false);
        controlPalette.addController(new NodeBarController("Node Bars", nodeBarPainter));
        treeViewer.setNodeBarPainter(nodeBarPainter);

        // Create a branch label painter and its controller
        final BasicLabelPainter branchLabelPainter = new BasicLabelPainter(BasicLabelPainter.PainterIntent.BRANCH);
        branchLabelPainter.setVisible(false);
        controlPalette.addController(new LabelPainterController("Branch Labels", "branchLabels", branchLabelPainter, frame));
        treeViewer.setBranchLabelPainter(branchLabelPainter);

        // Create a scale controller
        final ScaleBarPainter scaleBarPainter = new ScaleBarPainter();
        scaleBarPainter.setVisible(true);
        controlPalette.addController(new ScaleBarPainterController(scaleBarPainter));
        treeViewer.addScalePainter(scaleBarPainter);

        // Create a scale axis controller
        final ScaleAxisPainter scaleAxisPainter = new ScaleAxisPainter();
        final ScaleGridPainter scaleGridPainter = new ScaleGridPainter();
        scaleAxisPainter.setVisible(false);
        scaleGridPainter.setVisible(false);
        controlPalette.addController(new ScaleAxisPainterController(scaleAxisPainter, scaleGridPainter));
        treeViewer.addScalePainter(scaleAxisPainter);
        treeViewer.setScaleGridPainter(scaleGridPainter);

        slideOpenPanel = new SlideOpenPanel(treeViewer);

        setLayout(new BorderLayout());
        add(slideOpenPanel, BorderLayout.CENTER);
        add(controlPalette.getPanel(), BorderLayout.WEST);

        treeViewer.addAnnotationsListener(new AnnotationsListener() {
            public void annotationsChanged() {

                tipLabelPainter.setupAttributes(treeViewer.getTrees());
                nodeLabelPainter.setupAttributes(treeViewer.getTrees());
                nodeBarPainter.setupAttributes(treeViewer.getTrees());
                branchLabelPainter.setupAttributes(treeViewer.getTrees());
            }
        });
    }

    public TreeViewer getTreeViewer() {
        return treeViewer;
    }

    public ControlPalette getControlPalette() {
        return controlPalette;
    }

    public void showUtilityPanel(JPanel utilityPanel) {
        if (utilityPanel == null) {
            return;
        }
		slideOpenPanel.showUtilityPanel(utilityPanel);
    }

	public void hideUtilityPanel() {
		slideOpenPanel.hideUtilityPanel();
	}


	public JPanel getUtilityPanel() {
		return slideOpenPanel.getUtilityPanel();
	}

    public void toggleMidpointRoot() {
        treesController.toggleMidpointRoot();
    }

    public void toggleIncreasingNodeOrder() {
        treesController.toggleIncreasingNodeOrder();
    }

    public void toggleDecreasingNodeOrder() {
        treesController.toggleDecreasingNodeOrder();
    }

    private final TreeViewer treeViewer;
    private final TreeViewerController treeViewerController;
    private final TreesController treesController;
    private final ControlPalette controlPalette;
    private final SlideOpenPanel slideOpenPanel;
}
