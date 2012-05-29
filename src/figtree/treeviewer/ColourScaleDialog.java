package figtree.treeviewer;

import figtree.treeviewer.decorators.Decorator;
import figtree.treeviewer.decorators.DiscreteColorDecorator;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.event.*;
import java.awt.*;

import figtree.ui.components.RealNumberField;
import figtree.ui.components.ColorWellButton;

/**
 * ColourScaleDialog.java
 *
 * @author			Andrew Rambaut
 * @version			$Id$
 */
public class ColourScaleDialog {

    public static class ColourSettings {
        // continuous colour settings:
        public boolean autoRange = true;
        public double fromValue = 0.0;
        public double toValue = 1.0;
        public Color fromColour;
        public Color toColour;
        public Color middleColour;

        // discrete colour settings:


        // common settings:
        public boolean useGradient = false;
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

    public ColourScaleDialog(final JFrame frame, final ColourSettings settings) {
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

        autoScaleCheck.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                boolean enabled = !autoScaleCheck.isSelected();
                fromLabel.setEnabled(enabled);
                fromNumberField.setEnabled(enabled);
                toLabel.setEnabled(enabled);
                toNumberField.setEnabled(enabled);
            }});

        middleColourCheck.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                middleColourButton.setEnabled(middleColourCheck.isSelected());
            }});
        middleColourCheck.setSelected(settings.middleColour != null);
        middleColourButton.setEnabled(settings.middleColour != null);
    }

    public int showDialog() {

        final OptionsPanel options = new OptionsPanel(6, 6);

        boolean isResizable = false;

        if (decorator instanceof DiscreteColorDecorator) {
            JTable table = new JTable(new ColourTableModel());
            table.setDefaultRenderer(Color.class, new ColorRenderer(true));
            table.setDefaultRenderer(Paint.class, new ColorRenderer(true));

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setMinimumSize(new Dimension(240, 320));
            options.addSpanningComponent(scrollPane);
            isResizable = true;
        } else {
            // a continuous decorator...
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
        }

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

    public void setDecorator(Decorator decorator) {
        this.decorator = decorator;
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

    class ColourTableModel extends DefaultTableModel {
        private final String[] COLUMN_NAMES = { "Value", "Colour" };

        @Override
        public int getRowCount() {
            DiscreteColorDecorator dcd = (DiscreteColorDecorator)decorator;
            return dcd.getValues().size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Object getValueAt(int row, int column) {
            DiscreteColorDecorator dcd = (DiscreteColorDecorator)decorator;
            switch (column) {
                case 0:
                    return dcd.getValues().get(row);
                case 1:
                    return dcd.getColor(dcd.getValues().get(row));
            }
            return null;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 1) {
                    return Color.class;
            }
            return String.class;
        }

        @Override
        public void setValueAt(Object o, int row, int column) {
        }
    }


    /*
    * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
    *
    * Redistribution and use in source and binary forms, with or without
    * modification, are permitted provided that the following conditions
    * are met:
    *
    *   - Redistributions of source code must retain the above copyright
    *     notice, this list of conditions and the following disclaimer.
    *
    *   - Redistributions in binary form must reproduce the above copyright
    *     notice, this list of conditions and the following disclaimer in the
    *     documentation and/or other materials provided with the distribution.
    *
    *   - Neither the name of Oracle or the names of its
    *     contributors may be used to endorse or promote products derived
    *     from this software without specific prior written permission.
    *
    * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
    * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
    * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
    * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
    * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
    * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
    * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
    * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
    * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
    * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
    */

/*
 * ColorRenderer.java (compiles with releases 1.2, 1.3, and 1.4) is used by
 * TableDialogEditDemo.java.
 */

    public class ColorRenderer extends JLabel
            implements TableCellRenderer {
        Border unselectedBorder = null;
        Border selectedBorder = null;
        boolean isBordered = true;

        public ColorRenderer(boolean isBordered) {
            this.isBordered = isBordered;
            setOpaque(true); //MUST do this for background to show up.
        }

        public Component getTableCellRendererComponent(
                JTable table, Object color,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            Color newColor = (Color)color;
            setBackground(newColor);
            if (isBordered) {
                if (isSelected) {
                    if (selectedBorder == null) {
                        selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                table.getSelectionBackground());
                    }
                    setBorder(selectedBorder);
                } else {
                    if (unselectedBorder == null) {
                        unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                table.getBackground());
                    }
                    setBorder(unselectedBorder);
                }
            }

            setToolTipText("RGB value: " + newColor.getRed() + ", "
                    + newColor.getGreen() + ", "
                    + newColor.getBlue());
            return this;
        }
    }
}