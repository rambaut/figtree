package phylogeochronogrammer;

import jebl.evolution.trees.RootedTree;

/**
 * @author Andrew Rambaut
 * @author Philippe Lemey
 * @version $Id$
 */
public class TreeDefinition {
    public TreeDefinition(TreeType treeType, String traitName, String latitudeName, String longitudeName, double plotAltitude, double rootFraction, double arcAltitude, boolean taxaVisible, BranchDecoration branchDecoration, SurfaceDecoration locationContours, SurfaceDecoration groundContours, BranchDecoration projections) {
        this.treeType = treeType;
        this.traitName = traitName;
        this.latitudeName = latitudeName;
        this.longitudeName = longitudeName;
        this.plotAltitude = plotAltitude;
        this.rootFraction = rootFraction;
        this.arcAltitude = arcAltitude;
        this.taxaVisible = taxaVisible;
        this.branchDecoration = branchDecoration;
        this.locationContours = locationContours;
        this.groundContours = groundContours;
        this.projections = projections;
    }


    public TreeType getTreeType() {
        return treeType;
    }

    public void setTreeType(TreeType treeType) {
        this.treeType = treeType;
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

    public double getPlotAltitude() {
        return plotAltitude;
    }

    public void setPlotAltitude(double plotAltitude) {
        this.plotAltitude = plotAltitude;
    }

    public double getRootFraction() {
        return rootFraction;
    }

    public void setRootFraction(double rootFraction) {
        this.rootFraction = rootFraction;
    }

    public double getArcAltitude() {
        return arcAltitude;
    }

    public void setArcAltitude(double arcAltitude) {
        this.arcAltitude = arcAltitude;
    }

    public boolean isTaxaVisible() {
        return taxaVisible;
    }

    public void setTaxaVisible(boolean taxaVisible) {
        this.taxaVisible = taxaVisible;
    }

    public BranchDecoration getBranchDecoration() {
        return branchDecoration;
    }

    public void setBranchDecoration(BranchDecoration branchDecoration) {
        this.branchDecoration = branchDecoration;
    }

    public SurfaceDecoration getLocationContours() {
        return locationContours;
    }

    public void setLocationContours(SurfaceDecoration locationContours) {
        this.locationContours = locationContours;
    }

    public SurfaceDecoration getGroundContours() {
        return groundContours;
    }

    public void setGroundContours(SurfaceDecoration groundContours) {
        this.groundContours = groundContours;
    }

    public BranchDecoration getProjections() {
        return projections;
    }

    public void setProjections(BranchDecoration projections) {
        this.projections = projections;
    }

    private TreeType treeType;

    private String traitName;
    private String latitudeName;
    private String longitudeName;

    private double plotAltitude = 500000;
    private double rootFraction = 0.05; // additional fraction of the total plotHeight for the root branch

    private double arcAltitude = 10000; // this is the factor with which to multiply the time of the branch to get the altitude for that branch in the surface Tree

    private boolean taxaVisible = false;

    private BranchDecoration branchDecoration;

    private SurfaceDecoration locationContours;
    private SurfaceDecoration groundContours;

    private BranchDecoration projections;
}
