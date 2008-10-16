package phylogeochronogrammer;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public class Settings {
    public String getTraitName() {
        return traitName;
    }

    public void setTraitName(String traitName) {
        this.traitName = traitName;
    }

    public String getLatitudeName() {
        return latitudeName;
    }

    public void setLatitudeName(String latitudeName) {
        this.latitudeName = latitudeName;
    }

    public String getLongitudeName() {
        return longitudeName;
    }

    public void setLongitudeName(String longitudeName) {
        this.longitudeName = longitudeName;
    }

    public double getPlotAltitude() {
        return plotAltitude;
    }

    public void setPlotAltitude(double plotAltitude) {
        this.plotAltitude = plotAltitude;
    }

    public List<TreeSettings> getTreeSettings() {
        return treeSettings;
    }

    public void addTreeSettings(TreeSettings treeSettings) {
        this.treeSettings.add(treeSettings);
    }

    public int getTimeDivisionCount() {
        return timeDivisionCount;
    }

    public void setTimeDivisionCount(int timeDivisionCount) {
        this.timeDivisionCount = timeDivisionCount;
    }

    public SurfaceDecoration getGroundContours() {
        return groundContours;
    }

    public void setGroundContours(SurfaceDecoration groundContours) {
        this.groundContours = groundContours;
    }

    private String traitName;
    private String latitudeName;
    private String longitudeName;

    private double plotAltitude = 500000;

    private List<TreeSettings> treeSettings = new ArrayList<TreeSettings>();
    private SurfaceDecoration groundContours;

    private int timeDivisionCount;
}
