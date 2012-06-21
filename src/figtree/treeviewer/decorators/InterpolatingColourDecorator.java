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
public class InterpolatingContinuousColourDecorator extends ContinuousColourDecorator {

    public InterpolatingContinuousColourDecorator(ContinuousScale continuousScale,
                                                  Color color1, Color color2) throws NumberFormatException {
        super(continuousScale);
        this.color1 = new float[4];
        color1.getRGBComponents(this.color1);
        this.color2 = new float[4];
        color2.getRGBComponents(this.color2);
        this.color3 = null;
    }

    public InterpolatingContinuousColourDecorator(ContinuousScale continuousScale,
                                                  Color color1, Color color2, Color color3) throws NumberFormatException {
        super(continuousScale);
        this.color1 = new float[4];
        color1.getRGBComponents(this.color1);
        this.color2 = new float[4];
        color2.getRGBComponents(this.color2);
        this.color3 = new float[4];
        color3.getRGBComponents(this.color3);
    }

    // Private methods
    public Color getColour(double value) {
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

    private final float[] color1;
    private final float[] color2;
    private final float[] color3;
}
