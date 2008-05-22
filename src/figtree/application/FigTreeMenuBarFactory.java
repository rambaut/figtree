package figtree.application;

import org.virion.jam.framework.*;
import org.virion.jam.mac.*;
import figtree.application.menus.*;


public class FigTreeMenuBarFactory extends DefaultMenuBarFactory {

	public FigTreeMenuBarFactory() {
		if (org.virion.jam.mac.Utils.isMacOSX()) {
			registerMenuFactory(new FigTreeMacFileMenuFactory());
			registerMenuFactory(new DefaultEditMenuFactory());
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