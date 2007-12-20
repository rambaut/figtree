package figtree.treeviewer;

import jebl.evolution.trees.Tree;
import jebl.evolution.graphs.Node;
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
import java.util.prefs.Preferences;

import figtree.treeviewer.decorators.*;

/**
 * @author Andrew Rambaut
 * @version $Id: TreeAppearanceController.java 822 2007-10-26 13:50:26Z rambaut $
 */
public class TreeAppearanceController extends AbstractController {

    private static final String CONTROLLER_TITLE = "Appearance";

    private static Preferences PREFS = Preferences.userNodeForPackage(TreeAppearanceController.class);

    private static final String CONTROLLER_KEY = "appearance";

    private static final String FOREGROUND_COLOUR_KEY = "foregroundColour";
    private static final String BACKGROUND_COLOUR_KEY = "backgroundColour";
    private static final String SELECTION_COLOUR_KEY = "selectionColour";
    private static final String BRANCH_COLOR_ATTRIBUTE_KEY = "branchColorAttribute";
    private static final String BRANCH_LINE_WIDTH_KEY = "branchLineWidth";

	private static final String BRANCH_WIDTH_ATTRIBUTE_KEY = "branchWidthAttribute";

    // The defaults if there is nothing in the preferences
    private static Color DEFAULT_FOREGROUND_COLOUR = Color.BLACK;
    private static Color DEFAULT_BACKGROUND_COLOUR = Color.WHITE;
    private static Color DEFAULT_SELECTION_COLOUR = new Color(180, 213, 254);
    private static float DEFAULT_BRANCH_LINE_WIDTH = 1.0f;

    public TreeAppearanceController(final TreeViewer treeViewer) {
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
        treeViewer.setSelectionPaint(new Color(selectionRGB));
        treeViewer.setBranchStroke(new BasicStroke(branchLineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        titleLabel = new JLabel(CONTROLLER_TITLE);

	    optionsPanel = new ControllerPanel(2, 2);

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
	    setupAttributes(treeViewer.getTrees());

	    optionsPanel.addComponentWithLabel("Width by:", branchWidthAttributeCombo);
	    optionsPanel.addComponentWithLabel("Colour by:", branchColourAttributeCombo);


        branchWidthAttributeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
				setupBranchDecorators();
            }
        });

	    branchColourAttributeCombo.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent itemEvent) {
		        setupBranchDecorators();
	        }
	    });

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
		CompoundDecorator compoundDecorator = new CompoundDecorator();

		Set<Node> nodes = new HashSet<Node>();
		for (Tree tree : treeViewer.getTrees()) {
		    for (Node node : tree.getNodes()) {
		        nodes.add(node);
		    }
		}

		if (branchColourAttributeCombo.getSelectedIndex() == 0) {
		    treeViewer.setBranchColouringDecorator(null, null);
			compoundDecorator.addDecorator(userBranchColourDecorator);
		} else {
		    String attribute = (String) branchColourAttributeCombo.getSelectedItem();
		    if (attribute != null && attribute.length() > 0) {
		        if (attribute.endsWith("*")) {
		            Decorator decorator = new DiscreteColorDecorator();

		            treeViewer.setBranchColouringDecorator(attribute.substring(0, attribute.length() - 2), decorator);
		        } else if (DiscreteColorDecorator.isDiscrete(attribute, nodes)) {
			        treeViewer.setBranchColouringDecorator(null, null);
			        compoundDecorator.addDecorator(new DiscreteColorDecorator(attribute, nodes));
		        } else {
			        treeViewer.setBranchColouringDecorator(null, null);

		            compoundDecorator.addDecorator(new ContinuousGradientColorDecorator(
		                    new ContinousScale(attribute, nodes, false, false),
		                    new Color(192, 16, 0), new Color(0, 16, 192))
			        );

		        }
		    }
		}

		if (branchWidthAttributeCombo.getSelectedIndex() > 0) {
		    String attribute = (String) branchWidthAttributeCombo.getSelectedItem();
		    if (attribute != null && attribute.length() > 0) {
		        if (!DiscreteColorDecorator.isDiscrete(attribute, nodes)) {
		            compoundDecorator.addDecorator(new ContinuousStrokeDecorator(
		                    new ContinousScale(attribute, nodes, false, false),
		                    1.0F, 5.0F)
			        );

		        }
		    }
		}

		treeViewer.setBranchDecorator(compoundDecorator);

		// To apply a gradient decorator - not fully integrated with compound decorator

