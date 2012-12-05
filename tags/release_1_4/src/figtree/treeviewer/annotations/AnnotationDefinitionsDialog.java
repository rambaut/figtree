/*
 * AnnotationDefinitionsDialog.java
 *
 * Copyright (C) 2012 Andrew Rambaut
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

package figtree.treeviewer.annotations;

import jam.panels.ActionPanel;
import jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class AnnotationDefinitionsDialog {

    private JFrame frame;


    public AnnotationDefinitionsDialog(JFrame frame) {
        this.frame = frame;

    }

    public void showDialog(Collection<AnnotationDefinition> annotations) {

        this.annotations.clear();
        this.annotations.addAll(annotations);

        OptionsPanel options = new OptionsPanel();

        JPanel panel = new JPanel(new BorderLayout());

        annotationTableModel = new AnnotationTableModel();
        table = new JTable(annotationTableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
	            selectionChanged();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(scrollPane, BorderLayout.CENTER);

        ActionPanel actionPanel = new ActionPanel(true);
        actionPanel.setAddAction(addAnnotationAction);
        actionPanel.setRemoveAction(removeAnnotationAction);
	    actionPanel.setActionAction(editAnnotationAction);
        panel.add(actionPanel, BorderLayout.SOUTH);

        options.addSpanningComponent(panel);

        JOptionPane optionPane = new JOptionPane(options,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                new String[] { "Done" },
		        null);
        optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));

        final JDialog dialog = optionPane.createDialog(frame, "Annotation Definitions");
        dialog.pack();

	    selectionChanged();

        dialog.setVisible(true);
    }

	private void selectionChanged() {
		int row = table.getSelectedRow();
		if (row < 0) {
			removeAnnotationAction.setEnabled(false);
		} else {
			removeAnnotationAction.setEnabled(true);
		}
	}

    public List<AnnotationDefinition> getAnnotations() {
        return annotations;
    }

    int labelCount = 1;

    private void addAnnotation() {
	    AnnotationDefinition definition = new AnnotationDefinition("Label" + labelCount, AnnotationDefinition.Type.INTEGER);
		doDefineAnnotation(definition);
    }

    private void removeAnnotation() {
	    int row = table.getSelectedRow();
	    if (row < 0) return;
	    annotations.remove(row);
	    annotationTableModel.fireTableDataChanged();
    }

	private void editAnnotation() {
		int row = table.getSelectedRow();
		if (row < 0) return;
		AnnotationDefinition definition = annotations.get(row);
		doDefineAnnotation(definition);
	}

	private void doDefineAnnotation(AnnotationDefinition definition) {
		if (defineAnnotationDialog == null) {
		    defineAnnotationDialog = new DefineAnnotationDialog(frame);
		}
		int result = defineAnnotationDialog.showDialog(definition);

		if (result == JOptionPane.OK_OPTION) {
	        annotations.add(definition);
		    labelCount++;
		}
	    annotationTableModel.fireTableDataChanged();
	}


    private List<AnnotationDefinition> annotations = new ArrayList<AnnotationDefinition>();
    private AnnotationTableModel annotationTableModel;

    class AnnotationTableModel extends AbstractTableModel {
        final String[] columnNames = {"Label", "Type"};

        public int getColumnCount() { return columnNames.length; }
        public int getRowCount() {
            return annotations.size();
        }
        public String getColumnName(int col) { return columnNames[col]; }

        public Object getValueAt(int row, int col) {
            if (col == 0) {
                return annotations.get(row).getName();
            } else {
                return annotations.get(row).getType();
            }
        }

        public void setValueAt(Object value, int row, int col) {

        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private AbstractAction addAnnotationAction = new AbstractAction() {
        public void actionPerformed(ActionEvent ae) {
            addAnnotation();
        }

    };

    private AbstractAction removeAnnotationAction = new AbstractAction() {
        public void actionPerformed(ActionEvent ae) {
            removeAnnotation();
        }
    };

	private AbstractAction editAnnotationAction = new AbstractAction() {
	    public void actionPerformed(ActionEvent ae) {
	        editAnnotation();
	    }
	};

	private JTable table;
	private DefineAnnotationDialog defineAnnotationDialog = null;

}