/*
 * TreePaneSelector.java
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

import jebl.evolution.graphs.Node;
import jam.mac.Utils;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

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
public class TreePaneSelector implements MouseListener, MouseMotionListener, KeyListener {
    public enum SelectionMode {
        CLADE,
        NODES,
        TIPS,
        TAXA
    };

    public enum DragMode {
        SELECT,
        SCROLL
    };

    public enum ToolMode {
        SELECT,
        ROOTING,
        CARTOONING,
        COLLAPSING,
        ROTATING,
        ANNOTATING,
        COLOURING
    };

    public TreePaneSelector(TreePane treePane) {
        this.treePane = treePane;
        treePane.addMouseListener(this);
        treePane.addMouseMotionListener(this);
        treePane.addKeyListener(this);
    }

    public SelectionMode getSelectionMode() {
        return selectionMode;
    }

    public DragMode getDragMode() {
        return dragMode;
    }

    public void setSelectionMode(SelectionMode selectionMode) {
        defaultSelectionMode = selectionMode;
        this.selectionMode = selectionMode;
    }

    public void setDragMode(DragMode dragMode) {
        this.dragMode = dragMode;
    }

    public void setToolMode(ToolMode toolMode) {
        this.toolMode = toolMode;
        setupCursor();
    }

    public boolean isCrossHairCursor() {
        return crossHairCursor;
    }

    public void setCrossHairCursor(boolean crossHairCursor) {
        this.crossHairCursor = crossHairCursor;
    }

    private void setupCursor() {
        if (toolMode != ToolMode.SELECT) {
            treePane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.CROSSHAIR_CURSOR));
            treePane.setCrosshairShown(crossHairCursor);
        } else if (dragMode == DragMode.SELECT) {
            if (crossHairCursor) {
                treePane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.CROSSHAIR_CURSOR));
            } else {
                treePane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
            treePane.setCrosshairShown(crossHairCursor);
        } else {
            treePane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            treePane.setCrosshairShown(false);

        }
        treePane.repaint();
    }

    public void mouseClicked(MouseEvent mouseEvent) {
        if (treePane.getTree() == null) {
            return;
        }

        if (toolMode == ToolMode.ROOTING) {
            Node node = treePane.getNodeAt((Graphics2D) treePane.getGraphics(), mouseEvent.getPoint());
            if (node != null) {
                treePane.setRootLocation(node, 0.5);
            }
        } else if (toolMode == ToolMode.ROTATING) {
            Node node = treePane.getNodeAt((Graphics2D) treePane.getGraphics(), mouseEvent.getPoint());
            treePane.rotateNode(node);
        } else if (dragMode == DragMode.SELECT) {
            boolean isCrossHairShown = treePane.isCrosshairShown();

            treePane.setCrosshairShown(false);

            Node selectedNode = treePane.getNodeAt((Graphics2D) treePane.getGraphics(), mouseEvent.getPoint());

            boolean extendSelection = mouseEvent.isShiftDown();
            boolean invertSelection = isCommandKeyDown(mouseEvent);

            if (!extendSelection && !invertSelection) {
                treePane.clearSelection();
            }

            SelectionMode mode = selectionMode;
            if (mouseEvent.isAltDown()) {
                if (mode == SelectionMode.NODES) {
                    mode = SelectionMode.CLADE;
                } else if (mode == SelectionMode.CLADE) {
                    mode = SelectionMode.NODES;
                }
            }

            switch (mode) {
                case NODES:
                    treePane.addSelectedNode(selectedNode, invertSelection, extendSelection);
                    break;
                case CLADE:
                    treePane.addSelectedClade(selectedNode, invertSelection, extendSelection);
                    break;
                case TIPS:
                    treePane.addSelectedTip(selectedNode, invertSelection, extendSelection);
                    break;
                case TAXA:
                    treePane.addSelectedTipLabel(selectedNode, invertSelection, extendSelection);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown SelectionMode: " + selectionMode.name());
            }

            treePane.setCrosshairShown(isCrossHairShown);
        }
    }

    public void mousePressed(MouseEvent mouseEvent) {
        treePane.requestFocus();

        // This is used for dragging in combination with mouseDragged
        // in the MouseMotionListener, below.
        dragPoint = new Point2D.Double(mouseEvent.getPoint().getX(), mouseEvent.getPoint().getY());
    }

    public void mouseReleased(MouseEvent mouseEvent) {
        if (treePane.getTree() == null) {
            return;
        }

        if (dragMode == DragMode.SELECT) {
            if (treePane.getDragRectangle() != null) {
                Set<Node> selectedNodes = treePane.getNodesAt((Graphics2D) treePane.getGraphics(), treePane.getDragRectangle().getBounds());

                boolean extendSelection = mouseEvent.isShiftDown();
                boolean invertSelection = isCommandKeyDown(mouseEvent);

                if (!extendSelection && !invertSelection) {
                    treePane.clearSelection();
                }

                SelectionMode mode = selectionMode;
                if (mouseEvent.isAltDown()) {
                    if (mode == SelectionMode.NODES) {
                        mode = SelectionMode.CLADE;
                    } else if (mode == SelectionMode.CLADE) {
                        mode = SelectionMode.NODES;
                    }
                }

                for (Node selectedNode : selectedNodes) {
                    switch (mode) {
                        case NODES:
                            treePane.addSelectedNode(selectedNode, invertSelection, extendSelection);
                            break;
                        case CLADE:
                            treePane.addSelectedClade(selectedNode, invertSelection, extendSelection);
                            break;
                        case TIPS:
                            treePane.addSelectedTip(selectedNode, invertSelection, extendSelection);
                            break;
                        case TAXA:
                            treePane.addSelectedTipLabel(selectedNode, invertSelection, extendSelection);
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown SelectionMode: " + selectionMode.name());
                    }
                }
            }
        }
        treePane.setDragRectangle(null);
    }

    public void mouseEntered(MouseEvent mouseEvent) {
//        treePane.requestFocusInWindow();
        if (isCommandKeyDown(mouseEvent)) {
            treePane.setCursorPosition(mouseEvent.getPoint());
        }
    }

    public void mouseExited(MouseEvent mouseEvent) {
        if (isCommandKeyDown(mouseEvent)) {
            treePane.setCursorPosition(mouseEvent.getPoint());
        }
    }

    public void mouseMoved(MouseEvent mouseEvent) {
        if (isCommandKeyDown(mouseEvent)) {
            treePane.setCursorPosition(mouseEvent.getPoint());
        }
    }

    /**
     * On Mac, check for the 'Command' key, otherwise use the 'Control' key
     * @param event
     * @return is it pressed
     */
    private boolean isCommandKeyDown(InputEvent event) {
        return Utils.isMacOSX() ? event.isMetaDown() : event.isControlDown();
    }

    /**
     * On Mac, check for the 'Option' key, otherwise use the 'Alt' key
     * @param event
     * @return is it pressed
     */
    private boolean isOptionKeyDown(InputEvent event) {
        return event.isAltDown();
    }

    public void mouseDragged(MouseEvent mouseEvent) {

        if (toolMode != ToolMode.SELECT || dragPoint == null) {
            return;
        }

        if (dragMode == DragMode.SCROLL) {
            // Calculate how far the mouse has been dragged from the point clicked in
            // mousePressed, above.
            int deltaX = (int) (mouseEvent.getX() - dragPoint.getX());
            int deltaY = (int) (mouseEvent.getY() - dragPoint.getY());

            // Get the currently visible window
            Rectangle visRect = treePane.getVisibleRect();

            // Calculate how much we need to scroll
            if (deltaX > 0) {
                deltaX = visRect.x - deltaX;
            } else {
                deltaX = visRect.x + visRect.width - deltaX;
            }

            if (deltaY > 0) {
                deltaY = visRect.y - deltaY;
            } else {
                deltaY = visRect.y + visRect.height - deltaY;
            }

            // Scroll the visible region
            Rectangle r = new Rectangle(deltaX, deltaY, 1, 1);
            treePane.scrollRectToVisible(r);
        } else {
            double x1 = Math.min(dragPoint.getX(), mouseEvent.getPoint().getX());
            double y1 = Math.min(dragPoint.getY(), mouseEvent.getPoint().getY());
            double x2 = Math.max(dragPoint.getX(), mouseEvent.getPoint().getX());
            double y2 = Math.max(dragPoint.getY(), mouseEvent.getPoint().getY());
            treePane.setDragRectangle(new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1));
            treePane.scrollPointToVisible(mouseEvent.getPoint());
        }
    }

    public void keyTyped(KeyEvent event) {
    }

    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_SPACE) {
            dragMode = DragMode.SCROLL;
        }
        if (isOptionKeyDown(event)) {
            switch (defaultSelectionMode) {
                case NODES:
                case CLADE:
                    selectionMode = SelectionMode.TAXA;
                    break;
                case TAXA:
                    selectionMode = SelectionMode.NODES;
                    break;
            }
        } else {
            selectionMode = defaultSelectionMode;
        }
        crossHairCursor = isCommandKeyDown(event);
        setupCursor();
    }

    public void keyReleased(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_SPACE) {
            dragMode = DragMode.SELECT;
        }
        crossHairCursor = isCommandKeyDown(event);
        setupCursor();
    }

    private TreePane treePane;

    private SelectionMode defaultSelectionMode = SelectionMode.NODES;
    private SelectionMode selectionMode = SelectionMode.NODES;

    private ToolMode toolMode = ToolMode.SELECT;

    private DragMode dragMode = DragMode.SELECT;
    private Point2D dragPoint = null;

    private boolean crossHairCursor = false;
}
