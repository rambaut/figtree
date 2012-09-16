package figtree.treeviewer;

import figtree.treeviewer.treelayouts.*;
import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;
import jam.util.IconUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * @author Andrew Rambaut
 * @version $Id: TreeViewerController.java 766 2007-08-29 01:09:33Z rambaut $
 */
public class TreeViewerController extends AbstractController {

	public enum TreeLayoutType {
		RECTILINEAR("Rectangle"),
		POLAR("Polar"),
		RADIAL("Radial");

		TreeLayoutType(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}

		private final String name;
	}

	private static final String CONTROLLER_TITLE = "Layout";

	private static Preferences PREFS = Preferences.userNodeForPackage(TreeViewerController.class);

	private static final String CONTROLLER_KEY = "layout";

	private static final String LAYOUT_KEY = "layoutType";
	private static final String ZOOM_KEY = "zoom";
	private static final String EXPANSION_KEY = "expansion";

	// The defaults if there is nothing in the preferences
	private static String DEFAULT_LAYOUT = TreeLayoutType.RECTILINEAR.name();

	private final static int MAX_ZOOM_SLIDER = 1000;
	private final static int DELTA_ZOOM_SLIDER = 1;

	public TreeViewerController(final TreeViewer treeViewer) {

		this.treeViewer = treeViewer;

		final TreeLayoutType defaultLayout = TreeLayoutType.valueOf(PREFS.get(CONTROLLER_KEY + "." + LAYOUT_KEY, DEFAULT_LAYOUT));

		titleLabel = new JLabel(CONTROLLER_TITLE);

		optionsPanel = new ControllerOptionsPanel(2, 2);

		rectilinearTreeLayout = new RectilinearTreeLayout();
		rectilinearTreeLayoutController = new RectilinearTreeLayoutController(rectilinearTreeLayout);

		polarTreeLayout = new PolarTreeLayout();
		polarTreeLayoutController = new PolarTreeLayoutController(polarTreeLayout);

		radialTreeLayout = new RadialTreeLayout();
		radialTreeLayoutController = new RadialTreeLayoutController(radialTreeLayout);

		JPanel panel1 = new JPanel();
		panel1.setOpaque(false);
		panel1.setLayout(new BoxLayout(panel1, BoxLayout.LINE_AXIS));
		Icon rectangularTreeIcon = IconUtils.getIcon(this.getClass(), "images/rectangularTree.png");
		Icon polarTreeIcon = IconUtils.getIcon(this.getClass(), "images/polarTree.png");
		Icon radialTreeIcon = IconUtils.getIcon(this.getClass(), "images/radialTree.png");
		rectangularTreeToggle = new JToggleButton(rectangularTreeIcon);
		polarTreeToggle = new JToggleButton(polarTreeIcon);
		radialTreeToggle = new JToggleButton(radialTreeIcon);
		rectangularTreeToggle.setToolTipText("Rectangular tree layout");
		polarTreeToggle.setToolTipText("Polar tree layout");
		radialTreeToggle.setToolTipText("Radial tree layout");
        rectangularTreeToggle.setFocusable(false);
        polarTreeToggle.setFocusable(false);
        radialTreeToggle.setFocusable(false);
		rectangularTreeToggle.putClientProperty("Quaqua.Button.style", "toggleWest");
		rectangularTreeToggle.putClientProperty("JButton.buttonType", "segmentedTextured");
		rectangularTreeToggle.putClientProperty("JButton.segmentPosition", "first");
		polarTreeToggle.putClientProperty("Quaqua.Button.style", "toggleCenter");
		polarTreeToggle.putClientProperty("JButton.buttonType", "segmentedTextured");
		polarTreeToggle.putClientProperty("JButton.segmentPosition", "middle");
		radialTreeToggle.putClientProperty("Quaqua.Button.style", "toggleEast");
		radialTreeToggle.putClientProperty("JButton.buttonType", "segmentedTextured");
		radialTreeToggle.putClientProperty("JButton.segmentPosition", "last");
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(rectangularTreeToggle);
		buttonGroup.add(polarTreeToggle);
		buttonGroup.add(radialTreeToggle);
		rectangularTreeToggle.setSelected(true);
		panel1.add(Box.createHorizontalGlue());
		panel1.add(rectangularTreeToggle);
		panel1.add(polarTreeToggle);
		panel1.add(radialTreeToggle);
		panel1.add(Box.createHorizontalGlue());

		optionsPanel.addSpanningComponent(panel1);

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

		fishEyeSlider = new JSlider(SwingConstants.HORIZONTAL, 0, MAX_ZOOM_SLIDER, 0);
		fishEyeSlider.setOpaque(false);

		fishEyeSlider.setValue(0);

		fishEyeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				final double fishEye = ((double) fishEyeSlider.getValue()) / MAX_ZOOM_SLIDER;
				rectilinearTreeLayout.setFishEye(fishEye);
				polarTreeLayout.setFishEye(fishEye);
				radialTreeLayout.setFishEye(fishEye);
			}
		});

		fishEyeLabel = new JLabel("Fish Eye:");
		optionsPanel.addComponents(fishEyeLabel, false, fishEyeSlider, true);

		optionsPanel.addSeparator();

		layoutPanel = new JPanel(new BorderLayout());
		layoutPanel.setOpaque(false);
		setTreeLayout(defaultLayout);
		setExpansion();

		optionsPanel.addSpanningComponent(layoutPanel);

		rectangularTreeToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (rectangularTreeToggle.isSelected()) setTreeLayout(TreeLayoutType.RECTILINEAR);
            }
        });
		polarTreeToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
				if (polarTreeToggle.isSelected()) setTreeLayout(TreeLayoutType.POLAR);
			}
		});
		radialTreeToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
				if (radialTreeToggle.isSelected()) setTreeLayout(TreeLayoutType.RADIAL);
			}
		});

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
		polarTreeLayout.setBranchColouringAttributeName(attributeName);
		radialTreeLayout.setBranchColouringAttributeName(attributeName);
	}

	public void initialize() {
		// nothing to do
	}

	public void setSettings(Map<String,Object> settings) {
		String treeLayoutName = (String)settings.get(CONTROLLER_KEY + "." + LAYOUT_KEY);
		final TreeLayoutType layout = TreeLayoutType.valueOf(treeLayoutName);
		switch (layout) {
			case RECTILINEAR:
				rectangularTreeToggle.setSelected(true);
				break;
			case POLAR:
				polarTreeToggle.setSelected(true);
				break;
			case RADIAL:
				radialTreeToggle.setSelected(true);
				break;
		}
        setTreeLayout(layout);

		zoomSlider.setValue((Integer)settings.get(CONTROLLER_KEY + "." + ZOOM_KEY));
		verticalExpansionSlider.setValue((Integer)settings.get(CONTROLLER_KEY + "." + EXPANSION_KEY));

		// These controllers are internal to TreeViewerController so settings must be done here
		rectilinearTreeLayoutController.setSettings(settings);
		polarTreeLayoutController.setSettings(settings);
		radialTreeLayoutController.setSettings(settings);
	}

	public void getSettings(Map<String, Object> settings) {
		if (rectangularTreeToggle.isSelected()) {
			settings.put(CONTROLLER_KEY + "." + LAYOUT_KEY, TreeLayoutType.RECTILINEAR.name());
		} else if (polarTreeToggle.isSelected()) {
			settings.put(CONTROLLER_KEY + "." + LAYOUT_KEY, TreeLayoutType.POLAR.name());
		} else if (radialTreeToggle.isSelected()) {
			settings.put(CONTROLLER_KEY + "." + LAYOUT_KEY, TreeLayoutType.RADIAL.name());
		}
		settings.put(CONTROLLER_KEY + "." + ZOOM_KEY, zoomSlider.getValue());
		settings.put(CONTROLLER_KEY + "." + EXPANSION_KEY, verticalExpansionSlider.getValue());

		// These controllers are internal to TreeViewerController so settings must be done here
		rectilinearTreeLayoutController.getSettings(settings);
		polarTreeLayoutController.getSettings(settings);
		radialTreeLayoutController.getSettings(settings);
	}

	private void setTreeLayout(TreeLayoutType layoutType) {
		switch (layoutType) {
			case RECTILINEAR:
				treeViewer.setTreeLayout(rectilinearTreeLayout);
				setExpansion();
				fishEyeLabel.setEnabled(true);
				fishEyeSlider.setEnabled(true);
				layoutPanel.removeAll();
				layoutPanel.add(rectilinearTreeLayoutController.getPanel(), BorderLayout.CENTER);
				fireControllerChanged();
				break;
			case POLAR:
				treeViewer.setTreeLayout(polarTreeLayout);
				setExpansion();
				fishEyeLabel.setEnabled(true);
				fishEyeSlider.setEnabled(true);
				layoutPanel.removeAll();
				layoutPanel.add(polarTreeLayoutController.getPanel(), BorderLayout.CENTER);
				fireControllerChanged();
				break;
			case RADIAL:
				treeViewer.setTreeLayout(radialTreeLayout);
				setExpansion();
				fishEyeLabel.setEnabled(false);
				fishEyeSlider.setEnabled(false);
				layoutPanel.removeAll();
				layoutPanel.add(radialTreeLayoutController.getPanel(), BorderLayout.CENTER);
				fireControllerChanged();
				break;
			default:
				new RuntimeException("Unknown TreeLayoutType: " + layoutType);
		}

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


	private JToggleButton rectangularTreeToggle;
	private JToggleButton polarTreeToggle;
	private JToggleButton radialTreeToggle;
	private JSlider zoomSlider;
	private JSlider verticalExpansionSlider;
	private JLabel verticalExpansionLabel;
	private JSlider fishEyeSlider;
	private JLabel fishEyeLabel;

	private final JPanel layoutPanel;

	private final JLabel titleLabel;
	private final OptionsPanel optionsPanel;

	private final RectilinearTreeLayout rectilinearTreeLayout;
	private final PolarTreeLayout polarTreeLayout;
	private final RadialTreeLayout radialTreeLayout;

	private final RectilinearTreeLayoutController rectilinearTreeLayoutController;
	private final PolarTreeLayoutController polarTreeLayoutController;
	private final RadialTreeLayoutController radialTreeLayoutController;

	private final TreeViewer treeViewer;

}
