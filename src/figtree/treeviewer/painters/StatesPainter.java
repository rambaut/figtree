/*
 * StatesPainter.java
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
import jebl.evolution.trees.Tree;
import figtree.treeviewer.decorators.DiscreteColourDecorator;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author Andrew Rambaut
 * @version $Id: CharactersPainter.java,v 1.18 2006/11/21 16:10:26 rambaut Exp $
 */
public class StatesPainter extends BasicLabelPainter {

    public StatesPainter(String[] annotationNames, DiscreteColourDecorator[] decorators) {
        super(PainterIntent.TIP);

        this.annotationNames = annotationNames;
        this.decorators = decorators;
    }

    public Rectangle2D calibrate(Graphics2D g2, Node node) {
        super.calibrate(g2, node);

        int siteCount = annotationNames.length;
//        blockSize = super.getPreferredHeight() / 2;
//        blockGap = blockSize / 2;
        blockSize = super.getPreferredHeight() * 0.8;
        blockGap = 1;
        width = (siteCount * (blockSize + blockGap)) + blockGap + blockGap;
        return new Rectangle2D.Double(0.0, 0.0, getPreferredWidth(), getPreferredHeight());
    }

    public double getPreferredWidth() {
        return super.getPreferredWidth() + width;
    }

    public void paint(Graphics2D g2, Node node, Justification justification, Rectangle2D bounds) {

        Tree tree = getTree();

        Stroke oldStroke = g2.getStroke();
        Paint oldPaint = g2.getPaint();

        float yOffset = (float)getPreferredHeight() / 2;
        switch (justification) {
            case FLUSH:
            case LEFT: {
                super.paint(g2, node, justification,
                        new Rectangle2D.Double(bounds.getX() + width, bounds.getY(), bounds.getWidth() - width, bounds.getHeight()));


                double x =  blockGap;

                int i = 0;
                for (String name: annotationNames) {
                    Object item = tree.getTaxon(node).getAttribute(name);
                    if (item != null) {
                        decorators[i].setItem(item);
                        int state = ((Number)item).intValue();
                        // Now create a bounds for the label
                        Rectangle2D block = new Rectangle2D.Double(
                                x, yOffset - (blockSize / 2.0),
                                blockSize, blockSize);

                        g2.setPaint(decorators[i].getPaint(Color.white));
                        g2.fill(block);


                        g2.setPaint(Color.white);
                        g2.setFont(new Font("helvetica", Font.PLAIN, 9));
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString(Integer.toString(state), (float)(x+1), (float)(yOffset - (blockSize / 2.0) + fm.getAscent() + 1));
                        g2.setPaint(Color.black);
                        g2.setFont(new Font("helvetica", Font.PLAIN, 9));
                        g2.drawString(Integer.toString(state), (float)x, (float)(yOffset - (blockSize / 2.0) + fm.getAscent()));
                    }
                    x += blockSize + blockGap;
                    i++;
                }

            } break;
            case RIGHT: {
                super.paint(g2, node, justification,
                        new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth() - width, bounds.getHeight()));

                int i = 0;
                for (String name: annotationNames) {
                    double x = getPreferredWidth() - blockGap - blockSize;

                    Object item = tree.getTaxon(node).getAttribute(name);
                    if (item != null) {
                        decorators[i].setItem(item);
                        // Now create a bounds for the label
                        Rectangle2D block = new Rectangle2D.Double(
                                x, yOffset - (blockSize / 2.0),
                                blockSize, blockSize);

                        g2.setPaint(decorators[i].getPaint(Color.white));
                        g2.fill(block);

                        x -= (blockSize + blockGap);
                        i++;
                    }

                }
            } break;
            default:
                throw new IllegalArgumentException("Unrecognized alignment enum option");
        }


        g2.setStroke(oldStroke);
        g2.setPaint(oldPaint);
    }

    private String[] annotationNames = null;
    private DiscreteColourDecorator[] decorators;

    private double width;

    private double blockSize = 8;
    private double blockGap = 2;
}