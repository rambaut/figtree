/*
 * TreeColouringController.java
 *
 * Copyright (C) 2012 Andrew Rambaut
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

package figtree.panel;

import jebl.evolution.trees.Tree;
import jebl.evolution.graphs.Node;
import figtree.treeviewer.TreeViewer;
import figtree.treeviewer.TreeViewerListener;
import figtree.treeviewer.decorators.*;
import jebl.util.Attributable;
import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;

/**
 * @author Andrew Rambaut
 * @version $Id: TreeAppearanceController.java,v 1.2 2007/09/10 14:52:02 rambaut Exp $
 */
public class TreeColouringController extends AbstractController {

	private static final String CONTROLLER_KEY = "appearance";

	private static final String BRANCH_COLOR_ATTRIBUTE_KEY = "branchColorAttribute";

	public TreeColouringController(final TreeViewer treeViewer) {
		this(treeViewer, "Colour by:");
	}

	public TreeColouringController(final TreeViewer treeViewer, String label) {

		final AttributableDecorator branchDecorator = new AttributableDecorator();
		branchDecorator.setPaintAttributeName("!color");
		branchDecorator.setStrokeAttributeName("!stroke");
		treeViewer.setBranchDecorator(branchDecorator, false);

		optionsPanel = new OptionsPanel();

		branchColourAttributeCombo = new JComboBox(new String[] { "No attributes" });
		setupAttributes(treeViewer.getTrees());
		branchColourAttributeCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				Set<Node> nodes = new HashSet<Node>();
				for (Tree tree : treeViewer.getTrees()) {
					for (Node node : tree.getNodes()) {
						nodes.add(node);
					}
				}
				String attribute = (String) branchColourAttributeCombo.getSelectedItem();
				if (attribute != null && attribute.length() > 0) {
					if (attribute.endsWith("*")) {
						Decorator decorator = new DiscreteColourDecorator();

						treeViewer.setBranchColouringDecorator(attribute.substring(0, attribute.length() - 2), decorator);
						treeViewer.setBranchDecorator(null, false);
					} else if (DiscreteColourDecorator.isDiscrete(attribute, nodes)) {
						Decorator decorator = new HSBDiscreteColourDecorator(attribute, nodes);

						treeViewer.setBranchColouringDecorator(null, null);
						treeViewer.setBranchDecorator(decorator, false);
					} else {

						Decorator decorator = new InterpolatingColourDecorator(
								new ContinuousScale(attribute, nodes),
								new Color(192, 16, 0), new Color(0, 16, 192));

						treeViewer.setBranchColouringDecorator(null, null);
						treeViewer.setBranchDecorator(decorator, false);
					}
				}
			}
		});

		optionsPanel.addComponentWithLabel(label, branchColourAttributeCombo);

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
		Object selected = branchColourAttributeCombo.getSelectedItem();

		branchColourAttributeCombo.removeAllItems();
		//branchColourAttributeCombo.addItem("User Selection");
		if (trees == null) {
			return;
		}
		for (Tree tree : trees) {
			for (String name : getAttributeNames(tree.getNodes())) {
				branchColourAttributeCombo.addItem(name);
			}
		}
		branchColourAttributeCombo.setSelectedItem(selected);
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
		return null;
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
		branchColourAttributeCombo.setSelectedItem(settings.get(CONTROLLER_KEY+"."+BRANCH_COLOR_ATTRIBUTE_KEY));
	}

	public void getSettings(Map<String, Object> settings) {
		if (branchColourAttributeCombo.getSelectedItem() != null) {
			settings.put(CONTROLLER_KEY + "." + BRANCH_COLOR_ATTRIBUTE_KEY, branchColourAttributeCombo.getSelectedItem().toString());
		} else {
			settings.put(CONTROLLER_KEY + "." + BRANCH_COLOR_ATTRIBUTE_KEY, "none");
		}
	}


	private final OptionsPanel optionsPanel;

	private final JComboBox branchColourAttributeCombo;
}