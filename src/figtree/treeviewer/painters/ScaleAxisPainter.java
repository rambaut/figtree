/*
 * ScaleAxisPainter.java
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

package figtree.treeviewer.painters;

import figtree.treeviewer.ScaleAxis;
import figtree.treeviewer.TreePane;
import figtree.treeviewer.decorators.Decorator;
import figtree.treeviewer.treelayouts.RectilinearTreeLayout;
import figtree.treeviewer.treelayouts.RadialTreeLayout;
import jebl.evolution.trees.Tree;
import jebl.util.Attributable;
import jam.controlpalettes.ControlPalette;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Set;

/**
 * @author Andrew Rambaut
 * @version $Id: ScaleBarPainter.java,v 1.7 2006/11/21 16:10:24 rambaut Exp $
 */
public class ScaleAxisPainter extends LabelPainter<TreePane> implements ScalePainter {
	private double majorTickSpacing = 1.0;
    private double minorTickSpacing = 0.5;

    public ScaleAxisPainter() {
        super(null);
	}

	public void setTreePane(TreePane treePane) {
		this.treePane = treePane;
	}

    public void setAutomatic(boolean automatic) {
        if (automatic) {
            treePane.setAutomaticScale();
            majorTickSpacing = treePane.getMajorTickSpacing();
            minorTickSpacing = treePane.getMinorTickSpacing();
        } else {
            treePane.setTickSpacing(majorTickSpacing, minorTickSpacing);
        }

    }

    public void setAxisReversed(final boolean isAxisReversed) {
        treePane.setAxisReversed(isAxisReversed);
    }

    public void setAxisSpacing(double majorTickSpacing, double minorTickSpacing) {
        treePane.setTickSpacing(majorTickSpacing, minorTickSpacing);
    }

	public double getAxisOrigin() {
	    return 0.0;
	}

	public void setAxisOrigin(double axisOrigin) {
		//
	}

    public double getMajorTickSpacing() {
        return majorTickSpacing;
    }

    public double getMinorTickSpacing() {
        return minorTickSpacing;
    }

    public Rectangle2D calibrate(Graphics2D g2, TreePane treePane) {
		Font oldFont = g2.getFont();
		g2.setFont(getFont());

		FontMetrics fm = g2.getFontMetrics();
		double labelHeight = fm.getHeight();

		preferredWidth = treePane.getTreeBounds().getWidth();
		preferredHeight = labelHeight + topMargin + bottomMargin + scaleBarStroke.getLineWidth() + majorTickSize;

	    if (!(treePane.getTreeLayout() instanceof RectilinearTreeLayout)) {
		    // if the tree layout is not rectilinear, we are not going to display
		    // an axis at the moment so make the height 0.
		    preferredHeight = 0.0;
	    }

		tickLabelOffset = (float) (fm.getAscent() + topMargin + bottomMargin + majorTickSize) + scaleBarStroke.getLineWidth();

		g2.setFont(oldFont);

		return new Rectangle2D.Double(0.0, 0.0, preferredWidth, preferredHeight);
	}

	public void paint(Graphics2D g2, TreePane treePane, Justification justification, Rectangle2D bounds) {
		Font oldFont = g2.getFont();
		Paint oldPaint = g2.getPaint();
		Stroke oldStroke = g2.getStroke();

		if (TreePane.DEBUG_OUTLINE) {
			g2.setPaint(Color.red);
			g2.draw(bounds);
		}

		if (treePane.getTreeLayout() instanceof RadialTreeLayout) {
			// Unless the layout is the rectilinear or polar one, the grid won't make sense...
			return;
		}

		if (getBackground() != null) {
			g2.setPaint(getBackground());
			g2.fill(bounds);
		}

		if (getBorderPaint() != null && getBorderStroke() != null) {
			g2.setPaint(getBorderPaint());
			g2.setStroke(getBorderStroke());
			g2.draw(bounds);
		}

		g2.setFont(getFont());

		g2.setPaint(getForeground());
		g2.setStroke(getScaleBarStroke());

		paintAxis(g2, bounds);

		g2.setFont(oldFont);
		g2.setPaint(oldPaint);
		g2.setStroke(oldStroke);
	}

	/**
	 *	Get the maximum width of the labels of an axis
	 * @param g2
	 * @return
	 */
	protected double getMaxTickLabelWidth(Graphics2D g2)
	{
		String label;
		double width;
		double maxWidth = 0;

		ScaleAxis axis = treePane.getScaleAxis();

		if (axis.getLabelFirst()) { // Draw first minor tick as a major one (with a label)
			label = axis.getFormatter().format(axis.getMinorTickValue(0, -1));
			width = g2.getFontMetrics().stringWidth(label);
			if (maxWidth < width)
				maxWidth = width;
		}
		int n = axis.getMajorTickCount();
		for (int i = 0; i < n; i++) {
			label = axis.getFormatter().format(axis.getMajorTickValue(i));
			width = g2.getFontMetrics().stringWidth(label);
			if (maxWidth < width)
				maxWidth = width;
		}
		if (axis.getLabelLast()) { // Draw first minor tick as a major one (with a label)
			label = axis.getFormatter().format(axis.getMinorTickValue(0, n - 1));
			width = g2.getFontMetrics().stringWidth(label);
			if (maxWidth < width)
				maxWidth = width;
		}

		return maxWidth;
	}

