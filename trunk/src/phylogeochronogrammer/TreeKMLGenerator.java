package phylogeochronogrammer;

import jebl.evolution.trees.RootedTree;
import jebl.evolution.graphs.Node;
import jebl.evolution.io.TreeImporter;
import jebl.evolution.io.NexusImporter;
import jebl.evolution.io.ImportException;
import org.jdom.*;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;

import java.util.List;
import java.util.ArrayList;
import java.awt.*;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * @author Philippe Lemey
 * @author Andrew Rambaut
 * @version $Id$
 */
public class TreeKMLGenerator {

    // additional variables
    double[] rateMinMaxMedian; // used to calibrate the color range for the branches
    double[] heightMinAndMax;   // used to calibrate the color range for branches or node hpd polygons
    double mostRecentDate;  // required to convert heights to calendar dates

    public TreeKMLGenerator(){

    }

    public Element generate(String documentName, RootedTree tree, Settings settings) {


        heightMinAndMax = getHeightMinAndMax(tree);
        rateMinMaxMedian = getRateMinMaxMedian(tree);

        Element root = new Element("kml");
        root.setNamespace(Namespace.getNamespace("http://earth.google.com/kml/2.2"));

        Element doc = new Element("Document");
        doc.addContent(generate("name", documentName));

        List<Element> styles = new ArrayList<Element>();
        List<Element> trees = new ArrayList<Element>();
        List<Element> projections = new ArrayList<Element>();
        List<Element> labels = new ArrayList<Element>();
        List<Element> contours = new ArrayList<Element>();

        trees.add(generateTree(tree, settings, settings.getAltitudeTreeSettings(),  styles));
        trees.add(generateTree(tree, settings, settings.getGroundTreeSettings(),  styles));

        double scaleFactor = settings.getPlotAltitude()/tree.getHeight(tree.getRootNode());

        int nodeNumber = 0;
        for (Node node : tree.getNodes()) {
            nodeNumber++;

            double latitude = getDoubleNodeAttribute(node, settings.getLatitudeName());
            double longitude = getDoubleNodeAttribute(node, settings.getLongitudeName());
            double altitude = (tree.getHeight(node)*scaleFactor);


            if (tree.isExternal(node)) {
                generateProjection(projections, altitude, latitude, longitude);

                generateTaxonLabel(labels, tree,
                        settings.getGroundContours(),
                        node, latitude, longitude);
            } else {
                generateContour(contours,  tree,
                        settings.getGroundContours(),
                        node,  nodeNumber, 0.0, mostRecentDate,
                        settings.getTraitName(),  settings.getLatitudeName(), settings.getLongitudeName(),
                        true);
            }
        }
        doc.addContent(styles);
        doc.addContent(trees);

        Element placeMark = new Element("PlaceMark");
        placeMark.addContent(generate("name", "Projections"));
        placeMark.addContent(generate("description", "projections from tips to surface"));
//        placeMark.addContent(generate("visibility", settings.getProjections().isVisible()));
//        placeMark.addContent(projections);
        placeMark.addContent(new Element("MultiGeometry").addContent(projections));
        doc.addContent(placeMark);

        placeMark = new Element("Folder");
        placeMark.addContent(generate("name", "Taxon Labels"));
        placeMark.addContent(generate("description", "Taxon labels"));
//        placeMark.addContent(generate("visibility", settings.getProjections().isVisible()));
        placeMark.addContent(labels);
//        placeMark.addContent(new Element("MultiGeometry").addContent(labels));
        doc.addContent(placeMark);

        placeMark = new Element("Folder");
        placeMark.addContent(generate("name", "Surface Contours"));
        placeMark.addContent(generate("description", "Surface contours"));
        //placeMark.addContent(generate("visibility", settings.getProjections().isVisible()));
        placeMark.addContent(contours);
//        placeMark.addContent(new Element("MultiGeometry").addContent(contours));

        doc.addContent(placeMark);

        root.addContent(doc);

        return root;
    }

