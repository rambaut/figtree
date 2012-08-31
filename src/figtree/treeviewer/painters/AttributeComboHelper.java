package figtree.treeviewer.painters;

import figtree.treeviewer.TreeViewer;
import figtree.treeviewer.TreeViewerListener;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.Tree;
import jebl.util.Attributable;

import javax.swing.*;
import java.util.*;

/**
 * Date: 21/06/2012
 * Time: 11:49
 *
 * @author rambaut
 */
public class AttributeComboHelper {

    public AttributeComboHelper(final JComboBox attributeComboBox, final TreeViewer treeViewer) {

        this(attributeComboBox, treeViewer, null, null, false);

    }

    public AttributeComboHelper(final JComboBox attributeComboBox, final TreeViewer treeViewer,
                                final String defaultOption) {

        this(attributeComboBox, treeViewer, defaultOption, null, false);

    }

    public AttributeComboHelper(final JComboBox attributeComboBox, final TreeViewer treeViewer,
                                final String defaultOption,
                                final boolean includeLineageColourings) {

        this(attributeComboBox, treeViewer, defaultOption, null, includeLineageColourings);

    }

    public AttributeComboHelper(final JComboBox attributeComboBox, final TreeViewer treeViewer,
                                final LabelPainter.PainterIntent intent) {

        this(attributeComboBox, treeViewer, null, intent, false);
    }

    public AttributeComboHelper(final JComboBox attributeComboBox, final TreeViewer treeViewer,
                                final String defaultOption,
                                final LabelPainter.PainterIntent intent,
                                final boolean includeLineageColourings) {

        treeViewer.addTreeViewerListener(new TreeViewerListener() {
            public void treeChanged() {
                List<Tree> trees = treeViewer.getTrees();

                Object selected = attributeComboBox.getSelectedItem();

                attributeComboBox.removeAllItems();

                if (defaultOption != null) {
                    attributeComboBox.addItem(defaultOption);
                }

                if (trees == null) {
                    return;
                }
                List<String> names = new ArrayList<String>();
                if (intent != null) {
                    getAttributeNames(names, trees, intent);
                } else {
                    getAttributeNames(names, trees, includeLineageColourings);
                }

                for (String name : names) {
                    attributeComboBox.addItem(name);
                }

                attributeComboBox.setSelectedItem(selected);
            }

            public void treeSettingsChanged() {
                // nothing to do
            }
        });

    }

    public void getAttributeNames(List<String> attributeNames, Collection<? extends Tree> trees, LabelPainter.PainterIntent intent) {

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
                        nodeAttributes.addAll(node.getAttributeNames());
                    }
                } else {
                    for (Node node : tree.getNodes()) {
                        for (String key : node.getAttributeMap().keySet()) {
                            if (node.getAttributeMap().get(key) instanceof Object[]) {
                                nodeAttributes.add(key);
                            }
                        }
                    }
                }
            }
        }

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

}
