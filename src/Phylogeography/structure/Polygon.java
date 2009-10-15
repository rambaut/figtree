package phylogeography.structure;

import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class Polygon extends GeoItem {

    public Polygon(final List<Coordinates> vertices, final Style style) {
        this(null, vertices, style, -1.0, -1.0);
    }

    public Polygon(final List<Coordinates> vertices, final Style style, final double time) {
        this(null, vertices, style, time, -1.0);
    }

    public Polygon(String name, final List<Coordinates> vertices, final Style style, final double time, final double duration) {
        super(name, time, duration);
        this.vertices = vertices;
        this.style = style;
    }

    private final List<Coordinates> vertices;
    private final Style style;
}