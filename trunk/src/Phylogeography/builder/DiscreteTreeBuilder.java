package phylogeography.builder;

import phylogeography.structure.*;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.graphs.Node;

import java.awt.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class DiscreteTreeBuilder implements Builder {
    public static final String LONGITUDE_ATTRIBUTE = "long";
    public static final String LATITUDE_ATTRIBUTE = "lat";
    public static final String TIME_ATTRIBUTE = "height";

    public DiscreteTreeBuilder(final RootedTree tree) {
        this.tree = tree;
    }

    public Layer buildLayer() {
        Layer layer = new Layer(name, description, isVisible);
        buildTree(layer, tree, tree.getRootNode());
        return layer;
    }

    private void buildTree(Layer layer, final RootedTree tree, final Node node) {
        if (!tree.isRoot(node)) {
            Node parent = tree.getParent(node);
            double long0 = (Double)parent.getAttribute(LONGITUDE_ATTRIBUTE);
            double lat0 = (Double)parent.getAttribute(LATITUDE_ATTRIBUTE);
            double time0 = (Double)parent.getAttribute(TIME_ATTRIBUTE);
            Style style0 = new Style(Color.red, 1.0);

            double long1 = (Double)node.getAttribute(LONGITUDE_ATTRIBUTE);
            double lat1 = (Double)node.getAttribute(LATITUDE_ATTRIBUTE);
            double time1 = (Double)node.getAttribute(TIME_ATTRIBUTE);
            Style style1 = new Style(Color.red, 1.0);

            double maxAltitude = 10000;
            double duration = -1.0;

            Line line = new Line(
                    "",
                    new Coordinates(long0, lat0), time0, style0,
                    new Coordinates(long1, lat1), time1, style1,
                    maxAltitude,
                    duration
            );
            layer.addItem(line);
        }
        for (Node child : tree.getChildren(node)) {
            buildTree(layer, tree, child);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(final boolean visible) {
        isVisible = visible;
    }

    private final RootedTree tree;
    private String name;
    private String description;
    private boolean isVisible;
}
