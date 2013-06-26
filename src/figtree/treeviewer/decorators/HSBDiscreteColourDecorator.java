/*
 * HSBDiscreteColourDecorator.java
 *
 * Copyright (C) 2012 Andrew Rambaut
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package figtree.treeviewer.decorators;

import jebl.util.Attributable;

import java.awt.*;
import java.util.Set;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class HSBDiscreteColourDecorator extends DiscreteColourDecorator {
    public enum Axis {
        HUE,
        SATURATION,
        BRIGHTNESS;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    public HSBDiscreteColourDecorator(String attributeName) {
        super(attributeName);
    }


    public HSBDiscreteColourDecorator(String attributeName, String settings) {
        super(attributeName);
        setup(settings);
    }

    public HSBDiscreteColourDecorator(String attributeName, Set<? extends Attributable> items) {
        super(attributeName, items);
        setupColours();
    }

    /**
     * Set up from a settings string
     * @param settings
     */
    public void setup(String settings) {
        if (!settings.startsWith("{") || !settings.endsWith("}")) {
            throw new IllegalArgumentException("HSBDiscreteColourDecorator settings string not in correct format");
        }

        String[] parts = settings.substring(1, settings.length() - 1).split("[, ]+");
        if (parts.length != 8) {
            throw new IllegalArgumentException("HSBDiscreteColourDecorator settings string not in correct format");
        }

        try {
            primaryAxis = Axis.valueOf(parts[0].toUpperCase());
            secondaryCount = Integer.parseInt(parts[1]);
            hueLower = Float.parseFloat(parts[2]);
            hueUpper = Float.parseFloat(parts[3]);
            saturationLower = Float.parseFloat(parts[4]);
            saturationUpper = Float.parseFloat(parts[5]);
            brightnessLower = Float.parseFloat(parts[6]);
            brightnessUpper = Float.parseFloat(parts[7]);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("HSBDiscreteColourDecorator settings string not in correct format");
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("HSBDiscreteColourDecorator settings string not in correct format");
        }
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

    protected void setupColours() {
        if (secondaryCount == 0) {
            return;
        }
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
        setColourMap(getValues(), paints);
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

    /**
     * Create a string representation suitable for writing to a text file
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(primaryAxis);
        sb.append(",");
        sb.append(secondaryCount);
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

    private Axis primaryAxis = Axis.HUE;
    private int secondaryCount = 2;
    private float hueUpper = 1.0F;
    private float hueLower = 0.0F;
    private float saturationUpper = 0.6F;
    private float saturationLower = 0.6F;
    private float brightnessUpper = 0.8F;
    private float brightnessLower = 0.4F;
}
