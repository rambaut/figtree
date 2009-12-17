/*
 * ColorWellButton.java
 *
 * Copyright (c) 2009 JAM Development Team
 *
 * This package is distributed under the Lesser Gnu Public Licence (LGPL)
 *
 */

package figtree.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class ColorWellButton extends JButton {
	private JColorChooser chooser;
	private String colorChooserTitle;

	public ColorWellButton(Color color, final String colorChooserTitle) {
		super();

		this.colorChooserTitle = colorChooserTitle;

		putClientProperty("JButton.buttonType", "square");
		setBorderPainted(true);
//		putClientProperty("Quaqua.Button.style", "colorWell");
		setIcon(new ColorWell(color));
		setMargin(new Insets(10, 10, 10, 10));
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				chooserButtonActionPerformed(evt);
			}
		});
	}

	public Color getSelectedColor() {
		return ((ColorWell)getIcon()).color;
	}

	public void setSelectedColor(Color color) {
		((ColorWell)getIcon()).color = color;
		repaint();
	}

    private void chooserButtonActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            //      System.out.println("chooserButtonActionPerformed "+evt);
            if (chooser == null) {
                chooser = new JColorChooser();
                /*
                chooser.setSelectionModel(new DefaultColorSelectionModel() {
                    public void setSelectedColor(Color c) {
                        new Throwable().printStackTrace();
                        super.setSelectedColor(c);
                    }
                }
                );*/
                //JOptionPane.showMessageDialog(this, "Made a new chooser");
            }
            Color color = chooser.showDialog(this, colorChooserTitle, ((ColorWell)getIcon()).color);
	        if (color != null) {
		        setSelectedColor(color);
	        }
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            JOptionPane.showMessageDialog(this, "JColorChooser Failed "+sw.toString());
            t.printStackTrace();
        }
    }

	private class ColorWell implements Icon {
		Color color;

		ColorWell(Color color) {
			super();
			this.color = color;
		}

		public int getIconWidth() {
			return 15;
		}

		public int getIconHeight() {
			return 15;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			if (color == null) return;
			g.setColor(color);
			g.fillRect(x, y, getIconWidth(), getIconHeight());
			g.setColor(color.darker());
			g.drawRect(x, y, getIconWidth() - 1, getIconHeight() - 1);
		}
	}

}
