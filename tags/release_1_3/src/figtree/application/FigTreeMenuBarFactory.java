package figtree.application;

import jam.framework.*;
import jam.mac.*;
import figtree.application.menus.*;


public class        FigTreeMenuBarFactory extends DefaultMenuBarFactory {

	public FigTreeMenuBarFactory() {
		if (jam.mac.Utils.isMacOSX()) {
			registerMenuFactory(new FigTreeMacFileMenuFactory());
			registerMenuFactory(new MacEditMenuFactory());
			registerMenuFactory(new TreeMenuFactory());

			registerMenuFactory(new MacWindowMenuFactory());
			registerMenuFactory(new MacHelpMenuFactory());
		} else {
			registerMenuFactory(new FigTreeDefaultFileMenuFactory());
			registerMenuFactory(new DefaultEditMenuFactory());
			registerMenuFactory(new TreeMenuFactory());
			registerMenuFactory(new DefaultHelpMenuFactory());
		}
	}

}