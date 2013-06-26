/*
 * FigTreePanel.java
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

    public final static int CONTROL_PALETTE_WIDTH = 200;

    public FigTreePanel(JFrame frame, final ExtendedTreeViewer treeViewer, ControlPalette controlPalette) {

        this.treeViewer = treeViewer;
        this.controlPalette = controlPalette;

        // the scrollpane below now has this border
//        controlPalette.getPanel().setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        controlPalette.getPanel().setBackground(new Color(231, 237, 246));
        controlPalette.getPanel().setOpaque(true);

        treeViewerController = new TreeViewerController(treeViewer);
        controlPalette.addController(treeViewerController);

        controlPalette.addController(new MultipleTreesController(treeViewer));

        // This controller handles reading/writing of attribute colour schemes but doesn't presently
        // show any UI
        AttributeColourController attributeColourController = new AttributeColourController(treeViewer, frame);
        controlPalette.addController(attributeColourController);

        controlPalette.addController(new TreeAppearanceController(treeViewer, frame, attributeColourController));

        treesController = new TreesController(treeViewer);
        controlPalette.addController(treesController);

        controlPalette.addController(new TimeScaleController(treeViewer));

        // Create a tip label painter and its controller
        final BasicLabelPainter tipLabelPainter = new BasicLabelPainter(BasicLabelPainter.PainterIntent.TIP);
        controlPalette.addController(new LabelPainterController("Tip Labels", "tipLabels", tipLabelPainter, frame, attributeColourController, treeViewer));
        treeViewer.setTipLabelPainter(tipLabelPainter);

        // Create a node label painter and its controller
        final BasicLabelPainter nodeLabelPainter = new BasicLabelPainter(BasicLabelPainter.PainterIntent.NODE);
        nodeLabelPainter.setVisible(false);
        controlPalette.addController(new LabelPainterController("Node Labels", "nodeLabels", nodeLabelPainter, frame, attributeColourController, treeViewer));
        treeViewer.setNodeLabelPainter(nodeLabelPainter);

        // Create a node bar painter and its controller
        final NodeBarPainter nodeBarPainter = new NodeBarPainter();
        nodeBarPainter.setForeground(new Color(24, 32, 228, 128));
        nodeBarPainter.setVisible(false);
        controlPalette.addController(new NodeBarController("Node Bars", nodeBarPainter, treeViewer));
        treeViewer.setNodeBarPainter(nodeBarPainter);

        // Create a node shape painter and its controller
        final NodeShapePainter nodeShapePainter = new NodeShapePainter();
        nodeShapePainter.setVisible(false);
        controlPalette.addController(new NodeShapeController("Node Shapes", nodeShapePainter, attributeColourController, treeViewer));
        treeViewer.setNodeShapePainter(nodeShapePainter);

        // Create a branch label painter and its controller
        final BasicLabelPainter branchLabelPainter = new BasicLabelPainter(BasicLabelPainter.PainterIntent.BRANCH);
        branchLabelPainter.setVisible(false);
        controlPalette.addController(new LabelPainterController("Branch Labels", "branchLabels", branchLabelPainter, frame, attributeColourController, treeViewer));
        treeViewer.setBranchLabelPainter(branchLabelPainter);

        // Create a scale controller
        final ScaleBarPainter scaleBarPainter = new ScaleBarPainter();
        scaleBarPainter.setVisible(true);
        controlPalette.addController(new ScaleBarPainterController(scaleBarPainter, treeViewer));
        treeViewer.addScalePainter(scaleBarPainter);

        // Create a scale axis controller
        final ScaleAxisPainter scaleAxisPainter = new ScaleAxisPainter();
        final ScaleGridPainter scaleGridPainter = new ScaleGridPainter();
        scaleAxisPainter.setVisible(false);
        scaleGridPainter.setVisible(false);
        controlPalette.addController(new ScaleAxisPainterController(scaleAxisPainter, scaleGridPainter, treeViewer));
        treeViewer.addScalePainter(scaleAxisPainter);
        treeViewer.setScaleGridPainter(scaleGridPainter);

        // Create a legend controller
        final LegendPainter legendPainter = new LegendPainter(attributeColourController);
        legendPainter.setVisible(false);
        controlPalette.addController(new LegendPainterController(legendPainter, attributeColourController, treeViewer));
        treeViewer.setLegendPainter(legendPainter);

        slideOpenPanel = new SlideOpenPanel(treeViewer);

        setLayout(new BorderLayout());
        add(slideOpenPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        controlPalette.setPreferredWidth(CONTROL_PALETTE_WIDTH + scrollPane.getVerticalScrollBar().getWidth());

        scrollPane.setViewportView(controlPalette.getPanel());
        add(scrollPane, BorderLayout.WEST);

//        treeViewer.addAnnotationsListener(new AnnotationsListener() {
//            public void annotationsChanged() {
//
//                tipLabelPainter.setupAttributes(treeViewer.getTrees());
//                nodeLabelPainter.setupAttributes(treeViewer.getTrees());
//                nodeBarPainter.setupAttributes(treeViewer.getTrees());
//                branchLabelPainter.setupAttributes(treeViewer.getTrees());
//                legendPainter.setupAttributes(treeViewer.getTrees());
//            }
//        });
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
