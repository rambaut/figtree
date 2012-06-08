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
public class ColourControllerHelper {

    public ColourControllerHelper(final JFrame frame, final Decorator defaultDecorator,
                                  final JComboBox colourAttributeCombo,
                                  final JButton colourSetupButton) {
        this.defaultDecorator = defaultDecorator;
        this.colourAttributeCombo = colourAttributeCombo;
        this.colourSetupButton = colourSetupButton;

        setupAttributes(treeViewer.getTrees());

        colourSetupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Decorator decorator = null;
                if (colourAttributeCombo.getSelectedIndex() > 0) {
                    String attribute = (String) colourAttributeCombo.getSelectedItem();
                    decorator = treeViewer.getDecoratorForAttribute(attribute);
                }

                if (decorator instanceof HSBDiscreteColorDecorator) {
                    if (discreteColourScaleDialog == null) {
                        discreteColourScaleDialog = new DiscreteColourScaleDialog(frame);
                    }
                    discreteColourScaleDialog.setDecorator((HSBDiscreteColorDecorator)decorator);
                    int result = discreteColourScaleDialog.showDialog();
                    if (result != JOptionPane.CANCEL_OPTION && result != JOptionPane.CLOSED_OPTION) {
                        discreteColourScaleDialog.setupDecorator((HSBDiscreteColorDecorator)decorator);
                        setupBranchDecorators();
                    }
                } else if (decorator instanceof HSBContinuousColorDecorator) {
                    if (continuousColourScaleDialog == null) {
                        continuousColourScaleDialog = new ContinuousColourScaleDialog(frame);
                    }
                    continuousColourScaleDialog.setDecorator((HSBContinuousColorDecorator)decorator);
                    int result = continuousColourScaleDialog.showDialog();
                    if (result != JOptionPane.CANCEL_OPTION && result != JOptionPane.CLOSED_OPTION) {
                        continuousColourScaleDialog.setupDecorator((HSBContinuousColorDecorator)decorator);
                        setupBranchDecorators();
                    }
                } else {
                    throw new IllegalArgumentException("Unsupported decorator type");
//                    if (continuousColourScaleDialog == null) {
//                        continuousColourScaleDialog = new OldContinuousColourScaleDialog(frame, branchColourSettings);
//                    }
//                    int result = continuousColourScaleDialog.showDialog();
//                    if (result != JOptionPane.CANCEL_OPTION && result != JOptionPane.CLOSED_OPTION) {
//                        continuousColourScaleDialog.getSettings(branchColourSettings);
//                        setupBranchDecorators();
//                    }
                }

            }
        });
        optionsPanel.addComponentWithLabel("Colour by:", colourAttributeCombo);
        optionsPanel.addComponent(setupColourButton);
        optionsPanel.addSeparator();

        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                setupBranchDecorators();
            }
        };

        colourAttributeCombo.addActionListener(listener);

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

    public Decorator getColourDecorator() {

        Set<Node> nodes = new HashSet<Node>();
        for (Tree tree : treeViewer.getTrees()) {
            for (Node node : tree.getNodes()) {
                nodes.add(node);
            }
        }

        Decorator colourDecorator = null;

        if (colourAttributeCombo.getSelectedIndex() == 0) {
            colourDecorator = defaultDecorator;
        } else {
            String attribute = (String) colourAttributeCombo.getSelectedItem();
            if (attribute != null && attribute.length() > 0) {
                colourDecorator = treeViewer.getDecoratorForAttribute(attribute);
                if (colourDecorator == null) {
                    if (attribute.endsWith("*")) {
                        // This is a branch colouring (i.e., the colour can change
                        // along the length of the branch...
                        treeViewer.setBranchColouringDecorator(
                                attribute.substring(0, attribute.length() - 2),
                                new DiscreteColorDecorator());
                        return;
                    } else if (DiscreteColorDecorator.isDiscrete(attribute, nodes)) {
                        colourDecorator = new HSBDiscreteColorDecorator(attribute, nodes, branchColourIsGradient);
                    } else {
                        ContinuousScale scale;
                        if (colourSettings.autoRange) {
                            scale = new ContinuousScale(attribute, nodes);
                        } else {
                            scale = new ContinuousScale(attribute, nodes, colourSettings.fromValue, colourSettings.toValue);
                        }

                        colourDecorator = new HSBContinuousColorDecorator(scale);

//                        if (branchColourSettings.middleColour == null) {
//                            colourDecorator = new ContinuousColorDecorator(scale, branchColourSettings.fromColour, branchColourSettings.toColour, branchColourIsGradient);
//                        } else {
//                            colourDecorator = new ContinuousColorDecorator(scale, branchColourSettings.fromColour, branchColourSettings.middleColour, branchColourSettings.toColour, branchColourIsGradient);
//                        }

                    }
                    treeViewer.setDecoratorForAttribute(attribute, colourDecorator);
                }
                if (colourDecorator instanceof DiscreteColorDecorator) {
                    ((DiscreteColorDecorator)colourDecorator).setGradient(branchColourIsGradient);
                } else if (colourDecorator instanceof ContinuousColorDecorator) {
                    ((ContinuousColorDecorator)colourDecorator).setGradient(branchColourIsGradient);
                }

                treeViewer.setDecoratorForAttribute(attribute, colourDecorator);
            }
        }

        return colourDecorator;
    }


    private void setupAttributes(Collection<? extends Tree> trees) {
        Object selected = colourAttributeCombo.getSelectedItem();

        colourAttributeCombo.removeAllItems();

        colourAttributeCombo.addItem("User Selection");
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
            colourAttributeCombo.addItem(name);
        }

        colourAttributeCombo.setSelectedItem(selected);
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

    private final Decorator defaultDecorator;
    private final JComboBox colourAttributeCombo;
    private final JButton colourSetupButton;

    private ContinuousColourScaleDialog continuousColourScaleDialog = null;
    private DiscreteColourScaleDialog discreteColourScaleDialog = null;

    private OldContinuousColourScaleDialog.ColourSettings colourSettings = new OldContinuousColourScaleDialog.ColourSettings();

}
