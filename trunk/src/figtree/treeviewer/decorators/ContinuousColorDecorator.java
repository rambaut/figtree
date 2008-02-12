package figtree.treeviewer.decorators;

import jebl.util.Attributable;

import java.awt.*;

/**
 * This decorator takes an attribute name and a set of attibutable Objects. It
 * autodetects the type of attributes and then provides a colouring scheme for them
 * based on a gradient between color1 & color2.
 *
 * @author Andrew Rambaut
 * @version $Id: ContinuousColorDecorator.java 822 2007-10-26 13:50:26Z rambaut $
 */
public class ContinuousColorDecorator implements Decorator {

	public ContinuousColorDecorator(ContinousScale continuousScale,
	                                Color color1, Color color2) throws NumberFormatException {
		this.continuousScale = continuousScale;
		this.color1 = new float[4];
		color1.getRGBComponents(this.color1);
		this.color2 = new float[4];
		color2.getRGBComponents(this.color2);
	}

	// Decorator INTERFACE
	public Paint getPaint(Paint paint) {
		if (this.paint == null) return paint;
		return this.paint;
	}

	public Paint getFillPaint(Paint paint) {
		if (this.fillPaint == null) return paint;
		return fillPaint;
	}

	public Stroke getStroke(Stroke stroke) {
		return stroke;
	}

	public Font getFont(Font font) {
		return font;
	}

	public void setItem(Object item) {
		if (item instanceof Attributable) {
			setAttributableItem((Attributable)item);
		}
	}

	// Private methods
	private void setAttributableItem(Attributable item) {
		double value = continuousScale.getValue((Attributable)item);

		Color colour = getColour(value);

		Color fillColour = null;
		if (colour != null) {
			fillColour = getLighterColour(colour);
		}
		if (colour != null) {
			paint = colour;
			fillPaint = fillColour;
		} else {
			paint = null;
			fillPaint = null;
		}
	}

	// Private methods
	protected Color getColour(double value) {
		if (!Double.isNaN(value)) {
			float p = (float)value;
			float q = 1.0F - p;

			return new Color(
					color2[0] * p + color1[0] * q,
					color2[1] * p + color1[1] * q,
					color2[2] * p + color1[2] * q,
					color2[3] * p + color1[3] * q);
		} else {
			return null;
		}
	}

	protected Color getLighterColour(Color color) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 2);
	}

	public ContinousScale getContinuousScale() {
		return continuousScale;
	}

	private final ContinousScale continuousScale;

	private final float[] color1;
	private final float[] color2;

	private Color paint = null;
	private Color fillPaint = null;
}
