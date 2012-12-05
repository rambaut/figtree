package figtree.treeviewer;

import jebl.evolution.trees.Tree;
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

/**
 * @author Andrew Rambaut
 * @version $Id: MultiPaneTreeViewerController.java 760 2007-08-21 00:05:45Z rambaut $
 */
public class MultiPaneTreeViewerController extends AbstractController {

	public MultiPaneTreeViewerController(final MultiPaneTreeViewer treeViewer) {

		titleLabel = new JLabel("Current Tree");

		optionsPanel = new ControllerOptionsPanel(2, 2);

		final JLabel treeNameLabel = new JLabel("Tree 1");
		final SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 1, 1);
		JSpinner currentTreeSpinner = new JSpinner(spinnerModel);

		currentTreeSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				treeViewer.showTree((Integer)spinnerModel.getValue() - 1);
			}
		});

		final JComboBox treesPerPageCombo = new JComboBox(new String[] { "1", "2", "3", "4", "5", "6", "7", "8" });
		treesPerPageCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                treeViewer.setTreesPerPage(treesPerPageCombo.getSelectedIndex() + 1);
            }
        });


		treeViewer.addTreeViewerListener(new TreeViewerListener() {
			public void treeChanged() {
				int index = treeViewer.getCurrentTreeIndex() + 1;
				int treeCount = treeViewer.getTrees().size();
				Tree tree = treeViewer.getCurrentTree();
				spinnerModel.setValue(index);
				spinnerModel.setMaximum(treeCount);
				String name = (String)tree.getAttribute("name");
				if (name != null) {
					treeNameLabel.setText(name);
				} else {
					treeNameLabel.setText("Tree " + index);
				}
				titleLabel.setText("Current Tree: " + index + " / " + treeCount);
			}

            public void treeSettingsChanged() {
                // nothing to do
            }
        });
		optionsPanel.addComponentWithLabel("Name:", treeNameLabel);
		optionsPanel.addComponentWithLabel("Tree:", currentTreeSpinner);
		optionsPanel.addComponentWithLabel("Trees per page:", treesPerPageCombo);

	}

	public JComponent getTitleComponent() {
		return titleLabel;
	}

	public JPanel getPanel() {
		return optionsPanel;
	}

	public boolean isInitiallyVisible() {
		return true;
	}

	public void initialize() {
		// nothing to do
	}

	public void setSettings(Map<String,Object> settings) {
	}

	public void getSettings(Map<String, Object> settings) {
	}

	private final JLabel titleLabel;
	private final OptionsPanel optionsPanel;

}
