/*
 * ExtendedTreeViewer.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package figtree.treeviewer;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.alignments.Pattern;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.Tree;
import figtree.treeviewer.DefaultTreeViewer;
import org.virion.jam.panels.StatusListener;
import org.virion.jam.panels.StatusProvider;
import figtree.treeviewer.annotations.AnnotationDefinition;
import figtree.treeviewer.painters.CharactersPainter;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * A panel that displays correlation plots of 2 traces
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: ExtendedTreeViewer.java,v 1.38 2007/09/05 16:24:23 rambaut Exp $
 */
public class ExtendedTreeViewer extends DefaultTreeViewer implements StatusProvider {
	/** Creates new AlignmentPanel */
	public ExtendedTreeViewer() {
		super();

		// setTreesPerPage(1);

		setBackground(Color.white);

	}

	public void setPattern(Pattern pattern) {
		if (pattern != null) {
			//           setBranchDecorator(new ParsimonyBranchDecorator(pattern));
		} else {
			setBranchDecorator(null);
		}
	}

	public void addTree(Tree tree) {

		Set<String> names = new TreeSet<String>();
		for (Node node : tree.getNodes()) {
			names.addAll(node.getAttributeNames());
		}
		for (String name : names) {
			AnnotationDefinition annotation = new AnnotationDefinition(name, AnnotationDefinition.Type.REAL);
			getAnnotationDefinitions().add(annotation);
		}
		super.addTree(tree);

		showStatus();
	}

	public void showTree(int index) {
		super.showTree(index);
		showStatus();
	}

	public void setCharacters(Alignment characters) {
		CharactersPainter painter = new CharactersPainter(characters);
		setTipLabelPainter(painter);
	}

	public void setTaxonAnnotations(Map<AnnotationDefinition, Map<Taxon, Object>> annotations) {
		for (AnnotationDefinition definition: annotations.keySet()) {
			getAnnotationDefinitions().add(definition);
			Map<Taxon, Object> annotation = annotations.get(definition);
			for (Taxon taxon : annotation.keySet()) {
				taxon.setAttribute(definition.getName(), annotation.get(taxon));
			}
		}
	}

	public void showStatus() {
		fireStatusChanged(0, "Showing tree " + Integer.toString(getCurrentTreeIndex() + 1) + " / " + getTreeCount());
	}

	public void showInfomation() {

	}

	public void showStatistics() {
	}

	public void annotateSelected(String name, Object value) {
		annotateSelectedNodes(name, value);
		annotateSelectedTips(name, value);
		fireAnnotationsChanged();
	}

	public List<AnnotationDefinition> getAnnotationDefinitions() {
		if (annotations == null) {
			annotations = new ArrayList<AnnotationDefinition>();
		}
		return annotations;
	}

	public void annotateFromTips(String annotationName) {
		List<Object> stateCodes = new ArrayList<Object>();
		Map<Taxon, Integer> stateMap = new HashMap<Taxon, Integer>();

		Tree tree = treePane.getTree();
		int index = 0;
		for (Node node : tree.getExternalNodes()) {
			Taxon taxon = tree.getTaxon(node);
			Object state = taxon.getAttribute(annotationName);
			stateCodes.add(state);
			stateMap.put(taxon, index);
			node.setAttribute(annotationName, state);
			index ++;
		}



		Parsimony parsimony = new Parsimony(stateCodes.size(), stateMap);

		for (Node node : tree.getInternalNodes()) {
			Object state = stateCodes.get(parsimony.getState(tree, node));
			node.setAttribute(annotationName, state);
		}

		fireAnnotationsChanged();
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
	private final StatusProvider.Helper statusHelper = new StatusProvider.Helper();

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