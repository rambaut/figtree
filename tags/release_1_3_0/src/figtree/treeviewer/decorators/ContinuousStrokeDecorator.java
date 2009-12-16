package figtree.treeviewer.decorators;

import jebl.util.Attributable;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * This decorator takes an attribute name and a set of attibutable Objects. It
 * autodetects the type of attributes and then provides a colouring scheme for them
 * based on a gradient between color1 & color2.
 *
 * @author Andrew Rambaut
 * @version $Id: ContinuousStrokeDecorator.java 822 2007-10-26 13:50:26Z rambaut $
 */
public class ContinuousStrokeDecorator implements Decorator {

	public ContinuousStrokeDecorator(ContinousScale continuousScale,
	                                float minWidth, float maxWidth) throws NumberFormatException {
		this.continuousScale = continuousScale;
		this.minWidth = minWidth;
		this.maxWidth = maxWidth;
	}

	// Decorator INTERFACE
	public Paint getPaint(Paint paint) {
		return paint;
	}

    public Paint getPaint(final Paint paint, final Point2D point1, final Point2D point2) {
        throw new UnsupportedOperationException("This decorator doesn't support gradients");
    }

    public Paint getFillPaint(Paint paint) {
		return paint;
	}

    public Paint getFillPaint(final Paint paint, final Point2D point1, final Point2D point2) {
        throw new UnsupportedOperationException("This decorator doesn't support gradients");
    }

    public Stroke getStroke(Stroke stroke) {
		if (this.stroke == null) {
			return stroke;
		}
		return this.stroke;
	}

	public Font getFont(Font font) {
		return font;
	}

    public boolean isGradient() {
        return false;
    }

    public void setItem(Object item) {
		if (item instanceof Attributable) {
			setAttributableItem((Attributable)item);
		}
	}

    public void setItems(final Object item1, final Object item2) {
        throw new UnsupportedOperationException("This decorator doesn't support gradients");
    }

    // Private methods
	private void setAttributableItem(Attributable item) {

		double value = continuousScale.getValue(item);

		if (!Double.isNaN(value)) {
			stroke = new BasicStroke((float)((value * (maxWidth - minWidth)) + minWidth));
		} else {
			stroke = null;
		}
	}

	public ContinousScale getContinuousScale() {
		return continuousScale;
	}

	private final ContinousScale continuousScale;

	private final float minWidth;
	private final float maxWidth;

	private Stroke stroke = null;
}