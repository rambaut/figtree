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
public class ContinuousColorDecorator implements Decorator {

    public ContinuousColorDecorator(ContinousScale continuousScale,
                                    Color color1, Color color2,
                                    boolean isGradient) throws NumberFormatException {
        this.continuousScale = continuousScale;
        this.color1 = new float[4];
        color1.getRGBComponents(this.color1);
        this.color2 = new float[4];
        color2.getRGBComponents(this.color2);
        this.color3 = null;
        this.isGradient = isGradient;
    }

    public ContinuousColorDecorator(ContinousScale continuousScale,
                                    Color color1, Color color2, Color color3,
                                    boolean isGradient) throws NumberFormatException {
        this.continuousScale = continuousScale;
        this.color1 = new float[4];
        color1.getRGBComponents(this.color1);
        this.color2 = new float[4];
        color2.getRGBComponents(this.color2);
        this.color3 = new float[4];
        color3.getRGBComponents(this.color3);
        this.isGradient = isGradient;
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
    private Color getColour(double value) {
        if (!Double.isNaN(value)) {
            if (color3 != null) {
                if (value < 0.5) {
                    float p = (float)value * 2;
                    float q = 1.0F - p;

                    return new Color(
                            color2[0] * p + color1[0] * q,
                            color2[1] * p + color1[1] * q,
                            color2[2] * p + color1[2] * q,
                            color2[3] * p + color1[3] * q);
                } else {
                    float p = (float)(value - 0.5) * 2;
                    float q = 1.0F - p;

                    return new Color(
                            color3[0] * p + color2[0] * q,
                            color3[1] * p + color2[1] * q,
                            color3[2] * p + color2[2] * q,
                            color3[3] * p + color2[3] * q);
                }
            } else {
                float p = (float)value;
                float q = 1.0F - p;

                return new Color(
                        color2[0] * p + color1[0] * q,
                        color2[1] * p + color1[1] * q,
                        color2[2] * p + color1[2] * q,
                        color2[3] * p + color1[3] * q);
            }
        } else {
            return null;
        }
    }

    private Color getLighterColour(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 2);
    }

    private final ContinousScale continuousScale;

    private final float[] color1;
    private final float[] color2;
    private final float[] color3;

    private Color paint = null;
    private Color fillPaint = null;

    private boolean isGradient;
    private Color colour1 = null;
    private Color colour2 = null;
    private Color fillColour1 = null;
    private Color fillColour2 = null;

}