    private Element generateTree(RootedTree tree, Settings settings, TreeSettings treeSettings, List<Element> styles) {

        Element element = new Element("Folder");
        element.addContent(generate("name", treeSettings.getName()));
        element.addContent(generate("description", treeSettings.getDescription()));

        double scaleFactor = settings.getPlotAltitude() / tree.getHeight(tree.getRootNode());

        int nodeNumber = 0;
        for (Node node : tree.getNodes()) {
            nodeNumber++;
            double latitude = getDoubleNodeAttribute(node, settings.getLatitudeName());
            double longitude = getDoubleNodeAttribute(node, settings.getLongitudeName());
            double altitude = (tree.getHeight(node)*scaleFactor);

            if (!tree.isRoot(node)) {

                // Create each branch of the tree..

                Node parentNode = tree.getParent(node);
                double parentLatitude = getDoubleNodeAttribute(parentNode, settings.getLatitudeName());
                double parentLongitude = getDoubleNodeAttribute(parentNode, settings.getLongitudeName());
                double parentAltitude = (tree.getHeight(parentNode)*scaleFactor);

                BranchDecoration branches = treeSettings.getBranchDecoration();

                if (treeSettings.getTreeType() == TreeType.RECTANGLE_TREE || treeSettings.getTreeType() == TreeType.TRIANGLE_TREE) {
                    Element placeMark = new Element("Placemark");
                    placeMark.addContent(generate("visibility", branches.isVisible()));
                    String nodeName = treeSettings.getName() + "_node" + nodeNumber;
                    placeMark.addContent(generate("name", nodeName));
                    placeMark.addContent(generate("styleUrl", "#" + nodeName +"_style"));

                    Element lineString = new Element("LineString");
                    lineString.addContent(generate("altitudeMode", "relativeToGround"));

                    Element coordinates = new Element("coordinates");
                    if (treeSettings.getTreeType() == TreeType.RECTANGLE_TREE) {
                        coordinates.addContent(""+longitude+","+latitude+","+altitude+"\r");
                        coordinates.addContent(""+longitude+","+latitude+","+parentAltitude+"\r");
                        coordinates.addContent(""+parentLongitude+","+parentLatitude+","+parentAltitude+"\r");
                    } else {

                    }
                    lineString.addContent(coordinates);

                    placeMark.addContent(lineString);

                    element.addContent(placeMark);

                    Element style = new Element("Style");
                    style.setAttribute("id", nodeName + "_style");
                    Element lineStyle = new Element("LineStyle");
                    double width = branches.getBranchWidth();
                    if (branches.getWidthProperty() != null) {
                        double property = getDoubleNodeAttribute(node, branches.getWidthProperty(), 0.0);
                        width += property * branches.getBranchWidthScale();
                    }

                    String color = getKMLColor(branches.getStartColor());

                    if (branches.getColorProperty() != null) {
                        double property;
                        if (branches.getColorProperty().equalsIgnoreCase("height")) {
                            property = tree.getHeight(node) / heightMinAndMax[1];
                        } else {
                            property = getDoubleNodeAttribute(node, branches.getColorProperty());
                        }
                        color = getKMLColor((float)property, branches.getStartColor(), branches.getEndColor());
                    }
                    lineStyle.addContent(generate("width", "" + width));
                    lineStyle.addContent(generate("color", color));
                    style.addContent(lineStyle);
                    styles.add(style);
                } else if (treeSettings.getTreeType() == TreeType.SURFACE_TREE || treeSettings.getTreeType() == TreeType.ARC_TREE) {
                    //variables required for chopping up the branches of the surface Tree
                    double maxAltitude = (tree.getHeight(parentNode) - tree.getHeight(node) )*
                            (settings.getPlotAltitude() / heightMinAndMax[0]);
                    double latDiff = parentLatitude - latitude;
                    double latDelta = latDiff / settings.getTimeDivisionCount();
                    double longDiff = parentLongitude - longitude;
                    double longDelta = longDiff / settings.getTimeDivisionCount();

                    Element folder = new Element("Folder");
                    folder.addContent(generate("name", "surfaceTreeBranch"+ nodeNumber));
                    for (int division = 0; division < settings.getTimeDivisionCount(); division++) {
                        Element placeMark = new Element("Placemark");
                        placeMark.addContent(generate("visibility", branches.isVisible()));
                        placeMark.addContent(generate("name", "surfaceTreeBranch"+ nodeNumber + "_part" + (division + 1)));

                        Element timeSpan = new Element("TimeSpan");

                        //convert height of the branch segment to a real date (based on th date for the most recent sample)
                        double date = mostRecentDate - (tree.getHeight(node) + (division + 1) *
                                ((tree.getHeight(parentNode) - (tree.getHeight(node))) / settings.getTimeDivisionCount()));
                        String[] yearMonthDay = convertToYearMonthDay(date);

                        timeSpan.addContent("begin" + yearMonthDay[0] + "-"+yearMonthDay[1] + "-" + yearMonthDay[2]);
                        placeMark.addContent(timeSpan);

                        placeMark.addContent(generate("styleUrl", "#surfaceTreeBranch"+ nodeNumber + "_part" + (division + 1) +"_style"));

                        Element lineString = new Element("LineString");
                        if (treeSettings.getTreeType() == TreeType.ARC_TREE) {
                            lineString.addContent(generate("altitudeMode", "absolute"));
                            lineString.addContent(generate("tessellate", true));
                        } else {
                            lineString.addContent(generate("altitudeMode", "clampToGround"));
                        }

                        Element coordinates = new Element("coordinates");
                        double t = 2.0 / (double)settings.getTimeDivisionCount();
                        coordinates.addContent("" + (longitude + division * longDelta) + "," +
                                (latitude + division * latDelta) + "," +
                                (maxAltitude * Math.sin(Math.acos(1 - division * t))) + "\r");
                        coordinates.addContent("" + (longitude + (division + 1) * longDelta) + "," +
                                (latitude + (division + 1) * latDelta) + "," +
                                (maxAltitude * Math.sin(Math.acos(1 - (division + 1) * t))) + "\r");

                        lineString.addContent(coordinates);

                        placeMark.addContent(lineString);

                        folder.addContent(placeMark);

                        Element style = new Element("Style");
                        style.setAttribute("id", "surfaceTreeBranch"+ nodeNumber +"_part"+(division + 1) + "_style");
                        Element lineStyle = new Element("LineStyle");
                        double width = branches.getBranchWidth();
                        if (branches.getWidthProperty() != null) {
                            double property = getDoubleNodeAttribute(node, branches.getWidthProperty(), 0.0);
                            width += property * branches.getBranchWidthScale();
                        }

                        String color = getKMLColor(branches.getStartColor());

                        if (branches.getColorProperty() != null) {
                            double property;
                            if (branches.getColorProperty().equalsIgnoreCase("height")) {
                                property = tree.getHeight(node) + (division + 1) *
                                        ((tree.getHeight(parentNode) - tree.getHeight(node)) / settings.getTimeDivisionCount());
                            } else {
                                property = getDoubleNodeAttribute(node, branches.getColorProperty());
                            }
                            color = getKMLColor((float)property, branches.getStartColor(), branches.getEndColor());
                        }
                        lineStyle.addContent(generate("width", "" + width));
                        lineStyle.addContent(generate("color", color));
                        style.addContent(lineStyle);

                        styles.add(style);
                    }

                    element.addContent(folder);

                }
            }
        }

        return element;
    }

