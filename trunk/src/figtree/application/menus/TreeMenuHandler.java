/*
 * TreeMenuHandler.java
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

package figtree.application.menus;

import javax.swing.*;

/**
 * @author Andrew Rambaut
 * @version $Id: TreeMenuHandler.java,v 1.3 2007/09/05 11:17:15 rambaut Exp $
 */
public interface TreeMenuHandler {
	public static final String NEXT_TREE = "Next Tree";
	public static final String PREVIOUS_TREE = "Previous Tree";

	public static final String CARTOON_NODE = "Draw Subtree as Cartoon";
	public static final String COLLAPSE_NODE = "Draw Subtree as Collapsed";
	public static final String CLEAR_COLLAPSED = "Clear Collapsed/Cartoon";

	public static final String MIDPOINT_ROOT = "Midpoint Root";
	public static final String ROOT_ON_BRANCH = "Root on Branch...";
	public static final String CLEAR_ROTATIONS = "Clear Rotations...";

	public static final String INCREASING_NODE_ORDER = "Increasing Node Order";
	public static final String DECREASING_NODE_ORDER = "Decreasing Node Order";
	public static final String ROTATE_NODE = "Rotate Node...";
	public static final String CLEAR_ROOTING = "Clear Rooting...";

	public static final String COLOUR = "Colour...";
	public static final String CLEAR_COLOURING = "Clear Colouring...";

	public static final String HILIGHT = "Hilight...";
	public static final String CLEAR_HILIGHTING = "Clear Hilighting...";

    public static final String DEFINE_ANNOTATIONS = "Define Annotations...";
	public static final String ANNOTATE = "Annotate...";
    public static final String COPY_ANNOTATION_VALUES = "Copy values...";
	public static final String ANNOTATE_NODES_FROM_TIPS = "Annotate Nodes from Tips...";
	public static final String ANNOTATE_TIPS_FROM_NODES = "Annotate Tips from Nodes...";
	public static final String CLEAR_ANNOTATIONS = "Clear Annotations";

	Action getNextTreeAction();
	Action getPreviousTreeAction();
	Action getCartoonAction();
	Action getCollapseAction();
	Action getClearCollapsedAction();

	Action getMidpointRootAction();
	Action getRerootAction();
	Action getClearRootingAction();

	Action getIncreasingNodeOrderAction();
	Action getDecreasingNodeOrderAction();
	Action getRotateAction();
	Action getClearRotationsAction();

	Action getColourAction();
	Action getClearColouringAction();

	Action getHilightAction();
	Action getClearHilightingAction();

	Action getDefineAnnotationsAction();
	Action getAnnotateAction();
    Action getCopyAnnotationsAction();
	Action getAnnotateNodesFromTipsAction();
	Action getAnnotateTipsFromNodesAction();
	Action getClearAnnotationsAction();

}
