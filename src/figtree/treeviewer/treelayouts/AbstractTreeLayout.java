/*
 * AbstractTreeLayout.java
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

package figtree.treeviewer.treelayouts;

import java.util.HashSet;
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
public abstract class AbstractTreeLayout implements TreeLayout {
	private double rootLength = 0.0;

    public double getRootLength() {
		return rootLength;
	}

	public void setRootLength(double rootLength) {
		this.rootLength = rootLength;
		fireTreeLayoutChanged();
	}

	public void addTreeLayoutListener(TreeLayoutListener listener) {
        listeners.add(listener);
    }

    public void removeTreeLayoutListener(TreeLayoutListener listener) {
        listeners.remove(listener);
    }

	protected void fireTreeLayoutChanged() {
        for (TreeLayoutListener listener : listeners) {
            listener.treeLayoutChanged();
        }
    }

    public String getBranchColouringAttributeName() {
        return branchColouringAttribute;
    }

    public void setBranchColouringAttributeName(String branchColouringAttribute) {
        this.branchColouringAttribute = branchColouringAttribute;
        fireTreeLayoutChanged();
    }

    public String getCartoonAttributeName() {
        return cartoonAttributeName;
    }

    public void setCartoonAttributeName(String cartoonAttributeName) {
        this.cartoonAttributeName = cartoonAttributeName;
        fireTreeLayoutChanged();
    }

    public boolean isShowingCartoonTipLabels() {
        return showingCartoonTipLabels;
    }

    public void setShowingCartoonTipLabels(boolean showingCartoonTipLabels) {
        this.showingCartoonTipLabels = showingCartoonTipLabels;
        fireTreeLayoutChanged();
    }

	public String getCollapsedAttributeName() {
		return collapsedAttributeName;
	}

	public void setCollapsedAttributeName(String collapsedAttributeName) {
		this.collapsedAttributeName = collapsedAttributeName;
		fireTreeLayoutChanged();
	}

	public String getHilightAttributeName() {
		return hilightAttributeName;
	}

	public void setHilightAttributeName(String hilightAttributeName) {
		this.hilightAttributeName = hilightAttributeName;
		fireTreeLayoutChanged();
	}

    private Set<TreeLayoutListener> listeners = new HashSet<TreeLayoutListener>();
    protected String branchColouringAttribute = null;
    protected String cartoonAttributeName = null;
    protected boolean showingCartoonTipLabels = true;

	protected String collapsedAttributeName = null;

	protected String hilightAttributeName = null;
}
