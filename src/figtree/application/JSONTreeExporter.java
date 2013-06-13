/*
 * FigTreeNexusExporter.java
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

package figtree.application;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.distances.DistanceMatrix;
import jebl.evolution.graphs.Node;
import jebl.evolution.io.NexusExporter;
import jebl.evolution.io.NexusImporter;
import jebl.evolution.io.TreeExporter;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;
import jebl.evolution.trees.Utils;
import jebl.util.Attributable;

import java.awt.*;
import java.awt.List;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

/**
 * @author Andrew Rambaut
 * @version $Id: FigTreeNexusExporter.java,v 1.2 2006/08/27 15:17:36 rambaut Exp $
 */
public class JSONTreeExporter implements TreeExporter {
    public static final String treeNameAttributeKey = "name";

    public JSONTreeExporter(Writer writer) {
        this(writer, true);
    }

    /**
     *
     * @param writer where export text goes
     */
    public JSONTreeExporter(Writer writer, boolean writeMetaComments) {
        this.writeMetaComments = writeMetaComments;
        this.writer = new PrintWriter(writer);
    }

    /**
     * Export a single tree
     *
     * @param tree
     * @throws java.io.IOException
     */
    @Override
    public void exportTree(Tree tree) throws IOException {
        java.util.List<Tree> trees = new ArrayList<Tree>();
        trees.add(tree);
        exportTrees(trees);
    }

    private void writeTrees(Collection<? extends Tree> trees) throws IOException {
        int nt = 0;
        for( Tree t : trees ) {
            final boolean isRooted = t instanceof RootedTree;
            final RootedTree rtree = isRooted ? (RootedTree)t : Utils.rootTheTree(t);

            final Object name = t.getAttribute(treeNameAttributeKey);

            ++nt;
            final String treeName = (name != null) ? NexusImporter.makeIntoAllowableIdentifier(name.toString()) : "tree_" + nt;

            StringBuilder builder = new StringBuilder("\ttree ");

            builder.append(treeName);
            builder.append(" = ");

            // TREE & UTREE are depreciated in the NEXUS format in favour of a metacomment
            // [&U] or [&R] after the TREE command. Andrew.
            // TT: The [&U], [&R] should actually come *after* the " = " and be uppercase, see
            // e.g. tree_rest in http://www.cs.nmsu.edu/~epontell/nexus/nexus_grammar .
            // Before 2008-05-05 we incorrectly inserted it before the treeName.
            builder.append(isRooted && !rtree.conceptuallyUnrooted() ? "[&R] " : "[&U] ");

            appendAttributes(rtree, builder);

            appendTree(rtree, rtree.getRootNode(), builder);
            builder.append(";");

            writer.println(builder);
        }
    }

    @Override
    public void exportTrees(Collection<? extends Tree> trees) throws IOException {
        writeTrees(trees);
    }

    final private String nameRegex = "^(\\w|-)+$";

    /**
     * Name suitable as token - quotes if necessary
     * @param name to check
     * @return the name
     */
    private String safeName(String name) {
        // allow dash in names

        if (!name.matches(nameRegex)) {
            name = name.replace("\'", "\'\'");
            return "\'" + name + "\'";
        }
        return name;
    }

    /**
     * name suitable for printing - quotes if necessary
     * @param taxon
     * @param builder
     * @return
     */
    private StringBuilder appendTaxonName(Taxon taxon, StringBuilder builder) {
        String name = taxon.getName();
        if (!name.matches(nameRegex)) {
            // JEBL way of quoting the quote character
            name = name.replace("\'", "\'\'");
            builder.append("\'").append(name).append("\'");
            return builder;
        }
        return builder.append(name);
    }

    /**
     * Prepare for writing a tree. If a taxa block exists and is suitable for tree,
     * do nothing. If not, write a new taxa block.
     * @param tree
     * @param node
     * @param builder
     */
    private void appendTree(RootedTree tree, Node node, StringBuilder builder) {
        if (tree.isExternal(node)) {
            appendTaxonName(tree.getTaxon(node), builder);

            appendAttributes(node, builder);

            if( tree.hasLengths() ) {
                builder.append(':');
                builder.append(roundDouble(tree.getLength(node), 6));
            }
        } else {
            builder.append('(');
            java.util.List<Node> children = tree.getChildren(node);
            final int last = children.size() - 1;
            for (int i = 0; i < children.size(); i++) {
                appendTree(tree, children.get(i), builder);
                builder.append(i == last ? ')' : ',');
            }

            appendAttributes(node, builder);

            Node parent = tree.getParent(node);
            // Don't write root length. This is ignored elsewhere and the nexus importer fails
            // whet it is present.
            if (parent != null) {
                if (tree.hasLengths()) {
                    builder.append(":").append(roundDouble(tree.getLength(node), 6));
                }
            }
        }
    }

    public static double roundDouble(double value, int decimalPlace) {
        double power_of_ten = 1;
        while (decimalPlace-- > 0)
            power_of_ten *= 10.0;
        return Math.round(value * power_of_ten) / power_of_ten;
    }

    private StringBuilder appendAttributes(Attributable item, StringBuilder builder) {
        if (!writeMetaComments) {
            return builder;
        }

        boolean first = true;
        for( String key : item.getAttributeNames() ) {
            // we should replace the explicit check for name by something more general.
            // Like a reserved character at the start (here &). however we have to worry about backward
            // compatibility so no change yet with name.
            boolean exclude = false;
            if( !exclude && !key.startsWith("&") ) {
                if (first) {
                    builder.append("[&");
                    first = false;
                } else {
                    builder.append(",");
                }

                if( key.indexOf(' ') < 0 ) {
                    builder.append(key);
                } else {
                    builder.append("\"").append(key).append("\"");
                }

                builder.append('=');

                Object value = item.getAttribute(key);
                appendAttributeValue(value, builder);
            }
        }
        if (!first) {
            builder.append("]");
        }

        return builder;
    }

    private StringBuilder appendAttributeValue(Object value, StringBuilder builder) {
        if (value instanceof Object[]) {
            builder.append("{");
            Object[] elements = ((Object[])value);

            if (elements.length > 0) {
                appendAttributeValue(elements[0], builder);
                for (int i = 1; i < elements.length; i++) {
                    builder.append(",");
                    appendAttributeValue(elements[i], builder);
                }
            }
            return builder.append("}");
        }

        if (value instanceof Color) {
            return builder.append("#").append(((Color)value).getRGB());
        }

        if (value instanceof String) {
            return builder.append("\"").append(value).append("\"");
        }

        return builder.append(value);
    }



    private Set<Taxon> taxa = null;
    protected final PrintWriter writer;
    private boolean writeMetaComments;
}
