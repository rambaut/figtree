package figtree.treeviewer.decorators;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class CompoundDecorator implements Decorator {

	public void addDecorator(Decorator decorator) {
		decorators.add(decorator);
	}

    public boolean isGradient() {
        return false;
    }

    public void setItem(Object item) {
		for (Decorator decorator : decorators) {
			decorator.setItem(item);
		}
	}

    public void setItems(final Object item1, final Object item2) {
        throw new UnsupportedOperationException("This decorator doesn't support gradients");
    }

    public Paint getPaint(Paint paint) {
		Paint p = paint;
		for (Decorator decorator : decorators) {
			p = decorator.getPaint(p);
		}
		return p;
	}

    public Paint getPaint(final Paint paint, final Point2D point1, final Point2D point2) {
        throw new UnsupportedOperationException("This decorator doesn't support gradients");
    }

    public Paint getFillPaint(Paint paint) {
		Paint p = paint;
		for (Decorator decorator : decorators) {
			p = decorator.getFillPaint(p);
		}
		return p;
	}

    public Paint getFillPaint(final Paint paint, final Point2D point1, final Point2D point2) {
        throw new UnsupportedOperationException("This decorator doesn't support gradients");
    }

    public Stroke getStroke(Stroke stroke) {
		Stroke s = stroke;
		for (Decorator decorator : decorators) {
			s = decorator.getStroke(s);
		}
		return s;
	}

	public Font getFont(Font font) {
		Font f = font;
		for (Decorator decorator : decorators) {
			f = decorator.getFont(f);
		}
		return f;
	}

	private Set<Decorator> decorators = new HashSet<Decorator>();
}
