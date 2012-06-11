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

    public HSBContinuousColorDecorator(String settings) {
        if (!settings.startsWith("{") || !settings.endsWith("}")) {
            throw new IllegalArgumentException("HSBContinuousColorDecorator settings string not in correct format");
        }

        String[] parts = settings.substring(1, settings.length() - 1).split("[, ]+");
        if (parts.length != 7) {
            throw new IllegalArgumentException("HSBContinuousColorDecorator settings string not in correct format");
        }

        try {
            continuousScale = new ContinuousScale(parts[0]);
            hueUpper = Float.parseFloat(parts[1]);
            hueLower = Float.parseFloat(parts[2]);
            saturationUpper = Float.parseFloat(parts[3]);
            saturationLower = Float.parseFloat(parts[4]);
            brightnessUpper = Float.parseFloat(parts[5]);
            brightnessLower = Float.parseFloat(parts[6]);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("HSBContinuousColorDecorator settings string not in correct format");
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("HSBContinuousColorDecorator settings string not in correct format");
        }

    }

    public HSBContinuousColorDecorator(ContinuousScale continuousScale) throws NumberFormatException {
        this.continuousScale = continuousScale;
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

    public ContinuousScale getContinuousScale() {
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
        return ((hueUpper - hueLower) * (float)continuousScale.scaleValue(value)) + hueLower;
    }

    private float getSaturation(float value) {
        return ((saturationUpper - saturationLower) * (float)continuousScale.scaleValue(value)) + saturationLower;
    }

    private float getBrightness(float value) {
        return ((brightnessUpper - brightnessLower) * (float)continuousScale.scaleValue(value)) + brightnessLower;
    }

    public float getHueUpper() {
        return hueUpper;
    }

    public void setHueUpper(float hueUpper) {
        this.hueUpper = hueUpper;
    }

    public float getHueLower() {
        return hueLower;
    }

    public void setHueLower(float hueLower) {
        this.hueLower = hueLower;
    }

    public float getSaturationUpper() {
        return saturationUpper;
    }

    public void setSaturationUpper(float saturationUpper) {
        this.saturationUpper = saturationUpper;
    }

    public float getSaturationLower() {
        return saturationLower;
    }

    public void setSaturationLower(float saturationLower) {
        this.saturationLower = saturationLower;
    }

    public float getBrightnessUpper() {
        return brightnessUpper;
    }

    public void setBrightnessUpper(float brightnessUpper) {
        this.brightnessUpper = brightnessUpper;
    }

    public float getBrightnessLower() {
        return brightnessLower;
    }

    public void setBrightnessLower(float brightnessLower) {
        this.brightnessLower = brightnessLower;
    }

    /**
     * Create a string representation suitable for writing to a text file
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(continuousScale.toString());
        sb.append(",");
        sb.append(hueLower);
        sb.append(",");
        sb.append(hueUpper);
        sb.append(",");
        sb.append(saturationLower);
        sb.append(",");
        sb.append(saturationUpper);
        sb.append(",");
        sb.append(brightnessLower);
        sb.append(",");
        sb.append(brightnessUpper);
        sb.append("}");
        return sb.toString();
    }

    private final ContinuousScale continuousScale;

    private Color paint = null;
    private Color fillPaint = null;
    private Color colour1 = null;
    private Color colour2 = null;
    private Color fillColour1 = null;
    private Color fillColour2 = null;

    private float hueUpper = 1.0F;
    private float hueLower = 0.0F;
    private float saturationUpper = 0.6F;
    private float saturationLower = 0.6F;
    private float brightnessUpper = 0.8F;
    private float brightnessLower = 0.4F;

}
