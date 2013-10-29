package figtree.treeviewer;

import jebl.evolution.graphs.Node;
import jebl.evolution.graphs.Graph;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.*;
import figtree.treeviewer.decorators.*;
import figtree.treeviewer.painters.*;
import figtree.treeviewer.treelayouts.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id: TreePane.java 823 2007-10-28 20:28:47Z stevensh $
 */
public class TreePane extends JComponent implements PainterListener, Printable {
    public enum RootingType {
        USER_ROOTING("User Selection"),
        MID_POINT("Midpoint");
//		LEAST_SQUARES("least squares");

        RootingType(String name) {
            this.name = name;
        }

        public String toString() { return name; }

        private String name;
    }


    public final static boolean DEBUG_OUTLINE = false;

    public final String CARTOON_ATTRIBUTE_NAME = "!cartoon";
    public final String COLLAPSE_ATTRIBUTE_NAME = "!collapse";
    public final String HILIGHT_ATTRIBUTE_NAME = "!hilight";

    public TreePane() {
    }

    public RootedTree getTree() {
        return tree;
    }

    public void setTree(RootedTree tree) {
        if (tree != null) {
            this.originalTree = tree;
            if (!originalTree.hasLengths()) {
                transformBranchesOn = true;
            }
            setupTree();
        } else {
            originalTree = null;
            this.tree = null;
            invalidate();
            repaint();
        }
    }

    private void setupTree() {
        tree = constructTransformedTree(originalTree);

        recalculateCollapsedNodes();

        calibrated = false;
        invalidate();
        repaint();
    }

