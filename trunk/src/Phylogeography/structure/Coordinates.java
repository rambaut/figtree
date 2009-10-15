package phylogeography.structure;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class Coordinates {
    public Coordinates(final double longitude, final double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = 0.0;
    }

    public Coordinates(final double longitude, final double latitude, final double altitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    private final double longitude;
    private final double latitude;
    private final double altitude;
}
