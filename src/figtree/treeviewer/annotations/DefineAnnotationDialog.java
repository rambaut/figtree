/*
 * DefineAnnotationDialog.java
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

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class DefineAnnotationDialog {

    private JFrame frame;


    public DefineAnnotationDialog(JFrame frame) {
        this.frame = frame;

    }

    public int showDialog(AnnotationDefinition definition) {

        OptionsPanel options = new OptionsPanel(6, 6);

	    JTextField nameField = new JTextField(definition.getName());
	    nameField.setColumns(32);
	    options.addComponentWithLabel("Name:", nameField);

        JComboBox typeCombo = new JComboBox(new Object[] {
		        AnnotationDefinition.Type.INTEGER,
		        AnnotationDefinition.Type.REAL,
		        AnnotationDefinition.Type.STRING,
		        AnnotationDefinition.Type.BOOLEAN,
		        AnnotationDefinition.Type.RANGE
        });
	    typeCombo.setSelectedItem(definition.getType());
        options.addComponentWithLabel("Type:", typeCombo);

        JOptionPane optionPane = new JOptionPane(options,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null,
                null,
                null);
        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));

        final JDialog dialog = optionPane.createDialog(frame, "Annotation Definitions");
        dialog.pack();

        dialog.setVisible(true);

	    int result = JOptionPane.CANCEL_OPTION;
	    Integer value = (Integer)optionPane.getValue();
	    if (value != null && value.intValue() != -1) {
	        result = value.intValue();
	    }

	    if (result == JOptionPane.OK_OPTION) {
		    definition.setName(nameField.getText());
		    definition.setType((AnnotationDefinition.Type)typeCombo.getSelectedItem());
	    }

	    return result;
    }

}