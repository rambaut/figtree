package phylogeochronogrammer;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class Location {
    public Location(String name, String state, double latitude, double longitude) {
        this.name = name;
        this.state = state;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    private final String name;
    private final String state;
    private final double latitude;
    private final double longitude;
}
