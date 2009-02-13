package figtree.panel;

import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Map;

import figtree.treeviewer.treelayouts.RectilinearTreeLayout;

/**
 * @author Andrew Rambaut
 * @version $Id: TreeLayoutController.java,v 1.1 2007/08/14 15:50:42 rambaut Exp $
 */
public class TreeLayoutController extends AbstractController {

    private static final String RECTILINEAR_LAYOUT_KEY = "rectilinearLayout";
    private static final String ALIGN_TIP_LABELS_KEY = "alignTipLabels";

    public TreeLayoutController(final RectilinearTreeLayout treeLayout) {
        titleLabel = new JLabel("Rectangular Layout");
        optionsPanel = new OptionsPanel();

        alignTipLabelsCheck = new JCheckBox("Align Tip Labels");
        alignTipLabelsCheck.setOpaque(false);

        alignTipLabelsCheck.setSelected(treeLayout.isAlignTipLabels());
        alignTipLabelsCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                treeLayout.setAlignTipLabels(alignTipLabelsCheck.isSelected());
            }
        });
        optionsPanel.addComponent(alignTipLabelsCheck);
    }

    public JComponent getTitleComponent() {
        return titleLabel;
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
        alignTipLabelsCheck.setSelected((Boolean) settings.get(RECTILINEAR_LAYOUT_KEY + "." + ALIGN_TIP_LABELS_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(RECTILINEAR_LAYOUT_KEY + "." + ALIGN_TIP_LABELS_KEY, alignTipLabelsCheck.isSelected());
    }

    private final JLabel titleLabel;
    private final OptionsPanel optionsPanel;

    private final JCheckBox alignTipLabelsCheck;

}