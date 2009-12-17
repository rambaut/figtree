package phylogeochronogrammer;

/**
 * @author Andrew Rambaut
 * @author Philippe Lemey
 * @version $Id$
 */
public class TreeSettings {
    public TreeSettings(String name, String description, TreeType treeType) {
        this.name = name;
        this.description = description;
        this.treeType = treeType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TreeType getTreeType() {
        return treeType;
    }

    public void setTreeType(TreeType treeType) {
        this.treeType = treeType;
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

    public BranchStyle getBranchStyle() {
        return branchStyle;
    }

    public SurfaceDecoration getLocationContourStyle() {
        return locationContourStyle;
    }

    public SurfaceDecoration getGroundContourStyle() {
        return groundContourStyle;
    }

    public void setGroundContourStyle(SurfaceDecoration groundContourStyle) {
        this.groundContourStyle = groundContourStyle;
    }

    public BranchStyle getProjectionStyle() {
        return projectionStyle;
    }

    private String name;
    private String description;

    private TreeType treeType;

    private double rootFraction = 0.05; // additional fraction of the total plotHeight for the root branch

    private double arcAltitude = 10000; // this is the factor with which to multiply the time of the branch to get the altitude for that branch in the surface Tree

    private boolean taxaVisible = false;

    private BranchStyle branchStyle = new BranchStyle();

    private SurfaceDecoration locationContourStyle = new SurfaceDecoration();
    private SurfaceDecoration groundContourStyle = new SurfaceDecoration();

    private BranchStyle projectionStyle = new BranchStyle();
}
