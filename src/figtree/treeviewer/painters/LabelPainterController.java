package figtree.treeviewer.painters;

import org.virion.jam.controlpalettes.AbstractController;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.prefs.Preferences;

import figtree.treeviewer.ControllerOptionsPanel;
import figtree.treeviewer.TreeViewer;

/**
 * @author Andrew Rambaut
 * @version $Id: LabelPainterController.java 773 2007-09-04 23:51:07Z rambaut $
 */
public class LabelPainterController extends AbstractController {

    public static Preferences PREFS = Preferences.userNodeForPackage(TreeViewer.class);

    public static final String FONT_NAME_KEY = "fontName";
    public static final String FONT_SIZE_KEY = "fontSize";
    public static final String FONT_STYLE_KEY = "fontStyle";

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

    public LabelPainterController(String title, String key, final LabelPainter labelPainter) {

        this.title = title;
        this.key = key;
        this.labelPainter = labelPainter;

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

        final JLabel label1 = optionsPanel.addComponentWithLabel("Display:", displayAttributeCombo);

        Font font = labelPainter.getFont();
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(font.getSize(), 0.01, 48, 1));

        final JLabel label2 = optionsPanel.addComponentWithLabel("Font Size:", fontSizeSpinner);

        fontSizeSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final float size = ((Double) fontSizeSpinner.getValue()).floatValue();
                Font font = labelPainter.getFont().deriveFont(size);
                labelPainter.setFont(font);
            }
        });

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
                Object item = displayAttributeCombo.getSelectedItem();
                displayAttributeCombo.removeAllItems();
                for (String name : labelPainter.getAttributes()) {
                    displayAttributeCombo.addItem(name);
                }
                displayAttributeCombo.setSelectedItem(item);

                optionsPanel.repaint();
            }
        });

        final boolean isSelected = titleCheckBox.isSelected();
        label1.setEnabled(isSelected);
        displayAttributeCombo.setEnabled(isSelected);
        label2.setEnabled(isSelected);
        fontSizeSpinner.setEnabled(isSelected);
        label3.setEnabled(isSelected);
        numericalFormatCombo.setEnabled(isSelected);
        label4.setEnabled(isSelected);
        digitsSpinner.setEnabled(isSelected);

        titleCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final boolean isSelected = titleCheckBox.isSelected();
                label1.setEnabled(isSelected);
                displayAttributeCombo.setEnabled(isSelected);
                label2.setEnabled(isSelected);
                fontSizeSpinner.setEnabled(isSelected);
                label3.setEnabled(isSelected);
                numericalFormatCombo.setEnabled(isSelected);
                label4.setEnabled(isSelected);
                digitsSpinner.setEnabled(isSelected);
                labelPainter.setVisible(isSelected);
            }
        });

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
        fontSizeSpinner.setValue((Double)settings.get(key+"."+FONT_SIZE_KEY));
        digitsSpinner.setValue((Integer)settings.get(key+"."+SIGNIFICANT_DIGITS_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
	    settings.put(key+"."+IS_SHOWN, titleCheckBox.isSelected());
        settings.put(key+"."+DISPLAY_ATTRIBUTE_KEY, displayAttributeCombo.getSelectedItem().toString());
        settings.put(key+"."+FONT_SIZE_KEY, fontSizeSpinner.getValue());
        settings.put(key+"."+SIGNIFICANT_DIGITS_KEY, digitsSpinner.getValue());
    }

    public String getTitle() {
        return title;
    }

    private final JCheckBox titleCheckBox;
    private final OptionsPanel optionsPanel;

    private final JComboBox displayAttributeCombo;
    private final JSpinner fontSizeSpinner;

    private final JComboBox numericalFormatCombo;
    private final JSpinner digitsSpinner;

    private final String title;
    private final String key;

    private final LabelPainter labelPainter;
}