    private void generateContour(List<Element> contours, RootedTree tree, SurfaceDecoration surface,
                                 Node node, int nodeNumber, double plotHeight,
                                 double mostRecentDate,
                                 String latLongName, String latitudeName, String longitudeName,
                                 boolean groundContour) {


        double altitude;
        String altitudeMode;

        if (groundContour) {
            altitude = 0;
            altitudeMode = "clampToGround";
        } else {
            double scaleFactor = plotHeight/tree.getHeight(tree.getRootNode());
            altitude = (tree.getHeight(node)*scaleFactor);
            altitudeMode = "relativeToGround";
        }

        int modality = getIntegerNodeAttribute(node, latLongName+"_95%HPD_modality");

        for (int x = 0; x < modality; x++) {
            Object[] longitudeHPDs = getArrayNodeAttribute(node, longitudeName+"_95%HPD_"+(x + 1));
            Object[] latitudeHPDs = getArrayNodeAttribute(node, latitudeName+"_95%HPD_"+(x + 1));

            Element placeMark = new Element("Placemark");
            placeMark.addContent(generate("visibility", surface.isVisible()));
            Element timeSpan = new Element("TimeSpan");

            double date = mostRecentDate - tree.getHeight(node);
            String[] yearMonthDay = convertToYearMonthDay(date);

            timeSpan.addContent("begin" + yearMonthDay[0] + "-"+yearMonthDay[1] + "-" + yearMonthDay[2]);
            placeMark.addContent(timeSpan);

            placeMark.addContent(generate("styleUrl", "#contour"+ nodeNumber + "_style"));

            Element polygon = new Element("Polygon");
            polygon.addContent(generate("altitudeMode", altitudeMode));
            if (groundContour) {
                polygon.addContent(generate("tessellate", true));
            }
            Element outerBoundaryIs = new Element("outerBoundaryIs");
            Element linearRing = new Element("LinearRing");
            Element coordinates = new Element("coordinates");
            for (int y = 0; y < longitudeHPDs.length; y++) {
                coordinates.addContent(""+longitudeHPDs[y]+","+latitudeHPDs[y]+","+altitude+"\r");
            }
            linearRing.addContent(coordinates);
            outerBoundaryIs.addContent(linearRing);
            polygon.addContent(outerBoundaryIs);

            placeMark.addContent(polygon);

            contours.add(placeMark);
        }
    }


