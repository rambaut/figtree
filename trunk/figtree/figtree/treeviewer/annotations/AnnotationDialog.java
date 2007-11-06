/*
 * DemographicDialog.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package figtree.treeviewer.annotations;

import org.virion.jam.components.RealNumberField;
import org.virion.jam.components.WholeNumberField;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import jebl.util.Attributable;

/**
 * DemographicDialog.java
 *
 * Title:			Tracer
 * Description:		An application for analysing MCMC trace files.
 * @author			Andrew Rambaut
 * @author			Alexei Drummond
 * @version			$Id: AnnotationDialog.java,v 1.5 2007/09/04 23:54:44 rambaut Exp $
 */
public class AnnotationDialog {

    private JFrame frame;
    private OptionsPanel options;
    private JComboBox annotationCombo;
    private Attributable item;

    public AnnotationDialog(JFrame frame) {
        this.frame = frame;

    }

    public int showDialog(List<AnnotationDefinition> definitions, Attributable item) {

	    this.item = item;

        options = new OptionsPanel(6, 6);

        annotationCombo = new JComboBox(definitions.toArray());
        annotationCombo.setSelectedIndex(0);
        setupOptionsPanel();

        JOptionPane optionPane = new JOptionPane(options,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null,
                null,
                null);
        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));

        final JDialog dialog = optionPane.createDialog(frame, "Annotate Selection");
        dialog.pack();

        annotationCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                setupOptionsPanel();
                dialog.pack();
            }});

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

        AnnotationDefinition definition = getDefinition();

	    Object value = null;
	    if (item != null) {
		    value = item.getAttribute(definition.getName());
	    }

	    component2 = null;

	    switch (definition.getType()) {
		    case BOOLEAN:
			    component = new JComboBox(new String[] { "True", "False" });
			    if (value != null) {
				    ((JComboBox)component).setSelectedIndex(value.equals(Boolean.TRUE) ? 0 : 1);
			    }
			    options.addComponentWithLabel("Value:", component);
			    break;
		    case INTEGER:
			    component = new WholeNumberField();
			    options.addComponentWithLabel("Value:", component);
			    ((WholeNumberField)component).setColumns(12);
			    if (value != null) {
				    ((WholeNumberField)component).setValue((Integer)value);
			    }
			    break;
		    case REAL:
			    component = new RealNumberField();
			    options.addComponentWithLabel("Value:", component);
			    ((RealNumberField)component).setColumns(12);
			    if (value != null) {
				    ((RealNumberField)component).setValue((Double)value);
			    }
			    break;
		    case STRING:
			    component = new JTextField();
			    options.addComponentWithLabel("Value:", component);
			    ((JTextField)component).setColumns(20);
			    if (value != null) {
				    ((JTextField)component).setText((String)value);
			    }
			    break;
		    case RANGE:
			    component = new RealNumberField();
			    ((RealNumberField)component).setColumns(12);
			    options.addComponentWithLabel("Lower:", component);
			    component2 = new RealNumberField();
			    ((RealNumberField)component2).setColumns(12);
			    options.addComponentWithLabel("Upper:", component2);
			    if (value != null) {
				    ((RealNumberField)component).setValue((Double)((Object[])value)[0]);
				    ((RealNumberField)component2).setValue((Double)((Object[])value)[1]);
			    }
			    break;
		    default:
			    throw new IllegalArgumentException("Unrecognised enum value");
	    }
//        if (definition.getValues() != null) {
//            valuesCombo = new JComboBox(definition.getValues());
//            options.addComponentWithLabel("Value:", valuesCombo);
//        }
    }

    public AnnotationDefinition getDefinition() {
        return (AnnotationDefinition)annotationCombo.getSelectedItem();
    }

    public Object getValue() {
	    AnnotationDefinition definition = getDefinition();
	    switch (definition.getType()) {
		    case BOOLEAN:
			    return new Boolean(((JComboBox)component).getSelectedItem().toString());
		    case INTEGER:
			    return ((WholeNumberField)component).getValue();
		    case REAL:
			    return ((RealNumberField)component).getValue();
		    case STRING:
			    return ((JTextField)component).getText();
		    case RANGE:
			    double lower = ((RealNumberField)component).getValue();
			    double upper = ((RealNumberField)component2).getValue();
			    return new Double[] { lower, upper };
		    default:
			    throw new IllegalArgumentException("Unrecognised enum value");
	    }
    }

	private JComponent component = null;
	private JComponent component2 = null;


}