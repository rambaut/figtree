package figtree.treeviewer.decorators;

import jebl.util.Attributable;

import java.awt.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: AttributableDecorator.java 433 2006-08-27 19:34:13Z rambaut $
 */
public class AttributableDecorator implements Decorator {

    // Decorator INTERFACE
    public Paint getPaint(Paint paint) {
        if (this.paint == null) return paint;
        return this.paint;
    }

    public Paint getFillPaint(Paint paint) {
        if (this.fillPaint == null) return paint;
        return this.fillPaint;
    }

    public Stroke getStroke(Stroke stroke) {
        if (this.stroke == null) return stroke;
        return this.stroke;
    }

    public Font getFont(Font font) {
        if (this.font == null) return font;
        return this.font;
    }

    public void setItem(Object item) {
        if (item instanceof Attributable) {
            setAttributableItem((Attributable)item);
        }
    }

    // Public methods
    public String getFontAttributeName() {
        return fontAttributeName;
    }

    public void setFontAttributeName(String fontAttributeName) {
        this.fontAttributeName = fontAttributeName;
    }

    public String getPaintAttributeName() {
        return paintAttributeName;
    }

    public void setPaintAttributeName(String paintAttributeName) {
        this.paintAttributeName = paintAttributeName;
    }

    public String getStrokeAttributeName() {
        return strokeAttributeName;
    }

    public void setStrokeAttributeName(String strokeAttributeName) {
        this.strokeAttributeName = strokeAttributeName;
    }

    // Private methods
    private void setAttributableItem(Attributable item) {
        if (paintAttributeName != null) {
            Color color = getColorAttribute(item.getAttribute(paintAttributeName));
            if (color != null) {
                paint = color;
                fillPaint = color.brighter();
//                fillPaint = new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 2);
            } else {
                paint = null;
                fillPaint = null;
            }
        }
        if (fontAttributeName != null) {
            font = getFontAttribute(item.getAttribute(fontAttributeName));
        }
        if (strokeAttributeName != null) {
            stroke = getStrokeAttribute(item.getAttribute(strokeAttributeName));
        }
    }

    private Color getColorAttribute(Object value) {
        if (value != null) {
            if (value instanceof Color) {
                return (Color)value;
            }
            try {
                return Color.decode(value.toString());
            } catch (NumberFormatException nfe) {
                //
            }
        }
        return null;
    }

    private Font getFontAttribute(Object value) {
        if (value != null) {
            return Font.decode(value.toString());
        }
        return null;
    }

    private Stroke getStrokeAttribute(Object value) {
        return null;
    }

    private String paintAttributeName = null;
    private String fontAttributeName = null;
    private String strokeAttributeName = null;

    private Paint paint = null;
    private Paint fillPaint = null;
    private Font font = null;
    private Stroke stroke = null;
}
