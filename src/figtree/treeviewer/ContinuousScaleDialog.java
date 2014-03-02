/*
 * ContinuousScaleDialog.java
 *
 * Copyright (C) 2006-2014 Andrew Rambaut
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

package figtree.treeviewer;

import figtree.treeviewer.decorators.Decorator;
import figtree.ui.components.ColorWellButton;
import figtree.ui.components.RealNumberField;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Andrew Rambaut
 * @version $Id$
 *
 * $HeadURL$
 *
 * $LastChangedBy$
 * $LastChangedDate$
 * $LastChangedRevision$
 */
@Deprecated
public class ContinuousScaleDialog {

    public static class ScaleSettings {
        // continuous colour settings:
        public boolean autoRange = true;
        public double fromValue = 0.0;
        public double toValue = 1.0;
        public boolean logarithm = false;
    }

    private JFrame frame;

    private JCheckBox autoScaleCheck;
    private JLabel fromLabel;
    private RealNumberField fromNumberField;
    private JLabel toLabel;
    private RealNumberField toNumberField;

    public ContinuousScaleDialog(final JFrame frame, final ScaleSettings settings) {
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

        autoScaleCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                boolean enabled = !autoScaleCheck.isSelected();
                fromLabel.setEnabled(enabled);
                fromNumberField.setEnabled(enabled);
                toLabel.setEnabled(enabled);
                toNumberField.setEnabled(enabled);
            }
        }

        );
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

    public void getSettings(ScaleSettings settings) {
        settings.autoRange = autoScaleCheck.isSelected();
        settings.fromValue =  fromNumberField.getValue();
        settings.toValue = toNumberField.getValue();
    }

}