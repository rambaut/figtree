package figtree.treeviewer;

import figtree.treeviewer.decorators.*;
import jam.controlpalettes.AbstractController;
import jebl.evolution.graphs.Node;
import jebl.evolution.trees.Tree;
import jebl.util.Attributable;

import javax.swing.*;
import java.util.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class AttributeColourController extends AbstractController {

    public static final String CONTROLLER_KEY = "colour";

    public AttributeColourController(final TreeViewer treeViewer) {
        this.treeViewer = treeViewer;

        setupAttributeNames(treeViewer.getTrees());

        treeViewer.addTreeViewerListener(new TreeViewerListener() {
            public void treeChanged() {
                setupAttributeNames(treeViewer.getTrees());
            }

            public void treeSettingsChanged() {
                // nothing to do
            }
        });
    }

    private void setupAttributeNames(Collection<? extends Tree> trees) {
        if (trees == null) {
            return;
        }
        attributeNames = new ArrayList<String>();
        for (Tree tree : trees) {
            for (String name : getAttributeNames(tree.getNodes())) {
                if (!attributeNames.contains(name)) {
                    attributeNames.add(name);
                }
            }
        }
        fireControllerChanged();
    }

    private String[] getAttributeNames(Collection<? extends Attributable> items) {
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

    public List<String> getAttributeNames() {
        return attributeNames;
    }

    public Decorator getDecoratorForAttribute(String attribute) {
        Decorator colourDecorator = null;

        Set<Node> nodes = new HashSet<Node>();
        for (Tree tree : treeViewer.getTrees()) {
            for (Node node : tree.getNodes()) {
                nodes.add(node);
            }
        }

        if (colourDecorator == null) {
            if (attribute.endsWith("*")) {
                // todo reinstate branch colouring
                return null;
            } else if (DiscreteColorDecorator.isDiscrete(attribute, nodes)) {
                colourDecorator = new HSBDiscreteColorDecorator(attribute, nodes);
            } else {
                ContinuousScale scale = attributeScaleMap.get(attribute);
                if (scale == null) {
                    scale = new ContinuousScale();
                    attributeScaleMap.put(attribute, scale);
                }

                colourDecorator = new HSBContinuousColorDecorator(scale);

            }
            setDecoratorForAttribute(attribute, colourDecorator);
        }
        return attributeDecoratorMap.get(attribute);
    }

    public void setDecoratorForAttribute(String attribute, Decorator decorator) {
        attributeDecoratorMap.put(attribute, decorator);
    }

    @Override
    public JComponent getTitleComponent() {
        return null;
    }

    @Override
    public JPanel getPanel() {
        return null;
    }

    @Override
    public boolean isInitiallyVisible() {
        return false;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void getSettings(Map<String, Object> settings) {
        for (String key : settings.keySet()) {
            if (key.startsWith(CONTROLLER_KEY + ".")) {
                Decorator decorator = null;

                String attribute = key.substring(CONTROLLER_KEY.length() + 1);
                String colourSettings = (String)settings.get(key);
                if (colourSettings.startsWith("HSBDiscrete")) {
                    decorator = new HSBDiscreteColorDecorator(colourSettings.substring("HSBDiscrete".length()));
                } else if (colourSettings.startsWith("HSBContinuous")) {
                    decorator = new HSBContinuousColorDecorator(colourSettings.substring("HSBContinuous".length()));
                } else {
                    throw new IllegalArgumentException("Unrecognized colour decorator type");
                }

                attributeDecoratorMap.put(attribute, decorator);
            }
        }
    }

    @Override
    public void setSettings(Map<String, Object> settings) {
        for (String attribute : attributeDecoratorMap.keySet()) {
            Decorator decorator = attributeDecoratorMap.get(attribute);
            String colourSettings = decorator.toString();
            settings.put(CONTROLLER_KEY + "." + attribute, colourSettings);
        }
    }

    private final TreeViewer treeViewer;

    private Map<String, Decorator> attributeDecoratorMap = new HashMap<String, Decorator>();
    private Map<String, ContinuousScale> attributeScaleMap = new HashMap<String, ContinuousScale>();

    private List<String> attributeNames = new ArrayList<String>();
}
