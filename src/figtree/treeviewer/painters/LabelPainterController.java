package figtree.treeviewer.painters;

import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.prefs.Preferences;

import figtree.treeviewer.*;
import figtree.treeviewer.decorators.*;
import jebl.util.Attributable;

/**
 * @author Andrew Rambaut
 * @version $Id: LabelPainterController.java 773 2007-09-04 23:51:07Z rambaut $
 */
public class LabelPainterController extends AbstractController {

    public static Preferences PREFS = Preferences.userNodeForPackage(TreeViewer.class);

    private static final String USER_SELECTION = "User Selection";

    public static final String FONT_NAME_KEY = "fontName";
    public static final String FONT_SIZE_KEY = "fontSize";
    public static final String FONT_STYLE_KEY = "fontStyle";

    public static final String COLOR_ATTRIBUTE_KEY = "colorAttribute";

    public static final String NUMBER_FORMATTING_KEY = "numberFormatting";

    public static final String DISPLAY_ATTRIBUTE_KEY = "displayAttribute";
    public static final String SIGNIFICANT_DIGITS_KEY = "significantDigits";

    // The defaults if there is nothing in the preferences
    public static String DEFAULT_FONT_NAME = "sansserif";
    public static int DEFAULT_FONT_SIZE = 8;
    public static int DEFAULT_FONT_STYLE = Font.PLAIN;

    public static String DEFAULT_NUMBER_FORMATTING = "#.####";

    public static String DECIMAL_NUMBER_FORMATTING = "#.####";
    public static String SCIENTIFIC_NUMBER_FORMATTING = "0.###E0";

    public LabelPainterController(String title, String key, final LabelPainter labelPainter, final JFrame frame) {

        this.title = title;
        this.key = key;
        this.labelPainter = labelPainter;

        userLabelDecorator = new AttributableDecorator();
        userLabelDecorator.setPaintAttributeName("!color");
        userLabelDecorator.setFontAttributeName("!font");
        labelPainter.setTextDecorator(userLabelDecorator);

        final String defaultFontName = PREFS.get(key + "." + FONT_NAME_KEY, DEFAULT_FONT_NAME);
        final int defaultFontStyle = PREFS.getInt(key + "." + FONT_STYLE_KEY, DEFAULT_FONT_STYLE);
        final int defaultFontSize = PREFS.getInt(key + "." + FONT_SIZE_KEY, DEFAULT_FONT_SIZE);
        final String defaultNumberFormatting = PREFS.get(key + "." + NUMBER_FORMATTING_KEY, DEFAULT_NUMBER_FORMATTING);

        labelPainter.setFont(new Font(defaultFontName, defaultFontStyle, defaultFontSize));
        labelPainter.setNumberFormat(new DecimalFormat(defaultNumberFormatting));

        optionsPanel = new ControllerOptionsPanel(2, 2);

        titleCheckBox = new JCheckBox(getTitle());

        titleCheckBox.setSelected(labelPainter.isVisible());

        String[] attributes = labelPainter.getAttributes();
        displayAttributeCombo = new JComboBox(attributes);
        displayAttributeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String attribute = (String) displayAttributeCombo.getSelectedItem();
                labelPainter.setDisplayAttribute(attribute);
            }
        });

        colourAttributeCombo = new JComboBox(new String[] { USER_SELECTION });
        colourAttributeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                setupLabelDecorator();
            }
        });

        final JLabel label1 = optionsPanel.addComponentWithLabel("Display:", displayAttributeCombo);

        final JButton setupColourButton = new JButton("Colour");

        ColourControllerHelper helper = new ColourControllerHelper(frame, userLabelDecorator, colourAttributeCombo, setupColourButton);

