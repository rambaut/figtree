/*
 * Painter.java
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

package figtree.treeviewer.painters;

import figtree.treeviewer.TreePane;

import java.awt.*;
import java.awt.geom.Rectangle2D;


/**
 * A painter draws a particular decoration onto the tree within a
 * rectangle.
 * @author Andrew Rambaut
 * @version $Id: Painter.java 370 2006-06-29 18:57:56Z rambaut $
 */
public interface Painter<T> {

    public enum Orientation {
        TOP,
        LEFT,
        BOTTOM,
        RIGHT
    }

    public enum Justification {
        FLUSH,
        LEFT,
        RIGHT,
        CENTER
    }

	/**
	 * Called when the painter is installed in a TreePane. Gives the
	 * painter a handle on the TreePane so that it get additional
	 * information.
	 * @param treePane
	 */
	void setTreePane(TreePane treePane);

	/**
	 * If this is false then the painter should not be displayed.
	 * @return is visible?
	 */
    boolean isVisible();

	/**
	 * Called to calibrate the painters for a given graphics context. This should
	 * work out the preferred width and height (perhaps for the current font).
	 * @param g2
	 * @param item
	 */
    Rectangle2D calibrate(Graphics2D g2, T item);

	/**
	 * Called to actually paint into the current graphics context. The painter should
	 * respect the bounds.
	 * @param g2
	 * @param item
	 * @param justification
	 * @param bounds
	 */
    void paint(Graphics2D g2, T item, Justification justification, Rectangle2D bounds);

    double getPreferredWidth();
    double getPreferredHeight();
    double getHeightBound();

    void addPainterListener(PainterListener listener);
    void removePainterListener(PainterListener listener);
}
