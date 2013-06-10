package figtree.treeviewer;

import jebl.evolution.graphs.*;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.*;
import jebl.util.AttributableHelper;
import jebl.util.HashPair;

import java.util.*;

/**
 * A rooted tree concrete class that wraps another tree and provides a differently
 * rooted view of that tree.
 * @author Andrew Rambaut
 * @version $Id$
 */
final public class ReRootedTree implements RootedTree {

    /**
     * Make a copy of the given unrooted tree
     * @param source an unrooted source tree
     * @param ingroupNode the node on one side of the root
     * @param outgroupNode the node on the other side of the root
     * @param ingroupBranchLength the branch length from the root to the ingroup node
     * @throws jebl.evolution.graphs.Graph.NoEdgeException
     */
    public ReRootedTree(RootedTree source, Node ingroupNode, Node outgroupNode, double ingroupBranchLength) throws NoEdgeException {

        this.source = source;
        List<Node> children = new ArrayList<Node>();

        Node node1 = createNodes(source, outgroupNode, ingroupNode);
        setLength(node1, ingroupBranchLength);
        children.add(node1);

        Node node2 = createNodes(source, ingroupNode, outgroupNode);
        double l = source.getEdgeLength(ingroupNode, outgroupNode);
        if (outgroupNode == source.getRootNode()) {
            // the tree is already rooted at the required location
            for (Node adj : source.getAdjacencies(outgroupNode)) {
                if (adj != ingroupNode) {
                   l += source.getEdgeLength(outgroupNode, adj);
                }
            }
        }
        setLength(node2, Math.max(l - ingroupBranchLength, 0.0));
        children.add(node2);

        createInternalNode(null, children);
    }

    /**
     * Clones the entire tree structure from the given (unrooted) Tree.
     * @param tree the unrooted tree
     * @param parent the parent node
     * @param child the child node
     */
    public Node createNodes(RootedTree tree, Node parent, Node child) throws NoEdgeException {

        Node newNode = null;
        double length;

        if (tree.isExternal(child)) {
            newNode = createExternalNode(child, tree.getTaxon(child));
            length = tree.getEdgeLength(parent, child);
        } else {
            List<Node> adjacencies = tree.getAdjacencies(child);

            if (adjacencies.size() == 2) {
                // this is the root node so skip over it...
                if (adjacencies.get(0) == parent) {
                    newNode = createNodes(tree, child, adjacencies.get(1));
                } else {
                    newNode = createNodes(tree, child, adjacencies.get(0));
                }
                length = tree.getEdgeLength(adjacencies.get(0), child) +
                        tree.getEdgeLength(adjacencies.get(1), child);

            } else {
                List<Node> children = new ArrayList<Node>();

                for (Node child2 : adjacencies) {
                    if (child2 != parent) {
                        children.add(createNodes(tree, child, child2));
                    }
                }

                if (tree.getParent(parent) == child) {
                    newNode = createInternalNode(parent, children);
                } else {
                    newNode = createInternalNode(child, children);
                }
                length = tree.getEdgeLength(parent, child);
            }
        }

        setLength(newNode, length);

        return newNode;
    }

    /**
     * Creates a new external node with the given taxon. See createInternalNode
     * for a description of how to use these methods.
     * @param source the source node
     * @return the created node reference
     */
    private Node createExternalNode(Node source, Taxon taxon) {
        ReRootedNode node = new ReRootedNode(source, taxon);
        externalNodes.put(taxon, node);
        return node;
    }

    /**
     * Once a SimpleRootedTree has been created, the node stucture can be created by
     * calling createExternalNode and createInternalNode. First of all createExternalNode
     * is called giving Taxon objects for the external nodes. Then these are put into
     * sets and passed to createInternalNode to create a parent of these nodes. The
     * last node created using createInternalNode is automatically the root so when
     * all the nodes are created, the tree is complete.
     *
     * @param children the child nodes of this nodes
     * @return the created node reference
     */
    private ReRootedNode createInternalNode(Node source, List<? extends Node> children) {
        ReRootedNode node = new ReRootedNode(source, children);

        for (Node child : children) {
            ((ReRootedNode)child).setParent(node);
        }

        internalNodes.add(node);

        rootNode = node;
        return node;
    }

    public Node getSourceNode(Node node) {
        return ((ReRootedNode)node).source;
    }

