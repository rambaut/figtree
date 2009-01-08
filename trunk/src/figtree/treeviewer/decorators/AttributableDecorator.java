package figtree.treeviewer.decorators;

import jebl.util.Attributable;

import java.awt.*;
import java.awt.geom.Point2D;

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

    public Paint getPaint(Paint paint, Point2D point1, Point2D point2) {
        if (colour1 != null && colour2 != null) {
            return new GradientPaint(point1, colour1, point2, colour2, false);
        } else {
            return paint;
        }
    }

    public Paint getFillPaint(Paint paint, Point2D point1, Point2D point2) {
        if (fillColour1 != null && fillColour2 != null) {
            return new GradientPaint(point1, fillColour1, point2, fillColour2, false);
        } else {
            return paint;
        }
    }

    public Stroke getStroke(Stroke stroke) {
        if (this.stroke == null) return stroke;
        return this.stroke;
    }

    public Font getFont(Font font) {
        if (this.font == null) return font;
        return this.font;
    }

    public boolean isGradient() {
        return isGradient;
    }

    public void setGradient(final boolean gradient) {
        isGradient = gradient;
    }

    public void setItem(Object item) {
        if (item instanceof Attributable) {
            setAttributableItem((Attributable)item);
        }
    }

    public void setItems(final Object item1, final Object item2) {
        if (item2 == null) {
            setItem(item1);
            return;
        }

        if (item1 == null) {
            setItem(item2);
            return;
        }

        if (item1 instanceof Attributable) {
            setAttributableItem((Attributable)item1);
        }

        if (item2 instanceof Attributable && paintAttributeName != null) {
            colour2 = getColorAttribute(((Attributable)item2).getAttribute(paintAttributeName));
            if (colour2 != null) {
                fillColour2 = colour2.brighter();
            } else {
                fillColour2 = null;
            }
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
            colour1 = getColorAttribute(item.getAttribute(paintAttributeName));
            if (colour1 != null) {
                fillColour1 = colour1.brighter();
                paint = colour1;
                fillPaint = fillColour1;
//                fillPaint = new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 2);
            } else {
                fillColour1 = null;
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

    private boolean isGradient;
    private Color colour1 = null;
    private Color colour2 = null;
    private Color fillColour1 = null;
    private Color fillColour2 = null;
}
