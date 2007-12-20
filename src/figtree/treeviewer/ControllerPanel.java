package figtree.treeviewer;

import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import java.awt.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class ControllerPanel extends OptionsPanel {

	public ControllerPanel() {
		super();
	}

	public ControllerPanel(int hGap, int vGap) {
		super(hGap, vGap);
	}

	protected void adjustComponent(JComponent comp) {
		comp.putClientProperty("Quaqua.Component.visualMargin", new Insets(0,0,0,0));
		comp.setFont(UIManager.getFont("SmallSystemFont"));
		comp.putClientProperty("JComponent.sizeVariant", "small");
		if (comp instanceof JButton) {
			comp.putClientProperty("JButton.buttonType", "roundRect");
		}
		if (comp instanceof JComboBox) {
			comp.putClientProperty("JComboBox.isSquare", Boolean.TRUE);
		}
	}
}
