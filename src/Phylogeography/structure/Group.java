package phylogeography.structure;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class Group extends Container {

    public Group(final String name, final String description) {
        super(name, description, true);
    }

    public Group(final String name, final String description, final boolean visible) {
        super(name, description, visible);
    }
}
