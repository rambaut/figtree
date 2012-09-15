package figtree.treeviewer.painters;

import figtree.treeviewer.TreePane;
import figtree.treeviewer.decorators.Decorator;
import jebl.evolution.graphs.Node;

import java.awt.*;
import java.awt.geom.*;

/**
 * @author Andrew Rambaut
 * @version $Id: NodeShapePainter.java 536 2006-11-21 16:10:24Z rambaut $
 */
public class NodeShapePainter extends NodePainter {


    public static final String FIXED = "Fixed";
    public static final double SIZE = 1.0;

    public enum ShapeType {
        CIRCLE("Circle"),
        RECTANGLE("Rectangle");

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

    public NodeShapePainter() {
    }

    public void setTreePane(TreePane treePane) {
        this.treePane = treePane;
    }

    public Rectangle2D calibrate(Graphics2D g2, Node node) {
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

    public ShapeType getShapeType() {
        return shapeType;
    }

    public void setShapeType(ShapeType shapeType) {
        this.shapeType = shapeType;
        firePainterChanged();
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
        Shape nodeShape = createNodeShape(node, point.getX(), point.getY());

        nodeShape = transform.createTransformedShape(nodeShape);

        Paint paint = getForeground();
        if (colourDecorator != null) {
            colourDecorator.setItem(node);
            paint = colourDecorator.getPaint(paint);
        }
        g2.setPaint(paint);
        g2.fill(nodeShape);

        g2.setPaint(Color.black);
        g2.setStroke(new BasicStroke(0.5F));
        g2.draw(nodeShape);
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

    public String getSizeAttribute() {
        return sizeAttribute;
    }

    public void setDefaultSize(double defaultSize) {
        this.defaultSize = defaultSize;
        firePainterChanged();
    }

    public void setSizeAttribute(String sizeAttribute) {
        this.sizeAttribute = sizeAttribute;
        firePainterChanged();
    }

    public void setColourDecorator(Decorator colourDecorator) {
        this.colourDecorator = colourDecorator;
        firePainterChanged();
    }

    private Shape createNodeShape(Node node, double x, double y) {

        double size = 0;

        if (sizeAttribute != null && !sizeAttribute.equals(FIXED)) {
            Object value = node.getAttribute(sizeAttribute);
            if (value != null) {
                if (value != null ) {
                    if (value instanceof Number) {
                        size = ((Number)value).doubleValue();
                    } else {
                        size = Double.parseDouble(value.toString());
                    }
//                        size *= sizeScale;
                    size += defaultSize;
                } else {
                    // todo - warn the user somehow?
                }
            }

        } else {
            size = defaultSize;
        }

        if (size > 0.0) {
            switch (shapeType) {
                case CIRCLE:
                    return new Ellipse2D.Double(x - (size * 0.5), y - (size * 0.5), size, size);
                case RECTANGLE:
                    return new Rectangle2D.Double(x - (size * 0.5), y - (size * 0.5), size, size);
            }
            throw new IllegalArgumentException("Unknown node shapeType type");

        }
        return null;
    }

    private double defaultSize = SIZE;
    private ShapeType shapeType = ShapeType.CIRCLE;
    private String sizeAttribute = null;

    private Decorator colourDecorator = null;

    private TreePane treePane;
}
