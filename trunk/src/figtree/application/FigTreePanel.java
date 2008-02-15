package figtree.application;

import figtree.treeviewer.decorators.AttributableDecorator;
import org.virion.jam.controlpalettes.ControlPalette;
import org.virion.jam.disclosure.DisclosureListener;
import org.virion.jam.util.IconUtils;
import figtree.treeviewer.*;
import figtree.treeviewer.painters.*;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;

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

        // Create a node shape painter and its controller
        final NodeBarPainter nodeBarPainter = new NodeBarPainter();
        nodeBarPainter.setForeground(new Color(24, 32, 228, 128));
        nodeBarPainter.setVisible(false);
        controlPalette.addController(new NodeBarController("Node Bars", nodeBarPainter));
        treeViewer.setNodeBarPainter(nodeBarPainter);

        // Create a branch label painter and its controller
        final BasicLabelPainter branchLabelPainter = new BasicLabelPainter(BasicLabelPainter.PainterIntent.BRANCH);
        branchLabelPainter.setVisible(false);
        controlPalette.addController(new LabelPainterController("Branch Labels", "branchLabels", branchLabelPainter));
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

        topPanel = new JPanel(new BorderLayout()) {
            public void paint(Graphics graphics) {
                graphics.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
                super.paint(graphics);
            }
        };
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));

        centrePanel = new JPanel(new BorderLayout());
        centrePanel.add(topPanel, BorderLayout.NORTH);
        centrePanel.add(treeViewer, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(centrePanel, BorderLayout.CENTER);
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
        opening = true;
        target = utilityPanel.getPreferredSize().height;
        topPanel.add(utilityPanel, BorderLayout.CENTER);
        startAnimation();
    }

    public void hideUtilityPanel() {
//        if (utilityPanel == null) {
//            return;
//        }
//        centrePanel.add(null, BorderLayout.NORTH);
//        opening = false;
//        current = utilityPanel.getPreferredSize().height;
//        target = 0;
//        delta = current / 5;
//        startAnimation();
    }

    private void startAnimation() {
        timer = new Timer(animationSpeed, listener);
        timer.setRepeats(true);
        timer.setCoalesce(false);
        timer.start();
    }

    private void stopAnimation() {
        if (timer == null) return;
        timer.stop();
    }

    ActionListener listener = new ActionListener() {

        public void actionPerformed(ActionEvent e) {

            int delta = (target - topPanel.getHeight())/2;
            if (delta != 0) {
                Dimension size = topPanel.getSize();
                size.height += delta;
                topPanel.setPreferredSize(size);
//                topPanel.revalidate();
//                revalidate();
//                repaint();
                size = topPanel.getSize();
                topPanel.validate();
                validate();
                topPanel.paintImmediately(0, 0, size.width, size.height);
            } else {
                stopAnimation();
            }

        }
    };

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
    private final JPanel topPanel;
    private final JPanel centrePanel;

    private Timer timer = null;
    private boolean opening;
    private int animationSpeed = 1000;
    private int target;

    private static BufferedImage backgroundImage = null;

    static {
        try {
            backgroundImage = IconUtils.getBufferedImage(FigTreePanel.class, "images/utilityBackground.png");

        } catch (Exception e) {
            // no icons...
        }
    }
}
