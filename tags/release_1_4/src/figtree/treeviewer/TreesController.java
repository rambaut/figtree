package figtree.treeviewer;

import jebl.evolution.trees.SortedRootedTree;
import jebl.evolution.trees.TransformedRootedTree;
import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * @author Andrew Rambaut
 * @version $Id: TreesController.java 780 2007-09-09 09:46:01Z rambaut $
 */
public class TreesController extends AbstractController {
	private static final String CONTROLLER_TITLE = "Trees";

    private static Preferences PREFS = Preferences.userNodeForPackage(TreeViewer.class);

	private static final String CONTROLLER_KEY = "trees";

    private static final String TRANSFORM_KEY = "transform";
    private static final String TRANSFORM_TYPE_KEY = "transformType";
    private static final String ORDER_KEY = "order";
    private static final String ORDER_TYPE_KEY = "orderType";
    private static final String ROOTING_KEY = "rooting";
    private static final String ROOTING_TYPE_KEY = "rootingType";



    public TreesController(final TreeViewer treeViewer) {
        this.treeViewer = treeViewer;

        titleLabel = new JLabel(CONTROLLER_TITLE);

	    optionsPanel = new ControllerOptionsPanel(2, 6);

        rootingCheck = new JCheckBox("Root tree");
        rootingCheck.setOpaque(false);
        optionsPanel.addSpanningComponent(rootingCheck);

        rootingCheck.setSelected(treeViewer.isRootingOn());

        rootingCombo = new JComboBox(TreePane.RootingType.values());
        rootingCombo.setOpaque(false);
        rootingCombo.setSelectedItem(treeViewer.getRootingType());
        rootingCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                treeViewer.setRootingType((TreePane.RootingType) rootingCombo.getSelectedItem());
            }
        });

        final JLabel label1 = optionsPanel.addComponentWithLabel("Rooting:", rootingCombo);
        label1.setEnabled(rootingCheck.isSelected());
        rootingCombo.setEnabled(rootingCheck.isSelected());

        rootingCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                label1.setEnabled(rootingCheck.isSelected());
                rootingCombo.setEnabled(rootingCheck.isSelected());

                treeViewer.setRootingOn(rootingCheck.isSelected());
            }
        });

        optionsPanel.addSeparator();

        orderCheck = new JCheckBox("Order nodes");
        orderCheck.setOpaque(false);
        optionsPanel.addSpanningComponent(orderCheck);

        orderCheck.setSelected(treeViewer.isOrderBranchesOn());

        orderCombo = new JComboBox(SortedRootedTree.BranchOrdering.values());
        orderCombo.setOpaque(false);
        orderCombo.setSelectedItem(treeViewer.getBranchOrdering());
        orderCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                treeViewer.setBranchOrdering(
                        (SortedRootedTree.BranchOrdering) orderCombo.getSelectedItem());
            }
        });

        final JLabel label2 = optionsPanel.addComponentWithLabel("Ordering:", orderCombo);
        label2.setEnabled(orderCheck.isSelected());
        orderCombo.setEnabled(orderCheck.isSelected());

        orderCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                label2.setEnabled(orderCheck.isSelected());
                orderCombo.setEnabled(orderCheck.isSelected());

                treeViewer.setOrderBranchesOn(orderCheck.isSelected());
            }
        });

        optionsPanel.addSeparator();

        transformCheck = new JCheckBox("Transform branches");
        transformCheck.setOpaque(false);
        optionsPanel.addSpanningComponent(transformCheck);

        transformCheck.setSelected(treeViewer.isTransformBranchesOn());

        transformCombo = new JComboBox(TransformedRootedTree.Transform.values());
        transformCombo.setOpaque(false);
        transformCombo.setSelectedItem(treeViewer.getBranchTransform());
        transformCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                treeViewer.setBranchTransform(
                        (TransformedRootedTree.Transform) transformCombo.getSelectedItem());

            }
        });
        final JLabel label3 = optionsPanel.addComponentWithLabel("Transform:", transformCombo);
         label3.setEnabled(transformCheck.isSelected());
        transformCombo.setEnabled(transformCheck.isSelected());

        transformCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final boolean selected = transformCheck.isSelected();
                 label3.setEnabled(selected);
                transformCombo.setEnabled(selected);

                treeViewer.setTransformBranchesOn(selected);
            }
        });

	    treeViewer.addTreeViewerListener(new TreeViewerListener() {

		    public void treeChanged() {
				// ignore this one
 		    }

		    // the settings may have changed programmatically - update the controls to match
		    public void treeSettingsChanged() {
			    rootingCheck.setSelected(treeViewer.isRootingOn());
			    rootingCombo.setSelectedItem(treeViewer.getRootingType());

			    transformCheck.setSelected(treeViewer.isTransformBranchesOn());
			    transformCombo.setSelectedItem(treeViewer.getBranchTransform());

			    orderCheck.setSelected(treeViewer.isOrderBranchesOn());
			    orderCombo.setSelectedItem(treeViewer.getBranchOrdering());
		    }
	    });

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
        treeViewer.setRootingOn(rootingCheck.isSelected());
        treeViewer.setRootingType((TreePane.RootingType) rootingCombo.getSelectedItem());
        treeViewer.setTransformBranchesOn(transformCheck.isSelected());
        treeViewer.setBranchTransform((TransformedRootedTree.Transform) transformCombo.getSelectedItem());
        treeViewer.setOrderBranchesOn(orderCheck.isSelected());
        treeViewer.setBranchOrdering((SortedRootedTree.BranchOrdering) orderCombo.getSelectedItem());
    }

	public void toggleMidpointRoot() {
		if (!rootingCheck.isSelected() || rootingCombo.getSelectedItem() != TreePane.RootingType.MID_POINT) {
			rootingCheck.setSelected(true);
			rootingCombo.setSelectedItem(TreePane.RootingType.MID_POINT);
        } else {
			rootingCombo.setSelectedItem(TreePane.RootingType.USER_ROOTING);
        }
        treeViewer.setRootingType((TreePane.RootingType) rootingCombo.getSelectedItem());
        treeViewer.setRootingOn(rootingCheck.isSelected());
    }

	public void toggleIncreasingNodeOrder() {
		if (!orderCheck.isSelected() || orderCombo.getSelectedItem() != SortedRootedTree.BranchOrdering.INCREASING_NODE_DENSITY) {
			orderCheck.setSelected(true);
			orderCombo.setSelectedItem(SortedRootedTree.BranchOrdering.INCREASING_NODE_DENSITY);
		} else {
			orderCheck.setSelected(false);
		}
        treeViewer.setBranchOrdering((SortedRootedTree.BranchOrdering) orderCombo.getSelectedItem());
        treeViewer.setOrderBranchesOn(orderCheck.isSelected());
    }

	public void toggleDecreasingNodeOrder() {
		if (!orderCheck.isSelected() || orderCombo.getSelectedItem() != SortedRootedTree.BranchOrdering.DECREASING_NODE_DENSITY) {
			orderCheck.setSelected(true);
			orderCombo.setSelectedItem(SortedRootedTree.BranchOrdering.DECREASING_NODE_DENSITY);
		} else {
			orderCheck.setSelected(false);
		}
        treeViewer.setBranchOrdering((SortedRootedTree.BranchOrdering) orderCombo.getSelectedItem());
        treeViewer.setOrderBranchesOn(orderCheck.isSelected());
    }

    public void setSettings(Map<String,Object> settings) {
        transformCheck.setSelected((Boolean) settings.get(CONTROLLER_KEY + "." + TRANSFORM_KEY));
        String transformName = (String)settings.get(CONTROLLER_KEY + "." + TRANSFORM_TYPE_KEY);
        for (TransformedRootedTree.Transform transform : TransformedRootedTree.Transform.values()) {
            if (transform.toString().equalsIgnoreCase(transformName)) {
                transformCombo.setSelectedItem(transform);
            }
        }

        orderCheck.setSelected((Boolean) settings.get(CONTROLLER_KEY + "." + ORDER_KEY));
        String orderName = (String)settings.get(CONTROLLER_KEY + "." + ORDER_TYPE_KEY);
        for (SortedRootedTree.BranchOrdering order : SortedRootedTree.BranchOrdering.values()) {
            if (order.toString().equalsIgnoreCase(orderName)) {
                orderCombo.setSelectedItem(order);
            }
        }

        rootingCheck.setSelected((Boolean) settings.get(CONTROLLER_KEY + "." + ROOTING_KEY));
        String rootingName = (String)settings.get(CONTROLLER_KEY + "." + ROOTING_TYPE_KEY);
        for (TreePane.RootingType rooting : TreePane.RootingType.values()) {
            if (rooting.toString().equalsIgnoreCase(rootingName)) {
                rootingCombo.setSelectedItem(rooting);
            }
        }
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(CONTROLLER_KEY + "." + TRANSFORM_KEY, transformCheck.isSelected());
        settings.put(CONTROLLER_KEY + "." + TRANSFORM_TYPE_KEY, transformCombo.getSelectedItem().toString());
        settings.put(CONTROLLER_KEY + "." + ORDER_KEY, orderCheck.isSelected());
        settings.put(CONTROLLER_KEY + "." + ORDER_TYPE_KEY, orderCombo.getSelectedItem().toString());
        settings.put(CONTROLLER_KEY + "." + ROOTING_KEY, rootingCheck.isSelected());
        settings.put(CONTROLLER_KEY + "." + ROOTING_TYPE_KEY, rootingCombo.getSelectedItem().toString());
    }


    private final JLabel titleLabel;
    private final OptionsPanel optionsPanel;

    private final JCheckBox transformCheck;
    private final JComboBox transformCombo;

    private final JCheckBox orderCheck;
    private final JComboBox orderCombo;

    private final JCheckBox rootingCheck;
    private final JComboBox rootingCombo;

    private final TreeViewer treeViewer;
}