    /**
     * @param node the node whose height is being set
     * @param height the height
     */
    public void setHeight(Node node, double height) {
        lengthsKnown = false;
        heightsKnown = true;

        // If a single height of a single node is set then
        // assume that all nodes have heights and by extension,
        // branch lengths as well as these will be calculated
        // from the heights
        hasLengths = true;
        hasHeights = true;

        ((ReRootedNode)node).setHeight(height);
    }

    /**
     * @param node the node whose branch length (to its parent) is being set
     * @param length the length
     */
    public void setLength(Node node, double length) {
        heightsKnown = false;
        lengthsKnown = true;

        // If a single length of a single branch is set then
        // assume that all branch have lengths and by extension,
        // node heights as well as these will be calculated
        // from the lengths
        hasLengths = true;
        hasHeights = true;

        ((ReRootedNode)node).setLength(length);
    }

    /**
     * @param node the node whose children are being requested.
     * @return the list of nodes that are the children of the given node.
     *         The list may be empty for a terminal node (a tip).
     */
    public List<Node> getChildren(Node node) {
        return new ArrayList<Node>(((ReRootedNode)node).getChildren());
    }

    /**
     * @return Whether this tree has node heights available
     */
    public boolean hasHeights() {
        return hasHeights;
    }

    /**
     * @param node the node whose height is being requested.
     * @return the height of the given node. The height will be
     *         less than the parent's height and greater than it children's heights.
     */
    public double getHeight(Node node) {
        if (!hasHeights) throw new IllegalArgumentException("This tree has no node heights");
        if (!heightsKnown) calculateNodeHeights();
        return ((ReRootedNode)node).getHeight();
    }

    /**
     * @return Whether this tree has branch lengths available
     */
    public boolean hasLengths() {
        return hasLengths;
    }

    /**
     * @param node the node whose branch length (to its parent) is being requested.
     * @return the length of the branch to the parent node (0.0 if the node is the root).
     */
    public double getLength(Node node) {
        if (!hasLengths) throw new IllegalArgumentException("This tree has no branch lengths");
        if (!lengthsKnown) calculateBranchLengths();
        return ((ReRootedNode)node).getLength();
    }

    /**
     * @param node the node whose parent is requested
     * @return the parent node of the given node, or null
     *         if the node is the root node.
     */
    public Node getParent(Node node) {
        if (!(node instanceof ReRootedNode)) {
            throw new IllegalArgumentException("Node, " + node.toString() + " is not an instance of SimpleRootedNode");
        }
        return ((ReRootedNode)node).getParent();
    }

    public Edge getParentEdge(Node node) {
        if (!(node instanceof ReRootedNode)) {
            throw new IllegalArgumentException("Node, " + node.toString() + " is not an instance of SimpleRootedNode");
        }
        return ((ReRootedNode)node).getEdge();
    }

    /**
     * The root of the tree has the largest node height of
     * all nodes in the tree.
     *
     * @return the root of the tree.
     */
    public Node getRootNode() {
        return rootNode;
    }


    /**
     * @return a set of all nodes that have degree 1.
     *         These nodes are often refered to as 'tips'.
     */
    public Set<Node> getExternalNodes() {
        return new LinkedHashSet<Node>(externalNodes.values());
    }

    /**
     * @return a set of all nodes that have degree 2 or more.
     *         These nodes are often refered to as internal nodes.
     */
    public Set<Node> getInternalNodes() {
        return new LinkedHashSet<Node>(internalNodes);
    }

    /**
     * @return the set of taxa associated with the external
     *         nodes of this tree. The size of this set should be the
     *         same as the size of the external nodes set.
     */
    public Set<Taxon> getTaxa() {
        return new LinkedHashSet<Taxon>(externalNodes.keySet());
    }

    /**
     * @param node the node whose associated taxon is being requested.
     * @return the taxon object associated with the given node, or null
     *         if the node is an internal node.
     */
    public Taxon getTaxon(Node node) {
        if (!(node instanceof ReRootedNode)) {
            throw new IllegalArgumentException("Node, " + node.toString() + " is not an instance of SimpleRootedNode.  It is an instance of "+node.getClass().getName());
        }
        return ((ReRootedNode)node).getTaxon();
    }

    /**
     * @param node the node
     * @return true if the node is of degree 1.
     */
    public boolean isExternal(Node node) {
        if (!(node instanceof ReRootedNode)) {
            throw new IllegalArgumentException("Node, " + node.toString() + " is not an instance of SimpleRootedNode.  It is an instance of "+node.getClass().getName());
        }
        return ((ReRootedNode)node).getChildren().size() == 0;
    }

