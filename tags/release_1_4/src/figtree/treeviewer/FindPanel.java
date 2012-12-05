package figtree.treeviewer;

import figtree.treeviewer.annotations.AnnotationDefinition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import figtree.ui.components.RealNumberField;
import figtree.ui.components.WholeNumberField;

/**
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

    private int selectedTargetIndex = 0;

    public FindPanel(Action findAllAction, Action findNextAction) {
        setLayout(new BorderLayout(0,0));

        Font font = UIManager.getFont("SmallSystemFont");
        if (font != null) {
            searchText.setFont(font);
        }
        searchText.setColumns(18);

        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.setOpaque(false);
        targetCombo = new JComboBox();
        targetCombo.addItem(Target.TAXON_LABEL);
        targetCombo.addItem(Target.BRANCH_LENGTH);
        targetCombo.addItem(Target.NODE_AGE);
        targetCombo.addItem(Target.ANY_ANNOTATION);

        adjustComponent(targetCombo);
        adjustComponent(textSearchCombo);
        adjustComponent(numberSearchCombo);
        adjustComponent(caseSensitiveCheck);
        panel.add(targetCombo);
        panel.add(textSearchCombo);
        panel.add(searchText);
        panel.add(caseSensitiveCheck);

        add(panel, BorderLayout.CENTER);

        if (findNextAction != null) {
            JButton nextButton = new JButton(findNextAction);
            adjustComponent(nextButton);
            panel.add(nextButton);
        }
        findAllButton = new JButton(findAllAction);
        adjustComponent(findAllButton);

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "doFind");
        getActionMap().put("doFind", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                findAllButton.doClick();
            }
        });

        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.LINE_AXIS));
        panel2.setOpaque(false);
        panel2.add(findAllButton);

        add(panel2, BorderLayout.EAST);

        targetCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Object item = targetCombo.getSelectedItem();
                if (item != null) {
                    if (item.equals(Target.BRANCH_LENGTH) || item.equals(Target.NODE_AGE)) {
                        type = AnnotationDefinition.Type.REAL;
                    } else if (item instanceof AnnotationDefinition) {
                        type = ((AnnotationDefinition) item).getType();
                    } else {
                        type = AnnotationDefinition.Type.STRING;
                    }

                    panel.removeAll();
                    panel.add(targetCombo);
                    switch (type) {
                        case INTEGER:
                            panel.add(numberSearchCombo);
                            panel.add(integerText);
                            integerText.setColumns(10);
                            panel.add(caseSensitiveCheck);
                            caseSensitiveCheck.setEnabled(false);
                            break;
                        case REAL:
                            panel.add(numberSearchCombo);
                            panel.add(doubleText);
                            doubleText.setColumns(10);
                            panel.add(caseSensitiveCheck);
                            caseSensitiveCheck.setEnabled(false);
                            break;
                        default:
                            panel.add(textSearchCombo);
                            panel.add(searchText);
                            panel.add(caseSensitiveCheck);
                            caseSensitiveCheck.setEnabled(true);
                    }
                }
                revalidate();
                repaint();
            }
        });

        targetCombo.setSelectedIndex(selectedTargetIndex);
    }

    public void setup(List<AnnotationDefinition> annotations) {
        Object item = targetCombo.getSelectedItem();

        targetCombo.removeAllItems();
        targetCombo.addItem(Target.TAXON_LABEL);
        targetCombo.addItem(Target.BRANCH_LENGTH);
        targetCombo.addItem(Target.NODE_AGE);
        for (AnnotationDefinition annotation : annotations) {
            targetCombo.addItem(annotation);
        }
        targetCombo.addItem(Target.ANY_ANNOTATION);

        targetCombo.setSelectedItem(item);
    }

    public void doFind() {
        findAllButton.doClick();
    }

    protected void adjustComponent(JComponent comp) {
        // comp.putClientProperty("Quaqua.Component.visualMargin", new Insets(0,0,0,0));
        Font font = UIManager.getFont("SmallSystemFont");
        if (font != null) {
            comp.setFont(font);
        }
        comp.putClientProperty("JComponent.sizeVariant", "small");
        if (comp instanceof JButton) {
            comp.putClientProperty("JButton.buttonType", "roundRect");
            comp.setFocusable(false);
        }
        if (comp instanceof JComboBox) {
            comp.putClientProperty("JComboBox.isSquare", Boolean.TRUE);
            comp.setFocusable(false);
        }
        if (comp instanceof JCheckBox) {
            comp.setFocusable(false);
        }
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

    final JButton findAllButton;
}