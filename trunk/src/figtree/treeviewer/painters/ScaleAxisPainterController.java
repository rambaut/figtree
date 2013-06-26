/*
 * ScaleAxisPainterController.java
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

import figtree.treeviewer.TreeViewerListener;
import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.prefs.Preferences;

import figtree.treeviewer.ControllerOptionsPanel;
import figtree.treeviewer.TreeViewer;
import figtree.ui.components.RealNumberField;

/**
 * @author Andrew Rambaut
 * @version $Id: ScaleAxisPainterController.java 819 2007-10-22 14:42:58Z rambaut $
 */
public class ScaleAxisPainterController extends AbstractController {

    private static Preferences PREFS = Preferences.userNodeForPackage(TreeViewer.class);

    private static final String SCALE_AXIS_KEY = "scaleAxis";

    private static final String FONT_NAME_KEY = "fontName";
    private static final String FONT_SIZE_KEY = "fontSize";
    private static final String FONT_STYLE_KEY = "fontStyle";

    private static final String NUMBER_FORMATTING_KEY = "numberFormatting";

    private static final String REVERSE_AXIS_KEY = "reverseAxis";

    private static final String SHOW_GRID_KEY = "showGrid";

    private static final String AUTOMATIC_SCALE_KEY = "automaticScale";
    private static final String MINOR_TICKS_KEY = "minorTicks";
    private static final String MAJOR_TICKS_KEY = "majorTicks";
    private static final String ORIGIN_KEY = "origin";
    private static final String LINE_WIDTH_KEY = "lineWidth";

//	private static final String SIGNIFICANT_DIGITS_KEY = "significantDigits";

    // The defaults if there is nothing in the preferences
    private static String DEFAULT_FONT_NAME = "sansserif";
    private static int DEFAULT_FONT_SIZE = 8;
    private static int DEFAULT_FONT_STYLE = Font.PLAIN;

    private static String DEFAULT_NUMBER_FORMATTING = "#.####";
    private static float DEFAULT_LINE_WIDTH = 1.0f;

    public ScaleAxisPainterController(final ScaleAxisPainter scaleAxisPainter, final ScaleGridPainter scaleGridPainter, final TreeViewer treeViewer) {
        this.scaleAxisPainter = scaleAxisPainter;
        this.scaleGridPainter = scaleGridPainter;

        final String defaultFontName = PREFS.get(FONT_NAME_KEY, DEFAULT_FONT_NAME);
        final int defaultFontStyle = PREFS.getInt(FONT_SIZE_KEY, DEFAULT_FONT_STYLE);
        final int defaultFontSize = PREFS.getInt(FONT_STYLE_KEY, DEFAULT_FONT_SIZE);
        final String defaultNumberFormatting = PREFS.get(NUMBER_FORMATTING_KEY, DEFAULT_NUMBER_FORMATTING);

        float lineWidth = PREFS.getFloat(LINE_WIDTH_KEY, DEFAULT_LINE_WIDTH);

        scaleAxisPainter.setFont(new Font(defaultFontName, defaultFontStyle, defaultFontSize));
        scaleAxisPainter.setNumberFormat(new DecimalFormat(defaultNumberFormatting));
        scaleAxisPainter.setScaleBarStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        optionsPanel = new ControllerOptionsPanel(2, 2);

        titleCheckBox = new JCheckBox(getTitle());

        titleCheckBox.setSelected(scaleAxisPainter.isVisible());

        reverseAxisCheck = new JCheckBox("Reverse axis");
        reverseAxisCheck.setSelected(false);

        showGridCheck = new JCheckBox("Show grid");
        showGridCheck.setSelected(true);
        optionsPanel.addSpanningComponent(showGridCheck);

        autoScaleCheck = new JCheckBox("Automatic scale");
        autoScaleCheck.setSelected(true);

        majorTicksText = new RealNumberField();
        majorTicksText.setValue(1.0);

        minorTicksText = new RealNumberField();
        minorTicksText.setValue(0.5);

        originText = new RealNumberField();
        originText.setValue(0.0);

        Font font = scaleAxisPainter.getFont();
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(font.getSize(), 0.01, 48, 1));

        fontSizeSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final float size = ((Double) fontSizeSpinner.getValue()).floatValue();
                Font font = scaleAxisPainter.getFont().deriveFont(size);
                scaleAxisPainter.setFont(font);
            }
        });

