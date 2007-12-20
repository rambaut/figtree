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

	public static final String ROOT_ON_BRANCH = "Root on Branch...";
	public static final String CLEAR_ROTATIONS = "Clear Rotations...";

	public static final String ROTATE_NODE = "Rotate Node...";
	public static final String CLEAR_ROOTING = "Clear Rooting...";

	public static final String COLOUR = "Colour...";
	public static final String CLEAR_COLOURING = "Clear Colouring...";

    public static final String DEFINE_ANNOTATIONS = "Define Annotations...";
	public static final String ANNOTATE = "Annotate...";
	public static final String ANNOTATE_FROM_TIPS = "Annotate from Tips...";
	public static final String CLEAR_ANNOTATIONS = "Clear Annotations";

	Action getNextTreeAction();
	Action getPreviousTreeAction();
	Action getCartoonAction();
	Action getCollapseAction();
	Action getClearCollapsedAction();

	Action getRerootAction();
	Action getClearRootingAction();

	Action getRotateAction();
	Action getClearRotationsAction();

	Action getColourAction();
	Action getClearColouringAction();

	Action getDefineAnnotationsAction();
	Action getAnnotateAction();
	Action getAnnotateFromTipsAction();
	Action getClearAnnotationsAction();

}
