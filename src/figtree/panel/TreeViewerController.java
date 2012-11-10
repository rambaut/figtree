/*
 * TreeViewerController.java
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

import figtree.treeviewer.treelayouts.*;
import figtree.treeviewer.TreeViewer;
import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Map;

/**
 * @author Andrew Rambaut
 * @version $Id: TreeViewerController.java,v 1.1 2007/08/14 15:50:41 rambaut Exp $
 */
public class TreeViewerController extends AbstractController {

	private static final String CONTROLLER_TITLE = "Layout";

	private static final String CONTROLLER_KEY = "layout";
	private static final String ZOOM_KEY = "zoom";
	private static final String EXPANSION_KEY = "expansion";

	// The defaults if there is nothing in the preferences
	private final static int MAX_ZOOM_SLIDER = 10000;
	private final static int DELTA_ZOOM_SLIDER = 200;

	public TreeViewerController(final TreeViewer treeViewer) {
		this(treeViewer, false);
	}

	public TreeViewerController(final TreeViewer treeViewer, boolean hideZoom) {

		this.treeViewer = treeViewer;

		this.hideZoom = hideZoom;

		titleLabel = new JLabel(CONTROLLER_TITLE);
		optionsPanel = new OptionsPanel();

		rectilinearTreeLayout = new RectilinearTreeLayout();
		treeLayoutController = new TreeLayoutController(rectilinearTreeLayout);

		if (!hideZoom) {
			zoomSlider = new JSlider(SwingConstants.HORIZONTAL, 0, MAX_ZOOM_SLIDER, 0);
			zoomSlider.setOpaque(false);
			zoomSlider.setAlignmentX(Component.LEFT_ALIGNMENT);

//        zoomSlider.setPaintTicks(true);
//        zoomSlider.setPaintLabels(true);

			zoomSlider.setValue(0);

			zoomSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent changeEvent) {
					final int value = zoomSlider.getValue();
					treeViewer.setZoom(((double) value) / MAX_ZOOM_SLIDER);
				}
			});

			optionsPanel.addComponentWithLabel("Zoom:", zoomSlider, true);
		}

		verticalExpansionSlider = new JSlider(SwingConstants.HORIZONTAL, 0, MAX_ZOOM_SLIDER, 0);
		verticalExpansionSlider.setOpaque(false);
