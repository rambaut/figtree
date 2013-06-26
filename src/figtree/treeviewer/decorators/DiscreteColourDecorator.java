/*
 * DiscreteColorDecorator.java
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
public class DiscreteColourDecorator extends ColourDecorator {

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

    public DiscreteColourDecorator() {
        super(null);
        colours = DEFAULT_PAINTS;
    }

    public DiscreteColourDecorator(String attributeName) {
        super(attributeName);
    }

    public DiscreteColourDecorator(String attributeName, Set<? extends Attributable> items) {
        super(attributeName, items);
    }

    @Override
    public void setup(String settings) {
        throw new UnsupportedOperationException("setup from string not implemented");
    }

    public void setAttributes(String attributeName, Set<? extends Attributable> items) {
        super.setAttributes(attributeName, items);

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

        if (values == null) {
            values = new ArrayList<Object>(sortedValues);
            values.addAll(unsortedValues);
        } else {
            // if there is already a values array, only add the new values
            // to maintain the order that may have been edited by the user.

            for (Object value : sortedValues) {
                if (!values.contains(value)) {
                    values.add(value);
                }
            }
            for (Object value : unsortedValues) {
                if (!values.contains(value)) {
                    values.add(value);
                }
            }
        }

        setupColours();
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(Collection<Object> values) {
        this.values = new ArrayList<Object>(values);
        setupColours();
    }

    protected void setupColours() {
        setColourMap(values, DEFAULT_PAINTS);
    }

    public Color getColor(Object value) {
        return (Color)colours[orderMap.get(value)];
    }

    protected void setColourMap(Collection<Object> values, Color[] colours) {
        orderMap = new TreeMap<Object, Integer>();
        this.colours = colours;

        this.values = new ArrayList<Object>(values);

        // now create a paint map for these values
        int i = 0;
        for (Object value : this.values) {
            orderMap.put(value, i);
            i = (i + 1) % colours.length;
        }

    }

    protected Color getColourForValue(Object value) {
        int index = -1;

        if (value != null) {
            if (orderMap != null && orderMap.size() > 0) {
                index = orderMap.get(value);
            } else if (value instanceof Number) {
                index = ((Number)value).intValue() % colours.length;
            }
        }
        if (index == -1) {
            return null;
        }
        return colours[index];
    }

    public void setValuesOrder(List<Object> discreteValues) {
        values = discreteValues;
        hasReorderedValues = true;
    }

    public boolean hasReorderedValues() {
        return hasReorderedValues;
    }

    public String getOrderString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object value : values) {
            if (!first) {
                sb.append(",");
            } else {
                first = false;
            }
            sb.append(value);
        }
        return sb.toString();
    }

    private List<Object> values;
    private Map<Object, Integer> orderMap;
    private Color[] colours;

    private boolean hasReorderedValues = false;

}
