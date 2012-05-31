package figtree.treeviewer;

import figtree.treeviewer.decorators.Decorator;
import figtree.treeviewer.decorators.DiscreteColorDecorator;
import figtree.treeviewer.decorators.HSBDiscreteColorDecorator;
import figtree.ui.components.ColorWellButton;
import figtree.ui.components.RangeSlider;
import figtree.ui.components.RealNumberField;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * DiscreteColourScaleDialog.java
 *
 * @author			Andrew Rambaut
 * @version			$Id$
 */
public class DiscreteColourScaleDialog {
    private static final int SLIDER_RANGE = 1000;

    private JFrame frame;

    private HSBDiscreteColorDecorator decorator;

    private JComboBox primaryAxisCombo = new JComboBox(HSBDiscreteColorDecorator.Axis.values());
    private SpinnerNumberModel secondaryCountSpinnerModel = new SpinnerNumberModel(2, 1, 100, 1);
    private JSpinner secondaryCountSpinner = new JSpinner(secondaryCountSpinnerModel);

    private RangeSlider hueSlider;
    private RangeSlider saturationSlider;
    private RangeSlider brightnessSlider;

    private ColourTableModel tableModel;

    public DiscreteColourScaleDialog(final JFrame frame) {
        this.frame = frame;

        hueSlider = new RangeSlider(0, SLIDER_RANGE);
        saturationSlider = new RangeSlider(0, SLIDER_RANGE);
        brightnessSlider = new RangeSlider(0, SLIDER_RANGE);

        tableModel = new ColourTableModel();
    }

    public int showDialog() {

        final OptionsPanel options = new OptionsPanel(6, 6);

        JTable table = new JTable(tableModel);
        table.setDefaultRenderer(Color.class, new ColorRenderer(true));
        table.setDefaultRenderer(Paint.class, new ColorRenderer(true));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setMinimumSize(new Dimension(240, 320));
        options.addSpanningComponent(scrollPane);

        options.addComponentWithLabel("Primary: ", primaryAxisCombo);
        options.addComponentWithLabel("Secondary count: ", secondaryCountSpinner);

        options.addComponentWithLabel("Hue: ", hueSlider);
        options.addComponentWithLabel("Saturation: ", saturationSlider);
        options.addComponentWithLabel("Brightness: ", brightnessSlider);

        setDecorator(decorator);

        primaryAxisCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                setupDecorator(decorator);
                tableModel.fireTableDataChanged();
            }
        });

        ChangeListener listener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                setupDecorator(decorator);
                tableModel.fireTableDataChanged();
            }
        };

        secondaryCountSpinner.addChangeListener(listener);
        hueSlider.addChangeListener(listener);
        saturationSlider.addChangeListener(listener);
        brightnessSlider.addChangeListener(listener);

        JOptionPane optionPane = new JOptionPane(options,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null,
                null,
                null);
        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));

        final JDialog dialog = optionPane.createDialog(frame, "Setup colour range");
        dialog.pack();
        dialog.setResizable(true);
        dialog.setVisible(true);

        int result = JOptionPane.CANCEL_OPTION;
        Integer value = (Integer)optionPane.getValue();
        if (value != null && value.intValue() != -1) {
            result = value.intValue();
        }

        return result;
    }

    public void setDecorator(HSBDiscreteColorDecorator decorator) {
        this.decorator = decorator;

        primaryAxisCombo.setSelectedItem(decorator.getPrimaryAxis());
        secondaryCountSpinnerModel.setValue(decorator.getSecondaryCount());

        hueSlider.setValue((int)(decorator.getHueLower() * SLIDER_RANGE));
        hueSlider.setUpperValue((int) (decorator.getHueUpper() * SLIDER_RANGE));

        saturationSlider.setValue((int)(decorator.getSaturationLower() * SLIDER_RANGE));
        saturationSlider.setUpperValue((int)(decorator.getSaturationUpper() * SLIDER_RANGE));

        brightnessSlider.setValue((int)(decorator.getBrightnessLower() * SLIDER_RANGE));
        brightnessSlider.setUpperValue((int)(decorator.getBrightnessUpper() * SLIDER_RANGE));
    }

    public void setupDecorator(HSBDiscreteColorDecorator decorator) {
        decorator.setPrimaryAxis((HSBDiscreteColorDecorator.Axis) primaryAxisCombo.getSelectedItem());
        decorator.setSecondaryCount(secondaryCountSpinnerModel.getNumber().intValue());

        decorator.setHueLower(((float) hueSlider.getValue()) / SLIDER_RANGE);
        decorator.setHueUpper(((float) hueSlider.getUpperValue()) / SLIDER_RANGE);

        decorator.setSaturationLower(((float) saturationSlider.getValue()) / SLIDER_RANGE);
        decorator.setSaturationUpper(((float) saturationSlider.getUpperValue()) / SLIDER_RANGE);

        decorator.setBrightnessLower(((float) brightnessSlider.getValue()) / SLIDER_RANGE);
        decorator.setBrightnessUpper(((float) brightnessSlider.getUpperValue()) / SLIDER_RANGE);
    }

    class ColourTableModel extends DefaultTableModel {
        private final String[] COLUMN_NAMES = { "Value", "Colour" };

        @Override
        public int getRowCount() {
            if (decorator == null) return 0;
            return decorator.getValues().size();
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
            switch (column) {
                case 0:
                    return decorator.getValues().get(row);
                case 1:
                    return decorator.getColor(decorator.getValues().get(row));
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