//        verticalExpansionSlider.setPaintTicks(true);
//        verticalExpansionSlider.setPaintLabels(true);

		verticalExpansionSlider.setValue(0);

		verticalExpansionSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				final int value = verticalExpansionSlider.getValue();
				treeViewer.setVerticalExpansion(((double) value) / MAX_ZOOM_SLIDER);
			}
		});

		verticalExpansionLabel = new JLabel("Expansion:");
		optionsPanel.addComponents(verticalExpansionLabel, false, verticalExpansionSlider, true);

		layoutPanel = new JPanel(new BorderLayout());
		layoutPanel.setOpaque(false);
		setTreeLayout();
		setExpansion();

		optionsPanel.addSpanningComponent(layoutPanel);

		// Set some InputMaps and ActionMaps for key strokes. The ActionMaps are set in setExpansion()
		// because they differ by whether vertical expansion is allowed for the current layout.
		// The key strokes could be obtained from preferences and set in a preference dialog box
		optionsPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("meta 0"), "resetZoom");
		optionsPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("meta EQUALS"), "increasePrimaryZoom");
		optionsPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("meta MINUS"), "decreasePrimaryZoom");
		optionsPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("meta alt EQUALS"), "increaseSecondaryZoom");
		optionsPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("meta alt MINUS"), "decreaseSecondaryZoom");

		optionsPanel.getActionMap().put("resetZoom", resetZoomAction);

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

	public void setColouringAttributeName(String attributeName) {
		rectilinearTreeLayout.setBranchColouringAttributeName(attributeName);
	}

	public void initialize() {
		// nothing to do
	}

	public void setSettings(Map<String,Object> settings) {
		if (!hideZoom) {
			zoomSlider.setValue((Integer)settings.get(CONTROLLER_KEY + "." + ZOOM_KEY));
		}
		verticalExpansionSlider.setValue((Integer)settings.get(CONTROLLER_KEY + "." + EXPANSION_KEY));

		// These controllers are internal to TreeViewerController so settings must be done here
		treeLayoutController.setSettings(settings);
	}

	public void getSettings(Map<String, Object> settings) {
		if (!hideZoom) {
			settings.put(CONTROLLER_KEY + "." + ZOOM_KEY, zoomSlider.getValue());
		}
		settings.put(CONTROLLER_KEY + "." + EXPANSION_KEY, verticalExpansionSlider.getValue());

		// These controllers are internal to TreeViewerController so settings must be done here
		treeLayoutController.getSettings(settings);
	}

	private void setTreeLayout() {
		treeViewer.setTreeLayout(rectilinearTreeLayout);
		setExpansion();
		layoutPanel.removeAll();
		layoutPanel.add(treeLayoutController.getPanel(), BorderLayout.CENTER);
		fireControllerChanged();
	}

	private void setExpansion() {
		if (treeViewer.verticalExpansionAllowed()) {
			verticalExpansionLabel.setEnabled(true);
			verticalExpansionSlider.setEnabled(true);
			optionsPanel.getActionMap().put("increasePrimaryZoom", increaseVerticalExpansionAction);
			optionsPanel.getActionMap().put("decreasePrimaryZoom", decreaseVerticalExpansionAction);
			optionsPanel.getActionMap().put("increaseSecondaryZoom", increaseZoomAction);
			optionsPanel.getActionMap().put("decreaseSecondaryZoom", decreaseZoomAction);
		} else {
			verticalExpansionLabel.setEnabled(false);
			verticalExpansionSlider.setEnabled(false);
			optionsPanel.getActionMap().put("increasePrimaryZoom", increaseZoomAction);
			optionsPanel.getActionMap().put("decreasePrimaryZoom", decreaseZoomAction);
			optionsPanel.getActionMap().put("increaseSecondaryZoom", increaseZoomAction);
			optionsPanel.getActionMap().put("decreaseSecondaryZoom", decreaseZoomAction);
		}
	}

	private Action resetZoomAction = new AbstractAction("Reset Zoom") {
		public void actionPerformed(ActionEvent actionEvent) {
			zoomSlider.setValue(0);
			verticalExpansionSlider.setValue(0);
		}
	};

	private Action increaseZoomAction = new AbstractAction("Zoom In") {
		public void actionPerformed(ActionEvent actionEvent) {
			zoomSlider.setValue(zoomSlider.getValue() + DELTA_ZOOM_SLIDER);
		}
	};

	private Action decreaseZoomAction = new AbstractAction("Zoom In") {
		public void actionPerformed(ActionEvent actionEvent) {
			zoomSlider.setValue(zoomSlider.getValue() - DELTA_ZOOM_SLIDER);
		}
	};

	private Action increaseVerticalExpansionAction = new AbstractAction("Expand Vertically") {
		public void actionPerformed(ActionEvent actionEvent) {
			verticalExpansionSlider.setValue(verticalExpansionSlider.getValue() + DELTA_ZOOM_SLIDER);
		}
	};

	private Action decreaseVerticalExpansionAction = new AbstractAction("Unexpand Vertically") {
		public void actionPerformed(ActionEvent actionEvent) {
			int value = verticalExpansionSlider.getValue();
			if (value > 0) {
				verticalExpansionSlider.setValue(value - DELTA_ZOOM_SLIDER);
			} else {
				// If the vertical expansion was zero then assume the user is trying to un-zoom
				zoomSlider.setValue(zoomSlider.getValue() - DELTA_ZOOM_SLIDER);
			}
		}
	};


	private JSlider zoomSlider;
	private JSlider verticalExpansionSlider;
	private JLabel verticalExpansionLabel;

	private final JPanel layoutPanel;

	private final JLabel titleLabel;
	private final OptionsPanel optionsPanel;

	private final RectilinearTreeLayout rectilinearTreeLayout;
	private final TreeLayoutController treeLayoutController;

	private final TreeViewer treeViewer;

	private final boolean hideZoom;

}