package figtree.treeviewer.decorators;

import jebl.util.Attributable;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Set;

/**
 * This decorator takes an attribute name and a set of attibutable Objects. It
 * autodetects the type of attributes and then provides a colouring scheme for them
 * based on a gradient between color1 & color2.
 *
 * @author Andrew Rambaut
 * @version $Id: ContinuousColourDecorator.java 822 2007-10-26 13:50:26Z rambaut $
 */
public abstract class ContinuousColourDecorator extends ColourDecorator {

    public ContinuousColourDecorator(String attributeName) {
        super(attributeName);
    }

    public ContinuousColourDecorator(ContinuousScale continuousScale ) {
        super(continuousScale.getAttributeName());
        this.continuousScale = continuousScale;
    }

    public void setAttributes(String attributeName, Set<? extends Attributable> items) {
        super.setAttributes(attributeName, items);
        continuousScale.setAttributes(attributeName, items);
    }

    public ContinuousScale getContinuousScale() {
        return continuousScale;
    }

    public void setContinuousScale(ContinuousScale continuousScale) {
        this.continuousScale = continuousScale;
    }

    @Override
    public Color getColourForValue(Object value) {
        return getColourForScaledValue(continuousScale.getValue(value));
    }

    // Private methods
    protected abstract Color getColourForScaledValue(double value);

    private ContinuousScale continuousScale;
}
