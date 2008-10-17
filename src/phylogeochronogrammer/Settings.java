package phylogeochronogrammer;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public class Settings {
    public Settings(AnalysisType analysisType) {
        this.analysisType = analysisType;
    }

    public AnalysisType getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(AnalysisType analysisType) {
        this.analysisType = analysisType;
    }

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

    public double getMostRecentDate() {
        return mostRecentDate;
    }

    public void setMostRecentDate(double mostRecentDate) {
        this.mostRecentDate = mostRecentDate;
    }

    public double getPlotAltitude() {
        return plotAltitude;
    }

    public void setPlotAltitude(double plotAltitude) {
        this.plotAltitude = plotAltitude;
    }

    public TreeSettings getAltitudeTreeSettings() {
        return altitudeTreeSettings;
    }

    public TreeSettings getGroundTreeSettings() {
        return groundTreeSettings;
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

    public SurfaceDecoration getProjections() {
        return projections;
    }

    public void setProjections(SurfaceDecoration projections) {
        this.projections = projections;
    }

    private AnalysisType analysisType;

    private String traitName = "location";
    private String latitudeName = "location1";
    private String longitudeName = "location2";

    private double mostRecentDate;

    private double plotAltitude = 500000;

    private TreeSettings altitudeTreeSettings = new TreeSettings("altitudeTree", "", TreeType.RECTANGLE_TREE);
    private TreeSettings groundTreeSettings = new TreeSettings("groundTree", "", TreeType.ARC_TREE);
    private SurfaceDecoration groundContours = new SurfaceDecoration();
    private SurfaceDecoration projections = new SurfaceDecoration();

    private int timeDivisionCount = 50;
}
