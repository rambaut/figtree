package phylogeochronogrammer;

import jebl.evolution.trees.RootedTree;
import jebl.evolution.graphs.Node;
import org.jdom.*;

import java.util.List;
import java.util.ArrayList;

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

    public TreeKMLGenerator(RootedTree tree, TreeDefinition definition){
        Document doc = new Document();

    }
    public Element generate(String documentName, RootedTree tree, List<TreeDefinition> definitions) {

        Element root = new Element("Document");
        root.addContent(generate("name", documentName));

        List<Element> styles = new ArrayList<Element>();
        List<Element> trees = new ArrayList<Element>();
        List<Element> projections = new ArrayList<Element>();
        List<Element> labels = new ArrayList<Element>();
        List<Element> contours = new ArrayList<Element>();

        generate(styles, trees, projections, labels, contours);

        root.addContent(styles);
        root.addContent(trees);
        root.addContent(projections);
        root.addContent(labels);
        root.addContent(contours);

    }

    public Element generate(RootedTree tree, List<TreeDefinition> definitions, List<Element> styles) {
        for (TreeDefinition definition : definitions) {
            trees.add(generateTree(tree, definition,  styles));
        }
    }

    public Element generateTree(RootedTree tree, TreeDefinition definition, List<Element> styles) {

        Element element = new Element("Folder");
        element.addContent(generate("name", definition.getName()));
        element.addContent(generate("description", definition.getDescription()));

        double scaleFactor = definition.getPlotAltitude() / tree.getHeight(tree.getRootNode());

        heightMinAndMax = getHeightMinAndMax(tree);
        rateMinMaxMedian = getRateMinMaxMedian(tree);

        int nodeNumber = 0;
        for (Node node : tree.getNodes()) {
            nodeNumber++;
            Double longitude = (Double)node.getAttribute(definition.getLongitudeName());
            Double latitude = (Double)node.getAttribute(definition.getLatitudeName());
            double altitude = (tree.getHeight(node)*scaleFactor);

            if (!tree.isRoot(node)) {

                Node parentNode = tree.getParent(node);
                Double parentLongitude = (Double)parentNode.getAttribute(definition.getLongitudeName());
                Double parentLatitude = (Double)parentNode.getAttribute(definition.getLatitudeName());
                double parentAltitude = (tree.getHeight(parentNode)*scaleFactor);

                BranchDecoration branches = definition.getBranchDecoration();

                if (definition.getTreeType() == TreeType.RECTANGLE_TREE) {
                    Element placeMark = new Element("Placemark");
                    placeMark.addContent(generate("visibility", branches.isVisible()));
                    placeMark.addContent(generate("name", "rectangleTreeBranch"+ nodeNumber));
                    placeMark.addContent(generate("styleUrl", "#rectangleTreeBranch"+ nodeNumber +"_style"));

                    Element lineString = new Element("LineString");
                    lineString.addContent(generate("altitudeMode", "relativeToGround"));

                    Element coordinates = new Element("coordinates");
                    coordinates.addContent(""+longitude+","+latitude+","+altitude+"\r");
                    coordinates.addContent(""+longitude+","+latitude+","+parentAltitude+"\r");
                    coordinates.addContent(""+parentLongitude+","+parentLatitude+","+parentAltitude+"\r");

                    lineString.addContent(coordinates);

                    placeMark.addContent(lineString);

                    element.addContent(placeMark);

                } else if (definition.getTreeType() == TreeType.TRIANGLE_TREE) {
                    Element placeMark = new Element("Placemark");
                    placeMark.addContent(generate("visibility", branches.isVisible()));
                    placeMark.addContent(generate("name", "triangleTreeBranch"+ nodeNumber));
                    placeMark.addContent(generate("styleUrl", "#rectangleTreeBranch"+ nodeNumber +"_style"));

                    Element lineString = new Element("LineString");
                    lineString.addContent(generate("altitudeMode", "relativeToGround"));

                    Element coordinates = new Element("coordinates");
                    coordinates.addContent(""+longitude+","+latitude+","+altitude+"\r");
                    coordinates.addContent(""+parentLongitude+","+parentLatitude+","+parentAltitude+"\r");

                    lineString.addContent(coordinates);

                    placeMark.addContent(lineString);

                    element.addContent(placeMark);

                } else if (definition.getTreeType() == TreeType.SURFACE_TREE || definition.getTreeType() == TreeType.ARC_TREE) {
                    //variables required for chopping up the branches of the surface Tree
                    double maxAltitude = (tree.getHeight(parentNode) - tree.getHeight(node))*(definition.getPlotAltitude() / heightMinAndMax[0]);
                    double latitudeDifference = parentLatitude - latitude;
                    double longitudeDifference = parentLongitude - longitude;

                    Element folder = new Element("Folder");
                    folder.addContent(generate("name", "surfaceTreeBranch"+ nodeNumber));
                    for (int division = 0; division < divisionCount; division++) {
                        Element placeMark = new Element("Placemark");
                        placeMark.addContent(generate("visibility", branches.isVisible()));
                        placeMark.addContent(generate("name", "surfaceTreeBranch"+ nodeNumber + "_part" + (division + 1));

                        Element timeSpan = new Element("TimeSpan");

                        //convert height of the branch segment to a real date (based on th date for the most recent sample)
                        double date = mostRecentDate - (tree.getHeight(node) + (division + 1) * ((tree.getHeight(parentNode) - (tree.getHeight(node)))/divider));
                        String[] yearMonthDay = convertToYearMonthDay(date);

                        timeSpan.addContent("begin" + yearMonthDay[0] + "-"+yearMonthDay[1] + "-" + yearMonthDay[2]);
                        placeMark.addContent(timeSpan);

                        placeMark.addContent(generate("styleUrl", "#surfaceTreeBranch"+ nodeNumber + "_part" + (division + 1) +"_style"));

                        Element lineString = new Element("LineString");
                        if (definition.getTreeType() == TreeType.ARC_TREE) {
                            lineString.addContent(generate("altitudeMode", "absolute"));
                            lineString.addContent(generate("tessellate", true));
                        } else {
                            lineString.addContent(generate("altitudeMode", "clampToGround"));
                        }

                        Element coordinates = new Element("coordinates");
                        coordinates.addContent(""+(longitude+a*(longitudeDifference/divider))+","+
                                (latitude+a*(latitudeDifference/divider))+","+
                                (maxAltitude*Math.sin(Math.acos(1 - a*(1.0/(divider/2.0)))))+"\r");
                        coordinates.addContent(""+(longitude+(a+1)*(longitudeDifference/divider))+","+
                                (latitude+(a+1)*(latitudeDifference/divider))+","+
                                (maxAltitude*Math.sin(Math.acos(1 - (a+1)*(1.0/(divider/2.0)))))+"\r");

                        lineString.addContent(coordinates);

                        placeMark.addContent(lineString);

                        folder.addContent(placeMark);

                        element.addContent(folder);

                        Element style = new Element("Style");
                        style.addContent(new Attribute("id", "surfaceTreeBranch"+ nodeNumber +"_part"+(a+1)+"_style")))
                        Element lineStyle = new Element("LineStyle");
                        lineStyle.addContent(generate("width", ))
                        lineStyle.addContent(generate("color", ))

                        styles.addContent(style);
                    }

                }
            }
        }

        return element;
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

    // get the coordinates of the root (to locate a placemark and its icon)
    private String getRootPointLocation(RootedTree tree, double plotHeight, String latitudeName, String longitudeName) {

        Node root = tree.getRootNode();

        return root.getAttribute(longitudeName)+","+root.getAttribute(latitudeName)+","+plotHeight;
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

    public static String getKMLColor(double value, double[] minMaxMedian, String startColor, String endColor) {

        startColor = startColor.toLowerCase();
        String startBlue = startColor.substring(0,2);
        String startGreen = startColor.substring(2,4);
        String startRed = startColor.substring(4,6);

        endColor =  endColor.toLowerCase();
        String endBlue = endColor.substring(0,2);
        String endGreen = endColor.substring(2,4);
        String endRed = endColor.substring(4,6);

        double proportion = (value - minMaxMedian[0])/(minMaxMedian[1] - minMaxMedian[0]);

// generate an array with hexadecimal code for each RGB entry number
        String[] colorTable = new String[256];

        int colorTableCounter = 0;

        for (int a = 0; a < 10; a++) {

            for (int b = 0; b < 10; b++) {

                colorTable[colorTableCounter] = a + "" + b;
                colorTableCounter ++;
            }

            for(int c = (int)('a'); c<6+(int)('a'); c++) {
                colorTable[colorTableCounter] = a + "" + (char)c;
                colorTableCounter ++;
            }

        }
        for(int d = (int)('a'); d<6+(int)('a'); d++) {

            for (int e = 0; e < 10; e++) {

                colorTable[colorTableCounter] = (char) d + "" + e;
                colorTableCounter ++;
            }

            for(int f = (int)('a'); f<6+(int)('a'); f++) {
                colorTable[colorTableCounter] = (char) d + "" + (char) f;
                colorTableCounter ++;
            }

        }


        int startBlueInt = 0;
        int startGreenInt = 0;
        int startRedInt = 0;

        int endBlueInt = 0;
        int endGreenInt = 0;
        int endRedInt = 0;

        for (int i = 0; i < colorTable.length; i ++) {

            if (colorTable[i].equals(startBlue)) {startBlueInt = i; }
            if (colorTable[i].equals(startGreen)) {startGreenInt = i; }
            if (colorTable[i].equals(startRed)) {startRedInt = i; }
            if (colorTable[i].equals(endBlue)) {endBlueInt = i; }
            if (colorTable[i].equals(endGreen)) {endGreenInt = i; }
            if (colorTable[i].equals(endRed)) {endRedInt = i; }

        }

        int blueInt = startBlueInt + (int) Math.round((endBlueInt-startBlueInt)*proportion);
        int greenInt = startGreenInt + (int) Math.round((endGreenInt-startGreenInt)*proportion);
        int redInt = startRedInt + (int) Math.round((endRedInt-startRedInt)*proportion);

        String blue = null;
        String green =  null;
        String red = null;

        for (int j = 0; j < colorTable.length; j ++) {

            if (j == blueInt) {blue = colorTable[j]; }
            if (j == greenInt) {green = colorTable[j]; }
            if (j == redInt) {red = colorTable[j]; }

        }

        String color = blue+green+red;

        return color;
    }

    private static String getOpacity(double opacity) {

        String[] opacityTable = new String[256];

        int colorTableCounter = 0;

        for (int a = 0; a < 10; a++) {

            for (int b = 0; b < 10; b++) {

                opacityTable[colorTableCounter] = a + "" + b;
                colorTableCounter ++;
            }

            for(int c = (int)('a'); c<6+(int)('a'); c++) {
                opacityTable[colorTableCounter] = a + "" + (char)c;
                colorTableCounter ++;
            }

        }
        for(int d = (int)('a'); d<6+(int)('a'); d++) {

            for (int e = 0; e < 10; e++) {

                opacityTable[colorTableCounter] = (char) d + "" + e;
                colorTableCounter ++;
            }

            for(int f = (int)('a'); f<6+(int)('a'); f++) {
                opacityTable[colorTableCounter] = (char) d + "" + (char) f;
                colorTableCounter ++;
            }

        }

        int opacityInt = (int) Math.round(opacityTable.length*opacity);

        String opacityString = "FF";

        for (int j = 0; j < opacityTable.length; j ++) {

            if (j == opacityInt) {opacityString = opacityTable[j]; }

        }

        return opacityString;
    }

}