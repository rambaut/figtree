package phylogeography.structure;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class Point extends GeoItem {
    public Point(final String name, final String description, final double startTime, final double duration) {
        super(name, startTime, duration);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    private final String description;
}
