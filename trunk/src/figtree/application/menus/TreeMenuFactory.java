/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package figtree.application.menus;

import org.virion.jam.framework.*;
import org.virion.jam.mac.Utils;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * @author rambaut
 *         Date: Feb 24, 2005
 *         Time: 5:12:11 PM
 */
public class TreeMenuFactory implements MenuFactory {

    public String getMenuName() {
        return "Tree";
    }

    public void populateMenu(JMenu menu, AbstractFrame frame) {
        JMenuItem item;

        if (frame instanceof TreeMenuHandler) {
	        item = new JMenuItem(((TreeMenuHandler)frame).getNextTreeAction());
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT_PARENTHESIS, MenuBarFactory.MENU_MASK));
	        menu.add(item);

	        item = new JMenuItem(((TreeMenuHandler)frame).getPreviousTreeAction());
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT_PARENTHESIS, MenuBarFactory.MENU_MASK));
	        menu.add(item);

	        menu.addSeparator();

            item = new JMenuItem(((TreeMenuHandler)frame).getCartoonAction());
            menu.add(item);

	        item = new JMenuItem(((TreeMenuHandler)frame).getCollapseAction());
	        menu.add(item);

	        item = new JMenuItem(((TreeMenuHandler)frame).getClearCollapsedAction());
	        menu.add(item);

	        menu.addSeparator();

            item = new JMenuItem(((TreeMenuHandler)frame).getRerootAction());
            menu.add(item);

	        item = new JMenuItem(((TreeMenuHandler)frame).getClearRootingAction());
	        menu.add(item);

	        menu.addSeparator();

	        item = new JMenuItem(((TreeMenuHandler)frame).getRotateAction());
	        menu.add(item);

		    item = new JMenuItem(((TreeMenuHandler)frame).getClearRotationsAction());
		    menu.add(item);

	        menu.addSeparator();

            item = new JMenuItem(((TreeMenuHandler)frame).getDefineAnnotationsAction());
            menu.add(item);

	        item = new JMenuItem(((TreeMenuHandler)frame).getAnnotateAction());
	        menu.add(item);

	        item = new JMenuItem(((TreeMenuHandler)frame).getAnnotateNodesFromTipsAction());
	        menu.add(item);

	        item = new JMenuItem(((TreeMenuHandler)frame).getAnnotateTipsFromNodesAction());
	        menu.add(item);

//	        item = new JMenuItem(((TreeMenuHandler)frame).getClearAnnotationsAction());
//	        menu.add(item);

	        menu.addSeparator();

	        item = new JMenuItem(((TreeMenuHandler)frame).getColourAction());
	        menu.add(item);

		    item = new JMenuItem(((TreeMenuHandler)frame).getClearColouringAction());
		    menu.add(item);

        } else if (Utils.isMacOSX()) {
	        // make a false menu (only required for Mac OS X)
	        item = new JMenuItem(TreeMenuHandler.NEXT_TREE);
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT_PARENTHESIS, MenuBarFactory.MENU_MASK));
	        item.setEnabled(false);
	        menu.add(item);

	        item = new JMenuItem(TreeMenuHandler.PREVIOUS_TREE);
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT_PARENTHESIS, MenuBarFactory.MENU_MASK));
	        item.setEnabled(false);
	        menu.add(item);

	        menu.addSeparator();

            item = new JMenuItem(TreeMenuHandler.CARTOON_NODE);
            item.setEnabled(false);
            menu.add(item);

	        item = new JMenuItem(TreeMenuHandler.COLLAPSE_NODE);
	        item.setEnabled(false);
	        menu.add(item);

	        item = new JMenuItem(TreeMenuHandler.CLEAR_COLLAPSED);
	        item.setEnabled(false);
	        menu.add(item);

	        menu.addSeparator();

            item = new JMenuItem(TreeMenuHandler.ROOT_ON_BRANCH);
            item.setEnabled(false);
            menu.add(item);

	        item = new JMenuItem(TreeMenuHandler.CLEAR_ROOTING);
	        item.setEnabled(false);
	        menu.add(item);

		    menu.addSeparator();

	        item = new JMenuItem(TreeMenuHandler.ROTATE_NODE);
	        item.setEnabled(false);
	        menu.add(item);

		    item = new JMenuItem(TreeMenuHandler.CLEAR_ROTATIONS);
		    item.setEnabled(false);
		    menu.add(item);

			menu.addSeparator();

            item = new JMenuItem(TreeMenuHandler.DEFINE_ANNOTATIONS);
            item.setEnabled(false);
            menu.add(item);

	        item = new JMenuItem(TreeMenuHandler.ANNOTATE);
	        item.setEnabled(false);
	        menu.add(item);

	        item = new JMenuItem(TreeMenuHandler.ANNOTATE_NODES_FROM_TIPS);
	        item.setEnabled(false);
	        menu.add(item);

	        item = new JMenuItem(TreeMenuHandler.ANNOTATE_TIPS_FROM_NODES);
	        item.setEnabled(false);
	        menu.add(item);

//	        item = new JMenuItem(TreeMenuHandler.CLEAR_ANNOTATIONS);
//	        item.setEnabled(false);
//	        menu.add(item);

	        menu.addSeparator();

            item = new JMenuItem(TreeMenuHandler.COLOUR);
            item.setEnabled(false);
            menu.add(item);

	        item = new JMenuItem(TreeMenuHandler.CLEAR_COLOURING);
	        item.setEnabled(false);
	        menu.add(item);
        }

    }

    public int getPreferredAlignment() {
        return LEFT;
    }
}
