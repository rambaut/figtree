/*
 * LegendPainter.java
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

import figtree.treeviewer.AttributeColourController;
import figtree.treeviewer.TreePane;
import figtree.treeviewer.decorators.*;
import jam.controlpalettes.ControlPalette;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class LegendPainter extends LabelPainter<TreePane> implements ScalePainter {

    public final static int CONTINUOUS_LENGTH = 320;

    public LegendPainter(AttributeColourController attributeColourController) {
        super(null);
        this.attributeColourController = attributeColourController;
    }

    public void setTreePane(TreePane treePane) {
        this.treePane = treePane;
    }

    public Decorator getBorderDecorator() {
        return borderDecorator;
    }

    public void setBorderDecorator(Decorator borderDecorator) {
        this.borderDecorator = borderDecorator;
        firePainterSettingsChanged();
    }

    public Decorator getTextDecorator() {
        return textDecorator;
    }

    public void setTextDecorator(Decorator textDecorator) {
        this.textDecorator = textDecorator;
        firePainterSettingsChanged();
    }

    public Rectangle2D calibrate(Graphics2D g2, TreePane treePane) {
        final Font oldFont = g2.getFont();
        if (textDecorator != null) {
            g2.setFont(textDecorator.getFont(getFont()));
        } else {
            g2.setFont(getFont());
        }

        FontMetrics fm = g2.getFontMetrics();
        preferredHeight = fm.getHeight();
        preferredWidth = 0;

        if (colourDecorator instanceof DiscreteColourDecorator) {
            for (Object value : ((DiscreteColourDecorator)colourDecorator).getValues()) {
                Rectangle2D rect = fm.getStringBounds(value.toString(), g2);
                if (rect.getWidth() > preferredWidth) {
                    preferredWidth = rect.getWidth();
                }
            }
        } else if (colourDecorator instanceof ContinuousColourDecorator){
            // draw a continuous legend
            ContinuousScale scale = ((ContinuousColourDecorator)colourDecorator).getContinuousScale();

            double min = scale.getMinValue();
            double max = scale.getMaxValue();

            Rectangle2D rect = g2.getFontMetrics().getStringBounds(getNumberFormat().format(max), g2);
            if (rect.getWidth() > preferredWidth) {
                preferredWidth = rect.getWidth();
            }

             rect = g2.getFontMetrics().getStringBounds(getNumberFormat().format(min), g2);
            if (rect.getWidth() > preferredWidth) {
                preferredWidth = rect.getWidth();
            }

        } else if (colourDecorator != null) {
            throw new IllegalArgumentException("Unrecognized ColourDecorator class");
        }

        g2.setFont(oldFont);

        preferredWidth += insets.left + insets.right + fm.getHeight() * 2;
        return new Rectangle2D.Double(0.0, 0.0, preferredWidth, preferredHeight);
    }

    public void paint(Graphics2D g2, TreePane treePane, Justification justification, Rectangle2D bounds) {
        if (colourDecorator == null) {
            return;
        }
        String attribute = colourDecorator.getAttributeName();

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

        Rectangle2D labelBounds = g2.getFontMetrics().getStringBounds("ty", g2);

        // vertical inset
        float y = (float)(insets.top);

        float yOffset = (float)g2.getFontMetrics().getAscent();
        float xOffset1 = (float)(insets.left);
        float yDelta = (float)labelBounds.getHeight() * 1.5F;
        float xOffset2 = (float)(insets.left + labelBounds.getHeight() * 2);

        g2.setPaint(Color.BLACK);
        g2.drawString(attribute, xOffset1, y + yOffset);

        y += yDelta;

        if (colourDecorator instanceof DiscreteColourDecorator) {
            for (Object value : ((DiscreteColourDecorator)colourDecorator).getValues()) {
                g2.setPaint(((DiscreteColourDecorator)colourDecorator).getColor(value));
                Rectangle2D rect = new Rectangle2D.Double(xOffset1, y, labelBounds.getHeight(), labelBounds.getHeight());
                g2.fill(rect);
                g2.drawString(value.toString(), xOffset2, y + yOffset);

                y += yDelta;
            }
        } else if (colourDecorator instanceof ContinuousColourDecorator){
            // draw a continuous legend
            ContinuousScale scale = ((ContinuousColourDecorator)colourDecorator).getContinuousScale();

            double min = scale.getMinValue();
            double max = scale.getMaxValue();
            double delta = (max - min) / CONTINUOUS_LENGTH;

            final String label = "0.0";
            labelBounds = g2.getFontMetrics().getStringBounds(label, g2);

            float y0 = y;

            double v = max;
            for (int i = 0; i < CONTINUOUS_LENGTH; i++) {
                g2.setPaint(((ContinuousColourDecorator)colourDecorator).getColourForValue(v));
                Rectangle2D rect = new Rectangle2D.Double(xOffset1, y, labelBounds.getHeight(), 1);
                g2.fill(rect);

                y += 1;
                v -= delta;
            }

            g2.setPaint(Color.BLACK);
            g2.drawString(getNumberFormat().format(max), xOffset2, y0 + (yOffset / 2));
            g2.drawString(getNumberFormat().format(min), xOffset2, y + (yOffset / 2));

            Line2D line = new Line2D.Double(xOffset1, y0, xOffset1 + labelBounds.getHeight() * 1.25, y0);
            g2.draw(line);

            line = new Line2D.Double(xOffset1, y, xOffset1 + labelBounds.getHeight() * 1.25, y);
            g2.draw(line);

            line = new Line2D.Double(xOffset1, y0, xOffset1, y);
            g2.draw(line);

        } else {
            throw new IllegalArgumentException("Unrecognized ColourDecorator class");
        }
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
        return preferredHeight;
    }

    public void setColourDecorator(ColourDecorator colourDecorator) {
        this.colourDecorator = colourDecorator;
        firePainterChanged();
    }

    public void setControlPalette(ControlPalette controlPalette) {
        // nothing to do
    }

    protected ColourDecorator colourDecorator;

    private double preferredHeight;
    private double preferredWidth;

    protected final AttributeColourController attributeColourController;

    protected TreePane treePane;

    private Decorator textDecorator = null;
    private Decorator borderDecorator = null;

    private Insets insets = new Insets(8, 8, 8, 8);

    @Override
    public void setDisplayAttribute(String displayAttribute) {
        throw new UnsupportedOperationException("setDisplayAttribute not implmented - use setColourDecorator directly");
    }

}