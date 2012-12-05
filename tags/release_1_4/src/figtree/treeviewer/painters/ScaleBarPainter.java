/*
 * ScaleBarPainter.java
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

import figtree.treeviewer.*;
import figtree.treeviewer.decorators.Decorator;
import jebl.evolution.trees.RootedTree;
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
 * @version $Id: ScaleBarPainter.java 769 2007-08-30 12:14:37Z rambaut $
 */
public class ScaleBarPainter extends LabelPainter<TreePane> implements ScalePainter {

    public enum ScaleBarType {
        BAR,
        AXIS
    }

    public ScaleBarPainter() {
        this(0.0);
    }

    public ScaleBarPainter(double scaleRange) {
        super(null);
        this.scaleRange = scaleRange;
        type = ScaleBarType.BAR;
    }

    public void setTreePane(TreePane treePane) {
        this.treePane = treePane;
    }

    public Rectangle2D calibrate(Graphics2D g2, TreePane treePane) {
        Font oldFont = g2.getFont();
        g2.setFont(getFont());

        FontMetrics fm = g2.getFontMetrics();
        double labelHeight = fm.getHeight();

        preferredWidth = treePane.getTreeScale() * scaleRange;
        preferredHeight = labelHeight + topMargin + bottomMargin + scaleBarStroke.getLineWidth();

        yOffset = (float) (fm.getAscent() + topMargin + bottomMargin) + scaleBarStroke.getLineWidth();

        g2.setFont(oldFont);

        return new Rectangle2D.Double(0.0, 0.0, preferredWidth, preferredHeight);
    }

    public void paint(Graphics2D g2, TreePane treePane, Justification justification, Rectangle2D bounds) {
        Font oldFont = g2.getFont();
        Paint oldPaint = g2.getPaint();
        Stroke oldStroke = g2.getStroke();

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

        // we don't need accuracy but a nice short number
        final String label = Double.toString(scaleRange);

        Rectangle2D rect = g2.getFontMetrics().getStringBounds(label, g2);

        double x1, x2;
        float xOffset;
        switch (justification) {
            case CENTER:
                xOffset = (float) (bounds.getX() + (bounds.getWidth() - rect.getWidth()) / 2.0);
                x1 = (bounds.getX() + (bounds.getWidth() - preferredWidth) / 2.0);
                x2 = x1 + preferredWidth;
                break;
            case FLUSH:
            case LEFT:
                xOffset = (float) bounds.getX();
                x1 = bounds.getX();
                x2 = x1 + preferredWidth;
                break;
            case RIGHT:
                xOffset = (float) (bounds.getX() + bounds.getWidth() - rect.getWidth());
                x2 = bounds.getX() + bounds.getWidth();
                x1 = x2 - preferredWidth;
                break;
            default:
                throw new IllegalArgumentException("Unrecognized alignment enum option");
        }

        g2.setPaint(getForeground());
        g2.setStroke(getScaleBarStroke());

        g2.draw(new Line2D.Double(x1, bounds.getY() + topMargin, x2, bounds.getY() + topMargin));

        g2.drawString(label, xOffset, yOffset + (float) bounds.getY());

        g2.setFont(oldFont);
        g2.setPaint(oldPaint);
        g2.setStroke(oldStroke);
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

    public ScaleBarType getType() {
        return type;
    }

    public void setType(ScaleBarType type) {
        this.type = type;
        firePainterChanged();

    }

    public BasicStroke getScaleBarStroke() {
        return scaleBarStroke;
    }

    public void setScaleBarStroke(BasicStroke scaleBarStroke) {
        this.scaleBarStroke = scaleBarStroke;
        firePainterChanged();
    }

    public double getScaleRange() {
        return scaleRange;
    }

    public void setScaleRange(double scaleRange) {
        this.userScaleRange = scaleRange;
        calculateScaleRange();
        firePainterChanged();
    }

    public void setAutomaticScale(boolean automaticScale) {
        this.automaticScale = automaticScale;
        calculateScaleRange();
        firePainterChanged();
    }

    public void setControlPalette(ControlPalette controlPalette) {
        // nothing to do
    }

    public void calculateScaleRange() {
        if( !automaticScale && userScaleRange != 0.0 ) {
            scaleRange = userScaleRange;
        } else {
            RootedTree tree = treePane.getTree();
            if (tree != null) {
                final double treeHeight = tree.getHeight(tree.getRootNode());

                if( treeHeight == 0.0 ) {
                    scaleRange = 0.0;
                } else {

                    double low = treeHeight / 10.0;
                    double b = -(Math.ceil(Math.log10(low)) - 1);
                    for(int n = 0; n < 3; ++n) {
                        double factor = Math.pow(10, b);
                        double x = ((int)(low * factor) + 1)/factor;
                        if( n == 2 || x < treeHeight / 5.0 ) {
                            scaleRange = x;
                            break;
                        }
                        ++b;
                    }
                }
            }
        }

    }

    public String[] getAttributes() {
        return new String[0];
    }

    public void setupAttributes(Collection<? extends Tree> trees) {
        // nothing to do...
    }

    public void setDisplayAttribute(String displayAttribute) {
        throw new UnsupportedOperationException("setDisplayAttribute not implemented in ScaleBarPainter");
    }

    public void setTextDecorator(Decorator textDecorator) {
    }

    public Set<Attributable> getAttributableItems() {
        return null;
    }

    private BasicStroke scaleBarStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

    private ScaleBarType type = ScaleBarType.BAR;
    private ScaleAxis axis = new ScaleAxis(ScaleAxis.AT_DATA, ScaleAxis.AT_DATA);

    private double scaleRange;
    private double topMargin = 4.0;
    private double bottomMargin = 4.0;
    private double userScaleRange = 0.0;
    private boolean automaticScale = true;

    private double preferredHeight;
    private double preferredWidth;

    private float yOffset;

    protected TreePane treePane;
}