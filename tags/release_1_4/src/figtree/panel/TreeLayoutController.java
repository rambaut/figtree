/*
 * TreeLayoutController.java
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

import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import figtree.treeviewer.treelayouts.RectilinearTreeLayout;

/**
 * @author Andrew Rambaut
 * @version $Id: TreeLayoutController.java,v 1.1 2007/08/14 15:50:42 rambaut Exp $
 */
public class TreeLayoutController extends AbstractController {

    private static final String RECTILINEAR_LAYOUT_KEY = "rectilinearLayout";
    private static final String ALIGN_TIP_LABELS_KEY = "alignTipLabels";

    public TreeLayoutController(final RectilinearTreeLayout treeLayout) {
        titleLabel = new JLabel("Rectangular Layout");
        optionsPanel = new OptionsPanel();

        alignTipLabelsCheck = new JCheckBox("Align Tip Labels");
        alignTipLabelsCheck.setOpaque(false);

        alignTipLabelsCheck.setSelected(treeLayout.isAlignTipLabels());
        alignTipLabelsCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                treeLayout.setAlignTipLabels(alignTipLabelsCheck.isSelected());
            }
        });
        optionsPanel.addComponent(alignTipLabelsCheck);
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
        // nothing to do
    }

    public void setSettings(Map<String,Object> settings) {
        alignTipLabelsCheck.setSelected((Boolean) settings.get(RECTILINEAR_LAYOUT_KEY + "." + ALIGN_TIP_LABELS_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(RECTILINEAR_LAYOUT_KEY + "." + ALIGN_TIP_LABELS_KEY, alignTipLabelsCheck.isSelected());
    }

    private final JLabel titleLabel;
    private final OptionsPanel optionsPanel;

    private final JCheckBox alignTipLabelsCheck;

}