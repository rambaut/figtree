package phylogeochronogrammer;

import java.awt.*;

/**
 * @author Andrew Rambaut
 * @author Philippe Lemey
 * @version $Id$
 */
public class BranchDecoration {
    public BranchDecoration(String widthProperty, String colorProperty, boolean visible, double branchWidth, double branchWidthScale, Color startColor, Color endColor, double opacity) {
        this.widthProperty = widthProperty;
        this.colorProperty = colorProperty;
        isVisible = visible;
        this.branchWidth = branchWidth;
        this.branchWidthScale = branchWidthScale;
        this.startColor = startColor;
        this.endColor = endColor;
        this.opacity = opacity;
    }

    public String getWidthProperty() {
        return widthProperty;
    }

    public void setWidthProperty(String widthProperty) {
        this.widthProperty = widthProperty;
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

    public double getBranchWidth() {
        return branchWidth;
    }

    public void setBranchWidth(double branchWidth) {
        this.branchWidth = branchWidth;
    }

    public double getBranchWidthScale() {
        return branchWidthScale;
    }

    public void setBranchWidthScale(double branchWidthScale) {
        this.branchWidthScale = branchWidthScale;
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

    private String widthProperty;
    private String colorProperty;
    
    private boolean isVisible;
    private double branchWidth;
    private double branchWidthScale;
    private Color startColor;
    private Color endColor;
    private double opacity;

}
