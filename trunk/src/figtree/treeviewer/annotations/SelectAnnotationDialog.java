/*
 * SelectAnnotationDialog.java
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

package figtree.treeviewer.annotations;

import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class SelectAnnotationDialog {

    private JFrame frame;
    private OptionsPanel options;
    private JComboBox annotationCombo;

    public SelectAnnotationDialog(JFrame frame) {
        this.frame = frame;
    }

    public int showDialog(List<String> annotationNames) {

        options = new OptionsPanel(6, 6);

        annotationCombo = new JComboBox(annotationNames.toArray());
        annotationCombo.setSelectedIndex(0);
        setupOptionsPanel();

        JOptionPane optionPane = new JOptionPane(options,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null,
                null,
                null);
        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));

        final JDialog dialog = optionPane.createDialog(frame, "Select Annotation");
        dialog.pack();

        dialog.setVisible(true);

        int result = JOptionPane.CANCEL_OPTION;
        Integer value = (Integer)optionPane.getValue();
        if (value != null && value.intValue() != -1) {
            result = value.intValue();
        }

        return result;
    }

    private void setupOptionsPanel() {
        options.removeAll();

        options.addComponentWithLabel("Annotation:", annotationCombo);

    }

    public String getAnnotationName() {
        return (String)annotationCombo.getSelectedItem();
    }


}