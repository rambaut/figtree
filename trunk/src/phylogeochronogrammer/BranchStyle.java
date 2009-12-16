package phylogeochronogrammer;

import java.awt.*;

/**
 * @author Andrew Rambaut
 * @author Philippe Lemey
 * @version $Id$
 */
public class BranchStyle {
    public BranchStyle() {
    }

    public BranchStyle(String widthProperty, String colorProperty) {
        this.widthProperty = widthProperty;
        this.colorProperty = colorProperty;
    }

    public String getWidthProperty() {
        return widthProperty;
    }

    public void setWidthProperty(String widthProperty) {
        this.widthProperty = widthProperty;
    }

    public double getWidthPropertyMinimum() {
        return widthPropertyMinimum;
    }

    public void setWidthPropertyMinimum(double widthPropertyMinimum) {
        this.widthPropertyMinimum = widthPropertyMinimum;
    }

    public double getWidthPropertyMaximum() {
        return widthPropertyMaximum;
    }

    public void setWidthPropertyMaximum(double widthPropertyMaximum) {
        this.widthPropertyMaximum = widthPropertyMaximum;
    }

    public double getWidthBase() {
        return widthBase;
    }

    public void setWidthBase(double widthBase) {
        this.widthBase = widthBase;
    }

    public double getWidthScale() {
        return widthScale;
    }

    public void setWidthScale(double widthScale) {
        this.widthScale = widthScale;
    }

    public String getColorProperty() {
        return colorProperty;
    }

    public void setColorProperty(String colorProperty) {
        this.colorProperty = colorProperty;
    }

    public double getColorPropertyMinimum() {
        return colorPropertyMinimum;
    }

    public void setColorPropertyMinimum(double colorPropertyMinimum) {
        this.colorPropertyMinimum = colorPropertyMinimum;
    }

    public double getColorPropertyMaximum() {
        return colorPropertyMaximum;
    }

    public void setColorPropertyMaximum(double colorPropertyMaximum) {
        this.colorPropertyMaximum = colorPropertyMaximum;
    }

    public Color getColorStart() {
        return colorStart;
    }

    public void setColorStart(Color colorStart) {
        this.colorStart = colorStart;
    }

    public Color getColorFinish() {
        return colorFinish;
    }

    public void setColorFinish(Color colorFinish) {
        this.colorFinish = colorFinish;
    }

    private String widthProperty = null;
    private double widthPropertyMinimum = 0.0;
    private double widthPropertyMaximum = 1.0;

    private double widthBase = 4.0;
    private double widthScale = 3.0;

    private String colorProperty = null;
    private double colorPropertyMinimum = 0.0;
    private double colorPropertyMaximum = 1.0;

    private Color colorStart = Color.blue;
    private Color colorFinish = Color.white;
}
