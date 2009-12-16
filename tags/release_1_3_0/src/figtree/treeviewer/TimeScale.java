package figtree.treeviewer;

import jebl.evolution.trees.RootedTree;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class TimeScale {

	public TimeScale(double rootAge) {
		this.rootAge = rootAge;
		this.scaleFactor = Double.NaN;
		this.offsetAge = 0.0;
	}

	public TimeScale(double scaleFactor, double offsetAge) {
		this.rootAge = Double.NaN;
		this.offsetAge = offsetAge;
		this.scaleFactor = scaleFactor;
	}

	public double getScaleFactor(RootedTree tree) {
		if (Double.isNaN(scaleFactor)) {
			return rootAge / tree.getHeight(tree.getRootNode());
		}
		return scaleFactor;
	}

	public double getAge(double height, RootedTree tree) {
		return (height * getScaleFactor(tree)) + offsetAge;
	}

	public double getTime(double length, RootedTree tree) {

		return (length * getScaleFactor(tree));
	}

    public double getHeight(double age, RootedTree tree) {
        return (age - offsetAge) / getScaleFactor(tree);
    }

    public double getLength(double time, RootedTree tree) {

        return (time / getScaleFactor(tree));
    }

	private final double rootAge;
	private final double offsetAge;
	private final double scaleFactor;
}
