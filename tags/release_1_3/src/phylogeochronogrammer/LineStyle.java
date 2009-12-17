package phylogeochronogrammer;

import java.awt.*;

/**
 * @author Andrew Rambaut
 * @author Philippe Lemey
 * @version $Id$
 */
public class LineStyle {
    public LineStyle(double width, Color color) {
        this(width, color, 1.0);
    }

    public LineStyle(double width, Color color, double opacity) {
        this.width = width;
        this.color = color;
        this.opacity = opacity;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    private double width;
    private Color color;
    private double opacity;
}
