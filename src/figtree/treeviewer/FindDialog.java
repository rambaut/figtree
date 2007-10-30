/*
 * FindDialog.java
 */
package figtree.treeviewer;

import jebl.gui.trees.treeviewer_dev.DefaultTreeViewer;
import org.virion.jam.panels.OptionsPanel;
import figtree.treeviewer.annotations.AnnotationDefinition;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * FindDialog
 *
 * @author			Andrew Rambaut
 * @version			$Id: FindDialog.java,v 1.8 2006/11/21 11:11:23 rambaut Exp $
 */
public class FindDialog {
	public final static String TAXON_LABEL = "Taxon Label";
	public final static String ANY_ANNOTATION = "Any Annotation";

    private JFrame frame;
	private JComboBox targetCombo = new JComboBox(new String[] {TAXON_LABEL, ANY_ANNOTATION});
    private JComboBox searchCombo = new JComboBox(DefaultTreeViewer.SearchType.values());
    private JTextField searchText = new JTextField();

    private JCheckBox caseSensitiveCheck = new JCheckBox("Case sensitive");
    private JCheckBox findAllCheck = new JCheckBox("Find all");

    public FindDialog(JFrame frame) {
        this.frame = frame;

    }

    public int showDialog(List<AnnotationDefinition> annotations) {

        OptionsPanel options = new OptionsPanel(12, 12);

        searchText.setColumns(18);

	    JPanel panel = new JPanel(new BorderLayout(6,6));
	    targetCombo = new JComboBox(new String[] {TAXON_LABEL, ANY_ANNOTATION});
	    for (AnnotationDefinition annotation : annotations) {
		    targetCombo.addItem(annotation.getName());
	    }
		panel.add(targetCombo, BorderLayout.WEST);
	    panel.add(searchCombo, BorderLayout.CENTER);
	    panel.add(searchText, BorderLayout.EAST);
        options.addComponent(panel);

        options.addComponent(caseSensitiveCheck);

        options.addComponent(findAllCheck);

        JOptionPane optionPane = new JOptionPane(options,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null,
                null,
                null);
        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));

        final JDialog dialog = optionPane.createDialog(frame, "Find");
        dialog.pack();

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

	public String getSearchTarget() {
	    return targetCombo.getSelectedItem().toString();
	}

    public DefaultTreeViewer.SearchType getSearchType() {
        return (DefaultTreeViewer.SearchType)searchCombo.getSelectedItem();
    }

    public String getSearchText() {
        return searchText.getText();
    }

    public boolean isCaseSensitive() {
        return caseSensitiveCheck.isSelected();
    }


}