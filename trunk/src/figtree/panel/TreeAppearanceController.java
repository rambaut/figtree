/*
 * TreeAppearanceController.java
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

package figtree.panel;

import jebl.evolution.trees.Tree;
import jebl.evolution.graphs.Node;
import figtree.treeviewer.TreeViewer;
import figtree.treeviewer.TreeViewerListener;
import figtree.treeviewer.painters.LabelPainter;
import figtree.treeviewer.decorators.*;
import jebl.util.Attributable;
import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * @author Andrew Rambaut
 * @version $Id: TreeAppearanceController.java,v 1.2 2007/09/10 14:52:02 rambaut Exp $
 */
public class TreeAppearanceController extends AbstractController {

	private static final String CONTROLLER_TITLE = "Appearance";

	private static final String CONTROLLER_KEY = "appearance";

	private static final String FOREGROUND_COLOUR_KEY = "foregroundColour";
	private static final String BACKGROUND_COLOUR_KEY = "backgroundColour";
	private static final String SELECTION_COLOUR_KEY = "selectionColour";
	private static final String BRANCH_COLOR_ATTRIBUTE_KEY = "branchColorAttribute";
	private static final String BRANCH_LINE_WIDTH_KEY = "branchLineWidth";

	// The defaults if there is nothing in the preferences
	private static Color DEFAULT_FOREGROUND_COLOUR = Color.BLACK;
	private static Color DEFAULT_BACKGROUND_COLOUR = Color.WHITE;
	private static Color DEFAULT_SELECTION_COLOUR = new Color(90, 108, 128);
	private static float DEFAULT_BRANCH_LINE_WIDTH = 1.0f;

	private static final String FONT_NAME_KEY = "fontName";
	private static final String FONT_SIZE_KEY = "fontSize";
	private static final String FONT_STYLE_KEY = "fontStyle";

	private static final String NUMBER_FORMATTING_KEY = "numberFormatting";

	private static final String DISPLAY_ATTRIBUTE_KEY = "displayAttribute";
	private static final String SIGNIFICANT_DIGITS_KEY = "significantDigits";

	// The defaults if there is nothing in the preferences
	private static String DEFAULT_FONT_NAME = "sansserif";
	private static int DEFAULT_FONT_SIZE = 6;
	private static int DEFAULT_FONT_STYLE = Font.PLAIN;

	private static String DEFAULT_NUMBER_FORMATTING = "#.####";

	private static String DECIMAL_NUMBER_FORMATTING = "#.####";
	private static String SCIENTIFIC_NUMBER_FORMATTING = "0.###E0";

	public TreeAppearanceController(final TreeViewer treeViewer,
	                                String tipKey,
	                                final LabelPainter tipLabelPainter,
	                                String nodeKey,
	                                final LabelPainter nodeLabelPainter,
	                                String branchKey,
	                                final LabelPainter branchLabelPainter) {
		this(treeViewer, tipKey, tipLabelPainter, nodeKey, nodeLabelPainter, branchKey, branchLabelPainter, true);
	}

