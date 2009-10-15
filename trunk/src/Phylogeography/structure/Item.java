package phylogeography.structure;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public interface Item {
    boolean isContainer();
    boolean isVisible();
    String getName();
}
