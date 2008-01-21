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
import org.virion.jam.components.WholeNumberField;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.*;
import java.util.List;

import jebl.util.Attributable;
import figtree.treeviewer.annotations.AnnotationDefinition;

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

    private JColorChooser fromColorChooser;
    private JColorChooser toColorChooser;

    public ColourScaleDialog(JFrame frame) {
        this.frame = frame;

    }

    public int showDialog() {

        options = new OptionsPanel(6, 6);

        autoScaleCheck = new JCheckBox("Auto-scale range");
        autoScaleCheck.setSelected(true);

        fromLabel = new JLabel("Range from:");
        fromNumberField = new RealNumberField();
        fromNumberField.setColumns(10);

        toLabel = new JLabel("to:");
        toNumberField = new RealNumberField();
        toNumberField.setColumns(10);

        fromColorChooser = new JColorChooser();
        toColorChooser = new JColorChooser();

        fromLabel.setEnabled(false);
        fromNumberField.setEnabled(false);
        toLabel.setEnabled(false);
        toNumberField.setEnabled(false);

        setupOptionsPanel();

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

    private void setupOptionsPanel() {
        options.removeAll();

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
        panel1.add(new JLabel("Colour from:"));
        panel1.add(fromColorChooser);
        panel1.add(new JLabel("to:"));
        panel1.add(toColorChooser);
        options.addComponent(panel1);


    }

    public boolean getAutoRange() {
        return autoScaleCheck.isSelected();
    }

    public double getFromValue() {
        return fromNumberField.getValue();
    }

    public double getToValue() {
        return toNumberField.getValue();
    }

    public Color getFromColor() {
        return fromColorChooser.getColor();
    }

    public Color getToColor() {
        return toColorChooser.getColor();
    }


}