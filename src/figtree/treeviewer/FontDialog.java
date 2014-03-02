/*
 * FontDialog.java
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

import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import figtree.treeviewer.painters.LabelPainterController;
import figtree.ui.FontChooserPanel;

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
public class FontDialog {
    private OptionsPanel options;
    private final JFrame frame;
    private FontChooserPanel chooser = null;

    public FontDialog(final JFrame frame) {
        this.frame = frame;

    }

    public Font getFont() {
        return chooser.getSelectedFont();
    }

    public int showDialog(final Font font) {

        options = new OptionsPanel();
        if (chooser == null) {
            chooser = new FontChooserPanel(font);
        } else {
            chooser.setSelectedFont(font);
        }
        options.addSpanningComponent(chooser);

        JOptionPane optionPane = new JOptionPane(options,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null,
                null,
                null);
        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));

        final JDialog dialog = optionPane.createDialog(frame, "Setup colour range");
        dialog.pack();

        dialog.setVisible(true);

        int result = JOptionPane.CANCEL_OPTION;
        Integer value = (Integer)optionPane.getValue();
        if (value != null && value.intValue() != -1) {
            result = value.intValue();
        }

        return result;
    }
}
