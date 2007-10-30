package figtree.applet;

import figtree.treeviewer.ExtendedTreeViewer;
import org.virion.jam.controlpalettes.ControlPalette;

import javax.swing.*;
import java.awt.*;

import jebl.gui.trees.treeviewer_dev.painters.BasicLabelPainter;
import jebl.gui.trees.treeviewer_dev.painters.ScaleBarPainter;
import jebl.gui.trees.treeviewer_dev.decorators.AttributableDecorator;

/**
 * This is a panel that has a TreeViewer and a BasicControlPalette with
 * the default Controllers and Painters.
 *
 * @author Andrew Rambaut
 * @version $Id: FigTreeAppletPanel.java,v 1.4 2007/09/05 10:51:49 rambaut Exp $
 */
public class FigTreeAppletPanel extends JPanel {

	public FigTreeAppletPanel(final ExtendedTreeViewer treeViewer, ControlPalette controlPalette) {

		controlPalette.getPanel().setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        controlPalette.getPanel().setBackground(new Color(231, 237, 246));
        controlPalette.getPanel().setOpaque(true);

		TreeViewerController treeViewerController = new TreeViewerController(treeViewer);
        controlPalette.addController(treeViewerController);

		controlPalette.addController(new TreeAppearanceController(treeViewer));

		controlPalette.addController(new TreesController(treeViewer));

		// Create a tip label painter and its controller
		final BasicLabelPainter tipLabelPainter = new BasicLabelPainter(BasicLabelPainter.PainterIntent.TIP);
		controlPalette.addController(new LabelPainterController("Tip Labels", "tipLabels", tipLabelPainter));
		treeViewer.setTipLabelPainter(tipLabelPainter);

        AttributableDecorator tipDecorator = new AttributableDecorator();
        tipDecorator.setPaintAttributeName("!color");
        tipDecorator.setFontAttributeName("!font");
        tipLabelPainter.setTextDecorator(tipDecorator);

		// Create a node label painter and its controller
		final BasicLabelPainter nodeLabelPainter = new BasicLabelPainter(BasicLabelPainter.PainterIntent.NODE);
		nodeLabelPainter.setVisible(false);
		controlPalette.addController(new LabelPainterController("Node Labels", "nodeLabels", nodeLabelPainter));
		treeViewer.setNodeLabelPainter(nodeLabelPainter);

		// Create a branch label painter and its controller
		final BasicLabelPainter branchLabelPainter = new BasicLabelPainter(BasicLabelPainter.PainterIntent.BRANCH);
		branchLabelPainter.setVisible(false);
		controlPalette.addController(new LabelPainterController("Branch Labels", "branchLabels", branchLabelPainter));
		treeViewer.setBranchLabelPainter(branchLabelPainter);

		// Create a scale bar painter and its controller
		final ScaleBarPainter scaleBarPainter = new ScaleBarPainter();
		controlPalette.addController(new ScaleBarPainterController(scaleBarPainter));
		treeViewer.addScalePainter(scaleBarPainter);

		setLayout(new BorderLayout());

		add(treeViewer, BorderLayout.CENTER);
		add(controlPalette.getPanel(), BorderLayout.WEST);

    }

}