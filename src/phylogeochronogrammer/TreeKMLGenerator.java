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

    // used to calibrate the color range for the branches
    double minRate;
    double maxRate;
    double medianRate;

    double maxHeight;
    double maxBranchLength;

    private final RootedTree tree;

    public TreeKMLGenerator(RootedTree tree) {
        this.tree = tree;

        double[] rates = new double[(tree.getNodes().size() - 1)];

        maxBranchLength = 0.0;
        int counter = 0;
        for (Node node : tree.getNodes()) {
            if (!tree.isRoot(node)) {
                rates[counter] = (Double)node.getAttribute("rate");
                counter ++;

                if (tree.getLength(node) > maxBranchLength) {
                    maxBranchLength = tree.getLength(node);
                }
            }
        }

        minRate = DiscreteStatistics.min(rates);
        maxRate = DiscreteStatistics.max(rates);
        medianRate = DiscreteStatistics.quantile(0.5, rates);

        maxHeight = tree.getHeight(tree.getRootNode());
    }

    public Element generate(String documentName, Settings settings) {

        Element root = new Element("kml");
        root.setNamespace(Namespace.getNamespace("http://earth.google.com/kml/2.2"));

        Element doc = new Element("Document");
        doc.addContent(generateElement("name", documentName));

        List<Element> styles = new ArrayList<Element>();
        List<Element> trees = new ArrayList<Element>();
        List<Element> projections = new ArrayList<Element>();
        List<Element> labels = new ArrayList<Element>();
        List<Element> contours = new ArrayList<Element>();

        if (settings.getAnalysisType() == AnalysisType.CONTINUOUS) {
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
                    generateContour(contours, styles, tree,
                            settings.getGroundContours(), node,  nodeNumber, 0.0, settings.getMostRecentDate(),
                            settings.getTraitName(),  settings.getLatitudeName(), settings.getLongitudeName(),
                            true);
                }
            }

        } else if (settings.getAnalysisType() == AnalysisType.DISCRETE) {

        } else {
            throw new IllegalArgumentException("Unknown AnalysisType");
        }

        doc.addContent(styles);
        doc.addContent(trees);

        if (projections.size() > 0) {
            Element placeMark = generateContainer("PlaceMark", "Projections", "projections from tips to surface", true, null);
            placeMark.addContent(new Element("MultiGeometry").addContent(projections));
            doc.addContent(placeMark);
        }

        if (labels.size() > 0) {
            Element folder = generateContainer("Folder", "Taxon Labels", "Taxon labels", true, null);
            folder.addContent(labels);
//        placeMark.addContent(new Element("MultiGeometry").addContent(labels));
            doc.addContent(folder);
        }

        if (contours.size() > 0) {
            Element folder = generateContainer("Folder", "Surface Contours", "Contours overlaid on the surface to represent location", true, null);
            folder.addContent(contours);
//        placeMark.addContent(new Element("MultiGeometry").addContent(contours));
            doc.addContent(folder);
        }

        root.addContent(doc);

        return root;
    }

    private Element generateTree(RootedTree tree, Settings settings, TreeSettings treeSettings, List<Element> styles) {

        BranchDecoration branches = treeSettings.getBranchDecoration();

        Element element = generateContainer("Folder", treeSettings.getName(), treeSettings.getDescription(), branches.isVisible(), null);

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

                if (treeSettings.getTreeType() == TreeType.RECTANGLE_TREE || treeSettings.getTreeType() == TreeType.TRIANGLE_TREE) {
                    String nodeName = treeSettings.getName() + "_node" + nodeNumber;
                    String styleName = nodeName + "_style";
                    Element placeMark = generateContainer("Placemark", nodeName, null, "#" + styleName);

                    Element lineString = new Element("LineString");
                    lineString.addContent(generateElement("altitudeMode", "relativeToGround"));

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

                    double width = branches.getBranchWidth();
                    if (branches.getWidthProperty() != null) {
                        double property = getDoubleNodeAttribute(node, branches.getWidthProperty(), 0.0);
                        width += property * branches.getBranchWidthScale();
                    }

                    String color = getKMLColor(branches.getStartColor());
                    if (branches.getColorProperty() != null) {
                        double property;
                        if (branches.getColorProperty().equalsIgnoreCase("height")) {
                            property = tree.getHeight(node) / maxHeight;
                        } else {
                            property = getDoubleNodeAttribute(node, branches.getColorProperty());
                        }
                        color = getKMLColor((float)property, branches.getStartColor(), branches.getEndColor());
                    }

                    styles.add(generateLineStyle(styleName, width, color));

                } else if (treeSettings.getTreeType() == TreeType.SURFACE_TREE || treeSettings.getTreeType() == TreeType.ARC_TREE) {
                    //variables required for chopping up the branches of the surface Tree
                    double maxAltitude = settings.getPlotAltitude() * (tree.getHeight(parentNode) - tree.getHeight(node)) / maxBranchLength;

                    double latDiff = parentLatitude - latitude;
                    double latDelta = latDiff / settings.getTimeDivisionCount();
                    double longDiff = parentLongitude - longitude;
                    double longDelta = longDiff / settings.getTimeDivisionCount();

                    String nodeName = treeSettings.getName() + "_node" + nodeNumber;
                    Element folder = generateContainer("Folder", nodeName, null, null);
                    for (int division = 0; division < settings.getTimeDivisionCount(); division++) {
                        String partName = nodeName + "_part" + (division + 1);
                        String styleName = partName + "_style";
                        Element placeMark = generateContainer("Placemark", partName, null, "#" + styleName);

                        Element timeSpan = new Element("TimeSpan");

                        //convert height of the branch segment to a real date (based on th date for the most recent sample)
                        double date = settings.getMostRecentDate() - (tree.getHeight(node) + (division + 1) *
                                ((tree.getHeight(parentNode) - (tree.getHeight(node))) / settings.getTimeDivisionCount()));
                        timeSpan.addContent(generateElement("begin", getKMLDate(date)));
                        placeMark.addContent(timeSpan);


                        Element lineString = new Element("LineString");
                        if (treeSettings.getTreeType() == TreeType.ARC_TREE) {
                            lineString.addContent(generateElement("altitudeMode", "absolute"));
                            lineString.addContent(generateElement("tessellate", true));
                        } else {
                            lineString.addContent(generateElement("altitudeMode", "clampToGround"));
                        }

                        Element coordinates = new Element("coordinates");
                        double t = 2.0 / (double)settings.getTimeDivisionCount();
                        coordinates.addContent("" +
                                (longitude + division * longDelta) + "," +
                                (latitude + division * latDelta) + "," +
                                (maxAltitude * Math.sin(Math.acos(1.0 - division * t))) + "\r");
                        coordinates.addContent("" +
                                (longitude + (division + 1) * longDelta) + "," +
                                (latitude + (division + 1) * latDelta) + "," +
                                (maxAltitude * Math.sin(Math.acos(1.0 - ((division + 1) * t)))) + "\r");

                        lineString.addContent(coordinates);

                        placeMark.addContent(lineString);

                        folder.addContent(placeMark);

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

                        styles.add(generateLineStyle(styleName, width, color));
                    }

                    element.addContent(folder);

                }
            }
        }

        return element;
    }

    private void generateContour(List<Element> contours, List<Element> styles, RootedTree tree, SurfaceDecoration surfaces,
                                 Node node, int nodeNumber, double plotHeight, double mostRecentDate,
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
//            placeMark.addContent(generateElement("visibility", surface.isVisible()));
            Element timeSpan = new Element("TimeSpan");

            double date = mostRecentDate - tree.getHeight(node);

            timeSpan.addContent(generateElement("begin", getKMLDate(date)));
            placeMark.addContent(timeSpan);

            String styleName = "contour"+ nodeNumber + "_style";
            placeMark.addContent(generateElement("styleUrl", "#" + styleName));

            Element polygon = new Element("Polygon");
            polygon.addContent(generateElement("altitudeMode", altitudeMode));
            if (groundContour) {
                polygon.addContent(generateElement("tessellate", true));
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

            styles.add(generatePolyStyle(styleName, getKMLColor(surfaces.getStartColor(), surfaces.getOpacity())));
        }
    }


    private void generateProjection(List<Element> projections, double altitude, double latitude, double longitude) {

        Element lineString = new Element("LineString");
        lineString.addContent(generateElement("altitudeMode", "relativeToGround"));
        Element coordinates = new Element("coordinates");
        coordinates.addContent(""+longitude+","+latitude+","+altitude+"\r");
        coordinates.addContent(""+longitude+","+latitude+",0\r");
        lineString.addContent(coordinates);

        projections.add(lineString);
    }


    private void generateTaxonLabel(List<Element> labels, RootedTree tree, SurfaceDecoration surface,
                                    Node node, double latitude, double longitude) {

        Element placeMark = generateContainer("PlaceMark", tree.getTaxon(node).getName(), null, surface.isVisible(), null);

        Element point = new Element("Point");
        point.addContent(generateElement("altitudeMode", "relativeToGround"));
        Element coordinates = new Element("coordinates");
        coordinates.addContent(""+longitude+","+latitude+",0\r");
        point.addContent(coordinates);
        placeMark.addContent(point);

        labels.add(placeMark);
    }

    private Element generateLineStyle(String styleName, double width, String color) {
        Element style = new Element("Style");
        style.setAttribute("id", styleName);

        Element lineStyle = new Element("LineStyle");
        lineStyle.addContent(generateElement("width", "" + width));
        lineStyle.addContent(generateElement("color", color));
        style.addContent(lineStyle);

        return style;
    }

    private Element generatePolyStyle(String styleName, String color) {
        Element style = new Element("Style");
        style.setAttribute("id", styleName);

        Element polyStyle = new Element("PolyStyle");
        polyStyle.addContent(generateElement("color", color));
        polyStyle.addContent(generateElement("outline", false));
        style.addContent(polyStyle);

        return style;
    }

    private Element generatePolyStyle(String styleName, String color, double outlineWidth, String outlineColor) {
        Element style = new Element("Style");
        style.setAttribute("id", styleName);

        Element lineStyle = new Element("LineStyle");
        lineStyle.addContent(generateElement("width", "" + outlineWidth));
        lineStyle.addContent(generateElement("color", outlineColor));
        style.addContent(lineStyle);

        Element polyStyle = new Element("PolyStyle");
        polyStyle.addContent(generateElement("color", color));
        polyStyle.addContent(generateElement("outline", true));
        style.addContent(polyStyle);

        return style;
    }


    private Element generateContainer(String elementTag, String name, String description, String styleURL) {
        Element element = new Element(elementTag);
        if (name != null) {
            element.addContent(generateElement("name", name));
        }
        if (description != null) {
            element.addContent(generateElement("description", description));
        }
        if (styleURL != null) {
            element.addContent(generateElement("styleUrl", styleURL));
        }
        return element;
    }

    private Element generateContainer(String elementTag, String name, String description, boolean visibility, String styleURL) {
        Element element = new Element(elementTag);
        if (name != null) {
            element.addContent(generateElement("name", name));
        }
        if (description != null) {
            element.addContent(generateElement("description", description));
        }
        if (styleURL != null) {
            element.addContent(generateElement("styleUrl", styleURL));
        }
        element.addContent(generateElement("visibility", visibility));
        return element;
    }

    private Element generateElement(String elementName, boolean content) {
        Element e = new Element(elementName);
        e.addContent(content ? "1" : "0");
        return e;
    }

    private Element generateElement(String elementName, String content) {
        Element e = new Element(elementName);
        e.addContent(content);
        return e;
    }

    private static String getKMLDate(double fractionalDate) {

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

        double fractionalMonth = fractionalDate - year;

        int month = (int) (12.0 * fractionalMonth);
        String monthString;

        if (month < 10) {
            monthString = "0"+month;
        } else {
            monthString = ""+month;
        }

        int day = (int) Math.round(30*(12*fractionalMonth - month));
        String dayString;

        if (day < 10) {
            dayString = "0"+day;
        } else {
            dayString = ""+day;
        }


        return yearString + "-" + monthString + "-" + dayString;
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

    /**
     * converts a Java color into a 4 channel hex color string.
     * @param color
     * @return the color string
     */
    public static String getKMLColor(Color color, double opacity) {
        int alpha = (int)(256 * (1.0 - opacity));
        String a = Integer.toHexString(alpha);
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

        TreeKMLGenerator generator = new TreeKMLGenerator(tree);
        Settings settings = new Settings(AnalysisType.CONTINUOUS);
        settings.setMostRecentDate(2006);
        
        try {

            BufferedWriter out = new BufferedWriter(new FileWriter(args[0]+".kml"));
            Document doc = new Document(generator.generate(args[0], settings));

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