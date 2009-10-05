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
import jebl.evolution.graphs.Node;
import jebl.evolution.trees.Tree;
import jebl.util.Attributable;

/**
 * @author Andrew Rambaut
 * @version $Id: LabelPainterController.java 773 2007-09-04 23:51:07Z rambaut $
 */
public class LabelPainterController extends AbstractController {

    public static Preferences PREFS = Preferences.userNodeForPackage(TreeViewer.class);

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

        userLabelColourDecorator = new AttributableDecorator();
        userLabelColourDecorator.setPaintAttributeName("!color");
        userLabelColourDecorator.setStrokeAttributeName("!stroke");

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
        displayAttributeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                String attribute = (String)displayAttributeCombo.getSelectedItem();
                labelPainter.setDisplayAttribute(attribute);
            }
        });

        colourAttributeCombo = new JComboBox(attributes);
        colourAttributeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                String attribute = (String)colourAttributeCombo.getSelectedItem();
                labelPainter.setColourAttribute(attribute);
            }
        });

        final JLabel label1 = optionsPanel.addComponentWithLabel("Display:", displayAttributeCombo);

//        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(font.getSize(), 0.01, 48, 1));
//
//        final JLabel label2 = optionsPanel.addComponentWithLabel("Font Size:", fontSizeSpinner);
//
//        fontSizeSpinner.addChangeListener(new ChangeListener() {
//            public void stateChanged(ChangeEvent changeEvent) {
//                final float size = ((Double) fontSizeSpinner.getValue()).floatValue();
//                Font font = labelPainter.getFont().deriveFont(size);
//                labelPainter.setFont(font);
//            }
//        });


        colourAutoRange = true;
        colourFromValue = 0.0;
        colourToValue = 1.0;
        fromColour = new Color(0, 16, 192);
        toColour = new Color(192, 16, 0);
        middleColour = new Color(0, 0, 0);

        JButton setupColourButton = new JButton(new AbstractAction("Setup") {
            public void actionPerformed(ActionEvent e) {
                if (colourScaleDialog == null) {
                    colourScaleDialog = new ColourScaleDialog(frame, colourAutoRange,
                            colourFromValue, colourToValue,
                            fromColour, toColour, middleColour);
                }
                int result = colourScaleDialog.showDialog();
                if (result != JOptionPane.CANCEL_OPTION && result != JOptionPane.CLOSED_OPTION) {
                    colourAutoRange = colourScaleDialog.getAutoRange();
                    colourFromValue = colourScaleDialog.getFromValue().doubleValue();
                    colourToValue = colourScaleDialog.getToValue().doubleValue();
                    fromColour = colourScaleDialog.getFromColour();
                    toColour = colourScaleDialog.getToColour();
                    middleColour = colourScaleDialog.getMiddleColour();
//                    setupLabelDecorators();
                }
            }
        });

        optionsPanel.addComponentWithLabel("Colour by:", colourAttributeCombo);
        optionsPanel.addComponent(setupColourButton);
        optionsPanel.addSeparator();

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
        optionsPanel.addComponent(fontButton);

        NumberFormat format = labelPainter.getNumberFormat();
        int digits = format.getMaximumFractionDigits();

        numericalFormatCombo = new JComboBox(new String[] { "Decimal", "Scientific", "Percent", "Roman"});
        numericalFormatCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                String formatType = (String)numericalFormatCombo.getSelectedItem();
                final int digits = (Integer)digitsSpinner.getValue();
                NumberFormat format = null;
                if (formatType.equals("Decimal")) {
                    format = new DecimalFormat(DECIMAL_NUMBER_FORMATTING);
                } else if (formatType.equals("Scientific")) {
                    format = new DecimalFormat(SCIENTIFIC_NUMBER_FORMATTING);
                }  else if (formatType.equals("Percent")) {
                    format = new PercentFormat();
                } else if (formatType.equals("Roman")) {
                    format = new Roman();
                }
                format.setMaximumFractionDigits(digits);
                labelPainter.setNumberFormat(format);
            }
        });

        final JLabel label3 = optionsPanel.addComponentWithLabel("Format:", numericalFormatCombo);

        digitsSpinner = new JSpinner(new SpinnerNumberModel(digits, 2, 14, 1));

        final JLabel label4 = optionsPanel.addComponentWithLabel("Sig. Digits:", digitsSpinner);

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
                Object item1 = displayAttributeCombo.getSelectedItem();
                Object item2 = colourAttributeCombo.getSelectedItem();
                displayAttributeCombo.removeAllItems();
                colourAttributeCombo.removeAllItems();
                colourAttributeCombo.addItem("User Selection");
                for (String name : labelPainter.getAttributes()) {
                    displayAttributeCombo.addItem(name);
                    colourAttributeCombo.addItem(name);
                }
                displayAttributeCombo.setSelectedItem(item1);
                colourAttributeCombo.setSelectedItem(item2);

                optionsPanel.repaint();
            }
        });

        final boolean isSelected = titleCheckBox.isSelected();
        label1.setEnabled(isSelected);
        displayAttributeCombo.setEnabled(isSelected);
//        label2.setEnabled(isSelected);
//        fontSizeSpinner.setEnabled(isSelected);
        fontButton.setEnabled(isSelected);
        label3.setEnabled(isSelected);
        numericalFormatCombo.setEnabled(isSelected);
        label4.setEnabled(isSelected);
        digitsSpinner.setEnabled(isSelected);

        titleCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final boolean isSelected = titleCheckBox.isSelected();
                label1.setEnabled(isSelected);
                displayAttributeCombo.setEnabled(isSelected);
