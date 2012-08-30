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
 * @version $Id: ContinuousColourDecorator.java 822 2007-10-26 13:50:26Z rambaut $
 */
public class HSBContinuousColourDecorator extends ContinuousColourDecorator {

    public HSBContinuousColourDecorator(String attribute, String settings) {
        super(attribute);

        if (!settings.startsWith("{") || !settings.endsWith("}")) {
            throw new IllegalArgumentException("HSBContinuousColourDecorator settings string not in correct format");
        }

        String[] parts = settings.substring(1, settings.length() - 1).split("[, ]+");
        if (parts.length != 7) {
            throw new IllegalArgumentException("HSBContinuousColourDecorator settings string not in correct format");
        }

        try {
            setContinuousScale(new ContinuousScale(parts[0]));
            hueLower = Float.parseFloat(parts[1]);
            hueUpper = Float.parseFloat(parts[2]);
            saturationLower = Float.parseFloat(parts[3]);
            saturationUpper = Float.parseFloat(parts[4]);
            brightnessLower = Float.parseFloat(parts[5]);
            brightnessUpper = Float.parseFloat(parts[6]);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("HSBContinuousColourDecorator settings string not in correct format");
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("HSBContinuousColourDecorator settings string not in correct format");
        }

    }

    public HSBContinuousColourDecorator(ContinuousScale continuousScale) throws NumberFormatException {
        super(continuousScale);
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

    protected Color getColourForScaledValue(double value) {
        if (!Double.isNaN(value)) {
            return Color.getHSBColor(getHue((float)value), getSaturation((float)value), getBrightness((float)value));
        } else {
            return null;
        }
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
        sb.append(getContinuousScale().toString());
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

    private float hueUpper = 1.0F;
    private float hueLower = 0.0F;
    private float saturationUpper = 0.6F;
    private float saturationLower = 0.6F;
    private float brightnessUpper = 0.8F;
    private float brightnessLower = 0.4F;

}
