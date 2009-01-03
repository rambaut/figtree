package figtree.application.preferences;

import jam.preferences.PreferencesSection;
import jam.util.IconUtils;
import figtree.application.FigTreeApplication;

import javax.swing.*;
import java.util.prefs.Preferences;

/**
 * @author Andrew Rambaut
 * @version $Id: AdvancedPreferencesSection.java,v 1.1 2006/09/09 15:23:33 rambaut Exp $
 */
public class AdvancedPreferencesSection implements PreferencesSection {
	Icon projectToolIcon = IconUtils.getIcon(FigTreeApplication.class, "images/prefsAdvanced.png");


	public String getTitle() {
		return "Advanced";
	}

	public Icon getIcon() {
		return projectToolIcon;
	}

	public JPanel getPanel() {
		JPanel panel = new JPanel();
		panel.add(new JCheckBox("Advanced preference"));
		return panel;
	}

	public void retrievePreferences() {
		Preferences prefs = Preferences.userNodeForPackage(FigTreeApplication.class);
		advancedCheck.setSelected(prefs.getBoolean("advanced_check", true));
	}

	public void storePreferences() {
		Preferences prefs = Preferences.userNodeForPackage(FigTreeApplication.class);
		prefs.putBoolean("advanced_check", advancedCheck.isSelected());
	}

	JCheckBox advancedCheck = new JCheckBox("The preferences window is not implemented yet.");
}
