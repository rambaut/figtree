/*
 * TreePaneRollOver.java
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
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.RootedTreeUtils;
import jebl.util.NumberFormatter;
import jam.panels.StatusPanel;
import jam.panels.StatusProvider;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

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
public class TreePaneRollOver extends StatusProvider.Helper implements MouseMotionListener {

	public TreePaneRollOver(TreePane treePane) {
		this.treePane = treePane;
		treePane.addMouseMotionListener(this);
    }

	public void mouseEntered(MouseEvent mouseEvent) {
	}

	public void mouseExited(MouseEvent mouseEvent) {
	}

	public void mouseMoved(MouseEvent mouseEvent) {
		RootedTree tree = treePane.getTree();
		if (tree != null) {
			Node node = treePane.getNodeAt((Graphics2D) treePane.getGraphics(), mouseEvent.getPoint());
			if (node != null) {
				fireStatusChanged(StatusPanel.NORMAL, getNodeText(tree, node));
			} else {
				fireStatusChanged(StatusPanel.NORMAL, getNodeText(tree, tree.getRootNode()));
			}
		} else {
			fireStatusChanged(StatusPanel.NORMAL, " ");
		}
	}

    private String getNodeText(RootedTree tree, Node node) {
        StringBuilder sb = new StringBuilder();
        if (!tree.isExternal(node)) {
            int n = RootedTreeUtils.getTipCount(tree, node);
            sb.append(tree.isRoot(node) ? "Tree: " : "Subtree: ").append(n).append(" tips");
        } else {
            sb.append("Tip: \"").append(tree.getTaxon(node).toString()).append("\"");
        }
        sb.append(" [height = ").append(formatter.getFormattedValue(tree.getHeight(node)));
        sb.append(", length = ").append(formatter.getFormattedValue(tree.getLength(node)));
        sb.append("]");
        return sb.toString();
    }

	public void mouseDragged(MouseEvent mouseEvent) {
	}

	private TreePane treePane;
	private NumberFormatter formatter = new NumberFormatter(4); ;
}