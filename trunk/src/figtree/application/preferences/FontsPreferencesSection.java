/*
 * FontsPreferencesSection.java
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

import figtree.application.FigTreeApplication;
import figtree.treeviewer.painters.LabelPainterController;
import figtree.treeviewer.painters.ScaleBarPainterController;
import figtree.ui.FontChooserPanel;
import jam.panels.OptionsPanel;
import jam.preferences.PreferencesSection;
import jam.util.IconUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * @author Andrew Rambaut
 * @version $Id: AdvancedPreferencesSection.java,v 1.1 2006/09/09 15:23:33 rambaut Exp $
 */
public class FontsPreferencesSection implements PreferencesSection {
    Icon projectToolIcon = IconUtils.getIcon(FigTreeApplication.class, "images/prefsFonts.png");


    public String getTitle() {
        return "Fonts";
    }

    public Icon getIcon() {
        return projectToolIcon;
    }

    public JPanel getPanel() {
        OptionsPanel optionsPanel = new OptionsPanel(12, 18);

        if (tipLabelFontChooser == null) {
            Font font = new Font(
                    LabelPainterController.DEFAULT_FONT_NAME,
                    LabelPainterController.DEFAULT_FONT_STYLE,
                    LabelPainterController.DEFAULT_FONT_SIZE);

            tipLabelFontChooser = new FontChooserPanel(font);
        }
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(tipLabelFontChooser, BorderLayout.CENTER);
        panel.setBorder(new TitledBorder("Tip Labels"));
        optionsPanel.addSpanningComponent(panel);

        if (nodeLabelFontChooser == null) {
            Font font = new Font(
                    LabelPainterController.DEFAULT_FONT_NAME,
                    LabelPainterController.DEFAULT_FONT_STYLE,
                    LabelPainterController.DEFAULT_FONT_SIZE);

            nodeLabelFontChooser = new FontChooserPanel(font);
        }
        panel = new JPanel(new BorderLayout());
        panel.add(nodeLabelFontChooser, BorderLayout.CENTER);
        panel.setBorder(new TitledBorder("Node Labels"));
        optionsPanel.addSpanningComponent(panel);

        if (branchLabelFontChooser == null) {
            Font font = new Font(
                    LabelPainterController.DEFAULT_FONT_NAME,
                    LabelPainterController.DEFAULT_FONT_STYLE,
                    LabelPainterController.DEFAULT_FONT_SIZE);

            branchLabelFontChooser = new FontChooserPanel(font);
        }
        panel = new JPanel(new BorderLayout());
        panel.add(branchLabelFontChooser, BorderLayout.CENTER);
        panel.setBorder(new TitledBorder("Branch Labels"));
        optionsPanel.addSpanningComponent(panel);

        if (scaleLabelFontChooser == null) {
            Font font = new Font(
                    LabelPainterController.DEFAULT_FONT_NAME,
                    LabelPainterController.DEFAULT_FONT_STYLE,
                    LabelPainterController.DEFAULT_FONT_SIZE);

            scaleLabelFontChooser = new FontChooserPanel(font);
        }
        panel = new JPanel(new BorderLayout());
        panel.add(scaleLabelFontChooser, BorderLayout.CENTER);
        panel.setBorder(new TitledBorder("Scale Bar/Axis Labels"));
        optionsPanel.addSpanningComponent(panel);

        optionsPanel.addFlexibleSpace();

        return optionsPanel;
    }

