/*
 * TreePaneRuler.java
 *
 * Copyright (C) 2006-2014 Andrew Rambaut
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

package figtree.treeviewer;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

/**
 * @author Andrew Rambaut
 * @version $Id$
 *
 * $HeadURL$
 *
 * $LastChangedBy$
 * $LastChangedDate$
 * $LastChangedRevision$
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
