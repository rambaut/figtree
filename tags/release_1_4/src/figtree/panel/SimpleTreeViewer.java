/*
 * SimpleTreeViewer.java
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

/*
 * ExtendedTreeViewer.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package figtree.panel;

import figtree.treeviewer.AnnotationsListener;
import figtree.treeviewer.DefaultTreeViewer;
import figtree.treeviewer.annotations.AnnotationDefinition;
import jam.panels.StatusListener;
import jam.panels.StatusProvider;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A panel that displays correlation plots of 2 traces
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: ExtendedTreeViewer.java,v 1.38 2007/09/05 16:24:23 rambaut Exp $
 */
public class SimpleTreeViewer extends DefaultTreeViewer implements StatusProvider {
	/** Creates new AlignmentPanel */
	public SimpleTreeViewer() {
		super();

		// setTreesPerPage(1);

		setBackground(Color.white);

	}

	private List<AnnotationsListener> listeners = new ArrayList<AnnotationsListener>();

    public void addAnnotationsListener(AnnotationsListener listener) {
		listeners.add(listener);
	}

	public void fireAnnotationsChanged() {
		for (AnnotationsListener listener : listeners) {
			listener.annotationsChanged();
		}
	}

	private List<AnnotationDefinition> annotations = null;
	private final Helper statusHelper = new Helper();

	public void addStatusListener(StatusListener statusListener) {
		statusHelper.addStatusListener(statusListener);
	}

	public void removeStatusListener(StatusListener statusListener) {
		statusHelper.removeStatusListener(statusListener);
	}

	public void fireStatusChanged(int status, String statusText) {
		statusHelper.fireStatusChanged(status, statusText);
	}

	public void addOverrideProvider(StatusProvider provider) {
		statusHelper.addOverrideProvider(provider);
	}

	public void removeOverrideProvider(StatusProvider provider) {
		statusHelper.removeOverrideProvider(provider);
	}

	public void fireStatusButtonPressed() {
		statusHelper.fireStatusButtonPressed();
	}

	public void statusButtonPressed() {
		statusHelper.statusButtonPressed();
	}

	public int getStatus() {
		return statusHelper.getStatus();
	}

	public String getStatusText() {
		return statusHelper.getStatusText();
	}

	public void setStatusText(String statusText) {
		statusHelper.fireStatusChanged(0, statusText);
	}

}