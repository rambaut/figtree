package figtree.application.menus;

import jam.framework.*;
import jam.mac.*;


public class PyromaniaMenuBarFactory extends DefaultMenuBarFactory {

	public PyromaniaMenuBarFactory() {
		if (jam.mac.Utils.isMacOSX()) {
			registerMenuFactory(new PyromaniaMacFileMenuFactory());
			registerMenuFactory(new DefaultEditMenuFactory());

			registerMenuFactory(new MacWindowMenuFactory());
			registerMenuFactory(new MacHelpMenuFactory());
		} else {
			registerMenuFactory(new PyromaniaDefaultFileMenuFactory());
			registerMenuFactory(new DefaultEditMenuFactory());

			registerMenuFactory(new DefaultHelpMenuFactory());
		}
	}

}