    /**
     * @param taxon the taxon
     * @return the external node associated with the given taxon, or null
     *         if the taxon is not a member of the taxa set associated with this tree.
     */
    public Node getNode(Taxon taxon) {
        return externalNodes.get(taxon);
    }

    public void renameTaxa(Taxon from, Taxon to) {
        ReRootedNode node = (ReRootedNode)externalNodes.get(from);

        // TT: The javadoc doesn't specify whether renameTaxa() should fail or silently do nothing
        // if Taxon from doesn't exist. But the code already threw a NullPointerException before (bug 4824),
        // so it's probably ok to throw a more informative IllegalArgumentException instead.
        if (node == null) {
            throw new IllegalArgumentException("Unknown taxon " + from + "; can't rename to " + to);
        }

        node.setTaxon(to);

        externalNodes.remove(from);
        externalNodes.put(to, node);
    }

    /**
     * Returns a list of edges connected to this node
     *
     * @param node
     * @return the set of nodes that are attached by edges to the given node.
     */
    public List<Edge> getEdges(Node node) {
        List<Edge> edges = new ArrayList<Edge>();
        for (Node adjNode : getAdjacencies(node)) {
            edges.add(((ReRootedNode)adjNode).getEdge());

        }
        return edges;
    }

    /**
     * @param node
     * @return the set of nodes that are attached by edges to the given node.
     */
    public List<Node> getAdjacencies(Node node) {
        return ((ReRootedNode)node).getAdjacencies();
    }

    /**
     * Returns the Edge that connects these two nodes
     *
     * @param node1
     * @param node2
     * @return the edge object.
     * @throws jebl.evolution.graphs.Graph.NoEdgeException
     *          if the nodes are not directly connected by an edge.
     */
    public Edge getEdge(Node node1, Node node2) throws NoEdgeException {
        if (((ReRootedNode)node1).getParent() == node2) {
            return ((ReRootedNode)node1).getEdge();
        } else if (((ReRootedNode)node2).getParent() == node1) {
            return ((ReRootedNode)node2).getEdge();
        } else {
            throw new NoEdgeException();
        }
    }

    /**
     * @param node1
     * @param node2
     * @return the length of the edge connecting node1 and node2.
     * @throws jebl.evolution.graphs.Graph.NoEdgeException
     *          if the nodes are not directly connected by an edge.
     */
    public double getEdgeLength(Node node1, Node node2) throws NoEdgeException {
        if (((ReRootedNode)node1).getParent() == node2) {
            if (heightsKnown) {
                return ((ReRootedNode)node2).getHeight() - ((ReRootedNode)node1).getHeight();
            } else {
                return ((ReRootedNode)node1).getLength();
            }
        } else if (((ReRootedNode)node2).getParent() == node1) {
            if (heightsKnown) {
                return ((ReRootedNode)node1).getHeight() - ((ReRootedNode)node2).getHeight();
            } else {
                return ((ReRootedNode)node2).getLength();
            }
        } else {
            throw new NoEdgeException();
        }
    }

    /**
     * Returns an array of 2 nodes which are the nodes at either end of the edge.
     *
     * @param edge
     * @return an array of 2 edges
     */
    public Node[] getNodes(Edge edge) {
        for (Node node : getNodes()) {
            if (((ReRootedNode)node).getEdge() == edge) {
                return new Node[] { node, ((ReRootedNode)node).getParent() };
            }
        }
        return null;
    }

    /**
     * @return the set of all nodes in this graph.
     */
    public Set<Node> getNodes() {
        Set<Node> nodes = new LinkedHashSet<Node>(internalNodes);
        nodes.addAll(externalNodes.values());
        return nodes;
    }

    /**
     * @return the set of all edges in this graph.
     */
    public Set<Edge> getEdges() {
        Set<Edge> edges = new LinkedHashSet<Edge>();
        for (Node node : getNodes()) {
            if (node != getRootNode()) {
                edges.add(((ReRootedNode)node).getEdge());
            }

        }
        return edges;
    }

    /**
     * The set of external edges. This is a pretty inefficient implementation because
     * a new set is constructed each time this is called.
     * @return the set of external edges.
     */
    public Set<Edge> getExternalEdges() {
        Set<Edge> edges = new LinkedHashSet<Edge>();
        for (Node node : getExternalNodes()) {
            edges.add(((ReRootedNode)node).getEdge());
        }
        return edges;
    }

    /**
     * The set of internal edges. This is a pretty inefficient implementation because
     * a new set is constructed each time this is called.
     * @return the set of internal edges.
     */
    public Set<Edge> getInternalEdges() {
        Set<Edge> edges = new LinkedHashSet<Edge>();
        for (Node node : getInternalNodes()) {
            if (node != getRootNode()) {
                edges.add(((ReRootedNode)node).getEdge());
            }
        }
        return edges;
    }

