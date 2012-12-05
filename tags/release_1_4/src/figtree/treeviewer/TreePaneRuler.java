package figtree.treeviewer;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

/**
 * @author Andrew Rambaut
 * @version $Id: TreePaneRuler.java 294 2006-04-14 10:28:11Z rambaut $
 */
public class TreePaneRuler implements MouseListener, MouseMotionListener {
    public TreePaneRuler(TreePane treePane) {
        this.treePane = treePane;
        treePane.addMouseListener(this);
        treePane.addMouseMotionListener(this);
    }

    public void mouseClicked(MouseEvent mouseEvent) {
//        double selectedHeight = treePane.getHeightAt((Graphics2D)treePane.getGraphics(), mouseEvent.getPoint());
//        if (!mouseEvent.isShiftDown()) {
//            treePane.clearSelection();
//        }
//
//        treePane.addSelectedHeight(isShiftDown);
    }

    public void mousePressed(MouseEvent mouseEvent) {
        // This is used for dragging in combination with mouseDragged
        // in the MouseMotionListener, below.
        dragPoint = new Point2D.Double(mouseEvent.getPoint().getX(), mouseEvent.getPoint().getY());
    }

    public void mouseReleased(MouseEvent mouseEvent) {
    }

    public void mouseEntered(MouseEvent mouseEvent) {
        treePane.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void mouseExited(MouseEvent mouseEvent) {
    }

    public void mouseMoved(MouseEvent mouseEvent) {
        double height = treePane.getHeightAt((Graphics2D)treePane.getGraphics(), mouseEvent.getPoint());
        treePane.setRuler(height);
    }

    public void mouseDragged(MouseEvent mouseEvent) {
    }

    private TreePane treePane;

    private Point2D dragPoint = null;
}
