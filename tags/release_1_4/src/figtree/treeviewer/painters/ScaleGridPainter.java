/*
 * ScaleGridPainter.java
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
import figtree.treeviewer.treelayouts.RadialTreeLayout;
import jebl.evolution.trees.Tree;
import jebl.util.Attributable;
import jam.controlpalettes.ControlPalette;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Set;

/**
 * @author Andrew Rambaut
 * @version $Id: ScaleBarPainter.java,v 1.7 2006/11/21 16:10:24 rambaut Exp $
 */
public class ScaleGridPainter extends LabelPainter<TreePane> implements ScalePainter {

	public ScaleGridPainter() {
        super(null);
	}

	public void setTreePane(TreePane treePane) {
		this.treePane = treePane;
	}

	public Rectangle2D calibrate(Graphics2D g2, TreePane treePane) {
		preferredWidth = treePane.getTreeBounds().getWidth();
		preferredHeight = treePane.getTreeBounds().getHeight();

		return new Rectangle2D.Double(0.0, 0.0, preferredWidth, preferredHeight);
	}

	public void paint(Graphics2D g2, TreePane treePane, Justification justification, Rectangle2D bounds) {

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

		paintAxis(g2, bounds);

		g2.setPaint(oldPaint);
		g2.setStroke(oldStroke);
	}


	protected void paintAxis(Graphics2D g2, Rectangle2D axisBounds)
	{
		ScaleAxis axis = treePane.getScaleAxis();

		g2.setPaint(getForeground());

		int n1 = axis.getMajorTickCount();
		int n2, i, j;

		n2 = axis.getMinorTickCount(-1);
		if (axis.getLabelFirst()) { // Draw first minor tick as a major one (with a label)

			paintMajorLine(g2, axisBounds, axis, axis.getMinorTickValue(0, -1));

			for (j = 1; j < n2; j++) {
				paintMinorLine(g2, axisBounds, axis.getMinorTickValue(j, -1));
			}
		} else {

			for (j = 0; j < n2; j++) {
				paintMinorLine(g2, axisBounds, axis.getMinorTickValue(j, -1));
			}
		}

		for (i = 0; i < n1; i++) {

			paintMajorLine(g2, axisBounds, axis, axis.getMajorTickValue(i));
			n2 = axis.getMinorTickCount(i);

			if (i == (n1-1) && axis.getLabelLast()) { // Draw last minor tick as a major one

				paintMajorLine(g2, axisBounds, axis, axis.getMinorTickValue(0, i));

				for (j = 1; j < n2; j++) {
					paintMinorLine(g2, axisBounds, axis.getMinorTickValue(j, i));
				}
			} else {

				for (j = 0; j <  n2; j++) {
					paintMinorLine(g2, axisBounds, axis.getMinorTickValue(j, i));
				}
			}
		}
	}

	protected void paintMajorLine(Graphics2D g2, Rectangle2D axisBounds, ScaleAxis axis, double value) {
        g2.setStroke(getMajorStroke());
		Shape line = treePane.getAxisLine(value);
		g2.draw(line);
	}

	protected void paintMinorLine(Graphics2D g2, Rectangle2D axisBounds, double value) {
        g2.setStroke(getMinorStroke());
		Shape line = treePane.getAxisLine(value);
		g2.draw(line);
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

    public BasicStroke getMajorStroke() {
        return majorStroke;
    }

    public void setMajorStroke(BasicStroke majorStroke) {
        this.majorStroke = majorStroke;
        firePainterChanged();
    }

    public BasicStroke getMinorStroke() {
        return minorStroke;
    }

    public void setMinorStroke(BasicStroke minorStroke) {
        this.minorStroke = minorStroke;
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
        throw new UnsupportedOperationException("setDisplayAttribute not implemented in ScaleGridPainter");
	}

    public void setTextDecorator(Decorator textDecorator) {
    }

    public Set<Attributable> getAttributableItems() {
        return null;
    }

    private BasicStroke majorStroke = new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    private BasicStroke minorStroke = new BasicStroke(0.25f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

	private double preferredHeight;
	private double preferredWidth;

	protected TreePane treePane;
}