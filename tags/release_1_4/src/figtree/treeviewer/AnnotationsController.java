package figtree.treeviewer;

import figtree.treeviewer.TreePane;
import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;
import figtree.treeviewer.annotations.AnnotationDefinition;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;

/**
 * @author Andrew Rambaut
 * @version $Id: AnnotationsController.java,v 1.8 2007/08/21 00:05:52 rambaut Exp $
 */
public class AnnotationsController extends AbstractController {

    public AnnotationsController(final TreePane treePane) {

        this.treePane = treePane;

        titleLabel = new JLabel("Annotations");

	    optionsPanel = new ControllerOptionsPanel(2, 2);

        final JComboBox combo2 = new JComboBox();
        combo2.addItem("None");
//		for (AnnotationDefinition annotation : treeViewer.getAnnotationDefinitions()) {
//		    combo2.addItem(annotation);
//		}
        combo2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Object item = combo2.getSelectedItem();
                if (item instanceof AnnotationDefinition) {
//                    treePane.setBranchDecorator(new AnnotationBranchDecorator((AnnotationDefinition)item));
                } else {
                    treePane.setBranchDecorator(null, false);
                }
            }
        });
        optionsPanel.addComponentWithLabel("Node Colours:", combo2);


    }

    public JComponent getTitleComponent() {
        return titleLabel;
    }

    public JPanel getPanel() {
        return optionsPanel;
    }

    public boolean isInitiallyVisible() {
        return true;
    }

    public void initialize() {
        // nothing to do
    }

    public void setSettings(Map<String,Object> settings) {
    }

    public void getSettings(Map<String, Object> settings) {
    }

    private final JLabel titleLabel;
    private final OptionsPanel optionsPanel;

    private final TreePane treePane;

}
