package figtree.treeviewer.treelayouts;

import jebl.evolution.graphs.Graph;
import jebl.evolution.graphs.Node;
import jebl.evolution.trees.RootedTree;

import java.awt.*;
import java.awt.geom.*;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id: RadialTreeLayout.java 780 2007-09-09 09:46:01Z rambaut $
 */
public class RadialTreeLayout extends AbstractTreeLayout {

	private double spread = 0.0;

    public AxisType getXAxisType() {
        return AxisType.CONTINUOUS;
    }

    public AxisType getYAxisType() {
        return AxisType.CONTINUOUS;
    }

    public boolean isShowingRootBranch() {
        return false;
    }

	public double getTotalRootLength() {
		return 0.0;
	}

	public void setFishEye(double fishEye) {
		// do nothing
	}

	public void setPointOfInterest(double x, double y) {
		// do nothing
	}

    public boolean isShowingColouring() {
	    return (branchColouringAttribute != null);
    }

	public boolean maintainAspectRatio() {
        return true;
    }

    public double getHeightOfPoint(Point2D point) {
        throw new UnsupportedOperationException("Method getHeightOfPoint() is not supported in this TreeLayout");
    }

    public Line2D getAxisLine(double height) {
        throw new UnsupportedOperationException("Method getHeightLine() is not supported in this TreeLayout");
    }

    public Shape getHeightArea(double height1, double height2) {
        throw new UnsupportedOperationException("Method getHeightArea() is not supported in this TreeLayout");
    }

    public double getSpread() {
        return spread;
    }

    public void setSpread(double spread) {
        this.spread = spread;
        fireTreeLayoutChanged();
    }

	public void layout(RootedTree tree, TreeLayoutCache cache) {
        cache.clear();

        try {
            final Node root = tree.getRootNode();

            constructNode(tree, root, 0.0, Math.PI * 2, 0.0, 0.0, 0.0, cache);

            // Haven't been able to make these look nice....
//            constructNodeAreas(tree, root, new Area(), cache);

        } catch (Graph.NoEdgeException e) {
            e.printStackTrace();
        }
    }

    private Point2D constructNode(RootedTree tree, Node node,
                                  double angleStart, double angleFinish, double xPosition,
                                  double yPosition, double length,
                                  TreeLayoutCache cache) throws Graph.NoEdgeException {

        final double branchAngle = (angleStart + angleFinish) / 2.0;

        final double directionX = Math.cos(branchAngle);
        final double directionY = Math.sin(branchAngle);
        Point2D nodePoint = new Point2D.Double(xPosition + (length * directionX), yPosition + (length * directionY));

        // System.out.println("Node: " + Utils.DEBUGsubTreeRep(tree, node) + " at " + nodePoint);

        if (!tree.isExternal(node)) {

// Not too clear how to do hilighting for radial trees so leave it out...
//            if (hilightAttributeName != null && node.getAttribute(hilightAttributeName) != null) {
//                constructHilight(tree, node, angleStart, angleFinish, xPosition, yPosition, length, cache);
//            }

            List<Node> children = tree.getChildren(node);
            int[] leafCounts = new int[children.size()];
            int sumLeafCount = 0;

            int i = 0;
            for (Node child : children) {
                leafCounts[i] = jebl.evolution.trees.Utils.getExternalNodeCount(tree, child);
                sumLeafCount += leafCounts[i];
                i++;
            }

            double span = (angleFinish - angleStart);

            if (!tree.isRoot(node)) {
                span *= 1.0 + (spread / 10.0);
                angleStart = branchAngle - (span / 2.0);
                angleFinish = branchAngle + (span / 2.0);
            }

            double a2 = angleStart;

	        boolean rotate = false;
	        if (node.getAttribute("!rotate") != null &&
			        ((Boolean)node.getAttribute("!rotate"))) {
		        rotate = true;
	        }
            for(i = 0; i < children.size(); ++i) {
	            int index = i;
	            if (rotate) {
		            index = children.size() - i - 1;
	            }

	            Node child = children.get(index);

                final double childLength = tree.getLength(child);
                double a1 = a2;
                a2 = a1 + (span * leafCounts[index] / sumLeafCount);

                Point2D childPoint = constructNode(tree, child, a1, a2,
		                nodePoint.getX(), nodePoint.getY(), childLength, cache);

	            Line2D branchLine = new Line2D.Double(
				            childPoint.getX(), childPoint.getY(),
			                nodePoint.getX(), nodePoint.getY()
				            );

	            Object[] colouring = null;
	            if (branchColouringAttribute != null) {
		            colouring = (Object[])child.getAttribute(branchColouringAttribute);
	            }
	            if (colouring != null) {
		            // If there is a colouring, then we break the path up into
		            // segments. This should allow use to iterate along the segments
		            // and colour them as we draw them.

		            float nodeHeight = (float) tree.getHeight(node);
		            float childHeight = (float) tree.getHeight(child);

		            float x1 = (float)childPoint.getX();
		            float y1 = (float)childPoint.getY();
		            float x0 = (float)nodePoint.getX();
		            float y0 = (float)nodePoint.getY();

		            GeneralPath branchPath = new GeneralPath();

		            // to help this, we are going to draw the branch backwards
		            branchPath.moveTo(x1, y1);
		            float interval = 0.0F;
		            for (int j = 0; j < colouring.length - 1; j+=2) {
//			            float height = ((Number)colouring[j+1]).floatValue();
//			            float p = (height - childHeight) / (nodeHeight - childHeight);
			            interval += ((Number)colouring[j+1]).floatValue();
			            float p = interval / (nodeHeight - childHeight);
			            float x = x1 + ((x0 - x1) * p);
			            float y = y1 + ((y0 - y1) * p);
			            branchPath.lineTo(x, y);
		            }
		            branchPath.lineTo(x0, y0);

		            // add the branchPath to the map of branch paths
		            cache.branchPaths.put(child, branchPath);

	            } else {
		            // add the branchLine to the map of branch paths
		            cache.branchPaths.put(child, branchLine);
	            }

                cache.branchLabelPaths.put(child, (Line2D)branchLine.clone());
            }

            Point2D nodeLabelPoint = new Point2D.Double(xPosition + ((length + 1.0) * directionX),
                    yPosition + ((length + 1.0) * directionY));

            Line2D nodeLabelPath = new Line2D.Double(nodePoint, nodeLabelPoint);
            cache.nodeLabelPaths.put(node, nodeLabelPath);

        } else {

            Point2D taxonPoint = new Point2D.Double(xPosition + ((length + 1.0) * directionX),
                    yPosition + ((length + 1.0) * directionY));

            Line2D taxonLabelPath = new Line2D.Double(nodePoint, taxonPoint);
            cache.tipLabelPaths.put(node, taxonLabelPath);

        }

        Point2D nodeShapePoint = new Point2D.Double(xPosition + ((length - 1.0) * directionX),
                yPosition + ((length - 1.0) * directionY));
        Line2D nodeShapePath = new Line2D.Double(nodePoint, nodeShapePoint);
        cache.nodeShapePaths.put(node, nodeShapePath);

        // add the node point to the map of node points
        cache.nodePoints.put(node, nodePoint);

        return nodePoint;
    }

