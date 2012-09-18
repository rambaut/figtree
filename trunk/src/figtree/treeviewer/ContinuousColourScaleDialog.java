package figtree.treeviewer;

import figtree.treeviewer.decorators.ContinuousScale;
import figtree.treeviewer.decorators.HSBContinuousColourDecorator;
import figtree.ui.components.RangeSlider;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * DiscreteColourScaleDialog.java
 *
 * @author			Andrew Rambaut
 * @version			$Id$
 */
public class ContinuousColourScaleDialog {
    private static final int SLIDER_RANGE = 1000;

    private JFrame frame;

    private HSBContinuousColourDecorator decorator;

    private RangeSlider hueSlider;
    private RangeSlider saturationSlider;
    private RangeSlider brightnessSlider;
    private JCheckBox reverseHueCheck;

    public ContinuousColourScaleDialog(final JFrame frame) {
        this.frame = frame;

        hueSlider = new RangeSlider(0, SLIDER_RANGE);
        saturationSlider = new RangeSlider(0, SLIDER_RANGE);
        brightnessSlider = new RangeSlider(0, SLIDER_RANGE);
        reverseHueCheck = new JCheckBox("Reverse hue spectrum");
    }

    public int showDialog() {

        final OptionsPanel options = new OptionsPanel(6, 6);

//        options.addComponent(new JLabel("Editing colour range for attribute: " + decorator.getAttributeName()));

        final JComponent colourDisplay = new JComponent() {
            private final static int MAX_HEIGHT = 20;
            @Override
            public void paint(Graphics graphics) {
                Graphics2D g = (Graphics2D)graphics;
                Rectangle r = getBounds();
                int width = r.width;
                r.width = 1;
                ContinuousScale scale = decorator.getContinuousScale();
                double v = scale.getMinValue();
                double d = (scale.getMaxValue() - v) / width;
                for (int i = 0; i < width; i ++) {
                    g.setColor(decorator.getColourForValue(v));
                    g.fill(r);
                    r.x ++;
                    v += d;

                }
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(super.getMaximumSize().width, MAX_HEIGHT);
            }

            @Override
            public Dimension getMinimumSize() {
                return new Dimension(super.getMinimumSize().width, MAX_HEIGHT);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, MAX_HEIGHT);
            }
        };
        options.addSpanningComponent(colourDisplay);

        options.addComponentWithLabel("Hue: ", hueSlider);
        options.addComponentWithLabel("Saturation: ", saturationSlider);
        options.addComponentWithLabel("Brightness: ", brightnessSlider);
        options.addComponent(reverseHueCheck);

        setDecorator(decorator);

        ChangeListener listener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                setupDecorator(decorator);
                colourDisplay.repaint();
            }
        };

        hueSlider.addChangeListener(listener);
        saturationSlider.addChangeListener(listener);
        brightnessSlider.addChangeListener(listener);
        reverseHueCheck.addChangeListener(listener);

        JOptionPane optionPane = new JOptionPane(options,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null,
                null,
                null);
        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));

        final JDialog dialog = optionPane.createDialog(frame, "Setup colour range: " + decorator.getAttributeName());
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

    public void setDecorator(HSBContinuousColourDecorator decorator) {
        this.decorator = decorator;

        hueSlider.setValue((int)(decorator.getHueLower() * SLIDER_RANGE));
        hueSlider.setUpperValue((int) (decorator.getHueUpper() * SLIDER_RANGE));

        saturationSlider.setValue((int)(decorator.getSaturationLower() * SLIDER_RANGE));
        saturationSlider.setUpperValue((int)(decorator.getSaturationUpper() * SLIDER_RANGE));

        brightnessSlider.setValue((int)(decorator.getBrightnessLower() * SLIDER_RANGE));
        brightnessSlider.setUpperValue((int)(decorator.getBrightnessUpper() * SLIDER_RANGE));

        reverseHueCheck.setSelected(decorator.isReverseHue());
    }

    public void setupDecorator(HSBContinuousColourDecorator decorator) {
        decorator.setHueLower(((float) hueSlider.getValue()) / SLIDER_RANGE);
        decorator.setHueUpper(((float) hueSlider.getUpperValue()) / SLIDER_RANGE);

        decorator.setSaturationLower(((float) saturationSlider.getValue()) / SLIDER_RANGE);
        decorator.setSaturationUpper(((float) saturationSlider.getUpperValue()) / SLIDER_RANGE);

        decorator.setBrightnessLower(((float) brightnessSlider.getValue()) / SLIDER_RANGE);
        decorator.setBrightnessUpper(((float) brightnessSlider.getUpperValue()) / SLIDER_RANGE);

        decorator.setReverseHue(reverseHueCheck.isSelected());
    }

}