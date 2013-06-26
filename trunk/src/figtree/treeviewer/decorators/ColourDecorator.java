/*
 * ColourDecorator.java
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

import jebl.evolution.graphs.Node;
import jebl.util.Attributable;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

/**
 * This decorator takes an attribute name and a set of attibutable Objects.
 * Colours are given to each individual value.
 *
 * If the data take more values than colors, then they will wrap around
 *
 * @author Andrew Rambaut
 * @version $Id: DiscreteColourDecorator.java 639 2007-02-15 10:05:28Z rambaut $
 */
public abstract class ColourDecorator implements Decorator {

    public ColourDecorator(String attributeName) {
        this.attributeName = attributeName;
    }

    public ColourDecorator(String attributeName, Set<? extends Attributable> items) {
        setAttributes(attributeName, items);
    }

    public abstract void setup(String settings);

        @Override
    public boolean allowsGradient() {
        return true;
    }

    public void setAttributes(String attributeName, Set<? extends Attributable> items) {
        this.attributeName = attributeName;
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

    public String getAttributeName() {
        return attributeName;
    }

    public Stroke getStroke(Stroke stroke) {
        return stroke;
    }

    public Font getFont(Font font) {
        return font;
    }

    public void setItem(Object item) {
        if (item instanceof Node) {
            setAttributableItem((Attributable)item);
        } if (item instanceof Attributable) {
            setAttributableItem((Attributable)item);
        } else {
            paint = getColourForValue(item);
        }
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
            colour1 = getColourForValue(value1);
            paint = colour1;
        }

        Object value2 = item2;
        if (item2 instanceof Attributable) {
            value2 = ((Attributable)item2).getAttribute(getAttributeName());
        }
        if (value2 != null) {
            colour2 = getColourForValue(value2);
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

    public static boolean isNumerical(String attributeName, Set<? extends Attributable> items) {
        // First collect the set of all attribute values
        Set<Object> values = new HashSet<Object>();
        for (Attributable item : items) {
            Object value = item.getAttribute(attributeName);
            if (value != null) {
                values.add(value);
            }
        }

        boolean isNumber = true;

        for (Object value : values) {
            if (!(value instanceof Number)) {
                isNumber = false;
            }
        }

        return isNumber;
    }

    // Private methods
    private void setAttributableItem(Attributable item) {
        paint = null;
        Object value = item.getAttribute(attributeName);

        Color colour = getColourForValue(value);
        if (colour != null) {
            paint = colour;
            fillPaint = getLighterColour(colour);
        } else {
            paint = null;
            fillPaint = null;
        }
    }

    protected abstract Color getColourForValue(Object value);

    protected Color getLighterColour(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 2);
    }

    private String attributeName = null;

    protected Paint paint = null;
    protected Paint fillPaint = null;
    protected Color colour1 = null;
    protected Color colour2 = null;
    protected Color fillColour1 = null;
    protected Color fillColour2 = null;

}
