/*
 * FigTreeDefaultFileMenuFactory.java
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

package figtree.application.menus;

import jam.framework.*;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class FigTreeDefaultFileMenuFactory implements MenuFactory {
	public FigTreeDefaultFileMenuFactory() {
	}

	public String getMenuName() {
		return "File";
	}

	public void populateMenu(JMenu menu, AbstractFrame frame) {

		JMenuItem item;

		Application application = Application.getApplication();
		menu.setMnemonic('F');

		item = new JMenuItem(application.getNewAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, MenuBarFactory.MENU_MASK));
		menu.add(item);

		item = new JMenuItem(application.getOpenAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, MenuBarFactory.MENU_MASK));
		menu.add(item);

		item = new JMenuItem(frame.getSaveAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, MenuBarFactory.MENU_MASK));
		menu.add(item);

		item = new JMenuItem(frame.getSaveAsAction());
		menu.add(item);

		// On Windows and Linux platforms, each window has its own menu so items which are not needed
		// are simply missing. In contrast, on Mac, the menu is for the application so items should
		// be enabled/disabled as frames come to the front.
		if (frame instanceof FigTreeFileMenuHandler) {
		    Action action = frame.getImportAction();
		    if (action != null) {
		        item = new JMenuItem(action);
		        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, MenuBarFactory.MENU_MASK));
		        menu.add(item);

                item = new JMenuItem(((FigTreeFileMenuHandler)frame).getImportColourSchemeAction());
                menu.add(item);

                menu.addSeparator();
		    }

            item = new JMenuItem(((FigTreeFileMenuHandler)frame).getExportTreesAction());
		    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, MenuBarFactory.MENU_MASK));
		    menu.add(item);

		    item = new JMenuItem(((FigTreeFileMenuHandler)frame).getExportGraphicAction());
		    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, MenuBarFactory.MENU_MASK + KeyEvent.ALT_MASK));
		    menu.add(item);

            item = new JMenuItem(((FigTreeFileMenuHandler)frame).getExportPDFAction());
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, MenuBarFactory.MENU_MASK + KeyEvent.SHIFT_MASK + KeyEvent.ALT_MASK));
            menu.add(item);

            /* to be added ... */
//            menu.addSeparator();
//
//            item = new JMenuItem(((FigTreeFileMenuHandler)frame).getImportColourSchemeAction());
//            menu.add(item);
//
//            item = new JMenuItem(((FigTreeFileMenuHandler)frame).getExportColourSchemeAction());
//            menu.add(item);

        } else {
		    // If the frame is not a TracerFileMenuHandler then leave out the import/export options.
		}

		menu.addSeparator();

		item = new JMenuItem(frame.getPrintAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, MenuBarFactory.MENU_MASK));
		menu.add(item);

		item = new JMenuItem(application.getPageSetupAction());
		menu.add(item);

		menu.addSeparator();

		if (application.getRecentFileMenu() != null) {
			JMenu subMenu = application.getRecentFileMenu();
			menu.add(subMenu);

			menu.addSeparator();
		}

		item = new JMenuItem(application.getExitAction());
		menu.add(item);

	}

	public int getPreferredAlignment() {
		return LEFT;
	}
}
