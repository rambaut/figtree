package figtree.treeviewer;

import figtree.treeviewer.decorators.ContinuousColourDecorator;
import figtree.treeviewer.decorators.ContinuousScale;
import figtree.treeviewer.decorators.HSBContinuousColourDecorator;
import figtree.treeviewer.decorators.InterpolatingColourDecorator;
import figtree.ui.components.ColorWellButton;
import figtree.ui.components.RangeSlider;
import figtree.ui.components.RealNumberField;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * DiscreteColourScaleDialog.java
 *
 * @author			Andrew Rambaut
 * @version			$Id$
 */
public class ContinuousColourScaleDialog {
    private static final int SLIDER_RANGE = 1000;

    private static final String HSB_SPECTRUM = "HSB Spectrum";
    private static final String COLOUR_GRADIENT = "Colour Gradient";

    private JFrame frame;

    private ContinuousColourDecorator decorator;

    private JComboBox colourSchemeCombo = new JComboBox(new String[] { HSB_SPECTRUM, COLOUR_GRADIENT} );

    private Map<Class, ColourSchemePanel> colourSchemePanelMap = new HashMap<Class, ColourSchemePanel>();

    public ContinuousColourScaleDialog(final JFrame frame) {
        this.frame = frame;

        colourSchemePanelMap.put(HSBContinuousColourDecorator.class, new HSBColourSchemePanel());
        colourSchemePanelMap.put(InterpolatingColourDecorator.class, new InterpolatingColourSchemePanel());
    }