    public RootedTree constructTransformedTree(RootedTree sourceTree) {
        RootedTree newTree = sourceTree;

        if (isRootingOn) {
            if (rootingType == RootingType.MID_POINT) {
                newTree = ReRootedTree.rootTreeAtCenter(newTree);
            } else if (rootingType == RootingType.USER_ROOTING && rootingNode != null) {
                Node left = newTree.getParent(rootingNode);
                if (left != null) {
                    // rooting length should be [0, 1]

                    double length = newTree.hasLengths() ? newTree.getLength(rootingNode) * rootingLength : 1.0;
                    try {
                        newTree = new ReRootedTree(newTree, left, rootingNode, length);
                    } catch (Graph.NoEdgeException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (orderBranchesOn) {
            newTree = new SortedRootedTree(newTree, branchOrdering);
        }

        if (transformBranchesOn || !sourceTree.hasLengths()) {
            newTree = new TransformedRootedTree(newTree, branchTransform);
        }

        return newTree;
    }

    public TreeLayout getTreeLayout() {
        return treeLayout;
    }

    public TreeLayoutCache getTreeLayoutCache() {
        return treeLayoutCache;
    }

    public void setTreeLayout(TreeLayout treeLayout) {

        this.treeLayout = treeLayout;

        treeLayout.setCartoonAttributeName(CARTOON_ATTRIBUTE_NAME);
        treeLayout.setCollapsedAttributeName(COLLAPSE_ATTRIBUTE_NAME);
        treeLayout.setHilightAttributeName(HILIGHT_ATTRIBUTE_NAME);
        treeLayout.setBranchColouringAttributeName(branchColouringAttribute);

        treeLayout.addTreeLayoutListener(new TreeLayoutListener() {
            public void treeLayoutChanged() {
                calibrated = false;
                repaint();
            }
        });
        calibrated = false;
        invalidate();
        repaint();
    }

    public TimeScale getTimeScale() {
        return timeScale;
    }

    public void setTimeScale(TimeScale timeScale) {
        this.timeScale = timeScale;
        calibrated = false;
        repaint();
    }

    public boolean isCrosshairShown() {
        return isCrosshairShown;
    }

    public void setCrosshairShown(boolean crosshairShown) {
        isCrosshairShown = crosshairShown;
    }

    public void setCursorPosition(Point point) {
        cursorPosition = point;
        if (cursorPosition != null) {
            double xPos = (point.getX() - treeBounds.getX()) / treeBounds.getWidth();
            xPos = (xPos < 0.0 ? 0.0 : xPos > 1.0 ? 1.0 : xPos);
            double yPos = (point.getY() - treeBounds.getY()) / treeBounds.getHeight();
            yPos = (yPos < 0.0 ? 0.0 : yPos > 1.0 ? 1.0 : yPos);
            treeLayout.setPointOfInterest(xPos, yPos);
        }
    }

    public void midpointRoot() {
        isRootingOn = true;
        rootingType = RootingType.MID_POINT;

        setupTree();

        fireSettingsChanged();
    }

    public void setRootLocation(Node node, double length) {
        RootedTree source = tree;

        if (tree instanceof FilteredRootedTree) {
            source = ((FilteredRootedTree) tree).getSource();
        }

        if (source instanceof  ReRootedTree) {
            rootingNode = ((ReRootedTree)source).getSourceNode(node);
        } else {
            rootingNode = node;
        }
        rootingLength = length;

        isRootingOn = true;
        rootingType = RootingType.USER_ROOTING;

        setupTree();

        fireSettingsChanged();
    }



    public void rotateNode(Node node) {
        if (node != null) {
            Boolean rotate = (Boolean)node.getAttribute("!rotate");
            if (rotate != null) {
                rotate = !rotate;
            } else {
                rotate = true;
            }
            node.setAttribute("!rotate", rotate);

            calibrated = false;
            invalidate();
            repaint();
        }

    }

    public void clearRotation(Node node) {
        if (node != null) {
            Boolean rotate = (Boolean)node.getAttribute("!rotate");
            if (rotate != null) {
                node.removeAttribute("!rotate");
            }

            calibrated = false;
            invalidate();
            repaint();
        }

    }

    public void setBranchDecorator(Decorator branchDecorator, boolean isGradient) {
        this.branchDecorator = branchDecorator;
        this.branchDecoratorGradient = isGradient;
        repaint();
    }

    public void setBranchColouringDecorator(String branchColouringAttribute, Decorator branchColouringDecorator) {
        this.branchColouringAttribute = branchColouringAttribute;
        treeLayout.setBranchColouringAttributeName(branchColouringAttribute);
        this.branchColouringDecorator = branchColouringDecorator;
        repaint();
    }

    public boolean isHilightingGradient() {
        return hilightingGradient;
    }

    public void setHilightingGradient(boolean hilightingGradient) {
        this.hilightingGradient = hilightingGradient;
        repaint();
    }

    public void setNodeBackgroundDecorator(Decorator nodeBackgroundDecorator) {
        this.nodeBackgroundDecorator = nodeBackgroundDecorator;
        repaint();
    }

    public Rectangle2D getTreeBounds() {
        return treeBounds;
    }

    /**
     * This returns the scaling factor between the graphical image and the branch
     * lengths of the tree
     *
     * @return the tree scale
     */
    public double getTreeScale() {
        return treeScale / timeScale.getScaleFactor(tree);
    }

    /**
     *	Transform a chart co-ordinates into a drawing co-ordinates
     */
    public double scaleOnAxis(double value) {
        double height = timeScale.getHeight(value, tree);
        if (treeLayout.isAxisReversed()) {
            return treeBounds.getX() + treeBounds.getWidth() - (height * treeScale);
        } else {
            return treeBounds.getX() + (height * treeScale);
        }
    }

    public Shape getAxisLine(double value) {
        double height = timeScale.getHeight(value, tree);
        Shape line = treeLayout.getAxisLine(height);
        if (line != null) {
            return transform.createTransformedShape(line);
        }
        return null;
    }


    public ScaleAxis getScaleAxis() {
        return scaleAxis;
    }

    public double getAxisOrigin() {
        return axisOrigin;
    }

    public void setAxisOrigin(double axisOrigin) {
        this.axisOrigin = axisOrigin;
        calibrated = false;
        repaint();
    }

    public void setAxisReversed(final boolean isAxisReversed) {
        treeLayout.setAxisReversed(isAxisReversed);
        calibrated = false;
        repaint();
    }

    private void setupScaleAxis() {
        double treeHeight = tree.getHeight(tree.getRootNode()) + treeLayout.getRootLength();
        double minValue = timeScale.getAge(0.0, tree);
        double maxValue = timeScale.getAge(treeHeight, tree);

        if (minValue < maxValue) {
            if (axisOrigin < minValue) {
                minValue = axisOrigin;
            }
            scaleAxis.setRange(minValue, maxValue);
        } else {
            if (axisOrigin > minValue) {
                minValue = axisOrigin;
            }
            scaleAxis.setRange(maxValue, minValue);
        }
    }

    public void setRootAge(double rootAge) {
        double rootLength = timeScale.getHeight(rootAge, tree) - tree.getHeight(tree.getRootNode());
        treeLayout.setRootLength(rootLength);
        calibrated = false;
        repaint();
    }

    public double getRootAge() {
        double treeHeight = tree.getHeight(tree.getRootNode()) + treeLayout.getRootLength();
        return timeScale.getAge(treeHeight, tree);
    }

    public double getMajorTickSpacing() {
        return scaleAxis.getMajorTickSpacing();
    }

    public double getMinorTickSpacing() {
        return scaleAxis.getMinorTickSpacing();
    }

    public void setTickSpacing(double userMajorTickSpacing, double userMinorTickSpacing) {
        scaleAxis.setManualAxis(userMajorTickSpacing, userMinorTickSpacing);
        calibrated = false;
        repaint();
    }

    public void setAutomaticScale() {
        scaleAxis.setAutomatic();
        calibrated = false;
        repaint();
    }

    public void painterChanged() {
        calibrated = false;
        repaint();
    }

    public void painterSettingsChanged() {
        calibrated = false;
        repaint();
    }

    public void attributesChanged() {
        calibrated = false;
        repaint();
    }

    public BasicStroke getBranchStroke() {
        return branchLineStroke;
    }

    public void setBranchStroke(BasicStroke stroke) {
        branchLineStroke = stroke;
        float weight = stroke.getLineWidth();
        selectionStroke = new BasicStroke(Math.max(weight + 4.0F, weight * 1.5F), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        repaint();
    }

    public BasicStroke getCalloutStroke() {
        return calloutStroke;
    }

    public void setCalloutStroke(BasicStroke calloutStroke) {
        this.calloutStroke = calloutStroke;
    }

    public Paint getSelectionPaint() {
        return selectionPaint;
    }

    public void setSelectionColor(Color selectionColor) {
        this.selectionPaint = new Color(
                selectionColor.getRed(),
                selectionColor.getGreen(),
                selectionColor.getBlue(),
                128);
    }

    public boolean isTransformBranchesOn() {
        return transformBranchesOn;
    }

    public void setTransformBranchesOn(boolean transformBranchesOn) {
        this.transformBranchesOn = transformBranchesOn;
        setupTree();
    }

    public TransformedRootedTree.Transform getBranchTransform() {
        return branchTransform;
    }

    public void setBranchTransform(TransformedRootedTree.Transform branchTransform) {
        this.branchTransform = branchTransform;
        setupTree();
    }

    public boolean isOrderBranchesOn() {
        return orderBranchesOn;
    }

    public void setOrderBranchesOn(boolean orderBranchesOn) {
        this.orderBranchesOn = orderBranchesOn;
        setupTree();
    }

    public SortedRootedTree.BranchOrdering getBranchOrdering() {
        return branchOrdering;
    }

    public void setBranchOrdering(SortedRootedTree.BranchOrdering branchOrdering) {
        this.branchOrdering = branchOrdering;
        setupTree();
    }

    public boolean isRootingOn() {
        return isRootingOn;
    }

    public RootingType getRootingType() {
        return rootingType;
    }

    public void setRootingOn(boolean rootingOn) {
        this.isRootingOn = rootingOn;
        setupTree();
    }

    public void setRootingType(RootingType rootingType) {
        this.rootingType = rootingType;
        setupTree();
    }

    public RootedTree getOriginalTree() {
        return originalTree;
    }

    public boolean isShowingTipCallouts() {
        return showingTipCallouts;
    }

    public void setShowingTipCallouts(boolean showingTipCallouts) {
        this.showingTipCallouts = showingTipCallouts;
        calibrated = false;
        repaint();
    }

    public void setSelectedNode(Node selectedNode) {
        selectedNodes.clear();
        selectedTips.clear();
        addSelectedNode(selectedNode, false, false);
    }

    public void setSelectedTip(Node selectedTip) {
        selectedNodes.clear();
        selectedTips.clear();
        addSelectedTip(selectedTip, false, false);
    }

    public void setSelectedClade(Node selectedNode) {
        selectedNodes.clear();
        selectedTips.clear();
        addSelectedClade(selectedNode, false, false);
    }

    public void setSelectedTips(Node selectedNode) {
        selectedNodes.clear();
        selectedTips.clear();
        addSelectedTips(selectedNode, false);
    }

    private boolean canSelectNode(Node selectedNode) {
        return selectedNode != null;
    }

    public void addSelectedNode(Node selectedNode) {
        addSelectedNode(selectedNode, false, false);
    }

    public void addSelectedNode(Node selectedNode, boolean toggle, boolean extend) {
        amendNodeSelection(selectedNode, toggle, extend);
        fireSelectionChanged();
        clearSelectionPaths();
        repaint();
    }

    public void addSelectedTip(Node selectedTip) {
        addSelectedTip(selectedTip, false, false);
    }

    public void addSelectedTip(Node selectedTip, boolean toggle, boolean extend) {
        amendNodeSelection(selectedTip, toggle, extend);
        selectTipsFromSelectedNodes();
        fireSelectionChanged();
        clearSelectionPaths();
        repaint();
    }


    public void addSelectedClade(Node selectedNode) {
        addSelectedClade(selectedNode, false, false);
    }

    public void addSelectedClade(Node selectedNode, boolean toggle, boolean extend) {
        if (canSelectNode(selectedNode)) {
            amendCladeSelection(selectedNode, toggle, extend);
        }
        fireSelectionChanged();
        clearSelectionPaths();
        repaint();
    }

    private void amendNodeSelection(Node selectedNode, boolean toggle, boolean extend) {
        if ( !canSelectNode(selectedNode) ) {
            return;
        }

        if (extend) {
            Set<Node> nodeSet = new HashSet<Node>(selectedNodes);
            nodeSet.add(selectedNode);
            Node mrca = RootedTreeUtils.getCommonAncestorNode(tree, nodeSet);

            for (Node node : nodeSet) {
                while (node != null && node != mrca) {
                    amendNodeSelection(node, false, false);
                    node = tree.getParent(node);
                }
            }
        } else {
            if (toggle && selectedNodes.contains(selectedNode)) {
                selectedNodes.remove(selectedNode);
            } else {
                selectedNodes.add(selectedNode);
            }
        }
    }

    private void amendCladeSelection(Node selectedNode, boolean toggle, boolean extend) {
        if ( !canSelectNode(selectedNode) ) {
            return;
        }

        if (extend) {
            Set<Node> nodeSet = new HashSet<Node>(selectedNodes);
            nodeSet.add(selectedNode);
            Node mrca = RootedTreeUtils.getCommonAncestorNode(tree, nodeSet);

            for (Node node : nodeSet) {
                while (node != null && node != mrca) {
                    amendCladeSelection(node, false, false);
                    node = tree.getParent(node);
                }
            }
        } else {
            if (toggle && selectedNodes.contains(selectedNode)) {
                selectedNodes.remove(selectedNode);
            } else {
                selectedNodes.add(selectedNode);
            }
        }
        for (Node child : tree.getChildren(selectedNode)) {
            amendCladeSelection(child, toggle, false);
        }
    }

    public void addSelectedTips(Node selectedNode) {
        addSelectedTips(selectedNode, false);
    }

    public void addSelectedTips(Node selectedNode, boolean toggle) {
        if (selectedNode != null) {
            addSelectedChildTips(selectedNode, toggle);
        }
        fireSelectionChanged();
        clearSelectionPaths();
        repaint();
    }

    private void addSelectedChildTips(Node selectedNode, boolean toggle) {
        if (tree.isExternal(selectedNode)) {
            if (toggle && selectedTips.contains(selectedNode)) {
                selectedTips.remove(selectedNode);
            } else {
                selectedTips.add(selectedNode);
            }
        } else {
            for (Node child : tree.getChildren(selectedNode)) {
                addSelectedChildTips(child, toggle);
            }
        }
    }

    public void selectCladesFromSelectedNodes() {
        Set<Node> nodes = new HashSet<Node>(selectedNodes);
        selectedNodes.clear();
        for (Node node : nodes) {
            addSelectedClade(node, false, false);
        }
        fireSelectionChanged();
        clearSelectionPaths();
        repaint();
    }

    public void selectTipsFromSelectedNodes() {
        for (Node node : selectedNodes) {
            addSelectedChildTips(node, false);
        }
        selectedNodes.clear();
        fireSelectionChanged();
        clearSelectionPaths();
        repaint();
    }

    public void selectNodesFromSelectedTips() {
        if (selectedTips.size() > 0) {
            Node node = RootedTreeUtils.getCommonAncestorNode(tree, selectedTips);
            addSelectedClade(node, false, false);
        }

        selectedTips.clear();
        fireSelectionChanged();
        clearSelectionPaths();
        repaint();
    }

    public void selectAllTaxa() {
        selectedTips.addAll(tree.getExternalNodes());
        fireSelectionChanged();
        clearSelectionPaths();
        repaint();
    }

    public void selectAllNodes() {
        selectedNodes.addAll(tree.getNodes());
        fireSelectionChanged();
        clearSelectionPaths();
        repaint();
    }

    public void clearSelection() {
        selectedNodes.clear();
        selectedTips.clear();
        fireSelectionChanged();
        clearSelectionPaths();
        repaint();
    }

    public boolean hasSelection() {
        return selectedNodes.size() > 0 || selectedTips.size() > 0;
    }

    public void cartoonSelectedNodes() {
        cartoonSelectedNodes(tree.getRootNode());
    }

    private void cartoonSelectedNodes(Node node) {

        if (!tree.isExternal(node)) {
            if (selectedNodes.contains(node)) {
                if (node.getAttribute(CARTOON_ATTRIBUTE_NAME) != null) {
                    node.removeAttribute(CARTOON_ATTRIBUTE_NAME);
                } else {
                    int tipCount = RootedTreeUtils.getTipCount(tree, node);
                    double height = RootedTreeUtils.getMinTipHeight(tree, node);
                    Object[] values = new Object[] { tipCount, height };
                    node.setAttribute(CARTOON_ATTRIBUTE_NAME, values);
                }
                calibrated = false;
                repaint();
            } else {
                for (Node child : tree.getChildren(node)) {
                    cartoonSelectedNodes(child);
                }
            }
        }
    }

    public void collapseSelectedNodes() {
        collapseSelectedNodes(tree.getRootNode());
    }

    private void collapseSelectedNodes(Node node) {

        if (!tree.isExternal(node)) {
            if (selectedNodes.contains(node)) {
                if (node.getAttribute(COLLAPSE_ATTRIBUTE_NAME) != null) {
                    node.removeAttribute(COLLAPSE_ATTRIBUTE_NAME);
                } else {
                    String tipName = "collapsed";
                    double height = RootedTreeUtils.getMinTipHeight(tree, node);
                    Object[] values = new Object[] { tipName, height };
                    node.setAttribute(COLLAPSE_ATTRIBUTE_NAME, values);
                }
                calibrated = false;
                repaint();
            } else {
                for (Node child : tree.getChildren(node)) {
                    collapseSelectedNodes(child);
                }
            }
        }
    }

    public void hilightSelectedNodes(Color color) {
        hilightSelectedNodes(tree.getRootNode(), color);
    }

    private void hilightSelectedNodes(Node node, Color color) {

        if (!tree.isExternal(node)) {
            if (selectedNodes.contains(node)) {
                int tipCount = RootedTreeUtils.getTipCount(tree, node);
                double height = RootedTreeUtils.getMinTipHeight(tree, node);
                Object[] values = new Object[] { tipCount, height, color };
                node.setAttribute(HILIGHT_ATTRIBUTE_NAME, values);

                calibrated = false;
                repaint();
            } else {
                for (Node child : tree.getChildren(node)) {
                    hilightSelectedNodes(child, color);
                }
            }
        }
    }

    public void recalculateCollapsedNodes() {
        recalculateCollapsedNodes(tree.getRootNode());
    }

    private void recalculateCollapsedNodes(Node node) {

        if (!tree.isExternal(node)) {
            if (selectedNodes.contains(node)) {
                if (node.getAttribute(CARTOON_ATTRIBUTE_NAME) != null) {
                    int tipCount = RootedTreeUtils.getTipCount(tree, node);
                    double height = RootedTreeUtils.getMinTipHeight(tree, node);
                    Object[] values = new Object[] { tipCount, height };
                    node.setAttribute(CARTOON_ATTRIBUTE_NAME, values);
                }
                if (node.getAttribute(COLLAPSE_ATTRIBUTE_NAME) != null) {
                    String tipName = "collapsed";
                    double height = RootedTreeUtils.getMinTipHeight(tree, node);
                    Object[] values = new Object[] { tipName, height };
                    node.setAttribute(COLLAPSE_ATTRIBUTE_NAME, values);
                }
                Object[] oldValues = (Object[])node.getAttribute(HILIGHT_ATTRIBUTE_NAME);
                if (oldValues != null) {
                    int tipCount = RootedTreeUtils.getTipCount(tree, node);
                    double height = RootedTreeUtils.getMinTipHeight(tree, node);
                    Object[] values = new Object[] { tipCount, height, oldValues[2] };
                    node.setAttribute(HILIGHT_ATTRIBUTE_NAME, values);
                }
                calibrated = false;
                repaint();
            } else {
                for (Node child : tree.getChildren(node)) {
                    recalculateCollapsedNodes(child);
                }
            }
        }
    }

    public void clearCollapsedNodes() {
        clearSelectedCollapsedNodes(tree.getRootNode());
    }

    private void clearSelectedCollapsedNodes(Node node) {

        if (!tree.isExternal(node)) {
            // Although collapsed nodes could be nested, we don't go
            // deeper. So one 'clear collapsed' will reveal any nested
            // collapsed nodes.
            if (selectedNodes.contains(node)) {
                if (node.getAttribute(COLLAPSE_ATTRIBUTE_NAME) != null) {
                    node.removeAttribute(COLLAPSE_ATTRIBUTE_NAME);
                }
                if (node.getAttribute(CARTOON_ATTRIBUTE_NAME) != null) {
                    node.removeAttribute(CARTOON_ATTRIBUTE_NAME);
                }
                calibrated = false;
                repaint();
            } else {
                for (Node child : tree.getChildren(node)) {
                    clearSelectedCollapsedNodes(child);
                }
            }
        }
    }

    public void clearHilightedNodes() {
        clearSelectedHilightedNodes(tree.getRootNode());
    }

    private void clearSelectedHilightedNodes(Node node) {

        if (!tree.isExternal(node)) {
            if (selectedNodes.contains(node)) {
                if (node.getAttribute(HILIGHT_ATTRIBUTE_NAME) != null) {
                    node.removeAttribute(HILIGHT_ATTRIBUTE_NAME);
                    calibrated = false;
                    repaint();
                }
            }
            for (Node child : tree.getChildren(node)) {
                clearSelectedHilightedNodes(child);
            }
        }
    }

    public void rerootOnSelectedBranch() {

        for (Node selectedNode : selectedNodes) {
            setRootLocation(selectedNode, 0.5);
            // root on the first selected branch...
            // Check for multiple selected branch elsewhere
            return;
        }
        repaint();
    }

    public void clearRooting() {
        rootingNode = null;

        setupTree();

        fireSettingsChanged();
    }

    public void rotateSelectedNode() {
        for (Node selectedNode : selectedNodes) {
            rotateNode(selectedNode);
        }
        repaint();
    }

    public void clearSelectedNodeRotations() {
        for (Node selectedNode : selectedNodes) {
            clearRotation(selectedNode);
        }
        repaint();
    }

    public void annotateSelectedNodes(String name, Object value) {
        for (Node selectedNode : selectedNodes) {
            selectedNode.setAttribute(name, value);
        }
        repaint();
    }

    public void annotateSelectedTips(String name, Object value) {
        for (Node selectedTip : selectedTips) {
            Taxon selectedTaxon = tree.getTaxon(selectedTip);
//            if (selectedTaxon == null) {
//                throw new IllegalArgumentException("missing taxon?");
//            }
            selectedTaxon.setAttribute(name, value);
        }
        repaint();
    }

    public void clearSelectedNodeAnnotation(String name) {
        for (Node selectedNode : selectedNodes) {
            selectedNode.removeAttribute(name);
        }
        repaint();
    }

    public void clearSelectedTipAnnotation(String name) {
        for (Node selectedTip : selectedTips) {
            Taxon selectedTaxon = tree.getTaxon(selectedTip);
            selectedTaxon.removeAttribute(name);
        }
        repaint();
    }

    /**
     * Return whether the two axis scales should be maintained
     * relative to each other
     *
     * @return a boolean
     */
    public boolean maintainAspectRatio() {
        return treeLayout.maintainAspectRatio();
    }

    public void setTipLabelPainter(LabelPainter<Node> tipLabelPainter) {
        tipLabelPainter.setTreePane(this);
        if (this.tipLabelPainter != null) {
            this.tipLabelPainter.removePainterListener(this);
        }
        this.tipLabelPainter = tipLabelPainter;
        if (this.tipLabelPainter != null) {
            this.tipLabelPainter.addPainterListener(this);
        }
        calibrated = false;
        repaint();
    }

    public LabelPainter<Node> getTipLabelPainter() {
        return tipLabelPainter;
    }

    public void setNodeLabelPainter(LabelPainter<Node> nodeLabelPainter) {
        nodeLabelPainter.setTreePane(this);
        if (this.nodeLabelPainter != null) {
            this.nodeLabelPainter.removePainterListener(this);
        }
        this.nodeLabelPainter = nodeLabelPainter;
        if (this.nodeLabelPainter != null) {
            this.nodeLabelPainter.addPainterListener(this);
        }
        calibrated = false;
        repaint();
    }

    public LabelPainter<Node> getNodeLabelPainter() {
        return nodeLabelPainter;
    }

    public void setBranchLabelPainter(LabelPainter<Node> branchLabelPainter) {
        branchLabelPainter.setTreePane(this);
        if (this.branchLabelPainter != null) {
            this.branchLabelPainter.removePainterListener(this);
        }
        this.branchLabelPainter = branchLabelPainter;
        if (this.branchLabelPainter != null) {
            this.branchLabelPainter.addPainterListener(this);
        }
        calibrated = false;
        repaint();
    }

    public LabelPainter<Node> getBranchLabelPainter() {
        return branchLabelPainter;
    }

    public void setNodeBarPainter(NodeBarPainter nodeBarPainter) {
        nodeBarPainter.setTreePane(this);
        if (this.nodeBarPainter != null) {
            this.nodeBarPainter.removePainterListener(this);
        }
        this.nodeBarPainter = nodeBarPainter;
        if (this.nodeBarPainter != null) {
            this.nodeBarPainter.addPainterListener(this);
        }
        calibrated = false;
        repaint();
    }

    public NodeBarPainter getNodeBarPainter() {
        return nodeBarPainter;
    }


    public void setNodeShapePainter(NodeShapePainter nodeShapePainter) {
        nodeShapePainter.setTreePane(this);
        if (this.nodeShapePainter != null) {
            this.nodeShapePainter.removePainterListener(this);
        }
        this.nodeShapePainter = nodeShapePainter;
        if (this.nodeShapePainter != null) {
            this.nodeShapePainter.addPainterListener(this);
        }
        calibrated = false;
        repaint();
    }

    public NodeShapePainter getNodeShapePainter() {
        return nodeShapePainter;
    }

    public void addScalePainter(ScalePainter scalePainter) {
        assert scalePainter != null;

        scalePainter.setTreePane(this);
        scalePainter.addPainterListener(this);

        scalePainters.add(scalePainter);

        calibrated = false;
        repaint();
    }

    public void removeScalePainter(ScalePainter scalePainter) {
        assert scalePainter != null;

        scalePainter.removePainterListener(this);

        scalePainters.remove(scalePainter);

        calibrated = false;
        repaint();
    }

    public void setScaleGridPainter(ScaleGridPainter scaleGridPainter) {
        scaleGridPainter.setTreePane(this);
        if (this.scaleGridPainter != null) {
            this.scaleGridPainter.removePainterListener(this);
        }
        this.scaleGridPainter = scaleGridPainter;
        if (this.scaleGridPainter != null) {
            this.scaleGridPainter.addPainterListener(this);
        }
        calibrated = false;
        repaint();
    }

    public void setLegendPainter(LegendPainter legendPainter) {
        legendPainter.setTreePane(this);
        legendPainter.addPainterListener(this);

        this.legendPainter = legendPainter;

        calibrated = false;
        repaint();
    }

    public LegendPainter getLegendPainter() {
        return legendPainter;
    }

    public void setPreferredSize(Dimension dimension) {
        if (treeLayout.maintainAspectRatio()) {
            super.setPreferredSize(new Dimension(dimension.width, dimension.height));
        } else {
            super.setPreferredSize(dimension);
        }

        calibrated = false;
    }

    public double getHeightAt(Graphics2D graphics2D, Point2D point) {
        try {
            point = transform.inverseTransform(point, null);
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
        return treeLayout.getHeightOfPoint(point);
    }

    public Node getNodeAt(Graphics2D g2, Point point) {
        Rectangle rect = new Rectangle(point.x - 1, point.y - 1, 3, 3);

        for (Node node : tree.getExternalNodes()) {
            Shape taxonLabelBound = tipLabelBounds.get(node);

            if (taxonLabelBound != null && g2.hit(rect, taxonLabelBound, false)) {
                return node;
            }
        }

        if (transform == null) return null;

        for (Node node : tree.getNodes()) {
            Shape branchPath = transform.createTransformedShape(treeLayoutCache.getBranchPath(node));
            if (branchPath != null && g2.hit(rect, branchPath, true)) {
                return node;
            }
            Shape collapsedShape = transform.createTransformedShape(treeLayoutCache.getCollapsedShape(node));
            if (collapsedShape != null && g2.hit(rect, collapsedShape, false)) {
                return node;
            }
        }

        return null;
    }

    public Set<Node> getNodesAt(Graphics2D g2, Rectangle rect) {

        Set<Node> nodes = new HashSet<Node>();
        for (Node node : tree.getExternalNodes()) {
            Shape taxonLabelBound = tipLabelBounds.get(node);
            if (taxonLabelBound != null && g2.hit(rect, taxonLabelBound, false)) {
                nodes.add(node);
            }
        }

        for (Node node : tree.getNodes()) {
            Shape branchPath = transform.createTransformedShape(treeLayoutCache.getBranchPath(node));
            if (branchPath != null && g2.hit(rect, branchPath, true)) {
                nodes.add(node);
            }
            Shape collapsedShape = transform.createTransformedShape(treeLayoutCache.getCollapsedShape(node));
            if (collapsedShape != null && g2.hit(rect, collapsedShape, false)) {
                nodes.add(node);
            }
        }

        return nodes;
    }

    public Set<Node> getSelectedNodes() {
        return selectedNodes;
    }

    public Set<Node> getSelectedTips() {
        return selectedTips;
    }

    public RootedTree getSelectedSubtree() {
        if (selectedNodes.size() == 0 && selectedTips.size() == 0) {
            // nothing selected so return the whole tree
            return tree;
        }

        SimpleRootedTree newTree = new SimpleRootedTree();

        getSelectedSubtree(newTree, this.tree.getRootNode(), false);

        if (newTree.getRootNode() == null) {
            // no tree was constructed, most likely because only one tip was selected
            return null;
        }

        return newTree;
    }

    /**
     * Returns a new node structure within newTree that contains the subtree subtended by selected
     * nodes of the current tree.
     * @param newTree
     * @param node
     * @param isSelected
     * @return
     */
    private Node getSelectedSubtree(SimpleRootedTree newTree, Node node, boolean isSelected) {
        Node newNode;

        if (tree.isExternal(node)) {
            if (isSelected || selectedNodes.contains(node) || selectedTips.contains(node)) {
                newNode = newTree.createExternalNode(tree.getTaxon(node));
                newTree.setHeight(newNode, tree.getHeight(node));
                for (String key : node.getAttributeNames()) {
                    newNode.setAttribute(key, node.getAttribute(key));
                }
            } else {
                newNode = null;
            }
        } else {
            List<Node> children = new ArrayList<Node>();

            for (Node child : tree.getChildren(node)) {
                Node subtree = getSelectedSubtree(newTree, child, isSelected);
                if (subtree != null) {
                    children.add(subtree);
                }
            }

            if (children.size() == 0) {
                if (selectedNodes.contains(node)) {
                    // if this node was selected but none of its children then include the entire
                    // descendent clade...
                    newNode = getSelectedSubtree(newTree, node, true);
                } else {
                    newNode = null;
                }
            } else if (children.size() == 1) {
                // just one child so pass it up...
                newNode = children.get(0);
            } else {
                newNode = newTree.createInternalNode(children);
                newTree.setHeight(newNode, tree.getHeight(node));
                for (String key : node.getAttributeNames()) {
                    newNode.setAttribute(key, node.getAttribute(key));
                }
            }
        }

        return newNode;
    }

    public Rectangle2D getDragRectangle() {
        return dragRectangle;
    }

    public void setDragRectangle(Rectangle2D dragRectangle) {
        this.dragRectangle = dragRectangle;
        repaint();
    }

    public void setRuler(double rulerHeight) {
        this.rulerHeight = rulerHeight;
    }

    public void scrollPointToVisible(Point point) {
        scrollRectToVisible(new Rectangle(point.x, point.y, 0, 0));
    }


    private final Set<TreeSelectionListener> treeSelectionListeners = new HashSet<TreeSelectionListener>();

    public void addTreeSelectionListener(TreeSelectionListener treeSelectionListener) {
        treeSelectionListeners.add(treeSelectionListener);
    }

    public void removeTreeSelectionListener(TreeSelectionListener treeSelectionListener) {
        treeSelectionListeners.remove(treeSelectionListener);
    }

    private void fireSelectionChanged() {
        for (TreeSelectionListener treeSelectionListener : treeSelectionListeners) {
            treeSelectionListener.selectionChanged();
        }
    }

    private final Set<TreePaneListener> treePaneListeners = new HashSet<TreePaneListener>();

    public void addTreePaneListener(TreePaneListener treePaneListener) {
        treePaneListeners.add(treePaneListener);
    }

    public void removeTreePaneListener(TreePaneListener treePaneListener) {
        treePaneListeners.remove(treePaneListener);
    }

    private void fireSettingsChanged() {
        for (TreePaneListener treePaneListener : treePaneListeners) {
            treePaneListener.treePaneSettingsChanged();
        }
    }

    public void paint(Graphics graphics) {
        if (tree == null) return;

        graphics.setColor(Color.white);
        Rectangle r = graphics.getClipBounds();
        if (r != null) {
            graphics.fillRect(r.x,  r.y, r.width, r.height);
        }

        final Graphics2D g2 = (Graphics2D) graphics;
        if (!calibrated) {
            calibrate(g2, getWidth(), getHeight());
        }

        drawTree(g2, getWidth(), getHeight());

        Paint oldPaint = g2.getPaint();
        Stroke oldStroke = g2.getStroke();

//		if (isCrosshairShown && cursorPosition != null && dragRectangle == null) {
//			g2.setPaint(cursorPaint);
//			g2.setStroke(cursorStroke);
//			double x = Math.max(treeBounds.getX(),
//					Math.min(cursorPosition.getX(), treeBounds.getX() + treeBounds.getWidth()));
//			double y = Math.max(treeBounds.getY(),
//					Math.min(cursorPosition.getY(), treeBounds.getY() + treeBounds.getHeight()));
//
//			g2.draw(new Line2D.Double(0.0, y, getWidth(), y));
//			g2.draw(new Line2D.Double(x, 0.0, x, getHeight()));
//
//		}

        if (branchSelection == null) {
            branchSelection = new GeneralPath();
            for (Node selectedNode : selectedNodes) {
                Shape branchPath = treeLayoutCache.getBranchPath(selectedNode);
                if (branchPath != null) {
                    Shape transPath = transform.createTransformedShape(branchPath);
                    branchSelection.append(transPath, false);

                }
                Shape collapsedShape = treeLayoutCache.getCollapsedShape(selectedNode);
                if (collapsedShape != null) {
                    Shape transPath = transform.createTransformedShape(collapsedShape);
                    branchSelection.append(transPath, false);
                }
            }
        }

        if (labelSelection == null) {
            labelSelection = new GeneralPath();
            for (Node selectedTip : selectedTips) {
                Shape labelBounds = tipLabelBounds.get(selectedTip);
                if (labelBounds != null) {
                    labelSelection.append(labelBounds, false);
                }
            }
        }

        g2.setPaint(selectionPaint);
        g2.setStroke(selectionStroke);
        g2.draw(branchSelection);
        g2.fill(labelSelection);

        g2.setPaint(oldPaint);
        g2.setStroke(oldStroke);

        if (dragRectangle != null) {
            g2.setPaint(new Color(128, 128, 128, 128));
            g2.fill(dragRectangle);
        }
    }

    private void clearSelectionPaths() {
        branchSelection = null;
        labelSelection = null;
    }

    private GeneralPath branchSelection = null;
    private GeneralPath labelSelection = null;

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

        if (tree == null || pageIndex > 0) return NO_SUCH_PAGE;

        Graphics2D g2 = (Graphics2D) graphics;
        g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        calibrated = false;
        setDoubleBuffered(false);

        drawTree(g2, pageFormat.getImageableWidth(), pageFormat.getImageableHeight());

        setDoubleBuffered(true);
        calibrated = false;

        return PAGE_EXISTS;
    }

    public void drawTree(Graphics2D g2, double width, double height) {

        final RenderingHints rhints = g2.getRenderingHints();
        final boolean antialiasOn = rhints.containsValue(RenderingHints.VALUE_ANTIALIAS_ON);
        if( ! antialiasOn ) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        if (!calibrated) {
            calibrate(g2, width, height);
        }

        // save graphics state which draw changes so that upon exit it can be restored

        final AffineTransform oldTransform = g2.getTransform();
        final Paint oldPaint = g2.getPaint();
        final Stroke oldStroke = g2.getStroke();
        final Font oldFont = g2.getFont();

        if (legendPainter != null && legendPainter.isVisible()) {
            legendPainter.paint(g2, this, Painter.Justification.CENTER, legendBounds);
        }

        // Paint scales
        for (ScalePainter scalePainter : scalePainters) {

            if (scalePainter.isVisible()) {
                Rectangle2D scaleBounds = this.scaleBounds.get(scalePainter);
                scalePainter.paint(g2, this, Painter.Justification.CENTER, scaleBounds);
            }
        }

        // Paint backgrounds
        if (nodeBackgroundDecorator != null) {
            for (Node node : treeLayoutCache.getNodeAreaMap().keySet() ) {
                Shape nodeArea = treeLayoutCache.getNodeArea(node);
                if (nodeArea != null) {
                    nodeBackgroundDecorator.setItem(node);
                    Shape transNodePath = transform.createTransformedShape(nodeArea);
                    Paint background = new Color(0,0,0,0);
                    background = nodeBackgroundDecorator.getPaint(background);
                    g2.setPaint(background);
                    g2.fill(transNodePath);

//                  Experimental outlining - requires order of drawing to be pre-order 
//                    g2.setStroke(new BasicStroke(8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
//                    g2.draw(transNodePath);
                }
            }
        }

        // Paint hilighted nodes
        for (Node node : treeLayoutCache.getHilightNodesList() ) {
            Object[] values = (Object[])node.getAttribute(HILIGHT_ATTRIBUTE_NAME);

            Shape hilightShape = treeLayoutCache.getHilightShape(node);

            Shape transShape = transform.createTransformedShape(hilightShape);
            Paint paint = ((Color)values[2]).darker();
            Paint fillPaint = (Color)values[2];
            Stroke stroke = new BasicStroke(0.5F);

            if (hilightingGradient && fillPaint != null) {
                fillPaint = new GradientPaint(
                        (float)transShape.getBounds2D().getMinX(), 0.0F, Color.WHITE,
                        (float)transShape.getBounds2D().getMaxX(), 0.0F, (Color)values[2], false);
                g2.setPaint(fillPaint);
                g2.fill(transShape);
            } else {

                if (fillPaint != null) {
                    g2.setPaint(fillPaint);
                    g2.fill(transShape);
                }

                if (paint != null) {
                    g2.setPaint(paint);
                    g2.setStroke(stroke);
                    g2.draw(transShape);
                }
            }
        }

        if (scaleGridPainter != null && scaleGridPainter.isVisible()) {
            Rectangle2D gridBounds = new Rectangle2D.Double(
                    treeBounds.getX(), 0.0,
                    treeBounds.getWidth(), totalScaleBounds.getY());
            scaleGridPainter.paint(g2, this, null, gridBounds);
        }

        if (DEBUG_OUTLINE) {
            g2.setPaint(Color.red);
            g2.draw(treeBounds);
        }

        // Paint collapsed nodes
        for (Node node : treeLayoutCache.getCollapsedShapeMap().keySet() ) {
            Shape collapsedShape = treeLayoutCache.getCollapsedShape(node);

            Shape transShape = transform.createTransformedShape(collapsedShape);
            Paint paint = Color.BLACK;
            Paint fillPaint = null;
            Stroke stroke = branchLineStroke;

            if (branchDecorator != null) {
                branchDecorator.setItem(node);
                paint = branchDecorator.getPaint(paint);
                fillPaint = branchDecorator.getFillPaint(fillPaint);
                stroke = branchDecorator.getStroke(stroke);
            }

            if (fillPaint != null) {
                g2.setPaint(fillPaint);
                g2.fill(transShape);
            }

            g2.setPaint(paint);
            g2.setStroke(stroke);
            g2.draw(transShape);
        }

        // Paint branches
        for (Node node : treeLayoutCache.getBranchPathMap().keySet() ) {
            Stroke stroke = branchLineStroke;
            if (branchDecorator != null) {
                branchDecorator.setItem(node);
                stroke = branchDecorator.getStroke(stroke);
            }
            g2.setStroke(stroke);

            Object[] branchColouring = null;
            if (treeLayout.isShowingColouring() && branchColouringAttribute != null) {
                branchColouring = (Object[])node.getAttribute(branchColouringAttribute);
            }

            Shape branchPath = treeLayoutCache.getBranchPath(node);

            if (branchColouring != null) {
                PathIterator iter = branchPath.getPathIterator(transform);

                float[] coords1 = new float[2];
                iter.currentSegment(coords1);

                for (int i = 0; i < branchColouring.length - 1; i+=2) {
                    iter.next();
                    float[] coords2 = new float[2];
                    iter.currentSegment(coords2);

                    int colour = ((Number)branchColouring[i]).intValue();
                    branchColouringDecorator.setItem(colour);
                    g2.setPaint(branchColouringDecorator.getPaint(Color.BLACK));
                    g2.draw(new Line2D.Float(coords1[0], coords1[1], coords2[0], coords2[1]));

                    coords1 = coords2;
                }

                // Draw the remaining branch as a path so it has proper line joins...
                int colour = ((Number)branchColouring[branchColouring.length - 1]).intValue();
                branchColouringDecorator.setItem(colour);
                g2.setPaint(branchColouringDecorator.getPaint(Color.BLACK));

                // Append the rest of the PathIterator to this new path...
                GeneralPath path = new GeneralPath();
                path.moveTo(coords1[0], coords1[1]);
                path.append(iter, true);

                // and draw it...
                g2.draw(path);

            } else {
                Shape transPath = transform.createTransformedShape(branchPath);
                Paint paint = Color.BLACK;
                if (branchDecorator != null) {
                    if (branchDecoratorGradient && branchDecorator.allowsGradient()) {
                        branchDecorator.setItems(node, tree.getParent(node));
                        PathIterator iter = transPath.getPathIterator(null);
                        double[] coords = new double[6];
                        iter.currentSegment(coords);
                        Point2D point1 = new Point2D.Double(coords[0], coords[1]);
                        do {
                            iter.currentSegment(coords);
                            iter.next();
                        } while (!iter.isDone());
                        Point2D point2 = new Point2D.Double(coords[0], coords[1]);

                        paint = branchDecorator.getPaint(paint, point1, point2);
                    } else {
                        branchDecorator.setItem(node);
                        paint = branchDecorator.getPaint(paint);
                    }
                }
                g2.setPaint(paint);
                g2.draw(transPath);
            }
        }

        // Paint node bars
        if (!isTransformBranchesOn() && nodeBarPainter != null && nodeBarPainter.isVisible()) {
            for (Node node : nodeBars.keySet() ) {
                Shape nodeBar = nodeBars.get(node);
                nodeBar = transform.createTransformedShape(nodeBar);
                nodeBarPainter.paint(g2, node, NodePainter.Justification.CENTER, nodeBar);
            }
        }

        // Paint node shapes
        if (nodeShapePainter != null && nodeShapePainter.isVisible()) {
            for (Node node : nodePoints.keySet()) {
                Point2D point = nodePoints.get(node);
                point = transform.transform(point, null);
                nodeShapePainter.paint(g2, node, point, nodeShapeTransforms.get(node));
            }
        }

        // Paint tip labels
        if (tipLabelPainter != null && tipLabelPainter.isVisible()) {

            for (Node node : tipLabelTransforms.keySet()) {

                AffineTransform tipLabelTransform = tipLabelTransforms.get(node);

                Painter.Justification tipLabelJustification = tipLabelJustifications.get(node);
                g2.transform(tipLabelTransform);

                tipLabelPainter.paint(g2, node, tipLabelJustification,
                        new Rectangle2D.Double(0.0, 0.0, tipLabelWidth, tipLabelPainter.getPreferredHeight()));

                g2.setTransform(oldTransform);

                if (showingTipCallouts) {
                    Shape calloutPath = transform.createTransformedShape(treeLayoutCache.getCalloutPath(node));
                    if (calloutPath != null) {
                        g2.setStroke(calloutStroke);
                        g2.draw(calloutPath);
                    }
                }
            }
        }

        // Paint node labels
        if (nodeLabelPainter != null && nodeLabelPainter.isVisible()) {
            for (Node node : nodeLabelTransforms.keySet() ) {

                AffineTransform nodeTransform = nodeLabelTransforms.get(node);

                Painter.Justification nodeLabelJustification = nodeLabelJustifications.get(node);
                g2.transform(nodeTransform);

                nodeLabelPainter.paint(g2, node, nodeLabelJustification,
                        new Rectangle2D.Double(0.0, 0.0, nodeLabelPainter.getPreferredWidth(), nodeLabelPainter.getPreferredHeight()));

                g2.setTransform(oldTransform);
            }
        }

        // Paint branch labels
        if (branchLabelPainter != null && branchLabelPainter.isVisible()) {

            for (Node node : branchLabelTransforms.keySet() ) {

                AffineTransform branchTransform = branchLabelTransforms.get(node);

                g2.transform(branchTransform);

                branchLabelPainter.calibrate(g2, node);
                final double preferredWidth = branchLabelPainter.getPreferredWidth();
                final double preferredHeight = branchLabelPainter.getPreferredHeight();

                //Line2D labelPath = treeLayout.getBranchLabelPath(node);

                branchLabelPainter.paint(g2, node, Painter.Justification.CENTER,
                        //new Rectangle2D.Double(-preferredWidth/2, -preferredHeight, preferredWidth, preferredHeight));
                        new Rectangle2D.Double(0, 0, preferredWidth, preferredHeight));

                g2.setTransform(oldTransform);
            }
        }

        g2.setStroke(oldStroke);
        g2.setPaint(oldPaint);
        g2.setFont(oldFont);
    }

    private void calibrate(Graphics2D g2, double width, double height) {

        // First layout the tree
        treeLayout.layout(tree, treeLayoutCache);

        // Now rescale the scale axis
        setupScaleAxis();

        // First of all get the bounds for the unscaled tree
        treeBounds = null;

        // There are two sets of bounds here. The treeBounds are the bounds of the elements
        // that make up the actual tree. These are scaled from branch length space

        // The bounds are then the extra stuff that doesn't get scaled with the tree such
        // as labels and the like.

        // bounds on branches
        for (Shape branchPath : treeLayoutCache.getBranchPathMap().values()) {
            // Add the bounds of the branch path to the overall bounds
            final Rectangle2D branchBounds = branchPath.getBounds2D();
            if (treeBounds == null) {
                treeBounds = branchBounds;
            } else {
                treeBounds.add(branchBounds);
            }
        }

        // Iterate though the callout paths
        for (Shape calloutPath : treeLayoutCache.getCalloutPathMap().values()) {
            // Get the line that represents the path for the taxon label
            // and add the translated bounds to the overall bounds
            final Rectangle2D calloutBounds = calloutPath.getBounds2D();
            treeBounds.add(calloutBounds);
        }


        for (Shape collapsedShape : treeLayoutCache.getCollapsedShapeMap().values()) {
            // Add the bounds of the branch path to the overall bounds
            final Rectangle2D branchBounds = collapsedShape.getBounds2D();
            if (treeBounds == null) {
                treeBounds = branchBounds;
            } else {
                treeBounds.add(branchBounds);
            }
        }

        for (Shape hilightShape : treeLayoutCache.getHilightShapeMap().values()) {
            // Add the bounds of the branch path to the overall bounds
            final Rectangle2D branchBounds = hilightShape.getBounds2D();
            if (treeBounds == null) {
                treeBounds = branchBounds;
            } else {
                treeBounds.add(branchBounds);
            }
        }

        // bounds on node bars
        if (!isTransformBranchesOn() && nodeBarPainter != null && nodeBarPainter.isVisible()) {
            nodeBars.clear();
            // Iterate though the nodes
            for (Node node : tree.getInternalNodes()) {

                Rectangle2D shapeBounds = nodeBarPainter.calibrate(g2, node);
                if (shapeBounds != null) {
                    treeBounds.add(shapeBounds);
                    nodeBars.put(node, nodeBarPainter.getNodeBar());
                }
            }
        }

        // totalTreeBounds includes all the stuff which is not in a tree scale (like labels and shapes) but in
        // screen pixel scale. This is added to the treeBounds to make space round the edge.

        // add the tree bounds
        final Rectangle2D totalTreeBounds = treeBounds.getBounds2D(); // (YH) same as (Rectangle2D) treeBounds.clone();

        final Set<Node> externalNodes = tree.getExternalNodes();

        if (tipLabelPainter != null && tipLabelPainter.isVisible()) {

            tipLabelWidth = 0.0;
            final double tipLabelHeight = tipLabelPainter.getPreferredHeight();

            // put this in a recursive function to allow for collapsed node labels
            calibrateTipLabels(g2, tree.getRootNode(), tipLabelHeight, totalTreeBounds);
        }

        if (nodeLabelPainter != null && nodeLabelPainter.isVisible()) {
            // Iterate though the nodes with node labels
            for (Node node : treeLayoutCache.getNodeLabelPathMap().keySet()) {
                // Get the line that represents the path for the taxon label
                final Line2D labelPath = treeLayoutCache.getNodeLabelPath(node);

                nodeLabelPainter.calibrate(g2, node);
                final double labelHeight = nodeLabelPainter.getPreferredHeight();
                final double labelWidth = nodeLabelPainter.getPreferredWidth();
                Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, labelWidth, labelHeight);

                // Work out how it is rotated and create a transform that matches that
                AffineTransform labelTransform = calculateTransform(null, labelPath, labelWidth, labelHeight, true);

                // and add the translated bounds to the overall bounds
                totalTreeBounds.add(labelTransform.createTransformedShape(labelBounds).getBounds2D());
            }
        }

        if (branchLabelPainter != null && branchLabelPainter.isVisible()) {
            // Iterate though the nodes with branch labels
            for (Node node : treeLayoutCache.getBranchLabelPathMap().keySet()) {
                // Get the line that represents the path for the branch label
                final Line2D labelPath = treeLayoutCache.getBranchLabelPath(node);

                branchLabelPainter.calibrate(g2, node);
                final double labelHeight = branchLabelPainter.getHeightBound();
                final double labelWidth = branchLabelPainter.getPreferredWidth();

                Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, labelWidth, labelHeight);

                // Work out how it is rotated and create a transform that matches that
                AffineTransform labelTransform = calculateTransform(null, labelPath, labelWidth, labelHeight, false);

                // and add the translated bounds to the overall bounds
                totalTreeBounds.add(labelTransform.createTransformedShape(labelBounds).getBounds2D());
            }
        }

        // bounds on nodeShapes
        if (nodeShapePainter != null && nodeShapePainter.isVisible()) {
            nodePoints.clear();
            // Iterate though the nodes
            for (Node node : tree.getNodes()) {

                Rectangle2D shapeBounds = nodeShapePainter.calibrate(g2, node);
                if (shapeBounds != null) {
                    totalTreeBounds.add(shapeBounds);

                    // just at the centroid in here as the actual shape will be reconstructed when drawing
                    nodePoints.put(node, new Point2D.Double(shapeBounds.getCenterX(), shapeBounds.getCenterY()));
                }
            }
        }

        totalScaleBounds = new Rectangle2D.Double(0.0, 0.0, 0.0, 0.0);
        double y = totalTreeBounds.getY() + totalTreeBounds.getHeight();
        for (ScalePainter scalePainter : scalePainters) {
            if (scalePainter.isVisible()) {
                scalePainter.calibrate(g2, this);
                Rectangle2D sb = new Rectangle2D.Double(
                        treeBounds.getX(), y,
                        treeBounds.getWidth(), scalePainter.getPreferredHeight());
                y += sb.getHeight();
                totalScaleBounds.add(sb);
                scaleBounds.put(scalePainter, sb);
            }
        }
        totalTreeBounds.add(totalScaleBounds);

        if (legendPainter != null && legendPainter.isVisible()) {
            legendPainter.calibrate(g2, this);
            final double w2 = legendPainter.getPreferredWidth();
            legendBounds = new Rectangle2D.Double(-w2, 0, w2, treeBounds.getHeight());
            totalTreeBounds.add(legendBounds);
        }

        final double availableW = width - insets.left - insets.right;
        final double availableH = height - insets.top - insets.bottom;

        // get the difference between the tree's bounds and the overall bounds
        boolean maintainAspectRatio = treeLayout.maintainAspectRatio();

        double xDiff;
        double yDiff;

        if (maintainAspectRatio) {
            double topDiff = treeBounds.getY() - totalTreeBounds.getY();
            double leftDiff = treeBounds.getX() - totalTreeBounds.getX();
            double bottomDiff = (totalTreeBounds.getHeight() + totalTreeBounds.getY()) -
                    (treeBounds.getHeight() + treeBounds.getY());
            double rightDiff = (totalTreeBounds.getWidth() + totalTreeBounds.getX()) -
                    (treeBounds.getWidth() + treeBounds.getX());
            assert topDiff >= 0 && leftDiff >= 0 && bottomDiff >= 0 && rightDiff >= 0;

            xDiff = 2.0 * (leftDiff > rightDiff ? leftDiff : rightDiff);
            yDiff = 2.0 * (topDiff > bottomDiff ? topDiff : bottomDiff);
        } else {
            xDiff = totalTreeBounds.getWidth() - treeBounds.getWidth();
            yDiff = totalTreeBounds.getHeight() - treeBounds.getHeight();
            assert xDiff >= 0 && yDiff >= 0;
        }


        // small tree, long labels, label bounds may get larger that window, protect against that

        if( xDiff >= availableW ) {
            xDiff = Math.min(availableW, totalTreeBounds.getWidth()) - treeBounds.getWidth();
        }

        if( yDiff >= availableH ) {
            yDiff = Math.min(availableH, totalTreeBounds.getHeight()) - treeBounds.getHeight();
        }

        // Get the amount of canvas that is going to be taken up by the tree -
        // The rest is taken up by taxon labels which don't scale

        final double w = availableW - xDiff;
        final double h = availableH - yDiff;

        double xScale;
        double yScale;

        double xOffset = 0.0;
        double yOffset = 0.0;

        if (maintainAspectRatio) {
            // If the tree is laid out in both dimensions then we
            // need to find out which axis has the least space and scale
            // the tree to that (to keep the aspect ratio.

            if ((w / treeBounds.getWidth()) < (h / treeBounds.getHeight())) {
                xScale = w / treeBounds.getWidth();
                yScale = xScale;
            } else {
                yScale = h / treeBounds.getHeight();
                xScale = yScale;
            }

            treeScale = xScale;   assert treeScale > 0;

            // and set the origin so that the center of the tree is in
            // the center of the canvas
            xOffset = ((width - (treeBounds.getWidth() * xScale)) / 2) - (treeBounds.getX() * xScale);
            yOffset = ((height - (treeBounds.getHeight() * yScale)) / 2) - (treeBounds.getY() * yScale);

        } else {
            // Otherwise just scale both dimensions
            xScale = w / treeBounds.getWidth();
            yScale = h / treeBounds.getHeight();

            // and set the origin in the top left corner
            xOffset = - totalTreeBounds.getX() + (treeBounds.getX() * xScale);
            yOffset = - totalTreeBounds.getY() - (treeBounds.getY() * yScale);

            treeScale = xScale;
        }

        assert treeScale > 0;

        // Create the overall transform
        transform = new AffineTransform();
        transform.translate(xOffset + insets.left, yOffset + insets.top);
        transform.scale(xScale, yScale);

        // Get the bounds for the newly scaled tree
        treeBounds = null;

        // bounds on branches
        for (Shape branchPath : treeLayoutCache.getBranchPathMap().values()) {
            // Add the bounds of the branch path to the overall bounds
            final Rectangle2D branchBounds = transform.createTransformedShape(branchPath).getBounds2D();
            if (treeBounds == null) {
                treeBounds = branchBounds;
            } else {
                treeBounds.add(branchBounds);
            }
        }

        for (Shape collapsedShape : treeLayoutCache.getCollapsedShapeMap().values()) {
            // Add the bounds of the branch path to the overall bounds
            final Rectangle2D branchBounds = transform.createTransformedShape(collapsedShape).getBounds2D();
            if (treeBounds == null) {
                treeBounds = branchBounds;
            } else {
                treeBounds.add(branchBounds);
            }
        }

        // Clear the map of individual taxon label bounds and transforms
        tipLabelBounds.clear();
        tipLabelTransforms.clear();
        tipLabelJustifications.clear();

        if (tipLabelPainter != null && tipLabelPainter.isVisible()) {
            final double labelHeight = tipLabelPainter.getPreferredHeight();
            Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, tipLabelWidth, labelHeight);

            // Iterate though the external nodes with tip labels
            for (Node node : treeLayoutCache.getTipLabelPathMap().keySet()) {
                // Get the line that represents the path for the tip label
                Line2D tipPath = treeLayoutCache.getTipLabelPath(node);

                // Work out how it is rotated and create a transform that matches that
                AffineTransform taxonTransform = calculateTransform(transform, tipPath, tipLabelWidth, labelHeight, true);

                // Store the transformed bounds in the map for use when selecting
                tipLabelBounds.put(node, taxonTransform.createTransformedShape(labelBounds));

                // Store the transform in the map for use when drawing
                tipLabelTransforms.put(node, taxonTransform);

                // Store the alignment in the map for use when drawing
                final Painter.Justification just = (tipPath.getX1() < tipPath.getX2()) ?
                        Painter.Justification.LEFT : Painter.Justification.RIGHT;
                tipLabelJustifications.put(node, just);
            }
        }

        // Clear the map of individual node label bounds and transforms
        nodeLabelBounds.clear();
        nodeLabelTransforms.clear();
        nodeLabelJustifications.clear();

        if (nodeLabelPainter != null && nodeLabelPainter.isVisible()) {
            final double labelHeight = nodeLabelPainter.getPreferredHeight();
            final double labelWidth = nodeLabelPainter.getPreferredWidth();
            final Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, labelWidth, labelHeight);

            // Iterate though the external nodes with node labels
            for (Node node : treeLayoutCache.getNodeLabelPathMap().keySet()) {
                // Get the line that represents the path for the node label
                final Line2D labelPath = treeLayoutCache.getNodeLabelPath(node);

                // Work out how it is rotated and create a transform that matches that
                AffineTransform labelTransform = calculateTransform(transform, labelPath, labelWidth, labelHeight, true);

                // Store the transformed bounds in the map for use when selecting
                nodeLabelBounds.put(node, labelTransform.createTransformedShape(labelBounds));

                // Store the transform in the map for use when drawing
                nodeLabelTransforms.put(node, labelTransform);

                // Store the alignment in the map for use when drawing
                if (labelPath.getX1() < labelPath.getX2()) {
                    nodeLabelJustifications.put(node, Painter.Justification.LEFT);
                } else {
                    nodeLabelJustifications.put(node, Painter.Justification.RIGHT);
                }
            }
        }

        branchLabelBounds.clear();
        branchLabelTransforms.clear();
        branchLabelJustifications.clear();

        if (branchLabelPainter != null && branchLabelPainter.isVisible()) {

            // Iterate though the external nodes with branch labels
            for (Node node : treeLayoutCache.getBranchLabelPathMap().keySet()) {

                // Get the line that represents the path for the branch label
                Line2D labelPath = treeLayoutCache.getBranchLabelPath(node);

                final double labelHeight = branchLabelPainter.getPreferredHeight();
                final double labelWidth = branchLabelPainter.getPreferredWidth();
                final Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, labelWidth, labelHeight);

                final double dx = labelPath.getP2().getX() - labelPath.getP1().getX();
                final double dy = labelPath.getP2().getY() - labelPath.getP1().getY();
                final double branchLength = Math.sqrt(dx*dx + dy*dy);

                final Painter.Justification just = labelPath.getX1() < labelPath.getX2() ? Painter.Justification.LEFT :
                        Painter.Justification.RIGHT;

                // Work out how it is rotated and create a transform that matches that
                AffineTransform labelTransform = calculateTransform(transform, labelPath, labelWidth, labelHeight, false);
                // move to middle of branch - since the move is before the rotation
                final double direction = just == Painter.Justification.RIGHT ? 1 : -1;
                labelTransform.translate(-direction * xScale * branchLength /2, 0);

                // Store the transformed bounds in the map for use when selecting
                branchLabelBounds.put(node, labelTransform.createTransformedShape(labelBounds));

                // Store the transform in the map for use when drawing
                branchLabelTransforms.put(node, labelTransform);

                // Store the alignment in the map for use when drawing
                branchLabelJustifications.put(node, just);
            }
        }

        if (nodeShapePainter != null && nodeShapePainter.isVisible()) {
            nodeShapeTransforms.clear();
            // Iterate though the nodes
            for (Node node : nodePoints.keySet()) {
                Line2D shapePath = getTreeLayoutCache().getNodeShapePath(node);
                if (shapePath != null) {
                    nodeShapeTransforms.put(node, calculateTransform(transform, shapePath));
                }
            }
        }

        y = height;
        for (ScalePainter scalePainter : scalePainters) {
            if (scalePainter.isVisible()) {
                scalePainter.calibrate(g2, this);
                y -= scalePainter.getPreferredHeight();
            }
        }

        totalScaleBounds = new Rectangle2D.Double(treeBounds.getX(), y, treeBounds.getWidth(), 0.0);
        for (ScalePainter scalePainter : scalePainters) {
            if (scalePainter.isVisible()) {
                scalePainter.calibrate(g2, this);
                final double h1 = scalePainter.getPreferredHeight();
                Rectangle2D sb = new Rectangle2D.Double(treeBounds.getX(), y, treeBounds.getWidth(), h1);
                y += h1;
                totalScaleBounds.add(sb);
                scaleBounds.put(scalePainter, sb);
            }
        }

        if (legendPainter != null && legendPainter.isVisible()) {
            legendPainter.calibrate(g2, this);
            final double w2 = legendPainter.getPreferredWidth();
            legendBounds = new Rectangle2D.Double(0, 0, w2, treeBounds.getHeight());
        }

        calloutPaths.clear();
        clearSelectionPaths();

        calibrated = true;
    }

