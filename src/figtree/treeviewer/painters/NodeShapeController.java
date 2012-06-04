package figtree.treeviewer.painters;

import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import figtree.treeviewer.ControllerOptionsPanel;

/**
 * @author Andrew Rambaut
 * @version $Id: NodeShapeController.java 760 2007-08-21 00:05:45Z rambaut $
 */
public class NodeShapeController extends AbstractController {

    public NodeShapeController(String title, final NodeShapePainter nodeShapePainter) {
        this.title = title;
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

        colourAttributeCombo = new JComboBox(attributes);
        colourAttributeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String attribute = (String) colourAttributeCombo.getSelectedItem();
                nodeShapePainter.setColourAttribute(attribute);
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
        setupOptions();

        shapeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                setupOptions();
                optionsPanel.validate();
            }
        });

    }

    private void setupOptions() {
        optionsPanel.removeAll();
        optionsPanel.addComponentWithLabel("Shape:", shapeCombo);
        optionsPanel.addComponentWithLabel("Size by:", sizeAttributeCombo);
        optionsPanel.addComponentWithLabel("Colour by:", colourAttributeCombo);
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

    public String getTitle() {
        return title;
    }

    private final String title;

    private final NodeShapePainter nodeShapePainter;
}