    private void generateProjection(List<Element> projections, double altitude, double latitude, double longitude) {

        Element lineString = new Element("LineString");
        lineString.addContent(generate("altitudeMode", "relativeToGround"));
        Element coordinates = new Element("coordinates");
        coordinates.addContent(""+longitude+","+latitude+","+altitude+"\r");
        coordinates.addContent(""+longitude+","+latitude+",0\r");
        lineString.addContent(coordinates);

        projections.add(lineString);
    }


    private void generateTaxonLabel(List<Element> labels, RootedTree tree, SurfaceDecoration surface,
                                    Node node, double latitude, double longitude) {

        Element placeMark = new Element("Placemark");
        placeMark.addContent(generate("visibility", surface.isVisible()));
        placeMark.addContent(generate("name", tree.getTaxon(node).getName()));

        Element point = new Element("Point");
        point.addContent(generate("altitudeMode", "relativeToGround"));
        Element coordinates = new Element("coordinates");
        coordinates.addContent(""+longitude+","+latitude+",0\r");
        point.addContent(coordinates);
        placeMark.addContent(point);

        labels.add(placeMark);
    }


    private Element generate(String elementName, boolean content) {
        Element e = new Element(elementName);
        e.addContent(content ? "1" : "0");
        return e;
    }

    private Element generate(String elementName, String content) {
        Element e = new Element(elementName);
        e.addContent(content);
        return e;
    }

    private double[] getRateMinMaxMedian(RootedTree tree) {

        double[] minMaxMedian = new double[3];

        double[] rates = new double[(tree.getNodes().size() - 1)];

        int counter = 0;

        int i = 0;
        for (Node node : tree.getNodes()) {

            if (!tree.isRoot(node)) {

                rates[counter] = (Double)node.getAttribute("rate");
                counter ++;

            }
            i++;
        }

        double median = DiscreteStatistics.quantile(0.5, rates);

        double max = 0.0;
        double min = Double.MAX_VALUE;

        for (int j = 0; j < rates.length; j++) {


            if (rates[j] > max) {
                max = rates[j];
            }
            if (rates[j] < min) {
                min = rates[j];
            }
        }
        minMaxMedian[0] = min;
        minMaxMedian[1] = max;
        minMaxMedian[2] = median;

        return minMaxMedian;
    }

    private double[] getHeightMinAndMax(RootedTree tree) {

        double[] minAndMax = new double[2];

        double[] heights = new double[tree.getNodes().size()];

        int i = 0;
        for (Node node : tree.getNodes()) {
            heights[i] = tree.getHeight(node);
            i++;
        }

        double max = 0.0;
        double min = Double.MAX_VALUE;

        for (int j = 0; j < heights.length; j++) {


            if (heights[j] > max) {
                max = heights[j];
            }
            if (heights[j] < min) {
                min = heights[j];
            }
        }
        minAndMax[0] = min;
        minAndMax[1] = max;

        return minAndMax;
    }


