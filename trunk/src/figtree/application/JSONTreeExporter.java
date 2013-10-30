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

    public final static Set<String> ATTRIBUTE_NAMES = new TreeSet<String>(Arrays.asList(new String[] { "location", "host", "Hx", "Nx", "posterior" }));
    public final static String ORIGIN = "2013.34520547945";

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
        int indent = 0;
        int treeCount = 0;

        writer.println("{");

        if (trees.size() > 1) {
            writer.println("\t\"trees\": [");
            indent ++;
        }

        Map<String, Set<String>> attributeMap = new LinkedHashMap<String, Set<String>>();

        for( Tree t : trees ) {
            final boolean isRooted = t instanceof RootedTree;
            final RootedTree rtree = isRooted ? (RootedTree)t : Utils.rootTheTree(t);

            for (Node node : rtree.getNodes()) {
                for (String name : node.getAttributeNames()) {
                    if (ATTRIBUTE_NAMES.contains(name)) {
                        Object valueObject = node.getAttribute(name);
                        if (valueObject instanceof String) {
                            Set<String> values = attributeMap.get(name);
                            if (values == null) {
                                values = new TreeSet<String>();
                                attributeMap.put(name, values);
                            }
                            String value = (String)valueObject;
                            if (value.contains("+")) {
                                values.add(value.split("\\+")[0]);
                            } else {
                                values.add(valueObject.toString());
                            }
                        }
                    }
                }
            }

            StringBuilder builder = new StringBuilder();

            if (trees.size() > 1) {
                appendIndent(builder, indent);
                builder.append("\"tree\": {\n");
            }

            appendIndent(builder, indent + 1);
            builder.append("\"root\": ");
            appendTree(rtree, rtree.getRootNode(), builder, indent + 1);

            appendAttributes(rtree, builder, indent + 1);
            builder.append((trees.size() == 1 || treeCount < trees.size() - 1 ? ",\n" : "\n"));

            if (trees.size() > 1) {
                appendIndent(builder, indent);
                builder.append((treeCount < trees.size() - 1 ? "},\n" : "}\n"));
            }

            writer.println(builder);

            treeCount ++;
        }

        if (trees.size() > 1) {
            writer.println("\t],");
        }

        writer.println("\t\"origin\":\"" + ORIGIN + "\",");

        int i = 0;
        for (String name : attributeMap.keySet()) {
            Set<String> values = attributeMap.get(name);
            writer.println("\t\"" + name + ".fullSet\": [");
            int j = 0;
            for (String value : values) {
                writer.println("\t\t\"" + value + "\"" + (j < values.size() - 1 ? "," : ""));
                j++;
            }
            writer.println("\t]" + (i < attributeMap.keySet().size() - 1 ? "," : ""));
            i++;
        }


        writer.println("}");
    }

    private void appendIndent(StringBuilder builder, int indent) {
        for (int i = 0; i < indent; i++) {
            builder.append('\t');
        }
    }

    @Override
    public void exportTrees(Collection<? extends Tree> trees) throws IOException {
        writeTrees(trees);
    }

    final private String nameRegex = "^(\\w|-)+$";


    /**
     * name suitable for printing - quotes if necessary
     * @param taxon
     * @param builder
     * @return
     */
    private StringBuilder appendTaxonName(Taxon taxon, StringBuilder builder) {
        String name = taxon.getName();
        if (name.contains("\"")) {
            name = name.replace("\"", "\\\"");
        }
        return builder.append("\"name\": \"").append(name).append("\"");
    }

    /**
     * Prepare for writing a tree. If a taxa block exists and is suitable for tree,
     * do nothing. If not, write a new taxa block.
     * @param tree
     * @param node
     * @param builder
     */
    private void appendTree(RootedTree tree, Node node, StringBuilder builder, int indent) {
        builder.append("{\n");

        if (tree.isExternal(node)) {
            appendIndent(builder, indent + 1);
            appendTaxonName(tree.getTaxon(node), builder);
        } else {
            appendIndent(builder, indent + 1);
            builder.append("\"children\": [\n");
            java.util.List<Node> children = tree.getChildren(node);
            final int last = children.size() - 1;
            for (int i = 0; i < children.size(); i++) {
                appendIndent(builder, indent + 2);
                appendTree(tree, children.get(i), builder, indent + 2);
                builder.append(i == last ? "\n" : ",\n");
            }
            appendIndent(builder, indent + 1);
            builder.append("]");
        }

        Node parent = tree.getParent(node);
        if (parent != null) {
            if (tree.hasLengths()) {
                builder.append(",\n");
                appendIndent(builder, indent + 1);
                builder.append("\"length\": ").append(tree.getLength(node));
            }
        }

        builder.append(",\n");
        appendIndent(builder, indent + 1);
        builder.append("\"height\": ").append(tree.getHeight(node));

        appendAttributes(node, builder, indent + 1);

        builder.append("\n");

        appendIndent(builder, indent);
        builder.append("}");
    }

    public static double roundDouble(double value, int decimalPlace) {
        double power_of_ten = 1;
        while (decimalPlace-- > 0)
            power_of_ten *= 10.0;
        return Math.round(value * power_of_ten) / power_of_ten;
    }

    private StringBuilder appendAttributes(Attributable item, StringBuilder builder, int indent) {
        for( String key : item.getAttributeNames() ) {
            if (!key.startsWith("&") && ATTRIBUTE_NAMES.contains(key)) {
                builder.append(",\n");
                appendIndent(builder, indent);

                builder.append("\"").append(key).append("\": ");

                Object value = item.getAttribute(key);
                appendAttributeValue(value, builder);
            }
        }

        return builder;
    }

    private StringBuilder appendAttributeValue(Object value, StringBuilder builder) {
        if (value instanceof Object[]) {
            builder.append("[");
            Object[] elements = ((Object[])value);

            if (elements.length > 0) {
                appendAttributeValue(elements[0], builder);
                for (int i = 1; i < elements.length; i++) {
                    builder.append(",");
                    appendAttributeValue(elements[i], builder);
                }
            }
            return builder.append("]");
        }

        if (value instanceof Color) {
            return builder.append("#").append(((Color)value).getRGB());
        }

        if (value instanceof String) {
            if (((String) value).contains("+")) {
                return builder.append("\"").append(((String) value).split("\\+")[0]).append("\"");
            }
            return builder.append("\"").append(value).append("\"");
        }

        return builder.append(value);
    }



    private Set<Taxon> taxa = null;
    protected final PrintWriter writer;
    private boolean writeMetaComments;
}