//		Set<Node> nodes = new HashSet<Node>();
//		for (Tree tree : treeViewer.getTrees()) {
//		    for (Node node : tree.getNodes()) {
//		        nodes.add(node);
//		    }
//		}
//
//		if (branchColourAttributeCombo.getSelectedIndex() == 0) {
//		    treeViewer.setBranchColouringDecorator(null, null);
//			treeViewer.setBranchDecorator(userBranchColourDecorator);
//		} else {
//		    String attribute = (String) branchColourAttributeCombo.getSelectedItem();
//		    if (attribute != null && attribute.length() > 0) {
//		        if (attribute.endsWith("*")) {
//		            Decorator decorator = new DiscreteColorDecorator();
//
//		            treeViewer.setBranchColouringDecorator(attribute.substring(0, attribute.length() - 2), decorator);
//		        } else if (DiscreteColorDecorator.isDiscrete(attribute, nodes)) {
//			        treeViewer.setBranchColouringDecorator(null, null);
//			        treeViewer.setBranchDecorator(new DiscreteColorDecorator(attribute, nodes));
//		        } else {
//			        treeViewer.setBranchColouringDecorator(null, null);
//
//		            treeViewer.setBranchDecorator(new ContinuousGradientColorDecorator(
//		                    new ContinousScale(attribute, nodes, false, false),
//		                    new Color(192, 16, 0), new Color(0, 16, 192))
//			        );
//
//		        }
//		    }
//		}

	}

    private void setupAttributes(Collection<? extends Tree> trees) {
        Object selected1 = branchColourAttributeCombo.getSelectedItem();
	    Object selected2 = branchWidthAttributeCombo.getSelectedItem();

        branchColourAttributeCombo.removeAllItems();
	    branchWidthAttributeCombo.removeAllItems();
        branchColourAttributeCombo.addItem("User Selection");
	    branchWidthAttributeCombo.addItem("User Selection");
        if (trees == null) {
            return;
        }
        for (Tree tree : trees) {
            for (String name : getAttributeNames(tree.getNodes())) {
                branchColourAttributeCombo.addItem(name);
	            branchWidthAttributeCombo.addItem(name);
            }
        }
        branchColourAttributeCombo.setSelectedItem(selected1);
	    branchWidthAttributeCombo.setSelectedItem(selected2);
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
        treeViewer.setSelectionPaint((Color)settings.get(CONTROLLER_KEY + "." + SELECTION_COLOUR_KEY));

        branchColourAttributeCombo.setSelectedItem(settings.get(CONTROLLER_KEY+"."+BRANCH_COLOR_ATTRIBUTE_KEY));
        branchLineWidthSpinner.setValue((Double)settings.get(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        // These settings don't have controls yet but they will!
        settings.put(CONTROLLER_KEY + "." + FOREGROUND_COLOUR_KEY, treeViewer.getForeground());
        settings.put(CONTROLLER_KEY + "." + BACKGROUND_COLOUR_KEY, treeViewer.getBackground());
        settings.put(CONTROLLER_KEY + "." + SELECTION_COLOUR_KEY, treeViewer.getSelectionPaint());

        settings.put(CONTROLLER_KEY + "." + BRANCH_COLOR_ATTRIBUTE_KEY, branchColourAttributeCombo.getSelectedItem().toString());
        settings.put(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY, branchLineWidthSpinner.getValue());
    }

	private final AttributableDecorator userBranchColourDecorator;

    private final JLabel titleLabel;
    private final OptionsPanel optionsPanel;

    private final JComboBox branchColourAttributeCombo;
    private final JSpinner branchLineWidthSpinner;

	private final JComboBox branchWidthAttributeCombo;

    private final TreeViewer treeViewer;
}