    private void calibrateTipLabels(final Graphics2D g2, final Node node, final double tipLabelHeight, final Rectangle2D totalTreeBounds) {

        if (tree.isExternal(node) || node.getAttribute(COLLAPSE_ATTRIBUTE_NAME) != null) {
            tipLabelPainter.calibrate(g2, node);
            double width = tipLabelPainter.getPreferredWidth();
            tipLabelWidth = Math.max(tipLabelWidth, width);

            Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, width, tipLabelHeight);

            // Get the line that represents the path for the taxon label
            Line2D taxonPath = treeLayoutCache.getTipLabelPath(node);

            if (taxonPath != null) {
                // Work out how it is rotated and create a transform that matches that
                AffineTransform taxonTransform = calculateTransform(null, taxonPath, tipLabelWidth, tipLabelHeight, true);

                // and add the translated bounds to the overall bounds
                totalTreeBounds.add(taxonTransform.createTransformedShape(labelBounds).getBounds2D());
            }
        } else {
            for (Node child : tree.getChildren(node)) {
                calibrateTipLabels(g2, child, tipLabelHeight, totalTreeBounds);
            }
        }
    }

    private AffineTransform calculateTransform(AffineTransform globalTransform, Line2D line,
                                               double width, double height, boolean just) {
        final Point2D origin = line.getP1();
        if (globalTransform != null) {
            globalTransform.transform(origin, origin);
        }

        // Work out how it is rotated and create a transform that matches that
        AffineTransform lineTransform = new AffineTransform();

        final double dy = line.getY2() - line.getY1();
        // efficency
        if( dy != 0.0 ) {
            final double dx = line.getX2() - line.getX1();
            final double angle = dx != 0.0 ? Math.atan(dy / dx) : 0.0;
            lineTransform.rotate(angle, origin.getX(), origin.getY());
        }

        // Now add a translate to the transform - if it is on the left then we need
        // to shift it by the entire width of the string.
        final double ty = origin.getY() - (height / 2.0);
        double tx = origin.getX();
        if( just) {
            if (line.getX2() > line.getX1()) {
                tx += labelXOffset;
            } else {
                tx -= (labelXOffset + width);
            }
        }
        lineTransform.translate(tx, ty);
        return lineTransform;
    }

    private AffineTransform calculateTransform(AffineTransform globalTransform, Line2D line) {
        final Point2D origin = line.getP1();
        if (globalTransform != null) {
            globalTransform.transform(origin, origin);
        }

        // Work out how it is rotated and create a transform that matches that
        AffineTransform lineTransform = new AffineTransform();

        final double dy = line.getY2() - line.getY1();
        // efficency
        if( dy != 0.0 ) {
            final double dx = line.getX2() - line.getX1();
            final double angle = dx != 0.0 ? Math.atan(dy / dx) : 0.0;
            lineTransform.rotate(angle, origin.getX(), origin.getY());
        }

        return lineTransform;
    }


    // Overridden methods to recalibrate tree when bounds change
    public void setBounds(int x, int y, int width, int height) {
        calibrated = false;
        super.setBounds(x, y, width, height);
    }

    public void setBounds(Rectangle rectangle) {
        calibrated = false;
        super.setBounds(rectangle);
    }

    public void setSize(Dimension dimension) {
        calibrated = false;
        super.setSize(dimension);
    }

    public void setSize(int width, int height) {
        calibrated = false;
        super.setSize(width, height);
    }

    private RootedTree originalTree = null;
    private RootedTree tree = null;
    private TreeLayout treeLayout = null;
    private TreeLayoutCache treeLayoutCache = new TreeLayoutCache();

    private boolean orderBranchesOn = false;
    private SortedRootedTree.BranchOrdering branchOrdering = SortedRootedTree.BranchOrdering.INCREASING_NODE_DENSITY;

    private boolean transformBranchesOn = false;
    private TransformedRootedTree.Transform branchTransform = TransformedRootedTree.Transform.CLADOGRAM;

    private boolean isRootingOn = false;
    private RootingType rootingType = RootingType.USER_ROOTING;
    private Node rootingNode = null;
    private double rootingLength = 0.01;

    private Rectangle2D treeBounds = new Rectangle2D.Double();
    private double treeScale;

    private ScaleAxis scaleAxis = new ScaleAxis(ScaleAxis.AT_DATA, ScaleAxis.AT_DATA);
    private double axisOrigin = 0.0;
    private TimeScale timeScale = new TimeScale(1.0, 0.0);

    //private Insets insets = new Insets(0, 0, 0, 0);
    private Insets insets = new Insets(6, 6, 6, 6);

    private Set<Node> selectedNodes = new HashSet<Node>();
    private Set<Node> selectedTips = new HashSet<Node>();

    private double rulerHeight = -1.0;
    private Rectangle2D dragRectangle = null;
    private Point2D cursorPosition = null;

    private boolean isCrosshairShown = true;

    private Decorator branchDecorator = null;
    private Decorator branchColouringDecorator = null;
    private boolean branchDecoratorGradient = false;
    private String branchColouringAttribute = null;

    private boolean hilightingGradient = false;

    private Decorator nodeBackgroundDecorator = null;

    private float labelXOffset = 5.0F;
    private LabelPainter<Node> tipLabelPainter = null;
    private double tipLabelWidth;
    private LabelPainter<Node> nodeLabelPainter = null;
    private LabelPainter<Node> branchLabelPainter = null;

    private NodeBarPainter nodeBarPainter = null;

    private NodeShapePainter nodeShapePainter = null;

    private List<ScalePainter> scalePainters = new ArrayList<ScalePainter>();
    private Rectangle2D totalScaleBounds = null;
    private Map<ScalePainter, Rectangle2D> scaleBounds = new HashMap<ScalePainter, Rectangle2D>();

    private ScaleGridPainter scaleGridPainter = null;

    private LegendPainter legendPainter = null;
    private Rectangle2D legendBounds = new Rectangle2D.Double();


    private BasicStroke branchLineStroke = new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private BasicStroke calloutStroke = new BasicStroke(0.5F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{0.5f, 2.0f}, 0.0f);
    private Stroke selectionStroke = new BasicStroke(6.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private Paint selectionPaint;
    private Stroke cursorStroke = new BasicStroke(0.5F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private Paint cursorPaint = Color.DARK_GRAY;

    private boolean calibrated = false;
    private AffineTransform transform = null;

    private boolean showingTipCallouts = true;

    private Map<Node, AffineTransform> tipLabelTransforms = new HashMap<Node, AffineTransform>();
    private Map<Node, Shape> tipLabelBounds = new HashMap<Node, Shape>();
    private Map<Node, Painter.Justification> tipLabelJustifications = new HashMap<Node, Painter.Justification>();

    private Map<Node, AffineTransform> nodeLabelTransforms = new HashMap<Node, AffineTransform>();
    private Map<Node, Shape> nodeLabelBounds = new HashMap<Node, Shape>();
    private Map<Node, Painter.Justification> nodeLabelJustifications = new HashMap<Node, Painter.Justification>();

    private Map<Node, AffineTransform> branchLabelTransforms = new HashMap<Node, AffineTransform>();
    private Map<Node, Shape> branchLabelBounds = new HashMap<Node, Shape>();
    private Map<Node, Painter.Justification> branchLabelJustifications = new HashMap<Node, Painter.Justification>();

    private Map<Node, Shape> nodeBars = new HashMap<Node, Shape>();
    private Map<Node, Point2D> nodePoints = new HashMap<Node, Point2D>();
    private Map<Node, AffineTransform> nodeShapeTransforms = new HashMap<Node, AffineTransform>();

    private Map<Node, Shape> calloutPaths = new HashMap<Node, Shape>();

}