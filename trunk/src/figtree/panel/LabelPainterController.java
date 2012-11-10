/*
 * LabelPainterController.java
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

import figtree.treeviewer.TreeViewer;
import figtree.treeviewer.painters.AttributeComboHelper;
import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;

import figtree.treeviewer.painters.LabelPainter;
import sun.jvm.hotspot.tools.FinalizerInfo;

/**
 * @author Andrew Rambaut
 * @version $Id: LabelPainterController.java,v 1.1 2007/08/14 15:50:42 rambaut Exp $
 */
public class LabelPainterController extends AbstractController {

    private static final String DISPLAY_ATTRIBUTE_KEY = "displayAttribute";

    public LabelPainterController(String tipKey,
                                  final LabelPainter tipLabelPainter,
                                  String nodeKey,
                                  final LabelPainter nodeLabelPainter,
                                  String branchKey,
                                  final LabelPainter branchLabelPainter,
                                  final TreeViewer treeViewer) {

        this.tipKey = tipKey;
        this.nodeKey = nodeKey;
        this.branchKey = branchKey;

        optionsPanel = new OptionsPanel();

        tipComboBox = setupComboBox("Tips: ", tipLabelPainter, treeViewer);
        tipComboBox.setSelectedIndex(1);
        nodeComboBox = setupComboBox("Nodes: ", nodeLabelPainter, treeViewer);
        branchComboBox = setupComboBox("Branches: ", branchLabelPainter, treeViewer);

    }

    private JComboBox setupComboBox(String title, final LabelPainter labelPainter, final TreeViewer treeViewer) {
//		String[] attributes = labelPainter.getAttributes();
        final JComboBox displayAttributeCombo = new JComboBox();
        displayAttributeCombo.addItem("None");
        new AttributeComboHelper(displayAttributeCombo, treeViewer, "None");
        optionsPanel.addComponentWithLabel(title, displayAttributeCombo);

        displayAttributeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                String attribute = (String)displayAttributeCombo.getSelectedItem();
                if (attribute.equals("none")) {
                    labelPainter.setVisible(false);
                } else {
                    labelPainter.setDisplayAttribute(attribute);
                    labelPainter.setVisible(true);
                }
            }
        });

        return displayAttributeCombo;
    }

    public JComponent getTitleComponent() {
        return null;
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
        tipComboBox.setSelectedItem(settings.get(tipKey+"."+DISPLAY_ATTRIBUTE_KEY));
        nodeComboBox.setSelectedItem(settings.get(nodeKey+"."+DISPLAY_ATTRIBUTE_KEY));
        branchComboBox.setSelectedItem(settings.get(branchKey+"."+DISPLAY_ATTRIBUTE_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(tipKey+"."+DISPLAY_ATTRIBUTE_KEY, tipComboBox.getSelectedItem().toString());
        settings.put(nodeKey+"."+DISPLAY_ATTRIBUTE_KEY, nodeComboBox.getSelectedItem().toString());
        settings.put(branchKey+"."+DISPLAY_ATTRIBUTE_KEY, branchComboBox.getSelectedItem().toString());
    }

    public String getTitle() {
        return "";
    }

    private final OptionsPanel optionsPanel;

    private final JComboBox tipComboBox;
    private final JComboBox nodeComboBox;
    private final JComboBox branchComboBox;

    private final String tipKey;
    private final String nodeKey;
    private final String branchKey;
}