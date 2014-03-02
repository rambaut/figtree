/*
 * MultipleTreesController.java
 *
 * Copyright (C) 2006-2014 Andrew Rambaut
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package figtree.treeviewer;

import jebl.evolution.trees.Tree;
import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Map;

/**
 * @author Andrew Rambaut
 * @version $Id$
 *
 * $HeadURL$
 *
 * $LastChangedBy$
 * $LastChangedDate$
 * $LastChangedRevision$
 */
public class MultipleTreesController extends AbstractController {

	public MultipleTreesController(final TreeViewer treeViewer) {

		titleLabel = new JLabel("Current Tree");

		optionsPanel = new ControllerOptionsPanel(2, 2);

		final JLabel treeNameLabel = new JLabel("Tree 1");
		final SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 100000, 1);
		JSpinner currentTreeSpinner = new JSpinner(spinnerModel);
		currentTreeSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				treeViewer.showTree((Integer)spinnerModel.getValue() - 1);
			}
		});

		treeViewer.addTreeViewerListener(new TreeViewerListener() {
			public void treeChanged() {
				int index = treeViewer.getCurrentTreeIndex() + 1;
				int treeCount = treeViewer.getTrees().size();
                if (treeCount > 0) {
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
                } else {
                    titleLabel.setText("No trees");

                }
            }

            public void treeSettingsChanged() {
                // nothing to do
            }
        });
		optionsPanel.addComponentWithLabel("Name:", treeNameLabel);
		optionsPanel.addComponentWithLabel("Tree:", currentTreeSpinner);

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
	}

	public void getSettings(Map<String, Object> settings) {
	}

	private final JLabel titleLabel;
	private final OptionsPanel optionsPanel;

}
