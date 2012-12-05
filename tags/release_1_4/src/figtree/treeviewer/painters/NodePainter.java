/*
 * NodePainter.java
 *
 * Copyright (C) 2012 Andrew Rambaut
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

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