//		NumberFormat format = this.scaleAxisPainter.getNumberFormat();
//		int digits = format.getMaximumFractionDigits();
//		digitsSpinner = new JSpinner(new SpinnerNumberModel(digits, 2, 14, 1));
//		final JLabel label5 = optionsPanel.addComponentWithLabel("Sig. Digits:", digitsSpinner);
//
//		digitsSpinner.addChangeListener(new ChangeListener() {
//			public void stateChanged(ChangeEvent changeEvent) {
//				final int digits = (Integer)digitsSpinner.getValue();
//				NumberFormat format = scaleAxisPainter.getNumberFormat();
//				format.setMaximumFractionDigits(digits);
//				scaleAxisPainter.setNumberFormat(format);
//			}
//		});

        lineWeightSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 48.0, 1.0));

        lineWeightSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                float weight = ((Double) lineWeightSpinner.getValue()).floatValue();
                scaleAxisPainter.setScaleBarStroke(new BasicStroke(weight, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            }
        });
        final JLabel label6 = optionsPanel.addComponentWithLabel("Line Weight:", lineWeightSpinner);

        optionsPanel.addSpanningComponent(reverseAxisCheck);
        optionsPanel.addSpanningComponent(autoScaleCheck);
        final JLabel label1 = optionsPanel.addComponentWithLabel("Label spacing:", majorTicksText, true);
        final JLabel label2 = optionsPanel.addComponentWithLabel("Tick spacing:", minorTicksText, true);
        final JLabel label3 = optionsPanel.addComponentWithLabel("Origin value:", originText, true);
        final JLabel label4 = optionsPanel.addComponentWithLabel("Font Size:", fontSizeSpinner);

        final boolean isSelected1 = titleCheckBox.isSelected();
        final boolean isSelected2 = autoScaleCheck.isSelected();
        showGridCheck.setEnabled(isSelected1);
        reverseAxisCheck.setEnabled(isSelected1);
        autoScaleCheck.setEnabled(isSelected1);
        label1.setEnabled(isSelected1 && !isSelected2);
        majorTicksText.setEnabled(isSelected1 && !isSelected2);
        label2.setEnabled(isSelected1 && !isSelected2);
        minorTicksText.setEnabled(isSelected1 && !isSelected2);
        label3.setEnabled(isSelected1 && !isSelected2);
        originText.setEnabled(isSelected1 && !isSelected2);
        label4.setEnabled(isSelected1);
        fontSizeSpinner.setEnabled(isSelected1);
//		label5.setEnabled(isSelected1);
//		digitsSpinner.setEnabled(isSelected1);
        label6.setEnabled(isSelected1);
        lineWeightSpinner.setEnabled(isSelected1);

        titleCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final boolean isSelected1 = titleCheckBox.isSelected();
                final boolean isSelected2 = autoScaleCheck.isSelected();

                showGridCheck.setEnabled(isSelected1);
                reverseAxisCheck.setEnabled(isSelected1);

                autoScaleCheck.setEnabled(isSelected1);
                label1.setEnabled(isSelected1 && !isSelected2);
                majorTicksText.setEnabled(isSelected1 && !isSelected2);
                label2.setEnabled(isSelected1 && !isSelected2);
                minorTicksText.setEnabled(isSelected1 && !isSelected2);
                label3.setEnabled(isSelected1 && !isSelected2);
                originText.setEnabled(isSelected1 && !isSelected2);
                label4.setEnabled(isSelected1);
                fontSizeSpinner.setEnabled(isSelected1);
