package figtree.treeviewer.painters;

import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;

import figtree.treeviewer.ControllerOptionsPanel;

/**
 * @author Andrew Rambaut
 * @version $Id: NodeShapeController.java 760 2007-08-21 00:05:45Z rambaut $
 */
public class NodeShapeController extends AbstractController {

    public enum NodePainterType {
        BAR("Bar"),
        SHAPE("Shape");

        NodePainterType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return name;
        }

        private final String name;
    }

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

        shapeCombo = new JComboBox(new NodePainterType[] {
                NodePainterType.BAR,
                NodePainterType.SHAPE
        });

        String[] attributes = this.nodeShapePainter.getAttributeNames();

        displayAttributeCombo = new JComboBox(attributes);
        displayAttributeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                String attribute = (String)displayAttributeCombo.getSelectedItem();
                nodeShapePainter.setDisplayAttribute(NodeShapePainter.LOWER_ATTRIBUTE, attribute);
            }
        });

        displayLowerAttributeCombo = new JComboBox(attributes);
        displayLowerAttributeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                String attribute = (String)displayLowerAttributeCombo.getSelectedItem();
                nodeShapePainter.setDisplayAttribute(NodeShapePainter.LOWER_ATTRIBUTE, attribute);
            }
        });

        displayUpperAttributeCombo = new JComboBox(attributes);
        displayUpperAttributeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                String attribute = (String)displayUpperAttributeCombo.getSelectedItem();
                nodeShapePainter.setDisplayAttribute(NodeShapePainter.UPPER_ATTRIBUTE, attribute);
            }
        });

        this.nodeShapePainter.addPainterListener(new PainterListener() {
            public void painterChanged() {

            }

            public void painterSettingsChanged() {
            }

            public void attributesChanged() {
                Object item1 = displayLowerAttributeCombo.getSelectedItem();
                Object item2 = displayUpperAttributeCombo.getSelectedItem();
                displayLowerAttributeCombo.removeAllItems();
                displayUpperAttributeCombo.removeAllItems();
                for (String name : nodeShapePainter.getAttributeNames()) {
                    displayLowerAttributeCombo.addItem(name);
                    displayUpperAttributeCombo.addItem(name);
                }

                displayLowerAttributeCombo.setSelectedItem(item1);
                displayUpperAttributeCombo.setSelectedItem(item2);

                optionsPanel.repaint();
            }
        });
        setupOptions();

        shapeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                setupOptions();
                optionsPanel.validate();
            }
        });

    }

    private void setupOptions() {
        optionsPanel.removeAll();
        optionsPanel.addComponentWithLabel("Shape:", shapeCombo);
        switch ((NodePainterType) shapeCombo.getSelectedItem()) {
            case BAR:
                optionsPanel.addComponentWithLabel("Lower:", displayLowerAttributeCombo);
                optionsPanel.addComponentWithLabel("Upper:", displayUpperAttributeCombo);

                break;

            case SHAPE:
                optionsPanel.addComponentWithLabel("Radius:", displayAttributeCombo);
                break;
        }
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
    private JComboBox displayAttributeCombo;
    private JComboBox displayLowerAttributeCombo;
    private JComboBox displayUpperAttributeCombo;

    public String getTitle() {
        return title;
    }

    private final String title;

    private final NodeShapePainter nodeShapePainter;
}
