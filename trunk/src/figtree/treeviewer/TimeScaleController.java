package figtree.treeviewer;

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

import figtree.ui.components.RealNumberField;

/**
 * @author Andrew Rambaut
 * @version $Id: TimeScaleController.java 774 2007-09-05 09:27:30Z rambaut $
 */
public class TimeScaleController extends AbstractController {

	private static final String CONTROLLER_TITLE = "Time Scale";

	private static Preferences PREFS = Preferences.userNodeForPackage(TreeViewer.class);

	private static final String CONTROLLER_KEY = "scale";

	private static final String SCALE_ROOT_KEY = "scaleRoot";
	private static final String ROOT_AGE_KEY = "rootAge";
	private static final String OFFSET_AGE_KEY = "offsetAge";
	private static final String SCALE_FACTOR_KEY = "scaleFactor";

	// The defaults if there is nothing in the preferences
	private static boolean DEFAULT_SCALE_ROOT = false;
	private static double DEFAULT_ROOT_AGE = 1.0;
	private static double DEFAULT_OFFSET_AGE = 0.0;
	private static double DEFAULT_SCALE_FACTOR = 1.0;

	public TimeScaleController(final TreeViewer treeViewer) {

		this.treeViewer = treeViewer;

		titleLabel = new JLabel(CONTROLLER_TITLE);

		final boolean scaleRoot = PREFS.getBoolean(CONTROLLER_KEY + "." + SCALE_ROOT_KEY, DEFAULT_SCALE_ROOT);
		final double offsetAge = PREFS.getDouble(CONTROLLER_KEY + "." + OFFSET_AGE_KEY, DEFAULT_OFFSET_AGE);
		final double scaleFactor = PREFS.getDouble(CONTROLLER_KEY + "." + SCALE_FACTOR_KEY, DEFAULT_SCALE_FACTOR);
		final double rootAge = PREFS.getDouble(CONTROLLER_KEY + "." + ROOT_AGE_KEY, DEFAULT_ROOT_AGE);

		optionsPanel = new ControllerOptionsPanel(2, 2);

		scaleFactorRadio = new JRadioButton("Scale by factor:");
		scaleFactorRadio.setSelected(!scaleRoot);
		optionsPanel.addSpanningComponent(scaleFactorRadio);

		offsetAgeText = new RealNumberField(-Double.MAX_VALUE, Double.MAX_VALUE);
		offsetAgeText.setValue(offsetAge);
		final JLabel label1 = optionsPanel.addComponentWithLabel("Offset by:", offsetAgeText, true);

		scaleFactorText = new RealNumberField(-Double.MAX_VALUE, Double.MAX_VALUE);
		scaleFactorText.setValue(scaleFactor);
		final JLabel label2 = optionsPanel.addComponentWithLabel("Scale factor:", scaleFactorText, true);

		scaleRootRadio = new JRadioButton("Scale root to:");
		scaleRootRadio.setSelected(scaleRoot);
		optionsPanel.addSpanningComponent(scaleRootRadio);

		rootAgeText = new RealNumberField(0.0, Double.MAX_VALUE);
		rootAgeText.setValue(rootAge);
		final JLabel label3 = optionsPanel.addComponentWithLabel("Root age:", rootAgeText, true);

		ButtonGroup group = new ButtonGroup();
		group.add(scaleFactorRadio);
		group.add(scaleRootRadio);

		scaleFactorRadio.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                boolean selected = scaleFactorRadio.isSelected();
                label1.setEnabled(selected);
                offsetAgeText.setEnabled(selected);
                label2.setEnabled(selected);
                scaleFactorText.setEnabled(selected);

                label3.setEnabled(!selected);
                rootAgeText.setEnabled(!selected);

                setTimeScale();
            }
        });

        ChangeListener listener = new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                setTimeScale();
            }
        };
		offsetAgeText.addChangeListener(listener);
		scaleFactorText.addChangeListener(listener);
		rootAgeText.addChangeListener(listener);

	}

	private void setTimeScale() {
		TimeScale timeScale;

		if (scaleFactorRadio.isSelected()) {
			double offset = getValue(offsetAgeText, 0.0);
			double scaleFactor = getValue(scaleFactorText, 1.0);
			timeScale = new TimeScale(scaleFactor, offset);
		} else {
			double rootAge = getValue(rootAgeText, 0.0);
			timeScale = new TimeScale(rootAge);
		}

		treeViewer.setTimeScale(timeScale);
	}

	private double getValue(RealNumberField field, double defaultValue) {
		Double value = field.getValue();
		return (value != null ? value : defaultValue);
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
		scaleFactorRadio.setSelected(!((Boolean)settings.get(CONTROLLER_KEY + "." + SCALE_ROOT_KEY)));
		scaleRootRadio.setSelected(((Boolean)settings.get(CONTROLLER_KEY + "." + SCALE_ROOT_KEY)));
		offsetAgeText.setValue((Double)settings.get(CONTROLLER_KEY + "." + OFFSET_AGE_KEY));
		scaleFactorText.setValue((Double)settings.get(CONTROLLER_KEY + "." + SCALE_FACTOR_KEY));
		rootAgeText.setValue((Double)settings.get(CONTROLLER_KEY + "." + ROOT_AGE_KEY));
	}

	public void getSettings(Map<String, Object> settings) {
		settings.put(CONTROLLER_KEY + "." + SCALE_ROOT_KEY, new Boolean(scaleRootRadio.isSelected()));
		settings.put(CONTROLLER_KEY + "." + OFFSET_AGE_KEY, offsetAgeText.getValue());
		settings.put(CONTROLLER_KEY + "." + SCALE_FACTOR_KEY, scaleFactorText.getValue());
		settings.put(CONTROLLER_KEY + "." + ROOT_AGE_KEY, rootAgeText.getValue());
	}

	private JRadioButton scaleFactorRadio;
	private final RealNumberField offsetAgeText;
	private final RealNumberField scaleFactorText;

	private JRadioButton scaleRootRadio;
	private final RealNumberField rootAgeText;

	private final JLabel titleLabel;
	private final OptionsPanel optionsPanel;

	private final TreeViewer treeViewer;

}