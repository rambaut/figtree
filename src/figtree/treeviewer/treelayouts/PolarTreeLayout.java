package figtree.treeviewer.treelayouts;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.RootedTree;

import java.awt.*;
import java.awt.geom.*;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id: PolarTreeLayout.java 780 2007-09-09 09:46:01Z rambaut $
 */
public class PolarTreeLayout extends AbstractTreeLayout {

    private double rootAngle = 180.0;
    private double rootLengthProportion = 0.01;
    private double angularRange = 360.0;

    private double fishEye = 0.0;
    private double pointOfInterest = 0.5;
    private int tipCount = 0;

    private double totalRootLength = 0.0;

    private boolean showingRootBranch = true;

    private TipLabelPosition tipLabelPosition = TipLabelPosition.FLUSH;


    private double yPosition;
    private double yIncrement;

    private double maxXPosition;

    public enum TipLabelPosition {
        FLUSH,
        RADIAL,
        HORIZONTAL
    }

    public AxisType getXAxisType() {
        return AxisType.CONTINUOUS;
    }

    public AxisType getYAxisType() {
        return AxisType.CONTINUOUS;
    }

    public boolean maintainAspectRatio() {
        return true;
    }

    public void setFishEye(double fishEye) {
        this.fishEye = fishEye;
        fireTreeLayoutChanged();
    }

    public void setPointOfInterest(double x, double y) {
        this.pointOfInterest = getPolarAngle(x, y);
        fireTreeLayoutChanged();
    }

    public double getHeightOfPoint(Point2D point) {
        throw new UnsupportedOperationException("Method getHeightOfPoint() is not supported in this TreeLayout");
    }

    public Shape getAxisLine(double height) {
        double x = height;
        if (isAxisReversed()) {
            x = maxXPosition - x;
        }
        return new Ellipse2D.Double(-x, -x, x * 2.0, x * 2.0);
    }

    public Shape getHeightArea(double height1, double height2) {
        Area area1 = new Area(new Ellipse2D.Double(0.0, 0.0, height2 * 2.0, height2 * 2.0));
        Area area2 = new Area(new Ellipse2D.Double(0.0, 0.0, height1 * 2.0, height1 * 2.0));
        area1.subtract(area2);
        return area1;
    }


    public double getRootAngle() {
        return rootAngle;
    }

    public double getAngularRange() {
        return angularRange;
    }

    public boolean isShowingRootBranch() {
        return showingRootBranch;
    }

    public double getTotalRootLength() {
        return totalRootLength;
    }

    public double getRootLengthProportion() {
        return rootLengthProportion;
    }

    public TipLabelPosition getTipLabelPosition() {
        return tipLabelPosition;
    }

    public void setRootAngle(double rootAngle) {
        this.rootAngle = rootAngle;
        constant = rootAngle - ((360.0 - angularRange) * 0.5);
        fireTreeLayoutChanged();
    }

    public void setAngularRange(double angularRange) {
        this.angularRange = angularRange;
        constant = rootAngle - ((360.0 - angularRange) * 0.5);
        fireTreeLayoutChanged();
    }

    public void setShowingRootBranch(boolean showingRootBranch) {
        this.showingRootBranch = showingRootBranch;
        fireTreeLayoutChanged();
    }

    public void setRootLengthProportion(double rootLengthProportion) {
        this.rootLengthProportion = rootLengthProportion;
        fireTreeLayoutChanged();
    }

    public void setTipLabelPosition(TipLabelPosition tipLabelPosition) {
        this.tipLabelPosition = tipLabelPosition;
        fireTreeLayoutChanged();
    }

    public boolean isShowingColouring() {
        return (branchColouringAttribute != null);
    }

    public void layout(RootedTree tree, TreeLayoutCache cache) {

        cache.clear();


        Node root = tree.getRootNode();
        double totalRootLength = (rootLengthProportion * tree.getHeight(root)) * 10.0;

        maxXPosition = 0.0;
        getMaxXPosition(tree, root, totalRootLength);

        yPosition = 0.0;

        tipCount = tree.getExternalNodes().size();
        yIncrement = 1.0 / tipCount;

        final Point2D rootPoint = constructNode(tree, root, 0.0, totalRootLength, /*new Area(),*/ cache);

//        constructNodeAreas(tree, root, new Area(), cache);

        if (showingRootBranch) {
            // construct a root branch line
            final double y = rootPoint.getY();
            Line2D line = new Line2D.Double(transform(0.0, y), transform(rootPoint.getX(), y));

            // add the line to the map of branch paths
            cache.branchPaths.put(root, line);
        }
    }

