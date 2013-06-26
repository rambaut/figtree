package figtree.treeviewer;

import figtree.treeviewer.decorators.*;
import figtree.ui.components.RangeSlider;
import jam.panels.OptionsPanel;

import javax.activation.DataHandler;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.List;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/**
 * @author			Andrew Rambaut
 * @version			$Id$
 */
public class DiscreteColourScaleDialog {
    private static final int SLIDER_RANGE = 1000;

    private static final String HSB_SPECTRUM = "HSB Spectrum";
    private static final String FIXED_COLOURS = "Fixed Colours";

    private JFrame frame;

    private DiscreteColourDecorator decorator;

    private JTable table;

    private JComboBox colourSchemeCombo = new JComboBox(new String[] { HSB_SPECTRUM /*, FIXED_COLOURS */} );

    CardLayout cardLayout = new CardLayout();
    private final JPanel colourSchemePanel;

    private Map<String, ColourSchemePanel> colourSchemeNamePanelMap = new HashMap<String, ColourSchemePanel>();
    private Map<Class, String> colourSchemeClassNameMap = new HashMap<Class, String>();

    private java.util.List<Object> discreteValues = null;

    private ColourTableModel tableModel;

    private JDialog dialog;

    public DiscreteColourScaleDialog(final JFrame frame) {
        this.frame = frame;

        colourSchemeNamePanelMap.put(HSB_SPECTRUM, new HSBColourSchemePanel());
//        colourSchemeNamePanelMap.put(FIXED_COLOURS, new FixedColourSchemePanel());

        colourSchemeClassNameMap.put(HSBDiscreteColourDecorator.class, HSB_SPECTRUM);
//        colourSchemeClassNameMap.put(FixedDiscreteColourDecorator.class, FIXED_COLOURS);


        colourSchemePanel = new JPanel(cardLayout);

        for (String name : colourSchemeNamePanelMap.keySet()) {
            ColourSchemePanel panel = colourSchemeNamePanelMap.get(name);
            colourSchemePanel.add(panel.getPanel(), name);
        }

        colourSchemeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                cardLayout.show(colourSchemePanel, colourSchemeCombo.getSelectedItem().toString());
                decorator = colourSchemeNamePanelMap.get(colourSchemeCombo.getSelectedItem()).getDecorator();
                tableModel.fireTableDataChanged();
                dialog.pack();
            }
        });


        tableModel = new ColourTableModel();

        table = new JTable(tableModel);
        table.setDefaultRenderer(Color.class, new ColorRenderer(true));
        table.setDefaultRenderer(Paint.class, new ColorRenderer(true));
        table.setDragEnabled(true);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(new TableRowTransferHandler(table));

        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);

        table.getColumnModel().getColumn(1).setWidth(80);
        table.getColumnModel().getColumn(1).setMinWidth(80);
        table.getColumnModel().getColumn(1).setMaxWidth(80);
        table.getColumnModel().getColumn(1).setResizable(false);
    }

    public int showDialog() {

        final OptionsPanel options = new OptionsPanel(6, 6);

//        options.addSpanningComponent(new JLabel("Editing colour range for attribute: " + decorator.getAttributeName()));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setMinimumSize(new Dimension(120, 120));
        options.addSpanningComponent(scrollPane);

        options.addComponentWithLabel("Scheme: ", colourSchemeCombo);

        colourSchemePanel.setBorder(BorderFactory.createBevelBorder(1));
        options.addSpanningComponent(colourSchemePanel);

        setDecorator(decorator);

        JOptionPane optionPane = new JOptionPane(options,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null,
                null,
                null);
        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));

        dialog = optionPane.createDialog(frame, "Setup colour range: " + decorator.getAttributeName());

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

    public void setDecorator(DiscreteColourDecorator decorator) {
        this.decorator = decorator;

        discreteValues = new ArrayList<Object>(decorator.getValues());

        for (String key : colourSchemeNamePanelMap.keySet()) {
            colourSchemeNamePanelMap.get(key).setDecorator(decorator);
        }
    }

    public DiscreteColourDecorator getDecorator() {
        String name = colourSchemeCombo.getSelectedItem().toString();
        decorator = colourSchemeNamePanelMap.get(name).getDecorator();
        decorator.setValuesOrder(discreteValues);
        return decorator;
    }



    interface Reorderable {
        public void reorder(java.util.List<Integer> sourceIndices, int destinationIndex);
    };

    class ColourTableModel extends DefaultTableModel implements Reorderable {
        private final String[] COLUMN_NAMES = { "Value", "Colour" };

        @Override
        public int getRowCount() {
            if (discreteValues == null) return 0;
            return discreteValues.size();
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
                    return discreteValues.get(row);
                case 1:
                    // until the OK button is pressed the trait values are not actually
                    // reordered so use the default colour order.
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

        @Override
        public void reorder(java.util.List<Integer> sourceIndices, int destinationIndex) {
            java.util.List<Object> itemsToMove = new ArrayList<Object>();
            for (int src : sourceIndices) {
                itemsToMove.add(discreteValues.get(src));
            }
            for (int i = sourceIndices.size() - 1; i >= 0; i--) {
                int src = sourceIndices.get(i);
                discreteValues.remove(src);
                if (destinationIndex > src) {
                    destinationIndex -= 1;
                }
            }
            discreteValues.addAll(destinationIndex, itemsToMove);
            fireTableDataChanged();
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

    /**
     * Handles drag & drop row reordering
     */
    public class TableRowTransferHandler extends TransferHandler {
        //        private final DataFlavor localObjectFlavor = new ActivationDataFlavor(Integer.class, DataFlavor.javaJVMLocalObjectMimeType, "Integer Row Index");
        private final DataFlavor localObjectFlavor = new DataFlavor(ArrayList.class, "Integer Row Index");
        private JTable table = null;

        public TableRowTransferHandler(JTable table) {
            this.table = table;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            assert (c == table);
            java.util.List<Integer> selectedRows = new ArrayList<Integer>();
            for (int row : table.getSelectedRows()) {
                selectedRows.add(row);
            }

            return new DataHandler(selectedRows, localObjectFlavor.getMimeType());
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport info) {
            boolean b = info.getComponent() == table && info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
            table.setCursor(b ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
            return b;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.MOVE;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport info) {
            JTable target = (JTable) info.getComponent();
            JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
            int index = dl.getRow();
            int max = table.getModel().getRowCount();
            if (index < 0 || index > max)
                index = max;
            target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            try {
                java.util.List<Integer> selectedRows = (java.util.List)info.getTransferable().getTransferData(localObjectFlavor);
                if (selectedRows != null && selectedRows.size() > 0) {
                    ((Reorderable)table.getModel()).reorder(selectedRows, index);
                    for (int row : selectedRows) {
                    if (index > row)
                        index--;
                    }
                    target.getSelectionModel().addSelectionInterval(index, index + selectedRows.size() - 1);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void exportDone(JComponent c, Transferable t, int act) {
            if (act == TransferHandler.MOVE) {
                table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }

    }

    private interface ColourSchemePanel {
        void setDecorator(DiscreteColourDecorator decorator);
        DiscreteColourDecorator getDecorator();

        JPanel getPanel();


        String getName();
    }

    private class HSBColourSchemePanel implements ColourSchemePanel  {
        public HSBColourSchemePanel() {
            hueSlider = new RangeSlider(0, SLIDER_RANGE);
            saturationSlider = new RangeSlider(0, SLIDER_RANGE);
            brightnessSlider = new RangeSlider(0, SLIDER_RANGE);
        }

        public void setDecorator(DiscreteColourDecorator decorator) {
            if (decorator instanceof HSBDiscreteColourDecorator) {
                hsbDecorator = (HSBDiscreteColourDecorator)decorator;
            } else {
                if (hsbDecorator == null) {
                    hsbDecorator = new HSBDiscreteColourDecorator(decorator.getAttributeName());
                }
            }

            hueSlider.setValue((int)(hsbDecorator.getHueLower() * SLIDER_RANGE));
            hueSlider.setUpperValue((int) (hsbDecorator.getHueUpper() * SLIDER_RANGE));

            saturationSlider.setValue((int)(hsbDecorator.getSaturationLower() * SLIDER_RANGE));
            saturationSlider.setUpperValue((int)(hsbDecorator.getSaturationUpper() * SLIDER_RANGE));

            brightnessSlider.setValue((int)(hsbDecorator.getBrightnessLower() * SLIDER_RANGE));
            brightnessSlider.setUpperValue((int)(hsbDecorator.getBrightnessUpper() * SLIDER_RANGE));
        }

        @Override
        public  DiscreteColourDecorator getDecorator() {
            hsbDecorator.setHueLower(((float) hueSlider.getValue()) / SLIDER_RANGE);
            hsbDecorator.setHueUpper(((float) hueSlider.getUpperValue()) / SLIDER_RANGE);

            hsbDecorator.setSaturationLower(((float) saturationSlider.getValue()) / SLIDER_RANGE);
            hsbDecorator.setSaturationUpper(((float) saturationSlider.getUpperValue()) / SLIDER_RANGE);

            hsbDecorator.setBrightnessLower(((float) brightnessSlider.getValue()) / SLIDER_RANGE);
            hsbDecorator.setBrightnessUpper(((float) brightnessSlider.getUpperValue()) / SLIDER_RANGE);

            return hsbDecorator;
        }

        @Override
        public JPanel getPanel() {
            if (panel == null) {
                final OptionsPanel options = new OptionsPanel(6, 6);

                options.addComponentWithLabel("Primary: ", primaryAxisCombo);
                options.addComponentWithLabel("Secondary count: ", secondaryCountSpinner);

                options.addComponentWithLabel("Hue: ", hueSlider);
                options.addComponentWithLabel("Saturation: ", saturationSlider);
                options.addComponentWithLabel("Brightness: ", brightnessSlider);

                primaryAxisCombo.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        getDecorator();
                        tableModel.fireTableDataChanged();
                    }
                });

                ChangeListener listener = new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        getDecorator();
                        tableModel.fireTableDataChanged();
                    }
                };

                secondaryCountSpinner.addChangeListener(listener);
                hueSlider.addChangeListener(listener);
                saturationSlider.addChangeListener(listener);
                brightnessSlider.addChangeListener(listener);

                panel = options;
            }
            return panel;
        }

        @Override
        public String getName() {
            return HSB_SPECTRUM;
        }

        private JComboBox primaryAxisCombo = new JComboBox(HSBDiscreteColourDecorator.Axis.values());
        private SpinnerNumberModel secondaryCountSpinnerModel = new SpinnerNumberModel(2, 1, 100, 1);
        private JSpinner secondaryCountSpinner = new JSpinner(secondaryCountSpinnerModel);

        private RangeSlider hueSlider;
        private RangeSlider saturationSlider;
        private RangeSlider brightnessSlider;

        private HSBDiscreteColourDecorator hsbDecorator = null;

        private JPanel panel = null;
    }

    private class FixedColourSchemePanel implements ColourSchemePanel  {
        public FixedColourSchemePanel() {
        }

        public void setDecorator(DiscreteColourDecorator decorator) {
            if (decorator instanceof FixedDiscreteColourDecorator) {
                fixedDecorator = (FixedDiscreteColourDecorator)decorator;
            } else {
                if (fixedDecorator == null) {
                    fixedDecorator = new FixedDiscreteColourDecorator(decorator.getAttributeName());
                }
            }
//            fixedDecorator.setValues(discreteValues);
        }

        @Override
        public  DiscreteColourDecorator getDecorator() {
            return fixedDecorator;
        }

        @Override
        public JPanel getPanel() {
            if (panel == null) {
                final OptionsPanel options = new OptionsPanel(6, 6);

                panel = options;
            }
            return panel;
        }

        @Override
        public String getName() {
            return FIXED_COLOURS;
        }

        private FixedDiscreteColourDecorator fixedDecorator = null;

        private JPanel panel = null;
    }

}