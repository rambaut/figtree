/*
 * NodeShapePainter.java
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

package figtree.treeviewer.painters;

import figtree.treeviewer.TreePane;
import figtree.treeviewer.decorators.ContinuousScale;
import figtree.treeviewer.decorators.Decorator;
import jebl.evolution.graphs.Node;

import java.awt.*;
import java.awt.geom.*;
import java.util.HashMap;
import java.util.Map;

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
public class NodeShapePainter extends NodePainter {


    public static final String FIXED = "Fixed";
    public static final double MAX_SIZE = 4.0;
    public static final double MIN_SIZE = 0.0;

    public enum ShapeType {
        CIRCLE("Circle"),
        RECTANGLE("Rectangle"),
        DIAMOND("Diamond");

        ShapeType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return name;
        }

        private final String name;
    }

    public enum ScaleType {
        WIDTH("Width"),
        AREA("Area");

        ScaleType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return name;
        }

        private final String name;
    }

    public NodeShapePainter() {
    }

    public void setTreePane(TreePane treePane) {
        this.treePane = treePane;
    }

    public Rectangle2D calibrate(Graphics2D g2, Node node) {
        nodeShapeMap.clear();

        Line2D shapePath = treePane.getTreeLayoutCache().getNodeShapePath(node);

        if (shapePath != null) {
            double x1 = shapePath.getX1();
            double y1 = shapePath.getY1();

            Shape nodeShape = createNodeShape(node, x1, y1);

            if (nodeShape != null) {
                return nodeShape.getBounds2D();
            }
        }
        return null;
    }

    public void setShapeType(ShapeType shapeType) {
        this.shapeType = shapeType;
        firePainterChanged();
    }

    public void setScaleType(ScaleType scaleType) {
        this.scaleType = scaleType;
        firePainterChanged();
    }

    public void setNodeType(boolean external, boolean internal) {
        this.external = external;
        this.internal = internal;
        firePainterChanged();
    }

    public boolean isExternal() {
        return external;
    }

    public boolean isInternal() {
        return internal;
    }

    public double getPreferredWidth() {
        return 1.0;
    }

    public double getPreferredHeight() {
        return 1.0;
    }

    public double getHeightBound() {
        return 1.0;
    }

    /**
     * The bounds define the shapeType of the nodeBar so just draw it
     * @param g2
     * @param node
     */
    public void paint(Graphics2D g2, Node node, Point2D point, AffineTransform transform) {
        Shape nodeShape = getNodeShape(node, point, transform);
        Paint paint = getForeground();
        if (colourDecorator != null) {
            colourDecorator.setItem(node);
            paint = colourDecorator.getPaint(paint);
        }
        g2.setPaint(paint);
        g2.fill(nodeShape);

        if (outlineStroke > 0.0F) {
            g2.setPaint(outlinePaint);
            g2.setStroke(new BasicStroke(outlineStroke));
            g2.draw(nodeShape);
        }
    }

    /**
     * The bounds define the shapeType of the nodeBar so just draw it
     * @param g2
     * @param node
     */
    public void paintBackground(Graphics2D g2, Node node, Point2D point, AffineTransform transform) {
        Shape nodeShape = getNodeShape(node, point, transform);
        g2.setPaint(getBackground());
        g2.setStroke(new BasicStroke(backgroundStroke));
        g2.draw(nodeShape);
    }

    /**
     * The get the shape of the node shape
     * @param node
     */
    public Shape getNodeShape(Node node, Point2D point, AffineTransform transform) {
        return nodeShapeMap.computeIfAbsent(node,
                k -> transform.createTransformedShape(
                        createNodeShape(k, point.getX(), point.getY())));
    }

    public Shape getNodeShape(Node node) {
        return nodeShapeMap.get(node);
    }

    /**
     * The bounds define the shapeType of the nodeBar so just draw it
     * @param g2
     * @param node
     * @param justification
     * @param bounds
     */
    public void paint(Graphics2D g2, Node node, Justification justification, Rectangle2D bounds) {
        throw new UnsupportedOperationException("This version of paint is not used in NodeShapePainter");
    }

    public void setMaxSize(double maxSize) {
        this.maxSize = maxSize;
        firePainterChanged();
    }

    public void setMinSize(double minSize) {
        this.minSize = minSize;
        firePainterChanged();
    }

    public void setSizeAttribute(String sizeAttribute) {
        this.sizeAttribute = sizeAttribute;
        sizeScale = new ContinuousScale(sizeAttribute, treePane.getTree().getNodes());
        firePainterChanged();
    }

    public void setColourDecorator(Decorator colourDecorator) {
        this.colourDecorator = colourDecorator;
        firePainterChanged();
    }

    public void setOutline(final float outlineStroke, final Paint outlinePaint) {
        this.outlineStroke = outlineStroke;
        this.outlinePaint = outlinePaint;
        firePainterChanged();
    }

    private Shape createNodeShape(Node node, double x, double y) {

        double size = maxSize;

        if (sizeAttribute != null && !sizeAttribute.equals(FIXED)) {

            Object valueObject = node.getAttribute(sizeAttribute);
            double value = sizeScale.getValue(valueObject);
            if (scaleType == ScaleType.AREA) {
                double minArea = getShapeArea(minSize);
                double maxArea = getShapeArea(maxSize + minSize);
                size = getShapeWidth(((maxArea - minArea) * value) + minArea);
            } else {
                size = minSize + (maxSize * value);
            }
        }

        if (size > 0.0) {

            switch (shapeType) {
                case CIRCLE:
                    return new Ellipse2D.Double(x - (size * 0.5), y - (size * 0.5), size, size);
                case RECTANGLE:
                    return new Rectangle2D.Double(x - (size * 0.5), y - (size * 0.5), size, size);
                case DIAMOND:
                    Shape shape = new Rectangle2D.Double(x - (size * 0.5), y - (size * 0.5), size, size);
                    return AffineTransform.getRotateInstance(Math.PI / 4.0, x, y).createTransformedShape(shape);
            }
            throw new IllegalArgumentException("Unknown node shapeType type");

        }
        return null;
    }

    private double getShapeArea(double width) {
        switch (shapeType) {
            case CIRCLE:
                double radius = width * 0.5;
                return Math.PI * radius * radius;
            case RECTANGLE:
            case DIAMOND:
                return width * width;
        }
        throw new IllegalArgumentException("Unknown node shapeType type");
    }

    private double getShapeWidth(double area) {
        switch (shapeType) {
            case CIRCLE:
                return 2.0 * Math.sqrt(area / Math.PI);
            case RECTANGLE:
            case DIAMOND:
                return Math.sqrt(area);
        }
        throw new IllegalArgumentException("Unknown node shapeType type");
    }

    private Map<Node, Shape> nodeShapeMap = new HashMap<>();

    private double maxSize = MAX_SIZE;
    private double minSize =  MIN_SIZE;
    private ShapeType shapeType = ShapeType.CIRCLE;
    private ScaleType scaleType = ScaleType.WIDTH;
    private String sizeAttribute = null;

    private boolean external = true;
    private boolean internal = true;

    private Decorator colourDecorator = null;
    private ContinuousScale sizeScale = null;

    private float backgroundStroke = 3.0f;
    private float outlineStroke = 0.5f;
    private Paint outlinePaint = Color.black;

    private TreePane treePane;
}
