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

    public double getColumnRadius() {
        return columnRadius;
    }

    public void setColumnRadius(double columnRadius) {
        this.columnRadius = columnRadius;
    }

    public int getTimeDivisionCount() {
        return timeDivisionCount;
    }

    public void setTimeDivisionCount(int timeDivisionCount) {
        this.timeDivisionCount = timeDivisionCount;
    }

    public TreeSettings getAltitudeTreeSettings() {
        return altitudeTreeSettings;
    }

    public TreeSettings getGroundTreeSettings() {
        return groundTreeSettings;
    }

    public SurfaceDecoration getGroundContours() {
        return groundContours;
    }

    public SurfaceDecoration getProjections() {
        return projections;
    }

    public SurfaceDecoration getTaxonLabels() {
        return taxonLabels;
    }

    public SurfaceDecoration getLocationLabels() {
        return locationLabels;
    }

    private AnalysisType analysisType;

    private String traitName = "location";
    private String latitudeName = "location1";
    private String longitudeName = "location2";

    private double mostRecentDate;

    private double plotAltitude = 500000;
    private double columnRadius = 200000;           

    private final TreeSettings altitudeTreeSettings = new TreeSettings("altitudeTree", "", TreeType.RECTANGLE_TREE);
    private final TreeSettings groundTreeSettings = new TreeSettings("groundTree", "", TreeType.SURFACE_TREE);
    private final SurfaceDecoration groundContours = new SurfaceDecoration();
    private final SurfaceDecoration projections = new SurfaceDecoration();
    private final SurfaceDecoration taxonLabels = new SurfaceDecoration();
    private final SurfaceDecoration locationLabels = new SurfaceDecoration();

    private int timeDivisionCount = 50;
}
