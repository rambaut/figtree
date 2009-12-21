/*
 * FindDialog.java
 */
package figtree.application;

import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

/**
 * FindDialog
 *
 * @author			Andrew Rambaut
 * @version			$Id: FindDialog.java,v 1.8 2006/11/21 11:11:23 rambaut Exp $
 */
public class ExportTreeDialog {

	public enum Format {
		NEXUS("NEXUS"),
		NEWICK("Newick");

		Format(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}

		private String name;
	}

	private final JDialog dialog;
	private final JOptionPane optionPane;

	private JComboBox formatCombo = null;
	private JCheckBox asDisplayedCheck = new JCheckBox("Save as currently displayed");
	private JCheckBox allTreesCheck = new JCheckBox("Save all trees");
	private JCheckBox includeFigTreeCheck = new JCheckBox("Include FigTree block (NEXUS only)");
	private JCheckBox includeAnnotationsCheck = new JCheckBox("Include Annotations (NEXUS only)");

	public ExportTreeDialog(JFrame frame) {

		OptionsPanel options = new OptionsPanel(12, 12);

		formatCombo = new JComboBox(Format.values());
		options.addComponentWithLabel("Tree file format: ", formatCombo);

		options.addComponent(asDisplayedCheck);
		options.addComponent(allTreesCheck);
		options.addComponent(includeFigTreeCheck);
		options.addComponent(includeAnnotationsCheck);

		optionPane = new JOptionPane(options,
				JOptionPane.QUESTION_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION,
				null,
				null,
				null);
		optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));

		dialog = optionPane.createDialog(frame, "Export Trees");
		dialog.pack();

		formatCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Object item = formatCombo.getSelectedItem();

				boolean isNexus = item.equals(Format.NEXUS);
				includeFigTreeCheck.setEnabled(isNexus);
				includeAnnotationsCheck.setEnabled(isNexus);;
			}
		});
	}

	public int showDialog() {


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

	public Format getFormat() {
		return (Format)formatCombo.getSelectedItem();
	}

	public boolean asDisplayed() {
		return asDisplayedCheck.isSelected();
	}

	public boolean allTrees() {
		return allTreesCheck.isSelected();
	}

	public boolean includeFigTreeBlock() {
		return includeFigTreeCheck.isSelected();
	}

	public boolean includeAnnotations() {
		return includeAnnotationsCheck.isSelected();
	}


}