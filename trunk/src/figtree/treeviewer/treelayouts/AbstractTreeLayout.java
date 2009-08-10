package figtree.treeviewer.treelayouts;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Andrew Rambaut
 * @version $Id: AbstractTreeLayout.java 819 2007-10-22 14:42:58Z rambaut $
 */
public abstract class AbstractTreeLayout implements TreeLayout {
	private double rootLength = 0.0;
    private boolean isAxisReversed;

    public boolean isAxisReversed() {
        return isAxisReversed;
    }

    public void setAxisReversed(final boolean axisReversed) {
        isAxisReversed = axisReversed;
    }

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
