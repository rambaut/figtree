/*
 * TreeMenuFactory.java
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

import jam.framework.*;
import jam.mac.Utils;

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
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, MenuBarFactory.MENU_MASK));
	        menu.add(item);

	        item = new JMenuItem(((TreeMenuHandler)frame).getPreviousTreeAction());
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, MenuBarFactory.MENU_MASK));
	        menu.add(item);

	        menu.addSeparator();

            item = new JMenuItem(((TreeMenuHandler)frame).getCartoonAction());
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, MenuBarFactory.MENU_MASK));
            menu.add(item);

	        item = new JMenuItem(((TreeMenuHandler)frame).getCollapseAction());
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, MenuBarFactory.MENU_MASK));
	        menu.add(item);

	        item = new JMenuItem(((TreeMenuHandler)frame).getClearCollapsedAction());
	        menu.add(item);

	        menu.addSeparator();

            item = new JMenuItem(((TreeMenuHandler)frame).getRerootAction());
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, MenuBarFactory.MENU_MASK));
            menu.add(item);

	        item = new JMenuItem(((TreeMenuHandler)frame).getMidpointRootAction());
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, MenuBarFactory.MENU_MASK));
	        menu.add(item);

	        item = new JMenuItem(((TreeMenuHandler)frame).getClearRootingAction());
	        menu.add(item);

	        menu.addSeparator();

	        item = new JMenuItem(((TreeMenuHandler)frame).getRotateAction());
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, MenuBarFactory.MENU_MASK));
	        menu.add(item);

	        item = new JMenuItem(((TreeMenuHandler)frame).getIncreasingNodeOrderAction());
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, MenuBarFactory.MENU_MASK));
	        menu.add(item);

	        item = new JMenuItem(((TreeMenuHandler)frame).getDecreasingNodeOrderAction());
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, MenuBarFactory.MENU_MASK));
	        menu.add(item);

		    item = new JMenuItem(((TreeMenuHandler)frame).getClearRotationsAction());
		    menu.add(item);

	        menu.addSeparator();

            item = new JMenuItem(((TreeMenuHandler)frame).getDefineAnnotationsAction());
            menu.add(item);

	        item = new JMenuItem(((TreeMenuHandler)frame).getAnnotateAction());
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_QUOTE, MenuBarFactory.MENU_MASK));
	        menu.add(item);

//            item = new JMenuItem(((TreeMenuHandler)frame).getCopyAnnotationsAction());
//            menu.add(item);

	        item = new JMenuItem(((TreeMenuHandler)frame).getAnnotateNodesFromTipsAction());
	        menu.add(item);

	        item = new JMenuItem(((TreeMenuHandler)frame).getAnnotateTipsFromNodesAction());
	        menu.add(item);

//	        item = new JMenuItem(((TreeMenuHandler)frame).getClearAnnotationsAction());
//	        menu.add(item);

	        menu.addSeparator();

	        item = new JMenuItem(((TreeMenuHandler)frame).getColourAction());
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, MenuBarFactory.MENU_MASK));
	        menu.add(item);

		    item = new JMenuItem(((TreeMenuHandler)frame).getClearColouringAction());
		    menu.add(item);

	        item = new JMenuItem(((TreeMenuHandler)frame).getHilightAction());
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, MenuBarFactory.MENU_MASK));
	        menu.add(item);

		    item = new JMenuItem(((TreeMenuHandler)frame).getClearHilightingAction());
		    menu.add(item);

        } else if (Utils.isMacOSX()) {
	        // make a false menu (only required for Mac OS X)
	        item = new JMenuItem(TreeMenuHandler.NEXT_TREE);
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, MenuBarFactory.MENU_MASK));
	        item.setEnabled(false);
	        menu.add(item);

	        item = new JMenuItem(TreeMenuHandler.PREVIOUS_TREE);
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, MenuBarFactory.MENU_MASK));
	        item.setEnabled(false);
	        menu.add(item);

	        menu.addSeparator();

            item = new JMenuItem(TreeMenuHandler.CARTOON_NODE);
            item.setEnabled(false);
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, MenuBarFactory.MENU_MASK));
            menu.add(item);

	        item = new JMenuItem(TreeMenuHandler.COLLAPSE_NODE);
	        item.setEnabled(false);
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, MenuBarFactory.MENU_MASK));
	        menu.add(item);

	        item = new JMenuItem(TreeMenuHandler.CLEAR_COLLAPSED);
	        item.setEnabled(false);
	        menu.add(item);

	        menu.addSeparator();

            item = new JMenuItem(TreeMenuHandler.ROOT_ON_BRANCH);
            item.setEnabled(false);
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, MenuBarFactory.MENU_MASK));
            menu.add(item);

	        item = new JMenuItem(TreeMenuHandler.MIDPOINT_ROOT);
	        item.setEnabled(false);
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, MenuBarFactory.MENU_MASK));
	        menu.add(item);

	        item = new JMenuItem(TreeMenuHandler.CLEAR_ROOTING);
	        item.setEnabled(false);
	        menu.add(item);

		    menu.addSeparator();

	        item = new JMenuItem(TreeMenuHandler.ROTATE_NODE);
	        item.setEnabled(false);
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, MenuBarFactory.MENU_MASK));
	        menu.add(item);

	        item = new JMenuItem(TreeMenuHandler.INCREASING_NODE_ORDER);
	        item.setEnabled(false);
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, MenuBarFactory.MENU_MASK));
	        menu.add(item);

	        item = new JMenuItem(TreeMenuHandler.DECREASING_NODE_ORDER);
	        item.setEnabled(false);
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, MenuBarFactory.MENU_MASK));
	        menu.add(item);

		    item = new JMenuItem(TreeMenuHandler.CLEAR_ROTATIONS);
		    item.setEnabled(false);
		    menu.add(item);

			menu.addSeparator();

            item = new JMenuItem(TreeMenuHandler.DEFINE_ANNOTATIONS);
            item.setEnabled(false);
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_QUOTE, MenuBarFactory.MENU_MASK));
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

//            item = new JMenuItem(TreeMenuHandler.COPY_ANNOTATION_VALUES);
//            item.setEnabled(false);
//            menu.add(item);

//	        item = new JMenuItem(TreeMenuHandler.CLEAR_ANNOTATIONS);
//	        item.setEnabled(false);
//	        menu.add(item);

	        menu.addSeparator();

            item = new JMenuItem(TreeMenuHandler.COLOUR);
            item.setEnabled(false);
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, MenuBarFactory.MENU_MASK));
            menu.add(item);

	        item = new JMenuItem(TreeMenuHandler.CLEAR_COLOURING);
	        item.setEnabled(false);
	        menu.add(item);

	        item = new JMenuItem(TreeMenuHandler.HILIGHT);
	        item.setEnabled(false);
	        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, MenuBarFactory.MENU_MASK));
	        menu.add(item);

	        item = new JMenuItem(TreeMenuHandler.CLEAR_HILIGHTING);
	        item.setEnabled(false);
		    menu.add(item);

        }

    }

    public int getPreferredAlignment() {
        return LEFT;
    }
}
