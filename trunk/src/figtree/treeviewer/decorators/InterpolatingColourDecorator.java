/*
 * InterpolatingColourDecorator.java
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
 * This decorator takes an attribute name and a set of attibutable Objects. It
 * autodetects the type of attributes and then provides a colouring scheme for them
 * based on a gradient between color1 & color2.
 *
 * @author Andrew Rambaut
 * @version $Id: ContinuousColourDecorator.java 822 2007-10-26 13:50:26Z rambaut $
 */
public class InterpolatingColourDecorator extends ContinuousColourDecorator {

    public static final Color DEFAULT_COLOR_1 = Color.getHSBColor(0.01F, 0.7F, 0.85F);
    public static final Color DEFAULT_COLOR_2 = Color.getHSBColor(0.63F, 0.7F, 0.85F);
    public static final Color DEFAULT_COLOR_3 = Color.BLACK;

    public InterpolatingColourDecorator(String attribute, String settings) {
        super(attribute);
        setup(settings);
    }

    public InterpolatingColourDecorator(ContinuousScale continuousScale) throws NumberFormatException {
        super(continuousScale);
        setColours(DEFAULT_COLOR_1, DEFAULT_COLOR_2);
    }

    public InterpolatingColourDecorator(ContinuousScale continuousScale,
                                        Color color1, Color color2) throws NumberFormatException {
        super(continuousScale);
        setColours(color1, color2);
    }

    public InterpolatingColourDecorator(ContinuousScale continuousScale,
                                        Color color1, Color color2, Color color3) throws NumberFormatException {
        super(continuousScale);
        setColours(color1, color2, color3);
    }

    public void setColours(Color color1, Color color2) {
        this.color1 = new float[4];
        color1.getRGBComponents(this.color1);
        this.color2 = new float[4];
        color2.getRGBComponents(this.color2);
        this.color3 = null;
    }

    public void setColours(Color color1, Color color2, Color color3) {
        this.color1 = new float[4];
        color1.getRGBComponents(this.color1);
        this.color2 = new float[4];
        color2.getRGBComponents(this.color2);
        this.color3 = new float[4];
        color3.getRGBComponents(this.color3);
    }

    public Color getColor1() {
        if (color1 == null) {
            return null;
        }
        return new Color(color1[0], color1[1], color1[2], color1[3]);
    }

    public Color getColor2() {
        if (color2 == null) {
            return null;
        }
        return new Color(color2[0], color2[1], color2[2], color2[3]);
    }

    public Color getColor3() {
        if (color3 == null) {
            return null;
        }
        return new Color(color3[0], color3[1], color3[2], color3[3]);
    }

    // Private methods
    public Color getColourForScaledValue(double value) {
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

    private float[] color1;
    private float[] color2;
    private float[] color3;

    @Override
    public void setup(String settings) {
        if (!settings.startsWith("{") || !settings.endsWith("}")) {
            throw new IllegalArgumentException("InterpolatingColourDecorator settings string not in correct format");
        }

        String[] parts1 = settings.substring(1, settings.length() - 1).split("}[, ]+");
        if (parts1.length != 2) {
            throw new IllegalArgumentException("InterpolatingColourDecorator settings string not in correct format");
        }
        String[] parts2 = parts1[1].split("[, ]+");
        if (parts2.length != 2 && parts2.length != 3) {
            throw new IllegalArgumentException("InterpolatingColourDecorator settings string not in correct format");
        }

        try {
            setContinuousScale(new ContinuousScale(parts1[0]));
            if (parts2.length == 3) {
                setColours(parseColor(parts2[0]),
                        parseColor(parts2[1]),
                        parseColor(parts2[2]));
            } else {
                setColours(parseColor(parts2[0]),
                        parseColor(parts2[1]));
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("InterpolatingColourDecorator settings string not in correct format");
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("InterpolatingColourDecorator settings string not in correct format");
        }
    }

    private Color parseColor(String value) {
        if (value.startsWith("#")) {
            try {
                return Color.decode(value.substring(1));
            } catch (NumberFormatException nfe) {
            }
        }
        return null;
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
        sb.append(",#");
        sb.append(new Color(color1[0], color1[1], color1[2], color1[3]).getRGB());
        sb.append(",#");
        sb.append(new Color(color2[0], color2[1], color2[2], color2[3]).getRGB());
        if (color3 != null) {
            sb.append(",#");
            sb.append(new Color(color3[0], color3[1], color3[2], color3[3]).getRGB());
        }
        sb.append("}");
        return sb.toString();
    }


}