    public void retrievePreferences() {
        String name = LabelPainterController.PREFS.get("tipLabels" + "." + LabelPainterController.FONT_NAME_KEY,
                LabelPainterController.DEFAULT_FONT_NAME);
        int size = LabelPainterController.PREFS.getInt("tipLabels" + "." + LabelPainterController.FONT_SIZE_KEY,
                LabelPainterController.DEFAULT_FONT_SIZE);
        int style = LabelPainterController.PREFS.getInt("tipLabels" + "." + LabelPainterController.FONT_STYLE_KEY,
                LabelPainterController.DEFAULT_FONT_STYLE);
        Font font = new Font(name, style, size);
        tipLabelFontChooser.setSelectedFont(font);

        name = LabelPainterController.PREFS.get("nodeLabels" + "." + LabelPainterController.FONT_NAME_KEY,
                LabelPainterController.DEFAULT_FONT_NAME);
        size = LabelPainterController.PREFS.getInt("nodeLabels" + "." + LabelPainterController.FONT_SIZE_KEY,
                LabelPainterController.DEFAULT_FONT_SIZE);
        style = LabelPainterController.PREFS.getInt("nodeLabels" + "." + LabelPainterController.FONT_STYLE_KEY,
                LabelPainterController.DEFAULT_FONT_STYLE);
        font = new Font(name, style, size);
        nodeLabelFontChooser.setSelectedFont(font);

        name = LabelPainterController.PREFS.get("branchLabels" + "." + LabelPainterController.FONT_NAME_KEY,
                LabelPainterController.DEFAULT_FONT_NAME);
        size = LabelPainterController.PREFS.getInt("branchLabels" + "." + LabelPainterController.FONT_SIZE_KEY,
                LabelPainterController.DEFAULT_FONT_SIZE);
        style = LabelPainterController.PREFS.getInt("branchLabels" + "." + LabelPainterController.FONT_STYLE_KEY,
                LabelPainterController.DEFAULT_FONT_STYLE);
        font = new Font(name, style, size);
        branchLabelFontChooser.setSelectedFont(font);

        name = ScaleBarPainterController.PREFS.get(ScaleBarPainterController.CONTROLLER_KEY + "." +
                ScaleBarPainterController.FONT_NAME_KEY,
                ScaleBarPainterController.DEFAULT_FONT_NAME);
        size = ScaleBarPainterController.PREFS.getInt(ScaleBarPainterController.CONTROLLER_KEY + "." +
                ScaleBarPainterController.FONT_SIZE_KEY,
                ScaleBarPainterController.DEFAULT_FONT_SIZE);
        style = ScaleBarPainterController.PREFS.getInt(ScaleBarPainterController.CONTROLLER_KEY + "." +
                ScaleBarPainterController.FONT_STYLE_KEY,
                ScaleBarPainterController.DEFAULT_FONT_STYLE);
        font = new Font(name, style, size);
        scaleLabelFontChooser.setSelectedFont(font);
    }

    public void storePreferences() {

        Font font = tipLabelFontChooser.getSelectedFont();
        LabelPainterController.PREFS.put("tipLabels" + "." + LabelPainterController.FONT_NAME_KEY, font.getName());
        LabelPainterController.PREFS.putInt("tipLabels" + "." + LabelPainterController.FONT_SIZE_KEY, font.getSize());
        LabelPainterController.PREFS.putInt("tipLabels" + "." + LabelPainterController.FONT_STYLE_KEY, font.getStyle());

        font = nodeLabelFontChooser.getSelectedFont();
        LabelPainterController.PREFS.put("nodeLabels" + "." + LabelPainterController.FONT_NAME_KEY, font.getName());
        LabelPainterController.PREFS.putInt("nodeLabels" + "." + LabelPainterController.FONT_SIZE_KEY, font.getSize());
        LabelPainterController.PREFS.putInt("nodeLabels" + "." + LabelPainterController.FONT_STYLE_KEY, font.getStyle());

        font = branchLabelFontChooser.getSelectedFont();
        LabelPainterController.PREFS.put("branchLabels" + "." + LabelPainterController.FONT_NAME_KEY, font.getName());
        LabelPainterController.PREFS.putInt("branchLabels" + "." + LabelPainterController.FONT_SIZE_KEY, font.getSize());
        LabelPainterController.PREFS.putInt("branchLabels" + "." + LabelPainterController.FONT_STYLE_KEY, font.getStyle());

        font = scaleLabelFontChooser.getSelectedFont();
        ScaleBarPainterController.PREFS.put(ScaleBarPainterController.CONTROLLER_KEY + "." +
                ScaleBarPainterController.FONT_NAME_KEY, font.getName());
        ScaleBarPainterController.PREFS.putInt(ScaleBarPainterController.CONTROLLER_KEY + "." +
                ScaleBarPainterController.FONT_SIZE_KEY, font.getSize());
        ScaleBarPainterController.PREFS.putInt(ScaleBarPainterController.CONTROLLER_KEY + "." +
                ScaleBarPainterController.FONT_STYLE_KEY, font.getStyle());
    }

    FontChooserPanel tipLabelFontChooser = null;
    FontChooserPanel nodeLabelFontChooser = null;
    FontChooserPanel branchLabelFontChooser = null;
    FontChooserPanel scaleLabelFontChooser = null;

}
