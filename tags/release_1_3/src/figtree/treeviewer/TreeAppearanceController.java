package figtree.treeviewer;

import jebl.evolution.trees.Tree;
import jebl.evolution.graphs.Node;
import jebl.util.Attributable;
import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
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
    public static final String BACKGROUND_COLOR_ATTRIBUTE_KEY = "backgroundColorAttribute";
    public static final String BRANCH_LINE_WIDTH_KEY = "branchLineWidth";
    public static final String BRANCH_WIDTH_ATTRIBUTE_KEY = "branchWidthAttribute";

    // The defaults if there is nothing in the preferences
    public static Color DEFAULT_FOREGROUND_COLOUR = Color.BLACK;
    public static Color DEFAULT_BACKGROUND_COLOUR = Color.WHITE;
    public static Color DEFAULT_SELECTION_COLOUR = new Color(45, 54, 128);
    public static float DEFAULT_BRANCH_LINE_WIDTH = 1.0f;

    public TreeAppearanceController(final TreeViewer treeViewer, final JFrame frame) {
        this.treeViewer = treeViewer;

        userBranchColourDecorator = new AttributableDecorator();
        userBranchColourDecorator.setPaintAttributeName("!color");
        userBranchColourDecorator.setStrokeAttributeName("!stroke");
        treeViewer.setBranchDecorator(userBranchColourDecorator);

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

        branchLineWidthSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 48.0, 1.0));

        branchLineWidthSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                float lineWidth = ((Double) branchLineWidthSpinner.getValue()).floatValue();
                treeViewer.setBranchStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
            }
        });
        optionsPanel.addComponentWithLabel("Line Weight:", branchLineWidthSpinner);

        branchWidthAttributeCombo = new JComboBox(new String[] { "No attributes" });
        branchColourAttributeCombo = new JComboBox(new String[] { "No attributes" });
        backgroundColourAttributeCombo = new JComboBox(new String[] { "No attributes" });
        setupAttributes(treeViewer.getTrees());

        branchColourSettings.autoRange  = true;
        branchColourSettings.fromValue = 0.0;
        branchColourSettings.toValue = 1.0;
        branchColourSettings.fromColour = new Color(0, 16, 192);
        branchColourSettings.toColour = new Color(192, 16, 0);
        branchColourSettings.middleColour = new Color(0, 0, 0);
        branchColourSettings.useGradient = false;

        JButton setupColourButton = new JButton(new AbstractAction("Setup") {
            public void actionPerformed(ActionEvent e) {
                if (colourScaleDialog == null) {
                    colourScaleDialog = new ColourScaleDialog(frame, branchColourSettings);
                }
                int result = colourScaleDialog.showDialog();
                if (result != JOptionPane.CANCEL_OPTION && result != JOptionPane.CLOSED_OPTION) {
                    colourScaleDialog.getSettings(branchColourSettings);
                    setupBranchDecorators();
                }
            }
        });
        final JCheckBox useGradientCheck = new JCheckBox("Gradient");
        useGradientCheck.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                branchColourSettings.useGradient = useGradientCheck.isSelected();
                setupBranchDecorators();
            }
        });
        optionsPanel.addComponentWithLabel("Colour by:", branchColourAttributeCombo);
        optionsPanel.addComponent(setupColourButton);
        optionsPanel.addComponent(useGradientCheck);
        optionsPanel.addSeparator();

        widthAutoRange = true;
        widthFromValue = 0.0;
        widthToValue = 1.0;
        fromWidth = 1.0;
        toWidth = 10.0;

        JButton setupWidthButton = new JButton(new AbstractAction("Setup") {
            public void actionPerformed(ActionEvent e) {
                if (widthScaleDialog == null) {
                    widthScaleDialog = new WidthScaleDialog(frame, widthAutoRange,
                            widthFromValue, widthToValue,
                            fromWidth, toWidth);
                }
                int result = widthScaleDialog.showDialog();
                if (result != JOptionPane.CANCEL_OPTION) {
                    widthAutoRange = widthScaleDialog.getAutoRange();
                    widthFromValue = widthScaleDialog.getFromValue().doubleValue();
                    widthToValue = widthScaleDialog.getToValue().doubleValue();
                    fromWidth = widthScaleDialog.getFromWidth().doubleValue();
                    toWidth = widthScaleDialog.getToWidth().doubleValue();
                    setupBranchDecorators();
                }
            }
        });
        optionsPanel.addComponentWithLabel("Width by:", branchWidthAttributeCombo);
        optionsPanel.addComponent(setupWidthButton);
        optionsPanel.addSeparator();

        backgroundColourSettings.autoRange  = true;
        backgroundColourSettings.fromValue = 0.0;
        backgroundColourSettings.toValue = 1.0;
        backgroundColourSettings.fromColour = new Color(255, 255, 255);
        backgroundColourSettings.toColour = new Color(192, 16, 0);
        backgroundColourSettings.middleColour = null;
        backgroundColourSettings.useGradient = false;

        JButton bgSetupColourButton = new JButton(new AbstractAction("Setup") {
            public void actionPerformed(ActionEvent e) {
                if (bgColourScaleDialog == null) {
                    bgColourScaleDialog = new ColourScaleDialog(frame, backgroundColourSettings);
                }
                int result = bgColourScaleDialog.showDialog();
                if (result != JOptionPane.CANCEL_OPTION && result != JOptionPane.CLOSED_OPTION) {
                    bgColourScaleDialog.getSettings(backgroundColourSettings);
                    setupBranchDecorators();
                }
            }
        });

        optionsPanel.addComponentWithLabel("Background:", backgroundColourAttributeCombo);
        optionsPanel.addComponent(bgSetupColourButton);

       ItemListener listener = new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                setupBranchDecorators();
            }
       };

        branchColourAttributeCombo.addItemListener(listener);
        branchWidthAttributeCombo.addItemListener(listener);
        backgroundColourAttributeCombo.addItemListener(listener);

        treeViewer.addTreeViewerListener(new TreeViewerListener() {
            public void treeChanged() {
                setupAttributes(treeViewer.getTrees());
                optionsPanel.repaint();
            }

            public void treeSettingsChanged() {
                // nothing to do
            }
        });
    }

    private void setupBranchDecorators() {

        Set<Node> nodes = new HashSet<Node>();
        for (Tree tree : treeViewer.getTrees()) {
            for (Node node : tree.getNodes()) {
                nodes.add(node);
            }
        }

        Decorator colourDecorator = null;

        if (backgroundColourAttributeCombo.getSelectedIndex() > 0) {
            String attribute = (String) backgroundColourAttributeCombo.getSelectedItem();
            if (attribute != null && attribute.length() > 0) {
                if (DiscreteColorDecorator.isDiscrete(attribute, nodes)) {
                    colourDecorator = new DiscreteColorDecorator(attribute, nodes, false);
                } else {
                    ContinousScale scale;
                    if (branchColourSettings.autoRange) {
                        scale = new ContinousScale(attribute, nodes);
                    } else {
                        scale = new ContinousScale(attribute, nodes, backgroundColourSettings.fromValue, backgroundColourSettings.toValue);
                    }

                    if (backgroundColourSettings.middleColour == null) {
                        colourDecorator = new ContinuousColorDecorator(scale, backgroundColourSettings.fromColour, backgroundColourSettings.toColour, false);
                    } else {
                        colourDecorator = new ContinuousColorDecorator(scale, backgroundColourSettings.fromColour, backgroundColourSettings.middleColour, backgroundColourSettings.toColour, false);
                    }

                }
            }
        }

        treeViewer.setNodeBackgroundDecorator(colourDecorator);

        if (branchColourAttributeCombo.getSelectedIndex() == 0) {
            colourDecorator = userBranchColourDecorator;
            userBranchColourDecorator.setGradient(branchColourSettings.useGradient);
        } else {
            String attribute = (String) branchColourAttributeCombo.getSelectedItem();
            if (attribute != null && attribute.length() > 0) {
                if (attribute.endsWith("*")) {
                    // This is a branch colouring (i.e., the colour can change
                    // along the length of the branch...
                    treeViewer.setBranchColouringDecorator(
                            attribute.substring(0, attribute.length() - 2),
                            new DiscreteColorDecorator());
                    return;
                } else if (DiscreteColorDecorator.isDiscrete(attribute, nodes)) {

                    colourDecorator = new DiscreteColorDecorator(attribute, nodes, branchColourSettings.useGradient);
                } else {
                    ContinousScale scale;
                    if (branchColourSettings.autoRange) {
                        scale = new ContinousScale(attribute, nodes);
                    } else {
                        scale = new ContinousScale(attribute, nodes, branchColourSettings.fromValue, branchColourSettings.toValue);
                    }

                    if (branchColourSettings.middleColour == null) {
                        colourDecorator = new ContinuousColorDecorator(scale, branchColourSettings.fromColour, branchColourSettings.toColour, branchColourSettings.useGradient);
                    } else {
                        colourDecorator = new ContinuousColorDecorator(scale, branchColourSettings.fromColour, branchColourSettings.middleColour, branchColourSettings.toColour, branchColourSettings.useGradient);
                    }

                }
            }
        }

        if (colourDecorator != null && colourDecorator.isGradient()) {
            // At present using a gradient precludes the use of the compoundDecorator
            // and thus the branch width..
            treeViewer.setBranchDecorator(colourDecorator);
            return;
        }

        CompoundDecorator compoundDecorator = new CompoundDecorator();

        if (colourDecorator != null) {
            treeViewer.setBranchColouringDecorator(null, null);
            compoundDecorator.addDecorator(colourDecorator);
        }

        if (branchWidthAttributeCombo.getSelectedIndex() > 0) {
            String attribute = (String) branchWidthAttributeCombo.getSelectedItem();
            if (attribute != null && attribute.length() > 0) {
                if (!DiscreteColorDecorator.isDiscrete(attribute, nodes)) {
                    ContinousScale scale;
                    if (widthAutoRange) {
                        scale = new ContinousScale(attribute, nodes);
                    } else {
                        scale = new ContinousScale(attribute, nodes, widthFromValue, widthToValue);
                    }
                    compoundDecorator.addDecorator(new ContinuousStrokeDecorator(
                            scale, (float)fromWidth, (float)toWidth)
                    );

                }
            }
        }
        treeViewer.setBranchDecorator(compoundDecorator);

    }


    private void setupAttributes(Collection<? extends Tree> trees) {
        Object selected1 = branchColourAttributeCombo.getSelectedItem();
        Object selected2 = branchWidthAttributeCombo.getSelectedItem();
        Object selected3 = backgroundColourAttributeCombo.getSelectedItem();

        branchColourAttributeCombo.removeAllItems();
        branchWidthAttributeCombo.removeAllItems();
        backgroundColourAttributeCombo.removeAllItems();

        branchColourAttributeCombo.addItem("User Selection");
        branchWidthAttributeCombo.addItem("User Selection");
        backgroundColourAttributeCombo.addItem("User Selection");
        if (trees == null) {
            return;
        }
        List<String> names = new ArrayList<String>();
        for (Tree tree : trees) {
            for (String name : getAttributeNames(tree.getNodes())) {
                if (!names.contains(name)) {
                    names.add(name);
                }
            }
        }

        for (String name : names) {
            branchColourAttributeCombo.addItem(name);
            branchWidthAttributeCombo.addItem(name);
            backgroundColourAttributeCombo.addItem(name);
        }

        branchColourAttributeCombo.setSelectedItem(selected1);
        branchWidthAttributeCombo.setSelectedItem(selected2);
        backgroundColourAttributeCombo.setSelectedItem(selected3);
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

    public void setSettings(Map<String,Object> settings) {
        // These settings don't have controls yet but they will!
        treeViewer.setForeground((Color)settings.get(CONTROLLER_KEY + "." + FOREGROUND_COLOUR_KEY));
        treeViewer.setBackground((Color)settings.get(CONTROLLER_KEY + "." + BACKGROUND_COLOUR_KEY));
        treeViewer.setSelectionColor((Color)settings.get(CONTROLLER_KEY + "." + SELECTION_COLOUR_KEY));

        branchColourAttributeCombo.setSelectedItem(settings.get(CONTROLLER_KEY+"."+BRANCH_COLOR_ATTRIBUTE_KEY));
        backgroundColourAttributeCombo.setSelectedItem(settings.get(CONTROLLER_KEY+"."+BACKGROUND_COLOR_ATTRIBUTE_KEY));
        branchLineWidthSpinner.setValue((Double)settings.get(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        // These settings don't have controls yet but they will!
        settings.put(CONTROLLER_KEY + "." + FOREGROUND_COLOUR_KEY, treeViewer.getForeground());
        settings.put(CONTROLLER_KEY + "." + BACKGROUND_COLOUR_KEY, treeViewer.getBackground());
        settings.put(CONTROLLER_KEY + "." + SELECTION_COLOUR_KEY, treeViewer.getSelectionPaint());

        settings.put(CONTROLLER_KEY + "." + BRANCH_COLOR_ATTRIBUTE_KEY, branchColourAttributeCombo.getSelectedItem().toString());
        settings.put(CONTROLLER_KEY + "." + BACKGROUND_COLOR_ATTRIBUTE_KEY, backgroundColourAttributeCombo.getSelectedItem().toString());
        settings.put(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY, branchLineWidthSpinner.getValue());
    }

    private final AttributableDecorator userBranchColourDecorator;

    private final JLabel titleLabel;
    private final OptionsPanel optionsPanel;

    private final JComboBox branchColourAttributeCombo;
    private final JComboBox backgroundColourAttributeCombo;
    private final JSpinner branchLineWidthSpinner;

    private final JComboBox branchWidthAttributeCombo;

    private final TreeViewer treeViewer;

    private ColourScaleDialog colourScaleDialog = null;
    private ColourScaleDialog bgColourScaleDialog = null;
    private WidthScaleDialog widthScaleDialog = null;

    private ColourScaleDialog.ColourSettings branchColourSettings = new ColourScaleDialog.ColourSettings();
    private ColourScaleDialog.ColourSettings backgroundColourSettings = new ColourScaleDialog.ColourSettings();

    private boolean widthAutoRange = true;
    private double widthFromValue = 0.0;
    private double widthToValue = 1.0;
    private double fromWidth;
    private double toWidth;


}
