package figtree.application;

import figtree.treeviewer.decorators.AttributableDecorator;
import org.virion.jam.controlpalettes.ControlPalette;
import org.virion.jam.disclosure.DisclosureListener;
import org.virion.jam.util.IconUtils;
import figtree.treeviewer.*;
import figtree.treeviewer.painters.*;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.util.*;

/**
 * This is a panel that has a TreeViewer and a BasicControlPalette with
 * the default Controllers and Painters.
 *
 * @author Andrew Rambaut
 * @version $Id: FigTreePanel.java,v 1.13 2007/09/05 10:51:49 rambaut Exp $
 */
public class SlideOpenPanel extends JPanel {

	public SlideOpenPanel(JPanel mainPanel) {

		setOpaque(false);
		setLayout(new BorderLayout());

		topPanel = new JPanel(new BorderLayout()) {
			public void paint(Graphics graphics) {
				graphics.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
				super.paint(graphics);
			}
		};
		topPanel.setOpaque(false);
		topPanel.setVisible(false);
		topPanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray),
						BorderFactory.createEmptyBorder(4, 4, 4, 4)));

		add(topPanel, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);

	}

	public void showUtilityPanel(JPanel utilityPanel) {

		if (utilityPanel == null) {
			return;
		}

		final JButton doneButton = new JButton(closeIcon);
		adjustComponent(doneButton);
		doneButton.setIconTextGap(0);

		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hideUtilityPanel();
			}
		});
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "doClose");
		getActionMap().put("doClose", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				doneButton.doClick();
			}
		});

		topPanel.removeAll();
		topPanel.add(utilityPanel, BorderLayout.CENTER);
		topPanel.add(doneButton, BorderLayout.EAST);

		Dimension size = topPanel.getPreferredSize();

		target = utilityPanel.getPreferredSize().height + 8;
		size.height = 0;

		topPanel.setPreferredSize(size);
		topPanel.setVisible(true);
		hiding = false;
		startAnimation();
	}

	public void hideUtilityPanel() {
		target = 0;
		hiding = true;
		startAnimation();
	}

	protected void adjustComponent(JComponent comp) {
		// comp.putClientProperty("Quaqua.Component.visualMargin", new Insets(0,0,0,0));
		Font font = UIManager.getFont("SmallSystemFont");
		if (font != null) {
			comp.setFont(font);
		}
		comp.putClientProperty("JComponent.sizeVariant", "small");
		if (comp instanceof JButton) {
			comp.putClientProperty("JButton.buttonType", "roundRect");
		}
		if (comp instanceof JComboBox) {
			comp.putClientProperty("JComboBox.isSquare", Boolean.TRUE);
		}
	}

	private void startAnimation() {
		timer = new Timer(animationSpeed, listener);
		timer.setRepeats(true);
		timer.setCoalesce(false);
		timer.start();
	}

	private void stopAnimation() {
		if (timer == null) return;
		timer.stop();
		if (hiding) {
			topPanel.setVisible(false);
		}
	}

	ActionListener listener = new ActionListener() {

		public void actionPerformed(ActionEvent e) {

			int delta = (int)Math.ceil(((double)(target - topPanel.getHeight())) / 10.0);
			if (delta != 0) {
				Dimension size = topPanel.getPreferredSize();
				size.height += delta;
				topPanel.setPreferredSize(size);
				topPanel.revalidate();
				revalidate();
				repaint();
			} else {
				stopAnimation();
			}

		}
	};


	private final JPanel topPanel;

	private Timer timer = null;
	private int animationSpeed = 10;
	private int target;
	private boolean hiding;

	private static BufferedImage backgroundImage = null;
	private static Icon closeIcon;

	static {
		closeIcon = IconUtils.getIcon(SlideOpenPanel.class, "images/close.png");
		try {
			backgroundImage = IconUtils.getBufferedImage(SlideOpenPanel.class, "images/utilityBackground.png");

		} catch (Exception e) {
			// no icons...
		}
	}
}