	public TreeAppearanceController(final TreeViewer treeViewer,
	                                String tipKey,
	                                final LabelPainter tipLabelPainter,
	                                String nodeKey,
	                                final LabelPainter nodeLabelPainter,
	                                String branchKey,
	                                final LabelPainter branchLabelPainter,
	                                boolean hideColouring) {
		this.treeViewer = treeViewer;

		this.hideColouring = hideColouring;

		final AttributableDecorator branchDecorator = new AttributableDecorator();
		branchDecorator.setPaintAttributeName("!color");
		branchDecorator.setStrokeAttributeName("!stroke");
		treeViewer.setBranchDecorator(branchDecorator, false);

		int foregroundRGB = DEFAULT_FOREGROUND_COLOUR.getRGB();
		int backgroundRGB = DEFAULT_BACKGROUND_COLOUR.getRGB();
		int selectionRGB = DEFAULT_SELECTION_COLOUR.getRGB();
		float branchLineWidth = DEFAULT_BRANCH_LINE_WIDTH;

		treeViewer.setForeground(new Color(foregroundRGB));
		treeViewer.setBackground(new Color(backgroundRGB));
		treeViewer.setSelectionColor(new Color(selectionRGB));
		treeViewer.setBranchStroke(new BasicStroke(branchLineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		this.tipKey = tipKey;
		this.nodeKey = nodeKey;
		this.branchKey = branchKey;

		final String defaultFontName = DEFAULT_FONT_NAME;
		final int defaultFontStyle = DEFAULT_FONT_STYLE;
		final int defaultFontSize = DEFAULT_FONT_SIZE;
		final String defaultNumberFormatting = DEFAULT_NUMBER_FORMATTING;

		tipLabelPainter.setFont(new Font(defaultFontName, defaultFontStyle, defaultFontSize));
		tipLabelPainter.setNumberFormat(new DecimalFormat(defaultNumberFormatting));
		nodeLabelPainter.setFont(new Font(defaultFontName, defaultFontStyle, defaultFontSize));
		nodeLabelPainter.setNumberFormat(new DecimalFormat(defaultNumberFormatting));
		branchLabelPainter.setFont(new Font(defaultFontName, defaultFontStyle, defaultFontSize));
		branchLabelPainter.setNumberFormat(new DecimalFormat(defaultNumberFormatting));

        optionsPanel = new OptionsPanel();

		if (!hideColouring) {
			branchColourAttributeCombo = new JComboBox(new String[] { "No attributes" });
			setupAttributes(treeViewer.getTrees());
			branchColourAttributeCombo.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent itemEvent) {
					if (branchColourAttributeCombo.getSelectedIndex() == 0) {
						treeViewer.setBranchColouringDecorator(null, null);
						treeViewer.setBranchDecorator(branchDecorator, false);
					} else {
						Set<Node> nodes = new HashSet<Node>();
						for (Tree tree : treeViewer.getTrees()) {
							for (Node node : tree.getNodes()) {
								nodes.add(node);
							}
						}
						String attribute = (String) branchColourAttributeCombo.getSelectedItem();
						if (attribute != null && attribute.length() > 0) {
							if (attribute.endsWith("*")) {
								Decorator decorator = new DiscreteColourDecorator();

								treeViewer.setBranchColouringDecorator(attribute.substring(0, attribute.length() - 2), decorator);
								treeViewer.setBranchDecorator(null, false);
							} else if (DiscreteColourDecorator.isDiscrete(attribute, nodes)) {
								Decorator decorator = new HSBDiscreteColourDecorator(attribute, nodes);

								treeViewer.setBranchColouringDecorator(null, null);
								treeViewer.setBranchDecorator(decorator, false);
							} else {

								Decorator decorator = new InterpolatingColourDecorator(
										new ContinuousScale(attribute, nodes),
										new Color(192, 16, 0), new Color(0, 16, 192));

								treeViewer.setBranchColouringDecorator(null, null);
								treeViewer.setBranchDecorator(decorator, false);
							}
						}
					}
				}
			});

			optionsPanel.addComponentWithLabel("Colour by:", branchColourAttributeCombo);
		} else {
			branchColourAttributeCombo = null;
		}

		branchLineWidthSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 48.0, 1.0));

		branchLineWidthSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				float lineWidth = ((Double) branchLineWidthSpinner.getValue()).floatValue();
				treeViewer.setBranchStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
			}
		});
		optionsPanel.addComponentWithLabel("Line Weight:", branchLineWidthSpinner);

		Font font = tipLabelPainter.getFont();
		fontSizeSpinner = new JSpinner(new SpinnerNumberModel(font.getSize(), 0.01, 48, 1));

		optionsPanel.addComponentWithLabel("Font Size:", fontSizeSpinner);

		fontSizeSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				final float size = ((Double) fontSizeSpinner.getValue()).floatValue();
				Font font = tipLabelPainter.getFont().deriveFont(size);
				tipLabelPainter.setFont(font);
				font = nodeLabelPainter.getFont().deriveFont(size);
				nodeLabelPainter.setFont(font);
				font = branchLabelPainter.getFont().deriveFont(size);
				branchLabelPainter.setFont(font);
			}
		});

		NumberFormat format = tipLabelPainter.getNumberFormat();
		int digits = format.getMaximumFractionDigits();
		digitsSpinner = new JSpinner(new SpinnerNumberModel(digits, 2, 14, 1));
		digitsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				final int digits = (Integer)digitsSpinner.getValue();
				NumberFormat format = tipLabelPainter.getNumberFormat();
				format.setMaximumFractionDigits(digits);
				tipLabelPainter.setNumberFormat(format);

				format = nodeLabelPainter.getNumberFormat();
				format.setMaximumFractionDigits(digits);
				nodeLabelPainter.setNumberFormat(format);

				format = branchLabelPainter.getNumberFormat();
				format.setMaximumFractionDigits(digits);
				branchLabelPainter.setNumberFormat(format);
			}
		});

		if (!hideColouring) {
			treeViewer.addTreeViewerListener(new TreeViewerListener() {
				public void treeChanged() {
					setupAttributes(treeViewer.getTrees());
					optionsPanel.repaint();
				}

				public void treeSettingsChanged() {
					// nothing to do
				}
			});
		}
	}

	private void setupAttributes(Collection<? extends Tree> trees) {
		Object selected = branchColourAttributeCombo.getSelectedItem();

		branchColourAttributeCombo.removeAllItems();
		branchColourAttributeCombo.addItem("User Selection");
		if (trees == null) {
			return;
		}
		for (Tree tree : trees) {
			for (String name : getAttributeNames(tree.getNodes())) {
				branchColourAttributeCombo.addItem(name);
			}
		}
		branchColourAttributeCombo.setSelectedItem(selected);
	}

	private String[] getAttributeNames(Collection<? extends Attributable> items) {
		Set<String> attributeNames = new TreeSet<String>();

		for (Attributable item : items) {
			for (String name : item.getAttributeNames()) {
				if (!name.startsWith("!")) {
					Object attr = item.getAttribute(name);
					if (!(attr instanceof Object[])) {
						attributeNames.add(name);
					} else {
						boolean isColouring = true;

						Object[] array = (Object[])attr;
						boolean isIndex = true;
						for (Object element : array) {
							if (isIndex && !(element instanceof Integer) ||
									!isIndex && !(element instanceof Double)) {
								isColouring = false;
								break;
							}
							isIndex = !isIndex;
						}

						if (isIndex) {
							// a colouring should finish on an index (which means isIndex should be false)...
							isColouring = false;
						}

						if (isColouring) {
							attributeNames.add(name + " *");
						}

					}
				}
			}
		}

		String[] attributeNameArray = new String[attributeNames.size()];
		attributeNames.toArray(attributeNameArray);

		return attributeNameArray;
	}

	public JComponent getTitleComponent() {
		return null;
	}

	public JPanel getPanel() {
		return optionsPanel;
	}

	public boolean isInitiallyVisible() {
		return false;
	}

	public void initialize() {
		// nothing to do
	}

	public void setSettings(Map<String,Object> settings) {
		// These settings don't have controls yet but they will!
		treeViewer.setForeground((Color)settings.get(CONTROLLER_KEY + "." + FOREGROUND_COLOUR_KEY));
		treeViewer.setBackground((Color)settings.get(CONTROLLER_KEY + "." + BACKGROUND_COLOUR_KEY));
		treeViewer.setSelectionColor((Color)settings.get(CONTROLLER_KEY + "." + SELECTION_COLOUR_KEY));

		if (branchColourAttributeCombo != null) {
			branchColourAttributeCombo.setSelectedItem(settings.get(CONTROLLER_KEY+"."+BRANCH_COLOR_ATTRIBUTE_KEY));
		}
		branchLineWidthSpinner.setValue((Double)settings.get(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY));

		fontSizeSpinner.setValue(((Number)settings.get(tipKey+"."+FONT_SIZE_KEY)).doubleValue());
		digitsSpinner.setValue((Integer)settings.get(tipKey+"."+SIGNIFICANT_DIGITS_KEY));
	}

	public void getSettings(Map<String, Object> settings) {
		settings.put(CONTROLLER_KEY + "." + FOREGROUND_COLOUR_KEY, treeViewer.getForeground());
		settings.put(CONTROLLER_KEY + "." + BACKGROUND_COLOUR_KEY, treeViewer.getBackground());
		settings.put(CONTROLLER_KEY + "." + SELECTION_COLOUR_KEY, treeViewer.getSelectionPaint());

		if (branchColourAttributeCombo != null) {
			settings.put(CONTROLLER_KEY + "." + BRANCH_COLOR_ATTRIBUTE_KEY, branchColourAttributeCombo.getSelectedItem().toString());
		}
		settings.put(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY, branchLineWidthSpinner.getValue());

		settings.put(tipKey+"."+FONT_SIZE_KEY, fontSizeSpinner.getValue());
		settings.put(tipKey+"."+SIGNIFICANT_DIGITS_KEY, digitsSpinner.getValue());
	}


	private final OptionsPanel optionsPanel;

	private final JComboBox branchColourAttributeCombo;
	private final JSpinner branchLineWidthSpinner;
	private final JSpinner fontSizeSpinner;
	private final JSpinner digitsSpinner;

	private final TreeViewer treeViewer;

	private final String tipKey;
	private final String nodeKey;
	private final String branchKey;

	private final boolean hideColouring;
}