/*
 * ParsimonyBranchDecorator.java
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

package figtree.treeviewer.decorators;

import jebl.evolution.trees.Tree;
import jebl.evolution.graphs.Node;
import jebl.evolution.alignments.Pattern;
import jebl.evolution.parsimony.ParsimonyCriterion;
import jebl.evolution.parsimony.FitchParsimony;
import jebl.evolution.sequences.State;
import jebl.evolution.sequences.Nucleotides;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.AminoAcids;

import java.util.ArrayList;
import java.util.List;
import java.awt.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class ParsimonyBranchDecorator {

    public ParsimonyBranchDecorator(Pattern pattern) {
        List<Pattern> patterns = new ArrayList<Pattern>();
        patterns.add(pattern);
        parsimony = new FitchParsimony(patterns, true);

        if (pattern.getSequenceType() == SequenceType.NUCLEOTIDE) {
            paints = new Paint[Nucleotides.getStateCount()];
            paints[Nucleotides.A_STATE.getIndex()] = Color.RED;
            paints[Nucleotides.C_STATE.getIndex()] = Color.BLUE;
            paints[Nucleotides.G_STATE.getIndex()] = Color.BLACK;
            paints[Nucleotides.T_STATE.getIndex()] = Color.GREEN;
            paints[Nucleotides.R_STATE.getIndex()] = Color.MAGENTA;
            paints[Nucleotides.Y_STATE.getIndex()] = Color.DARK_GRAY;
            paints[Nucleotides.M_STATE.getIndex()] = Color.DARK_GRAY;
            paints[Nucleotides.W_STATE.getIndex()] = Color.DARK_GRAY;
            paints[Nucleotides.S_STATE.getIndex()] = Color.DARK_GRAY;
            paints[Nucleotides.K_STATE.getIndex()] = Color.DARK_GRAY;
            paints[Nucleotides.B_STATE.getIndex()] = Color.DARK_GRAY;
            paints[Nucleotides.D_STATE.getIndex()] = Color.DARK_GRAY;
            paints[Nucleotides.H_STATE.getIndex()] = Color.DARK_GRAY;
            paints[Nucleotides.V_STATE.getIndex()] = Color.DARK_GRAY;
            paints[Nucleotides.N_STATE.getIndex()] = Color.GRAY;
            paints[Nucleotides.UNKNOWN_STATE.getIndex()] = Color.GRAY;
            paints[Nucleotides.GAP_STATE.getIndex()] = Color.GRAY;
        } else if (pattern.getSequenceType() == SequenceType.AMINO_ACID) {
            paints = new Paint[AminoAcids.getStateCount()];
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
        } else {
            throw new IllegalArgumentException("Unsupported sequence type");
        }
    }

    public Paint getBranchPaint(Tree tree, Node node) {
        State[] states = parsimony.getStates(tree, node);

        return paints[states[0].getIndex()];
    }

    private final ParsimonyCriterion parsimony;
    private final Paint[] paints;
}