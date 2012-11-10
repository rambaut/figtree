/*
 * ScaleBarPainterController.java
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

package figtree.panel;

import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.Map;

import figtree.treeviewer.painters.ScaleBarPainter;
import figtree.ui.components.RealNumberField;

/**
 * @author Andrew Rambaut
 * @version $Id: ScaleBarPainterController.java,v 1.1 2007/08/14 15:50:41 rambaut Exp $
 */
public class ScaleBarPainterController extends AbstractController {

    private static final String SCALE_BAR_KEY = "scaleBar";

    private static final String FONT_NAME_KEY = "fontName";
    private static final String FONT_SIZE_KEY = "fontSize";
    private static final String FONT_STYLE_KEY = "fontStyle";

    private static final String NUMBER_FORMATTING_KEY = "numberFormatting";


    private static final String AUTOMATIC_SCALE_KEY = "automaticScale";
    private static final String SCALE_RANGE_KEY = "scaleRange";
    private static final String LINE_WIDTH_KEY = "lineWidth";

    private static final String SIGNIFICANT_DIGITS_KEY = "significantDigits";

    // The defaults if there is nothing in the preferences
    private static String DEFAULT_FONT_NAME = "sansserif";
    private static int DEFAULT_FONT_SIZE = 6;
    private static int DEFAULT_FONT_STYLE = Font.PLAIN;

    private static String DEFAULT_NUMBER_FORMATTING = "#.####";
    private static float DEFAULT_LINE_WIDTH = 1.0f;

    public ScaleBarPainterController(final ScaleBarPainter scaleBarPainter) {
        this.scaleBarPainter = scaleBarPainter;

        final String defaultFontName = DEFAULT_FONT_NAME;
        final int defaultFontStyle = DEFAULT_FONT_STYLE;
        final int defaultFontSize = DEFAULT_FONT_SIZE;
        final String defaultNumberFormatting = DEFAULT_NUMBER_FORMATTING;

        float lineWidth = DEFAULT_LINE_WIDTH;

        scaleBarPainter.setFont(new Font(defaultFontName, defaultFontStyle, defaultFontSize));
        scaleBarPainter.setNumberFormat(new DecimalFormat(defaultNumberFormatting));
        scaleBarPainter.setScaleBarStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        optionsPanel = new OptionsPanel();

        titleCheckBox = new JCheckBox(getTitle());

        titleCheckBox.setSelected(scaleBarPainter.isVisible());

        autoScaleCheck = new JCheckBox("Automatic scale");
        autoScaleCheck.setSelected(true);
        optionsPanel.addComponent(autoScaleCheck, true);

        scaleRangeText = new RealNumberField(0.0, Double.MAX_VALUE);
        scaleRangeText.setValue(0.0);

        final JLabel label1 = optionsPanel.addComponentWithLabel("Scale Range:", scaleRangeText, true);
        label1.setEnabled(false);
        scaleRangeText.setEnabled(false);

        Font font = scaleBarPainter.getFont();
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(font.getSize(), 0.01, 48, 1));

        final JLabel label2 = optionsPanel.addComponentWithLabel("Font Size:", fontSizeSpinner);

        fontSizeSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final float size = ((Double) fontSizeSpinner.getValue()).floatValue();
                Font font = scaleBarPainter.getFont().deriveFont(size);
                scaleBarPainter.setFont(font);
            }
        });

        NumberFormat format = this.scaleBarPainter.getNumberFormat();
        int digits = format.getMaximumFractionDigits();
        digitsSpinner = new JSpinner(new SpinnerNumberModel(digits, 2, 14, 1));
        final JLabel label3 = optionsPanel.addComponentWithLabel("Significant Digits:", digitsSpinner);

        digitsSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final int digits = (Integer)digitsSpinner.getValue();
                NumberFormat format = scaleBarPainter.getNumberFormat();
                format.setMaximumFractionDigits(digits);
                scaleBarPainter.setNumberFormat(format);
            }
        });

        lineWeightSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 48.0, 1.0));

        lineWeightSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                float weight = ((Double) lineWeightSpinner.getValue()).floatValue();
                scaleBarPainter.setScaleBarStroke(new BasicStroke(weight, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            }
        });
        final JLabel label4 = optionsPanel.addComponentWithLabel("Line Weight:", lineWeightSpinner);

        final boolean isSelected1 = titleCheckBox.isSelected();
        final boolean isSelected2 = autoScaleCheck.isSelected();
        label1.setEnabled(isSelected1 && !isSelected2);
        scaleRangeText.setEnabled(isSelected1 && !isSelected2);
        label2.setEnabled(isSelected1);
        fontSizeSpinner.setEnabled(isSelected1);
        label3.setEnabled(isSelected1);
        digitsSpinner.setEnabled(isSelected1);
        label4.setEnabled(isSelected1);
        lineWeightSpinner.setEnabled(isSelected1);

        titleCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                final boolean isSelected1 = titleCheckBox.isSelected();
                final boolean isSelected2 = autoScaleCheck.isSelected();

                autoScaleCheck.setEnabled(isSelected1);
                label1.setEnabled(isSelected1 && !isSelected2);
                scaleRangeText.setEnabled(isSelected1 && !isSelected2);
                label2.setEnabled(isSelected1);
                fontSizeSpinner.setEnabled(isSelected1);
                label3.setEnabled(isSelected1);
                digitsSpinner.setEnabled(isSelected1);
                label4.setEnabled(isSelected1);
                lineWeightSpinner.setEnabled(isSelected1);

                scaleBarPainter.setVisible(isSelected1);
            }
        });

        autoScaleCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (autoScaleCheck.isSelected()) {
                    scaleBarPainter.setAutomaticScale(true);
                    double range = scaleBarPainter.getScaleRange();
                    scaleRangeText.setValue(range);
                    label1.setEnabled(false);
                    scaleRangeText.setEnabled(false);
                } else {
                    label1.setEnabled(true);
                    scaleRangeText.setEnabled(true);
                    scaleBarPainter.setAutomaticScale(false);
                }
            }
        });

        scaleRangeText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Double value = scaleRangeText.getValue();
                if (value != null) {
                    scaleBarPainter.setScaleRange(value);
                }
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
        // force a toggle of the checkbox
        autoScaleCheck.setSelected(false);
        autoScaleCheck.setSelected(true);
    }

    public void setSettings(Map<String,Object> settings) {
        autoScaleCheck.setSelected((Boolean)settings.get(SCALE_BAR_KEY + "." + AUTOMATIC_SCALE_KEY));
        scaleRangeText.setValue((Double)settings.get(SCALE_BAR_KEY + "." + SCALE_RANGE_KEY));
        fontSizeSpinner.setValue((Double)settings.get(SCALE_BAR_KEY + "." + FONT_SIZE_KEY));
        digitsSpinner.setValue((Integer)settings.get(SCALE_BAR_KEY + "." + SIGNIFICANT_DIGITS_KEY));
        lineWeightSpinner.setValue((Double)settings.get(SCALE_BAR_KEY + "." + LINE_WIDTH_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(SCALE_BAR_KEY + "." + AUTOMATIC_SCALE_KEY, autoScaleCheck.isSelected());
        settings.put(SCALE_BAR_KEY + "." + SCALE_RANGE_KEY, scaleRangeText.getValue());
        settings.put(SCALE_BAR_KEY + "." + FONT_SIZE_KEY, fontSizeSpinner.getValue());
        settings.put(SCALE_BAR_KEY + "." + SIGNIFICANT_DIGITS_KEY, digitsSpinner.getValue());
        settings.put(SCALE_BAR_KEY + "." + LINE_WIDTH_KEY, lineWeightSpinner.getValue());
    }

    private final JCheckBox titleCheckBox;
    private final OptionsPanel optionsPanel;

    private final JCheckBox autoScaleCheck;
    private final RealNumberField scaleRangeText;
    private final JSpinner fontSizeSpinner;
    private final JSpinner digitsSpinner;
    private final JSpinner lineWeightSpinner;

    public String getTitle() {
        return "Scale Bar";
    }

    private final ScaleBarPainter scaleBarPainter;
}