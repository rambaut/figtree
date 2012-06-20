package figtree.treeviewer;

import figtree.treeviewer.decorators.*;
import jam.controlpalettes.AbstractController;
import jam.controlpalettes.ControllerListener;
import jebl.evolution.graphs.Node;
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
public class AttributeColourController extends AbstractController {

    public static final String CONTROLLER_KEY = "colour";

    public AttributeColourController(final TreeViewer treeViewer, final JFrame frame) {
        this.treeViewer = treeViewer;
        this.frame = frame;

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
        fireAttributesChanged();
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

    public void setupControls(
            final JComboBox colourAttributeCombo,
            final JButton colourSetupButton) {

        addListener(new Listener() {
            @Override
            public void attributesChanged() {
                Object selected = colourAttributeCombo.getSelectedItem();

                editingComboBox = true;

                colourAttributeCombo.removeAllItems();

                colourAttributeCombo.addItem("User Selection");

                for (String name : getAttributeNames()) {
                    colourAttributeCombo.addItem(name);
                }

                editingComboBox = false;

                colourAttributeCombo.setSelectedItem(selected);
            }
        });

        colourSetupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Decorator decorator = null;
                if (colourAttributeCombo.getSelectedIndex() > 0) {
                    String attribute = (String) colourAttributeCombo.getSelectedItem();
                    decorator = getDecoratorForAttribute(attribute);
                }

                boolean update = false;
                if (decorator instanceof HSBDiscreteColorDecorator) {
                    if (discreteColourScaleDialog == null) {
                        discreteColourScaleDialog = new DiscreteColourScaleDialog(frame);
                    }
                    discreteColourScaleDialog.setDecorator((HSBDiscreteColorDecorator)decorator);
                    int result = discreteColourScaleDialog.showDialog();
                    if (result != JOptionPane.CANCEL_OPTION && result != JOptionPane.CLOSED_OPTION) {
                        discreteColourScaleDialog.setupDecorator((HSBDiscreteColorDecorator)decorator);
                        update = true;
                    }
                } else if (decorator instanceof HSBContinuousColorDecorator) {
                    if (continuousColourScaleDialog == null) {
                        continuousColourScaleDialog = new ContinuousColourScaleDialog(frame);
                    }
                    continuousColourScaleDialog.setDecorator((HSBContinuousColorDecorator)decorator);
                    int result = continuousColourScaleDialog.showDialog();
                    if (result != JOptionPane.CANCEL_OPTION && result != JOptionPane.CLOSED_OPTION) {
                        continuousColourScaleDialog.setupDecorator((HSBContinuousColorDecorator)decorator);
                        update = true;
                    }
                } else {
                    throw new IllegalArgumentException("Unsupported decorator type");
//                    if (continuousColourScaleDialog == null) {
//                        continuousColourScaleDialog = new OldContinuousColourScaleDialog(frame, branchColourSettings);
//                    }
//                    int result = continuousColourScaleDialog.showDialog();
//                    if (result != JOptionPane.CANCEL_OPTION && result != JOptionPane.CLOSED_OPTION) {
//                        continuousColourScaleDialog.getSettings(branchColourSettings);
//                        setupBranchDecorators();
//                    }
                }

                if (update) {
                    if (colourAttributeCombo.getSelectedIndex() > 0) {
                        String attribute = (String) colourAttributeCombo.getSelectedItem();
                        setDecoratorForAttribute(attribute, decorator);
                    }
                    fireControllerChanged();
                }
            }
        });

        colourAttributeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (!editingComboBox) {
                    fireControllerChanged();
                }
            }
        });
    }

    public Decorator getColourDecorator(JComboBox colourAttributeCombo, Decorator defaultDecorator) {
        Decorator colourDecorator = defaultDecorator;
        if (colourAttributeCombo.getSelectedIndex() > 0) {
            String attribute = (String) colourAttributeCombo.getSelectedItem();
            if (attribute != null && attribute.length() > 0) {
                colourDecorator = getDecoratorForAttribute(attribute);
            }
        }
        return colourDecorator;
    }

    public Decorator getDecoratorForAttribute(String attribute) {
        Decorator colourDecorator = attributeDecoratorMap.get(attribute);

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
        } else if (colourDecorator instanceof DiscreteColorDecorator) {
            ((DiscreteColorDecorator)colourDecorator).setAttributes(attribute, nodes);
        }

        return colourDecorator;
    }

    private void setDecoratorForAttribute(String attribute, Decorator decorator) {
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
    public void setSettings(Map<String, Object> settings) {
        for (String key : settings.keySet()) {
            if (key.startsWith(CONTROLLER_KEY + ".")) {
                Decorator decorator = null;

                String attribute = key.substring(CONTROLLER_KEY.length() + 1);
                String colourSettings = (String)settings.get(key);
                if (colourSettings.startsWith("HSBDiscrete")) {
                    decorator = new HSBDiscreteColorDecorator(attribute, colourSettings.substring("HSBDiscrete".length()));
                } else if (colourSettings.startsWith("HSBContinuous")) {
                    decorator = new HSBContinuousColorDecorator(attribute, colourSettings.substring("HSBContinuous".length()));
                } else {
//                    throw new IllegalArgumentException("Unrecognized colour decorator type");
                }

                attributeDecoratorMap.put(attribute, decorator);
            }
        }
    }

    @Override
    public void getSettings(Map<String, Object> settings) {
        for (String attribute : attributeDecoratorMap.keySet()) {
            Decorator decorator = attributeDecoratorMap.get(attribute);
            String colourSettings = decorator.toString();
            String name = "";
            if (decorator instanceof HSBDiscreteColorDecorator) {
                name = "HSBDiscrete";
            } else if (decorator instanceof HSBContinuousColorDecorator) {
                name = "HSBContinuous";
            } else {
                throw new IllegalArgumentException("Unrecognized colour decorator type");
            }
            settings.put(CONTROLLER_KEY + "." + attribute, name + colourSettings);
        }
    }

    private final TreeViewer treeViewer;
    private final JFrame frame;

    private Map<String, Decorator> attributeDecoratorMap = new HashMap<String, Decorator>();
    private Map<String, ContinuousScale> attributeScaleMap = new HashMap<String, ContinuousScale>();

    private List<String> attributeNames = new ArrayList<String>();

    private boolean editingComboBox = false;

    private ContinuousColourScaleDialog continuousColourScaleDialog = null;
    private DiscreteColourScaleDialog discreteColourScaleDialog = null;

    // an internal listener interface
    private interface Listener {
        void attributesChanged();
    }

    private void addListener(Listener listener) {
        listeners.add(listener);
    }

    private void fireAttributesChanged() {
        for (Listener listener : listeners) {
            listener.attributesChanged();
        }

    }
    private final List<Listener> listeners = new ArrayList<Listener>();

}