    private void constructNodeAreas(final RootedTree tree, final Node node, final Area parentNodeArea, TreeLayoutCache cache) {

        if (!tree.isExternal(node)) {

            List<Node> children = tree.getChildren(node);

            boolean rotate = false;
            if (node.getAttribute("!rotate") != null &&
                    ((Boolean)node.getAttribute("!rotate"))) {
                rotate = true;
            }

            int index = (rotate ? children.size() - 1 : 0);
            Node child1 = children.get(index);
            Area childArea1 = new Area();

            constructNodeAreas(tree, child1, childArea1, cache);

            index = (rotate ? 0 : children.size() - 1);
            Node child2 = children.get(index);
            Area childArea2 = new Area();
            constructNodeAreas(tree, child2, childArea2, cache);

            GeneralPath nodePath = new GeneralPath();
            Line2D line1 = (Line2D)cache.getBranchPath(child1);
            Line2D line2 = (Line2D)cache.getBranchPath(child2);

            nodePath.moveTo(line2.getX1(), line2.getY1());
            nodePath.lineTo(line2.getX2(), line2.getY2());
            nodePath.lineTo(line1.getX1(), line1.getY1());

            nodePath.closePath();

            Area nodeArea = new Area(nodePath);

            parentNodeArea.add(nodeArea);
            parentNodeArea.add(childArea1);
            parentNodeArea.add(childArea2);

            nodeArea.subtract(childArea1);
            nodeArea.subtract(childArea2);

            cache.nodeAreas.put(node, nodeArea);
        }
    }

    private void constructHilight(RootedTree tree, Node node, double angleStart, double angleFinish, double xPosition,
                                  double yPosition, double length, TreeLayoutCache cache) {

        Object[] values = (Object[])node.getAttribute(hilightAttributeName);
        int tipCount = (Integer)values[0];
        double tipHeight = (Double)values[1];
        double height = tree.getHeight(node);

        GeneralPath hilightShape = new GeneralPath();

        final double branchAngle = (angleStart + angleFinish) / 2.0;

        float x0 = (float)(xPosition + (0.5 * length * Math.cos(branchAngle)));
        float y0 = (float)(yPosition + (0.5 * length * Math.sin(branchAngle)));

        float x1 = (float)(x0 + (tipHeight * Math.cos(angleStart)));
        float y1 = (float)(y0 + (tipHeight * Math.sin(angleStart)));

        float x2 = (float)(x0 + (tipHeight * Math.cos(angleFinish)));
        float y2 = (float)(y0 + (tipHeight * Math.sin(angleFinish)));

        hilightShape.moveTo(x0, y0);
        hilightShape.lineTo(x1, y1);
        hilightShape.lineTo(x2, y2);
        hilightShape.lineTo(x0, y0);

        hilightShape.closePath();

        // add the collapsedShape to the map of branch paths
        cache.hilightNodes.add(node);
        cache.hilightShapes.put(node, hilightShape);
    }

}