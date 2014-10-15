/*
 * FindDialog.java
 *
 * Copyright (C) 2006-2014 Andrew Rambaut
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

package figtree.treeviewer;

import figtree.treeviewer.DefaultTreeViewer;
import jam.panels.OptionsPanel;
import figtree.treeviewer.annotations.AnnotationDefinition;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.util.List;

import figtree.ui.components.RealNumberField;
import figtree.ui.components.WholeNumberField;

/**
 * @author Andrew Rambaut
 * @version $Id$
 *
 * $HeadURL$
 *
 * $LastChangedBy$
 * $LastChangedDate$
 * $LastChangedRevision$
 */
public class FindDialog {

	public enum Target {
		TAXON_LABEL("Taxon Label"),
		BRANCH_LENGTH("Branch Length"),
		NODE_AGE("Node Age"),
		ANY_ANNOTATION("Any Annotation"),
		ANNOTATION("Annotation");

		Target(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}

		private String name;
	}

	private JFrame frame;
	private JComboBox targetCombo = null;
	private JComboBox textSearchCombo = new JComboBox(DefaultTreeViewer.TextSearchType.values());
	private JComboBox numberSearchCombo = new JComboBox(DefaultTreeViewer.NumberSearchType.values());

	private AnnotationDefinition.Type type = AnnotationDefinition.Type.STRING;

	private JTextField searchText = new JTextField();
	private RealNumberField doubleText = new RealNumberField();
	private WholeNumberField integerText = new WholeNumberField();

	private JCheckBox caseSensitiveCheck = new JCheckBox("Case sensitive");
	private JCheckBox findAllCheck = new JCheckBox("Find all");

	private int selectedTargetIndex = 0;

	public FindDialog(JFrame frame) {
		this.frame = frame;

	}

	public int showDialog(List<AnnotationDefinition> annotations) {

		OptionsPanel options = new OptionsPanel(12, 12);

		searchText.setColumns(18);

		final JPanel panel = new JPanel(new BorderLayout(6,6));
		targetCombo = new JComboBox();
		targetCombo.addItem(Target.TAXON_LABEL);
		targetCombo.addItem(Target.BRANCH_LENGTH);
		targetCombo.addItem(Target.NODE_AGE);
		targetCombo.addItem(Target.ANY_ANNOTATION);
		for (AnnotationDefinition annotation : annotations) {
			targetCombo.addItem(annotation);
		}
		panel.add(targetCombo, BorderLayout.WEST);
		panel.add(textSearchCombo, BorderLayout.CENTER);
		panel.add(searchText, BorderLayout.EAST);
		options.addComponent(panel);

		options.addComponent(caseSensitiveCheck);

		// options.addComponent(findAllCheck);

		JOptionPane optionPane = new JOptionPane(options,
				JOptionPane.QUESTION_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION,
				null,
				null,
				null);
		optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));

		final JDialog dialog = optionPane.createDialog(frame, "Find");
		dialog.pack();

		targetCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Object item = targetCombo.getSelectedItem();

                if (item.equals(Target.BRANCH_LENGTH) || item.equals(Target.NODE_AGE)) {
                    type = AnnotationDefinition.Type.REAL;
                } else if (item instanceof AnnotationDefinition) {
                    type = ((AnnotationDefinition) item).getType();
                } else {
                    type = AnnotationDefinition.Type.STRING;
                }

                panel.removeAll();
                panel.add(targetCombo, BorderLayout.WEST);
                switch (type) {
                    case INTEGER:
                        panel.add(numberSearchCombo, BorderLayout.CENTER);
                        panel.add(integerText, BorderLayout.EAST);
                        integerText.setColumns(10);
                        caseSensitiveCheck.setEnabled(false);
                        break;
                    case REAL:
                        panel.add(numberSearchCombo, BorderLayout.CENTER);
                        panel.add(doubleText, BorderLayout.EAST);
                        doubleText.setColumns(10);
                        caseSensitiveCheck.setEnabled(false);
                        break;
                    default:
                        panel.add(textSearchCombo, BorderLayout.CENTER);
                        panel.add(searchText, BorderLayout.EAST);
                        caseSensitiveCheck.setEnabled(true);
                }
                dialog.pack();
            }
        });

		targetCombo.setSelectedIndex(selectedTargetIndex);

		dialog.setVisible(true);

		int result = JOptionPane.CANCEL_OPTION;
		Integer value = (Integer)optionPane.getValue();
		if (value != null && value.intValue() != -1) {
			result = value.intValue();
		}

		if (result == JOptionPane.OK_OPTION) {

		}

		return result;
	}

	public Target getSearchTarget() {
		// store this for next time the dialog is shown...
		selectedTargetIndex = targetCombo.getSelectedIndex();

		Object item = targetCombo.getSelectedItem();
		if (item instanceof Target) {
			return (Target)item;
		}
		return Target.ANNOTATION;
	}

	public String getSearchTargetString() {
		return targetCombo.getSelectedItem().toString();
	}

	public boolean isNumericSearchType() {
		return (type == AnnotationDefinition.Type.REAL || type == AnnotationDefinition.Type.INTEGER);
	}

	public TreeViewer.TextSearchType getTextSearchType() {
		return (TreeViewer.TextSearchType)textSearchCombo.getSelectedItem();
	}

	public TreeViewer.NumberSearchType getNumberSearchType() {
		return (TreeViewer.NumberSearchType)numberSearchCombo.getSelectedItem();
	}

	public String getSearchText() {
		return searchText.getText();
	}

	public boolean isCaseSensitive() {
		return caseSensitiveCheck.isSelected();
	}

	public Number getSearchValue() {
		if (type == AnnotationDefinition.Type.REAL) {
			return doubleText.getValue();
		} else if (type == AnnotationDefinition.Type.INTEGER) {
			return integerText.getValue();
		}
		return null;
	}

}