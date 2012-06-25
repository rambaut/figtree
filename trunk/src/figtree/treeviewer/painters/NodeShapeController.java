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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import figtree.treeviewer.ControllerOptionsPanel;

/**
 * @author Andrew Rambaut
 * @version $Id: NodeShapeController.java 760 2007-08-21 00:05:45Z rambaut $
 */
public class NodeShapeController extends AbstractController {

    public NodeShapeController(String title, final NodeShapePainter nodeShapePainter,
                               final AttributeColourController colourController,
                               final TreeViewer treeViewer) {
        this.title = title;
        this.colourController = colourController;
        this.nodeShapePainter = nodeShapePainter;

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
        new AttributeComboHelper(sizeAttributeCombo, treeViewer);

        colourAttributeCombo = new JComboBox(attributes);
        setupColourButton = new JButton("Colour");

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
        JLabel label3 = optionsPanel.addComponentWithLabel("Colour by:", colourAttributeCombo);
        JLabel label4 = optionsPanel.addComponentWithLabel("Setup:", setupColourButton);

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
        addComponent(colourAttributeCombo);
        addComponent(label4);
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

    public void getSettings(Map<String, Object> settings) {
    }

    public void setSettings(Map<String,Object> settings) {
    }

    private final JCheckBox titleCheckBox;
    private final OptionsPanel optionsPanel;

    private JComboBox shapeCombo;
    private JComboBox sizeAttributeCombo;
    private JComboBox colourAttributeCombo;
    private JButton setupColourButton;

    public String getTitle() {
        return title;
    }

    private final String title;
    private final AttributeColourController colourController;
    private final NodeShapePainter nodeShapePainter;
}
