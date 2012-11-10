/*
 * SimpleLabelPainter.java
 *
 * Copyright (C) 2012 Andrew Rambaut
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

package figtree.panel;

import figtree.treeviewer.TreePane;
import figtree.treeviewer.painters.LabelPainter;
import figtree.treeviewer.decorators.Decorator;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;
import jebl.util.Attributable;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

/**
 * A simple implementation of LabelPainter that can be used to display
 * tip, node or branch labels. It can display, taxon names, branch lengths,
 * node heights or other attributeNames of nodes.
 *
 * @author Andrew Rambaut
 * @version $Id: BasicLabelPainter.java 773 2007-09-04 23:51:07Z rambaut $
 */
public class SimpleLabelPainter extends LabelPainter<Node> {

	public static final String TAXON_NAMES = "Taxon names";
	public static final String NODE_AGES = "Node ages";
	public static final String BRANCH_LENGTHS = "Branch lengths";

	public SimpleLabelPainter(PainterIntent intent) {
        super(intent);

		setupAttributes(null);

		if (this.displayAttribute == null) {
			this.displayAttribute = attributes[0];
		} else {
			this.displayAttribute = "";
		}

	}

	public void setupAttributes(Collection<? extends Tree> trees) {

		List<String> attributeNames = new ArrayList<String>();
		switch( intent ) {
			case TIP: {
				attributeNames.add(TAXON_NAMES);
				attributeNames.add(NODE_AGES);
				attributeNames.add(BRANCH_LENGTHS);
				break;
			}
			case NODE: {
				attributeNames.add(NODE_AGES);
				attributeNames.add(BRANCH_LENGTHS);
				break;
			}
			case BRANCH: {
				attributeNames.add(BRANCH_LENGTHS);
				attributeNames.add(NODE_AGES);
				break;
			}
		}

		if (trees != null) {
			for (Tree tree : trees) {
				Set<String> nodeAttributes = new TreeSet<String>();
				if (intent == PainterIntent.TIP) {
					for (Node node : tree.getExternalNodes()) {
						nodeAttributes.addAll(node.getAttributeNames());
					}
				} else if (intent == PainterIntent.NODE) {
					for (Node node : tree.getInternalNodes()) {
						nodeAttributes.addAll(node.getAttributeNames());
					}
				} else {
					for (Node node : tree.getNodes()) {
						nodeAttributes.addAll(node.getAttributeNames());
					}
				}
				for (String attributeName : nodeAttributes) {
					if (!attributeName.startsWith("!")) {
						attributeNames.add(attributeName);
					}
				}
			}
		}

		this.attributes = new String[attributeNames.size()];
		attributeNames.toArray(this.attributes);

		firePainterSettingsChanged();
	}

	public void setTreePane(TreePane treePane) {
		this.treePane = treePane;
	}

	public Decorator getBorderDecorator() {
		return borderDecorator;
	}

	public void setBorderDecorator(Decorator borderDecorator) {
		this.borderDecorator = borderDecorator;
	}

	public Decorator getTextDecorator() {
		return textDecorator;
	}

	public void setTextDecorator(Decorator textDecorator) {
		this.textDecorator = textDecorator;
	}

    public Set<Attributable> getAttributableItems() {
        return null;
    }

    public Tree getTree() {
		return treePane.getTree();
	}

	protected String getLabel(Tree tree, Node node) {
		if (displayAttribute.equalsIgnoreCase(TAXON_NAMES)) {
			Taxon taxon = tree.getTaxon(node);
			if (taxon != null) {
				if (textDecorator != null) {
					textDecorator.setItem(taxon);
				}
				return taxon.getName();
			} else {
				String name = (String)node.getAttribute("name");
				if (name != null) {
					return name;
				}
				return "unlabelled";
			}
		}

		if ( tree instanceof RootedTree) {
			final RootedTree rtree = (RootedTree) tree;

			if (textDecorator != null) {
				textDecorator.setItem(node);
			}

			if (displayAttribute.equalsIgnoreCase(NODE_AGES) ) {
				return getNumberFormat().format(rtree.getHeight(node));
			} else if (displayAttribute.equalsIgnoreCase(BRANCH_LENGTHS) ) {
				return getNumberFormat().format(rtree.getLength(node));
			}
		}

		return formatValue(node.getAttribute(displayAttribute));
	}

