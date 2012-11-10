package figtree.treeviewer;

import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;

import figtree.ui.components.RealNumberField;

/**
 * @author			Andrew Rambaut
 * @version			$Id$
 */
public class SizeScaleDialog {

    private JFrame frame;
    private String dimensionName;

    private OptionsPanel options;
    private JCheckBox autoScaleCheck;
    private JLabel fromLabel;
    private RealNumberField fromNumberField;
    private JLabel toLabel;
    private RealNumberField toNumberField;

    private RealNumberField fromSizeField;
	private RealNumberField toSizeField;

    public SizeScaleDialog(final JFrame frame, final String dimensionName, boolean autoRange, double fromValue, double toValue,
                           final double fromWidth, final double toWidth) {
        this.frame = frame;

        this.dimensionName = dimensionName;

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

	    fromSizeField = new RealNumberField();
		fromSizeField.setColumns(4);
	    fromSizeField.setValue(fromWidth);
		toSizeField = new RealNumberField();
		toSizeField.setColumns(4);
	    toSizeField.setValue(toWidth);

	    autoScaleCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                boolean enabled = !autoScaleCheck.isSelected();
                fromLabel.setEnabled(enabled);
                fromNumberField.setEnabled(enabled);
                toLabel.setEnabled(enabled);
                toNumberField.setEnabled(enabled);
            }
        });
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
	    panel1.add(new JLabel(dimensionName + " from:"));
	    panel1.add(fromSizeField);
	    panel1.add(new JLabel("to:"));
	    panel1.add(toSizeField);
	    options.addComponent(panel1);

        JOptionPane optionPane = new JOptionPane(options,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null,
                null,
                null);
        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));

        final JDialog dialog = optionPane.createDialog(frame, "Setup " + dimensionName + " scale");
        dialog.pack();

        autoScaleCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                boolean enabled = !autoScaleCheck.isSelected();
                fromLabel.setEnabled(enabled);
                fromNumberField.setEnabled(enabled);
                toLabel.setEnabled(enabled);
                toNumberField.setEnabled(enabled);
            }});

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

    public Number getFromWidth() {
        return fromSizeField.getValue();
    }

    public Number getToWidth() {
        return toSizeField.getValue();
    }


}