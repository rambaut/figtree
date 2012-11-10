/*
 * GeneralPreferencesSection.java
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

package figtree.application.preferences;

import jam.preferences.PreferencesSection;
import jam.util.IconUtils;
import figtree.application.FigTreeApplication;

import javax.swing.*;
import java.util.prefs.Preferences;

/**
 * @author Andrew Rambaut
 * @version $Id: GeneralPreferencesSection.java,v 1.1 2006/09/09 15:23:33 rambaut Exp $
 */
public class GeneralPreferencesSection implements PreferencesSection {
	Icon projectToolIcon = IconUtils.getIcon(FigTreeApplication.class, "images/prefsGeneral.png");

	public String getTitle() {
		return "General";
	}

	public Icon getIcon() {
		return projectToolIcon;
	}

	public JPanel getPanel() {
		JPanel panel = new JPanel();
		panel.add(generalCheck);
		return panel;
	}

	public void retrievePreferences() {
		Preferences prefs = Preferences.userNodeForPackage(FigTreeApplication.class);
		generalCheck.setSelected(prefs.getBoolean("general_check", true));
	}

	public void storePreferences() {
		Preferences prefs = Preferences.userNodeForPackage(FigTreeApplication.class);
		prefs.putBoolean("general_check", generalCheck.isSelected());
	}

	JCheckBox generalCheck = new JCheckBox("The preferences window is not implemented yet.");
}
