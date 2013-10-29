package figtree.treeviewer;

import figtree.treeviewer.painters.*;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.*;
import figtree.treeviewer.decorators.Decorator;
import figtree.treeviewer.treelayouts.TreeLayout;
import jam.panels.StatusProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.util.*;
import java.util.List;
import java.util.regex.PatternSyntaxException;

/**
 * @author Andrew Rambaut
 * @version $Id: DefaultTreeViewer.java 822 2007-10-26 13:50:26Z rambaut $
 */
public class DefaultTreeViewer extends TreeViewer {
    private final static double ZOOM_SCALE = 0.02;
    private final static double VERTICAL_EXPANSION_SCALE = 0.02;
    private final static double ZOOM_POWER = 1.2;

    public DefaultTreeViewer() {
        this(null);
    }

    /**
     * Creates new TreeViewer
     */
    public DefaultTreeViewer(JFrame frame) {
        this.frame = frame;

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

//        if (treePane.getTipLabelPainter() != null) {
//            treePane.getTipLabelPainter().setupAttributes(trees);
//        }
//
//        if (treePane.getBranchLabelPainter() != null) {
//            treePane.getBranchLabelPainter().setupAttributes(trees);
//        }
//
//        if (treePane.getNodeLabelPainter() != null) {
//            treePane.getNodeLabelPainter().setupAttributes(trees);
//        }
//
//        if (treePane.getNodeBarPainter() != null) {
//            treePane.getNodeBarPainter().setupAttributes(trees);
//        }
//
//        if (treePane.getLegendPainter() != null) {
//            treePane.getLegendPainter().setupAttributes(trees);
//        }
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

    public List<Tree> getTreesAsViewed() {
        List<Tree> treeAsDisplayed = new ArrayList<Tree>();
        for (Tree originalTree : trees) {
            treeAsDisplayed.add(treePane.constructTransformedTree((RootedTree)originalTree));
        }
        return treeAsDisplayed;
    }

    public RootedTree getSelectedSubtree() {
        return treePane.getSelectedSubtree();
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
        if (isRootingOn() && getRootingType() == TreePane.RootingType.USER_ROOTING) {
            JOptionPane.showMessageDialog(frame, "Cannot switch trees when user rooting option is on.\n" +
                    "Turn this option off to switch trees",
                    "Unable to switch trees",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

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
    private double zoom = 0.0;
    private double verticalExpansion = 0.0;

    public void setZoom(double zoom) {
        double n = Math.max(treePane.getTree().getTaxa().size(), 50);
        this.zoom = Math.pow(zoom * n * ZOOM_SCALE, ZOOM_POWER);
//        this.zoom = zoom * MAX_ZOOM;
        refreshZoom();
    }

    public void setVerticalExpansion(double verticalExpansion) {
        double n = Math.max(treePane.getTree().getTaxa().size(), 50);
        this.verticalExpansion = Math.pow(verticalExpansion * n * VERTICAL_EXPANSION_SCALE, ZOOM_POWER);
//        this.verticalExpansion = verticalExpansion * MAX_VERTICAL_EXPANSION;
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

    public void selectTaxa(String attributeName, TextSearchType searchType, String searchString, boolean caseSensitive) {
        if (treePane.getTree() == null) {
            return;
        }

        treePane.clearSelection();

        String query = searchString;

        if (searchType != TextSearchType.REG_EX) {
            query = (caseSensitive ? searchString : searchString.toUpperCase());
            query = query.trim();
        }

        Tree tree = treePane.getTree();

        for (Node node : tree.getExternalNodes()) {
            Taxon taxon = tree.getTaxon(node);

            if (attributeName == null) {
                Object target = taxon.getName();
                if (matchesItem(searchType, target, query, caseSensitive)) {
                    treePane.addSelectedTip(node);
                    break;
                }
                for (String name : taxon.getAttributeNames()) {
                    target = taxon.getAttribute(name);
                    if (matchesItem(searchType, target, query, caseSensitive)) {
                        treePane.addSelectedTip(node);
                        break;
                    }
                }
            } else {
                Object target;
                if (attributeName.equals("!name")) {
                    target = taxon.getName();
                } else {
                    target = taxon.getAttribute(attributeName);
                }
                if (matchesItem(searchType, target, query, caseSensitive)) {
                    treePane.addSelectedTip(node);
                }
            }
        }
    }

    public void selectNodes(String attributeName, TextSearchType searchType, String searchString, boolean caseSensitive) {
        treePane.clearSelection();

        String query = searchString;

        if (searchType != TextSearchType.REG_EX) {
            query = (caseSensitive ? searchString : searchString.toUpperCase());
            query = query.trim();
        }

        Tree tree = treePane.getTree();

        for (Node node : tree.getNodes()) {
            if (attributeName == null) {
                for (String name : node.getAttributeNames()) {
                    Object target = node.getAttribute(name);
                    if (matchesItem(searchType, target, query, caseSensitive)) {
                        treePane.addSelectedNode(node);
                        break;
                    }
                }
            } else {
                Object target = node.getAttribute(attributeName);
                if (matchesItem(searchType, target, query, caseSensitive)) {
                    treePane.addSelectedNode(node);
                }
            }
        }
    }

    private boolean matchesItem(TextSearchType searchType, Object object, String query, boolean caseSensitive) {

        if (object != null) {
            String target = (caseSensitive ? object.toString() : object.toString().toUpperCase());
            switch (searchType) {
                case CONTAINS:
                    if (target.contains(query)) {
                        return true;
                    }
                    break;
                case STARTS_WITH:
                    if (target.startsWith(query)) {
                        return true;
                    }
                    break;
                case ENDS_WITH:
                    if (target.endsWith(query)) {
                        return true;
                    }
                    break;
                case MATCHES:
                    if (target.equals(query)) {
                        return true;
                    }
                    break;
                case REG_EX:
                    try {
                        if (target.matches(query)) {
                            return true;
                        }
                    } catch (PatternSyntaxException pse) {
                        // ignore
                    }
                    break;
            }
        }
        return false;
    }

    public void selectTaxa(String attributeName, NumberSearchType searchType, Number searchValue) {
        treePane.clearSelection();

        RootedTree tree = treePane.getTree();

        for (Node node : tree.getExternalNodes()) {
            Object value = null;
            if (attributeName.equals("!length")) {
                value = tree.getLength(node);
            } else if (attributeName.equals("!height")) {
                value = tree.getHeight(node);
            } else {
                Taxon taxon = tree.getTaxon(node);
                value = taxon.getAttribute(attributeName);
            }
            if (matchesItem(value, searchType, searchValue)) {
                treePane.addSelectedTip(node);
            }
        }
    }

    public void selectNodes(String attributeName, NumberSearchType searchType, Number searchValue) {
        treePane.clearSelection();

        RootedTree tree = treePane.getTree();

        for (Node node : tree.getNodes()) {
            Object value = null;
            if (attributeName.equals("!length")) {
                value = tree.getLength(node);
            } else if (attributeName.equals("!height")) {
                value = tree.getHeight(node);
            } else {
                value = node.getAttribute(attributeName);
            }
            if (matchesItem(value, searchType, searchValue)) {
                treePane.addSelectedNode(node);
            }
        }
    }

    public void selectTaxa(Collection<String> taxonNames) {
        treePane.clearSelection();

        RootedTree tree = treePane.getTree();

        for (Node node : tree.getExternalNodes()) {
            Object value = null;
            if (taxonNames.contains(tree.getTaxon(node).getName())) {
                treePane.addSelectedTip(node);
            }
        }
    }

    private boolean matchesItem(Object item, NumberSearchType searchType, Number searchValue) {
        if (item != null && item instanceof Number) {

            Number value = (Number)item;

            switch (searchType) {
                case EQUALS:
                    if (value.equals(searchValue)) {
                        return true;
                    }
                    break;
                case EQUALS_OR_GREATER_THAN:
                    if (value.doubleValue() >= searchValue.doubleValue()) {
                        return true;
                    }
                    break;
                case EQUALS_OR_LESS_THAN:
                    if (value.doubleValue() <= searchValue.doubleValue()) {
                        return true;
                    }
                    break;
                case GREATER_THAN:
                    if (value.doubleValue() > searchValue.doubleValue()) {
                        return true;
                    }
                    break;
                case LESS_THAN:
                    if (value.doubleValue() < searchValue.doubleValue()) {
                        return true;
                    }
                    break;
                case NOT_EQUALS:
                    if (!searchValue.equals(value)) {
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

    public void clearCollapsedNodes() {
        treePane.clearCollapsedNodes();
        fireTreeSettingsChanged();
    }

    public void hilightSelectedNodes(Color color) {
        treePane.hilightSelectedNodes(color);
        fireTreeSettingsChanged();
    }

    public void clearHilighting() {
        treePane.clearHilightedNodes();
        fireTreeSettingsChanged();
    }

    public void rerootOnSelectedBranch() {
        treePane.rerootOnSelectedBranch();
        fireTreeSettingsChanged();
    }

    public void clearRooting() {
        treePane.clearRooting();
        fireTreeSettingsChanged();
    }

    public void rotateSelectedNode() {
        treePane.rotateSelectedNode();
        fireTreeSettingsChanged();
    }

    public void clearRotations() {
        treePane.clearSelectedNodeRotations();
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

    public void clearAnnotation(String name) {
        treePane.clearSelectedNodeAnnotation(name);
        treePane.clearSelectedTipAnnotation(name);
        fireTreeSettingsChanged();
    }

    public void clearColouring() {
        treePane.clearSelectedNodeAnnotation("!color");
        treePane.clearSelectedTipAnnotation("!color");
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

    // A load of delegated method calls through to treePane (which is now hidden outside the package).
    public void setTipLabelPainter(LabelPainter<Node> tipLabelPainter) {
        treePane.setTipLabelPainter(tipLabelPainter);
//        tipLabelPainter.setupAttributes(trees);

    }

    public void setNodeLabelPainter(LabelPainter<Node> nodeLabelPainter) {
        treePane.setNodeLabelPainter(nodeLabelPainter);
//        nodeLabelPainter.setupAttributes(trees);
    }

    public void setNodeBarPainter(NodeBarPainter nodeBarPainter) {
        treePane.setNodeBarPainter(nodeBarPainter);
//        nodeBarPainter.setupAttributes(trees);
    }

    public void setNodeShapePainter(NodeShapePainter nodeShapePainter) {
        treePane.setNodeShapePainter(nodeShapePainter);
    }

    public void setBranchLabelPainter(LabelPainter<Node> branchLabelPainter) {
        treePane.setBranchLabelPainter(branchLabelPainter);
//        branchLabelPainter.setupAttributes(trees);
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

    public void setLegendPainter(LegendPainter legendPainter) {
        treePane.setLegendPainter(legendPainter);
//        legendPainter.setupAttributes(trees);
    }

    public void setBranchDecorator(Decorator branchDecorator, boolean isGradient) {
        treePane.setBranchDecorator(branchDecorator, isGradient);
    }

    public void setBranchColouringDecorator(String branchColouringAttribute, Decorator branchColouringDecorator) {
        treePane.setBranchColouringDecorator(branchColouringAttribute, branchColouringDecorator);
    }

    public void setNodeBackgroundDecorator(Decorator nodeBackgroundDecorator) {
        treePane.setNodeBackgroundDecorator(nodeBackgroundDecorator);
    }

    public void setHilightingGradient(boolean hilightingGradient) {
        treePane.setHilightingGradient(hilightingGradient);
    }

    public void setSelectionColor(Color selectionColor) {
        treePane.setSelectionColor(selectionColor);
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

    private final JFrame frame;
}