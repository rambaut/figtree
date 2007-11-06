package figtree.treeviewer.decorators;

import jebl.util.Attributable;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class ContinuousGradientColorDecorator extends ContinuousColorDecorator {

	public ContinuousGradientColorDecorator(ContinousScale continuousScale, Color color1, Color color2) throws NumberFormatException {
		super(continuousScale, color1, color2);
	}

	public Paint getPaint(Paint paint, Point2D point1, Point2D point2) {
		if (colour1 != null && colour2 != null) {
			return new GradientPaint(point1, colour1, point2, colour2, false);
		} else {
			return paint;
		}
	}

	public Paint getFillPaint(Paint paint, Point2D point1, Point2D point2) {
		if (fillColour1 != null && fillColour2 != null) {
			return new GradientPaint(point1, fillColour1, point2, fillColour2, false);
		} else {
			return paint;
		}
	}

	public Stroke getStroke(Stroke stroke) {
		return stroke;
	}

	public Font getFont(Font font) {
		return font;
	}

	public void setItems(Object item1, Object item2) {
		if (item2 == null) {
			setItem(item1);
			return;
		}

		double value1 = getContinuousScale().getValue((Attributable)item1);
		double value2 = getContinuousScale().getValue((Attributable)item2);

		colour1 = getColour(value1);
		colour2 = getColour(value2);

		fillColour1 = null;
		if (colour1 != null) {
			fillColour1 = getLighterColour(colour1);
		}

		fillColour2 = null;
		if (colour2 != null) {
			fillColour2 = getLighterColour(colour2);
		}


	}

	private Color colour1 = null;
	private Color colour2 = null;
	private Color fillColour1 = null;
	private Color fillColour2 = null;
}
