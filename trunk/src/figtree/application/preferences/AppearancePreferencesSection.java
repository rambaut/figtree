package figtree.application.preferences;

import org.virion.jam.panels.OptionsPanel;
import org.virion.jam.preferences.PreferencesSection;
import org.virion.jam.util.IconUtils;
import figtree.application.FigTreeApplication;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

/**
 * @author Andrew Rambaut
 * @version $Id: AppearancePreferencesSection.java,v 1.2 2007/08/18 09:52:24 rambaut Exp $
 */
public class AppearancePreferencesSection implements PreferencesSection {
	Icon coloursToolIcon = IconUtils.getIcon(FigTreeApplication.class, "images/coloursTool.png");


	public String getTitle() {
		return "Appearance";
	}

	public Icon getIcon() {
		return coloursToolIcon;
	}

	public JPanel getPanel() {
		OptionsPanel optionsPanel = new OptionsPanel(12, 18);

		JButton foregroundButton = new JButton("Foreground");
		optionsPanel.addComponentWithLabel("Foreground Colour:", foregroundButton);
		JButton backgroundButton = new JButton("Background");
		optionsPanel.addComponentWithLabel("Background Colour:", backgroundButton);
		JButton selectionButton = new JButton("Selection");
		optionsPanel.addComponentWithLabel("Selection Colour:", selectionButton);

		optionsPanel.addSeparator();

		branchLineWidthSpinner = new JSpinner(
				new SpinnerNumberModel(1.0, 0.01, 48.0, 1.0));
		optionsPanel.addComponentWithLabel("Line Weight:", branchLineWidthSpinner);
		return optionsPanel;
	}

	public void retrievePreferences() {
		Preferences prefs = Preferences.userNodeForPackage(FigTreeApplication.class);
		foregroundRGB = prefs.getInt(CONTROLLER_KEY + "." + FOREGROUND_COLOUR_KEY, DEFAULT_FOREGROUND_COLOUR.getRGB());
		backgroundRGB = prefs.getInt(CONTROLLER_KEY + "." + BACKGROUND_COLOUR_KEY, DEFAULT_BACKGROUND_COLOUR.getRGB());
		selectionRGB = prefs.getInt(CONTROLLER_KEY + "." + SELECTION_COLOUR_KEY, DEFAULT_SELECTION_COLOUR.getRGB());
		branchLineWidth = prefs.getFloat(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY, DEFAULT_BRANCH_LINE_WIDTH);

		branchLineWidthSpinner.setValue(branchLineWidth);
	}

	public void storePreferences() {
		branchLineWidth = (Float)branchLineWidthSpinner.getValue();

		Preferences prefs = Preferences.userNodeForPackage(FigTreeApplication.class);
		prefs.putInt(CONTROLLER_KEY + "." + FOREGROUND_COLOUR_KEY, foregroundRGB);
		prefs.putInt(CONTROLLER_KEY + "." + BACKGROUND_COLOUR_KEY, backgroundRGB);
		prefs.putInt(CONTROLLER_KEY + "." + SELECTION_COLOUR_KEY, selectionRGB);
		prefs.putFloat(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY, branchLineWidth);
	}

	JButton foregroundButton;
	JButton backgroundButton;
	JButton selectionButton;
	JSpinner branchLineWidthSpinner;

	int foregroundRGB;
	int backgroundRGB;
	int selectionRGB;
	float branchLineWidth;

	private final String CONTROLLER_KEY = "appearance";

	private final String FOREGROUND_COLOUR_KEY = "foregroundColour";
	private final String BACKGROUND_COLOUR_KEY = "backgroundColour";
	private final String SELECTION_COLOUR_KEY = "selectionColour";
	private final String BRANCH_LINE_WIDTH_KEY = "branchLineWidth";

	// The defaults if there is nothing in the preferences
	private Color DEFAULT_FOREGROUND_COLOUR = Color.BLACK;
	private Color DEFAULT_BACKGROUND_COLOUR = Color.WHITE;
	private Color DEFAULT_SELECTION_COLOUR = new Color(180, 213, 254);
	private float DEFAULT_BRANCH_LINE_WIDTH = 1.0f;
}
