package figtree.treeviewer.treelayouts;

import jebl.evolution.graphs.Node;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id: TreeLayoutCache.java 535 2006-11-21 11:11:20Z rambaut $
 */
public class TreeLayoutCache {
    public Point2D getNodePoint(Node node) {
        return nodePoints.get(node);
    }

    public Map<Node, Point2D> getNodePointMap() {
        return nodePoints;
    }

    public Shape getBranchPath(Node node) {
        return branchPaths.get(node);
    }

    public Map<Node, Shape> getBranchPathMap() {
        return branchPaths;
    }

    public Shape getNodeArea(Node node) {
        return nodeAreas.get(node);
    }

    public Map<Node, Shape> getNodeAreaMap() {
        return nodeAreas;
    }

    public Shape getCollapsedShape(Node node) {
        return collapsedShapes.get(node);
    }

    public Map<Node, Shape> getCollapsedShapeMap() {
        return collapsedShapes;
    }

	public List<Node> getHilightNodesList() {
	    return hilightNodes;
	}

	public Shape getHilightShape(Node node) {
	    return hilightShapes.get(node);
	}

	public Map<Node, Shape> getHilightShapeMap() {
	    return hilightShapes;
	}

    public Line2D getTipLabelPath(Node node) {
        return tipLabelPaths.get(node);
    }

    public Map<Node, Line2D> getTipLabelPathMap() {
        return tipLabelPaths;
    }

    public Line2D getBranchLabelPath(Node node) {
        return branchLabelPaths.get(node);
    }

    public Map<Node, Line2D> getBranchLabelPathMap() {
        return branchLabelPaths;
    }

    public Line2D getNodeLabelPath(Node node) {
        return nodeLabelPaths.get(node);
    }

    public Map<Node, Line2D> getNodeLabelPathMap() {
        return nodeLabelPaths;
    }

    public Line2D getNodeShapePath(Node node) {
        return nodeShapePaths.get(node);
    }

    public Map<Node, Line2D> getNodeShapePathMap() {
        return nodeShapePaths;
    }

    public Shape getCalloutPath(Node node) {
        return calloutPaths.get(node);
    }

    public Map<Node, Shape> getCalloutPathMap() {
        return calloutPaths;
    }

	public void clear() {
		nodePoints.clear();
        branchPaths.clear();
        nodeAreas.clear();
        collapsedShapes.clear();
		hilightNodes.clear();
		hilightShapes.clear();
        tipLabelPaths.clear();
		branchLabelPaths.clear();
        nodeLabelPaths.clear();
        nodeShapePaths.clear();
        calloutPaths.clear();
}

    protected Map<Node, Point2D> nodePoints = new HashMap<Node, Point2D>();
    protected Map<Node, Shape> branchPaths = new HashMap<Node, Shape>();
    protected Map<Node, Shape> nodeAreas = new HashMap<Node, Shape>();
    protected Map<Node, Shape> collapsedShapes = new HashMap<Node, Shape>();
	protected List<Node> hilightNodes = new ArrayList<Node>();
	protected Map<Node, Shape> hilightShapes = new HashMap<Node, Shape>();
    protected Map<Node, Line2D> tipLabelPaths = new HashMap<Node, Line2D>();
    protected Map<Node, Line2D> branchLabelPaths = new HashMap<Node, Line2D>();
    protected Map<Node, Line2D> nodeLabelPaths = new HashMap<Node, Line2D>();
    protected Map<Node, Line2D> nodeShapePaths = new HashMap<Node, Line2D>();
    protected Map<Node, Shape> calloutPaths = new HashMap<Node, Shape>();
}
