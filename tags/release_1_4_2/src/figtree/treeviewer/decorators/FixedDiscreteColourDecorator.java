/*
 * FixedDiscreteColourDecorator.java
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

import jebl.util.Attributable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Set;

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
public class FixedDiscreteColourDecorator extends DiscreteColourDecorator {

    public FixedDiscreteColourDecorator(String attributeName) {
        super(attributeName);
        setupColours();
    }

    public FixedDiscreteColourDecorator(String attributeName, String settings) {
        super(attributeName);
        setup(settings);
    }

    public FixedDiscreteColourDecorator(String attributeName, Set<? extends Attributable> items) {
        super(attributeName, items);
        setupColours();
    }

    /**
     * Set up from a settings string
     * @param settings
     */
    public void setup(String settings) {
        if (!settings.startsWith("{") || !settings.endsWith("}")) {
            throw new IllegalArgumentException("FixedDiscreteColourDecorator settings string not in correct format");
        }

//        String[] parts = settings.substring(1, settings.length() - 1).split("[, ]+");
//        if (parts.length != 8) {
//            throw new IllegalArgumentException("FixedDiscreteColourDecorator settings string not in correct format");
//        }
//
//        try {
//        } catch (NumberFormatException nfe) {
//            throw new IllegalArgumentException("FixedDiscreteColourDecorator settings string not in correct format");
//        } catch (IllegalArgumentException iae) {
//            throw new IllegalArgumentException("FixedDiscreteColourDecorator settings string not in correct format");
//        }
    }

    protected void setupColours() {
        setColourMap(getValues(), DEFAULT_PAINTS);
    }

    /**
     * Create a string representation suitable for writing to a text file
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("FIXED");
        sb.append("}");
        return sb.toString();
    }

}
