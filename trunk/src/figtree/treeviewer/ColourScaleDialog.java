/*
 * DemographicDialog.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package figtree.treeviewer;

import org.virion.jam.components.RealNumberField;
import org.virion.jam.components.ColorWellButton;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.*;
import java.awt.*;

/**
 * DemographicDialog.java
 *
 * Title:			Tracer
 * Description:		An application for analysing MCMC trace files.
 * @author			Andrew Rambaut
 * @author			Alexei Drummond
 * @version			$Id: AnnotationDialog.java,v 1.5 2007/09/04 23:54:44 rambaut Exp $
 */
public class ColourScaleDialog {

	private JFrame frame;
	private OptionsPanel options;
	private JCheckBox autoScaleCheck;
	private JLabel fromLabel;
	private RealNumberField fromNumberField;
	private JLabel toLabel;
	private RealNumberField toNumberField;

	private ColorWellButton fromColourButton;
	private ColorWellButton toColourButton;

	public ColourScaleDialog(final JFrame frame, boolean autoRange, double fromValue, double toValue,
	                         final Color fromColour, final Color toColour) {
		this.frame = frame;

		autoScaleCheck = new JCheckBox("Auto-scale range");
		autoScaleCheck.setSelected(autoRange);

		fromLabel = new JLabel("Range from:");
		fromNumberField = new RealNumberField();
		fromNumberField.setColumns(10);
		fromNumberField.setValue(fromValue);

		toLabel = new JLabel("to:");
		toNumberField = new RealNumberField();
		toNumberField.setColumns(10);
		toNumberField.setValue(toValue);

		fromLabel.setEnabled(false);
		fromNumberField.setEnabled(false);
		toLabel.setEnabled(false);
		toNumberField.setEnabled(false);

		fromColourButton = new ColorWellButton(fromColour, "Choose Start Colour");
		toColourButton = new ColorWellButton(toColour, "Choose End Colour");

		autoScaleCheck.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				boolean enabled = !autoScaleCheck.isSelected();
				fromLabel.setEnabled(enabled);
				fromNumberField.setEnabled(enabled);
				toLabel.setEnabled(enabled);
				toNumberField.setEnabled(enabled);
			}});
	}

	public int showDialog() {

		options = new OptionsPanel(6, 6);

		options.addComponent(autoScaleCheck);

		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		panel.add(fromLabel);
		panel.add(fromNumberField);
		panel.add(toLabel);
		panel.add(toNumberField);
		options.addComponent(panel);

		JPanel panel1 = new JPanel();
		panel1.setLayout(new FlowLayout());
		panel1.add(new JLabel("Colour gradient from:"));
		panel1.add(fromColourButton);
		panel1.add(new JLabel("to:"));
		panel1.add(toColourButton);
		options.addComponent(panel1);

		JOptionPane optionPane = new JOptionPane(options,
				JOptionPane.QUESTION_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION,
				null,
				null,
				null);
		optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));

		final JDialog dialog = optionPane.createDialog(frame, "Setup colour range");
		dialog.pack();

		dialog.setVisible(true);

		int result = JOptionPane.CANCEL_OPTION;
		Integer value = (Integer)optionPane.getValue();
		if (value != null && value.intValue() != -1) {
			result = value.intValue();
		}

		return result;
	}

	public boolean getAutoRange() {
		return autoScaleCheck.isSelected();
	}

	public Number getFromValue() {
		return fromNumberField.getValue();
	}

	public Number getToValue() {
		return toNumberField.getValue();
	}

	public Color getFromColour() {
		return fromColourButton.getSelectedColor();
	}

	public Color getToColour() {
		return toColourButton.getSelectedColor();
	}

}