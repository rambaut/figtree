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