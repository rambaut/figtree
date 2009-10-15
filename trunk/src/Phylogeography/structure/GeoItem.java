package phylogeography.structure;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public abstract class GeoItem implements Item {
    protected GeoItem(final String name, final double startTime, final double duration) {
        this.name = name;
        this.startTime = startTime;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getDuration() {
        return duration;
    }

    public boolean isContainer() {
        return false;
    }

    public boolean isVisible() {
        return true;
    }

    private final String name;
    private final double startTime;
    private final double duration;

}
