package figtree.treeviewer.decorators;

import java.awt.*;
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

	public void setItem(Object item) {
		for (Decorator decorator : decorators) {
			decorator.setItem(item);
		}
	}

	public Paint getPaint(Paint paint) {
		Paint p = paint;
		for (Decorator decorator : decorators) {
			p = decorator.getPaint(p);
		}
		return p;
	}

	public Paint getFillPaint(Paint paint) {
		Paint p = paint;
		for (Decorator decorator : decorators) {
			p = decorator.getFillPaint(p);
		}
		return p;
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
