package figtree.treeviewer.painters;

import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.*;
import java.util.*;
import java.util.prefs.Preferences;

import figtree.treeviewer.ControllerOptionsPanel;
import jebl.util.Attributable;

/**
 * @author Andrew Rambaut
 * @version $Id: NodeBarController.java 760 2007-08-21 00:05:45Z rambaut $
 */
public class NodeBarController extends AbstractController {

    private static Preferences PREFS = Preferences.userNodeForPackage(NodeBarController.class);

    private static final String NODE_BARS_KEY = "nodeBars";

    private static final String BAR_WIDTH_KEY = "barWidth";

    private static float DEFAULT_BAR_WIDTH = 4.0f;

    public NodeBarController(String title, final NodeBarPainter nodeBarPainter) {
        this.title = title;
        this.nodeBarPainter = nodeBarPainter;

        final float defaultBarWidth = PREFS.getFloat(BAR_WIDTH_KEY, DEFAULT_BAR_WIDTH);

	    optionsPanel = new ControllerOptionsPanel(2, 2);

        titleCheckBox = new JCheckBox(getTitle());

        titleCheckBox.setSelected(this.nodeBarPainter.isVisible());

        String[] attributeNames = this.nodeBarPainter.getAttributeNames();

        displayAttributeCombo = new JComboBox(attributeNames);
        displayAttributeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                String attribute = (String)displayAttributeCombo.getSelectedItem();
                nodeBarPainter.setDisplayAttribute(attribute);
            }
        });

        final JLabel label1 = optionsPanel.addComponentWithLabel("Display:", displayAttributeCombo);

        this.nodeBarPainter.addPainterListener(new PainterListener() {
            public void painterChanged() {
            }

            public void painterSettingsChanged() {
            }

            public void attributesChanged() {
                setupAttributes();
            }
        });

        barWidthSpinner = new JSpinner(new SpinnerNumberModel(defaultBarWidth, 0.01, 48.0, 1.0));
        barWidthSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                float lineWidth = ((Double) barWidthSpinner.getValue()).floatValue();
                nodeBarPainter.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            }
        });
        final JLabel label2 = optionsPanel.addComponentWithLabel("Bar Width:", barWidthSpinner);

        nodeBarPainter.setStroke(new BasicStroke(defaultBarWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

        final boolean isSelected = titleCheckBox.isSelected();
        label1.setEnabled(isSelected);
        displayAttributeCombo.setEnabled(isSelected);
        label2.setEnabled(isSelected);
        barWidthSpinner.setEnabled(isSelected);

        titleCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final boolean isSelected = titleCheckBox.isSelected();
                label1.setEnabled(isSelected);
                displayAttributeCombo.setEnabled(isSelected);
                label2.setEnabled(isSelected);
                barWidthSpinner.setEnabled(isSelected);
                nodeBarPainter.setVisible(isSelected);
            }
        });
    }

    private void setupAttributes() {
        Object item1 = displayAttributeCombo.getSelectedItem();
        displayAttributeCombo.removeAllItems();
        for (String name : nodeBarPainter.getAttributeNames()) {
            displayAttributeCombo.addItem(name);
        }
        displayAttributeCombo.setSelectedItem(item1);

        optionsPanel.repaint();
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
        barWidthSpinner.setValue((Double)settings.get(NODE_BARS_KEY + "." + BAR_WIDTH_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(NODE_BARS_KEY + "." + BAR_WIDTH_KEY, barWidthSpinner.getValue());
    }


    private final JCheckBox titleCheckBox;
    private final OptionsPanel optionsPanel;

    private JComboBox displayAttributeCombo;

    public String getTitle() {
        return title;
    }

    private final String title;

    private final NodeBarPainter nodeBarPainter;

    private final JSpinner barWidthSpinner;
}
