package figtree.treeviewer;

import figtree.treeviewer.painters.*;
import jebl.evolution.graphs.Node;
import jebl.evolution.trees.*;
import figtree.treeviewer.decorators.Decorator;
import figtree.treeviewer.treelayouts.TreeLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.print.Printable;
import java.util.Collection;
import java.util.Set;

import jam.panels.StatusProvider;

/**
 * @author Andrew Rambaut
 * @version $Id: TreeViewer.java 822 2007-10-26 13:50:26Z rambaut $
 */
public abstract class TreeViewer extends JPanel implements Printable {

    public abstract void setTrees(Collection<? extends Tree> trees);

    public abstract java.util.List<Tree> getTrees();

    public abstract Tree getCurrentTree();

    public abstract int getCurrentTreeIndex();

    public abstract int getTreeCount();

    public abstract StatusProvider getStatusProvider();

    public abstract void showTree(int index);

    public abstract void setTreeLayout(TreeLayout treeLayout);

    public abstract void setZoom(double zoom);

    public abstract void setVerticalExpansion(double verticalExpansion);

    public abstract boolean verticalExpansionAllowed();

    public abstract void setTimeScale(TimeScale timeScale);

    public abstract boolean hasSelection();

    public abstract Set<Node> getSelectedNodes();

    public abstract Set<Node> getSelectedTips();

    public abstract void selectTaxa(String attributeName, TextSearchType searchType, String searchString, boolean caseSensitive);

    public abstract void selectNodes(String attributeName, TextSearchType searchType, String searchString, boolean caseSensitive);

    public abstract void selectTaxa(String attributeName, NumberSearchType searchType, Number searchValue);

    public abstract void selectNodes(String attributeName, NumberSearchType searchType, Number searchValue);

    public abstract void selectTaxa(Collection<String> taxonNames);

    public abstract void collapseSelectedNodes();

    public abstract void annotateSelectedNodes(String name, Object value);

    public abstract void annotateSelectedTips(String name, Object value);

    public abstract void selectAll();

    public abstract void clearSelectedTaxa();

    public abstract void addTreeSelectionListener(TreeSelectionListener treeSelectionListener);

    public abstract void removeTreeSelectionListener(TreeSelectionListener treeSelectionListener);


    public abstract void setSelectionMode(TreePaneSelector.SelectionMode selectionMode);

    public abstract void setDragMode(TreePaneSelector.DragMode dragMode);

    public abstract void setTipLabelPainter(LabelPainter<Node> tipLabelPainter);

    public abstract void setNodeLabelPainter(LabelPainter<Node> nodeLabelPainter);

    public abstract void setNodeBarPainter(NodeBarPainter nodeBarPainter);

    public abstract void setNodeShapePainter(NodeShapePainter nodeShapePainter);

    public abstract void setBranchLabelPainter(LabelPainter<Node> branchLabelPainter);

    public abstract void addScalePainter(ScalePainter scalePainter);

    public abstract void removeScalePainter(ScalePainter scalePainter);

    public abstract void setScaleGridPainter(ScaleGridPainter scaleGridPainter);

    public abstract void setLegendPainter(LegendPainter legendPainter);

    public abstract void setBranchDecorator(Decorator branchDecorator, boolean isGradient);

    public abstract void setBranchColouringDecorator(String branchColouringAttribute, Decorator branchColouringDecorator);

    public abstract void setNodeBackgroundDecorator(Decorator nodeBackgroundDecorator);

    public abstract void setHilightingGradient(boolean hilightingGradient);

    public abstract void setSelectionColor(Color selectionColor);

    public abstract Paint getSelectionPaint();

    public abstract void setBranchStroke(BasicStroke branchStroke);


    public abstract boolean isTransformBranchesOn();

    public abstract TransformedRootedTree.Transform getBranchTransform();

    public abstract void setTransformBranchesOn(boolean transformBranchesOn);

    public abstract void setBranchTransform(TransformedRootedTree.Transform transform);


    public abstract boolean isOrderBranchesOn();

    public abstract SortedRootedTree.BranchOrdering getBranchOrdering();

    public abstract void setOrderBranchesOn(boolean orderBranchesOn);

    public abstract void setBranchOrdering(SortedRootedTree.BranchOrdering branchOrdering);

    public abstract boolean isRootingOn();

    public abstract TreePane.RootingType getRootingType();

    public abstract void setRootingOn(boolean rootingOn);

    public abstract void setRootingType(TreePane.RootingType rootingType);


    public abstract JComponent getContentPane();

    public abstract void addTreeViewerListener(TreeViewerListener listener);

    public abstract void removeTreeViewerListener(TreeViewerListener listener);


    public enum TextSearchType {
        CONTAINS("contains"),
        STARTS_WITH("starts with"),
        ENDS_WITH("ends with"),
        MATCHES("matches"),
        REG_EX("regular expression");

        TextSearchType(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

        private final String name;
    }

    public enum NumberSearchType {
        EQUALS("equals"),
        NOT_EQUALS("doesn't equals"),
        GREATER_THAN("greater than"),
        EQUALS_OR_GREATER_THAN("equals or greater than"),
        LESS_THAN("less than"),
        EQUALS_OR_LESS_THAN("equals or less than");

        NumberSearchType(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

        private final String name;
    }
}
