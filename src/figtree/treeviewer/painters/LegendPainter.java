package figtree.treeviewer.painters;

import figtree.treeviewer.AttributeColourController;
import figtree.treeviewer.TreePane;
import figtree.treeviewer.decorators.*;
import jam.controlpalettes.ControlPalette;
import jebl.evolution.trees.Tree;
import jebl.util.Attributable;
import jebl.util.NumberFormatter;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Set;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: ScaleBarPainter.java 769 2007-08-30 12:14:37Z rambaut $
 */
public class LegendPainter extends LabelPainter<TreePane> implements ScalePainter {

    public final static int CONTINUOUS_LENGTH = 320;

    public LegendPainter(AttributeColourController attributeColourController) {
        this.attributeColourController = attributeColourController;
    }

    public void setTreePane(TreePane treePane) {
        this.treePane = treePane;
    }

    public Rectangle2D calibrate(Graphics2D g2, TreePane treePane) {
        preferredHeight = 120;
        preferredWidth = 120;
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
        float y = (float)(labelBounds.getHeight() * 0.5);

        float yOffset = (float)g2.getFontMetrics().getAscent();
        float xOffset1 = (float)(labelBounds.getHeight() * 0.5);
        float yDelta = (float)labelBounds.getHeight() * 1.5F;
        float xOffset2 = (float)(labelBounds.getHeight() * 2);

        g2.setPaint(Color.BLACK);
        g2.drawString(attribute, xOffset1, y + yOffset);

        y += yDelta;

        if (colourDecorator instanceof DiscreteColorDecorator) {
            if (((DiscreteColorDecorator)colourDecorator).getValues().size() > 0) {
                ((DiscreteColorDecorator)colourDecorator).getValues().get(0).toString();
            }


            for (Object value : ((DiscreteColorDecorator)colourDecorator).getValues()) {
                g2.setPaint(((DiscreteColorDecorator)colourDecorator).getColor(value));
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

            NumberFormatter formatter = new NumberFormatter(4);
            final String label = "0.0";
            labelBounds = g2.getFontMetrics().getStringBounds(label, g2);

                    float y0 = y;

            double v = max;
            for (int i = 0; i < CONTINUOUS_LENGTH; i++) {
                g2.setPaint(((HSBContinuousColourDecorator)colourDecorator).getColour(v));
                Rectangle2D rect = new Rectangle2D.Double(xOffset1, y, labelBounds.getHeight(), 1);
                g2.fill(rect);

                y += 1;
                v -= delta;
            }

            g2.setPaint(Color.BLACK);
            g2.drawString(formatter.getFormattedValue(max), xOffset2, y0 + (yOffset / 2));
            g2.drawString(formatter.getFormattedValue(min), xOffset2, y + (yOffset / 2));

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
        setVisible(colourDecorator == null);
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

    @Override
    public String[] getAttributes() {
        return new String[0];
    }

    @Override
    public void setupAttributes(Collection<? extends Tree> trees) {
    }

    @Override
    public void setDisplayAttribute(String displayAttribute) {
    }

    @Override
    public void setTextDecorator(Decorator textDecorator) {
    }

    @Override
    public Set<Attributable> getAttributableItems() {
        return null;
    }
}