package figtree.applet;

import jebl.evolution.trees.Tree;
import jebl.evolution.graphs.Node;
import jebl.gui.trees.treeviewer_dev.decorators.*;
import jebl.gui.trees.treeviewer_dev.TreeViewer;
import jebl.gui.trees.treeviewer_dev.TreeViewerListener;
import jebl.util.Attributable;
import org.virion.jam.controlpalettes.AbstractController;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;

/**
 * @author Andrew Rambaut
 * @version $Id: TreeAppearanceController.java,v 1.2 2007/09/10 14:52:02 rambaut Exp $
 */
public class TreeAppearanceController extends AbstractController {

    private static final String CONTROLLER_TITLE = "Appearance";

    private static final String CONTROLLER_KEY = "appearance";

    private static final String FOREGROUND_COLOUR_KEY = "foregroundColour";
    private static final String BACKGROUND_COLOUR_KEY = "backgroundColour";
    private static final String SELECTION_COLOUR_KEY = "selectionColour";
    private static final String BRANCH_COLOR_ATTRIBUTE_KEY = "branchColorAttribute";
    private static final String BRANCH_LINE_WIDTH_KEY = "branchLineWidth";

    // The defaults if there is nothing in the preferences
    private static Color DEFAULT_FOREGROUND_COLOUR = Color.BLACK;
    private static Color DEFAULT_BACKGROUND_COLOUR = Color.WHITE;
    private static Color DEFAULT_SELECTION_COLOUR = new Color(180, 213, 254);
    private static float DEFAULT_BRANCH_LINE_WIDTH = 1.0f;

    public TreeAppearanceController(final TreeViewer treeViewer) {
        this.treeViewer = treeViewer;

        final AttributableDecorator branchDecorator = new AttributableDecorator();
        branchDecorator.setPaintAttributeName("!color");
        branchDecorator.setStrokeAttributeName("!stroke");
        treeViewer.setBranchDecorator(branchDecorator);

        int foregroundRGB = DEFAULT_FOREGROUND_COLOUR.getRGB();
        int backgroundRGB = DEFAULT_BACKGROUND_COLOUR.getRGB();
        int selectionRGB = DEFAULT_SELECTION_COLOUR.getRGB();
        float branchLineWidth = DEFAULT_BRANCH_LINE_WIDTH;

        treeViewer.setForeground(new Color(foregroundRGB));
        treeViewer.setBackground(new Color(backgroundRGB));
        treeViewer.setSelectionPaint(new Color(selectionRGB));
        treeViewer.setBranchStroke(new BasicStroke(branchLineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        titleLabel = new JLabel(CONTROLLER_TITLE);

        optionsPanel = new OptionsPanel();

        branchLineWidthSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 48.0, 1.0));

        branchLineWidthSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                float lineWidth = ((Double) branchLineWidthSpinner.getValue()).floatValue();
                treeViewer.setBranchStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
            }
        });
        optionsPanel.addComponentWithLabel("Line Weight:", branchLineWidthSpinner);

        branchColorAttributeCombo = new JComboBox(new String[] { "No attributes" });
        setupAttributes(treeViewer.getTrees());
        branchColorAttributeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                if (branchColorAttributeCombo.getSelectedIndex() == 0) {
                    treeViewer.setBranchColouringDecorator(null, null);
                    treeViewer.setBranchDecorator(branchDecorator);
                } else {
                    Set<Node> nodes = new HashSet<Node>();
                    for (Tree tree : treeViewer.getTrees()) {
                        for (Node node : tree.getNodes()) {
                            nodes.add(node);
                        }
                    }
                    String attribute = (String)branchColorAttributeCombo.getSelectedItem();
                    if (attribute != null && attribute.length() > 0) {
                        if (attribute.endsWith("*")) {
                            Decorator decorator = new DiscreteColorDecorator();

                            treeViewer.setBranchColouringDecorator(attribute.substring(0, attribute.length() - 2), decorator);
                            treeViewer.setBranchDecorator(null);
                        } else if (DiscreteColorDecorator.isDiscrete(attribute, nodes)) {
	                        Decorator decorator = new DiscreteColorDecorator(attribute, nodes);

	                        treeViewer.setBranchColouringDecorator(null, null);
	                        treeViewer.setBranchDecorator(decorator);
                        } else {

                            Decorator decorator = new ContinuousColorDecorator(
		                            new ContinousScale(attribute, nodes, true, false),
                                    new Color(192, 16, 0), new Color(0, 16, 192));

                            treeViewer.setBranchColouringDecorator(null, null);
                            treeViewer.setBranchDecorator(decorator);
                        }
                    }
                }
            }
        });

        optionsPanel.addComponentWithLabel("Color by:", branchColorAttributeCombo);

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

    private void setupAttributes(Collection<? extends Tree> trees) {
        Object selected = branchColorAttributeCombo.getSelectedItem();

        branchColorAttributeCombo.removeAllItems();
        branchColorAttributeCombo.addItem("User Selection");
        if (trees == null) {
            return;
        }
        for (Tree tree : trees) {
            for (String name : getAttributeNames(tree.getNodes())) {
                branchColorAttributeCombo.addItem(name);
            }
        }
        branchColorAttributeCombo.setSelectedItem(selected);
    }

    private String[] getAttributeNames(Collection<? extends Attributable> items) {
        Set<String> attributeNames = new TreeSet<String>();

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
        treeViewer.setSelectionPaint((Color)settings.get(CONTROLLER_KEY + "." + SELECTION_COLOUR_KEY));

        branchColorAttributeCombo.setSelectedItem(settings.get(CONTROLLER_KEY+"."+BRANCH_COLOR_ATTRIBUTE_KEY));
        branchLineWidthSpinner.setValue((Double)settings.get(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        // These settings don't have controls yet but they will!
        settings.put(CONTROLLER_KEY + "." + FOREGROUND_COLOUR_KEY, treeViewer.getForeground());
        settings.put(CONTROLLER_KEY + "." + BACKGROUND_COLOUR_KEY, treeViewer.getBackground());
        settings.put(CONTROLLER_KEY + "." + SELECTION_COLOUR_KEY, treeViewer.getSelectionPaint());

        settings.put(CONTROLLER_KEY + "." + BRANCH_COLOR_ATTRIBUTE_KEY, branchColorAttributeCombo.getSelectedItem().toString());
        settings.put(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY, branchLineWidthSpinner.getValue());
    }


    private final JLabel titleLabel;
    private final OptionsPanel optionsPanel;

    private final JComboBox branchColorAttributeCombo;
    private final JSpinner branchLineWidthSpinner;

    private final TreeViewer treeViewer;
}