    /**
     * @param degree the number of edges connected to a node
     * @return a set containing all nodes in this graph of the given degree.
     */
    public Set<Node> getNodes(int degree) {
        Set<Node> nodes = new LinkedHashSet<Node>();
        for (Node node : getNodes()) {
            // Account for no anncesstor of root, assumed by default in getDegree
            final int deg = node.getDegree() ;
            if (deg == degree) nodes.add(node);
        }
        return nodes;
    }

    /**
     * Set the node heights from the current branch lengths.
     */
    private void calculateNodeHeights() {

        if (!lengthsKnown) {
            throw new IllegalArgumentException("Can't calculate node heights because branch lengths not known");
        }

        nodeLengthsToHeights(rootNode, 0.0);

        double maxHeight = 0.0;
        for (Node externalNode : getExternalNodes()) {
            if (((ReRootedNode)externalNode).getHeight() > maxHeight) {
                maxHeight = ((ReRootedNode)externalNode).getHeight();
            }
        }

        for (Node node : getNodes()) {
            ((ReRootedNode)node).setHeight(maxHeight - ((ReRootedNode)node).getHeight());
        }

        heightsKnown = true;
    }

    /**
     * Set the node heights from the current node branch lengths. Actually
     * sets distance from root so the heights then need to be reversed.
     */
    private void nodeLengthsToHeights(ReRootedNode node, double height) {

        double newHeight = height;

        if (node.getLength() > 0.0) {
            newHeight += node.getLength();
        }

        node.setHeight(newHeight);

        for (Node child : node.getChildren()) {
            nodeLengthsToHeights((ReRootedNode)child, newHeight);
        }
    }

    /**
     * Calculate branch lengths from the current node heights.
     */
    protected void calculateBranchLengths() {

        if (!hasLengths) {
            throw new IllegalArgumentException("Can't calculate branch lengths because node heights not known");
        }

        nodeHeightsToLengths(rootNode, getHeight(rootNode));

        lengthsKnown = true;
    }

    /**
     * Calculate branch lengths from the current node heights.
     */
    private void nodeHeightsToLengths(ReRootedNode node, double height) {
        final double h = node.getHeight();
        node.setLength(h >= 0 ? height - h : 1);

        for (Node child : node.getChildren()) {
            nodeHeightsToLengths((ReRootedNode)child, node.getHeight());
        }

    }

    public boolean conceptuallyUnrooted() {
        return false;
    }

    public boolean isRoot(Node node) {
        return node == rootNode;
    }

    // Attributable IMPLEMENTATION

    public void setAttribute(String name, Object value) {
        source.setAttribute(name, value);
    }

    public Object getAttribute(String name) {
        return source.getAttribute(name);
    }

    public void removeAttribute(String name) {
        source.removeAttribute(name);
    }

    public Set<String> getAttributeNames() {
        return source.getAttributeNames();
    }

    public Map<String, Object> getAttributeMap() {
        return source.getAttributeMap();
    }

    /**
     * Root any tree by locating the "center" of tree and adding a new root node at that point
     * <p/>
     * for any point on the tree x let D(x) = Max{distance between x and t : for all tips t}
     * The "center" c is the point with the smallest distance, i.e. D(c) = min{ D(x) : x in tree }
     *
     * @param tree to root
     * @return rooted tree
     */
    public static RootedTree rootTreeAtCenter(RootedTree tree) {
        // Method - find the pair of tips with the longest distance. It is easy to see that the center
        // is at the midpoint of the path between them.

        HashMap<HashPair<Node>, Double> dists = new LinkedHashMap<HashPair<Node>, Double>();
        try {
            double maxDistance = -Double.MAX_VALUE;
            // node on maximal path
            Node current = null;
            // next node on maximal path
            Node direction = null;

            // locate one terminal node of longest path
            for (Node e : tree.getExternalNodes()) {
                for (Node n : tree.getAdjacencies(e)) {
                    final double d = dist(tree, e, n, dists);
                    if (d > maxDistance) {
                        maxDistance = d;
                        current = e;
                        direction = n;
                    }
                }
            }

            // traverse along maximal path to it's middle
            double distanceLeft = maxDistance / 2.0;

            while (true) {
                final double len = tree.getEdgeLength(current, direction);
                if (distanceLeft <= len) {
                    //System.out.println(toNewick(rtree));
                    return new ReRootedTree(tree, current, direction, distanceLeft);
                }
                distanceLeft -= len;

                maxDistance = -Double.MAX_VALUE;
                Node next = null;
                for (Node n : tree.getAdjacencies(direction)) {
                    if (n == current) continue;
                    final double d = dist(tree, direction, n, dists);
                    if (d > maxDistance) {
                        maxDistance = d;
                        next = n;
                    }
                }
                current = direction;
                direction = next;
            }
        } catch (Graph.NoEdgeException e1) {
            return null; // serious bug, should not happen
        }
    }

