/*
 * JVerticalLabel.java
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

package figtree.ui.components;

import javax.swing.*;

public class JVerticalLabel extends JLabel {
    private boolean clockwise;

    public JVerticalLabel(boolean clockwise) {
        super();
        this.clockwise = clockwise;
    }

    public JVerticalLabel(Icon image, boolean clockwise) {
        super(image);
        this.clockwise = clockwise;
    }

    public JVerticalLabel(Icon image, int horizontalAlignment, boolean clockwise) {
        super(image, horizontalAlignment);
        this.clockwise = clockwise;
    }

    public JVerticalLabel(String text, boolean clockwise) {
        super(text);
        this.clockwise = clockwise;
    }

    public JVerticalLabel(String text, Icon image, int horizontalAlignment, boolean clockwise) {
        super(text, image, horizontalAlignment);
        this.clockwise = clockwise;
    }

    public JVerticalLabel(String text, int horizontalAlignment, boolean clockwise) {
        super(text, horizontalAlignment);
        this.clockwise = clockwise;
    }

    public java.awt.Dimension getPreferredSize() {
        java.awt.Insets ins = getInsets();
        java.awt.FontMetrics fm = getFontMetrics(getFont());
        String text = getText();
        int h = fm.stringWidth(text), descent = fm.getDescent(),
                ascent = fm.getAscent();
        return new java.awt.Dimension(ins.top + ascent + descent + ins.bottom,
                ins.right + h + ins.left);
    }

    public void paint(java.awt.Graphics g) {
        java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();

        String text = getText();
        java.awt.Dimension size = getSize();
        java.awt.Insets ins = getInsets();

        java.awt.FontMetrics fm = g2d.getFontMetrics(getFont());
        int h = fm.stringWidth(text), x = ins.right;

        switch (getHorizontalAlignment()) {
            case SwingConstants.CENTER:
                x = (size.height - h + ins.right - ins.left) / 2;
                break;
            case SwingConstants.TOP:
                x = size.height - h - ins.left;
                break;
        }
        int descent = fm.getDescent(), ascent = fm.getAscent(),
                y = ins.top + ascent;
        switch (getVerticalAlignment()) {
            case SwingConstants.CENTER:
                y = (size.width + ascent - descent + ins.top - ins.bottom) / 2;
                break;
            case SwingConstants.RIGHT:
                y = size.width - descent - ins.bottom;
                break;
        }

        java.awt.geom.AffineTransform trans;

        if (clockwise) {
            trans = new java.awt.geom.AffineTransform(0, 1, -1, 0, -size.height, 0);
        } else {
            trans = new java.awt.geom.AffineTransform(0, -1, 1, 0, 0, size.height);
        }
        g2d.transform(trans);
        g2d.setPaintMode();
        if (isOpaque() && (getBackground() != null)) {
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, size.height, size.width);
        }
        g2d.setFont(getFont());
        g2d.setColor(getForeground());
        g2d.drawString(text, x, y);
        trans = null;
        g2d = null;
    }
}



