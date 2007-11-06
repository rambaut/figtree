package figtree.treeviewer.decorators;

import jebl.util.Attributable;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class ContinousScale {

	public ContinousScale(String attributeName,
	                      Set<? extends Attributable> items,
	                      boolean normalize,
	                      boolean logarithm) {
		this.attributeName = attributeName;
		this.normalize = normalize;
		this.logarithm = logarithm;

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

		if (logarithm) {
			if (minValue <= 0.0) {
				throw new NumberFormatException("One or more values for this attribute are negative or zero under a log scale");
			}
			minValue = Math.log(minValue);
			maxValue = Math.log(maxValue);
		}
	}

	public double getValue(Attributable item) {
		Object value = item.getAttribute(attributeName);

		if (value != null) {

			double number = 0.0;
			if (value instanceof Number) {
				number = ((Number)value).doubleValue();
			} else {
				number = Double.parseDouble(value.toString());
			}
			if (logarithm) {
				number = Math.log(number);
			}

			double min = 0.0;
			double max = 1.0;

			if (normalize) {
				min = minValue;
				max = maxValue;
			} else {
				if (maxValue > 1.0) {
					max = maxValue;
				}
				if (minValue < 0.0) {
					min = minValue;
				}
			}
			return ((number - min)/(max - min));
		}

		return Double.NaN;
	}

	private final String attributeName;
	private final boolean normalize;
	private final boolean logarithm;

	private double minValue = Double.MAX_VALUE;
	private double maxValue = Double.MIN_VALUE;

}