	protected void paintAxis(Graphics2D g2, Rectangle2D axisBounds)
	{
		ScaleAxis axis = treePane.getScaleAxis();

		g2.setPaint(getForeground());
		g2.setStroke(getScaleBarStroke());

		double minX = treePane.scaleOnAxis(axis.getMinAxis());
		double maxX = treePane.scaleOnAxis(axis.getMaxAxis());

		Line2D line = new Line2D.Double(minX, axisBounds.getMinY(), maxX, axisBounds.getMinY());
		g2.draw(line);

		int n1 = axis.getMajorTickCount();
		int n2, i, j;

		n2 = axis.getMinorTickCount(-1);
		if (axis.getLabelFirst()) { // Draw first minor tick as a major one (with a label)

			paintMajorTick(g2, axisBounds, axis, axis.getMinorTickValue(0, -1));

			for (j = 1; j < n2; j++) {
				paintMinorTick(g2, axisBounds, axis.getMinorTickValue(j, -1));
			}
		} else {

			for (j = 0; j < n2; j++) {
				paintMinorTick(g2, axisBounds, axis.getMinorTickValue(j, -1));
			}
		}

		for (i = 0; i < n1; i++) {

			paintMajorTick(g2, axisBounds, axis, axis.getMajorTickValue(i));
			n2 = axis.getMinorTickCount(i);

			if (i == (n1-1) && axis.getLabelLast()) { // Draw last minor tick as a major one

				paintMajorTick(g2, axisBounds, axis, axis.getMinorTickValue(0, i));

				for (j = 1; j < n2; j++) {
					paintMinorTick(g2, axisBounds, axis.getMinorTickValue(j, i));
				}
			} else {

				for (j = 0; j <  n2; j++) {
					paintMinorTick(g2, axisBounds, axis.getMinorTickValue(j, i));
				}
			}
		}
	}

	protected void paintMajorTick(Graphics2D g2, Rectangle2D axisBounds, ScaleAxis axis, double value)
	{
		g2.setPaint(getForeground());
		g2.setStroke(getScaleBarStroke());

		String label = axis.getFormatter().format(value);
		double pos = treePane.scaleOnAxis(value);

		Line2D line = new Line2D.Double(pos, axisBounds.getMinY(), pos, axisBounds.getMinY() + majorTickSize);
		g2.draw(line);

		g2.setPaint(getForeground());
		double width = g2.getFontMetrics().stringWidth(label);
		g2.drawString(label, (float)(pos - (width / 2)), (float)(axisBounds.getMinY() + tickLabelOffset));
	}

	protected void paintMinorTick(Graphics2D g2, Rectangle2D axisBounds, double value)
	{

		g2.setPaint(getForeground());
		g2.setStroke(getScaleBarStroke());

		double pos = treePane.scaleOnAxis(value);

		Line2D line = new Line2D.Double(pos, axisBounds.getMinY(), pos, axisBounds.getMinY() + minorTickSize);
		g2.draw(line);
	}

	public double getPreferredWidth() {
		return preferredWidth;
	}

	public double getPreferredHeight() {
		return preferredHeight;
	}

	public double getHeightBound() {
		return preferredHeight + tickLabelOffset;
	}

	public BasicStroke getScaleBarStroke() {
		return scaleBarStroke;
	}

	public void setScaleBarStroke(BasicStroke scaleBarStroke) {
		this.scaleBarStroke = scaleBarStroke;
		firePainterChanged();
	}

	public void setControlPalette(ControlPalette controlPalette) {
		// nothing to do
	}

	public String[] getAttributes() {
		return new String[0];
	}

	public void setupAttributes(Collection<? extends Tree> trees) {
		// nothing to do...
	}

	public void setDisplayAttribute(String displayAttribute) {
        throw new UnsupportedOperationException("setDisplayAttribute not implemented in ScaleAxisPainter");
	}

    public void setTextDecorator(Decorator textDecorator) {
    }

    public Set<Attributable> getAttributableItems() {
        return null;
    }

    private BasicStroke scaleBarStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

	private double topMargin = 4.0;
	private double bottomMargin = 4.0;

	private double majorTickSize = 5.0;
	private double minorTickSize = 2.0;
	private double tickLabelOffset = 4.0;

	private double preferredHeight;
	private double preferredWidth;

	protected TreePane treePane;

}
