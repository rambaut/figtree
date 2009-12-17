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
 * @version $Id: TreePaneRollOver.java 772 2007-09-04 16:27:27Z rambaut $
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
				StringBuilder sb = new StringBuilder();
				if (!tree.isExternal(node)) {
					int n = RootedTreeUtils.getTipCount(tree, node);
					sb.append("Subtree: ").append(n).append(" tips");
				} else {
					sb.append("Tip: \"").append(tree.getTaxon(node).toString()).append("\"");
				}
				sb.append(" [height = ").append(formatter.getFormattedValue(tree.getHeight(node)));
				sb.append(", length = ").append(formatter.getFormattedValue(tree.getLength(node)));
				sb.append("]");
				fireStatusChanged(StatusPanel.NORMAL, sb.toString());
			} else {
				fireStatusChanged(StatusPanel.NORMAL, " ");
			}
		} else {
			fireStatusChanged(StatusPanel.NORMAL, " ");
		}
	}

	public void mouseDragged(MouseEvent mouseEvent) {
	}

	private TreePane treePane;
	private NumberFormatter formatter = new NumberFormatter(4); ;
}