//                label2.setEnabled(isSelected);
//                fontSizeSpinner.setEnabled(isSelected);
                fontButton.setEnabled(isSelected);
                label3.setEnabled(isSelected);
                numericalFormatCombo.setEnabled(isSelected);
                label4.setEnabled(isSelected);
                digitsSpinner.setEnabled(isSelected);
                labelPainter.setVisible(isSelected);
            }
        });

    }

//    private void setupLabelDecorators() {
//
//        Set<Node> nodes = new HashSet<Node>();
//        for (Tree tree : treeViewer.getTrees()) {
//            for (Node node : tree.getNodes()) {
//                nodes.add(node);
//            }
//        }
//
//        Decorator colourDecorator = null;
//
//        if (branchColourAttributeCombo.getSelectedIndex() == 0) {
//            colourDecorator = userBranchColourDecorator;
//            userBranchColourDecorator.setGradient(useGradient);
//        } else {
//            String attribute = (String) branchColourAttributeCombo.getSelectedItem();
//            if (attribute != null && attribute.length() > 0) {
//                if (attribute.endsWith("*")) {
//                    // This is a branch colouring (i.e., the colour can change
//                    // along the length of the branch...
//                    treeViewer.setBranchColouringDecorator(
//                            attribute.substring(0, attribute.length() - 2),
//                            new DiscreteColorDecorator());
//                    return;
//                } else if (DiscreteColorDecorator.isDiscrete(attribute, nodes)) {
//
//                    colourDecorator = new DiscreteColorDecorator(attribute, nodes, useGradient);
//                } else {
//                    ContinousScale scale;
//                    if (colourAutoRange) {
//                        scale = new ContinousScale(attribute, nodes);
//                    } else {
//                        scale = new ContinousScale(attribute, nodes, colourFromValue, colourToValue);
//                    }
//
//                    if (middleColour == null) {
//                        colourDecorator = new ContinuousColorDecorator(scale, fromColour, toColour, useGradient);
//                    } else {
//                        colourDecorator = new ContinuousColorDecorator(scale, fromColour, middleColour, toColour, useGradient);
//                    }
//
//                }
//            }
//        }
//
//        if (colourDecorator != null && colourDecorator.isGradient()) {
//            // At present using a gradient precludes the use of the compoundDecorator
//            // and thus the branch width..
//            treeViewer.setBranchDecorator(colourDecorator);
//            return;
//        }
//
//
//
//        CompoundDecorator compoundDecorator = new CompoundDecorator();
//
//        if (colourDecorator != null) {
//            treeViewer.setBranchColouringDecorator(null, null);
//            compoundDecorator.addDecorator(colourDecorator);
//        }
//
//        if (branchWidthAttributeCombo.getSelectedIndex() > 0) {
//            String attribute = (String) branchWidthAttributeCombo.getSelectedItem();
//            if (attribute != null && attribute.length() > 0) {
//                if (!DiscreteColorDecorator.isDiscrete(attribute, nodes)) {
//                    ContinousScale scale;
//                    if (widthAutoRange) {
//                        scale = new ContinousScale(attribute, nodes);
//                    } else {
//                        scale = new ContinousScale(attribute, nodes, widthFromValue, widthToValue);
//                    }
//                    compoundDecorator.addDecorator(new ContinuousStrokeDecorator(
//                            scale, (float)fromWidth, (float)toWidth)
//                    );
//
//                }
//            }
//        }
//        treeViewer.setBranchDecorator(compoundDecorator);
//
//    }

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
	    titleCheckBox.setSelected((Boolean)settings.get(key+"."+IS_SHOWN));
        displayAttributeCombo.setSelectedItem(settings.get(key+"."+DISPLAY_ATTRIBUTE_KEY));
        colourAttributeCombo.setSelectedItem(settings.get(key+"."+COLOR_ATTRIBUTE_KEY));
        //fontSizeSpinner.setValue((Double)settings.get(key+"."+FONT_SIZE_KEY));
        String name = (String)settings.get(key + "." + FONT_NAME_KEY);
        int size = ((Number)settings.get(key + "." + FONT_SIZE_KEY)).intValue();
        int style = (Integer)settings.get(key + "." + FONT_STYLE_KEY);
        labelPainter.setFont(new Font(name, style, size));
        digitsSpinner.setValue((Integer)settings.get(key+"."+SIGNIFICANT_DIGITS_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
	    settings.put(key+"."+IS_SHOWN, titleCheckBox.isSelected());
        settings.put(key+"."+DISPLAY_ATTRIBUTE_KEY, displayAttributeCombo.getSelectedItem().toString());
        settings.put(key+"."+COLOR_ATTRIBUTE_KEY, colourAttributeCombo.getSelectedItem().toString());
        //settings.put(key+"."+FONT_SIZE_KEY, fontSizeSpinner.getValue());
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

    private final AttributableDecorator userLabelColourDecorator;

    private final JComboBox colourAttributeCombo;

    private ColourScaleDialog colourScaleDialog = null;

    private boolean colourAutoRange = true;
    private double colourFromValue = 0.0;
    private double colourToValue = 1.0;
    private Color fromColour;
    private Color toColour;
    private Color middleColour;

}
