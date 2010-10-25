package figtree.application.menus;

import jam.framework.*;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class PyromaniaMacFileMenuFactory implements MenuFactory {
	public PyromaniaMacFileMenuFactory() {
	}

	public String getMenuName() {
	    return "File";
	}

	public void populateMenu(JMenu menu, AbstractFrame frame) {

	    Application application = Application.getApplication();
	    JMenuItem item;

		item = new JMenuItem(application.getNewAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, MenuBarFactory.MENU_MASK));
		menu.add(item);

		item = new JMenuItem(frame.getCloseWindowAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, MenuBarFactory.MENU_MASK));
		menu.add(item);

		// On Windows and Linux platforms, each window has its own menu so items which are not needed
		// are simply missing. In contrast, on Mac, the menu is for the application so items should
		// be enabled/disabled as frames come to the front.
		if (frame instanceof PyromaniaFileMenuHandler) {
		    Action action = frame.getImportAction();
		    if (action != null) {
		        item = new JMenuItem(action);
		        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, MenuBarFactory.MENU_MASK));
		        menu.add(item);
		    } else {
		        item = new JMenuItem("Import...");
		        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, MenuBarFactory.MENU_MASK));
		        item.setEnabled(false);
		        menu.add(item);
		    }

		    menu.addSeparator();

		} else {
		    // If the frame is not a PyromaniaFileMenuHandler then create a dummy set of disabled menu options.
		    // At present the only situation where this may happen is in Mac OS X when no windows
		    // are open and the menubar is created by the hidden frame.

		    item = new JMenuItem("Import...");
		    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, MenuBarFactory.MENU_MASK));
		    item.setEnabled(false);
		    menu.add(item);

		}

		menu.addSeparator();

		item = new JMenuItem(frame.getPrintAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, MenuBarFactory.MENU_MASK));
		menu.add(item);

		item = new JMenuItem(application.getPageSetupAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, MenuBarFactory.MENU_MASK + ActionEvent.SHIFT_MASK));
		menu.add(item);


	}

	public int getPreferredAlignment() {
	    return LEFT;
	}
}
