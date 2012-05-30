package figtree.treeviewer.painters;

import figtree.treeviewer.ScaleAxis;
import figtree.treeviewer.TreePane;
import figtree.treeviewer.decorators.Decorator;
import figtree.treeviewer.decorators.DiscreteColorDecorator;
import jam.controlpalettes.ControlPalette;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;
import jebl.util.Attributable;

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

    public LegendPainter() {
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
        g2.setPaint(Color.BLACK);
            g2.draw(bounds);

//        Font oldFont = g2.getFont();
//        Paint oldPaint = g2.getPaint();
//        Stroke oldStroke = g2.getStroke();
//
//        if (getBackground() != null) {
//            g2.setPaint(getBackground());
//            g2.fill(bounds);
//        }
//
//        if (getBorderPaint() != null && getBorderStroke() != null) {
//            g2.setPaint(getBorderPaint());
//            g2.setStroke(getBorderStroke());
//            g2.draw(bounds);
//        }
//
//        g2.setFont(getFont());
//
//        // we don't need accuracy but a nice short number
//        final String label = Double.toString(scaleRange);
//
//        Rectangle2D rect = g2.getFontMetrics().getStringBounds(label, g2);
//
//        double x1, x2;
//        float xOffset;
//        switch (justification) {
//            case CENTER:
//                xOffset = (float) (bounds.getX() + (bounds.getWidth() - rect.getWidth()) / 2.0);
//                x1 = (bounds.getX() + (bounds.getWidth() - preferredWidth) / 2.0);
//                x2 = x1 + preferredWidth;
//                break;
//            case FLUSH:
//            case LEFT:
//                xOffset = (float) bounds.getX();
//                x1 = bounds.getX();
//                x2 = x1 + preferredWidth;
//                break;
//            case RIGHT:
//                xOffset = (float) (bounds.getX() + bounds.getWidth() - rect.getWidth());
//                x2 = bounds.getX() + bounds.getWidth();
//                x1 = x2 - preferredWidth;
//                break;
//            default:
//                throw new IllegalArgumentException("Unrecognized alignment enum option");
//        }
//
//        g2.setPaint(getForeground());
//        g2.setStroke(getScaleBarStroke());
//
//        g2.draw(new Line2D.Double(x1, bounds.getY() + topMargin, x2, bounds.getY() + topMargin));
//
//        g2.drawString(label, xOffset, yOffset + (float) bounds.getY());
//
//        g2.setFont(oldFont);
//        g2.setPaint(oldPaint);
//        g2.setStroke(oldStroke);
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

    public DiscreteColorDecorator getDecorator() {
        return decorator;
    }

    public void setDecorator(DiscreteColorDecorator decorator) {
        this.decorator = decorator;
        firePainterChanged();
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
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
        throw new UnsupportedOperationException("setDisplayAttribute not implemented in ScaleBarPainter");
    }

    public void setTextDecorator(Decorator textDecorator) {
    }

    public Set<Attributable> getAttributableItems() {
        return null;
    }


    private DiscreteColorDecorator decorator;

    private String attribute;

    private double topMargin = 4.0;
    private double bottomMargin = 4.0;

    private double preferredHeight;
    private double preferredWidth;

    private float yOffset;

    protected TreePane treePane;
}