    private Point2D constructNode(RootedTree tree, Node node, double xParent, double xPosition, /*final Area parentNodeArea,*/ TreeLayoutCache cache) {

        Point2D nodePoint;

        if (hilightAttributeName != null && node.getAttribute(hilightAttributeName) != null) {
            constructHilight(tree, node, xParent, xPosition, cache);
        }

        if (!tree.isExternal(node)) {

            if (collapsedAttributeName != null && node.getAttribute(collapsedAttributeName) != null) {
                nodePoint = constructCollapsedNode(tree, node, xPosition, cache);
            } else if (cartoonAttributeName != null && node.getAttribute(cartoonAttributeName) != null) {
                nodePoint = constructCartoonNode(tree, node, xPosition, cache);
            } else {
                double yPos = 0.0;

                List<Node> childList = tree.getChildren(node);
                Node[] children = new Node[childList.size()];
                Point2D[] childPoints = new Point2D[childList.size()];

                boolean rotate = false;
                if (node.getAttribute("!rotate") != null &&
                        ((Boolean)node.getAttribute("!rotate"))) {
                    rotate = true;
                }

//                Area[] childAreas = new Area[childList.size()];

                for (int i = 0; i < childList.size(); i++) {
                    int index = i;
                    if (rotate) {
                        index = childList.size() - i - 1;
                    }
                    children[i] = childList.get(index);

                    final double length = tree.getLength(children[i]);
//                    childAreas[i] = new Area();
                    childPoints[i] = constructNode(tree, children[i], xPosition, xPosition + length, cache);

//                    parentNodeArea.add(childAreas[i]);

                    yPos += childPoints[i].getY();
                }

                // the y-position of the node is the average of the child nodes
                yPos /= childList.size();

                nodePoint = new Point2D.Double(xPosition, yPos);
                Point2D transformedNodePoint = transform(nodePoint);

                final double start = getAngle(yPos);

//                GeneralPath nodeAreaPath = new GeneralPath();

                double firstChildAngle = 0;

                for (int i = 0; i < childList.size(); i++) {
                    int index = i;
                    if (rotate) {
                        index = childList.size() - i - 1;
                    }

                    GeneralPath branchPath = new GeneralPath();
                    final Point2D transformedChildPoint = transform(childPoints[i]);

                    final Point2D transformedShoulderPoint = transform(
                            nodePoint.getX(), childPoints[i].getY());

//                    if (i == 0) {
//                        nodeAreaPath.moveTo(
//                                (float) transformedShoulderPoint.getX(),
//                                (float) transformedShoulderPoint.getY());
//                        final Point2D transformedPoint2 = transform(
//                                maxXPosition, childPoints[i].getY());
//                        nodeAreaPath.lineTo(
//                                (float) transformedPoint2.getX(),
//                                (float) transformedPoint2.getY());
//                        firstChildAngle = getAngle(childPoints[i].getY());
//                    }

                    Object[] colouring = null;
                    if (branchColouringAttribute != null) {
                        colouring = (Object[])children[i].getAttribute(branchColouringAttribute);
                    }
                    if (colouring != null) {
                        // If there is a colouring, then we break the path up into
                        // segments. This should allow use to iterate along the segments
                        // and colour them as we draw them.

                        float nodeHeight = (float) tree.getHeight(node);
                        float childHeight = (float) tree.getHeight(children[i]);

                        double x1 = childPoints[i].getX();
                        double x0 = nodePoint.getX();

                        branchPath.moveTo(
                                (float) transformedChildPoint.getX(),
                                (float) transformedChildPoint.getY());

                        float x = (float)x1;
                        for (int j = 0; j < colouring.length - 1; j+=2) {
//                            double height = ((Number)colouring[j+1]).doubleValue();
//                            double p = (height - childHeight) / (nodeHeight - childHeight);
                            float interval = ((Number)colouring[j+1]).floatValue();
                            float p = interval / (nodeHeight - childHeight);
                            x -= ((x1 - x0) * p);
                            final Point2D transformedPoint = transform(x, childPoints[index].getY());
                            branchPath.lineTo(
                                    (float) transformedPoint.getX(),
                                    (float) transformedPoint.getY());
                        }
                        branchPath.lineTo(
                                (float) transformedShoulderPoint.getX(),
                                (float) transformedShoulderPoint.getY());

                    } else {
                        branchPath.moveTo(
                                (float) transformedChildPoint.getX(),
                                (float) transformedChildPoint.getY());

                        branchPath.lineTo(
                                (float) transformedShoulderPoint.getX(),
                                (float) transformedShoulderPoint.getY());
                    }

                    final double finish = getAngle(childPoints[index].getY());

                    Arc2D arc = new Arc2D.Double();
                    arc.setArcByCenter(0.0, 0.0, nodePoint.getX(), finish, start - finish, Arc2D.OPEN);
                    branchPath.append(arc, true);

//                    if (i == childList.size() - 1) {
//                        Arc2D arc2 = new Arc2D.Double();
//                        arc2.setArcByCenter(0.0, 0.0, maxXPosition, firstChildAngle, finish - firstChildAngle, Arc2D.OPEN);
//                        nodeAreaPath.append(arc2, true);
//
//                        nodeAreaPath.lineTo(
//                                (float) transformedShoulderPoint.getX(),
//                                (float) transformedShoulderPoint.getY());
//
//                        Arc2D arc3 = new Arc2D.Double();
//                        arc3.setArcByCenter(0.0, 0.0, nodePoint.getX(), finish, firstChildAngle - finish, Arc2D.OPEN);
//                        nodeAreaPath.append(arc3, true);
//                    }

                    // add the branchPath to the map of branch paths
                    cache.branchPaths.put(children[i], branchPath);

                    final double x3 = (nodePoint.getX() + childPoints[index].getX()) / 2;

                    Line2D branchLabelPath = new Line2D.Double(
                            transform(x3 - 1.0, childPoints[index].getY()),
                            transform(x3 + 1.0, childPoints[index].getY()));

                    cache.branchLabelPaths.put(children[i], branchLabelPath);
                }

//                nodeAreaPath.closePath();
//
//                Area nodeArea = new Area(nodeAreaPath);
//                parentNodeArea.add(nodeArea);
//
//                for (Area childArea : childAreas) {
//                    nodeArea.subtract(childArea);
//                }
//
//                cache.nodeAreas.put(node, nodeArea);

                Line2D nodeLabelPath = new Line2D.Double(
                        transform(nodePoint.getX(), yPos),
                        transform(nodePoint.getX() + 1.0, yPos));
                cache.nodeLabelPaths.put(node, nodeLabelPath);

                Line2D nodeShapePath = new Line2D.Double(
                        transform(nodePoint.getX(), yPos),
                        transform(nodePoint.getX() - 1.0, yPos));
                cache.nodeShapePaths.put(node, nodeShapePath);

                // add the node point to the map of node points
                cache.nodePoints.put(node, transformedNodePoint);
            }
        } else {

            nodePoint = new Point2D.Double(xPosition, yPosition);
            Point2D transformedNodePoint = transform(nodePoint);

            Line2D tipLabelPath;

            if (tipLabelPosition == TipLabelPosition.FLUSH) {

                tipLabelPath = new Line2D.Double(transformedNodePoint, transform(xPosition + 1.0, yPosition));

            } else if (tipLabelPosition == TipLabelPosition.RADIAL) {

                tipLabelPath = new Line2D.Double(transform(maxXPosition, yPosition),
                        transform(maxXPosition + 1.0, yPosition));

                Line2D calloutPath = new Line2D.Double(transformedNodePoint, transform(maxXPosition, yPosition));

                cache.calloutPaths.put(node, calloutPath);

            } else if (tipLabelPosition == TipLabelPosition.HORIZONTAL) {
                // this option disabled in getControls (JH)
                throw new UnsupportedOperationException("Not implemented yet");
            } else {
                // this is a bug
                throw new IllegalArgumentException("Unrecognized enum value");
            }

            cache.tipLabelPaths.put(node, tipLabelPath);

            Line2D nodeShapePath = new Line2D.Double(
                    transform(nodePoint.getX(), yPosition),
                    transform(nodePoint.getX() - 1.0, yPosition));
            cache.nodeShapePaths.put(node, nodeShapePath);

            yPosition += yIncrement;

            // add the node point to the map of node points
            cache.nodePoints.put(node, transformedNodePoint);
        }

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

            PathIterator pi1 = cache.getBranchPath(child1).getPathIterator(null);
            nodePath.append(pi1, false);

            // start point
//            final float x0 = (float) branchBounds1.getX();
//            final float y0 = (float) (branchBounds1.getY() + branchBounds1.getHeight());
//            nodePath.moveTo(x0, y0);
//
//            final float y1 = (float) branchBounds1.getY();
//            nodePath.lineTo(x0, y1);
//
//            nodePath.lineTo(maxXPosition, y1);
//
//            final float y2 = (float) (branchBounds2.getY() + branchBounds2.getHeight());
//            nodePath.lineTo(maxXPosition, y2);
//
//            nodePath.lineTo(x0, y2);
//
//            nodePath.lineTo(x0, y0);
//            nodePath.closePath();
//
//            Area nodeArea = new Area(nodePath);
//
//            parentNodeArea.add(nodeArea);
//            parentNodeArea.add(childArea1);
//            parentNodeArea.add(childArea2);
//
//            nodeArea.subtract(childArea1);
//            nodeArea.subtract(childArea2);
//
//            cache.nodeAreas.put(node, nodeArea);
        }
    }

    private Point2D constructCartoonNode(RootedTree tree, Node node, double xPosition, TreeLayoutCache cache) {

        Point2D nodePoint;

        Object[] values = (Object[])node.getAttribute(cartoonAttributeName);
        int tipCount = (Integer)values[0];
        double tipHeight = (Double)values[1];
        double height = tree.getHeight(node);
        double maxXPos = xPosition + height - tipHeight;

        double minYPos = yPosition;
        yPosition += yIncrement * (tipCount - 1);
        double maxYPos = yPosition;
        yPosition += yIncrement;

        // the y-position of the node is the average of the child nodes
        double yPos = (maxYPos + minYPos) / 2;

        nodePoint = new Point2D.Double(xPosition, yPos);
        Point2D transformedNodePoint0 = transform(nodePoint);
        Point2D transformedNodePoint1 = transform(new Point2D.Double(maxXPos, minYPos));
        Point2D transformedNodePoint2 = transform(new Point2D.Double(maxXPos, maxYPos));

        GeneralPath collapsedShape = new GeneralPath();

        collapsedShape.moveTo((float)transformedNodePoint0.getX(), (float)transformedNodePoint0.getY());
        collapsedShape.lineTo((float)transformedNodePoint1.getX(), (float)transformedNodePoint1.getY());
        final double start = getAngle(maxYPos);
        final double finish = getAngle(minYPos);
        Arc2D arc = new Arc2D.Double();
        arc.setArcByCenter(0.0, 0.0, maxXPos, finish, start - finish, Arc2D.OPEN);
        collapsedShape.append(arc, true);
        collapsedShape.closePath();

        // add the collapsedShape to the map of branch paths
        cache.collapsedShapes.put(node, collapsedShape);

        Line2D nodeLabelPath = new Line2D.Double(
                transform(nodePoint.getX(), yPos),
                transform(nodePoint.getX() + 1.0, yPos));

        cache.nodeLabelPaths.put(node, nodeLabelPath);

        Line2D nodeBarPath = new Line2D.Double(
                transform(nodePoint.getX(), yPos),
                transform(nodePoint.getX() - 1.0, yPos));

        cache.nodeShapePaths.put(node, nodeBarPath);

        if (showingCartoonTipLabels) {
            constructCartoonTipLabelPaths(tree, node, maxXPos, new double[] { minYPos }, cache);
        }

        // add the node point to the map of node points
        cache.nodePoints.put(node, transformedNodePoint0);

        return nodePoint;
    }

    private void constructCartoonTipLabelPaths(RootedTree tree, Node node, double xPosition, double[] yPosition, TreeLayoutCache cache) {

        if (!tree.isExternal(node)) {
            for (Node child :  tree.getChildren(node)) {
                constructCartoonTipLabelPaths(tree, child, xPosition, yPosition, cache);
            }
        } else {

            Point2D nodePoint = new Point2D.Double(xPosition, yPosition[0]);

            Point2D transformedNodePoint = transform(nodePoint);

            Line2D tipLabelPath;

            if (tipLabelPosition == TipLabelPosition.FLUSH) {

                tipLabelPath = new Line2D.Double(transformedNodePoint, transform(xPosition + 1.0, yPosition[0]));

            } else if (tipLabelPosition == TipLabelPosition.RADIAL) {

                tipLabelPath = new Line2D.Double(transform(maxXPosition, yPosition[0]),
                        transform(maxXPosition + 1.0, yPosition[0]));

                Line2D calloutPath = new Line2D.Double(transformedNodePoint, transform(maxXPosition, yPosition[0]));

                cache.calloutPaths.put(node, calloutPath);

            } else if (tipLabelPosition == TipLabelPosition.HORIZONTAL) {
                // this option disabled in getControls (JH)
                throw new UnsupportedOperationException("Not implemented yet");
            } else {
                // this is a bug
                throw new IllegalArgumentException("Unrecognized enum value");
            }

            cache.tipLabelPaths.put(node, tipLabelPath);

            yPosition[0] += yIncrement;

        }
    }

    private Point2D constructCollapsedNode(RootedTree tree, Node node, double xPosition, TreeLayoutCache cache) {

        Point2D nodePoint;

        Object[] values = (Object[])node.getAttribute(collapsedAttributeName);
        //String tipName = (String)values[0];
        double tipHeight = (Double)values[1];
        double height = tree.getHeight(node);
        double maxXPos = xPosition + height - tipHeight;

        double minYPos = yPosition - (yIncrement * 0.5);
        double maxYPos = minYPos + yIncrement;
        yPosition += yIncrement;

        // the y-position of the node is the average of the child nodes
        double yPos = (maxYPos + minYPos) / 2;

        nodePoint = new Point2D.Double(xPosition, yPos);
        Point2D transformedNodePoint0 = transform(nodePoint);
        Point2D transformedNodePoint1 = transform(new Point2D.Double(maxXPos, minYPos));

        GeneralPath collapsedShape = new GeneralPath();

        collapsedShape.moveTo((float)transformedNodePoint0.getX(), (float)transformedNodePoint0.getY());

        collapsedShape.lineTo((float)transformedNodePoint1.getX(), (float)transformedNodePoint1.getY());
        final double start = getAngle(maxYPos);
        final double finish = getAngle(minYPos);
        Arc2D arc = new Arc2D.Double();
        arc.setArcByCenter(0.0, 0.0, maxXPos, finish, start - finish, Arc2D.OPEN);
        collapsedShape.append(arc, true);
        collapsedShape.closePath();

        // add the collapsedShape to the map of branch paths
        cache.collapsedShapes.put(node, collapsedShape);

        Line2D nodeLabelPath = new Line2D.Double(
                transform(nodePoint.getX(), yPos),
                transform(nodePoint.getX() + 1.0, yPos));

        cache.nodeLabelPaths.put(node, nodeLabelPath);

        Line2D nodeBarPath = new Line2D.Double(
                transform(nodePoint.getX(), yPos),
                transform(nodePoint.getX() - 1.0, yPos));

        cache.nodeShapePaths.put(node, nodeBarPath);

        Point2D transformedNodePoint = transform(maxXPos, yPos);

        Line2D tipLabelPath;

        if (tipLabelPosition == TipLabelPosition.FLUSH) {

            tipLabelPath = new Line2D.Double(transformedNodePoint, transform(maxXPos + 1.0, yPos));

        } else if (tipLabelPosition == TipLabelPosition.RADIAL) {

            tipLabelPath = new Line2D.Double(transform(maxXPosition, yPos),
                    transform(maxXPosition + 1.0, yPos));

            Line2D calloutPath = new Line2D.Double(transformedNodePoint, transform(maxXPosition, yPos));

            cache.calloutPaths.put(node, calloutPath);

        } else if (tipLabelPosition == TipLabelPosition.HORIZONTAL) {
            // this option disabled in getControls (JH)
            throw new UnsupportedOperationException("Not implemented yet");
        } else {
            // this is a bug
            throw new IllegalArgumentException("Unrecognized enum value");
        }

        cache.tipLabelPaths.put(node, tipLabelPath);

        // add the node point to the map of node points
        cache.nodePoints.put(node, transformedNodePoint0);

        return nodePoint;
    }

    private void constructHilight(RootedTree tree, Node node, double xParent, double xPosition, TreeLayoutCache cache) {

        Object[] values = (Object[])node.getAttribute(hilightAttributeName);
        int tipCount = (Integer)values[0];
        double tipHeight = (Double)values[1];
        double height = tree.getHeight(node);

        GeneralPath hilightShape = new GeneralPath();

        double x0 = ((xPosition + xParent) / 2.0);
        double x1 = (xPosition + height /*- tipHeight*/);
        double y0 = yPosition - (yIncrement / 2);
        double y1 = yPosition + (yIncrement * tipCount) - (yIncrement / 2);

        Point2D p1 = transform(new Point2D.Double(x0, y0));
        Point2D p2 = transform(new Point2D.Double(x1, y0));
        Point2D p3 = transform(new Point2D.Double(x1, y1));
        Point2D p4 = transform(new Point2D.Double(x0, y1));


        final double start = getAngle(y0);
        final double finish = getAngle(y1);

        hilightShape.moveTo((float)p1.getX(), (float)p1.getY());
        hilightShape.lineTo((float)p2.getX(), (float)p2.getY());

        Arc2D arc = new Arc2D.Double();
        arc.setArcByCenter(0.0, 0.0, x1, start, finish - start, Arc2D.OPEN);
        hilightShape.append(arc, true);

        hilightShape.lineTo((float)p3.getX(), (float)p3.getY());

        arc = new Arc2D.Double();
        arc.setArcByCenter(0.0, 0.0, x0, finish, start - finish, Arc2D.OPEN);
        hilightShape.append(arc, true);

        hilightShape.closePath();

        // add the collapsedShape to the map of branch paths
        cache.hilightNodes.add(node);
        cache.hilightShapes.put(node, hilightShape);
    }

    private void getMaxXPosition(RootedTree tree, Node node, double xPosition) {

        if (!tree.isExternal(node)) {

            List<Node> children = tree.getChildren(node);

            for (Node child : children) {
                final double length = tree.getLength(child);
                getMaxXPosition(tree, child, xPosition + length);
            }

        } else {
            if (xPosition > maxXPosition) {
                maxXPosition = xPosition;
            }
        }
    }

    /**
     * Polar transform
     *
     * @param point
     * @return the point in polar space
     */
    private Point2D transform(Point2D point) {
        return transform(point.getX(), point.getY());
    }

    /**
     * Polar transform
     *
     * @param h the hypotenuse
     * @param a the angle
     * @return the point in euclidean space
     */
    private Point2D transform(double h, double a) {
        double r = - Math.toRadians(getAngle(a));
        return new Point2D.Double(h * Math.cos(r), h * Math.sin(r));
    }

    private double constant;

    /**
     * Polar angle for an x, y coordinate
     *
     * @param x
     * @param y
     * @return the angle
     */
    private double getPolarAngle(double x, double y) {
        double r = Math.toDegrees(Math.atan(y/x));
        return (constant - r) / angularRange;
    }

    /**
     * The angle in degrees given by a 0, 1 proportion of the circle
     * @param y
     * @return
     */
    private double getAngle(double y) {
        if (fishEye == 0.0) {
            return constant - (y * angularRange);
        }

        double scale = 1.0 / (fishEye * tipCount);
        double dist = pointOfInterest - y;
        double min =  1.0 - (pointOfInterest / (scale + pointOfInterest));
        double max =  1.0 - ((pointOfInterest - 1.0) / (scale - (pointOfInterest - 1.0)));

        double c =  1.0 - (dist < 0 ? (dist / (scale - dist)) : (dist / (scale + dist)));

        double tY = (c - min) / (max - min);

        return rootAngle - ((360.0 - angularRange) * 0.5) - (tY * angularRange);
    }


}
