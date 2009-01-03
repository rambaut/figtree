/*
 * DemographicDialog.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package figtree.treeviewer.annotations;

import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * DemographicDialog.java
 *
 * Title:			Tracer
 * Description:		An application for analysing MCMC trace files.
 * @author			Andrew Rambaut
 * @author			Alexei Drummond
 * @version			$Id: DefineAnnotationDialog.java,v 1.3 2006/08/28 13:19:41 rambaut Exp $
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