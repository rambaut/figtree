/*
 * NodeShapeController.java
 *
 * Copyright (C) 2006-2014 Andrew Rambaut
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
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.prefs.Preferences;

import figtree.treeviewer.ControllerOptionsPanel;

/**
 * @author Andrew Rambaut
 * @version $Id$
 *
 * $HeadURL$
 *
 * $LastChangedBy$
 * $LastChangedDate$
 * $LastChangedRevision$
 */
public class NodeShapeController extends AbstractController {

    public enum NodeType {
        INTERNAL, EXTERNAL, BOTH
    }

    private static Preferences PREFS = Preferences.userNodeForPackage(TreeViewer.class);

    private static final String IS_EXTERNAL = "isExternal";
    private static final String IS_INTERNAL = "isInternal";
    private static final String NODE_SHAPE_KEY = "nodeShape";
    public static final String SHAPE_TYPE_KEY = "shapeType";
    public static final String SCALE_TYPE_KEY = "scaleType";
    public static final String SIZE_ATTRIBUTE_KEY = "sizeAttribute";
    public static final String COLOUR_ATTRIBUTE_KEY = "colourAttribute";
    private static final String SHAPE_SIZE_KEY = "size";
    private static final String SHAPE_MIN_SIZE_KEY = "minSize";

    public NodeShapeController(final String title, final NodeType type, final NodeShapePainter nodeShapePainter,
                               final AttributeColourController colourController,
                               final TreeViewer treeViewer) {
        this.title = title;

        this.type = type;

        final float defaultShapeSize = PREFS.getFloat(SHAPE_SIZE_KEY, (float)NodeShapePainter.MAX_SIZE);

        optionsPanel = new ControllerOptionsPanel(2, 2);

        titleCheckBox = new JCheckBox(getTitle());
        titleCheckBox.setSelected(nodeShapePainter.isVisible());

        titleCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final boolean selected = titleCheckBox.isSelected();
                nodeShapePainter.setVisible(selected);
            }
        });

        final ControllerOptionsPanel nodeCheckPanel;
        if (type == NodeType.BOTH) {
            externalNodeCheck = new JCheckBox("external");
            internalNodeCheck = new JCheckBox("internal");
            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    nodeShapePainter.setNodeType(externalNodeCheck.isSelected(), internalNodeCheck.isSelected());
                }
            };
            externalNodeCheck.addActionListener(listener);
            internalNodeCheck.addActionListener(listener);
            nodeCheckPanel = new ControllerOptionsPanel(2, 2);
            nodeCheckPanel.setBorder(BorderFactory.createEmptyBorder());
            nodeCheckPanel.addSpanningComponent(externalNodeCheck);
            nodeCheckPanel.addSpanningComponent(internalNodeCheck);

            externalNodeCheck.setSelected(nodeShapePainter.isExternal());
            internalNodeCheck.setSelected(nodeShapePainter.isInternal());
        } else {
            nodeCheckPanel = null;
            externalNodeCheck = null;
            internalNodeCheck = null;
            if (type == NodeType.EXTERNAL) {
                nodeShapePainter.setNodeType(true, false);
            } else if (type == NodeType.INTERNAL) {
                nodeShapePainter.setNodeType(false, true);
            }
        }

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

        outlineStrokeCombo = new JComboBox(new String[] {"None", "0.25", "0.5", "1.0", "2.0", "3.0", "4.0", "5.0"});
        outlineStrokeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                nodeShapePainter.setOutline((outlineStrokeCombo.getSelectedIndex() == 0 ? 0.0f :
                    Float.parseFloat(outlineStrokeCombo.getSelectedItem().toString())),
                        (Paint)outlinePaintCombo.getSelectedItem()
                );
            }
        });

        final Paint[] outlinePaints = {Color.black, Color.white};
        outlinePaintCombo = new JComboBox(new String[] {"black", "white"});
        outlinePaintCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                nodeShapePainter.setOutline((outlineStrokeCombo.getSelectedIndex() == 0 ? 0.0f :
                                Float.parseFloat(outlineStrokeCombo.getSelectedItem().toString())),
                        outlinePaints[outlinePaintCombo.getSelectedIndex()]
                );
            }
        });

        final JLabel label8;
        if (type == NodeType.BOTH) {
            label8 = optionsPanel.addComponentWithLabel("Show:", nodeCheckPanel);
        } else {
            label8 = null;
        }

        final JLabel label1 = optionsPanel.addComponentWithLabel("Shape:", shapeTypeCombo);
        final JLabel label2 = optionsPanel.addComponentWithLabel("Max size:", shapeSizeSpinner);
        final JLabel label3 = optionsPanel.addComponentWithLabel("Size by:", sizeAttributeCombo);
        final JLabel label4 = optionsPanel.addComponentWithLabel("Min size:", shapeMinSizeSpinner);
        final JLabel label5 = optionsPanel.addComponentWithLabel("Using:", scaleTypeCombo);
        optionsPanel.addSeparator();
        final JLabel label6 = optionsPanel.addComponentWithLabel("Colour by:", colourAttributeCombo);
        final JLabel label7 = optionsPanel.addComponentWithLabel("Setup:", setupColourButton);

        final JLabel label9 = optionsPanel.addComponentWithLabel("Outline width:", outlineStrokeCombo);
        final JLabel label10 = optionsPanel.addComponentWithLabel("Outline colour:", outlinePaintCombo);

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
        if (type == NodeType.BOTH) {
            addComponent(label8);
            addComponent(externalNodeCheck);
            addComponent(internalNodeCheck);
        }
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
        addComponent(label9);
        addComponent(outlineStrokeCombo);
        addComponent(label10);
        addComponent(outlinePaintCombo);
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
        String key = NODE_SHAPE_KEY + (type == NodeType.INTERNAL ? "Internal" : (type == NodeType.EXTERNAL ? "External" : ""));

        titleCheckBox.setSelected((Boolean)settings.get(key + "." + IS_SHOWN));

        if (type == NodeType.BOTH) {
            externalNodeCheck.setSelected((Boolean) settings.get(key + "." + IS_EXTERNAL));
            internalNodeCheck.setSelected((Boolean) settings.get(key + "." + IS_INTERNAL));
        }
        shapeTypeCombo.setSelectedItem((NodeShapePainter.ShapeType.valueOf(settings.get(key + "." + SHAPE_TYPE_KEY).toString().toUpperCase())));
        scaleTypeCombo.setSelectedItem((NodeShapePainter.ScaleType.valueOf(settings.get(key + "." + SCALE_TYPE_KEY).toString().toUpperCase())));
        colourAttributeCombo.setSelectedItem((String) settings.get(key + "." + COLOUR_ATTRIBUTE_KEY));
        sizeAttributeCombo.setSelectedItem((String) settings.get(key + "." + SIZE_ATTRIBUTE_KEY));
        shapeSizeSpinner.setValue((Double)settings.get(key + "." + SHAPE_SIZE_KEY));
        shapeMinSizeSpinner.setValue((Double) settings.get(key + "." + SHAPE_MIN_SIZE_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        String key = NODE_SHAPE_KEY + (type == NodeType.INTERNAL ? "Internal" : (type == NodeType.EXTERNAL ? "External" : ""));
        settings.put(key + "." + IS_SHOWN, titleCheckBox.isSelected());
        if (type == NodeType.BOTH) {
            settings.put(key + "." + IS_EXTERNAL, externalNodeCheck.isSelected());
            settings.put(key + "." + IS_INTERNAL, internalNodeCheck.isSelected());
        }
        settings.put(key + "." + SHAPE_TYPE_KEY, shapeTypeCombo.getSelectedItem());
        settings.put(key + "." + SCALE_TYPE_KEY, scaleTypeCombo.getSelectedItem());
        settings.put(key + "." + COLOUR_ATTRIBUTE_KEY, colourAttributeCombo.getSelectedItem());
        settings.put(key + "." + SIZE_ATTRIBUTE_KEY, sizeAttributeCombo.getSelectedItem());
        settings.put(key + "." + SHAPE_SIZE_KEY, shapeSizeSpinner.getValue());
        settings.put(key + "." + SHAPE_MIN_SIZE_KEY, shapeMinSizeSpinner.getValue());
    }

    private final JCheckBox titleCheckBox;
    private final OptionsPanel optionsPanel;

    private final JCheckBox externalNodeCheck;
    private final JCheckBox internalNodeCheck;
    private final JComboBox shapeTypeCombo;
    private final JComboBox scaleTypeCombo;
    private final JComboBox sizeAttributeCombo;
    private final JComboBox colourAttributeCombo;
    private final JSpinner shapeSizeSpinner;
    private final JSpinner shapeMinSizeSpinner;
    private final JComboBox outlineStrokeCombo;
    private final JComboBox outlinePaintCombo;

    public String getTitle() {
        return title;
    }

    private final String title;
    private final NodeType type;
}
