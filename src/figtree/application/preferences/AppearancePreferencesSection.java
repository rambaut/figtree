/*
 * AppearancePreferencesSection.java
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

import jam.panels.OptionsPanel;
import jam.preferences.PreferencesSection;
import jam.util.IconUtils;
import figtree.application.FigTreeApplication;
import figtree.treeviewer.TreeAppearanceController;

import javax.swing.*;
import java.awt.*;

import figtree.ui.components.ColorWellButton;

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

//        foregroundButton = new ColorWellButton(Color.white, "Choose Foreground Colour");
//        optionsPanel.addComponentWithLabel("Foreground Colour:", foregroundButton);
//
//        backgroundButton = new ColorWellButton(Color.white, "Choose Background Colour");
//        optionsPanel.addComponentWithLabel("Background Colour:", backgroundButton);

        selectionButton = new ColorWellButton(Color.white, "Choose Selection Colour");
        optionsPanel.addComponentWithLabel("Selection Colour:", selectionButton);

        optionsPanel.addSeparator();

        branchLineWidthSpinner = new JSpinner(
                new SpinnerNumberModel(1.0, 0.01, 48.0, 1.0));
        optionsPanel.addComponentWithLabel("Line Weight:", branchLineWidthSpinner);

        optionsPanel.addFlexibleSpace();

        return optionsPanel;
    }

    public void retrievePreferences() {
//        int foregroundRGB = TreeAppearanceController.PREFS.getInt(
//                TreeAppearanceController.CONTROLLER_KEY + "." + TreeAppearanceController.FOREGROUND_COLOUR_KEY,
//                TreeAppearanceController.DEFAULT_FOREGROUND_COLOUR.getRGB());
//        int backgroundRGB = TreeAppearanceController.PREFS.getInt(
//                TreeAppearanceController.CONTROLLER_KEY + "." + TreeAppearanceController.BACKGROUND_COLOUR_KEY,
//                TreeAppearanceController.DEFAULT_BACKGROUND_COLOUR.getRGB());
        int selectionRGB = TreeAppearanceController.PREFS.getInt(
                TreeAppearanceController.CONTROLLER_KEY + "." + TreeAppearanceController.SELECTION_COLOUR_KEY,
                TreeAppearanceController.DEFAULT_SELECTION_COLOUR.getRGB());
        double branchLineWidth = TreeAppearanceController.PREFS.getFloat(
                TreeAppearanceController.CONTROLLER_KEY + "." + TreeAppearanceController.BRANCH_LINE_WIDTH_KEY,
                TreeAppearanceController.DEFAULT_BRANCH_LINE_WIDTH);

//        foregroundButton.setSelectedColor(new Color(foregroundRGB));
//        backgroundButton.setSelectedColor(new Color(backgroundRGB));
        selectionButton.setSelectedColor(new Color(selectionRGB));
        branchLineWidthSpinner.setValue(branchLineWidth);
    }

    public void storePreferences() {
//        int foregroundRGB = foregroundButton.getSelectedColor().getRGB();
//        int backgroundRGB = backgroundButton.getSelectedColor().getRGB();
        int selectionRGB = selectionButton.getSelectedColor().getRGB();
        float branchLineWidth = ((Number)branchLineWidthSpinner.getValue()).floatValue();

//        TreeAppearanceController.PREFS.putInt(
//                TreeAppearanceController.CONTROLLER_KEY + "." + TreeAppearanceController.FOREGROUND_COLOUR_KEY,
//                foregroundRGB);
//        TreeAppearanceController.PREFS.putInt(
//                TreeAppearanceController.CONTROLLER_KEY + "." + TreeAppearanceController.BACKGROUND_COLOUR_KEY,
//                backgroundRGB);
        TreeAppearanceController.PREFS.putInt(
                TreeAppearanceController.CONTROLLER_KEY + "." + TreeAppearanceController.SELECTION_COLOUR_KEY,
                selectionRGB);
        TreeAppearanceController.PREFS.putFloat(
                TreeAppearanceController.CONTROLLER_KEY + "." + TreeAppearanceController.BRANCH_LINE_WIDTH_KEY,
                branchLineWidth);
    }

//    ColorWellButton foregroundButton;
//    ColorWellButton backgroundButton;
    ColorWellButton selectionButton;
    JSpinner branchLineWidthSpinner;
}
