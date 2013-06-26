/*
 * HSBDiscreteColourDecorator.java
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
import java.util.ArrayList;
import java.util.Set;

/**
 * @author Andrew Rambaut
 * @version $Id$
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

        String[] parts = settings.substring(1, settings.length() - 1).split("[, ]+");
        if (parts.length != 8) {
            throw new IllegalArgumentException("FixedDiscreteColourDecorator settings string not in correct format");
        }

        try {
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("FixedDiscreteColourDecorator settings string not in correct format");
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("FixedDiscreteColourDecorator settings string not in correct format");
        }
    }

    protected void setupColours() {
        java.util.List<Object> values = new ArrayList<Object>();

        values.add("AC1");
        values.add("AC10");
        values.add("AC11");
        values.add("AC12");
        values.add("AC13");
        values.add("AC14");
        values.add("AC2");
//        values.add("AC3");
        values.add("AC4");
        values.add("AC5");
        values.add("AC6");
        values.add("AC7");
        values.add("AC8");
        values.add("AC9");

        Color[] airCommunities = new Color[] {
                new Color(0.879F, 0.261F, 0.262F),  // AC1
                new Color(0.917F, 0.58F, 0.322F),   // AC10
                new Color(0.64F, 0.46F, 0.28F),     // AC11
                new Color(0.599F, 0.772F, 0.513F),  // AC12
                new Color(0.551F, 0.242F, 0.598F),  // AC13
                new Color(0.43F, 0.674F, 0.744F),  // AC14
                new Color(0.816F, 0.765F, 0.376F),  // AC2
//                new Color(0.26F, 0.141F, 0.632F),   // AC3
                new Color(0.242F, 0.445F, 0.718F),  // AC4
                new Color(0.592F, 0.669F, 0.295F),  // AC5
                new Color(0.791F, 0.27F, 0.146F),   // AC6
                new Color(0.359F, 0.425F, 0.833F),  // AC7
                new Color(0.374F, 0.623F, 0.505F),  // AC8
                new Color(0.785F, 0.585F, 0.209F)  // AC9
        };

        setColourMap(values, airCommunities);
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
