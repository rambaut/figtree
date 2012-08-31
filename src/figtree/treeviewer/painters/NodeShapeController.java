package figtree.treeviewer.painters;

import figtree.treeviewer.AttributeColourController;
import figtree.treeviewer.TreeViewer;
import figtree.treeviewer.decorators.Decorator;
import jam.controlpalettes.AbstractController;
import jam.controlpalettes.ControllerListener;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import figtree.treeviewer.ControllerOptionsPanel;

/**
 * @author Andrew Rambaut
 * @version $Id: NodeShapeController.java 760 2007-08-21 00:05:45Z rambaut $
 */
public class NodeShapeController extends AbstractController {

    private static Preferences PREFS = Preferences.userNodeForPackage(NodeBarController.class);

    private static final String NODE_SHAPE_KEY = "nodeShape";
    public static final String SIZE_ATTRIBUTE_KEY = "sizeAttribute";
    public static final String COLOUR_ATTRIBUTE_KEY = "colourAttribute";
    private static final String SHAPE_SIZE_KEY = "size";

    private static float DEFAULT_SHAPE_SIZE = 4.0f;

    public NodeShapeController(String title, final NodeShapePainter nodeShapePainter,
                               final AttributeColourController colourController,
                               final TreeViewer treeViewer) {
        this.title = title;
        this.nodeShapePainter = nodeShapePainter;

        final float defaultShapeSize = PREFS.getFloat(SHAPE_SIZE_KEY, DEFAULT_SHAPE_SIZE);

        optionsPanel = new ControllerOptionsPanel(2, 2);

        titleCheckBox = new JCheckBox(getTitle());
        titleCheckBox.setSelected(this.nodeShapePainter.isVisible());

        titleCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final boolean selected = titleCheckBox.isSelected();
                nodeShapePainter.setVisible(selected);
            }
        });

        shapeCombo = new JComboBox(NodeShapePainter.NodeShape.values());

        String[] attributes = this.nodeShapePainter.getAttributeNames();

        sizeAttributeCombo = new JComboBox(attributes);
        sizeAttributeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String attribute = (String) sizeAttributeCombo.getSelectedItem();
                nodeShapePainter.setSizeAttribute(attribute);
            }
        });
        new AttributeComboHelper(sizeAttributeCombo, treeViewer, "Fixed");

        colourAttributeCombo = new JComboBox(attributes);
        setupColourButton = new JButton("Colour");

        shapeSizeSpinner = new JSpinner(new SpinnerNumberModel(defaultShapeSize, 0.0, 48.0, 1.0));
        shapeSizeSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                float shapeSize = ((Double) shapeSizeSpinner.getValue()).floatValue();
                nodeShapePainter.setDefaultSize(shapeSize);
            }
        });

        new AttributeComboHelper(colourAttributeCombo, treeViewer, "User selection");
        colourController.setupControls(colourAttributeCombo, setupColourButton);
        colourController.addControllerListener(new ControllerListener() {
            @Override
            public void controlsChanged() {
                Decorator colourDecorator = colourController.getColourDecorator(colourAttributeCombo, null);
                nodeShapePainter.setColourDecorator(colourDecorator);
            }
        });

        this.nodeShapePainter.addPainterListener(new PainterListener() {
            public void painterChanged() {

            }

            public void painterSettingsChanged() {
            }

            public void attributesChanged() {
                Object item = sizeAttributeCombo.getSelectedItem();
                sizeAttributeCombo.removeAllItems();
                for (String name : nodeShapePainter.getAttributeNames()) {
                    sizeAttributeCombo.addItem(name);
                }

                sizeAttributeCombo.setSelectedItem(item);

                optionsPanel.repaint();
            }
        });

        JLabel label1 = optionsPanel.addComponentWithLabel("Shape:", shapeCombo);
        JLabel label2 = optionsPanel.addComponentWithLabel("Size by:", sizeAttributeCombo);
        JLabel label3 = optionsPanel.addComponentWithLabel("Min size:", shapeSizeSpinner);
        JLabel label4 = optionsPanel.addComponentWithLabel("Colour by:", colourAttributeCombo);
        JLabel label5 = optionsPanel.addComponentWithLabel("Setup:", setupColourButton);

        // only needed if we want to change the options depending on
        // the choice of shapeCombo
//        shapeCombo.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent event) {
//                setupOptions();
//                optionsPanel.validate();
//            }
//        });

        addComponent(label1);
        addComponent(shapeCombo);
        addComponent(label2);
        addComponent(sizeAttributeCombo);
        addComponent(label3);
        addComponent(shapeSizeSpinner);
        addComponent(label4);
        addComponent(colourAttributeCombo);
        addComponent(label5);
        addComponent(setupColourButton);
        enableComponents(titleCheckBox.isSelected());

        titleCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                enableComponents(titleCheckBox.isSelected());
            }
        });
    }


    private void setupOptions() {
        optionsPanel.removeAll();
        fireControllerChanged();
    }

    public JComponent getTitleComponent() {
        return titleCheckBox;
    }

    public JPanel getPanel() {
        return optionsPanel;
    }

    public boolean isInitiallyVisible() {
        return false;
    }

    public void initialize() {
        // nothing to do
    }

    public void setSettings(Map<String,Object> settings) {
        titleCheckBox.setSelected((Boolean)settings.get(NODE_SHAPE_KEY + "." + IS_SHOWN));
        colourAttributeCombo.setSelectedItem((String) settings.get(NODE_SHAPE_KEY + "." + COLOUR_ATTRIBUTE_KEY));
        sizeAttributeCombo.setSelectedItem((String) settings.get(NODE_SHAPE_KEY + "." + SIZE_ATTRIBUTE_KEY));
        shapeSizeSpinner.setValue((Double)settings.get(NODE_SHAPE_KEY + "." + SHAPE_SIZE_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(NODE_SHAPE_KEY + "." + IS_SHOWN, titleCheckBox.isSelected());
        settings.put(NODE_SHAPE_KEY + "." + COLOUR_ATTRIBUTE_KEY, colourAttributeCombo.getSelectedItem());
        settings.put(NODE_SHAPE_KEY + "." + SIZE_ATTRIBUTE_KEY, sizeAttributeCombo.getSelectedItem());
        settings.put(NODE_SHAPE_KEY + "." + SHAPE_SIZE_KEY, shapeSizeSpinner.getValue());
    }

    private final JCheckBox titleCheckBox;
    private final OptionsPanel optionsPanel;

    private final JComboBox shapeCombo;
    private final JComboBox sizeAttributeCombo;
    private final JComboBox colourAttributeCombo;
    private final JSpinner shapeSizeSpinner;
    private final JButton setupColourButton;

    public String getTitle() {
        return title;
    }

    private final String title;
    private final NodeShapePainter nodeShapePainter;
}
