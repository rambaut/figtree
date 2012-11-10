/*
 * TreesController.java
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

package figtree.panel;

import jebl.evolution.trees.SortedRootedTree;
import jebl.evolution.trees.TransformedRootedTree;
import figtree.treeviewer.*;
import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;

/**
 * @author Andrew Rambaut
 * @version $Id: TreesController.java,v 1.1 2007/08/14 15:50:41 rambaut Exp $
 */
public class TreesController extends AbstractController {

    private static final String CONTROLLER_TITLE = "Trees";

    private static final String CONTROLLER_KEY = "trees";

    private static final String TRANSFORM_KEY = "transform";
    private static final String TRANSFORM_TYPE_KEY = "transformType";
    private static final String ORDER_KEY = "order";
    private static final String ORDER_TYPE_KEY = "orderType";
    private static final String ROOTING_KEY = "rooting";
    private static final String ROOTING_TYPE_KEY = "rootingType";


    public TreesController(final TreeViewer treeViewer) {
        this.treeViewer = treeViewer;

        titleLabel = new JLabel(CONTROLLER_TITLE);

        optionsPanel = new OptionsPanel();

        rootingCheck = new JCheckBox("Midpoint root");
        rootingCheck.setOpaque(false);
        optionsPanel.addComponent(rootingCheck);

        rootingCheck.setSelected(treeViewer.isRootingOn());

        rootingCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (rootingCheck.isSelected()) {
                    treeViewer.setRootingOn(true);
                    treeViewer.setRootingType(TreePane.RootingType.MID_POINT);
                } else {
                    treeViewer.setRootingOn(false);
                    treeViewer.setRootingType(TreePane.RootingType.USER_ROOTING);
                }

            }
        });

        orderCombo = new JComboBox(new String[] {"Off",
                SortedRootedTree.BranchOrdering.INCREASING_NODE_DENSITY.toString(),
                SortedRootedTree.BranchOrdering.DECREASING_NODE_DENSITY.toString()});
        orderCombo.setOpaque(false);
        orderCombo.setSelectedItem(treeViewer.isOrderBranchesOn() ?
                treeViewer.getBranchOrdering().ordinal() + 1 : 0);
        orderCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                if (orderCombo.getSelectedIndex() == 0) {
                    treeViewer.setOrderBranchesOn(false);
                } else {
                    treeViewer.setOrderBranchesOn(true);
                    treeViewer.setBranchOrdering(SortedRootedTree.BranchOrdering.values()[orderCombo.getSelectedIndex() - 1]);
                }
            }
        });

        optionsPanel.addComponentWithLabel("Order:", orderCombo);

        transformCombo = new JComboBox(new String[] {"Off",
                TransformedRootedTree.Transform.CLADOGRAM.toString(),
                TransformedRootedTree.Transform.PROPORTIONAL.toString(),
                TransformedRootedTree.Transform.EQUAL_LENGTHS.toString()});
        transformCombo.setOpaque(false);
        transformCombo.setSelectedItem(treeViewer.isOrderBranchesOn() ?
                treeViewer.getBranchTransform().ordinal() + 1 : 0);
        transformCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                if (transformCombo.getSelectedIndex() == 0) {
                    treeViewer.setTransformBranchesOn(false);
                } else {
                    treeViewer.setTransformBranchesOn(true);
                    treeViewer.setBranchTransform(TransformedRootedTree.Transform.values()[transformCombo.getSelectedIndex() - 1]);
                }
            }
        });
        optionsPanel.addComponentWithLabel("Transform:", transformCombo);
    }

    public JComponent getTitleComponent() {
        return titleLabel;
    }

    public JPanel getPanel() {
        return optionsPanel;
    }

    public boolean isInitiallyVisible() {
        return false;
    }

    public void initialize() {
    }

    public void setSettings(Map<String,Object> settings) {
        if (((Boolean)settings.get(CONTROLLER_KEY + "." + TRANSFORM_KEY))) {
            String transformName = (String)settings.get(CONTROLLER_KEY + "." + TRANSFORM_TYPE_KEY);
            int i = 0;
            for (TransformedRootedTree.Transform transform : TransformedRootedTree.Transform.values()) {
                if (transform.toString().equalsIgnoreCase(transformName)) {
                    transformCombo.setSelectedIndex(i);
                }
                i++;
            }
        } else {
            transformCombo.setSelectedIndex(0);
        }

        if (((Boolean)settings.get(CONTROLLER_KEY + "." + ORDER_KEY))) {
            String orderName = (String)settings.get(CONTROLLER_KEY + "." + ORDER_TYPE_KEY);
            int i = 0;
            for (SortedRootedTree.BranchOrdering order : SortedRootedTree.BranchOrdering.values()) {
                if (order.toString().equalsIgnoreCase(orderName)) {
                    orderCombo.setSelectedIndex(i);
                }
                i++;
            }
        } else {
            orderCombo.setSelectedIndex(0);
        }


        rootingCheck.setSelected((Boolean) settings.get(CONTROLLER_KEY + "." + ROOTING_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(CONTROLLER_KEY + "." + TRANSFORM_KEY, false);
        settings.put(CONTROLLER_KEY + "." + TRANSFORM_TYPE_KEY, transformCombo.getSelectedItem().toString());
        settings.put(CONTROLLER_KEY + "." + ORDER_KEY, false);
        settings.put(CONTROLLER_KEY + "." + ORDER_TYPE_KEY, orderCombo.getSelectedItem().toString());
        settings.put(CONTROLLER_KEY + "." + ROOTING_KEY, rootingCheck.isSelected());
        settings.put(CONTROLLER_KEY + "." + ROOTING_TYPE_KEY, TreePane.RootingType.MID_POINT.toString());
    }


    private final JLabel titleLabel;
    private final OptionsPanel optionsPanel;

    private final JComboBox transformCombo;

    private final JComboBox orderCombo;

    private final JCheckBox rootingCheck;

    private final TreeViewer treeViewer;
}