    private static String[] convertToYearMonthDay(double fractionalDate) {

        String[] yearMonthDay = new String[3];

        int year = (int) fractionalDate;
        String yearString;

        if (year < 10) {
            yearString = "000"+year;
        } else if (year < 100) {
            yearString = "00"+year;
        } else if (year < 1000) {
            yearString = "0"+year;
        } else {
            yearString = ""+year;
        }

        yearMonthDay[0]  = yearString;

        double fractionalMonth = fractionalDate - year;

        int month = (int) (12.0 * fractionalMonth);
        String monthString;

        if (month < 10) {
            monthString = "0"+month;
        } else {
            monthString = ""+month;
        }

        yearMonthDay[1] = monthString;

        int day = (int) Math.round(30*(12*fractionalMonth - month));
        String dayString;

        if (day < 10) {
            dayString = "0"+day;
        } else {
            dayString = ""+day;
        }

        yearMonthDay[2] = dayString;

        return yearMonthDay;

    }


    private int getIntegerNodeAttribute(Node node, String attributeName) {
        if (node.getAttribute(attributeName) == null) {
            throw new RuntimeException("Attribute, " + attributeName + ", missing from node");
        }
        return (Integer)node.getAttribute(attributeName);
    }

    private int getIntegerNodeAttribute(Node node, String attributeName, int defaultValue) {
        if (node.getAttribute(attributeName) == null) {
            return defaultValue;
        }
        return (Integer)node.getAttribute(attributeName);
    }

    private double getDoubleNodeAttribute(Node node, String attributeName) {
        if (node.getAttribute(attributeName) == null) {
            throw new RuntimeException("Attribute, " + attributeName + ", missing from node");
        }
        return (Double)node.getAttribute(attributeName);
    }

    private double getDoubleNodeAttribute(Node node, String attributeName, double defaultValue) {
        if (node.getAttribute(attributeName) == null) {
            return defaultValue;
        }
        return (Double)node.getAttribute(attributeName);
    }

    private Object[] getArrayNodeAttribute(Node node, String attributeName) {
        if (node.getAttribute(attributeName) == null) {
            throw new RuntimeException("Attribute, " + attributeName + ", missing from node");
        }
        return (Object[])node.getAttribute(attributeName);
    }
    /**
     * converts a Java color into a 4 channel hex color string.
     * @param color
     * @return the color string
     */
    public static String getKMLColor(Color color) {
        String a = Integer.toHexString(color.getAlpha());
        String b = Integer.toHexString(color.getBlue());
        String g = Integer.toHexString(color.getGreen());
        String r = Integer.toHexString(color.getRed());
        return  (a.length() < 2 ? "0" : "") + a +
                (b.length() < 2 ? "0" : "") + b +
                (g.length() < 2 ? "0" : "") + g +
                (r.length() < 2 ? "0" : "") + r;
    }

    public static String getKMLColor(float proportion, Color startColor, Color endColor) {
        float[] start = startColor.getRGBColorComponents(null);
        float[] end = endColor.getRGBColorComponents(null);

        float[] color = new float[start.length];
        for (int i = 0; i < start.length; i++) {
            color[i] = start[i] + ((end[i] - start[i]) * proportion);
        }

        return getKMLColor(new Color(color[0], color[1], color[2]));
    }

    public static void main(String[] args) {


        String inputTreeFile = args[0];
        RootedTree tree = null;

        try {
            TreeImporter importer = new NexusImporter(new FileReader(inputTreeFile));
            tree = (RootedTree)importer.importNextTree();
        } catch (ImportException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        TreeKMLGenerator generator = new TreeKMLGenerator();
        Settings settings = new Settings();

        try {

            BufferedWriter out = new BufferedWriter(new FileWriter(args[0]+".kml"));
            Document doc = new Document(generator.generate(args[0], tree, settings));

            try {
                XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                outputter.output(doc, out);
            }
            catch (IOException e) {
                System.err.println(e);
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

    }

}