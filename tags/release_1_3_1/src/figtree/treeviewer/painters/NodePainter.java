package figtree.treeviewer.painters;

import jebl.evolution.graphs.Node;

import java.awt.*;

/**
 * @author Andrew Rambaut
 * @version $Id: NodePainter.java 373 2006-07-01 15:18:27Z rambaut $
 */
public abstract class NodePainter extends AbstractPainter<Node> {


    protected NodePainter() {
    }

    // Getters

    public Stroke getStroke() {
        return stroke;
    }

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

    public boolean isVisible() {
        return visible;
    }

    // Setters

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
        firePainterChanged();
    }

    public void setBackground(Paint background) {
        this.background = background;
        firePainterChanged();
    }

    public void setBorder(Paint borderPaint, Stroke borderStroke) {
        this.borderPaint = borderPaint;
        this.borderStroke = borderStroke;
        firePainterChanged();
    }

    public void setForeground(Paint foreground) {
        this.foreground = foreground;
        firePainterChanged();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        firePainterChanged();
    }

    private Stroke stroke = new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    private Paint foreground = Color.BLACK;
    private Paint background = null;
    private Paint borderPaint = null;
    private Stroke borderStroke = null;

    private boolean visible = true;

}
