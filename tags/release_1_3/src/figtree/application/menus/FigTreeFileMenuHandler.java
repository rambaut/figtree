package figtree.application.menus;

import javax.swing.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public interface FigTreeFileMenuHandler {
	Action getExportTreesAction();

	Action getExportGraphicAction();

    Action getExportPDFAction();
}
