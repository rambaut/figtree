package figtree.treeviewer.painters;

import figtree.treeviewer.AttributeColourController;
import figtree.treeviewer.TreeViewer;
import figtree.treeviewer.WidthScaleDialog;
import figtree.treeviewer.decorators.Decorator;
import jam.controlpalettes.AbstractController;
import jam.controlpalettes.ControllerListener;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    public static final String SHAPE_TYPE_KEY = "shapeType";
    public static final String SIZE_ATTRIBUTE_KEY = "sizeAttribute";
    public static final String COLOUR_ATTRIBUTE_KEY = "colourAttribute";
    private static final String SHAPE_SIZE_KEY = "size";

    private static float DEFAULT_SHAPE_SIZE = 10.0f;

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

        shapeTypeCombo = new JComboBox(NodeShapePainter.ShapeType.values());
        shapeTypeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                NodeShapePainter.ShapeType shapeTypeType = (NodeShapePainter.ShapeType) shapeTypeCombo.getSelectedItem();
                nodeShapePainter.setShapeType(shapeTypeType);
            }
        });

        sizeAttributeCombo = new JComboBox();
        sizeAttributeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String attribute = (String) sizeAttributeCombo.getSelectedItem();
                nodeShapePainter.setSizeAttribute(attribute);
            }
        });
        new AttributeComboHelper(sizeAttributeCombo, treeViewer, NodeShapePainter.FIXED);

        colourAttributeCombo = new JComboBox();
        setupColourButton = new JButton("Colour");

        shapeSizeSpinner = new JSpinner(new SpinnerNumberModel(defaultShapeSize, 0.0, 100.0, 1.0));
        shapeSizeSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                float shapeSize = ((Double) shapeSizeSpinner.getValue()).floatValue();
                nodeShapePainter.setDefaultSize(shapeSize);
            }
        });

//        sizeAutoRange = true;
//        sizeFromValue = 0.0;
//        widthToValue = 1.0;
//        fromWidth = 1.0;
//        toWidth = 10.0;

        JButton setupWidthButton = new JButton(new AbstractAction("Scale") {
            public void actionPerformed(ActionEvent e) {
//                if (widthScaleDialog == null) {
//                    widthScaleDialog = new WidthScaleDialog(frame, widthAutoRange,
//                            widthFromValue, widthToValue,
//                            fromWidth, toWidth);
//                }
//                int result = widthScaleDialog.showDialog();
//                if (result != JOptionPane.CANCEL_OPTION) {
//                    widthAutoRange = widthScaleDialog.getAutoRange();
//                    widthFromValue = widthScaleDialog.getFromValue().doubleValue();
//                    widthToValue = widthScaleDialog.getToValue().doubleValue();
//                    fromWidth = widthScaleDialog.getFromWidth().doubleValue();
//                    toWidth = widthScaleDialog.getToWidth().doubleValue();
//                    setupBranchDecorators();
//                }
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

        JLabel label1 = optionsPanel.addComponentWithLabel("Shape:", shapeTypeCombo);
        JLabel label2 = optionsPanel.addComponentWithLabel("Size by:", sizeAttributeCombo);
        JLabel label3 = optionsPanel.addComponentWithLabel("Min size:", shapeSizeSpinner);
        JLabel label4 = optionsPanel.addComponentWithLabel("Setup:", setupWidthButton);
        optionsPanel.addSeparator();
        JLabel label5 = optionsPanel.addComponentWithLabel("Colour by:", colourAttributeCombo);
        JLabel label6 = optionsPanel.addComponentWithLabel("Setup:", setupColourButton);

        // only needed if we want to change the options depending on
        // the choice of shapeTypeCombo
//        shapeTypeCombo.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent event) {
//                setupOptions();
//                optionsPanel.validate();
//            }
//        });

        addComponent(label1);
        addComponent(shapeTypeCombo);
        addComponent(label2);
        addComponent(sizeAttributeCombo);
        addComponent(label3);
        addComponent(shapeSizeSpinner);
        addComponent(label4);
        addComponent(setupWidthButton);
        addComponent(label5);
        addComponent(colourAttributeCombo);
        addComponent(label6);
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

        shapeTypeCombo.setSelectedItem((NodeShapePainter.ShapeType.valueOf(settings.get(NODE_SHAPE_KEY + "." + SHAPE_TYPE_KEY).toString().toUpperCase())));
        colourAttributeCombo.setSelectedItem((String) settings.get(NODE_SHAPE_KEY + "." + COLOUR_ATTRIBUTE_KEY));
        sizeAttributeCombo.setSelectedItem((String) settings.get(NODE_SHAPE_KEY + "." + SIZE_ATTRIBUTE_KEY));
        shapeSizeSpinner.setValue((Double)settings.get(NODE_SHAPE_KEY + "." + SHAPE_SIZE_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(NODE_SHAPE_KEY + "." + IS_SHOWN, titleCheckBox.isSelected());
        settings.put(NODE_SHAPE_KEY + "." + SHAPE_TYPE_KEY, shapeTypeCombo.getSelectedItem());
        settings.put(NODE_SHAPE_KEY + "." + COLOUR_ATTRIBUTE_KEY, colourAttributeCombo.getSelectedItem());
        settings.put(NODE_SHAPE_KEY + "." + SIZE_ATTRIBUTE_KEY, sizeAttributeCombo.getSelectedItem());
        settings.put(NODE_SHAPE_KEY + "." + SHAPE_SIZE_KEY, shapeSizeSpinner.getValue());
    }

    private final JCheckBox titleCheckBox;
    private final OptionsPanel optionsPanel;

    private final JComboBox shapeTypeCombo;
    private final JComboBox sizeAttributeCombo;
    private final JComboBox colourAttributeCombo;
    private final JSpinner shapeSizeSpinner;
    private final JButton setupColourButton;

    private WidthScaleDialog widthScaleDialog = null;

    public String getTitle() {
        return title;
    }

    private final String title;
    private final NodeShapePainter nodeShapePainter;
}
