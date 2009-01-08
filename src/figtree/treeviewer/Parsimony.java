package figtree.treeviewer;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.*;

import java.util.*;

public class Parsimony {

	private final int stateCount;
	private final Map<Taxon, Integer> stateMap;

	private Map<Node, boolean[]> stateSets = new HashMap<Node, boolean[]>();
	private Map<Node, Integer> states = new HashMap<Node, Integer>();

//    private boolean[][] union;         // Must now be local to recursive function
//    private boolean[][] intersection;  // as nodes are not guaranteed to be called in post-order

	private RootedTree tree = null;
	private List<Taxon> taxa;

	private boolean hasCalculatedSteps = false;
	private boolean hasRecontructedStates = false;


	public Parsimony(int stateCount, Map<Taxon, Integer> stateMap) {

		this.stateCount = stateCount;
		this.stateMap = stateMap;
	}

	/**
	 * Returns the reconstructed character states for a given node in the tree. If this method is repeatedly
	 * called with the same tree and patterns then only the first call will reconstruct the states and each
	 * subsequent call will return the stored states.
	 *
	 * @param tree a tree object to reconstruct the characters on
	 * @param node the node of the tree
	 * @return an array containing the reconstructed states for this node
	 */
	public Integer getState(Tree tree, Node node) {

		if (tree == null) {
			throw new IllegalArgumentException("The tree cannot be null");
		}

		if (!(tree instanceof RootedTree)) {
			throw new IllegalArgumentException("The tree must be an instance of rooted tree");
		}

		if (this.tree == null || this.tree != tree) {
			this.tree = (RootedTree) tree;

			if (!Utils.isBinary(this.tree)) {
				throw new IllegalArgumentException("The Fitch algorithm can only reconstruct ancestral states on binary trees");
			}

			initialize();
		}

		if (!hasCalculatedSteps) {
			calculateSteps(this.tree); //this.tree.getRootNode());
			hasCalculatedSteps = true;
		}

		if (!hasRecontructedStates) {
//			reconstructStates(this.tree.getRootNode(), -1);
            reconstructStates2(this.tree.getRootNode(), null);
			hasRecontructedStates = true;
		}

		return states.get(node);
	}

	private void initialize() {
		hasCalculatedSteps = false;
		hasRecontructedStates = false;

		for (Node node : tree.getNodes()) {
			boolean[] stateSet = new boolean[stateCount];
			stateSets.put(node, stateSet);
		}
	}

	/**
	 * This is the first pass of the Fitch algorithm. This calculates the set of states
	 * at each node and counts the total number of siteScores (the score). If that is all that
	 * is required then the second pass is not necessary.
	 */
	private void calculateSteps(RootedTree tree) {

		// nodes in pre-order
		final List<Node> nodes = Utils.getNodes(tree, tree.getRootNode());

		// used as locals in the loop below, allocated once
		boolean[] union = new boolean[stateCount];
		boolean[] intersection = new boolean[stateCount];

		// iterate in reverse - post order. State of child is gurantted to be reasy before parent

		for (int k = nodes.size() - 1; k >= 0; --k) {
			final Node node = nodes.get(k);
			final boolean[] nodeStateSet = stateSets.get(node);

			if (tree.isExternal(node)) {
				boolean[] stateSet = stateSets.get(node);


				Taxon taxon = tree.getTaxon(node);

				int state = stateMap.get(taxon);

				stateSet[state] = true;
			} else {
				boolean first = true;
				for (Node child : tree.getChildren(node)) {
					boolean[] childStateSet = stateSets.get(child);
					if (first) {
						copyOf(childStateSet, union);
						copyOf(childStateSet, intersection);
						first = false;
					} else {
						unionOf(union, childStateSet, union);
						intersectionOf(intersection, childStateSet, intersection);
					}
				}

				if (sizeOf(intersection) > 0) {
					copyOf(intersection, nodeStateSet);
				} else {
					copyOf(union, nodeStateSet);
				}
			}
		}
	}



	/**
	 * The second pass of the Fitch algorithm. This reconstructs the ancestral states at
	 * each node.
	 *
	 * @param node
	 * @param parentState
	 */
	private void reconstructStates(Node node, int parentState) {

		if (!tree.isExternal(node)) {
			boolean[] nodeStateSet = stateSets.get(node);
			Integer nodeState = null;

			if (parentState != -1 && nodeStateSet[parentState]) {
				nodeState = parentState;
			} else {
				nodeState = firstIndexOf(nodeStateSet);
			}

			for (Node child : tree.getChildren(node)) {
				reconstructStates(child, nodeState);
			}

			states.put(node, nodeState);

		}
	}

	private boolean[] reconstructStates2(Node node, boolean[] parentStateSet) {
		boolean[] nodeStateSet = stateSets.get(node);

		if (!tree.isExternal(node)) {

			boolean[] union = new boolean[stateCount];
			boolean first = true;
			for (Node child : tree.getChildren(node)) {
				boolean[] childStateSet = reconstructStates2(child, nodeStateSet);
				if (first) {
					copyOf(childStateSet, union);
					first = false;
				} else {
					unionOf(union, childStateSet, union);
				}
			}

			if (parentStateSet != null) {
				boolean[] intersection = new boolean[stateCount];
				intersectionOf(parentStateSet, union, intersection);
				if (sizeOf(intersection) > 0) {
					nodeStateSet = intersection;
				}
			}


			if (sizeOf(nodeStateSet) == 1) {
				states.put(node, firstIndexOf(nodeStateSet));
			}
		}

		return nodeStateSet;
	}

	private static void copyOf(boolean[] s, boolean[] d) {

		for (int i = 0; i < d.length; i++) {
			d[i] = s[i];
		}
	}

	private static void unionOf(boolean[] s1, boolean[] s2, boolean[] d) {

		for (int i = 0; i < d.length; i++) {
			d[i] = s1[i] || s2[i];
		}
	}

	private static void intersectionOf(boolean[] s1, boolean[] s2, boolean[] d) {

		for (int i = 0; i < d.length; i++) {
			d[i] = s1[i] && s2[i];
		}
	}

	private static int firstIndexOf(boolean[] s1) {

		for (int i = 0; i < s1.length; i++) {
			if (s1[i]) {
				return i;
			}
		}
		return -1;
	}

	private static int sizeOf(boolean[] s1) {

		int count = 0;
		for (int i = 0; i < s1.length; i++) {
			if (s1[i]) count += 1;
		}
		return count;
	}

}