package figtree.treeviewer.treelayouts;

import jam.controlpalettes.AbstractController;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import figtree.treeviewer.ControllerOptionsPanel;

/**
 * @author Andrew Rambaut
 * @version $Id: PolarTreeLayoutController.java 769 2007-08-30 12:14:37Z rambaut $
 */
public class PolarTreeLayoutController extends AbstractController {

    private static final String POLAR_LAYOUT_KEY = "polarLayout";

    private static final String ROOT_ANGLE_KEY = "rootAngle";
    private static final String ANGULAR_RANGE_KEY = "angularRange";
    private static final String ROOT_LENGTH_KEY = "rootLength";
    private static final String SHOW_ROOT_KEY = "showRoot";
    private static final String ALIGN_TIP_LABELS_KEY = "alignTipLabels";

    public PolarTreeLayoutController(final PolarTreeLayout treeLayout) {
        this.treeLayout = treeLayout;

        titleLabel = new JLabel("Polar Layout");

	    optionsPanel = new ControllerOptionsPanel(0, 0);

        rootAngleSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 360000, 0);
        rootAngleSlider.setOpaque(false);
        rootAngleSlider.setValue((int) ((180.0 - (treeLayout.getRootAngle()) * 1000)));
        //rootAngleSlider.setMajorTickSpacing(rootAngleSlider.getMaximum() / 5);
//        rootAngleSlider.setPaintTicks(true);

        rootAngleSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                double value = 180 + (rootAngleSlider.getValue() / 1000.0);
                treeLayout.setRootAngle(value % 360);
            }
        });
        optionsPanel.addComponentWithLabel("Root Angle:", rootAngleSlider, true);

        angularRangeSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 360000, 0);
        angularRangeSlider.setOpaque(false);
        angularRangeSlider.setValue((int) ((360.0 - (treeLayout.getAngularRange()) * 1000.0)));
        //angularRangeSlider.setMajorTickSpacing(angularRangeSlider.getMaximum() / 5);
//        angularRangeSlider.setPaintTicks(true);

        angularRangeSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                double value = 360.0 - (angularRangeSlider.getValue() / 1000.0);
                treeLayout.setAngularRange(value);
            }
        });
        optionsPanel.addComponentWithLabel("Angle Range:", angularRangeSlider, true);

        final int sliderMax = 10000;
        rootLengthSlider = new JSlider(SwingConstants.HORIZONTAL, 0, sliderMax, 0);
        rootLengthSlider.setOpaque(false);
        rootLengthSlider.setValue((int) (treeLayout.getRootLengthProportion() * sliderMax));
        //rootLengthSlider.setMajorTickSpacing(rootLengthSlider.getMaximum() / 5);
  //      rootLengthSlider.setPaintTicks(true);

        rootLengthSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                double value = rootLengthSlider.getValue();
                treeLayout.setRootLengthProportion(value / sliderMax);
            }
        });
        optionsPanel.addComponentWithLabel("Root Length:", rootLengthSlider, true);

        showRootCheck = new JCheckBox("Show Root");
        showRootCheck.setOpaque(false);
        optionsPanel.addComponent(showRootCheck);

        showRootCheck.setSelected(treeLayout.isShowingRootBranch());
        showRootCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                treeLayout.setShowingRootBranch(showRootCheck.isSelected());
            }
        });


//        labelPositionCombo = new JComboBox();
//        for (PolarTreeLayout.TipLabelPosition position : PolarTreeLayout.TipLabelPosition.values()) {
//            if (position != PolarTreeLayout.TipLabelPosition.HORIZONTAL) // not implemented yet
//                labelPositionCombo.addItem(position);
//        }
//        labelPositionCombo.setSelectedItem(treeLayout.getTipLabelPosition());
//        labelPositionCombo.addActionListener(new ActionListener() {
//        public void actionPerformed(ActionEvent event) {
//                treeLayout.setTipLabelPosition((PolarTreeLayout.TipLabelPosition) labelPositionCombo.getSelectedItem());
//
//            }
//        });
//        optionsPanel.addComponentWithLabel("Label Position:", labelPositionCombo);

        alignTipLabelsCheck = new JCheckBox("Align Tip Labels");
        alignTipLabelsCheck.setOpaque(false);

        alignTipLabelsCheck.setSelected(treeLayout.getTipLabelPosition() == PolarTreeLayout.TipLabelPosition.RADIAL);
        alignTipLabelsCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                treeLayout.setTipLabelPosition(alignTipLabelsCheck.isSelected() ? PolarTreeLayout.TipLabelPosition.RADIAL : PolarTreeLayout.TipLabelPosition.FLUSH);
            }
        });
        optionsPanel.addSpanningComponent(alignTipLabelsCheck);
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
        rootAngleSlider.setValue((Integer) settings.get(POLAR_LAYOUT_KEY + "." + ROOT_ANGLE_KEY));
        angularRangeSlider.setValue((Integer) settings.get(POLAR_LAYOUT_KEY + "." + ANGULAR_RANGE_KEY));
        rootLengthSlider.setValue((Integer) settings.get(POLAR_LAYOUT_KEY + "." + ROOT_LENGTH_KEY));
        showRootCheck.setSelected((Boolean) settings.get(POLAR_LAYOUT_KEY + "." + SHOW_ROOT_KEY));
        alignTipLabelsCheck.setSelected((Boolean) settings.get(POLAR_LAYOUT_KEY + "." + ALIGN_TIP_LABELS_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(POLAR_LAYOUT_KEY + "." + ROOT_ANGLE_KEY, rootAngleSlider.getValue());
        settings.put(POLAR_LAYOUT_KEY + "." + ANGULAR_RANGE_KEY, angularRangeSlider.getValue());
        settings.put(POLAR_LAYOUT_KEY + "." + ROOT_LENGTH_KEY, rootLengthSlider.getValue());
        settings.put(POLAR_LAYOUT_KEY + "." + SHOW_ROOT_KEY, showRootCheck.isSelected());
        settings.put(POLAR_LAYOUT_KEY + "." + ALIGN_TIP_LABELS_KEY, alignTipLabelsCheck.isSelected());
    }

    private final JLabel titleLabel;
    private final OptionsPanel optionsPanel;

    private final JSlider rootAngleSlider;
    private final JSlider rootLengthSlider;
    private final JSlider angularRangeSlider;
    //private final JComboBox labelPositionCombo;
    private final JCheckBox alignTipLabelsCheck;

    private final JCheckBox showRootCheck;

    private final PolarTreeLayout treeLayout;

}