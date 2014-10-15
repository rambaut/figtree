package figtree.webui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import eu.webtoolkit.jwt.KeyboardModifier;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPaintDevice;
import eu.webtoolkit.jwt.WPaintedWidget;
import eu.webtoolkit.jwt.WPainter;
import eu.webtoolkit.jwt.WMouseEvent.Button;
import eu.webtoolkit.jwt.utils.WebGraphics2D;
import figtree.treeviewer.TreePane;
import figtree.treeviewer.TreePaneSelector;
import figtree.treeviewer.treelayouts.RectilinearTreeLayout;

public class TreeWidget extends WPaintedWidget {

	private TreePane treePane;
	private WebGraphics2D graphics = new WebGraphics2D(new WPainter());

	public TreeWidget() {
		treePane = new TreePane() {
			private static final long serialVersionUID = 1L; 

			@Override
			public void repaint() {
				TreeWidget.this.update();
			}

			@Override
			public Graphics getGraphics() {
				return graphics;
			}
		};

		treePane.setTreeLayout(new RectilinearTreeLayout());
		treePane.setSelectionColor(new Color(90, 108, 128));
		final TreePaneSelector selector = new TreePaneSelector(treePane);
		
		clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			@Override
			public void trigger(WMouseEvent event) {
				int dx = event.getDragDelta().x;
				int dy = event.getDragDelta().y;
				if (dx*dx + dy*dy < 4)
					selector.mouseClicked(convertMouseEvent(event, MouseEvent.MOUSE_CLICKED));
			}
		});
		
		mouseWentDown().addListener(this, new Signal1.Listener<WMouseEvent>() {
			@Override
			public void trigger(WMouseEvent event) {
				selector.mousePressed(convertMouseEvent(event, MouseEvent.MOUSE_PRESSED));
			}
		});

		mouseWentUp().addListener(this, new Signal1.Listener<WMouseEvent>() {
			@Override
			public void trigger(WMouseEvent event) {
				selector.mouseReleased(convertMouseEvent(event, MouseEvent.MOUSE_RELEASED));
			}
		});

		mouseDragged().addListener(this, new Signal1.Listener<WMouseEvent>() {
			@Override
			public void trigger(WMouseEvent event) {
				selector.mouseDragged(convertMouseEvent(event, MouseEvent.MOUSE_DRAGGED));
			}
		});
	}
	
	MouseEvent convertMouseEvent(WMouseEvent event, int type) {
		int modifiers = 0;
		
		if (event.getModifiers().contains(KeyboardModifier.AltModifier))
			modifiers |= MouseEvent.ALT_DOWN_MASK;
		if (event.getModifiers().contains(KeyboardModifier.ShiftModifier))
			modifiers |= MouseEvent.SHIFT_DOWN_MASK;
		if (event.getModifiers().contains(KeyboardModifier.ControlModifier))
			modifiers |= MouseEvent.CTRL_DOWN_MASK;
		if (event.getModifiers().contains(KeyboardModifier.MetaModifier))
			modifiers |= MouseEvent.META_DOWN_MASK;
		
		int button = 0;
		
		if (event.getButton() == Button.LeftButton) {
			modifiers |= MouseEvent.BUTTON1_DOWN_MASK;
			button = MouseEvent.BUTTON1;
		} else if (event.getButton() == Button.MiddleButton) {
			modifiers |= MouseEvent.BUTTON2_DOWN_MASK;
			button = MouseEvent.BUTTON2;
		} else if (event.getButton() == Button.RightButton) {
			modifiers |= MouseEvent.BUTTON3_DOWN_MASK;
			button = MouseEvent.BUTTON3;
		}
		
		return new MouseEvent(treePane, type, System.currentTimeMillis(),
					modifiers, event.getWidget().x, event.getWidget().y, 1, event.getButton() == Button.RightButton, button);
	}

	@Override
	public void resize(WLength width, WLength height) {
		super.resize(width, height);
		treePane.setSize((int)width.toPixels(), (int)height.toPixels());
	}
	

	@Override
	protected void layoutSizeChanged(int width, int height) {
		super.layoutSizeChanged(width, height);
		treePane.setSize(width, height);
	}	
	
	@Override
	protected void paintEvent(WPaintDevice paintDevice) {
		WPainter painter = new WPainter(paintDevice);
		WebGraphics2D graphics = new WebGraphics2D(painter);		
		treePane.paint(graphics);
	}

	public TreePane getTreePane() {
		return treePane;
	}
}
