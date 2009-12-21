package figtree.treeviewer.painters;

import figtree.treeviewer.TreePane;
import jebl.evolution.graphs.Node;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.TreeSet;

/**
 * @author Andrew Rambaut
 * @version $Id: NodeBarPainter.java 536 2006-11-21 16:10:24Z rambaut $
 */
public class NodeBarPainter extends NodePainter {

	public NodeBarPainter() {

		setupAttributes(null);
	}

	public void setupAttributes(Collection<? extends Tree> trees) {
		java.util.Set<String> attributeNames = new TreeSet<String>();
        if (trees != null) {
            for (Tree tree : trees) {
                for (Node node : tree.getNodes()) {
                    for (String name : node.getAttributeNames()) {
                        if (!name.startsWith("!")) {
                            Object attr = node.getAttribute(name);
                            if (attr instanceof Object[]) {
                                Object[] array = (Object[])attr;
                                if (array.length == 2 &&
                                        array[0] instanceof Double &&
                                        array[1] instanceof Double) {
                                    attributeNames.add(name);
                                }
                            }
                        }
                    }
                }
            }
        }
		if (attributeNames.size() == 0) {
			attributeNames.add("no attributes");
		}

		this.attributeNames = new String[attributeNames.size()];
		attributeNames.toArray(this.attributeNames);

		fireAttributesChanged();
	}

	public void setTreePane(TreePane treePane) {
		this.treePane = treePane;
	}

	public Line2D getNodeBar() {
		return nodeBar;
	}

	public Rectangle2D calibrate(Graphics2D g2, Node node) {
		RootedTree tree = treePane.getTree();

		nodeBar = null;

		Line2D barPath = treePane.getTreeLayoutCache().getNodeBarPath(node);
		if (barPath != null) {

			double height = tree.getHeight(node);
			double upper = height;
			double lower = height;

			boolean hasBar = false;
			Object[] values = (Object[])node.getAttribute(displayAttribute);
			if (values != null) {
				Object value = values[0];
				if (value != null ) {
					if (value instanceof Number) {
						lower = ((Number)value).doubleValue();
					} else {
						lower = Double.parseDouble(value.toString());
					}
					hasBar = true;
				} else {
					// todo - warn the user somehow?
				}

				value = values[1];
				if (value != null ) {
					if (value instanceof Number) {
						upper = ((Number)value).doubleValue();
					} else {
						upper = Double.parseDouble(value.toString());
					}
					hasBar = true;
				} else {
					// todo - warn the user somehow?
				}
			}


			if (hasBar) {
				// x1,y1 is the node point
				double x1 = barPath.getX1();
				double y1 = barPath.getY1();
				// x2,y2 is 1.0 units heigher than the node
				double x2 = barPath.getX2();
				double y2 = barPath.getY2();

				// dx,dy is the change in x,y for one unit of height
				double dx = x2 - x1;
				double dy = y2 - y1;

				double h1 = lower - height;
				double h2 = upper - height;

				nodeBar = new Line2D.Double(
						x1 + (dx * h1), y1 + (dy * h1),
						x1 + (dx * h2), y1 + (dy * h2));

			}
		}

		if (nodeBar == null) {
			return new Rectangle2D.Double(0,0,0,0);
		}

		return nodeBar.getBounds2D();
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
	 * The bounds define the shape of the nodeBar so just draw it
	 * @param g2
	 * @param node
	 * @param justification
	 * @param barShape
	 */
	public void paint(Graphics2D g2, Node node, Justification justification, Shape barShape) {
		if (barShape != null) {

			Stroke stroke = getStroke();
			Shape strokedOutline = stroke.createStrokedShape(barShape);

			g2.setPaint(getForeground());
			g2.fill(strokedOutline);

			g2.setPaint(Color.black);
			g2.setStroke(new BasicStroke(0.5F));

			g2.draw(strokedOutline);
		}

	}

	/**
	 * The bounds define the shape of the nodeBar so just draw it
	 * @param g2
	 * @param node
	 * @param justification
	 * @param bounds
	 */
	public void paint(Graphics2D g2, Node node, Justification justification, Rectangle2D bounds) {
		throw new UnsupportedOperationException("This version of paint is not used in NodeBarPainter");
	}

	public String[] getAttributeNames() {
		return attributeNames;
	}

	public String getDisplayAttributeName() {
		return displayAttribute;
	}

	public void setDisplayAttribute(String displayAttribute) {
		this.displayAttribute = displayAttribute;
		firePainterChanged();
	}

	private double preferredWidth;
	private double preferredHeight;

	private String displayAttribute = null;
	private String[] attributeNames;

	private TreePane treePane;

	private Line2D nodeBar = null;
}
