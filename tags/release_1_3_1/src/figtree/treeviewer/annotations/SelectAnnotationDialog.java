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
import java.util.List;

/**
 * DemographicDialog.java
 *
 * Title:			Tracer
 * Description:		An application for analysing MCMC trace files.
 * @author			Andrew Rambaut
 * @author			Alexei Drummond
 * @version			$Id: AnnotationDialog.java,v 1.5 2007/09/04 23:54:44 rambaut Exp $
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