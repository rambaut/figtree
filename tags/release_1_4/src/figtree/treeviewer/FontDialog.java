package figtree.treeviewer;

import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import figtree.treeviewer.painters.LabelPainterController;
import figtree.ui.FontChooserPanel;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class FontDialog {
    private OptionsPanel options;
    private final JFrame frame;
    private FontChooserPanel chooser = null;

    public FontDialog(final JFrame frame) {
        this.frame = frame;

    }

    public Font getFont() {
        return chooser.getSelectedFont();
    }

    public int showDialog(final Font font) {

        options = new OptionsPanel();
        if (chooser == null) {
            chooser = new FontChooserPanel(font);
        } else {
            chooser.setSelectedFont(font);
        }
        options.addSpanningComponent(chooser);

        JOptionPane optionPane = new JOptionPane(options,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null,
                null,
                null);
        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));

        final JDialog dialog = optionPane.createDialog(frame, "Setup colour range");
        dialog.pack();

        dialog.setVisible(true);

        int result = JOptionPane.CANCEL_OPTION;
        Integer value = (Integer)optionPane.getValue();
        if (value != null && value.intValue() != -1) {
            result = value.intValue();
        }

        return result;
    }
}
