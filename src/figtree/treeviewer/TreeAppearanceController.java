package figtree.treeviewer;

import figtree.treeviewer.painters.AttributeComboHelper;
import figtree.treeviewer.painters.AttributeComboHelperListener;
import figtree.treeviewer.painters.NodeShapePainter;
import jam.controlpalettes.ControllerListener;
import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.prefs.Preferences;

import figtree.treeviewer.decorators.*;

/**
 * @author Andrew Rambaut
 * @version $Id: TreeAppearanceController.java 822 2007-10-26 13:50:26Z rambaut $
 */
public class TreeAppearanceController extends AbstractController {

    private static final String CONTROLLER_TITLE = "Appearance";

    public static Preferences PREFS = Preferences.userNodeForPackage(TreeViewer.class);

    public static final String CONTROLLER_KEY = "appearance";

    public static final String FOREGROUND_COLOUR_KEY = "foregroundColour";
    public static final String BACKGROUND_COLOUR_KEY = "backgroundColour";
    public static final String SELECTION_COLOUR_KEY = "selectionColour";
    public static final String BRANCH_COLOR_ATTRIBUTE_KEY = "branchColorAttribute";
    public static final String BRANCH_COLOR_GRADIENT_KEY = "branchColorGradient";
    public static final String HILIGHTING_GRADIENT_KEY = "hilightingGradient";
    public static final String BACKGROUND_COLOR_ATTRIBUTE_KEY = "backgroundColorAttribute";
    public static final String BRANCH_LINE_WIDTH_KEY = "branchLineWidth";
    public static final String BRANCH_MIN_LINE_WIDTH_KEY = "branchMinLineWidth";
    public static final String BRANCH_WIDTH_ATTRIBUTE_KEY = "branchWidthAttribute";

    // The defaults if there is nothing in the preferences
    public static Color DEFAULT_FOREGROUND_COLOUR = Color.BLACK;
    public static Color DEFAULT_BACKGROUND_COLOUR = Color.WHITE;
    public static Color DEFAULT_SELECTION_COLOUR = new Color(45, 54, 128);
    public static float DEFAULT_BRANCH_LINE_WIDTH = 1.0f;

    public static final String FIXED = "Fixed";

    public TreeAppearanceController(final TreeViewer treeViewer, final JFrame frame,
                                    final AttributeColourController colourController) {
        this.treeViewer = treeViewer;
        this.colourController = colourController;

        userBranchColourDecorator = new AttributableDecorator();
        userBranchColourDecorator.setPaintAttributeName("!color");
        userBranchColourDecorator.setStrokeAttributeName("!stroke");
        treeViewer.setBranchDecorator(userBranchColourDecorator, false);

        int foregroundRGB = TreeAppearanceController.PREFS.getInt(CONTROLLER_KEY + "." + FOREGROUND_COLOUR_KEY, DEFAULT_FOREGROUND_COLOUR.getRGB());
        int backgroundRGB = TreeAppearanceController.PREFS.getInt(CONTROLLER_KEY + "." + BACKGROUND_COLOUR_KEY, DEFAULT_BACKGROUND_COLOUR.getRGB());
        int selectionRGB = TreeAppearanceController.PREFS.getInt(CONTROLLER_KEY + "." + SELECTION_COLOUR_KEY, DEFAULT_SELECTION_COLOUR.getRGB());
        float branchLineWidth = TreeAppearanceController.PREFS.getFloat(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY, DEFAULT_BRANCH_LINE_WIDTH);

        treeViewer.setForeground(new Color(foregroundRGB));
        treeViewer.setBackground(new Color(backgroundRGB));
        treeViewer.setSelectionColor(new Color(selectionRGB));
        treeViewer.setBranchStroke(new BasicStroke(branchLineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        titleLabel = new JLabel(CONTROLLER_TITLE);

        optionsPanel = new ControllerOptionsPanel(2, 0);

        branchWidthAttributeCombo = new JComboBox(new String[] { "No attributes" });
        branchColourAttributeCombo = new JComboBox(new String[] { "No attributes" });
        backgroundColourAttributeCombo = new JComboBox(new String[] { "No attributes" });

//        setupAttributes(treeViewer.getTrees());

//        branchColourSettings.autoRange  = true;
//        branchColourSettings.fromValue = 0.0;
//        branchColourSettings.toValue = 1.0;
//        branchColourSettings.fromColour = new Color(0, 16, 192);
//        branchColourSettings.toColour = new Color(192, 16, 0);
//        branchColourSettings.middleColour = new Color(0, 0, 0);

        branchColourIsGradient = TreeAppearanceController.PREFS.getBoolean(CONTROLLER_KEY + "." + BRANCH_COLOR_GRADIENT_KEY, false);

        final JButton setupColourButton = new JButton("Colours");

        colourController.setupControls(branchColourAttributeCombo, setupColourButton);
        colourController.addControllerListener(new ControllerListener() {
            @Override
            public void controlsChanged() {
                setupBranchDecorators();
            }
        });

        branchColourGradientCheck = new JCheckBox("Gradient");
        branchColourGradientCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                branchColourIsGradient = branchColourGradientCheck.isSelected();
                setupBranchDecorators();
            }
        });
        optionsPanel.addComponentWithLabel("Colour by:", branchColourAttributeCombo);
        final JLabel setupColourButtonLabel = optionsPanel.addComponentWithLabel("Setup:", setupColourButton);
        optionsPanel.addComponent(branchColourGradientCheck);
        optionsPanel.addSeparator();

