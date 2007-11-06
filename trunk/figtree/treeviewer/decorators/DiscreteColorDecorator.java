package figtree.treeviewer.decorators;

import jebl.util.Attributable;

import java.awt.*;
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
			Color.DARK_GRAY
	};

	public DiscreteColorDecorator() {
		this(DEFAULT_PAINTS);
	}

	public DiscreteColorDecorator(Color[] paints) {
		this.paints = paints;
	}

	public DiscreteColorDecorator(String attributeName, Set<? extends Attributable> items) {
		this(attributeName, items, DEFAULT_PAINTS);
	}

	public DiscreteColorDecorator(String attributeName, Set<? extends Attributable> items, Color[] paints) {
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
		if (colourMap != null) {
			paint = colourMap.get(value);
		} else if (value instanceof Number) {
			int index = ((Number)value).intValue() % paints.length;
			paint = paints[index];
		}
	}

	private String attributeName = null;

	private Map<Object, Paint> colourMap = null;
	private Paint[] paints = null;
	private Paint paint = null;
}
