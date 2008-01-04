package figtree.application;

import figtree.treeviewer.decorators.AttributableDecorator;
import org.virion.jam.controlpalettes.ControlPalette;
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

	public FigTreePanel(final ExtendedTreeViewer treeViewer, ControlPalette controlPalette) {

		this.treeViewer = treeViewer;
		this.controlPalette = controlPalette;

		controlPalette.getPanel().setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        controlPalette.getPanel().setBackground(new Color(231, 237, 246));
        controlPalette.getPanel().setOpaque(true);

		TreeViewerController treeViewerController = new TreeViewerController(treeViewer);
        controlPalette.addController(treeViewerController);

		controlPalette.addController(new MultipleTreesController(treeViewer));

		controlPalette.addController(new TreeAppearanceController(treeViewer));

		controlPalette.addController(new TreesController(treeViewer));

		controlPalette.addController(new TimeScaleController(treeViewer));

		// Create a tip label painter and its controller
		final BasicLabelPainter tipLabelPainter = new BasicLabelPainter(BasicLabelPainter.PainterIntent.TIP);
		controlPalette.addController(new TreeDrawLabelPainterController("Tip Labels", "tipLabels", tipLabelPainter));
		treeViewer.setTipLabelPainter(tipLabelPainter);

        AttributableDecorator tipDecorator = new AttributableDecorator();
        tipDecorator.setPaintAttributeName("!color");
        tipDecorator.setFontAttributeName("!font");
        tipLabelPainter.setTextDecorator(tipDecorator);

		// Create a node label painter and its controller
		final BasicLabelPainter nodeLabelPainter = new BasicLabelPainter(BasicLabelPainter.PainterIntent.NODE);
		nodeLabelPainter.setVisible(false);
		controlPalette.addController(new TreeDrawLabelPainterController("Node Labels", "nodeLabels", nodeLabelPainter));
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
		controlPalette.addController(new TreeDrawLabelPainterController("Branch Labels", "branchLabels", branchLabelPainter));
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

		setLayout(new BorderLayout());

		add(treeViewer, BorderLayout.CENTER);
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

	private final TreeViewer treeViewer;
	private final ControlPalette controlPalette;

}
