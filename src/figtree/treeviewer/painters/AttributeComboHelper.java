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
    public static final String NAMES = "Names";
    public static final String NODE_AGES = "Node ages";
    public static final String NODE_HEIGHTS = "Node heights (raw)";
    public static final String BRANCH_TIMES = "Branch times";
    public static final String BRANCH_LENGTHS = "Branch lengths (raw)";


    public enum Intent {
        NODE,
        BRANCH,
        TIP
    };


    public AttributeComboHelper(final JComboBox attributeComboBox, final TreeViewer treeViewer) {
        treeViewer.addTreeViewerListener(new TreeViewerListener() {
            public void treeChanged() {
                List<Tree> trees = treeViewer.getTrees();

                Object selected = attributeComboBox.getSelectedItem();

                attributeComboBox.removeAllItems();

                attributeComboBox.addItem("User Selection");
                if (trees == null) {
                    return;
                }
                List<String> names = new ArrayList<String>();
                for (Tree tree : trees) {
                    for (String name : getAttributeNames(tree.getNodes())) {
                        if (!names.contains(name)) {
                            names.add(name);
                        }
                    }
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

//    public List<String> setupAttributes(Collection<? extends Tree> trees, Intent intent) {
//
//        List<String> attributeNames = new ArrayList<String>();
//
//        Set<String> nodeAttributes = new TreeSet<String>();
//        if (trees != null) {
//            for (Tree tree : trees) {
//                if (intent == Intent.TIP) {
//                    for (Node node : tree.getExternalNodes()) {
//                        nodeAttributes.addAll(node.getAttributeNames());
//                    }
//                    for (Taxon taxon : tree.getTaxa()) {
//                        nodeAttributes.addAll(taxon.getAttributeNames());
//                    }
//                } else if (intent == Intent.NODE) {
//                    for (Node node : tree.getInternalNodes()) {
//                        nodeAttributes.addAll(node.getAttributeNames());
//                    }
//                } else {
//                    for (Node node : tree.getNodes()) {
//                        nodeAttributes.addAll(node.getAttributeNames());
//                    }
//                }
//            }
//        }
//
//        switch( intent ) {
//            case TIP: {
//                attributeNames.add(NAMES);
//                attributeNames.add(NODE_AGES);
//                attributeNames.add(NODE_HEIGHTS);
//                attributeNames.add(BRANCH_TIMES);
//                attributeNames.add(BRANCH_LENGTHS);
//                break;
//            }
//            case NODE: {
//                if (nodeAttributes.contains("!name")) {
//                    attributeNames.add(NAMES);
//                }
//                attributeNames.add(NODE_AGES);
//                attributeNames.add(NODE_HEIGHTS);
//                attributeNames.add(BRANCH_TIMES);
//                attributeNames.add(BRANCH_LENGTHS);
//                break;
//            }
//            case BRANCH: {
//                if (nodeAttributes.contains("!name")) {
//                    attributeNames.add(NAMES);
//                }
//                attributeNames.add(BRANCH_TIMES);
//                attributeNames.add(BRANCH_LENGTHS);
//                attributeNames.add(NODE_AGES);
//                attributeNames.add(NODE_HEIGHTS);
//                break;
//            }
//        }
//
//        for (String attributeName : nodeAttributes) {
//            if (!attributeName.startsWith("!")) {
//                attributeNames.add(attributeName);
//            }
//        }
//
//       return attributeNames;
//    }

    private static String[] getAttributeNames(Collection<? extends Attributable> items) {
        java.util.Set<String> attributeNames = new TreeSet<String>();

        for (Attributable item : items) {
            for (String name : item.getAttributeNames()) {
                if (!name.startsWith("!")) {
                    Object attr = item.getAttribute(name);
                    if (!(attr instanceof Object[])) {
                        attributeNames.add(name);
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

                        if (isColouring) {
                            attributeNames.add(name + " *");
                        }

                    }
                }
            }
        }

        String[] attributeNameArray = new String[attributeNames.size()];
        attributeNames.toArray(attributeNameArray);

        return attributeNameArray;
    }

}
