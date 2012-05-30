package figtree.treeviewer.decorators;

import jebl.util.Attributable;

import java.awt.*;
import java.util.Set;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class HSBDiscreteColorDecorator extends DiscreteColorDecorator {
    public enum Axis {
        HUE,
        SATURATION,
        BRIGHTNESS;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    public HSBDiscreteColorDecorator(String attributeName, Set<? extends Attributable> items) {
        super(attributeName, items);
        setupColours();
    }

    public HSBDiscreteColorDecorator(String attributeName, Set<? extends Attributable> items, boolean isGradient) {
        super(attributeName, items, isGradient);
        setupColours();
    }

    public void setup(int secondaryCount, float hueLower, float hueUpper, float saturationLower, float saturationUpper, float brightnessLower, float brightnessUpper, Axis primaryAxis) {

        this.secondaryCount = secondaryCount;
        this.hueUpper = hueUpper;
        this.hueLower = hueLower;
        this.saturationUpper = saturationUpper;
        this.saturationLower = saturationLower;
        this.brightnessUpper = brightnessUpper;
        this.brightnessLower = brightnessLower;
        this.primaryAxis = primaryAxis;

        setupColours();
    }

    private void setupColours() {
        int valueCount = getValues().size();
        int primaryCount = valueCount / secondaryCount;
        if (valueCount % secondaryCount > 0) {
            primaryCount +=  secondaryCount - (valueCount % secondaryCount);
        }

        Color[] paints = new Color[primaryCount * secondaryCount];

        float hDelta = (hueUpper - hueLower) / (primaryAxis == Axis.HUE ? primaryCount :  secondaryCount);
        float bDelta = (brightnessUpper - brightnessLower) /  (primaryAxis == Axis.BRIGHTNESS ? primaryCount : secondaryCount);
        float sDelta = (saturationUpper - saturationLower) /  (primaryAxis == Axis.SATURATION ? primaryCount : secondaryCount);

        float hue = hueLower;
        float brightness = brightnessUpper;
        float saturation = saturationLower;

        int k = 0;
        for (int i = 0; i < primaryCount; i++) {
            switch (primaryAxis) {
                case HUE:
                    saturation = saturationLower;
                    brightness = brightnessUpper;
                    break;
                case SATURATION:
                    hue = hueLower;
                    brightness = brightnessUpper;
                    break;
                case BRIGHTNESS:
                    hue = hueLower;
                    saturation = saturationLower;
                    break;
            }
            for (int j = 0; j < secondaryCount; j++) {
                paints[k] = Color.getHSBColor(hue, saturation, brightness);
                switch (primaryAxis) {
                    case HUE:
                        saturation += sDelta;
                        brightness -= bDelta;
                        break;
                    case SATURATION:
                        hue += hDelta;
                        brightness -= bDelta;
                        break;
                    case BRIGHTNESS:
                        hue += hDelta;
                        saturation += sDelta;
                        break;
                }
                k++;
            }
            switch (primaryAxis) {
                case HUE:
                    hue += hDelta;
                    break;
                case SATURATION:
                    saturation += sDelta;
                    break;
                case BRIGHTNESS:
                    brightness -= bDelta;
                    break;
            }
        }
        setValues(getValues(), paints);
    }

    public void setSecondaryCount(int secondaryCount) {
        this.secondaryCount = secondaryCount;
        setupColours();
    }

    public void setHueUpper(float hueUpper) {
        this.hueUpper = hueUpper;
        setupColours();
    }

    public void setHueLower(float hueLower) {
        this.hueLower = hueLower;
        setupColours();
    }

    public void setSaturationUpper(float saturationUpper) {
        this.saturationUpper = saturationUpper;
        setupColours();
    }

    public void setSaturationLower(float saturationLower) {
        this.saturationLower = saturationLower;
        setupColours();
    }

    public void setBrightnessUpper(float brightnessUpper) {
        this.brightnessUpper = brightnessUpper;
        setupColours();
    }

    public void setBrightnessLower(float brightnessLower) {
        this.brightnessLower = brightnessLower;
        setupColours();
    }

    public void setPrimaryAxis(Axis primaryAxis) {
        this.primaryAxis = primaryAxis;
        setupColours();
    }

    public int getSecondaryCount() {
        return secondaryCount;
    }

    public float getHueUpper() {
        return hueUpper;
    }

    public float getHueLower() {
        return hueLower;
    }

    public float getSaturationUpper() {
        return saturationUpper;
    }

    public float getSaturationLower() {
        return saturationLower;
    }

    public float getBrightnessUpper() {
        return brightnessUpper;
    }

    public float getBrightnessLower() {
        return brightnessLower;
    }

    public Axis getPrimaryAxis() {
        return primaryAxis;
    }

    private int secondaryCount = 2;
    private float hueUpper = 1.0F;
    private float hueLower = 0.0F;
    private float saturationUpper = 0.6F;
    private float saturationLower = 0.6F;
    private float brightnessUpper = 0.8F;
    private float brightnessLower = 0.4F;
    private Axis primaryAxis = Axis.HUE;
}