	private String formatValue(Object value) {
		if (value != null) {
			if (value instanceof Double) {
				return getNumberFormat().format(value);
			} else if (value instanceof Object[]) {
				Object[] values = (Object[])value;

				if (values.length == 0) return null;
				if (values.length == 1) return formatValue(values[0]);

				StringBuilder builder = new StringBuilder("[");
				builder.append(formatValue(values[0]));
				for (int i = 1; i < values.length; i++) {
					builder.append(",");
					builder.append(formatValue(values[i]));
				}
				builder.append("]");
				return builder.toString();
			}
			return value.toString();
		}
		return null;
	}

	public Rectangle2D calibrate(Graphics2D g2, Node item) {
		Tree tree = treePane.getTree();

		String label = getLabel(tree, item);

		final Font oldFont = g2.getFont();
		if (textDecorator != null) {
			g2.setFont(textDecorator.getFont(getFont()));
		} else {
			g2.setFont(getFont());
		}

		FontMetrics fm = g2.getFontMetrics();
		preferredHeight = fm.getHeight();
		preferredWidth = 0;

		if (label != null) {
			Rectangle2D rect = fm.getStringBounds(label, g2);
			preferredWidth = rect.getWidth();
		}

		yOffset = (float)fm.getAscent();

		g2.setFont(oldFont);

		return new Rectangle2D.Double(0.0, 0.0, preferredWidth, preferredHeight);
	}

	public double getPreferredWidth() {
		return preferredWidth;
	}

	public double getPreferredHeight() {
		return preferredHeight;
	}

	public double getHeightBound() {
		return preferredHeight + yOffset;
	}

	public void paint(Graphics2D g2, Node item, Justification justification, Rectangle2D bounds) {
		Tree tree = treePane.getTree();

		if (TreePane.DEBUG_OUTLINE) {
			g2.setPaint(Color.red);
			g2.draw(bounds);
		}

		String label = getLabel(tree, item);

		Font oldFont = g2.getFont();

		Paint backgroundPaint = getBackground();
		Paint borderPaint = getBorderPaint();
		Stroke borderStroke = getBorderStroke();

		if (borderDecorator != null) {
			backgroundPaint = borderDecorator.getPaint(backgroundPaint);
			borderPaint = borderDecorator.getPaint(borderPaint);
			borderStroke = borderDecorator.getStroke(borderStroke);
		}

		if (backgroundPaint != null) {
			g2.setPaint(backgroundPaint);
			g2.fill(bounds);
		}

		if (borderPaint != null && borderStroke != null) {
			g2.setPaint(borderPaint);
			g2.setStroke(borderStroke);
			g2.draw(bounds);
		}

		if (textDecorator != null) {
			g2.setPaint(textDecorator.getPaint(getForeground()));
			g2.setFont(textDecorator.getFont(getFont()));
		} else {
			g2.setPaint(getForeground());
			g2.setFont(getFont());
		}

		if (label != null) {

			Rectangle2D rect = null;
			if (justification == Justification.CENTER || justification == Justification.RIGHT)
				rect = g2.getFontMetrics().getStringBounds(label, g2);

			float xOffset;
			float y = yOffset + (float) bounds.getY();
			switch (justification) {
				case CENTER:
					xOffset = (float)(-rect.getWidth()/2.0);
					y = yOffset + (float) rect.getY();
//xOffset = (float) (bounds.getX() + (bounds.getWidth() - rect.getWidth()) / 2.0);
					break;
				case FLUSH:
				case LEFT:
					xOffset = (float) bounds.getX();
					break;
				case RIGHT:
					xOffset = (float) (bounds.getX() + bounds.getWidth() - rect.getWidth());
					break;
				default:
					throw new IllegalArgumentException("Unrecognized alignment enum option");
			}

			g2.drawString(label, xOffset, y);
		}

		g2.setFont(oldFont);
	}

	public String[] getAttributes() {
		return attributes;
	}

	public void setDisplayAttribute(String displayAttribute) {
		this.displayAttribute = displayAttribute;
		firePainterChanged();
	}

    private PainterIntent intent;

	private double preferredWidth;
	private double preferredHeight;
	private float yOffset;

	protected String displayAttribute;
	protected String[] attributes;

	protected TreePane treePane;

	private Decorator textDecorator = null;
	private Decorator borderDecorator = null;

}