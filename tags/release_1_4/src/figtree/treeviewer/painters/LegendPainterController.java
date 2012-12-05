/*
 * LegendPainterController.java
 *
 * Copyright (C) 2012 Andrew Rambaut
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

package figtree.treeviewer.painters;

import figtree.treeviewer.AttributeColourController;
import figtree.treeviewer.ControllerOptionsPanel;
import figtree.treeviewer.TreeViewer;
import figtree.treeviewer.decorators.ColourDecorator;
import figtree.treeviewer.decorators.Decorator;
import figtree.ui.components.RealNumberField;
import jam.controlpalettes.AbstractController;
import jam.controlpalettes.ControllerListener;
import jam.panels.OptionsPanel;
import jebl.util.Attributable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * @author Andrew Rambaut
 * @version $Id: ScaleBarPainterController.java 760 2007-08-21 00:05:45Z rambaut $
 */
public class LegendPainterController extends AbstractController {

    public static Preferences PREFS = Preferences.userNodeForPackage(TreeViewer.class);

    public static final String CONTROLLER_KEY = "legend";

    public static final String FONT_NAME_KEY = "fontName";
    public static final String FONT_SIZE_KEY = "fontSize";
    public static final String FONT_STYLE_KEY = "fontStyle";

    public static final String ATTRIBUTE_KEY = "attribute";

    public static final String NUMBER_FORMATTING_KEY = "numberFormatting";
    public static final String SIGNIFICANT_DIGITS_KEY = "significantDigits";

    // The defaults if there is nothing in the preferences
    public static String DEFAULT_FONT_NAME = "sansserif";
    public static int DEFAULT_FONT_SIZE = 10;
    public static int DEFAULT_FONT_STYLE = Font.PLAIN;

    public static String DEFAULT_ATTRIBUTE_KEY = "";

    public static String DECIMAL_NUMBER_FORMATTING = "#.####";
    public static String SCIENTIFIC_NUMBER_FORMATTING = "0.###E0";

    public static int DEFAULT_SIGNIFICANT_DIGITS = 2;
    public static String DEFAULT_NUMBER_FORMATTING = DECIMAL_NUMBER_FORMATTING;

    public LegendPainterController(final LegendPainter legendPainter,
                                   final AttributeColourController colourController,
                                   final TreeViewer treeViewer) {
        final String defaultFontName = PREFS.get(CONTROLLER_KEY + "." + FONT_NAME_KEY, DEFAULT_FONT_NAME);
        final int defaultFontStyle = PREFS.getInt(CONTROLLER_KEY + "." + FONT_STYLE_KEY, DEFAULT_FONT_STYLE);
        final int defaultFontSize = PREFS.getInt(CONTROLLER_KEY + "." + FONT_SIZE_KEY, DEFAULT_FONT_SIZE);
        final int defaultSignificantDigits = PREFS.getInt(CONTROLLER_KEY + "." + SIGNIFICANT_DIGITS_KEY, DEFAULT_SIGNIFICANT_DIGITS);
        final String defaultNumberFormatting = PREFS.get(CONTROLLER_KEY + "." + NUMBER_FORMATTING_KEY, DEFAULT_NUMBER_FORMATTING);

        final String attribute = PREFS.get(CONTROLLER_KEY + "." + ATTRIBUTE_KEY, DEFAULT_ATTRIBUTE_KEY);

        legendPainter.setFont(new Font(defaultFontName, defaultFontStyle, defaultFontSize));
//        legendPainter.setDisplayAttribute(attribute);
        legendPainter.setNumberFormat(new DecimalFormat(defaultNumberFormatting));

        optionsPanel = new ControllerOptionsPanel(2, 2);

        titleCheckBox = new JCheckBox(getTitle());
        titleCheckBox.setSelected(legendPainter.isVisible());

        attributeCombo = new JComboBox();
        new AttributeComboHelper(attributeCombo, treeViewer).addListener(new AttributeComboHelperListener() {
            @Override
            public void attributeComboChanged() {
                ColourDecorator colourDecorator = (ColourDecorator)colourController.getColourDecorator(attributeCombo, null);
                legendPainter.setColourDecorator(colourDecorator);
            }
        });

        colourController.setupControls(attributeCombo, null);
        colourController.addControllerListener(new ControllerListener() {
            @Override
            public void controlsChanged() {
                ColourDecorator colourDecorator = (ColourDecorator)colourController.getColourDecorator(attributeCombo, null);
                legendPainter.setColourDecorator(colourDecorator);
            }
        });

//        attributeCombo.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent event) {
//                legendPainter.setDisplayAttribute((String) attributeCombo.getSelectedItem());
//            }
//        });

        Font font = legendPainter.getFont();
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(font.getSize(), 0.01, 48, 1));

        fontSizeSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final float size = ((Double) fontSizeSpinner.getValue()).floatValue();
                Font font = legendPainter.getFont().deriveFont(size);
                legendPainter.setFont(font);
            }
        });

        NumberFormat format = legendPainter.getNumberFormat();
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
                legendPainter.setNumberFormat(format);
            }
        });

        digitsSpinner = new JSpinner(new SpinnerNumberModel(digits, defaultSignificantDigits, 14, 1));
        digitsSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final int digits = (Integer)digitsSpinner.getValue();
                NumberFormat format = legendPainter.getNumberFormat();
                format.setMaximumFractionDigits(digits);
                legendPainter.setNumberFormat(format);
            }
        });

        final JLabel label1 = optionsPanel.addComponentWithLabel("Attribute:", attributeCombo);
        final JLabel label2 = optionsPanel.addComponentWithLabel("Font Size:", fontSizeSpinner);
        final JLabel label3 = optionsPanel.addComponentWithLabel("Format:", numericalFormatCombo);
        final JLabel label4 = optionsPanel.addComponentWithLabel("Sig. Digits:", digitsSpinner);

        addComponent(label1);
        addComponent(attributeCombo);
        addComponent(label2);
        addComponent(fontSizeSpinner);
        addComponent(label3);
        addComponent(numericalFormatCombo);
        addComponent(label4);
        addComponent(digitsSpinner);
        enableComponents(titleCheckBox.isSelected());

        titleCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                enableComponents(titleCheckBox.isSelected());
                legendPainter.setVisible(titleCheckBox.isSelected());
            }
        });

//        autoScaleCheck.addActionListener(new ActionListener() {
//        @Override
//        public void actionPerformed(ActionEvent actionEvent) {
//            public void stateChanged(ChangeEvent changeEvent) {
//                if (autoScaleCheck.isSelected()) {
//                    scaleBarPainter.setAutomaticScale(true);
//                    double range = scaleBarPainter.getScaleRange();
//                    scaleRangeText.setValue(range);
//                    label1.setEnabled(false);
//                    scaleRangeText.setEnabled(false);
//                } else {
//                    label1.setEnabled(true);
//                    scaleRangeText.setEnabled(true);
//                    scaleBarPainter.setAutomaticScale(false);
//                }
//            }
//        });

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
    }

    public void setSettings(Map<String,Object> settings) {
        titleCheckBox.setSelected((Boolean) settings.get(CONTROLLER_KEY + "." + IS_SHOWN));
        attributeCombo.setSelectedItem((String) settings.get(CONTROLLER_KEY + "." + ATTRIBUTE_KEY));
        fontSizeSpinner.setValue((Double)settings.get(CONTROLLER_KEY + "." + FONT_SIZE_KEY));
        digitsSpinner.setValue((Integer) settings.get(CONTROLLER_KEY + "." + SIGNIFICANT_DIGITS_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(CONTROLLER_KEY + "." + IS_SHOWN, titleCheckBox.isSelected());
        settings.put(CONTROLLER_KEY + "." + ATTRIBUTE_KEY, attributeCombo.getSelectedItem());
        settings.put(CONTROLLER_KEY + "." + FONT_SIZE_KEY, fontSizeSpinner.getValue());
        settings.put(CONTROLLER_KEY + "." + SIGNIFICANT_DIGITS_KEY, digitsSpinner.getValue());
    }

    private final JCheckBox titleCheckBox;
    private final OptionsPanel optionsPanel;

    private final JComboBox attributeCombo;
    private final JSpinner fontSizeSpinner;

    private final JComboBox numericalFormatCombo;
    private final JSpinner digitsSpinner;

    public String getTitle() {
        return "Legend";
    }
}