    public int showDialog() {

        setDecorator(decorator);

        JPanel panel1 = new JPanel(new BorderLayout());
        panel1.add(colourSchemeCombo, BorderLayout.CENTER);
        panel1.add(new JLabel("Scheme: "), BorderLayout.WEST);

        JPanel options = colourSchemePanelMap.get(decorator.getClass()).getPanel();

        final JPanel panel2 = new JPanel(new BorderLayout(12, 12));
        panel2.add(panel1, BorderLayout.NORTH);
        panel2.add(options, BorderLayout.CENTER);

        JOptionPane optionPane = new JOptionPane(panel2,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null,
                null,
                null);
        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));


        final JDialog dialog = optionPane.createDialog(frame, "Setup colour range: " + decorator.getAttributeName());

        colourSchemeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JPanel options;
                if (colourSchemeCombo.getSelectedItem().equals(HSB_SPECTRUM)) {
                    options = colourSchemePanelMap.get(HSBContinuousColourDecorator.class).getPanel();
                } else if (colourSchemeCombo.getSelectedItem().equals(COLOUR_GRADIENT)) {
                    options = colourSchemePanelMap.get(InterpolatingColourDecorator.class).getPanel();
                } else {
                    throw new IllegalArgumentException("Unrecognized colour scheme type: " + colourSchemeCombo.getSelectedItem().toString());
                }
                panel2.add(options, BorderLayout.CENTER);
                dialog.pack();

            }
        });

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

    public void setDecorator(ContinuousColourDecorator decorator) {
        this.decorator = decorator;
        colourSchemePanelMap.get(decorator.getClass()).setDecorator(decorator);
    }

    public ContinuousColourDecorator getDecorator() {
        colourSchemePanelMap.get(decorator.getClass()).setupDecorator(decorator);

        return decorator;
    }

    private interface ColourSchemePanel {
        void setDecorator(ContinuousColourDecorator decorator);
        void setupDecorator(ContinuousColourDecorator decorator);

        JPanel getPanel();
    }

    private class HSBColourSchemePanel implements ColourSchemePanel  {
        public HSBColourSchemePanel() {
            hueSlider = new RangeSlider(0, SLIDER_RANGE);
            saturationSlider = new RangeSlider(0, SLIDER_RANGE);
            brightnessSlider = new RangeSlider(0, SLIDER_RANGE);
            reverseHueCheck = new JCheckBox("Reverse hue spectrum");
        }

        public void setDecorator(ContinuousColourDecorator decorator) {
            HSBContinuousColourDecorator hsbDecorator = (HSBContinuousColourDecorator)decorator;

            hueSlider.setValue((int)(hsbDecorator.getHueLower() * SLIDER_RANGE));
            hueSlider.setUpperValue((int) (hsbDecorator.getHueUpper() * SLIDER_RANGE));

            saturationSlider.setValue((int)(hsbDecorator.getSaturationLower() * SLIDER_RANGE));
            saturationSlider.setUpperValue((int)(hsbDecorator.getSaturationUpper() * SLIDER_RANGE));

            brightnessSlider.setValue((int)(hsbDecorator.getBrightnessLower() * SLIDER_RANGE));
            brightnessSlider.setUpperValue((int)(hsbDecorator.getBrightnessUpper() * SLIDER_RANGE));

            reverseHueCheck.setSelected(hsbDecorator.isReverseHue());
        }

        public void setupDecorator(ContinuousColourDecorator decorator) {
            HSBContinuousColourDecorator hsbDecorator = (HSBContinuousColourDecorator)decorator;

            hsbDecorator.setHueLower(((float) hueSlider.getValue()) / SLIDER_RANGE);
            hsbDecorator.setHueUpper(((float) hueSlider.getUpperValue()) / SLIDER_RANGE);

            hsbDecorator.setSaturationLower(((float) saturationSlider.getValue()) / SLIDER_RANGE);
            hsbDecorator.setSaturationUpper(((float) saturationSlider.getUpperValue()) / SLIDER_RANGE);

            hsbDecorator.setBrightnessLower(((float) brightnessSlider.getValue()) / SLIDER_RANGE);
            hsbDecorator.setBrightnessUpper(((float) brightnessSlider.getUpperValue()) / SLIDER_RANGE);

            hsbDecorator.setReverseHue(reverseHueCheck.isSelected());
        }

        @Override
        public JPanel getPanel() {
            final OptionsPanel options = new OptionsPanel(6, 6);

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

            return options;
        }

        private RangeSlider hueSlider;
        private RangeSlider saturationSlider;
        private RangeSlider brightnessSlider;
        private JCheckBox reverseHueCheck;
    }

    private class InterpolatingColourSchemePanel implements ColourSchemePanel  {
        public InterpolatingColourSchemePanel() {

            autoScaleCheck = new JCheckBox("Auto-scale range between min and max values");
            autoScaleCheck.setSelected(false);

            fromLabel = new JLabel("Range from:");
            fromNumberField = new RealNumberField();
            fromNumberField.setColumns(10);

            toLabel = new JLabel("to:");
            toNumberField = new RealNumberField();
            toNumberField.setColumns(10);

            fromLabel.setEnabled(false);
            fromNumberField.setEnabled(false);
            toLabel.setEnabled(false);
            toNumberField.setEnabled(false);

            middleColourCheck = new JCheckBox("through:");

            fromColourButton = new ColorWellButton(Color.RED, "Choose Start Colour");
            toColourButton = new ColorWellButton(Color.BLUE, "Choose End Colour");
            middleColourButton = new ColorWellButton(Color.BLACK, "Choose Middle Colour");

            autoScaleCheck.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    boolean enabled = !autoScaleCheck.isSelected();
                    fromLabel.setEnabled(enabled);
                    fromNumberField.setEnabled(enabled);
                    toLabel.setEnabled(enabled);
                    toNumberField.setEnabled(enabled);
                }
            });

            middleColourCheck.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    middleColourButton.setEnabled(middleColourCheck.isSelected());
                }
            });
            middleColourCheck.setSelected(false);
            middleColourButton.setEnabled(false);
        }

        public void setDecorator(ContinuousColourDecorator decorator) {
            InterpolatingColourDecorator interpolatingDecorator = (InterpolatingColourDecorator)decorator;

//            autoScaleCheck.setSelected(interpolatingDecorator);
//            fromNumberField.setValue(settings.fromValue);
//            toNumberField.setValue(settings.toValue);

        }

        public void setupDecorator(ContinuousColourDecorator decorator) {
            InterpolatingColourDecorator interpolatingDecorator = (InterpolatingColourDecorator)decorator;
//            settings.autoRange = autoScaleCheck.isSelected();
//            settings.fromValue =  fromNumberField.getValue();
//            settings.toValue = toNumberField.getValue();
//            settings.fromColour = fromColourButton.getSelectedColor();
//            settings.toColour = toColourButton.getSelectedColor();
//            if (middleColourCheck.isSelected()) {
//                settings.middleColour = middleColourButton.getSelectedColor();
//            } else {
//                settings.middleColour = null;
//            }
        }

        @Override
        public JPanel getPanel() {
            final OptionsPanel options = new OptionsPanel(6, 6);

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
            return options;
        }

        private JCheckBox autoScaleCheck;
        private JLabel fromLabel;
        private RealNumberField fromNumberField;
        private JLabel toLabel;
        private RealNumberField toNumberField;

        private ColorWellButton fromColourButton;
        private ColorWellButton toColourButton;

        private JCheckBox middleColourCheck;
        private ColorWellButton middleColourButton;

    }
}