        boolean hilightingGradient = TreeAppearanceController.PREFS.getBoolean(CONTROLLER_KEY + "." + HILIGHTING_GRADIENT_KEY, false);

        hilightingGradientCheck = new JCheckBox("Hilight with gradient");
        hilightingGradientCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                treeViewer.setHilightingGradient(hilightingGradientCheck.isSelected());
            }
        });
        optionsPanel.addComponent(hilightingGradientCheck);
        hilightingGradientCheck.setSelected(hilightingGradient);
        optionsPanel.addSeparator();

        branchLineWidthSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 48.0, 1.0));

        optionsPanel.addComponentWithLabel("Line Weight:", branchLineWidthSpinner);
        optionsPanel.addComponentWithLabel("Width by:", branchWidthAttributeCombo);

        branchMinLineWidthSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 48.0, 1.0));
        final JLabel label = optionsPanel.addComponentWithLabel("Min Weight:", branchMinLineWidthSpinner);

        optionsPanel.addSeparator();

//        backgroundColourSettings.autoRange  = true;
//        backgroundColourSettings.fromValue = 0.0;
//        backgroundColourSettings.toValue = 1.0;
//        backgroundColourSettings.fromColour = new Color(255, 255, 255);
//        backgroundColourSettings.toColour = new Color(192, 16, 0);
//        backgroundColourSettings.middleColour = null;

        final JButton bgSetupColourButton = new JButton("Colours");
        colourController.setupControls(backgroundColourAttributeCombo, bgSetupColourButton);
        colourController.addControllerListener(new ControllerListener() {
            @Override
            public void controlsChanged() {
                setupBranchDecorators();
            }
        });

        optionsPanel.addComponentWithLabel("Background:", backgroundColourAttributeCombo);
        final JLabel bgSetupColourButtonLabel = optionsPanel.addComponentWithLabel("Setup:", bgSetupColourButton);

        new AttributeComboHelper(branchColourAttributeCombo, treeViewer, "User selection", false, true).addListener(new AttributeComboHelperListener() {
            @Override
            public void attributeComboChanged() {
                boolean isSelected = branchColourAttributeCombo.getSelectedIndex() != 0;
                setupColourButtonLabel.setEnabled(isSelected);
                setupColourButton.setEnabled(isSelected);

                setupBranchDecorators();
            }
        });

        new AttributeComboHelper(backgroundColourAttributeCombo, treeViewer, "Default").addListener(new AttributeComboHelperListener() {
            @Override
            public void attributeComboChanged() {
                boolean isSelected = backgroundColourAttributeCombo.getSelectedIndex() != 0;
                bgSetupColourButtonLabel.setEnabled(isSelected);
                bgSetupColourButton.setEnabled(isSelected);

                setupBranchDecorators();
            }
        });

        new AttributeComboHelper(branchWidthAttributeCombo, treeViewer, FIXED, true, false).addListener(new AttributeComboHelperListener() {
            @Override
            public void attributeComboChanged() {
                boolean isSelected = branchColourAttributeCombo.getSelectedIndex() != 0;
                label.setEnabled(isSelected);
                branchMinLineWidthSpinner.setEnabled(isSelected);
                setupBranchDecorators();
            }
        });

        branchLineWidthSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                float lineWidth = ((Double) branchLineWidthSpinner.getValue()).floatValue();
                treeViewer.setBranchStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                setupBranchDecorators();
            }
        });
        branchMinLineWidthSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                setupBranchDecorators();
            }
        });

    }

    private void setupBranchDecorators() {

        Decorator colourDecorator = colourController.getColourDecorator(branchColourAttributeCombo, userBranchColourDecorator);


//                if (colourDecorator == null) {
//                    if (attribute.endsWith("*")) {
//                        // This is a branch colouring (i.e., the colour can change
//                        // along the length of the branch...
//                        treeViewer.setBranchColouringDecorator(
//                                attribute.substring(0, attribute.length() - 2),
//                                new DiscreteColourDecorator());
//                        return;

//        if (colourDecorator != null && colourDecorator.isGradient()) {
//            // At present using a gradient precludes the use of the compoundDecorator
//            // and thus the branch width..
//            treeViewer.setBranchDecorator(colourDecorator);
//            return;
//        }

        CompoundDecorator compoundDecorator = new CompoundDecorator();

        if (colourDecorator != null) {
            treeViewer.setBranchColouringDecorator(null, null);
            compoundDecorator.addDecorator(colourDecorator);
        }

        if (branchWidthAttributeCombo.getSelectedIndex() > 0) {
            String attribute = (String) branchWidthAttributeCombo.getSelectedItem();
            ContinuousScale widthScale = new ContinuousScale(attribute, treeViewer.getTrees().get(0).getNodes());

            double fromWidth = (Double) branchMinLineWidthSpinner.getValue();
            double toWidth = (Double)branchLineWidthSpinner.getValue() + fromWidth;
            compoundDecorator.addDecorator(new ContinuousStrokeDecorator(
                    widthScale, (float)fromWidth, (float)toWidth)
            );
        }

        treeViewer.setBranchDecorator(compoundDecorator, branchColourIsGradient);

        colourDecorator = null;
        if (backgroundColourAttributeCombo.getSelectedIndex() > 1) {
            colourDecorator = colourController.getColourDecorator(backgroundColourAttributeCombo, null);
        }
        treeViewer.setNodeBackgroundDecorator(colourDecorator);

    }


    public JComponent getTitleComponent() {
        return titleLabel;
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

    public void setSettings(Map<String, Object> settings) {
        // These settings don't have controls yet but they will!
        treeViewer.setForeground((Color)settings.get(CONTROLLER_KEY + "." + FOREGROUND_COLOUR_KEY));
        treeViewer.setBackground((Color)settings.get(CONTROLLER_KEY + "." + BACKGROUND_COLOUR_KEY));
        treeViewer.setSelectionColor((Color)settings.get(CONTROLLER_KEY + "." + SELECTION_COLOUR_KEY));

        branchColourAttributeCombo.setSelectedItem(settings.get(CONTROLLER_KEY+"."+BRANCH_COLOR_ATTRIBUTE_KEY));
        branchColourGradientCheck.setSelected((Boolean)settings.get(CONTROLLER_KEY+"."+BRANCH_COLOR_GRADIENT_KEY));
        hilightingGradientCheck.setSelected((Boolean)settings.get(CONTROLLER_KEY+"."+HILIGHTING_GRADIENT_KEY));
        backgroundColourAttributeCombo.setSelectedItem(settings.get(CONTROLLER_KEY + "." + BACKGROUND_COLOR_ATTRIBUTE_KEY));
        branchLineWidthSpinner.setValue((Double) settings.get(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY));
        branchWidthAttributeCombo.setSelectedItem(settings.get(CONTROLLER_KEY+"."+BRANCH_WIDTH_ATTRIBUTE_KEY));
        branchMinLineWidthSpinner.setValue((Double) settings.get(CONTROLLER_KEY + "." + BRANCH_MIN_LINE_WIDTH_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        // These settings don't have controls yet but they will!
        settings.put(CONTROLLER_KEY + "." + FOREGROUND_COLOUR_KEY, treeViewer.getForeground());
        settings.put(CONTROLLER_KEY + "." + BACKGROUND_COLOUR_KEY, treeViewer.getBackground());
        settings.put(CONTROLLER_KEY + "." + SELECTION_COLOUR_KEY, treeViewer.getSelectionPaint());

        settings.put(CONTROLLER_KEY + "." + BRANCH_COLOR_ATTRIBUTE_KEY, branchColourAttributeCombo.getSelectedItem().toString());
        settings.put(CONTROLLER_KEY + "." + BRANCH_COLOR_GRADIENT_KEY, branchColourGradientCheck.isSelected());
        settings.put(CONTROLLER_KEY + "." + HILIGHTING_GRADIENT_KEY, hilightingGradientCheck.isSelected());
        settings.put(CONTROLLER_KEY + "." + BACKGROUND_COLOR_ATTRIBUTE_KEY, backgroundColourAttributeCombo.getSelectedItem().toString());
        settings.put(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY, branchLineWidthSpinner.getValue());
        settings.put(CONTROLLER_KEY + "." + BRANCH_WIDTH_ATTRIBUTE_KEY, branchWidthAttributeCombo.getSelectedItem().toString());
        settings.put(CONTROLLER_KEY + "." + BRANCH_MIN_LINE_WIDTH_KEY, branchMinLineWidthSpinner.getValue());
    }

    private final AttributeColourController colourController;

    private final AttributableDecorator userBranchColourDecorator;

    private final JLabel titleLabel;
    private final OptionsPanel optionsPanel;

    private final JComboBox branchColourAttributeCombo;
    private final JCheckBox branchColourGradientCheck;
    private final JCheckBox hilightingGradientCheck;
    private final JComboBox backgroundColourAttributeCombo;
    private final JSpinner branchLineWidthSpinner;
    private final JSpinner branchMinLineWidthSpinner;

    private final JComboBox branchWidthAttributeCombo;

    private final TreeViewer treeViewer;

    private boolean branchColourIsGradient = false;


}
