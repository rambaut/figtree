/*
 * CompoundDecorator.java
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

    public boolean allowsGradient() {
        for (Decorator decorator : decorators) {
            if (decorator.allowsGradient()) {
                return true;
            }
        }
        return false;
    }

    public void setItem(Object item) {
        for (Decorator decorator : decorators) {
            decorator.setItem(item);
        }
    }

    public void setItems(final Object item1, final Object item2) {
        for (Decorator decorator : decorators) {
            if (decorator.allowsGradient()) {
                decorator.setItems(item1, item2);
                return;
            }
        }
    }

    public Paint getPaint(Paint paint) {
        Paint p = paint;
        for (Decorator decorator : decorators) {
            p = decorator.getPaint(p);
        }
        return p;
    }

    public Paint getPaint(final Paint paint, final Point2D point1, final Point2D point2) {
        for (Decorator decorator : decorators) {
            if (decorator.allowsGradient()) {
                return decorator.getPaint(paint, point1, point2);
            }
        }
        return null;
    }

    public Paint getFillPaint(Paint paint) {
        Paint p = paint;
        for (Decorator decorator : decorators) {
            p = decorator.getFillPaint(p);
        }
        return p;
    }

    public Paint getFillPaint(final Paint paint, final Point2D point1, final Point2D point2) {
        for (Decorator decorator : decorators) {
            if (decorator.allowsGradient()) {
                return decorator.getFillPaint(paint, point1, point2);
            }
        }
        return null;
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