//        colourSettings.autoRange = true;
//        colourSettings.fromValue = 0.0;
//        colourSettings.toValue = 1.0;
//        colourSettings.fromColour = new Color(0, 16, 192);
//        colourSettings.toColour = new Color(192, 16, 0);
//        colourSettings.middleColour = new Color(0, 0, 0);
//
//        final JButton setupColourButton = new JButton(new AbstractAction("Colour") {
//            public void actionPerformed(ActionEvent e) {
//                Decorator decorator = null;
//                if (colourAttributeCombo.getSelectedIndex() > 0) {
//                    String attribute = (String) colourAttributeCombo.getSelectedItem();
//                    decorator = labelPainter.getColourDecoratorForAttribute(attribute);
//                }
//
//                if (decorator instanceof HSBDiscreteColorDecorator) {
//                    if (discreteColourScaleDialog == null) {
//                        discreteColourScaleDialog = new DiscreteColourScaleDialog(frame);
//                    }
//                    discreteColourScaleDialog.setDecorator((HSBDiscreteColorDecorator)decorator);
//                    int result = discreteColourScaleDialog.showDialog();
//                    if (result != JOptionPane.CANCEL_OPTION && result != JOptionPane.CLOSED_OPTION) {
//                        discreteColourScaleDialog.setupDecorator((HSBDiscreteColorDecorator)decorator);
//                        setupLabelDecorator();
//                    }
//                } else  if (decorator instanceof HSBContinuousColorDecorator) {
//                    if (continuousColourScaleDialog == null) {
//                        continuousColourScaleDialog = new ContinuousColourScaleDialog(frame);
//                    }
//                    continuousColourScaleDialog.setDecorator((HSBContinuousColorDecorator)decorator);
//                    int result = continuousColourScaleDialog.showDialog();
//                    if (result != JOptionPane.CANCEL_OPTION && result != JOptionPane.CLOSED_OPTION) {
//                        continuousColourScaleDialog.setupDecorator((HSBContinuousColorDecorator)decorator);
//                        setupLabelDecorator();
//                    }
//                } else {
//                    throw new IllegalArgumentException("Unsupported decorator type");
////                    if (continuousColourScaleDialog == null) {
////                        continuousColourScaleDialog = new OldContinuousColourScaleDialog(frame, colourSettings);
////                    }
////                    int result = continuousColourScaleDialog.showDialog();
////                    if (result != JOptionPane.CANCEL_OPTION && result != JOptionPane.CLOSED_OPTION) {
////                        continuousColourScaleDialog.getSettings(colourSettings);
////                        setupLabelDecorator();
////                    }
//                }
//
//            }
//        });

        final JLabel label2 = optionsPanel.addComponentWithLabel("Colour by:", colourAttributeCombo);

        final JButton fontButton = new JButton(new AbstractAction("Font") {
            public void actionPerformed(ActionEvent e) {
                final Font font = labelPainter.getFont();
                if (fontDialog == null) {
                    fontDialog = new FontDialog(frame);
                }
                int result = fontDialog.showDialog(font);
                if (result != JOptionPane.CANCEL_OPTION) {
                    labelPainter.setFont(fontDialog.getFont());
                }
            }
        });

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        ControllerOptionsPanel.setComponentLook(setupColourButton);
        ControllerOptionsPanel.setComponentLook(fontButton);
        panel.add(setupColourButton);
        panel.add(fontButton);
        final JLabel label3 = optionsPanel.addComponentWithLabel("Setup:", panel);

        NumberFormat format = labelPainter.getNumberFormat();
        int digits = format.getMaximumFractionDigits();

        numericalFormatCombo = new JComboBox(new String[] { "Decimal", "Scientific", "Percent", "Roman"});
        numericalFormatCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String formatType = (String) numericalFormatCombo.getSelectedItem();
                final int digits = (Integer) digitsSpinner.getValue();
                NumberFormat format = null;
                if (formatType.equals("Decimal")) {
                    format = new DecimalFormat(DECIMAL_NUMBER_FORMATTING);
                } else if (formatType.equals("Scientific")) {
                    format = new DecimalFormat(SCIENTIFIC_NUMBER_FORMATTING);
                } else if (formatType.equals("Percent")) {
                    format = new PercentFormat();
                } else if (formatType.equals("Roman")) {
                    format = new Roman();
                }
                format.setMaximumFractionDigits(digits);
                labelPainter.setNumberFormat(format);
            }
        });

        final JLabel label4 = optionsPanel.addComponentWithLabel("Format:", numericalFormatCombo);

        digitsSpinner = new JSpinner(new SpinnerNumberModel(digits, 2, 14, 1));

        final JLabel label5 = optionsPanel.addComponentWithLabel("Sig. Digits:", digitsSpinner);

        digitsSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final int digits = (Integer)digitsSpinner.getValue();
                NumberFormat format = labelPainter.getNumberFormat();
                format.setMaximumFractionDigits(digits);
                labelPainter.setNumberFormat(format);
            }
        });

        labelPainter.addPainterListener(new PainterListener() {
            public void painterChanged() {

            }
            public void painterSettingsChanged() {
            }
            public void attributesChanged() {
                setupAttributes();
            }
        });

        final boolean isSelected = titleCheckBox.isSelected();
        label1.setEnabled(isSelected);
        displayAttributeCombo.setEnabled(isSelected);
        label2.setEnabled(isSelected);
        colourAttributeCombo.setEnabled(isSelected);
        label3.setEnabled(isSelected);
        setupColourButton.setEnabled(isSelected);
        fontButton.setEnabled(isSelected);
        label4.setEnabled(isSelected);
        numericalFormatCombo.setEnabled(isSelected);
        label5.setEnabled(isSelected);
        digitsSpinner.setEnabled(isSelected);

        titleCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final boolean isSelected = titleCheckBox.isSelected();
                label1.setEnabled(isSelected);
                displayAttributeCombo.setEnabled(isSelected);
                label2.setEnabled(isSelected);
                colourAttributeCombo.setEnabled(isSelected);
                label3.setEnabled(isSelected);
                setupColourButton.setEnabled(isSelected);
                fontButton.setEnabled(isSelected);
                label4.setEnabled(isSelected);
                numericalFormatCombo.setEnabled(isSelected);
                label5.setEnabled(isSelected);
                digitsSpinner.setEnabled(isSelected);
                labelPainter.setVisible(isSelected);
            }
        });

    }

    private void setupAttributes() {
        Object item1 = displayAttributeCombo.getSelectedItem();
        displayAttributeCombo.removeAllItems();
        for (String name : labelPainter.getAttributes()) {
            displayAttributeCombo.addItem(name);
        }
        displayAttributeCombo.setSelectedItem(item1);

        String[] names = getAttributeNames(labelPainter.getAttributableItems());
        Object item2 = colourAttributeCombo.getSelectedItem();
        colourAttributeCombo.removeAllItems();
        colourAttributeCombo.addItem(USER_SELECTION);
        for (String name : names) {
            colourAttributeCombo.addItem(name);
        }
        colourAttributeCombo.setSelectedItem(item2);

        optionsPanel.repaint();
    }

    private String[] getAttributeNames(Collection<? extends Attributable> items) {
        java.util.Set<String> attributeNames = new TreeSet<String>();

        for (Attributable item : items) {
            for (String name : item.getAttributeNames()) {
                if (!name.startsWith("!")) {
                    Object attr = item.getAttribute(name);
                    if (!(attr instanceof Object[])) {
                        attributeNames.add(name);
                    } else {
                        boolean isColouring = true;

                        Object[] array = (Object[])attr;
                        boolean isIndex = true;
                        for (Object element : array) {
                            if (isIndex && !(element instanceof Integer) ||
                                    !isIndex && !(element instanceof Double)) {
                                isColouring = false;
                                break;
                            }
                            isIndex = !isIndex;
                        }

                        if (isIndex) {
                            // a colouring should finish on an index (which means isIndex should be false)...
                            isColouring = false;
                        }

                        if (isColouring) {
                            attributeNames.add(name + " *");
                        }

                    }
                }
            }
        }

        String[] attributeNameArray = new String[attributeNames.size()];
        attributeNames.toArray(attributeNameArray);

        return attributeNameArray;
    }

    private void setupLabelDecorator() {

        Decorator textDecorator = null;
        String attribute = (String)colourAttributeCombo.getSelectedItem();

        textDecorator = userLabelDecorator;
        Decorator colourDecorator = null;

        if (attribute != null && attribute.length() > 0) {
            if (!attribute.equalsIgnoreCase(USER_SELECTION)) {
                colourDecorator = labelPainter.getColourDecoratorForAttribute(attribute);
                if (colourDecorator == null) {
                    Set<Attributable> items = labelPainter.getAttributableItems();

                    if (DiscreteColorDecorator.isDiscrete(attribute, items)) {
                        colourDecorator = new HSBDiscreteColorDecorator(attribute, items, false);
                    } else {
                        ContinuousScale scale;
                        if (colourSettings.autoRange) {
                            scale = new ContinuousScale(attribute, items);
                        } else {
                            scale = new ContinuousScale(attribute, items, colourSettings.fromValue, colourSettings.toValue);
                        }

                        colourDecorator = new HSBContinuousColorDecorator(scale, false);
//                        if (colourSettings.middleColour == null) {
//                            colourDecorator = new ContinuousColorDecorator(scale, colourSettings.fromColour, colourSettings.toColour, false);
//                        } else {
//                            colourDecorator = new ContinuousColorDecorator(scale, colourSettings.fromColour, colourSettings.middleColour, colourSettings.toColour, false);
//                        }
                    }
                }

                CompoundDecorator compoundDecorator = new CompoundDecorator();
                compoundDecorator.addDecorator(colourDecorator);
                AttributableDecorator fontDecorator = new AttributableDecorator();
                userLabelDecorator.setFontAttributeName("!font");
                compoundDecorator.addDecorator(fontDecorator);

                textDecorator = compoundDecorator;

                labelPainter.setColourDecoratorForAttribute(attribute, colourDecorator);

            }
        }

        labelPainter.setTextDecorator(textDecorator);
    }

    public JComponent getTitleComponent() {
        return titleCheckBox;
    }

    public JPanel getPanel() {
        return optionsPanel;
    }

    public boolean isInitiallyVisible() {
        return false;
    }

    public void initialize() {
        // nothing to do
    }

    public void setSettings(Map<String,Object> settings) {
        titleCheckBox.setSelected((Boolean) settings.get(key + "." + IS_SHOWN));
        displayAttributeCombo.setSelectedItem(settings.get(key + "." + DISPLAY_ATTRIBUTE_KEY));
        colourAttributeCombo.setSelectedItem(settings.get(key + "." + COLOR_ATTRIBUTE_KEY));
        String name = (String)settings.get(key + "." + FONT_NAME_KEY);
        int size = ((Number)settings.get(key + "." + FONT_SIZE_KEY)).intValue();
        int style = (Integer)settings.get(key + "." + FONT_STYLE_KEY);
        labelPainter.setFont(new Font(name, style, size));
        digitsSpinner.setValue((Integer) settings.get(key + "." + SIGNIFICANT_DIGITS_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(key+"."+IS_SHOWN, titleCheckBox.isSelected());
        settings.put(key+"."+DISPLAY_ATTRIBUTE_KEY, displayAttributeCombo.getSelectedItem().toString());
        settings.put(key+"."+COLOR_ATTRIBUTE_KEY, colourAttributeCombo.getSelectedItem().toString());
        Font font = labelPainter.getFont();
        settings.put(key+"."+FONT_NAME_KEY, font.getName());
        settings.put(key+"."+FONT_SIZE_KEY, font.getSize());
        settings.put(key+"."+FONT_STYLE_KEY, font.getStyle());
        settings.put(key+"."+SIGNIFICANT_DIGITS_KEY, digitsSpinner.getValue());
    }

    public String getTitle() {
        return title;
    }


    private final JCheckBox titleCheckBox;
    private final OptionsPanel optionsPanel;

    private final JComboBox displayAttributeCombo;
    //    private final JSpinner fontSizeSpinner;
    private FontDialog fontDialog = null;

    private final JComboBox numericalFormatCombo;
    private final JSpinner digitsSpinner;

    private final String title;
    private final String key;

    private final LabelPainter labelPainter;

    private final AttributableDecorator userLabelDecorator;

    private final JComboBox colourAttributeCombo;

    private ContinuousColourScaleDialog continuousColourScaleDialog = null;
    private DiscreteColourScaleDialog discreteColourScaleDialog = null;

    OldContinuousColourScaleDialog.ColourSettings colourSettings = new OldContinuousColourScaleDialog.ColourSettings();

}
