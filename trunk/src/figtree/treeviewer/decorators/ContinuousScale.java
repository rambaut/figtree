package figtree.treeviewer.decorators;

import jebl.util.Attributable;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class ContinuousScale {

    /**
     * constructor that sets options from a string
     * @param settings
     */
    public ContinuousScale(String settings) {
        if (!settings.startsWith("{") || !settings.endsWith("}")) {
            throw new IllegalArgumentException("ContinousScale settings string not in correct format");
        }

        String[] parts = settings.substring(1, settings.length() - 1).split("[, ]+");
        if (parts.length != 4) {
            throw new IllegalArgumentException("ContinousScale settings string not in correct format");
        }

        try {
            normalize = Boolean.parseBoolean(parts[0]);
            logarithm = Boolean.parseBoolean(parts[1]);
            lowerRange = Double.parseDouble(parts[2]);
            upperRange = Double.parseDouble(parts[3]);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("ContinousScale settings string not in correct format");
        }
    }

    public ContinuousScale() {
        this(false, 0.0, 0.0, false);
    }

    public ContinuousScale(boolean normalize,
                           double lowerRange,
                           double upperRange,
                           boolean logarithm) {
        this.normalize = normalize;
        this.lowerRange = lowerRange;
        this.upperRange = upperRange;
        this.logarithm = logarithm;
    }

    public void calibrate(String attributeName, Set<? extends Attributable> items) {
        this.attributeName = attributeName;

        // First collect the set of all attribute values
        Set<Object> values = new TreeSet<Object>();
        for (Attributable item : items) {
            Object value = item.getAttribute(attributeName);
            if (value != null) {
                values.add(value);
            }
        }

        boolean isNumber = true;

        // Find the range of numbers
        for (Object value : values) {
            double realValue = -1.0;

            if (value instanceof Boolean) {
                realValue = ((Boolean)value ? 1 : 0);
            } else if (value instanceof Number) {
                realValue = ((Number)value).doubleValue();
            } else if (value instanceof String) {
                // it is a string but it could still code for
                // a boolean, integer or real
                if (value.toString().equalsIgnoreCase("true")) {
                    realValue = 1;
                } else if (value.toString().equalsIgnoreCase("false")) {
                    realValue = 0;
                } else {
                    try {
                        realValue = Double.parseDouble(value.toString());
                    } catch(NumberFormatException nfe) {
                        isNumber = false;
                    }
                }
            }

            if (isNumber) {
                if (realValue < minValue) {
                    minValue = realValue;
                }
                if (realValue > maxValue) {
                    maxValue = realValue;
                }

            }
        }

        if (!isNumber) {
            throw new NumberFormatException("One or more values for this attribute are not numbers");
        }

        if (normalize && minValue < 0 && maxValue > 0) {
            // if normalizing, and some are negative, assume we are normalizing with 0 at 0.5
            minValue = - maxValue;
        }
        if (logarithm) {
            if (minValue <= 0.0) {
                throw new NumberFormatException("One or more values for this attribute are negative or zero under a log scale");
            }
            minValue = Math.log(minValue);
            maxValue = Math.log(maxValue);
        }

    }

    public double getValue(Attributable item) {
        return getValue(item.getAttribute(attributeName));
    }

    public double getValue(Object value) {

        if (value != null) {

            double number = 0.0;
            if (value instanceof Number) {
                number = ((Number)value).doubleValue();
            } else {
                number = Double.parseDouble(value.toString());
            }
            return scaleValue(number);
        }

        return Double.NaN;
    }

    /**
     * Scales the value to a range 0,1 based on the current settings
     * @param value
     * @return
     */
    public double scaleValue(double value) {

        if (logarithm) {
            value = Math.log(value);
        }

        double min = 0.0;
        double max = 1.0;

        if (!normalize) {
            min = minValue;
            max = maxValue;
        } else {
            min = lowerRange;
            max = upperRange;
            if (maxValue > max) {
                max = maxValue;
            }
            if (minValue < min) {
                min = minValue;
            }
        }

        return ((value - min)/(max - min));
    }
    public String getAttributeName() {
        return attributeName;
    }

    public boolean isNormalize() {
        return normalize;
    }

    public boolean isLogarithm() {
        return logarithm;
    }

    public double getLowerRange() {
        return lowerRange;
    }

    public double getUpperRange() {
        return upperRange;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(normalize);
        sb.append(",");
        sb.append(logarithm);
        sb.append(",");
        sb.append(lowerRange);
        sb.append(",");
        sb.append(upperRange);
        sb.append("}");
        return sb.toString();
    }

    private final boolean normalize;
    private final boolean logarithm;

    private final double lowerRange;
    private final double upperRange;

    private String attributeName = null;
    private double minValue = Double.MAX_VALUE;
    private double maxValue = Double.MIN_VALUE;

}
