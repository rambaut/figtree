package figtree.webui;

import java.awt.BasicStroke;
import java.awt.Font;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import jebl.evolution.io.ImportException;
import jebl.evolution.io.NewickImporter;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.SortedRootedTree;
import jebl.evolution.trees.TransformedRootedTree;
import jebl.evolution.trees.Tree;
import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal1.Listener;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBoxLayout;
import eu.webtoolkit.jwt.WCheckBox;
import eu.webtoolkit.jwt.WComboBox;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WFormWidget;
import eu.webtoolkit.jwt.WHBoxLayout;
import eu.webtoolkit.jwt.WIntValidator;
import eu.webtoolkit.jwt.WLabel;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WSpinBox;
import eu.webtoolkit.jwt.WVBoxLayout;
import figtree.application.FigTreeNexusImporter;
import figtree.panel.SimpleLabelPainter;
import figtree.treeviewer.TreePane;
import figtree.treeviewer.treelayouts.PolarTreeLayout;
import figtree.treeviewer.treelayouts.RadialTreeLayout;
import figtree.treeviewer.treelayouts.RectilinearTreeLayout;

public class FigTreeWebApplication extends WApplication {
	private TreeWidget treeWidget;
	private SimpleLabelPainter tipPainter;
	private SimpleLabelPainter nodePainter;
	private SimpleLabelPainter branchPainter;

	public FigTreeWebApplication(WEnvironment env) {
		super(env);
		setTitle("FigTree");
		useStyleSheet("figtree.css");

		WVBoxLayout layout = new WVBoxLayout(getRoot());

		FileUploadWidget uploadWidget = new FileUploadWidget();
		uploadWidget.fileUploaded().addListener(this, new Listener<String>() {
			@Override
			public void trigger(String path) {
				readFile(path);
			}
		});
		
		layout.addWidget(uploadWidget);
		layout.addWidget(treeWidget = new TreeWidget(), 1);
		treeWidget.setStyleClass("tree");

		readFile("/etc/figtree/example.tree");

		treeWidget.getTreePane().setTipLabelPainter
			(tipPainter = new SimpleLabelPainter(SimpleLabelPainter.PainterIntent.TIP));
		treeWidget.getTreePane().setNodeLabelPainter
			(nodePainter = new SimpleLabelPainter(SimpleLabelPainter.PainterIntent.NODE));
		treeWidget.getTreePane().setBranchLabelPainter
			(branchPainter = new SimpleLabelPainter(SimpleLabelPainter.PainterIntent.BRANCH));

		createControls(layout);
	}


