/*
 * AlignmentPanel.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package figtree.treeviewer;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.*;
import figtree.treeviewer.decorators.Decorator;
import figtree.treeviewer.painters.LabelPainter;
import figtree.treeviewer.painters.ScalePainter;
import figtree.treeviewer.painters.ScaleGridPainter;
import figtree.treeviewer.painters.NodeBarPainter;
import figtree.treeviewer.treelayouts.TreeLayout;
import figtree.treeviewer.annotations.AnnotationDefinition;
import org.virion.jam.panels.StatusProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.util.*;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id: DefaultTreeViewer.java 822 2007-10-26 13:50:26Z rambaut $
 */
public class DefaultTreeViewer extends TreeViewer {
	private final static double MAX_ZOOM = 20;
	private final static double MAX_VERTICAL_EXPANSION = 20;

	/**
	 * Creates new TreeViewer
	 */
	public DefaultTreeViewer() {
		setLayout(new BorderLayout());

		this.treePane = new TreePane();
		treePane.setAutoscrolls(true); //enable synthetic drag events

		treePane.addTreePaneListener(new TreePaneListener() {
			public void treePaneSettingsChanged() {
				fireTreeSettingsChanged();
			}
		});

		JScrollPane scrollPane = new JScrollPane(treePane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setMinimumSize(new Dimension(150, 150));

		scrollPane.setBorder(null);
		viewport = scrollPane.getViewport();

		add(scrollPane, BorderLayout.CENTER);

		// This overrides MouseListener and MouseMotionListener to allow selection in the TreePane -
		// It installs itself within the constructor.
		treePaneSelector = new TreePaneSelector(treePane);

		treePaneRollOver = new TreePaneRollOver(treePane);

		setFocusable(true);

	}

	public void setTree(Tree tree) {
		trees.clear();
		addTree(tree);
		showTree(0);
	}

	public void setTrees(Collection<? extends Tree> trees) {
		this.trees.clear();
		for (Tree tree : trees) {
			addTree(tree);
		}
		showTree(0);
	}

	public void addTree(Tree tree) {
		this.trees.add(tree);

		if (treePane.getTipLabelPainter() != null) {
			treePane.getTipLabelPainter().setupAttributes(trees);
		}

		if (treePane.getBranchLabelPainter() != null) {
			treePane.getBranchLabelPainter().setupAttributes(trees);
		}

		if (treePane.getNodeLabelPainter() != null) {
			treePane.getNodeLabelPainter().setupAttributes(trees);
		}

		if (treePane.getNodeBarPainter() != null) {
			treePane.getNodeBarPainter().setupAttributes(trees);
		}
	}

	public void addTrees(Collection<? extends Tree> trees) {
		int count = getTreeCount();
		for (Tree tree : trees) {
			addTree(tree);
		}
		showTree(count);
	}

	public List<Tree> getTrees() {
		return trees;
	}

	public Tree getCurrentTree() {
		return trees.get(currentTreeIndex);
	}

	public int getCurrentTreeIndex() {
		return currentTreeIndex;
	}

	public int getTreeCount() {
		if (trees == null) return 0;
		return trees.size();
	}

	public StatusProvider getStatusProvider() {
		return treePaneRollOver;
	}

	public void showTree(int index) {
		Tree tree = trees.get(index);
		if (tree instanceof RootedTree) {
			treePane.setTree((RootedTree)tree);
		} else {
			treePane.setTree(Utils.rootTheTree(tree));
		}

		currentTreeIndex = index;
		fireTreeChanged();
	}

	public void showNextTree() {
		if (currentTreeIndex < trees.size() - 1) {
			showTree(currentTreeIndex + 1);
		}
	}

	public void showPreviousTree() {
		if (currentTreeIndex > 0) {
			showTree(currentTreeIndex - 1);
		}
	}

	public void setTreeLayout(TreeLayout treeLayout) {
		treePane.setTreeLayout(treeLayout);
		fireTreeSettingsChanged();
	}

	private boolean zoomPending = false;
	private double zoom = 0.0, verticalExpansion = 0.0;

	public void setZoom(double zoom) {
		this.zoom = zoom * MAX_ZOOM;
		refreshZoom();
	}

	public void setVerticalExpansion(double verticalExpansion) {
		this.verticalExpansion = verticalExpansion * MAX_VERTICAL_EXPANSION;
		refreshZoom();
	}

	public boolean verticalExpansionAllowed() {
		return !treePane.maintainAspectRatio();
	}

	public void setTimeScale(TimeScale timeScale) {
		treePane.setTimeScale(timeScale);
	}

	private void refreshZoom() {
		setZoom(zoom, zoom + verticalExpansion);
	}

	private void setZoom(double xZoom, double yZoom) {

		Dimension viewportSize = viewport.getViewSize();
		Point position = viewport.getViewPosition();

		Dimension extentSize = viewport.getExtentSize();
		double w = extentSize.getWidth() * (1.0 + xZoom);
		double h = extentSize.getHeight() * (1.0 + yZoom);

		Dimension newSize = new Dimension((int) w, (int) h);
		treePane.setPreferredSize(newSize);

		double cx = position.getX() + (0.5 * extentSize.getWidth());
		double cy = position.getY() + (0.5 * extentSize.getHeight());

		double rx = ((double) newSize.getWidth()) / viewportSize.getWidth();
		double ry = ((double) newSize.getHeight()) / viewportSize.getHeight();

		double px = (cx * rx) - (extentSize.getWidth() / 2.0);
		double py = (cy * ry) - (extentSize.getHeight() / 2.0);

		Point newPosition = new Point((int) px, (int) py);
		viewport.setViewPosition(newPosition);
		treePane.revalidate();
	}

	public boolean hasSelection() {
		return treePane.hasSelection();
	}

	public Set<Node> getSelectedNodes() {
		return treePane.getSelectedNodes();
	}

	public Set<Node> getSelectedTips() {
		return treePane.getSelectedTips();
	}

	public void selectTaxa(SearchType searchType, String searchString, boolean caseSensitive) {
		treePane.clearSelection();

		String query = searchString;

		if (searchType != SearchType.REG_EX) {
			query = (caseSensitive ? searchString : searchString.toUpperCase());
			query = query.trim();
		}

		Tree tree = treePane.getTree();

		for (Node node : tree.getExternalNodes()) {
			Taxon taxon = tree.getTaxon(node);
			String target = (caseSensitive ? taxon.getName() : taxon.getName().toUpperCase());
			switch (searchType) {
				case CONTAINS:
					if (target.contains(query)) {
						treePane.addSelectedTip(node);
					}
					break;
				case STARTS_WITH:
					if (target.startsWith(query)) {
						treePane.addSelectedTip(node);
					}
					break;
				case ENDS_WITH:
					if (target.endsWith(query)) {
						treePane.addSelectedTip(node);
					}
					break;
				case MATCHES:
					if (target.equals(query)) {
						treePane.addSelectedTip(node);
					}
					break;
				case REG_EX:
					if (target.matches(query)) {
						treePane.addSelectedTip(node);
					}
					break;
			}
		}
	}

	public void selectNodes(String attribute, SearchType searchType, String searchString, boolean caseSensitive) {
		treePane.clearSelection();

		String query = searchString;

		if (searchType != SearchType.REG_EX) {
			query = (caseSensitive ? searchString : searchString.toUpperCase());
			query = query.trim();
		}

		Tree tree = treePane.getTree();

		for (Node node : tree.getNodes()) {
			if (attribute == null) {
				for (String attributeName : node.getAttributeNames()) {
					if (selectNodeWithAttribute(node, attributeName, searchType, query, caseSensitive)) {
						break;
					}
				}
			} else {
				selectNodeWithAttribute(node, attribute, searchType, query, caseSensitive);
			}
		}
	}

	private boolean selectNodeWithAttribute(Node node, String attribute, SearchType searchType, String query, boolean caseSensitive) {
		Object value = node.getAttribute(attribute);

		if (value != null) {
			String target = (caseSensitive ?
					value.toString() : value.toString().toUpperCase());
			switch (searchType) {
				case CONTAINS:
					if (target.contains(query)) {
						treePane.addSelectedNode(node);
						return true;
					}
					break;
				case STARTS_WITH:
					if (target.startsWith(query)) {
						treePane.addSelectedNode(node);
						return true;
					}
					break;
				case ENDS_WITH:
					if (target.endsWith(query)) {
						treePane.addSelectedNode(node);
						return true;
					}
					break;
				case MATCHES:
					if (target.equals(query)) {
						treePane.addSelectedNode(node);
						return true;
					}
					break;
				case REG_EX:
					if (target.matches(query)) {
						treePane.addSelectedNode(node);
						return true;
					}
					break;
			}
		}
		return false;
	}

	public void cartoonSelectedNodes() {
		treePane.cartoonSelectedNodes();
		fireTreeSettingsChanged();
	}

	public void collapseSelectedNodes() {
		treePane.collapseSelectedNodes();
		fireTreeSettingsChanged();
	}

	public void rerootOnSelectedBranch() {
		treePane.rerootOnSelectedBranch();
		fireTreeSettingsChanged();
	}

	public void rotateSelectedNode() {
		treePane.rotateSelectedNode();
		fireTreeSettingsChanged();
	}

	public void annotateSelectedNodes(String name, Object value) {
		treePane.annotateSelectedNodes(name, value);
		fireTreeSettingsChanged();
	}

	public void annotateSelectedTips(String name, Object value) {
		treePane.annotateSelectedTips(name, value);
		fireTreeSettingsChanged();
	}

	public void selectAll() {
		if (treePaneSelector.getSelectionMode() == TreePaneSelector.SelectionMode.TAXA) {
			treePane.selectAllTaxa();
		} else {
			treePane.selectAllNodes();
		}
	}

	public void clearSelectedTaxa() {
		treePane.clearSelection();
	}

	public void addTreeSelectionListener(TreeSelectionListener treeSelectionListener) {
		treePane.addTreeSelectionListener(treeSelectionListener);
	}

	public void removeTreeSelectionListener(TreeSelectionListener treeSelectionListener) {
		treePane.removeTreeSelectionListener(treeSelectionListener);
	}

	public void setSelectionMode(TreePaneSelector.SelectionMode selectionMode) {
		TreePaneSelector.SelectionMode oldSelectionMode = treePaneSelector.getSelectionMode();

		if (selectionMode == oldSelectionMode) {
			return;
		}

		if (oldSelectionMode == TreePaneSelector.SelectionMode.TAXA) {
			treePane.selectNodesFromSelectedTips();
		} else if (selectionMode == TreePaneSelector.SelectionMode.TAXA) {
			treePane.selectTipsFromSelectedNodes();
		} else if (selectionMode == TreePaneSelector.SelectionMode.CLADE) {
			treePane.selectCladesFromSelectedNodes();
		}
		treePaneSelector.setSelectionMode(selectionMode);
	}

	public void setDragMode(TreePaneSelector.DragMode dragMode) {
		treePaneSelector.setDragMode(dragMode);
	}

	// A load of deligated method calls through to treePane (which is now hidden outside the package).
	public void setTipLabelPainter(LabelPainter<Node> tipLabelPainter) {
		treePane.setTipLabelPainter(tipLabelPainter);
		tipLabelPainter.setupAttributes(trees);

	}

	public void setNodeLabelPainter(LabelPainter<Node> nodeLabelPainter) {
		treePane.setNodeLabelPainter(nodeLabelPainter);
		nodeLabelPainter.setupAttributes(trees);
	}

	public void setNodeBarPainter(NodeBarPainter nodeBarPainter) {
		treePane.setNodeBarPainter(nodeBarPainter);
		nodeBarPainter.setupAttributes(trees);
	}

	public void setBranchLabelPainter(LabelPainter<Node> branchLabelPainter) {
		treePane.setBranchLabelPainter(branchLabelPainter);
		branchLabelPainter.setupAttributes(trees);
	}

	public void addScalePainter(ScalePainter scalePainter) {
		treePane.addScalePainter(scalePainter);
	}

	public void removeScalePainter(ScalePainter scalePainter) {
		treePane.removeScalePainter(scalePainter);
	}

	public void setScaleGridPainter(ScaleGridPainter scaleGridPainter) {
		treePane.setScaleGridPainter(scaleGridPainter);
	}

	public void setBranchDecorator(Decorator branchDecorator) {
		treePane.setBranchDecorator(branchDecorator);
	}

	public void setBranchColouringDecorator(String branchColouringAttribute, Decorator branchColouringDecorator) {
		treePane.setBranchColouringDecorator(branchColouringAttribute, branchColouringDecorator);
	}

	public void setSelectionPaint(Paint selectionPane) {
		treePane.setSelectionPaint(selectionPane);
	}

	public Paint getSelectionPaint() {
		return treePane.getSelectionPaint();
	}

	public void setBranchStroke(BasicStroke branchStroke) {
		treePane.setBranchStroke(branchStroke);
	}

	public boolean isTransformBranchesOn() {
		return treePane.isTransformBranchesOn();
	}

	public TransformedRootedTree.Transform getBranchTransform() {
		return treePane.getBranchTransform();
	}

	public void setTransformBranchesOn(boolean transformBranchesOn) {
		treePane.setTransformBranchesOn(transformBranchesOn);
	}

	public void setBranchTransform(TransformedRootedTree.Transform transform) {
		treePane.setBranchTransform(transform);
	}

	public boolean isOrderBranchesOn() {
		return treePane.isOrderBranchesOn();
	}

	public SortedRootedTree.BranchOrdering getBranchOrdering() {
		return treePane.getBranchOrdering();
	}

	public void setOrderBranchesOn(boolean orderBranchesOn) {
		treePane.setOrderBranchesOn(orderBranchesOn);
	}

	public void setBranchOrdering(SortedRootedTree.BranchOrdering branchOrdering) {
		treePane.setBranchOrdering(branchOrdering);
	}

	public boolean isRootingOn() {
		return treePane.isRootingOn();
	}

	public TreePane.RootingType getRootingType() {
		return treePane.getRootingType();
	}

	public void setRootingOn(boolean rootingOn) {
		treePane.setRootingOn(rootingOn);
	}

	public void setRootingType(TreePane.RootingType rootingType) {
		treePane.setRootingType(rootingType);
	}

	public void setToolMode(TreePaneSelector.ToolMode toolMode) {
		treePaneSelector.setToolMode(toolMode);
	}

	public JComponent getContentPane() {
		return treePane;
	}

	public void paint(Graphics g) {
		if( zoomPending  ) {
			refreshZoom();
			zoomPending = false;
		}
		super.paint(g);
	}

	public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
		return treePane.print(g, pageFormat, pageIndex);
	}

	public void addTreeViewerListener(TreeViewerListener listener) {
		listeners.add(listener);
	}

	public void removeTreeViewerListener(TreeViewerListener listener) {
		listeners.remove(listener);
	}

	public void fireTreeChanged() {
		for (TreeViewerListener listener : listeners) {
			listener.treeChanged();
		}
	}

	public void fireTreeSettingsChanged() {
		for (TreeViewerListener listener : listeners) {
			listener.treeSettingsChanged();
		}
	}

	private java.util.List<TreeViewerListener> listeners = new ArrayList<TreeViewerListener>();

	private List<Tree> trees = new ArrayList<Tree>();
	private int currentTreeIndex = 0;

	protected TreePane treePane;
	protected TreePaneSelector treePaneSelector;
	protected TreePaneRollOver treePaneRollOver;

	protected JViewport viewport;

}