//				label5.setEnabled(isSelected1);
//				digitsSpinner.setEnabled(isSelected1);
                label6.setEnabled(isSelected1);
                lineWeightSpinner.setEnabled(isSelected1);

                scaleAxisPainter.setVisible(isSelected1);
                scaleGridPainter.setVisible(isSelected1);
            }
        });

        reverseAxisCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                scaleAxisPainter.setAxisReversed(reverseAxisCheck.isSelected());
            }
        });

        showGridCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                scaleGridPainter.setVisible(showGridCheck.isSelected());
            }
        });

        autoScaleCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                if (autoScaleCheck.isSelected()) {
                    scaleAxisPainter.setAutomatic(true);
                    majorTicksText.setValue(scaleAxisPainter.getMajorTickSpacing());
                    minorTicksText.setValue(scaleAxisPainter.getMinorTickSpacing());
                    originText.setValue(scaleAxisPainter.getAxisOrigin());
                    label1.setEnabled(false);
                    majorTicksText.setEnabled(false);
                    label2.setEnabled(false);
                    minorTicksText.setEnabled(false);
                    label3.setEnabled(false);
                    originText.setEnabled(false);
                } else {
                    label1.setEnabled(true);
                    minorTicksText.setEnabled(true);
                    label2.setEnabled(true);
                    majorTicksText.setEnabled(true);
                    label3.setEnabled(true);
                    originText.setEnabled(true);
                    double majorTickSpacing = getValue(majorTicksText, 1.0);
                    double minorTickSpacing = getValue(minorTicksText, 0.5);
                    double originValue = getValue(originText, 0.0);
                    scaleAxisPainter.setAutomatic(false);
                    scaleAxisPainter.setAxisSpacing(majorTickSpacing, minorTickSpacing);
                    scaleAxisPainter.setAxisOrigin(originValue);
                }
            }
        });



        ChangeListener listener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                if (!autoScaleCheck.isSelected()) {
                    double majorTickSpacing = getValue(majorTicksText, 1.0);
                    double minorTickSpacing = getValue(minorTicksText, 0.5);
                    scaleAxisPainter.setAxisSpacing(majorTickSpacing, minorTickSpacing);
                }
            }
        };

        minorTicksText.addChangeListener(listener);

        majorTicksText.addChangeListener(listener);

        originText.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                if (!autoScaleCheck.isSelected()) {
                    double originValue = getValue(originText, 0.0);
                    scaleAxisPainter.setAxisOrigin(originValue);
                }
            }
        });

        treeViewer.addTreeViewerListener(new TreeViewerListener() {
            public void treeChanged() {
                scaleAxisPainter.firePainterChanged();
            }

            public void treeSettingsChanged() {
                // nothing to do
            }
        });
    }

    private double getValue(RealNumberField field, double defaultValue) {
        Double value = field.getValue();
        return (value != null ? value : defaultValue);
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
        titleCheckBox.setSelected((Boolean)settings.get(SCALE_AXIS_KEY + "." + IS_SHOWN));
        reverseAxisCheck.setSelected((Boolean)settings.get(SCALE_AXIS_KEY + "." + REVERSE_AXIS_KEY));
        showGridCheck.setSelected((Boolean)settings.get(SCALE_AXIS_KEY + "." + SHOW_GRID_KEY));
//		minorTicksText.setValue((Double)settings.get(SCALE_AXIS_KEY + "." + MINOR_TICKS_KEY));
        majorTicksText.setValue((Double)settings.get(SCALE_AXIS_KEY + "." + MAJOR_TICKS_KEY));
        originText.setValue((Double)settings.get(SCALE_AXIS_KEY + "." + ORIGIN_KEY));
        autoScaleCheck.setSelected((Boolean)settings.get(SCALE_AXIS_KEY + "." + AUTOMATIC_SCALE_KEY));
//        scaleAxisPainter.setAutomaticScale(autoScaleCheck.isSelected());
        fontSizeSpinner.setValue((Double)settings.get(SCALE_AXIS_KEY + "." + FONT_SIZE_KEY));
//		digitsSpinner.setValue((Integer)settings.get(SCALE_AXIS_KEY + "." + SIGNIFICANT_DIGITS_KEY));
        lineWeightSpinner.setValue((Double)settings.get(SCALE_AXIS_KEY + "." + LINE_WIDTH_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(SCALE_AXIS_KEY + "." + IS_SHOWN, titleCheckBox.isSelected());
        settings.put(SCALE_AXIS_KEY + "." + REVERSE_AXIS_KEY, reverseAxisCheck.isSelected());
        settings.put(SCALE_AXIS_KEY + "." + SHOW_GRID_KEY, showGridCheck.isSelected());
        settings.put(SCALE_AXIS_KEY + "." + AUTOMATIC_SCALE_KEY, autoScaleCheck.isSelected());
//		settings.put(SCALE_AXIS_KEY + "." + MINOR_TICKS_KEY, minorTicksText.getValue());
        settings.put(SCALE_AXIS_KEY + "." + MAJOR_TICKS_KEY, majorTicksText.getValue());
        settings.put(SCALE_AXIS_KEY + "." + ORIGIN_KEY, originText.getValue());
        settings.put(SCALE_AXIS_KEY + "." + FONT_SIZE_KEY, fontSizeSpinner.getValue());
//		settings.put(SCALE_AXIS_KEY + "." + SIGNIFICANT_DIGITS_KEY, digitsSpinner.getValue());
        settings.put(SCALE_AXIS_KEY + "." + LINE_WIDTH_KEY, lineWeightSpinner.getValue());
    }

    private final JCheckBox titleCheckBox;
    private final OptionsPanel optionsPanel;

    private final JCheckBox reverseAxisCheck;
    private final JCheckBox showGridCheck;
    private final JCheckBox autoScaleCheck;
    private final RealNumberField minorTicksText;
    private final RealNumberField majorTicksText;
    private final RealNumberField originText;
    private final JSpinner fontSizeSpinner;
    //	private final JSpinner digitsSpinner;
    private final JSpinner lineWeightSpinner;

    public String getTitle() {
        return "Scale Axis";
    }

    private final ScaleAxisPainter scaleAxisPainter;
    private final ScaleGridPainter scaleGridPainter;
}