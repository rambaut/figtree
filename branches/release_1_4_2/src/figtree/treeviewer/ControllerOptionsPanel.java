/*
 * ControllerOptionsPanel.java
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
import javax.swing.text.JTextComponent;
import java.awt.*;

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
public class ControllerOptionsPanel extends OptionsPanel {

    public ControllerOptionsPanel() {
        super();
    }

    public ControllerOptionsPanel(int hGap, int vGap) {
        super(hGap, vGap);
    }

    protected void adjustComponent(JComponent comp) {
        setComponentLook(comp);
    }

    public static void setComponentLook(JComponent comp) {
        comp.putClientProperty("Quaqua.Component.visualMargin", new Insets(0,0,0,0));
        Font font = UIManager.getFont("SmallSystemFont");
        if (font != null) {
            comp.setFont(font);
        }
        comp.putClientProperty("JComponent.sizeVariant", "small");
        if (comp instanceof JSpinner && font != null) {
            ((JSpinner.NumberEditor)((JSpinner)comp).getEditor()).getTextField().setFont(font);
        }
        if (comp instanceof JButton) {
            comp.putClientProperty("JButton.buttonType", "roundRect");
        }
        if (comp instanceof JComboBox) {
            //comp.putClientProperty("JComboBox.isSquare", Boolean.TRUE);
        }
        if (!(comp instanceof JTextField)) {
            comp.setFocusable(false);
        }
    }
}
