package phylogeography.structure;

import java.util.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class Layer extends Container {

    public Layer(final String name, final String description) {
        super(name, description, true);
    }
    
    public Layer(final String name, final String description, final boolean visible) {
        super(name, description, visible);
    }
}
