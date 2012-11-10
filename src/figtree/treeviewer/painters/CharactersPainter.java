/*
 * CharactersPainter.java
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

import jebl.evolution.alignments.Alignment;
import jebl.evolution.graphs.Node;
import jebl.evolution.sequences.AminoAcids;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.trees.Tree;
import figtree.treeviewer.painters.BasicLabelPainter;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class CharactersPainter extends BasicLabelPainter {

    public CharactersPainter(Alignment characters) {
        super(BasicLabelPainter.PainterIntent.TIP);

        this.characters = characters;

        paints[AminoAcids.A_STATE.getIndex()] = new Color(204, 255, 255);
        paints[AminoAcids.C_STATE.getIndex()] = new Color(0, 255, 255);
        paints[AminoAcids.D_STATE.getIndex()] = new Color(255, 204, 153);
        paints[AminoAcids.E_STATE.getIndex()] = new Color(255, 204, 0);
        paints[AminoAcids.F_STATE.getIndex()] = new Color(0, 204, 255);
        paints[AminoAcids.G_STATE.getIndex()] = new Color(0, 255, 0);
        paints[AminoAcids.H_STATE.getIndex()] = new Color(255, 255, 153);
        paints[AminoAcids.I_STATE.getIndex()] = new Color(0, 0, 128);
        paints[AminoAcids.K_STATE.getIndex()] = new Color(198, 66, 0);
        paints[AminoAcids.L_STATE.getIndex()] = new Color(51, 102, 255);
        paints[AminoAcids.M_STATE.getIndex()] = new Color(153, 204, 255);
        paints[AminoAcids.N_STATE.getIndex()] = new Color(255, 153, 0);
        paints[AminoAcids.P_STATE.getIndex()] = new Color(255, 255, 0);
        paints[AminoAcids.Q_STATE.getIndex()] = new Color(255, 102, 0);
        paints[AminoAcids.R_STATE.getIndex()] = new Color(230, 6, 6);
        paints[AminoAcids.S_STATE.getIndex()] = new Color(204, 255, 153);
        paints[AminoAcids.T_STATE.getIndex()] = new Color(0, 255, 153);
        paints[AminoAcids.V_STATE.getIndex()] = new Color(0, 0, 255);
        paints[AminoAcids.W_STATE.getIndex()] = new Color(204, 153, 255);
        paints[AminoAcids.Y_STATE.getIndex()] = new Color(204, 255, 204);
        paints[AminoAcids.B_STATE.getIndex()] = Color.DARK_GRAY;
        paints[AminoAcids.Z_STATE.getIndex()] = Color.DARK_GRAY;
        paints[AminoAcids.X_STATE.getIndex()] = Color.GRAY;
        paints[AminoAcids.UNKNOWN_STATE.getIndex()] = Color.GRAY;
        paints[AminoAcids.STOP_STATE.getIndex()] = Color.GRAY;
        paints[AminoAcids.GAP_STATE.getIndex()] = Color.GRAY;
    };

    public Rectangle2D calibrate(Graphics2D g2, Node node) {
        super.calibrate(g2, node);

        int siteCount = characters.getSiteCount();
        blockSize = super.getPreferredHeight() / 2;
        blockGap = blockSize / 2;
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

                Sequence sequence = characters.getSequence(tree.getTaxon(node));
                if (sequence != null) {
                    double x =  blockGap;

                    for (int state : sequence.getStateIndices()) {
                        if (state != AminoAcids.GAP_STATE.getIndex()) {
                            // Now create a bounds for the label
                            Rectangle2D block = new Rectangle2D.Double(
                                    x, yOffset - (blockSize / 2.0),
                                    blockSize, blockSize);

                            g2.setPaint(getStatePaint(state));
                            g2.fill(block);
                        }
                        x += blockSize + blockGap;
                    }

                }
            } break;
            case RIGHT: {
                super.paint(g2, node, justification,
                        new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth() - width, bounds.getHeight()));

                Sequence sequence = characters.getSequence(tree.getTaxon(node));
                if (sequence != null) {
                    double x = getPreferredWidth() - blockGap - blockSize;

                    for (int state : sequence.getStateIndices()) {
                        // Now create a bounds for the label
                        Rectangle2D block = new Rectangle2D.Double(
                                x, yOffset - (blockSize / 2.0),
                                blockSize, blockSize);

                        g2.setPaint(getStatePaint(state));
                        g2.fill(block);

                        x -= (blockSize + blockGap);
                    }

                }
            } break;
            default:
                throw new IllegalArgumentException("Unrecognized alignment enum option");
        }


        g2.setStroke(oldStroke);
        g2.setPaint(oldPaint);
    }

    private Alignment characters = null;

    private double width;

    private double blockSize = 6;
    private double blockGap = 2;

    Paint[] paints = new Paint[AminoAcids.getStateCount()];

    public Paint getStatePaint(int stateIndex) {
        return paints[stateIndex];
    }
}
