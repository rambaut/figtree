package figtree.treeviewer;

import figtree.treeviewer.decorators.Decorator;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

import figtree.ui.components.RealNumberField;
import figtree.ui.components.ColorWellButton;

/**
 * @author			Andrew Rambaut
 * @version			$Id$
 */
@Deprecated
public class OldContinuousColourScaleDialog {

    public static class ColourSettings {
        // continuous colour settings:
        public boolean autoRange = true;
        public double fromValue = 0.0;
        public double toValue = 1.0;
        public Color fromColour;
        public Color toColour;
        public Color middleColour;
    }

    private JFrame frame;

    private Decorator decorator;

    private JCheckBox autoScaleCheck;
    private JLabel fromLabel;
    private RealNumberField fromNumberField;
    private JLabel toLabel;
    private RealNumberField toNumberField;

    private ColorWellButton fromColourButton;
    private ColorWellButton toColourButton;

    private JCheckBox middleColourCheck;
    private ColorWellButton middleColourButton;

    public OldContinuousColourScaleDialog(final JFrame frame, final ColourSettings settings) {
        this.frame = frame;

        autoScaleCheck = new JCheckBox("Auto-scale range between min and max values");
        autoScaleCheck.setSelected(settings.autoRange);

        fromLabel = new JLabel("Range from:");
        fromNumberField = new RealNumberField();
        fromNumberField.setColumns(10);
        fromNumberField.setValue(settings.fromValue);

        toLabel = new JLabel("to:");
        toNumberField = new RealNumberField();
        toNumberField.setColumns(10);
        toNumberField.setValue(settings.toValue);

        fromLabel.setEnabled(false);
        fromNumberField.setEnabled(false);
        toLabel.setEnabled(false);
        toNumberField.setEnabled(false);

        middleColourCheck = new JCheckBox("through:");

        fromColourButton = new ColorWellButton(settings.fromColour, "Choose Start Colour");
        toColourButton = new ColorWellButton(settings.toColour, "Choose End Colour");
        middleColourButton = new ColorWellButton(settings.middleColour, "Choose Middle Colour");

        autoScaleCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                boolean enabled = !autoScaleCheck.isSelected();
                fromLabel.setEnabled(enabled);
                fromNumberField.setEnabled(enabled);
                toLabel.setEnabled(enabled);
                toNumberField.setEnabled(enabled);
            }
        });

        middleColourCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                middleColourButton.setEnabled(middleColourCheck.isSelected());
            }
        });
        middleColourCheck.setSelected(settings.middleColour != null);
        middleColourButton.setEnabled(settings.middleColour != null);
    }

    public int showDialog() {

        final OptionsPanel options = new OptionsPanel(6, 6);

        boolean isResizable = false;

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

            JPanel panel2 = new JPanel();
            panel1.setLayout(new FlowLayout());
            panel1.add(middleColourCheck);
            panel1.add(middleColourButton);
            options.addComponent(panel2);

        JOptionPane optionPane = new JOptionPane(options,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null,
                null,
                null);
        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));

        final JDialog dialog = optionPane.createDialog(frame, "Setup colour range");
        dialog.pack();
        dialog.setResizable(isResizable);
        dialog.setVisible(true);

        int result = JOptionPane.CANCEL_OPTION;
        Integer value = (Integer)optionPane.getValue();
        if (value != null && value.intValue() != -1) {
            result = value.intValue();
        }

        return result;
    }

    public void getSettings(ColourSettings settings) {
        settings.autoRange = autoScaleCheck.isSelected();
        settings.fromValue =  fromNumberField.getValue();
        settings.toValue = toNumberField.getValue();
        settings.fromColour = fromColourButton.getSelectedColor();
        settings.toColour = toColourButton.getSelectedColor();
        if (middleColourCheck.isSelected()) {
            settings.middleColour = middleColourButton.getSelectedColor();
        } else {
            settings.middleColour = null;
        }
    }

}