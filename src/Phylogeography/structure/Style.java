package phylogeography.structure;

import java.awt.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class Style {
    public Style(final Color strokeColor, final double strokeWidth) {
        this(strokeColor, strokeWidth, null);
    }

    public Style(final Color strokeColor, final double strokeWidth, final Color fillColor) {
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.fillColor = fillColor;
    }

    public Color getStrokeColor() {
        return strokeColor;
    }

    public double getStrokeWidth() {
        return strokeWidth;
    }

    public Color getFillColor() {
        return fillColor;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Style style = (Style) o;

        if (Double.compare(style.strokeWidth, strokeWidth) != 0) return false;
        if (fillColor != null ? !fillColor.equals(style.fillColor) : style.fillColor != null) return false;
        if (strokeColor != null ? !strokeColor.equals(style.strokeColor) : style.strokeColor != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = strokeColor != null ? strokeColor.hashCode() : 0;
        temp = strokeWidth != +0.0d ? Double.doubleToLongBits(strokeWidth) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (fillColor != null ? fillColor.hashCode() : 0);
        return result;
    }

    private final Color strokeColor;
    private final double strokeWidth;
    private final Color fillColor;
}
