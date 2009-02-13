package figtree.panel;

import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;

import figtree.treeviewer.painters.LabelPainter;

/**
 * @author Andrew Rambaut
 * @version $Id: LabelPainterController.java,v 1.1 2007/08/14 15:50:42 rambaut Exp $
 */
public class LabelPainterController extends AbstractController {

    private static final String DISPLAY_ATTRIBUTE_KEY = "displayAttribute";

    public LabelPainterController(String tipKey,
                                  final LabelPainter tipLabelPainter,
                                  String nodeKey,
                                  final LabelPainter nodeLabelPainter,
                                  String branchKey,
		                                  final LabelPainter branchLabelPainter) {

        this.tipKey = tipKey;
	    this.nodeKey = nodeKey;
	    this.branchKey = branchKey;

        optionsPanel = new OptionsPanel();

	    tipComboBox = setupComboBox("Tips: ", tipLabelPainter);
        tipComboBox.setSelectedIndex(1);
        nodeComboBox = setupComboBox("Nodes: ", nodeLabelPainter);
	    branchComboBox = setupComboBox("Branches: ", branchLabelPainter);

   }

	private JComboBox setupComboBox(String title, final LabelPainter labelPainter) {
		String[] attributes = labelPainter.getAttributes();
		final JComboBox displayAttributeCombo = new JComboBox();
		displayAttributeCombo.addItem("None");
		for (String attr : attributes) {
			displayAttributeCombo.addItem(attr);
		}
		optionsPanel.addComponentWithLabel(title, displayAttributeCombo);

		displayAttributeCombo.addItemListener(new ItemListener() {
		    public void itemStateChanged(ItemEvent itemEvent) {
		        String attribute = (String)displayAttributeCombo.getSelectedItem();
			    if (attribute.equals("none")) {
				    labelPainter.setVisible(false);
			    } else {
		            labelPainter.setDisplayAttribute(attribute);
				    labelPainter.setVisible(true);
			    }
		    }
		});

		return displayAttributeCombo;
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
        tipComboBox.setSelectedItem(settings.get(tipKey+"."+DISPLAY_ATTRIBUTE_KEY));
	    nodeComboBox.setSelectedItem(settings.get(nodeKey+"."+DISPLAY_ATTRIBUTE_KEY));
	    branchComboBox.setSelectedItem(settings.get(branchKey+"."+DISPLAY_ATTRIBUTE_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(tipKey+"."+DISPLAY_ATTRIBUTE_KEY, tipComboBox.getSelectedItem().toString());
        settings.put(nodeKey+"."+DISPLAY_ATTRIBUTE_KEY, nodeComboBox.getSelectedItem().toString());
        settings.put(branchKey+"."+DISPLAY_ATTRIBUTE_KEY, branchComboBox.getSelectedItem().toString());
    }

    public String getTitle() {
        return "";
    }

    private final OptionsPanel optionsPanel;

	private final JComboBox tipComboBox;
	private final JComboBox nodeComboBox;
	private final JComboBox branchComboBox;

    private final String tipKey;
	private final String nodeKey;
	private final String branchKey;
}