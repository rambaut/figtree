package figtree.treeviewer.decorators;

import jebl.evolution.taxa.Taxon;

import java.awt.*;

/**
 * @author Andrew Rambaut
 * @version $Id: Decorator.java 433 2006-08-27 19:34:13Z rambaut $
 */
public interface Decorator {

    void setItem(Object item);

    Paint getPaint(Paint paint);
    Paint getFillPaint(Paint paint);
    Stroke getStroke(Stroke stroke);
    Font getFont(Font font);
}
