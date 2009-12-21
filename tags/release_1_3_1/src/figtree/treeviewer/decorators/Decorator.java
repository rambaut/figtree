package figtree.treeviewer.decorators;

import jebl.evolution.taxa.Taxon;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * @author Andrew Rambaut
 * @version $Id: Decorator.java 433 2006-08-27 19:34:13Z rambaut $
 */
public interface Decorator {

    boolean isGradient();
    
    void setItem(Object item);
    void setItems(Object item1, Object item2);

    Paint getPaint(Paint paint);

    /**
     * Get stroke paint for a gradient between two point in colour scale
     * @param paint
     * @param point1
     * @param point2
     * @return
     */
    Paint getPaint(Paint paint, Point2D point1, Point2D point2);

    Paint getFillPaint(Paint paint);

    /**
     * Get fill paint for a gradient between two point in colour scale
     * @param paint
     * @param point1
     * @param point2
     * @return
     */
    Paint getFillPaint(Paint paint, Point2D point1, Point2D point2);

    Stroke getStroke(Stroke stroke);
    Font getFont(Font font);
}
