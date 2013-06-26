package figtree.treeviewer;

import figtree.treeviewer.decorators.*;
import jam.controlpalettes.AbstractController;
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
    public static final String SCHEME_KEY = "scheme";
    public static final String ORDER_KEY = "order";

    public AttributeColourController(final TreeViewer treeViewer, final JFrame frame) {
        this.treeViewer = treeViewer;
        this.frame = frame;
    }

    public void setupControls(
            final JComboBox colourAttributeCombo,
            final JButton colourSetupButton) {

        if (colourSetupButton != null) {
            colourSetupButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    ColourDecorator decorator = null;
                    if (colourAttributeCombo.getSelectedIndex() > 0) {
                        String attribute = (String) colourAttributeCombo.getSelectedItem();
                        decorator = getDecoratorForAttribute(attribute);
                    }

                    if (decorator == null) {
                        return;
                    }

                    boolean update = false;
                    if (decorator instanceof HSBDiscreteColourDecorator) {
                        if (discreteColourScaleDialog == null) {
                            discreteColourScaleDialog = new DiscreteColourScaleDialog(frame);
                        }
                        discreteColourScaleDialog.setDecorator((DiscreteColourDecorator)decorator);
                        int result = discreteColourScaleDialog.showDialog();
                        if (result != JOptionPane.CANCEL_OPTION && result != JOptionPane.CLOSED_OPTION) {
                            decorator = discreteColourScaleDialog.getDecorator();

                            String attribute = (String) colourAttributeCombo.getSelectedItem();
                            setDecoratorForAttribute(attribute, decorator);

                            update = true;
                        }
                    } else if (decorator instanceof ContinuousColourDecorator) {
                        if (continuousColourScaleDialog == null) {
                            continuousColourScaleDialog = new ContinuousColourScaleDialog(frame);
                        }
                        continuousColourScaleDialog.setDecorator((ContinuousColourDecorator)decorator);
                        int result = continuousColourScaleDialog.showDialog();
                        if (result != JOptionPane.CANCEL_OPTION && result != JOptionPane.CLOSED_OPTION) {

                            decorator = continuousColourScaleDialog.getDecorator();
                            String attribute = (String) colourAttributeCombo.getSelectedItem();
                            setDecoratorForAttribute(attribute, decorator);

                            update = true;
                        }
                    } else {
                        throw new IllegalArgumentException("Unsupported decorator type");
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
        }

        // I don't think this is required and it throws up many, many events (i.e., every time
        // an attribute is added to the combo.
//        colourAttributeCombo.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent event) {
//                fireControllerChanged();
//            }
//        });
    }

    public Decorator getColourDecorator(JComboBox colourAttributeCombo, Decorator defaultDecorator) {
        Decorator decorator = defaultDecorator;
        if (defaultDecorator == null || colourAttributeCombo.getSelectedIndex() > 0) {
            String attribute = (String) colourAttributeCombo.getSelectedItem();
            if (attribute != null && attribute.length() > 0) {
                decorator = getDecoratorForAttribute(attribute);
            }
        }
        return decorator;
    }

    public ColourDecorator getDecoratorForAttribute(String attribute) {
        ColourDecorator colourDecorator = attributeDecoratorMap.get(attribute);

        Set<Attributable> items = new HashSet<Attributable>();
        for (Tree tree : treeViewer.getTrees()) {
            for (Node node : tree.getNodes()) {
                if (node.getAttribute(attribute) != null) {
                    items.add(node);
                }
            }
        }

        if (colourDecorator == null) {

            if (attribute.endsWith("*")) {
                // todo reinstate branch colouring
                return null;
            } else if (DiscreteColourDecorator.isDiscrete(attribute, items)) {
                colourDecorator = new HSBDiscreteColourDecorator(attribute, items);
            } else {
                ContinuousScale scale = attributeScaleMap.get(attribute);
                if (scale == null) {
                    scale = new ContinuousScale();
                    attributeScaleMap.put(attribute, scale);
                }
                scale.setAttributes(attribute, items);

                colourDecorator = new HSBContinuousColourDecorator(scale);

            }
        } else if (colourDecorator instanceof DiscreteColourDecorator) {
            ((DiscreteColourDecorator)colourDecorator).setAttributes(attribute, items);
        } else if (colourDecorator instanceof ContinuousColourDecorator) {
            ((ContinuousColourDecorator)colourDecorator).setAttributes(attribute, items);
        }

        return colourDecorator;
    }

    private void setDecoratorForAttribute(String attribute, ColourDecorator decorator) {
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
            if (key.trim().startsWith(CONTROLLER_KEY + "." + SCHEME_KEY)) {

                String value = (String)settings.get(key);
                if (value != null) {
                    String[] parts = value.split(":");
                    if (parts.length == 2) {
                        String attribute = parts[0];
                        ColourDecorator decorator = getDecoratorForAttribute(attribute);

                        String colourSettings = parts[1];
                        if (colourSettings.startsWith("HSBDiscrete")) {
                            String settingsString = colourSettings.substring("HSBDiscrete".length());
                            if (decorator == null || !(decorator instanceof HSBDiscreteColourDecorator)) {
                                decorator = new HSBDiscreteColourDecorator(attribute, settingsString);
                            } else {
                                decorator.setup(settingsString);
                            }
                        } else if (colourSettings.startsWith("HSBContinuous")) {
                            String settingsString = colourSettings.substring("HSBContinuous".length());
                            if (decorator == null || !(decorator instanceof HSBDiscreteColourDecorator)) {
                                decorator = new HSBContinuousColourDecorator(attribute, settingsString);
                            } else {
                                decorator.setup(settingsString);
                            }
                        } else if (colourSettings.startsWith("InterpolatingContinuous")) {
                            String settingsString = colourSettings.substring("InterpolatingContinuous".length());
                            if (decorator == null || !(decorator instanceof InterpolatingColourDecorator)) {
                                decorator = new InterpolatingColourDecorator(attribute, settingsString);
                            } else {
                                decorator.setup(settingsString);
                            }
                        } else {
//                    throw new IllegalArgumentException("Unrecognized colour decorator type");
                        }
                        setDecoratorForAttribute(attribute, decorator);
                    }
                }
            } else if (key.trim().startsWith(CONTROLLER_KEY + "." + ORDER_KEY)) {
                String value = (String)settings.get(key);
                if (value != null) {
                    String[] parts = value.split(":");
                    if (parts.length == 2) {
                        String attribute = parts[0];
                        Object[] values = parts[1].split(",");
                        ColourDecorator decorator = getDecoratorForAttribute(attribute);
                        if (decorator != null && decorator instanceof DiscreteColourDecorator) {
                            ((DiscreteColourDecorator)decorator).setValuesOrder(Arrays.asList(values));
                            setDecoratorForAttribute(attribute, decorator);
                        }
                    }
                }
            }

        }
    }

    @Override
    public void getSettings(Map<String, Object> settings) {
        for (String attribute : attributeDecoratorMap.keySet()) {
            ColourDecorator decorator = attributeDecoratorMap.get(attribute);
            String colourSettings = decorator.toString();
            String name = "";
            if (decorator instanceof HSBDiscreteColourDecorator) {
                name = "HSBDiscrete";
            } else if (decorator instanceof HSBContinuousColourDecorator) {
                name = "HSBContinuous";
            } else if (decorator instanceof InterpolatingColourDecorator) {
                name = "InterpolatingContinuous";
            } else {
                throw new IllegalArgumentException("Unrecognized colour decorator type");
            }
            settings.put(CONTROLLER_KEY + "." + SCHEME_KEY + "." + flattenName(attribute), attribute + ":" + name + colourSettings);
            if (decorator instanceof DiscreteColourDecorator) {
                if (((DiscreteColourDecorator)decorator).hasReorderedValues()) {
                    String orderString = ((DiscreteColourDecorator)decorator).getOrderString();
                    settings.put(CONTROLLER_KEY + "." + ORDER_KEY + "." + flattenName(attribute), attribute + ":" + orderString);

                }
            }
        }
    }

    private String flattenName(String name) {
        String flattened = name.trim().toLowerCase();
        flattened.replaceAll(" ", "_");
        flattened.replaceAll("\t", "_");
        flattened.replaceAll("\r", "_");
        return flattened;
    }

    private final TreeViewer treeViewer;
    private final JFrame frame;

    private Map<String, ColourDecorator> attributeDecoratorMap = new HashMap<String, ColourDecorator>();
    private Map<String, ContinuousScale> attributeScaleMap = new HashMap<String, ContinuousScale>();

//    private List<String> attributeNames = new ArrayList<String>();

//    private boolean editingComboBox = false;

    private ContinuousColourScaleDialog continuousColourScaleDialog = null;
    private DiscreteColourScaleDialog discreteColourScaleDialog = null;

//    // an internal listener interface
//    private interface Listener {
//        void attributesChanged();
//    }
//
//    private void addListener(Listener listener) {
//        listeners.add(listener);
//    }
//
//    private void fireAttributesChanged() {
//        for (Listener listener : listeners) {
//            listener.attributesChanged();
//        }
//
//    }
//    private final List<Listener> listeners = new ArrayList<Listener>();

}