    private static double dist(Tree tree, Node root, Node node, Map<HashPair<Node>, Double> dists) throws Graph.NoEdgeException {
        HashPair<Node> p = new HashPair<Node>(root, node);
        if (dists.containsKey(p)) {
            return dists.get(p);
        }

        // assume positive branches
        double maxDist = 0;
        for (Node n : tree.getAdjacencies(node)) {
            if (n != root) {
                double d = dist(tree, node, n, dists);
                maxDist = Math.max(maxDist, d);
            }
        }
        double dist = tree.getEdgeLength(node, root) + maxDist;

        dists.put(p, dist);
        return dist;
    }

    // PRIVATE members

    private RootedTree source = null;

    private ReRootedNode rootNode = null;
    private final Set<Node> internalNodes = new LinkedHashSet<Node>();
    private final Map<Taxon, Node> externalNodes = new LinkedHashMap<Taxon, Node>();

    private boolean heightsKnown = false;
    private boolean lengthsKnown = false;

    private boolean hasHeights = false;
    private boolean hasLengths = false;

    private class ReRootedNode implements Node {
        public ReRootedNode(Node source, Taxon taxon) {
            this.source = source;
            this.children = Collections.unmodifiableList(new ArrayList<Node>());
            this.taxon = taxon;
        }

        public ReRootedNode(Node source, List<? extends Node> children) {
            this.source = source;
            this.children = Collections.unmodifiableList(new ArrayList<Node>(children));
            this.taxon = null;
        }

        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public List<Node> getChildren() {
            return children;
        }

        public double getHeight() {
            return height;
        }

        // height above latest tip
        public void setHeight(double height) {
            this.height = height;
        }

        // length of branch to parent
        public double getLength() {
            return length;
        }

        public void setLength(double length) {
            this.length = length;
        }

        public int getDegree() {
            return children.size() +(this==rootNode?0:1);
        }

        public void setTaxon(Taxon to) {
            taxon = to;
        }

        /**
         * returns the edge connecting this node to the parent node
         * @return the edge
         */
        public Edge getEdge() {
            if (edge == null) {
                edge = new BaseEdge() {
                    public double getLength() {
                        return length;
                    }
                };
            }

            return edge;
        }

        /**
         * For a rooted tree, getting the adjacencies is not the most efficient
         * operation as it makes a new set containing the children and the parent.
         * @return the adjacaencies
         */
        public List<Node> getAdjacencies() {
            List<Node> adjacencies = new ArrayList<Node>();
            if (children != null) adjacencies.addAll(children);
            if (parent != null) adjacencies.add(parent);
            return adjacencies;
        }

        public Taxon getTaxon() {
            return taxon;
        }

        // Attributable IMPLEMENTATION

        public void setAttribute(String name, Object value) {
            if (source == null) {
                if (helper == null) {
                    helper = new AttributableHelper();
                }
                helper.setAttribute(name, value);
            } else {
                source.setAttribute(name, value);
            }
        }

        public Object getAttribute(String name) {
            if (source == null) {
                if (helper == null) {
                    return null;
                }
                return helper.getAttribute(name);
            }
            return source.getAttribute(name);
        }

        public void removeAttribute(String name) {
            if (source == null) {
                if( helper != null ) {
                    helper.removeAttribute(name);
                }
            } else {
                source.removeAttribute(name);
            }
        }

        public Set<String> getAttributeNames() {
            if (source == null) {
                if (helper == null) {
                    return Collections.emptySet();
                }
                return helper.getAttributeNames();
            }
            return source.getAttributeNames();
        }

        public Map<String, Object> getAttributeMap() {
            if (source == null) {
                if (helper == null) {
                    return Collections.emptyMap();
                }
                return helper.getAttributeMap();
            }
            return source.getAttributeMap();
        }

        private final Node source;

        private List<Node> children;
        private Taxon taxon;

        private Node parent;
        private double height;
        private double length;

        private Edge edge = null;

        private AttributableHelper helper = null;

    }
}