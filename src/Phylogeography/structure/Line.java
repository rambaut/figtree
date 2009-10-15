package phylogeography.structure;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class Line extends GeoItem {

    public Line(final Coordinates startCoordinates,
                final Style startStyle,
                final Coordinates endCoordinates,
                final Style endStyle) {
        this(null, startCoordinates, -1.0, startStyle, endCoordinates, -1.0, endStyle, 0.0, -1.0);
    }

    public Line(final Coordinates startCoordinates,
                final double startTime,
                final Style startStyle,
                final Coordinates endCoordinates,
                final Style endStyle,
                final double endTime) {
        this(null, startCoordinates, startTime, startStyle, endCoordinates, endTime, endStyle, 0.0, -1.0);
    }


    public Line(final String name,
                final Coordinates startCoordinates,
                final double startTime,
                final Style startStyle,
                final Coordinates endCoordinates,
                final double endTime,
                final Style endStyle,
                final double maxAltitude,
                final double duration) {
        super(name, startTime, duration);
        this.startCoordinates = startCoordinates;
        this.startStyle = startStyle;
        this.endCoordinates = endCoordinates;
        this.endStyle = endStyle;
        this.endTime = endTime;
        this.maxAltitude = maxAltitude;
    }

    public Coordinates getStartLocation() {
        return startCoordinates;
    }

    public Coordinates getEndLocation() {
        return endCoordinates;
    }

    public double getEndTime() {
        return endTime;
    }

    public Style getStartStyle() {
        return startStyle;
    }

    public Style getEndStyle() {
        return endStyle;
    }

    public double getMaxAltitude() {
        return maxAltitude;
    }

    private final Coordinates startCoordinates;
    private final Style startStyle;
    private final Coordinates endCoordinates;
    private final Style endStyle;
    private final double endTime;
    private final double maxAltitude;
}
