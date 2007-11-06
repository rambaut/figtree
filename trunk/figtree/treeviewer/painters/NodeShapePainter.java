package figtree.treeviewer.painters;

import figtree.treeviewer.TreePane;
import jebl.evolution.graphs.Node;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

/**
 * @author Andrew Rambaut
 * @version $Id: NodeShapePainter.java 536 2006-11-21 16:10:24Z rambaut $
 */
public class NodeShapePainter extends NodePainter {

	public static final String AREA_ATTRIBUTE = "area";
	public static final String RADIUS_ATTRIBUTE = "radius";

	public static final String WIDTH_ATTRIBUTE = "width";
	public static final String HEIGHT_ATTRIBUTE = "height";

    public static final String LOWER_ATTRIBUTE = "lower";
    public static final String UPPER_ATTRIBUTE = "upper";

	public enum NodeShape {
	    CIRCLE("Circle"),
	    RECTANGLE("Rectangle");

	    NodeShape(String name) {
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

        setupAttributes(null);
    }

    public void setupAttributes(Tree tree) {
        java.util.List<String> attributeNames = new ArrayList<String>();
        if (tree != null) {
            Set<String> nodeAttributes = new TreeSet<String>();
            for (Node node : tree.getNodes()) {
                nodeAttributes.addAll(node.getAttributeNames());
            }
            attributeNames.addAll(nodeAttributes);
        }

        this.attributes = new String[attributeNames.size()];
        attributeNames.toArray(this.attributes);

        firePainterSettingsChanged();
    }

    public void setTreePane(TreePane treePane) {
        this.treePane = treePane;
    }

    public Rectangle2D calibrate(Graphics2D g2, Node item) {
        RootedTree tree = treePane.getTree();
        Point2D nodePoint = treePane.getTreeLayoutCache().getNodePoint(item);

        preferredWidth = 20;
        preferredHeight = 20;

        return new Rectangle2D.Double(nodePoint.getX() - 10, nodePoint.getY() - 10, preferredWidth, preferredHeight);
    }

    public double getPreferredWidth() {
        return preferredWidth;
    }

    public double getPreferredHeight() {
        return preferredHeight;
    }

    public double getHeightBound() {
        return preferredHeight;
    }

    /**
     * The bounds define the shape of the bar so just draw it
     * @param g2
     * @param item
     * @param justification
     * @param bounds
     */
    public void paint(Graphics2D g2, Node item, Justification justification, Rectangle2D bounds) {
        if (getBackground() != null) {
            g2.setPaint(getBackground());
            g2.fill(bounds);
        }

        if (getBorderPaint() != null && getBorderStroke() != null) {
            g2.setPaint(getBorderPaint());
            g2.setStroke(getBorderStroke());
        }

        g2.draw(bounds);
    }

    public String[] getAttributeNames() {
        return attributes;
    }

    public void setDisplayAttribute(String display, String attribute) {
        displayAttributes.put(display, attribute);
        firePainterChanged();
    }

    public void setDisplayValues(String display, double value) {
        displayValues.put(display, new Double(value));
        firePainterChanged();
    }

    private double preferredWidth;
    private double preferredHeight;

    protected Map<String, String> displayAttributes = new HashMap<String, String>();
    protected Map<String, Number> displayValues = new HashMap<String, Number>();
    protected String[] attributes;

    protected TreePane treePane;
}
