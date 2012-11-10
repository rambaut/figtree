/*
 * AttributeComboHelper.java
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

import figtree.treeviewer.TreeViewer;
import figtree.treeviewer.TreeViewerListener;
import figtree.treeviewer.decorators.ColourDecorator;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.Tree;
import jebl.util.Attributable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class AttributeComboHelper {

    public AttributeComboHelper(final JComboBox attributeComboBox, final TreeViewer treeViewer) {

        this(attributeComboBox, treeViewer, null, null, false, false);

    }

    public AttributeComboHelper(final JComboBox attributeComboBox, final TreeViewer treeViewer,
                                final String defaultOption) {

        this(attributeComboBox, treeViewer, defaultOption, null, false, false);

    }

    public AttributeComboHelper(final JComboBox attributeComboBox, final TreeViewer treeViewer,
                                final String defaultOption,
                                final boolean numericalOnly,
                                final boolean includeLineageColourings) {

        this(attributeComboBox, treeViewer, defaultOption, null, numericalOnly, includeLineageColourings);

    }

    public AttributeComboHelper(final JComboBox attributeComboBox, final TreeViewer treeViewer,
                                final LabelPainter.PainterIntent intent) {

        this(attributeComboBox, treeViewer, null, intent, false, false);

    }

    public AttributeComboHelper(final JComboBox attributeComboBox, final TreeViewer treeViewer,
                                final LabelPainter.PainterIntent intent,
                                final boolean numericalOnly) {

        this(attributeComboBox, treeViewer, null, intent, numericalOnly, false);
    }

    public AttributeComboHelper(final JComboBox attributeComboBox, final TreeViewer treeViewer,
                                final String defaultOption,
                                final LabelPainter.PainterIntent intent,
                                final boolean numericalOnly,
                                final boolean includeLineageColourings) {

        treeViewer.addTreeViewerListener(new TreeViewerListener() {
            public void treeChanged() {
                List<Tree> trees = treeViewer.getTrees();

                Object selected = attributeComboBox.getSelectedItem();

                addingItems = true;

                attributeComboBox.removeAllItems();

                if (defaultOption != null) {
                    attributeComboBox.addItem(defaultOption);
                }

                if (trees == null) {
                    return;
                }
                List<String> names = new ArrayList<String>();
                if (intent != null || numericalOnly) {
                    getAttributeNames(names, trees, intent, numericalOnly);
                } else {
                    getAttributeNames(names, trees, includeLineageColourings);
                }

                for (String name : names) {
                    attributeComboBox.addItem(name);
                }

                addingItems = false;

                if (selected != null) {
                    attributeComboBox.setSelectedItem(selected);
                }
            }

            public void treeSettingsChanged() {
                // nothing to do
            }
        });

        attributeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!addingItems) {
                    fireAttributeSelectionChanged();
                }
            }
        });
    }

    public static void getAttributeNames(List<String> attributeNames, Collection<? extends Tree> trees,
                                         LabelPainter.PainterIntent intent,
                                         boolean numericalOnly) {

        Set<String> nodeAttributes = new TreeSet<String>();
        if (trees != null) {
            for (Tree tree : trees) {
                if (intent == LabelPainter.PainterIntent.TIP) {
                    for (Node node : tree.getExternalNodes()) {
                        nodeAttributes.addAll(node.getAttributeNames());
                    }
                    for (Taxon taxon : tree.getTaxa()) {
                        nodeAttributes.addAll(taxon.getAttributeNames());
                    }
                } else if (intent == LabelPainter.PainterIntent.NODE) {
                    for (Node node : tree.getInternalNodes()) {
                        nodeAttributes.addAll(node.getAttributeNames());
                    }
                } else if (intent == LabelPainter.PainterIntent.RANGE) {
                    for (Node node : tree.getInternalNodes()) {
                        for (String name : node.getAttributeNames()) {
                            Object attr = node.getAttribute(name);
                            if (attr instanceof Object[]) {
                                Object[] array = (Object[])attr;
                                if (array.length == 2 &&
                                        array[0] instanceof Double &&
                                        array[1] instanceof Double) {
                                    nodeAttributes.add(name);
                                }
                            }
                        }
                    }
                } else {
                    for (Node node : tree.getNodes()) {
                        for (String key : node.getAttributeMap().keySet()) {
                            nodeAttributes.add(key);
                        }
                    }
                }
            }
        }

        if (numericalOnly) {
            Set<String> continuousNodeAttributes = new TreeSet<String>();
            for (String nodeAttributeName : nodeAttributes) {
                boolean isNumerical = true;
                for (Tree tree : trees) {
                    if (!ColourDecorator.isNumerical(nodeAttributeName, tree.getNodes())) {
                        isNumerical = false;
                    }
                }
                if (isNumerical) {
                    continuousNodeAttributes.add(nodeAttributeName);
                }
            }
            nodeAttributes = continuousNodeAttributes;
        }

        if (intent != null) {
            switch( intent ) {
                case TIP: {
                    attributeNames.add(LabelPainter.NAMES);
                    attributeNames.add(LabelPainter.NODE_AGES);
                    attributeNames.add(LabelPainter.NODE_HEIGHTS);
                    attributeNames.add(LabelPainter.BRANCH_TIMES);
                    attributeNames.add(LabelPainter.BRANCH_LENGTHS);
                    break;
                }
                case NODE: {
                    if (nodeAttributes.contains("!name")) {
                        attributeNames.add(LabelPainter.NAMES);
                    }
                    attributeNames.add(LabelPainter.NODE_AGES);
                    attributeNames.add(LabelPainter.NODE_HEIGHTS);
                    attributeNames.add(LabelPainter.BRANCH_TIMES);
                    attributeNames.add(LabelPainter.BRANCH_LENGTHS);
                    break;
                }
                case BRANCH: {
                    if (nodeAttributes.contains("!name")) {
                        attributeNames.add(LabelPainter.NAMES);
                    }
                    attributeNames.add(LabelPainter.BRANCH_TIMES);
                    attributeNames.add(LabelPainter.BRANCH_LENGTHS);
                    attributeNames.add(LabelPainter.NODE_AGES);
                    attributeNames.add(LabelPainter.NODE_HEIGHTS);
                    break;
                }
            }
        }

        for (String attributeName : nodeAttributes) {
            if (!attributeName.startsWith("!")) {
                attributeNames.add(attributeName);
            }
        }
    }

    private static void getAttributeNames(List<String> attributeNames, Collection<? extends Tree> trees, final boolean includeLineageColourings) {
        for (Tree tree : trees) {
            Set<String> attributes = new TreeSet<String>();

            for (Attributable item : tree.getNodes()) {
                for (String name : item.getAttributeNames()) {
                    if (!name.startsWith("!")) {
                        Object attr = item.getAttribute(name);
                        if (!(attr instanceof Object[])) {
                            attributes.add(name);
                        } else {
                            boolean isColouring = true;

                            Object[] array = (Object[])attr;
                            boolean isIndex = true;
                            for (Object element : array) {
                                if (isIndex && !(element instanceof Integer) ||
                                        !isIndex && !(element instanceof Double)) {
                                    isColouring = false;
                                    break;
                                }
                                isIndex = !isIndex;
                            }

                            if (isIndex) {
                                // a colouring should finish on an index (which means isIndex should be false)...
                                isColouring = false;
                            }

                            if (isColouring && includeLineageColourings) {
                                attributes.add(name + " *");
                            }

                        }
                    }
                }
            }

            attributeNames.addAll(attributes);
        }
    }

    public void addListener(AttributeComboHelperListener listener) {
        listeners.add(listener);
    }

    private void fireAttributeSelectionChanged() {
        for (AttributeComboHelperListener listener : listeners) {
            listener.attributeComboChanged();
        }
    }

    private final List<AttributeComboHelperListener> listeners = new ArrayList<AttributeComboHelperListener>();
    private boolean addingItems = false;
}
