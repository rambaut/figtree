/*
 * FindDialog.java
 */
package figtree.treeviewer;

import figtree.treeviewer.DefaultTreeViewer;
import org.virion.jam.panels.OptionsPanel;
import org.virion.jam.components.RealNumberField;
import org.virion.jam.components.WholeNumberField;
import figtree.treeviewer.annotations.AnnotationDefinition;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.util.List;

/**
 * FindDialog
 *
 * @author			Andrew Rambaut
 * @version			$Id: FindDialog.java,v 1.8 2006/11/21 11:11:23 rambaut Exp $
 */
public class FindPanel extends JPanel {

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

	public FindPanel(List<AnnotationDefinition> annotations) {

        setLayout(new BorderLayout(0,0));

        OptionsPanel options = new OptionsPanel(12, 12);

		searchText.setColumns(18);

		final JPanel panel = new JPanel(new BorderLayout(6,6));
        panel.setOpaque(false);
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

        add(options, BorderLayout.CENTER);

        targetCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
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
			}
		});

		targetCombo.setSelectedIndex(selectedTargetIndex);
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