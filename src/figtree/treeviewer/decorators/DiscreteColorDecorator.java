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
 * @version $Id: DiscreteColorDecorator.java 639 2007-02-15 10:05:28Z rambaut $
 */
public class DiscreteColorDecorator extends ColourDecorator {

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
        super(null);
        colours = DEFAULT_PAINTS;
    }

    public DiscreteColorDecorator(String attributeName) {
        super(attributeName);
    }

    public DiscreteColorDecorator(String attributeName, Set<? extends Attributable> items) {
        super(attributeName, items);
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

        values = new ArrayList<Object>(sortedValues);
        values.addAll(unsortedValues);

        setupColours();
    }

    public List<Object> getValues() {
        return values;
    }

    protected void setupColours() {
        setValues(values, DEFAULT_PAINTS);
    }

    public Color getColor(Object value) {
        return (Color)colours[orderMap.get(value)];
    }

    public void setValues(Collection<? extends Object> values, Color[] colours) {
        orderMap = new TreeMap<Object, Integer>();
        this.colours = colours;

        // now create a paint map for these values
        int i = 0;
        for (Object value : values) {
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

    private List<Object> values = new ArrayList<Object>();
    private Map<Object, Integer> orderMap = null;
    private Color[] colours = null;
}
