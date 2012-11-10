/*
 * NodeBarController.java
 *
 * Copyright (C) 2012 Andrew Rambaut
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package figtree.treeviewer.painters;

import figtree.treeviewer.TreeViewer;
import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    public static final String DISPLAY_ATTRIBUTE_KEY = "displayAttribute";
    private static final String BAR_WIDTH_KEY = "barWidth";

    private static float DEFAULT_BAR_WIDTH = 4.0f;

    public NodeBarController(String title, final NodeBarPainter nodeBarPainter,
                             final TreeViewer treeViewer) {
        this.title = title;
        this.nodeBarPainter = nodeBarPainter;

        final float defaultBarWidth = PREFS.getFloat(BAR_WIDTH_KEY, DEFAULT_BAR_WIDTH);

	    optionsPanel = new ControllerOptionsPanel(2, 2);

        titleCheckBox = new JCheckBox(getTitle());

        titleCheckBox.setSelected(this.nodeBarPainter.isVisible());

        displayAttributeCombo = new JComboBox();
        new AttributeComboHelper(displayAttributeCombo, treeViewer, LabelPainter.PainterIntent.RANGE).addListener(new AttributeComboHelperListener() {
            @Override
            public void attributeComboChanged() {
                String attribute = (String) displayAttributeCombo.getSelectedItem();
                nodeBarPainter.setDisplayAttribute(attribute);
            }
        });

        barWidthSpinner = new JSpinner(new SpinnerNumberModel(defaultBarWidth, 0.01, 48.0, 1.0));
        barWidthSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                float lineWidth = ((Double) barWidthSpinner.getValue()).floatValue();
                nodeBarPainter.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            }
        });

        nodeBarPainter.setStroke(new BasicStroke(defaultBarWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

        final JLabel label1 = optionsPanel.addComponentWithLabel("Display:", displayAttributeCombo);
        final JLabel label2 = optionsPanel.addComponentWithLabel("Bar Width:", barWidthSpinner);
        addComponent(label1);
        addComponent(displayAttributeCombo);
        addComponent(label2);
        addComponent(barWidthSpinner);
        enableComponents(titleCheckBox.isSelected());

        titleCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                enableComponents(titleCheckBox.isSelected());
                nodeBarPainter.setVisible(titleCheckBox.isSelected());
            }
        });
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
        titleCheckBox.setSelected((Boolean) settings.get(NODE_BARS_KEY + "." + IS_SHOWN));
        displayAttributeCombo.setSelectedItem((String) settings.get(NODE_BARS_KEY + "." + DISPLAY_ATTRIBUTE_KEY));
        barWidthSpinner.setValue((Double)settings.get(NODE_BARS_KEY + "." + BAR_WIDTH_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(NODE_BARS_KEY + "." + IS_SHOWN, titleCheckBox.isSelected());
        settings.put(NODE_BARS_KEY + "." + DISPLAY_ATTRIBUTE_KEY, displayAttributeCombo.getSelectedItem());
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
