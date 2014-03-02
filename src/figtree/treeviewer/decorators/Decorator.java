/*
 * Decorator.java
 *
 * Copyright (C) 2006-2014 Andrew Rambaut
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

import jebl.evolution.taxa.Taxon;
import jebl.util.Attributable;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * @author Andrew Rambaut
 * @version $Id$
 *
 * $HeadURL$
 *
 * $LastChangedBy$
 * $LastChangedDate$
 * $LastChangedRevision$
 */
public interface Decorator {

    boolean allowsGradient();

    void setItem(Attributable item, Attributable fallbackItem);
    void setItem(Object item);
    void setItems(Object item1, Object item2);

    Paint getPaint(Paint paint);

    /**
     * Get stroke paint for a gradient between two point in colour scale
     * @param paint
     * @param point1
     * @param point2
     * @return
     */
    Paint getPaint(Paint paint, Point2D point1, Point2D point2);

    Paint getFillPaint(Paint paint);

    /**
     * Get fill paint for a gradient between two point in colour scale
     * @param paint
     * @param point1
     * @param point2
     * @return
     */
    Paint getFillPaint(Paint paint, Point2D point1, Point2D point2);

    Stroke getStroke(Stroke stroke);
    Font getFont(Font font);
}
