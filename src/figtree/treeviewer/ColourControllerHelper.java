package figtree.treeviewer;

import jam.controlpalettes.ControllerListener;
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

    public static final String CONTROLLER_KEY = "colour";

    public ColourControllerHelper(final JFrame frame, final Decorator defaultDecorator,
                                  final JComboBox colourAttributeCombo,
                                  final JButton colourSetupButton,
                                  final AttributeColourController colourController) {
        this.defaultDecorator = defaultDecorator;
        this.colourAttributeCombo = colourAttributeCombo;
        this.colourSetupButton = colourSetupButton;
        this.colourController = colourController;

        colourController.addControllerListener(new ControllerListener() {
            @Override
            public void controlsChanged() {
                setupAttributeCombo();
            }
        });

        colourSetupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Decorator decorator = null;
                if (colourAttributeCombo.getSelectedIndex() > 0) {
                    String attribute = (String) colourAttributeCombo.getSelectedItem();
                    decorator = colourController.getDecoratorForAttribute(attribute);
                }

                if (decorator instanceof HSBDiscreteColorDecorator) {
                    if (discreteColourScaleDialog == null) {
                        discreteColourScaleDialog = new DiscreteColourScaleDialog(frame);
                    }
                    discreteColourScaleDialog.setDecorator((HSBDiscreteColorDecorator)decorator);
                    int result = discreteColourScaleDialog.showDialog();
                    if (result != JOptionPane.CANCEL_OPTION && result != JOptionPane.CLOSED_OPTION) {
                        discreteColourScaleDialog.setupDecorator((HSBDiscreteColorDecorator)decorator);
                        fireColoursChanged();
                    }
                } else if (decorator instanceof HSBContinuousColorDecorator) {
                    if (continuousColourScaleDialog == null) {
                        continuousColourScaleDialog = new ContinuousColourScaleDialog(frame);
                    }
                    continuousColourScaleDialog.setDecorator((HSBContinuousColorDecorator)decorator);
                    int result = continuousColourScaleDialog.showDialog();
                    if (result != JOptionPane.CANCEL_OPTION && result != JOptionPane.CLOSED_OPTION) {
                        continuousColourScaleDialog.setupDecorator((HSBContinuousColorDecorator)decorator);
                        fireColoursChanged();
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

        colourAttributeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                colourAttributeChanged();
            }
        });
    }

    private void setupAttributeCombo() {

        Object selected = colourAttributeCombo.getSelectedItem();

        colourAttributeCombo.removeAllItems();

        colourAttributeCombo.addItem("User Selection");

        for (String name : colourController.getAttributeNames()) {
            colourAttributeCombo.addItem(name);
        }

        colourAttributeCombo.setSelectedItem(selected);
    }

    private void colourAttributeChanged() {
        Decorator colourDecorator = null;

        if (colourAttributeCombo.getSelectedIndex() == 0) {
            colourDecorator = defaultDecorator;
        } else {
            String attribute = (String) colourAttributeCombo.getSelectedItem();
            if (attribute != null && attribute.length() > 0) {
                colourDecorator = colourController.getDecoratorForAttribute(attribute);

                fireColoursChanged();
            }
        }

    }

    private void fireColoursChanged() {
        for (Listener listener : listeners) {
            listener.coloursChanged();
        }
    }

    public Decorator getColourDecorator() {
        return colourDecorator;
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private final List<Listener> listeners = new ArrayList<Listener>();

    private final Decorator defaultDecorator;
    private final JComboBox colourAttributeCombo;
    private final JButton colourSetupButton;
    private final AttributeColourController colourController;

    private Decorator colourDecorator;

    private ContinuousColourScaleDialog continuousColourScaleDialog = null;
    private DiscreteColourScaleDialog discreteColourScaleDialog = null;

    public interface Listener {
        void coloursChanged();
    }
}
