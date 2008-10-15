package phylogeochronogrammer;

import java.awt.*;

/**
 * @author Andrew Rambaut
 * @author Philippe Lemey
 * @version $Id$
 */
public class SurfaceDecoration {
    public SurfaceDecoration(String colorProperty, boolean visible, Color startColor, Color endColor, double opacity) {
        this.colorProperty = colorProperty;
        isVisible = visible;
        this.startColor = startColor;
        this.endColor = endColor;
        this.opacity = opacity;
    }

    public String getColorProperty() {
        return colorProperty;
    }

    public void setColorProperty(String colorProperty) {
        this.colorProperty = colorProperty;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public Color getStartColor() {
        return startColor;
    }

    public void setStartColor(Color startColor) {
        this.startColor = startColor;
    }

    public Color getEndColor() {
        return endColor;
    }

    public void setEndColor(Color endColor) {
        this.endColor = endColor;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    private String colorProperty;
    private boolean isVisible;
    private Color startColor;
    private Color endColor;
    private double opacity;

}