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
 * @author Andrew Rambaut
 * @version $Id$
 */
public class ContinuousColourScaleDialog {
    private static final int SLIDER_RANGE = 1000;

    private static final String HSB_SPECTRUM = "HSB Spectrum";
    private static final String COLOUR_GRADIENT = "Colour Gradient";

    private JFrame frame;

    private JComboBox colourSchemeCombo = new JComboBox(new String[] { HSB_SPECTRUM, COLOUR_GRADIENT} );

    CardLayout cardLayout = new CardLayout();
    private final JPanel colourSchemePanel;

    private Map<String, ColourSchemePanel> colourSchemeNamePanelMap = new HashMap<String, ColourSchemePanel>();
    private Map<Class, String> colourSchemeClassNameMap = new HashMap<Class, String>();

    private ContinuousColourDecorator decorator;

    private JComponent colourDisplay;

    private final JCheckBox autoScaleCheck;
    private final JLabel fromLabel;
    private final RealNumberField fromNumberField;
    private final JLabel toLabel;
    private final RealNumberField toNumberField;


    private JDialog dialog;

    public ContinuousColourScaleDialog(final JFrame frame) {
        this.frame = frame;

        colourSchemeNamePanelMap.put(HSB_SPECTRUM, new HSBColourSchemePanel());
        colourSchemeNamePanelMap.put(COLOUR_GRADIENT, new InterpolatingColourSchemePanel());

        colourSchemeClassNameMap.put(HSBContinuousColourDecorator.class, HSB_SPECTRUM);
        colourSchemeClassNameMap.put(InterpolatingColourDecorator.class, COLOUR_GRADIENT);

        autoScaleCheck = new JCheckBox("Auto-scale range between min and max values");
        fromLabel = new JLabel("Range from:");
        fromNumberField = new RealNumberField();
        fromNumberField.setColumns(10);
        toLabel = new JLabel("to:");
        toNumberField = new RealNumberField();
        toNumberField.setColumns(10);

        autoScaleCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                boolean enabled = !autoScaleCheck.isSelected();
                fromLabel.setEnabled(enabled);
                fromNumberField.setEnabled(enabled);
                toLabel.setEnabled(enabled);
                toNumberField.setEnabled(enabled);
            }
        });

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
                colourDisplay.repaint();
                dialog.pack();
            }
        });

        colourDisplay = new JComponent() {
            private final static int MAX_HEIGHT = 20;
            @Override
            public void paint(Graphics graphics) {
                ContinuousColourDecorator decorator = getDecorator();

                Graphics2D g = (Graphics2D)graphics;
                Rectangle r = getVisibleRect();
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
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, MAX_HEIGHT);
            }
        };

    }

    public int showDialog() {

        ContinuousColourDecorator currentDecorator = getDecorator();

        final OptionsPanel options = new OptionsPanel(6, 6);

        JPanel panel5 = new JPanel(new BorderLayout());
        panel5.add(colourDisplay, BorderLayout.CENTER);
        panel5.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        options.addSpanningComponent(panel5);

        options.addComponent(autoScaleCheck);

        JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout());
        panel4.add(fromLabel);
        panel4.add(fromNumberField);
        panel4.add(toLabel);
        panel4.add(toNumberField);
        options.addComponent(panel4);

        options.addComponentWithLabel("Scheme: ", colourSchemeCombo);

        colourSchemePanel.setBorder(BorderFactory.createBevelBorder(1));
        options.addSpanningComponent(colourSchemePanel);


        JOptionPane optionPane = new JOptionPane(options,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null,
                null,
                null);
        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));

        dialog = optionPane.createDialog(frame, "Setup colour range: " + currentDecorator.getAttributeName());

        colourSchemeCombo.setSelectedItem(colourSchemeClassNameMap.get(decorator.getClass()));

        dialog.pack();
        dialog.setResizable(false);
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
        autoScaleCheck.setSelected(!decorator.getContinuousScale().isNormalize());
        fromNumberField.setValue(decorator.getContinuousScale().getLowerRange());
        toNumberField.setValue(decorator.getContinuousScale().getUpperRange());
        for (String key : colourSchemeNamePanelMap.keySet()) {
            colourSchemeNamePanelMap.get(key).setDecorator(decorator);
        }
    }

    public ContinuousColourDecorator getDecorator() {
        String name = colourSchemeCombo.getSelectedItem().toString();
        decorator = colourSchemeNamePanelMap.get(name).getDecorator();
        decorator.getContinuousScale().setNormalize(!autoScaleCheck.isSelected());
        decorator.getContinuousScale().setLowerRange(fromNumberField.getValue());
        decorator.getContinuousScale().setUpperRange(toNumberField.getValue());
        return decorator;
    }

    private interface ColourSchemePanel {
        void setDecorator(ContinuousColourDecorator decorator);
        ContinuousColourDecorator getDecorator();

        JPanel getPanel();


        String getName();
    }

    private class HSBColourSchemePanel implements ColourSchemePanel  {
        public HSBColourSchemePanel() {
            hueSlider = new RangeSlider(0, SLIDER_RANGE);
            saturationSlider = new RangeSlider(0, SLIDER_RANGE);
            brightnessSlider = new RangeSlider(0, SLIDER_RANGE);
            reverseHueCheck = new JCheckBox("Reverse hue spectrum");
        }

        public void setDecorator(ContinuousColourDecorator decorator) {
            if (decorator instanceof HSBContinuousColourDecorator) {
                hsbDecorator = (HSBContinuousColourDecorator)decorator;
            } else {
                if (hsbDecorator == null) {
                    hsbDecorator = new HSBContinuousColourDecorator(decorator.getContinuousScale());
                }
            }

            hueSlider.setValue((int)(hsbDecorator.getHueLower() * SLIDER_RANGE));
            hueSlider.setUpperValue((int) (hsbDecorator.getHueUpper() * SLIDER_RANGE));

            saturationSlider.setValue((int)(hsbDecorator.getSaturationLower() * SLIDER_RANGE));
            saturationSlider.setUpperValue((int)(hsbDecorator.getSaturationUpper() * SLIDER_RANGE));

            brightnessSlider.setValue((int)(hsbDecorator.getBrightnessLower() * SLIDER_RANGE));
            brightnessSlider.setUpperValue((int)(hsbDecorator.getBrightnessUpper() * SLIDER_RANGE));

            reverseHueCheck.setSelected(hsbDecorator.isReverseHue());
        }

        @Override
        public  ContinuousColourDecorator getDecorator() {
            hsbDecorator.setHueLower(((float) hueSlider.getValue()) / SLIDER_RANGE);
            hsbDecorator.setHueUpper(((float) hueSlider.getUpperValue()) / SLIDER_RANGE);

            hsbDecorator.setSaturationLower(((float) saturationSlider.getValue()) / SLIDER_RANGE);
            hsbDecorator.setSaturationUpper(((float) saturationSlider.getUpperValue()) / SLIDER_RANGE);

            hsbDecorator.setBrightnessLower(((float) brightnessSlider.getValue()) / SLIDER_RANGE);
            hsbDecorator.setBrightnessUpper(((float) brightnessSlider.getUpperValue()) / SLIDER_RANGE);

            hsbDecorator.setReverseHue(reverseHueCheck.isSelected());

            return hsbDecorator;
        }

        @Override
        public JPanel getPanel() {
            if (panel == null) {
                final OptionsPanel options = new OptionsPanel(6, 6);

                options.addComponentWithLabel("Hue: ", hueSlider);
                options.addComponentWithLabel("Saturation: ", saturationSlider);
                options.addComponentWithLabel("Brightness: ", brightnessSlider);
                options.addComponent(reverseHueCheck);

                ChangeListener listener = new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        getDecorator();
                        colourDisplay.repaint();
                    }
                };

                hueSlider.addChangeListener(listener);
                saturationSlider.addChangeListener(listener);
                brightnessSlider.addChangeListener(listener);
                reverseHueCheck.addChangeListener(listener);
                panel = options;
            }
            return panel;
        }

        @Override
        public String getName() {
            return HSB_SPECTRUM;
        }

        private RangeSlider hueSlider;
        private RangeSlider saturationSlider;
        private RangeSlider brightnessSlider;
        private JCheckBox reverseHueCheck;

        private HSBContinuousColourDecorator hsbDecorator = null;

        private JPanel panel = null;
    }

    private class InterpolatingColourSchemePanel implements ColourSchemePanel  {
        public InterpolatingColourSchemePanel() {

            middleColourCheck = new JCheckBox("through:");

            fromColourButton = new ColorWellButton(InterpolatingColourDecorator.DEFAULT_COLOR_1, "Choose Start Colour");
            toColourButton = new ColorWellButton(InterpolatingColourDecorator.DEFAULT_COLOR_2, "Choose End Colour");
            middleColourButton = new ColorWellButton(InterpolatingColourDecorator.DEFAULT_COLOR_3, "Choose Middle Colour");

            middleColourCheck.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    middleColourButton.setEnabled(middleColourCheck.isSelected());
                }
            });
            middleColourCheck.setSelected(false);
            middleColourButton.setEnabled(false);
        }

        public void setDecorator(ContinuousColourDecorator decorator) {
            if (decorator instanceof InterpolatingColourDecorator) {
                this.interpolatingDecorator = (InterpolatingColourDecorator)decorator;
            } else {
                if (interpolatingDecorator == null) {
                    interpolatingDecorator = new InterpolatingColourDecorator(decorator.getContinuousScale());
                }
            }

            fromColourButton.setSelectedColor(interpolatingDecorator.getColor1());
            if (interpolatingDecorator.getColor3() != null) {
                middleColourCheck.setSelected(true);
                middleColourButton.setSelectedColor(interpolatingDecorator.getColor2());
                toColourButton.setSelectedColor(interpolatingDecorator.getColor3());
            } else {
                middleColourCheck.setSelected(false);
                toColourButton.setSelectedColor(interpolatingDecorator.getColor2());
            }
        }

        @Override
        public  ContinuousColourDecorator getDecorator() {
            if (middleColourCheck.isSelected()) {
                interpolatingDecorator.setColours(
                        fromColourButton.getSelectedColor(),
                        middleColourButton.getSelectedColor(),
                        toColourButton.getSelectedColor());
            } else {
                interpolatingDecorator.setColours(
                        fromColourButton.getSelectedColor(),
                        toColourButton.getSelectedColor());
            }
            return interpolatingDecorator;
        }

        @Override
        public JPanel getPanel() {
            if (panel == null) {
                final OptionsPanel options = new OptionsPanel(6, 6);

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
                this.panel = options;

                ChangeListener listener = new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        getDecorator();
                        colourDisplay.repaint();
                    }
                };

                fromColourButton.addChangeListener(listener);
                toColourButton.addChangeListener(listener);
                middleColourCheck.addChangeListener(listener);
                middleColourButton.addChangeListener(listener);
            }
            return panel;
        }

        @Override
        public String getName() {
            return COLOUR_GRADIENT;
        }

        private ColorWellButton fromColourButton;
        private ColorWellButton toColourButton;

        private JCheckBox middleColourCheck;
        private ColorWellButton middleColourButton;

        private InterpolatingColourDecorator interpolatingDecorator = null;

        private JPanel panel = null;
    }
}