	private void readFile(String path) {
		try {
			FileReader reader = new FileReader(path);
			readData(reader, true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void createControls(WVBoxLayout layout) {
		WHBoxLayout controlsLayout = new WHBoxLayout();
		layout.addLayout(controlsLayout, 0, AlignmentFlag.AlignJustify, AlignmentFlag.AlignTop);
		
		WVBoxLayout column = new WVBoxLayout();
		controlsLayout.addLayout(column, 0, AlignmentFlag.AlignJustify, AlignmentFlag.AlignMiddle);

		WComboBox box = new WComboBox();
		addField(column, "Layout: ", box, AlignmentFlag.AlignLeft);

		box.addItem("Rectilinear");
		box.addItem("Radial");
		box.addItem("Polar");
		box.activated().addListener(this, new Listener<Integer>() {
			@Override
			public void trigger(Integer choice) {
				switch (choice) {
				case 0:
					treeWidget.getTreePane().setTreeLayout(new RectilinearTreeLayout()); break;
				case 1:
					treeWidget.getTreePane().setTreeLayout(new RadialTreeLayout()); break;
				case 2:
					treeWidget.getTreePane().setTreeLayout(new PolarTreeLayout());
				}
			}
		});

		column = new WVBoxLayout();
		controlsLayout.addLayout(column, 0, AlignmentFlag.AlignJustify, AlignmentFlag.AlignMiddle);

		WSpinBox spinBox = new WSpinBox();
		spinBox.setValue(2);
		spinBox.setRange(0, 48);
		spinBox.setMaxLength(3);
		addField(column, "Line Weight: ", spinBox, AlignmentFlag.AlignRight);
		
		spinBox.valueChanged().addListener(this, new Listener<Double>() {
			@Override
			public void trigger(Double value) {
				treeWidget.getTreePane().setBranchStroke(new BasicStroke(value.floatValue()));
			}
		});
		spinBox.valueChanged().trigger(spinBox.getValue());
		
		spinBox = new WSpinBox();
		spinBox.setValue(6);
		spinBox.setRange(0, 48);
		spinBox.setValidator(new WIntValidator(0, 48));
		spinBox.setMaxLength(3);
		addField(column, "Font Size: ", spinBox, AlignmentFlag.AlignRight);
		
		spinBox.valueChanged().addListener(this, new Listener<Double>() {
			@Override
			public void trigger(Double value) {
				tipPainter.setFont(new Font("sansserif", Font.PLAIN, value.intValue()));
			}
		});
		spinBox.valueChanged().trigger(spinBox.getValue());
		
		column = new WVBoxLayout();
		controlsLayout.addLayout(column, 0, AlignmentFlag.AlignJustify, AlignmentFlag.AlignMiddle);

		final WCheckBox midPointCheck = new WCheckBox("Midpoint root");
		
		midPointCheck.setChecked(false);
		addField(column, null, midPointCheck, AlignmentFlag.AlignRight);

		midPointCheck.changed().addListener(this, new Signal.Listener() {
			@Override
			public void trigger() {
				if (midPointCheck.isChecked()) {
                    treeWidget.getTreePane().setRootingOn(true);
                    treeWidget.getTreePane().setRootingType(TreePane.RootingType.MID_POINT);
                } else {
                	treeWidget.getTreePane().setRootingOn(false);
                	treeWidget.getTreePane().setRootingType(TreePane.RootingType.USER_ROOTING);
                }
			}
		});
		
		final WComboBox orderCombo = new WComboBox();
		orderCombo.addItem("Off");
		orderCombo.addItem("Increasing");
		orderCombo.addItem("Decreasing");
		orderCombo.resize(new WLength(120), WLength.Auto);
		
		addField(column, "Order: ", orderCombo, AlignmentFlag.AlignRight);
		
		orderCombo.changed().addListener(this, new Signal.Listener() {
			@Override
			public void trigger() {
				if (orderCombo.getCurrentIndex() == 0) {
					treeWidget.getTreePane().setOrderBranchesOn(false);
                } else {
                	treeWidget.getTreePane().setOrderBranchesOn(true);
                	treeWidget.getTreePane().setBranchOrdering(SortedRootedTree.BranchOrdering.values()[orderCombo.getCurrentIndex() - 1]);
                }
			}
		});
		
		final WComboBox transformCombo = new WComboBox();
		transformCombo.addItem("Off");
		transformCombo.addItem(TransformedRootedTree.Transform.CLADOGRAM.toString());
		transformCombo.addItem(TransformedRootedTree.Transform.PROPORTIONAL.toString());
		transformCombo.addItem(TransformedRootedTree.Transform.EQUAL_LENGTHS.toString());
		transformCombo.resize(new WLength(120), WLength.Auto);

		addField(column, "Transform: ", transformCombo, AlignmentFlag.AlignRight);
		
        transformCombo.changed().addListener(this, new Signal.Listener() {
        	@Override
            public void trigger() {
                if (transformCombo.getCurrentIndex() == 0) {
                	treeWidget.getTreePane().setTransformBranchesOn(false);
                } else {
                	treeWidget.getTreePane().setTransformBranchesOn(true);
                	treeWidget.getTreePane().setBranchTransform(TransformedRootedTree.Transform.values()[transformCombo.getCurrentIndex() - 1]);
                }
            }
        });

		column = new WVBoxLayout();
		controlsLayout.addLayout(column, 0, AlignmentFlag.AlignJustify, AlignmentFlag.AlignMiddle);
		WComboBox combo = addLabelCombo(column, tipPainter, "Tips: ");
		combo.setCurrentIndex(1);
		combo.changed().trigger();
		addLabelCombo(column, nodePainter, "Nodes: ");
		addLabelCombo(column, branchPainter, "Branches: ");
	}
	

	private WComboBox addLabelCombo(WVBoxLayout column,	final SimpleLabelPainter labelPainter, String title) {
		labelPainter.setNumberFormat(new DecimalFormat("#.####"));
		labelPainter.setFont(new Font("sansserif", Font.PLAIN, 8));
		
		String[] attributes = labelPainter.getAttributes();

		final WComboBox displayAttributeCombo = new WComboBox();
		displayAttributeCombo.addItem("None");
		for (String attr : attributes) {
			displayAttributeCombo.addItem(attr);
		}

		addField(column, title, displayAttributeCombo, AlignmentFlag.AlignRight);

		displayAttributeCombo.changed().addListener(this, new Signal.Listener() {
			@Override
			public void trigger() {
		        String attribute = (String)displayAttributeCombo.getCurrentText().toString();
			    if (attribute.equals("none")) {
				    labelPainter.setVisible(false);
			    } else {
		            labelPainter.setDisplayAttribute(attribute);
				    labelPainter.setVisible(true);
			    }
		    }
		});
		displayAttributeCombo.changed().trigger();
		
		return displayAttributeCombo;
	}


	private void addField(WBoxLayout controlsLayout, String label, WFormWidget widget, AlignmentFlag alignment) {
		WContainerWidget w = new WContainerWidget();
		if (label != null) {
			WLabel l;
			w.addWidget(l = new WLabel(label));
			l.setBuddy(widget);
		}
		w.addWidget(widget);
		controlsLayout.addWidget(w, 0, alignment, AlignmentFlag.AlignTop);
	}


	protected boolean readData(Reader reader, boolean isNexus) throws IOException {

		List<Tree> trees = new ArrayList<Tree>();

		try {
			if (isNexus) {
				FigTreeNexusImporter importer = new FigTreeNexusImporter(reader);
				while (importer.hasTree()) {
					Tree tree = importer.importNextTree();
					trees.add(tree);
				}
			} else {
				NewickImporter importer = new NewickImporter(reader, true);
				while (importer.hasTree()) {
					Tree tree = importer.importNextTree();
					trees.add(tree);
				}
			}

			if (trees.size() == 0) {
				throw new ImportException("This file contained no trees.");
			}

			treeWidget.getTreePane().setTree((RootedTree) trees.get(0));
		} catch (ImportException ie) {
			// FIXME
			return false;
		}

		return true;
	}
}
