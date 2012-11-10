/*
 * ContinuousColourDecorator.java
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
import java.util.Set;

/**
 * This decorator takes an attribute name and a set of attibutable Objects. It
 * autodetects the type of attributes and then provides a colouring scheme for them
 * based on a gradient between color1 & color2.
 *
 * @author Andrew Rambaut
 * @version $Id: ContinuousColourDecorator.java 822 2007-10-26 13:50:26Z rambaut $
 */
public abstract class ContinuousColourDecorator extends ColourDecorator {

    public ContinuousColourDecorator(String attributeName) {
        super(attributeName);
    }

    public ContinuousColourDecorator(ContinuousScale continuousScale ) {
        super(continuousScale.getAttributeName());
        this.continuousScale = continuousScale;
    }

    public void setAttributes(String attributeName, Set<? extends Attributable> items) {
        super.setAttributes(attributeName, items);
        continuousScale.setAttributes(attributeName, items);
    }

    public ContinuousScale getContinuousScale() {
        return continuousScale;
    }

    public void setContinuousScale(ContinuousScale continuousScale) {
        this.continuousScale = continuousScale;
    }

    @Override
    public Color getColourForValue(Object value) {
        return getColourForScaledValue(continuousScale.getValue(value));
    }

    // Private methods
    protected abstract Color getColourForScaledValue(double value);

    private ContinuousScale continuousScale;
}
