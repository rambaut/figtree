package figtree.treeviewer.painters;

import figtree.treeviewer.TreePane;
import figtree.treeviewer.decorators.*;
import jam.controlpalettes.ControlPalette;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.Tree;
import jebl.util.Attributable;
import jebl.util.NumberFormatter;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: ScaleBarPainter.java 769 2007-08-30 12:14:37Z rambaut $
 */
public class LegendPainter extends LabelPainter<TreePane> implements ScalePainter {

    public final static int CONTINUOUS_LENGTH = 320;

    public LegendPainter() {
        setupAttributes(null);

        if (this.displayAttribute == null) {
            this.displayAttribute = attributes[0];
        } else {
            this.displayAttribute = "";
        }
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
        Decorator decorator = treePane.getDecoratorForAttribute(displayAttribute);

        if (decorator == null) {
            return;
        }

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

        if (decorator instanceof DiscreteColorDecorator) {
            final String label = ((DiscreteColorDecorator)decorator).getValues().get(0).toString();

            Rectangle2D labelBounds = g2.getFontMetrics().getStringBounds(label, g2);
            float yOffset = (float)g2.getFontMetrics().getAscent();

            float xOffset1 = (float)(labelBounds.getHeight() * 0.5);
            float xOffset2 = (float)(labelBounds.getHeight() * 2);
            float y = (float)(labelBounds.getHeight() * 0.5);


            for (Object value : ((DiscreteColorDecorator)decorator).getValues()) {
                g2.setPaint(((DiscreteColorDecorator)decorator).getColor(value));
                Rectangle2D rect = new Rectangle2D.Double(xOffset1, y, labelBounds.getHeight(), labelBounds.getHeight());
                g2.fill(rect);
                g2.drawString(value.toString(), xOffset2, y + yOffset);

                y += labelBounds.getHeight() * 1.5F;
            }
        } else {
            // draw a continuous legend


            ContinuousScale scale = ((HSBContinuousColorDecorator)decorator).getContinuousScale();

            double min = scale.getMinValue();
            double max = scale.getMaxValue();
            double delta = (max - min) / CONTINUOUS_LENGTH;

            NumberFormatter formatter = new NumberFormatter(4);
            final String label = "0.0";
            Rectangle2D labelBounds = g2.getFontMetrics().getStringBounds(label, g2);

            float xOffset1 = (float)(labelBounds.getHeight() * 0.5);
            float y = (float)(labelBounds.getHeight() * 0.5);

            double v = min;
            for (int i = 0; i < CONTINUOUS_LENGTH; i++) {
                g2.setPaint(((HSBContinuousColorDecorator)decorator).getColour(v));
                Rectangle2D rect = new Rectangle2D.Double(xOffset1, y, labelBounds.getHeight(), 1);
                 g2.fill(rect);

                y += 1;
                v += delta;
            }

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

    public String getDisplayAttribute() {
        return displayAttribute;
    }

    public void setDisplayAttribute(String displayAttribute) {
        this.displayAttribute = displayAttribute;
        firePainterChanged();
    }

    public void setControlPalette(ControlPalette controlPalette) {
        // nothing to do
    }

    public String[] getAttributes() {
        return attributes;
    }

    public void setupAttributes(Collection<? extends Tree> trees) {

        java.util.List<String> attributeNames = new ArrayList<String>();

        attributableItems.clear();

        Set<String> nodeAttributes = new TreeSet<String>();
        if (trees != null) {
            for (Tree tree : trees) {
                for (Node node : tree.getNodes()) {
                    attributableItems.add(node);
                    nodeAttributes.addAll(node.getAttributeNames());
                }
                for (Taxon taxon : tree.getTaxa()) {
                    attributableItems.add(taxon);
                    nodeAttributes.addAll(taxon.getAttributeNames());
                }
            }
        }

        attributeNames.add(BasicLabelPainter.NODE_AGES);
        attributeNames.add(BasicLabelPainter.NODE_HEIGHTS);
        attributeNames.add(BasicLabelPainter.BRANCH_TIMES);
        attributeNames.add(BasicLabelPainter.BRANCH_LENGTHS);

        for (String attributeName : nodeAttributes) {
            if (!attributeName.startsWith("!")) {
                attributeNames.add(attributeName);
            }
        }

        this.attributes = new String[attributeNames.size()];
        attributeNames.toArray(this.attributes);

        fireAttributesChanged();
    }

    public void setTextDecorator(Decorator textDecorator) {
    }

    public Set<Attributable> getAttributableItems() {
        return attributableItems;
    }

    protected String displayAttribute;
    protected String[] attributes;
    private Set<Attributable> attributableItems = new HashSet<Attributable>();

    private double preferredHeight;
    private double preferredWidth;

    protected TreePane treePane;
}