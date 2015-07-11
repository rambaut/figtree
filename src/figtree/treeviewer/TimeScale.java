/*
 * TimeScale.java
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

package figtree.treeviewer;

import jebl.evolution.trees.RootedTree;

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
public class TimeScale {

    private boolean isReversed = false;

    public TimeScale(double rootAge) {
		this.rootAge = rootAge;
		this.scaleFactor = Double.NaN;
		this.offsetAge = 0.0;
	}

	public TimeScale(double scaleFactor, double offsetAge) {
		this.rootAge = Double.NaN;
		this.offsetAge = offsetAge;
		this.scaleFactor = scaleFactor;
	}

	public double getScaleFactor(RootedTree tree) {
		if (Double.isNaN(scaleFactor)) {
			return rootAge / tree.getHeight(tree.getRootNode());
		}
		return scaleFactor * (isReversed ? -1.0 : 1.0);
	}

	public double getAge(double height, RootedTree tree) {
		return (height * getScaleFactor(tree)) + offsetAge;
	}

	public double getTime(double length, RootedTree tree) {

		return (length * getScaleFactor(tree));
	}

    public double getHeight(double age, RootedTree tree) {
        return (age - offsetAge) / getScaleFactor(tree);
    }

    public double getLength(double time, RootedTree tree) {

        return (time / getScaleFactor(tree));
    }

    public void setReversed(boolean isReversed) {
        this.isReversed = isReversed;
    }

    private final double rootAge;
	private final double offsetAge;
	private final double scaleFactor;

}
