package phylogeography.generator;

import phylogeography.structure.Layer;

import java.util.Collection;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public interface Generator {
    public void generate(PrintWriter writer, final Collection<Layer> layers) throws IOException;
}
