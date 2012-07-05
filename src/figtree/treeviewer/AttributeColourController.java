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
                        discreteColourScaleDialog.setDecorator((HSBDiscreteColourDecorator)decorator);
                        int result = discreteColourScaleDialog.showDialog();
                        if (result != JOptionPane.CANCEL_OPTION && result != JOptionPane.CLOSED_OPTION) {
                            discreteColourScaleDialog.setupDecorator((HSBDiscreteColourDecorator)decorator);
                            update = true;
                        }
                    } else if (decorator instanceof HSBContinuousColourDecorator) {
                        if (continuousColourScaleDialog == null) {
                            continuousColourScaleDialog = new ContinuousColourScaleDialog(frame);
                        }
                        continuousColourScaleDialog.setDecorator((HSBContinuousColourDecorator)decorator);
                        int result = continuousColourScaleDialog.showDialog();
                        if (result != JOptionPane.CANCEL_OPTION && result != JOptionPane.CLOSED_OPTION) {
                            continuousColourScaleDialog.setupDecorator((HSBContinuousColourDecorator)decorator);
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

        colourAttributeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                fireControllerChanged();
            }
        });
    }

    public Decorator getColourDecorator(JComboBox colourAttributeCombo, Decorator defaultDecorator) {
        Decorator decorator = defaultDecorator;
        if (colourAttributeCombo.getSelectedIndex() > 0) {
            String attribute = (String) colourAttributeCombo.getSelectedItem();
            if (attribute != null && attribute.length() > 0) {
                decorator = getDecoratorForAttribute(attribute);
            }
        }
        return decorator;
    }

    public ColourDecorator getDecoratorForAttribute(String attribute) {
        ColourDecorator colourDecorator = attributeDecoratorMap.get(attribute);

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
                colourDecorator = new HSBDiscreteColourDecorator(attribute, nodes);
            } else {
                ContinuousScale scale = attributeScaleMap.get(attribute);
                if (scale == null) {
                    scale = new ContinuousScale();
                    attributeScaleMap.put(attribute, scale);
                }
                scale.setAttributes(attribute, nodes);

                colourDecorator = new HSBContinuousColourDecorator(scale);

            }
        } else if (colourDecorator instanceof DiscreteColorDecorator) {
            ((DiscreteColorDecorator)colourDecorator).setAttributes(attribute, nodes);
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
            if (key.startsWith(CONTROLLER_KEY + ".")) {
                ColourDecorator decorator = null;

                String attribute = key.substring(CONTROLLER_KEY.length() + 1);
                String colourSettings = (String)settings.get(key);
                if (colourSettings.startsWith("HSBDiscrete")) {
                    decorator = new HSBDiscreteColourDecorator(attribute, colourSettings.substring("HSBDiscrete".length()));
                } else if (colourSettings.startsWith("HSBContinuous")) {
                    decorator = new HSBContinuousColourDecorator(attribute, colourSettings.substring("HSBContinuous".length()));
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
            ColourDecorator decorator = attributeDecoratorMap.get(attribute);
            String colourSettings = decorator.toString();
            String name = "";
            if (decorator instanceof HSBDiscreteColourDecorator) {
                name = "HSBDiscrete";
            } else if (decorator instanceof HSBContinuousColourDecorator) {
                name = "HSBContinuous";
            } else {
                throw new IllegalArgumentException("Unrecognized colour decorator type");
            }
            settings.put(CONTROLLER_KEY + "." + attribute, name + colourSettings);
        }
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
