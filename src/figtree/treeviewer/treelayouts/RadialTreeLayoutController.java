/*
 * RadialTreeLayoutController.java
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

package figtree.treeviewer.treelayouts;

import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.util.Map;

import figtree.treeviewer.ControllerOptionsPanel;

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
public class RadialTreeLayoutController extends AbstractController {

    private static final String RADIAL_LAYOUT_KEY = "radialLayout";

    private static final String SPREAD_KEY = "spread";

	public RadialTreeLayoutController(final RadialTreeLayout treeLayout) {
		this.treeLayout = treeLayout;

		titleLabel = new JLabel("Radial Layout");

		optionsPanel = new ControllerOptionsPanel(0, 0);

//		final int sliderMax = 100;
//		final JSlider spreadSlider = new JSlider(SwingConstants.HORIZONTAL, 0, sliderMax, 0);
//		spreadSlider.setValue((int)(treeLayout.getSpread() * sliderMax / 2.0));
//
//		spreadSlider.addChangeListener(new ChangeListener() {
//			public void stateChanged(ChangeEvent changeEvent) {
//				double value = spreadSlider.getValue();
//				treeLayout.setSpread((value / sliderMax));
//			}
//		});
//		optionsPanel.addComponentWithLabel("Spread:", spreadSlider, true);

		double spread = treeLayout.getSpread();
		spreadSpinner = new JSpinner(new SpinnerNumberModel(spread, 0, 100, 1));

		optionsPanel.addComponentWithLabel("Spread:", spreadSpinner, true);

		spreadSpinner.addChangeListener(new ChangeListener() {
		    public void stateChanged(ChangeEvent changeEvent) {
		        final double spread = (Double)spreadSpinner.getValue();
				treeLayout.setSpread(spread / 100.0);
		    }
		});
	}

	public JComponent getTitleComponent() {
		return titleLabel;
	}

	public JPanel getPanel() {
		return optionsPanel;
	}

	public boolean isInitiallyVisible() {
		return false;
	}

    public void initialize() {
        // nothing to do
    }

    public void setSettings(Map<String,Object> settings) {
        spreadSpinner.setValue((Double) settings.get(RADIAL_LAYOUT_KEY + "." + SPREAD_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(RADIAL_LAYOUT_KEY + "." + SPREAD_KEY, spreadSpinner.getValue());
    }

	private final JLabel titleLabel;
	private final OptionsPanel optionsPanel;

	private final JSpinner spreadSpinner;

	private final RadialTreeLayout treeLayout;

}
