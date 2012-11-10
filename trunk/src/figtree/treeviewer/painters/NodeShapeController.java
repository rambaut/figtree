/*
 * NodeShapeController.java
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
    public static final String SCALE_TYPE_KEY = "scaleType";
    public static final String SIZE_ATTRIBUTE_KEY = "sizeAttribute";
    public static final String COLOUR_ATTRIBUTE_KEY = "colourAttribute";
    private static final String SHAPE_SIZE_KEY = "size";
    private static final String SHAPE_MIN_SIZE_KEY = "minSize";

    public NodeShapeController(String title, final NodeShapePainter nodeShapePainter,
                               final AttributeColourController colourController,
                               final TreeViewer treeViewer) {
        this.title = title;
        this.nodeShapePainter = nodeShapePainter;

        final float defaultShapeSize = PREFS.getFloat(SHAPE_SIZE_KEY, (float)NodeShapePainter.MAX_SIZE);

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
                nodeShapePainter.setShapeType((NodeShapePainter.ShapeType) shapeTypeCombo.getSelectedItem());
            }
        });

        scaleTypeCombo = new JComboBox(NodeShapePainter.ScaleType.values());
        scaleTypeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                nodeShapePainter.setScaleType((NodeShapePainter.ScaleType) scaleTypeCombo.getSelectedItem());
            }
        });

        sizeAttributeCombo = new JComboBox();
        // AttributeComboHelper & listener set after other controls are created...

        colourAttributeCombo = new JComboBox();
        JButton setupColourButton = new JButton("Colour");
        colourController.setupControls(colourAttributeCombo, setupColourButton);

        shapeSizeSpinner = new JSpinner(new SpinnerNumberModel(defaultShapeSize, 0.0, 100.0, 1.0));
        shapeSizeSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                double shapeSize = ((Double)shapeSizeSpinner.getValue());
                nodeShapePainter.setMaxSize(shapeSize);
            }
        });

        shapeMinSizeSpinner = new JSpinner(new SpinnerNumberModel(10.0, 0.0, 100.0, 1.0));
        shapeMinSizeSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                double minSize = ((Double)shapeMinSizeSpinner.getValue());
                nodeShapePainter.setMinSize(minSize);
            }
        });


        final JLabel label1 = optionsPanel.addComponentWithLabel("Shape:", shapeTypeCombo);
        final JLabel label2 = optionsPanel.addComponentWithLabel("Max size:", shapeSizeSpinner);
        final JLabel label3 = optionsPanel.addComponentWithLabel("Size by:", sizeAttributeCombo);
        final JLabel label4 = optionsPanel.addComponentWithLabel("Min size:", shapeMinSizeSpinner);
        final JLabel label5 = optionsPanel.addComponentWithLabel("Using:", scaleTypeCombo);
        optionsPanel.addSeparator();
        final JLabel label6 = optionsPanel.addComponentWithLabel("Colour by:", colourAttributeCombo);
        final JLabel label7 = optionsPanel.addComponentWithLabel("Setup:", setupColourButton);

        new AttributeComboHelper(colourAttributeCombo, treeViewer, "User selection").addListener(new AttributeComboHelperListener() {
            @Override
            public void attributeComboChanged() {
                Decorator colourDecorator = colourController.getColourDecorator(colourAttributeCombo, null);
                nodeShapePainter.setColourDecorator(colourDecorator);
            }
        });

        new AttributeComboHelper(sizeAttributeCombo,
                treeViewer,
                NodeShapePainter.FIXED, true, false).addListener(new AttributeComboHelperListener() {
            @Override
            public void attributeComboChanged() {
                String attribute = (String) sizeAttributeCombo.getSelectedItem();
                nodeShapePainter.setSizeAttribute(attribute);
                if (attribute != null) {
                    boolean isSelected = !attribute.equals(NodeShapePainter.FIXED);
                    label4.setEnabled(isSelected);
                    shapeMinSizeSpinner.setEnabled(isSelected);
                    label5.setEnabled(isSelected);
                    scaleTypeCombo.setEnabled(isSelected);
                }
            }
        });

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
        addComponent(shapeSizeSpinner);
        addComponent(label3);
        addComponent(sizeAttributeCombo);
        addComponent(label4);
        addComponent(shapeMinSizeSpinner);
        addComponent(label5);
        addComponent(scaleTypeCombo);
        addComponent(label6);
        addComponent(colourAttributeCombo);
        addComponent(label7);
        addComponent(setupColourButton);
        enableComponents(titleCheckBox.isSelected());

        titleCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                enableComponents(titleCheckBox.isSelected());
            }
        });
    }


    private void setupOptions() {
//        optionsPanel.removeAll();
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
        scaleTypeCombo.setSelectedItem((NodeShapePainter.ScaleType.valueOf(settings.get(NODE_SHAPE_KEY + "." + SCALE_TYPE_KEY).toString().toUpperCase())));
        colourAttributeCombo.setSelectedItem((String) settings.get(NODE_SHAPE_KEY + "." + COLOUR_ATTRIBUTE_KEY));
        sizeAttributeCombo.setSelectedItem((String) settings.get(NODE_SHAPE_KEY + "." + SIZE_ATTRIBUTE_KEY));
        shapeSizeSpinner.setValue((Double)settings.get(NODE_SHAPE_KEY + "." + SHAPE_SIZE_KEY));
        shapeMinSizeSpinner.setValue((Double) settings.get(NODE_SHAPE_KEY + "." + SHAPE_MIN_SIZE_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(NODE_SHAPE_KEY + "." + IS_SHOWN, titleCheckBox.isSelected());
        settings.put(NODE_SHAPE_KEY + "." + SHAPE_TYPE_KEY, shapeTypeCombo.getSelectedItem());
        settings.put(NODE_SHAPE_KEY + "." + SCALE_TYPE_KEY, scaleTypeCombo.getSelectedItem());
        settings.put(NODE_SHAPE_KEY + "." + COLOUR_ATTRIBUTE_KEY, colourAttributeCombo.getSelectedItem());
        settings.put(NODE_SHAPE_KEY + "." + SIZE_ATTRIBUTE_KEY, sizeAttributeCombo.getSelectedItem());
        settings.put(NODE_SHAPE_KEY + "." + SHAPE_SIZE_KEY, shapeSizeSpinner.getValue());
        settings.put(NODE_SHAPE_KEY + "." + SHAPE_MIN_SIZE_KEY, shapeMinSizeSpinner.getValue());
    }

    private final JCheckBox titleCheckBox;
    private final OptionsPanel optionsPanel;

    private final JComboBox shapeTypeCombo;
    private final JComboBox scaleTypeCombo;
    private final JComboBox sizeAttributeCombo;
    private final JComboBox colourAttributeCombo;
    private final JSpinner shapeSizeSpinner;
    private final JSpinner shapeMinSizeSpinner;

    public String getTitle() {
        return title;
    }

    private final String title;
    private final NodeShapePainter nodeShapePainter;
}
