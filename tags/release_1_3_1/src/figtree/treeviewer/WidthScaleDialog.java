package figtree.treeviewer;

import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.*;

import figtree.ui.components.RealNumberField;

/**
 * WidthScaleDialog.java
 *
 * @author			Andrew Rambaut
 * @version			$Id$
 */
public class WidthScaleDialog {

    private JFrame frame;
    private OptionsPanel options;
    private JCheckBox autoScaleCheck;
    private JLabel fromLabel;
    private RealNumberField fromNumberField;
    private JLabel toLabel;
    private RealNumberField toNumberField;

    private RealNumberField fromWidthField;
	private RealNumberField toWidthField;

    public WidthScaleDialog(final JFrame frame, boolean autoRange, double fromValue, double toValue,
	                         final double fromWidth, final double toWidth) {
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

	    fromWidthField = new RealNumberField();
		fromWidthField.setColumns(4);
	    fromWidthField.setValue(fromWidth);
		toWidthField = new RealNumberField();
		toWidthField.setColumns(4);
	    toWidthField.setValue(toWidth);

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
	    panel1.add(new JLabel("Width from:"));
	    panel1.add(fromWidthField);
	    panel1.add(new JLabel("to:"));
	    panel1.add(toWidthField);
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

        autoScaleCheck.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
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
        return fromWidthField.getValue();
    }

    public Number getToWidth() {
        return toWidthField.getValue();
    }


}