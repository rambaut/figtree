package figtree.treeviewer;

import figtree.treeviewer.decorators.Decorator;
import figtree.treeviewer.decorators.HSBContinuousColorDecorator;
import figtree.treeviewer.decorators.HSBDiscreteColorDecorator;
import jam.controlpalettes.AbstractController;
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
        treeViewer.addTreeViewerListener(new TreeViewerListener() {
            public void treeChanged() {
                setupAttributes(treeViewer.getTrees());
            }

            public void treeSettingsChanged() {
                // nothing to do
            }
        });
    }

    private void setupAttributes(Collection<? extends Tree> trees) {
        Object selected = colourAttributeCombo.getSelectedItem();

        colourAttributeCombo.removeAllItems();

        colourAttributeCombo.addItem("User Selection");
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
            colourAttributeCombo.addItem(name);
        }

        colourAttributeCombo.setSelectedItem(selected);
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

    public Decorator getDecoratorForAttribute(String attribute) {
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

    private Map<String, Decorator> attributeDecoratorMap = new HashMap<String, Decorator>();

}
