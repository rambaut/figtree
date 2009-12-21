package figtree.treeviewer.painters;

import jebl.evolution.trees.Tree;
import jebl.util.Attributable;

import java.awt.*;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Set;

import figtree.treeviewer.decorators.Decorator;

/**
 * @author Andrew Rambaut
 * @version $Id: LabelPainter.java 536 2006-11-21 16:10:24Z rambaut $
 */
public abstract class LabelPainter<T> extends AbstractPainter<T> {
    private Set<? extends Attributable> attributableItems;

    protected LabelPainter() {
	}

	// Abstract

	public abstract String[] getAttributes();

    public abstract void setupAttributes(Collection<? extends Tree> trees);

    public abstract void setDisplayAttribute(String displayAttribute);

    public abstract void setTextDecorator(Decorator textDecorator);

    public abstract Set<Attributable> getAttributableItems();

	// Getters

	public Paint getForeground() {
		return foreground;
	}

	public Paint getBackground() {
		return background;
	}

	public Paint getBorderPaint() {
		return borderPaint;
	}

	public Stroke getBorderStroke() {
		return borderStroke;
	}

	public Font getFont() {
		return font;
	}

	public NumberFormat getNumberFormat() {
		return numberFormat;
	}

	public boolean isVisible() {
	    return visible;
	}

	// Setters

	public void setBackground(Paint background) {
	    this.background = background;
	    firePainterChanged();
	}

	public void setBorder(Paint borderPaint, Stroke borderStroke) {
	    this.borderPaint = borderPaint;
	    this.borderStroke = borderStroke;
	    firePainterChanged();
	}

	public void setFont(Font font) {
		this.font = font;
	    firePainterChanged();
	}

	public void setForeground(Paint foreground) {
	    this.foreground = foreground;
	    firePainterChanged();
	}

	public void setNumberFormat(NumberFormat numberFormat) {
		this.numberFormat = numberFormat;
	    firePainterChanged();
	}

	public void setVisible(boolean visible) {
	    this.visible = visible;
	    firePainterChanged();
	}

	private Paint foreground = Color.BLACK;
	private Paint background = null;
	private Paint borderPaint = null;
	private Stroke borderStroke = null;

	private Font font;
	private boolean visible = true;

	private NumberFormat numberFormat = null;

}
