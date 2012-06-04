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
 * @version $Id: ContinuousColorDecorator.java 822 2007-10-26 13:50:26Z rambaut $
 */
public class HSBContinuousColorDecorator implements Decorator {

    public HSBContinuousColorDecorator(ContinousScale continuousScale) throws NumberFormatException {
        this(continuousScale, false);
    }

    public HSBContinuousColorDecorator(ContinousScale continuousScale,
                                       boolean isGradient) {
        this.continuousScale = continuousScale;
        this.isGradient = isGradient;
    }

    public void setup(float hueUpper, float hueLower,
                                       float saturationUpper, float saturationLower,
                                       float brightnessUpper, float brightnessLower) {
        this.hueUpper = hueUpper;
        this.hueLower = hueLower;
        this.saturationUpper = saturationUpper;
        this.saturationLower = saturationLower;
        this.brightnessUpper = brightnessUpper;
        this.brightnessLower = brightnessLower;
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

    public Paint getPaint(Paint paint, Point2D point1, Point2D point2) {
        if (colour1 != null && colour2 != null) {
            return new GradientPaint(point1, colour1, point2, colour2, false);
        } else {
            return getPaint(paint);
        }
    }

    public Paint getFillPaint(Paint paint, Point2D point1, Point2D point2) {
        if (fillColour1 != null && fillColour2 != null) {
            return new GradientPaint(point1, fillColour1, point2, fillColour2, false);
        } else {
            return getFillPaint(paint);
        }
    }

    public Stroke getStroke(Stroke stroke) {
        return stroke;
    }

    public Font getFont(Font font) {
        return font;
    }

    public boolean isGradient() {
        return isGradient;
    }

    public void setGradient(final boolean gradient) {
        isGradient = gradient;
    }

    public void setItem(Object item) {
        if (item instanceof Attributable) {
            setAttributableItem((Attributable)item);
        }
    }

    public void setItems(Object item1, Object item2) {
        if (item2 == null) {
            setItem(item1);
            return;
        }

        if (item1 == null) {
            setItem(item2);
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

    public ContinousScale getContinuousScale() {
        return continuousScale;
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
    public Color getColour(double value) {
        if (!Double.isNaN(value)) {
            return Color.getHSBColor(getHue((float)value), getSaturation((float)value), getBrightness((float)value));
        } else {
            return null;
        }
    }

    private Color getLighterColour(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 2);
    }


    private float getHue(float value) {
        return ((hueUpper - hueLower) * value) + hueLower;
    }

    private float getSaturation(float value) {
        return ((saturationUpper - saturationLower) * value) + saturationLower;
    }

    private float getBrightness(float value) {
        return ((brightnessUpper - brightnessLower) * value) + brightnessLower;
    }

    private final ContinousScale continuousScale;

    private Color paint = null;
    private Color fillPaint = null;
    private Color colour1 = null;
    private Color colour2 = null;
    private Color fillColour1 = null;
    private Color fillColour2 = null;

    private boolean isGradient;
    private float hueUpper = 1.0F;
    private float hueLower = 0.0F;
    private float saturationUpper = 0.6F;
    private float saturationLower = 0.6F;
    private float brightnessUpper = 0.8F;
    private float brightnessLower = 0.4F;

}
