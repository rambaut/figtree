package figtree.treeviewer.decorators;

import jebl.util.Attributable;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;

/**
 * This decorator takes an attribute name and a set of attibutable Objects.
 * Colours are given to each individual value.
 *
 * If the data take more values than colors, then they will wrap around
 *
 * @author Andrew Rambaut
 * @version $Id: DiscreteColorDecorator.java 639 2007-02-15 10:05:28Z rambaut $
 */
public class DiscreteColorDecorator implements Decorator {


    public static Color[] DEFAULT_PAINTS = new Color[] {
            new Color(64,35,225),
            new Color(229,35,60),
            new Color(255,174,34),
            new Color(86,255,34),
            new Color(35,141,148),
            new Color(146,35,142),
            new Color(255,90,34),
            new Color(239,255,34),
            Color.DARK_GRAY,

            new Color(160,146,241),
            new Color(243,146,158),
            new Color(255,215,145),
            new Color(171,255,145),
            new Color(146,199,202),
            new Color(201,146,199),
            new Color(255,173,145),
            new Color(248,255,145),
            Color.LIGHT_GRAY,

            new Color(32,18,113),
            new Color(115,18,30),
            new Color(128,87,17),
            new Color(43,128,17),
            new Color(18,71,74),
            new Color(73,18,71),
            new Color(128,45,17),
            new Color(120,128,17)
    };

    public DiscreteColorDecorator() {
        this(DEFAULT_PAINTS);
    }

    public DiscreteColorDecorator(Color[] paints) {
        this.paints = paints;
    }

    public DiscreteColorDecorator(String attributeName, Set<? extends Attributable> items) {
        this(attributeName, items, DEFAULT_PAINTS, false);
    }

    public DiscreteColorDecorator(String attributeName, Set<? extends Attributable> items, boolean isGradient) {
        this(attributeName, items, DEFAULT_PAINTS, isGradient);
    }

    public DiscreteColorDecorator(String attributeName, Set<? extends Attributable> items, Color[] paints, boolean isGradient) {
        this.attributeName = attributeName;

        // First collect the set of all attribute values
        Set<Object> sortedValues = new TreeSet<Object>();
        Set<Object> unsortedValues = new HashSet<Object>();

        for (Attributable item : items) {
            Object value = item.getAttribute(attributeName);
            if (value != null) {
                if (value instanceof Comparable) {
                    sortedValues.add(value);
                } else {
                    unsortedValues.add(value);
                }
            }
        }

        if (unsortedValues.size() > 0) {
            unsortedValues.addAll(sortedValues);
            setValues(unsortedValues, paints);
        } else {
            setValues(sortedValues, paints);
        }

        this.isGradient = isGradient;
    }

    public void setValues(Collection<? extends Object> values, Color[] paints) {
        colourMap = new HashMap<Object, Paint>();
        this.paints = paints;

        // now create a paint map for these values
        int i = 0;
        for (Object value : values) {
            colourMap.put(value, paints[i]);
            i = (i + 1) % paints.length;
        }

    }

    // Decorator INTERFACE
    public Paint getPaint(Paint paint) {
        if (this.paint == null) return paint;
        return this.paint;
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

    public String getAttributeName() {
        return attributeName;
    }

    public Paint getFillPaint(Paint paint) {
        return paint;
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
        } else {
            setValue(item);
        }
    }

    public boolean isGradient() {
        return isGradient;
    }

    public void setGradient(final boolean gradient) {
        isGradient = gradient;
    }

    public void setItems(Object item1, Object item2) {
        colour1 = null;
        colour2 = null;

        if (item2 == null) {
            setItem(item1);
            return;
        }

        if (item1 == null) {
            setItem(item2);
            return;
        }

        Object value1 = item1;
        if (item1 instanceof Attributable) {
            value1 = ((Attributable)item1).getAttribute(getAttributeName());
        }
        if (value1 != null) {
            colour1 = (Color)getPaintForValue(value1);
            paint = colour1;
        }

        Object value2 = item2;
        if (item2 instanceof Attributable) {
            value2 = ((Attributable)item2).getAttribute(getAttributeName());
        }
        if (value2 != null) {
            colour2 = (Color)getPaintForValue(value2);
            paint = colour2;
        }

        fillColour1 = null;
        if (colour1 != null) {
            fillColour1 = getLighterColour(colour1);

        }

        fillColour2 = null;
        if (colour2 != null) {
            fillColour2 = getLighterColour(colour2);
        }


    }


    public static boolean isDiscrete(String attributeName, Set<? extends Attributable> items) {
        // First collect the set of all attribute values
        Set<Object> values = new HashSet<Object>();
        for (Attributable item : items) {
            Object value = item.getAttribute(attributeName);
            if (value != null) {
                values.add(value);
            }
        }

        boolean isNumber = true;
        boolean isInteger = true;

        for (Object value : values) {
            if (value instanceof Number) {
                if (((Number)value).doubleValue() != ((Number)value).intValue()) {
                    isInteger = false;
                }
            } else {
                isNumber = false;
            }
        }

        if (isNumber && !isInteger) return false;

        return true;
    }

    // Private methods
    private void setAttributableItem(Attributable item) {
        paint = null;
        Object value = item.getAttribute(attributeName);
        if (value != null) {
            setValue(value);
        }
    }

    private void setValue(Object value) {
        paint = getPaintForValue(value);
    }

    private Paint getPaintForValue(Object value) {
        if (colourMap != null) {
            return colourMap.get(value);
        } else if (value instanceof Number) {
            int index = ((Number)value).intValue() % paints.length;
            return paints[index];
        }
        return null;
    }

    private Color getLighterColour(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 2);
    }

    private String attributeName = null;

    private Map<Object, Paint> colourMap = null;
    private Paint[] paints = null;
    private Paint paint = null;

    private boolean isGradient = false;

    private Color colour1 = null;
    private Color colour2 = null;
    private Color fillColour1 = null;
    private Color fillColour2 = null;

}
