package phylogeochronogrammer;

import jebl.evolution.trees.RootedTree;
import jebl.evolution.graphs.Node;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: phil
 * Date: May 16, 2008
 * Time: 11:30:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class DiscreteKML {

    //input related variables
    RootedTree treeToExport;
    String documentName;
    String[] stateNames;
    double[][] stateCoordinates;
    String[] states;

    double branchWidthConstant = 2.0;   // the width of branches will be stateProbability*branchWidthMultiplier+branchWidthConstant
    double branchWidthMultiplier = 5.0;

    double divider = 20; // this is to chop up the branches of the surface tree in 'divider' segments
    boolean useStateProbability = true; // use state probabilities for branch width
    double branchWidth = 2.0; // branch width if posterior probabilities are not used
    boolean usePosterior = false; // use posterior probabilities to color branch
    boolean useHeights = true; // use heights (time) to color branches
    String startBranchColor = "FFFF33"; //red: 0000FF green: 00FF00 magenta: FF00FF white: FFFFFF yellow: 00FFFF
    String endBranchColor = "00FF00";
    String branchColor = "ffffff"; // branch color if color range based on rates is not used
    boolean arcBranches = true; // branches are arcs with heights proportional to the distance between locations
    boolean arcTimeHeight = true; // the height of the arcs is proportional to the time the branch spans, by default archeights are proportional to the distance between locations
    double altitudeFactor = 30; // this is the factor with which to multiply the time of the branch to get the altitude for that branch in the surface Tree

    double mostRecentDate;  // required to convert heights to calendar dates
    boolean ancient = false;

    double[] posteriorMinAndMax = new double[2];  // used to calibrate the color range for branches
    double[] heightMinAndMax = new double[2];   // used to calibrate the color range for branches


    //circles
    int numberOfIntervals = 100;
    double radius = 10000;

    //everything is written to separate buffers, and than collected in structured KML document by compileBuffer
    StringBuffer branchesBuffer = new StringBuffer();
    StringBuffer circleBuffer = new StringBuffer();
    StringBuffer locationsBuffer = new StringBuffer();
    StringBuffer styleBuffer = new StringBuffer();

    public DiscreteKML(){
    }

    public DiscreteKML(RootedTree tree, String name, double date, String[][] locations){
        treeToExport = tree;
        documentName = name;
        mostRecentDate = date;
        double[][] locationCoordinates = new double[locations.length][2];
        String[] locationNames = new String[locations.length];
        String[] locationStates = new String[locations.length];

        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double minLong = Double.MAX_VALUE;
        double maxLong = -Double.MAX_VALUE;

        for (int i = 0; i < locations.length; i++) {
            locationNames[i] = locations[i][(locations[0].length - 3)];  //in a three column file, the name is the first element, so length -3
            locationCoordinates[i][0] = Double.parseDouble(locations[i][(locations[0].length - 2)]);
            if (locationCoordinates[i][0] < minLat)  {
                minLat = locationCoordinates[i][0];
            }
            if (locationCoordinates[i][0] > maxLat)  {
                maxLat = locationCoordinates[i][0];
            }

            locationCoordinates[i][1] = Double.parseDouble(locations[i][(locations[0].length - 1)]);
            if (locationCoordinates[i][1] < minLong)  {
                minLong = locationCoordinates[i][1];
            }
            if (locationCoordinates[i][1] > maxLong)  {
                maxLong = locationCoordinates[i][1];
            }

            if (locations[0].length > 3) {
                locationStates[i] = locations[i][(locations[0].length - 4)];
            }

        }

        if (locations[0].length < 4) {
            int statesCounter = 0;
            for(int j = (int)('A'); j<locations.length+(int)('A'); j++) {
                locationStates[statesCounter] = String.valueOf(((char)j));
                statesCounter++;
            }
        }

        states = locationStates;

        radius = 100*Math.abs(maxLat-minLat)*Math.abs(maxLong-minLong);
        radius = 200000;

        stateCoordinates = locationCoordinates;
        stateNames = locationNames;

        if (mostRecentDate - treeToExport.getHeight(treeToExport.getRootNode()) < 0) {
            ancient = true;
        }
    }

    public void writeTreeToKML() {

        int nodeNumber = 0;

        posteriorMinAndMax[1] = 1;
        posteriorMinAndMax[0] = getPosteriorMin(treeToExport);
        heightMinAndMax[0] = 0;
        heightMinAndMax[1] = treeToExport.getHeight(treeToExport.getRootNode());

        for (Node node : treeToExport.getNodes()) {
            nodeNumber++;
            char state = ((String)node.getAttribute("state")).charAt(0);


            if (!treeToExport.isRoot(node)) {

                Node parentNode = treeToExport.getParent(node);
                char parentState = ((String)parentNode.getAttribute("state")).charAt(0);

                if (state != parentState) {

                    double stateProbability = (Double)node.getAttribute("state.prob");
                    double latitude = getCoordinate(state, states, stateCoordinates, 0);
                    double longitude = getCoordinate(state, states, stateCoordinates, 1);
                    double posteriorProb = 1;
                    if (!treeToExport.isExternal(node)) {
                        posteriorProb = (Double)node.getAttribute("posterior");
                    }

                    double parentStateProbability = (Double)parentNode.getAttribute("state.prob");
                    double parentLatitude = getCoordinate(parentState, states, stateCoordinates, 0);
                    double parentLongitude = getCoordinate(parentState, states, stateCoordinates, 1);

                    //System.out.println(latitude+"\t"+parentLatitude+"\t"+longitude+"\t"+parentLongitude);

                    // distance used for chopping up and altitude
                    double distance = (3958*Math.PI*Math.sqrt((parentLatitude-latitude)*(parentLatitude-latitude)+Math.cos(parentLatitude/57.29578)*Math.cos(latitude/57.29578)*(parentLongitude-longitude)*(parentLongitude-longitude))/180);
                    double maxAltitude;
                    if (arcTimeHeight) {
                        maxAltitude = (treeToExport.getHeight(parentNode) - treeToExport.getHeight(node))*altitudeFactor;
                    } else {
                        maxAltitude = distance*altitudeFactor;
                    }
                    double latitudeDifference = parentLatitude - latitude;
                    double longitudeDifference = parentLongitude - longitude;
                    boolean longitudeBreak = false; //if we go through the 180

                    // check if we have to go through the 180
                    if (longitude*parentLongitude < 0) {

                        double trialDistance = 0;

                        if (longitude < 0) {
                            trialDistance += (longitude + 180);
                            trialDistance += (180 - parentLongitude);
                            //System.out.println(parentLongitude+"\t"+longitude+"\t"+trialDistance+"\t"+longitudeDifference);
                        } else {
                            trialDistance += (parentLongitude + 180);
                            trialDistance += (180 - longitude);
                            //System.out.println(parentLongitude+"\t"+longitude+"\t"+trialDistance+"\t"+longitudeDifference);
                         }

                        if (trialDistance < Math.abs(longitudeDifference)) {
                            longitudeDifference = trialDistance;
                            longitudeBreak = true;
                        }
                    }

                    branchesBuffer.append("\t<Folder>\r");

                    branchesBuffer.append("\t\t\t<name>branch"+ nodeNumber +"</name>\r");
                    //System.out.println("branch "+nodeNumber+"\t+state+"+"\t"+latitude+"\t"+longitude+"\t"+parentLatitude+"\t"+parentLongitude);

                    //divider dependent on distance
                    divider = (2*(int)(distance/25));

                    double currentLongitude1 = 0;
                    double currentLongitude2 = 0;
                    for (int a = 0; a < divider; a ++) {

                        branchesBuffer.append("\t\t<Placemark>\r");

                        branchesBuffer.append("\t\t\t<name>branch"+ nodeNumber +"_part"+(a+1)+"</name>\r");

                        branchesBuffer.append("\t\t\t<TimeSpan>\r");
                        //convert height of the branch segment to a real date (based on th date for the most recent sample)
                        double date = mostRecentDate - (treeToExport.getHeight(node) + (a + 1) *
                            ((treeToExport.getHeight(parentNode) - (treeToExport.getHeight(node)))/divider));
                        String[] yearMonthDay = convertToYearMonthDay(date);
                        //System.out.println(yearMonthDay[0]+"\t"+date+"\t"+mostRecentDate+"\t"+(treeToExport.getHeight(node) + (a + 1) * ((treeToExport.getHeight(parentNode) - (treeToExport.getHeight(node)))/divider))+"\t"+date);
                        if (ancient) {
                            branchesBuffer.append("\t\t\t\t<begin>"+date+"</begin>\r");
                        } else {
                            branchesBuffer.append("\t\t\t\t<begin>"+yearMonthDay[0]+"-"+yearMonthDay[1]+"-"+yearMonthDay[2]+"</begin>\r");
                        }

                        branchesBuffer.append("\t\t\t</TimeSpan>\r");

                        branchesBuffer.append("\t\t\t<styleUrl>#branch"+ nodeNumber +"_part"+(a+1)+"_style</styleUrl>\r");
                        // branchesBuffer.append("\t\t\t<styleUrl>#surfaceTreeBranch"+nodeNumber+"_style</styleUrl>\r");
                        branchesBuffer.append("\t\t\t<LineString>\r");

                        if (arcBranches) {
                            branchesBuffer.append("\t\t\t\t<altitudeMode>absolute</altitudeMode>\r");
                            branchesBuffer.append("\t\t\t\t<tessellate>1</tessellate>\r");
                        } else {
                            branchesBuffer.append("\t\t\t\t<altitudeMode>clampToGround</altitudeMode>\r");
                        }

                        branchesBuffer.append("\t\t\t\t<coordinates>\r");

                        if (longitudeBreak) {

                            if (longitude > 0) {
                                currentLongitude1 = longitude+a*(longitudeDifference/divider);

                                if (currentLongitude1 < 180) {
                                   branchesBuffer.append("\t\t\t\t\t"+currentLongitude1+",");
                                   //System.out.println("1 currentLongitude1 < 180\t"+currentLongitude1+"\t"+longitude);

                                } else {
                                   branchesBuffer.append("\t\t\t\t\t"+(-180-(180-currentLongitude1))+",");
                                   //System.out.println("2 currentLongitude1 > 180\t"+currentLongitude1+"\t"+(-180-(180-currentLongitude1))+"\t"+longitude);
                                }
                            } else {
                                currentLongitude1 = longitude-a*(longitudeDifference/divider);

                                if (currentLongitude1 > (-180)) {
                                    branchesBuffer.append("\t\t\t\t\t"+currentLongitude1+",");
                                    //System.out.println("currentLongitude1 > -180\t"+currentLongitude1+"\t"+longitude);
                                 } else {
                                    branchesBuffer.append("\t\t\t\t\t"+(180+(currentLongitude1+180))+",");
                                    //System.out.println("currentLongitude1 > -180\t"+(180+(currentLongitude1+180))+"\t"+longitude);
                                 }
                            }

                        } else {
                            branchesBuffer.append("\t\t\t\t\t"+(longitude+a*(longitudeDifference/divider))+",");
                        }
                        branchesBuffer.append((latitude+a*(latitudeDifference/divider))+",");
                        branchesBuffer.append((maxAltitude*Math.sin(Math.acos(1 - a*(1.0/(divider/2.0)))))+"\r");

                        if (longitudeBreak) {

                            if (longitude > 0) {
                                currentLongitude2 = longitude+(a+1)*(longitudeDifference/divider);

                                if (currentLongitude2 < 180) {
                                   branchesBuffer.append("\t\t\t\t\t"+(currentLongitude2)+",");
                                } else {
                                   branchesBuffer.append("\t\t\t\t\t"+(-180-(180-currentLongitude2))+",");
                                }
                            } else {
                                currentLongitude2 = longitude-(a+1)*(longitudeDifference/divider);

                                if (currentLongitude2 > (-180)) {
                                    branchesBuffer.append("\t\t\t\t\t"+currentLongitude2+",");
                                 } else {
                                    branchesBuffer.append("\t\t\t\t\t"+(180+(currentLongitude2+180))+",");
                                 }
                            }

                        } else {
                            branchesBuffer.append("\t\t\t\t\t"+(longitude+(a+1)*(longitudeDifference/divider))+",");
                        }
                        branchesBuffer.append((latitude+(a+1)*(latitudeDifference/divider))+",");
                        branchesBuffer.append((maxAltitude*Math.sin(Math.acos(1 - (a+1)*(1.0/(divider/2.0)))))+"\r");

                        branchesBuffer.append("\t\t\t\t</coordinates>\r");

                        branchesBuffer.append("\t\t\t</LineString>\r");
                        branchesBuffer.append("\t\t</Placemark>\r");

                        styleBuffer.append("\t<Style id=\"branch"+ nodeNumber +"_part"+(a+1)+"_style\">\r");
                        styleBuffer.append("\t\t<BranchStyle>\r");
                        if (useStateProbability) {
                            double stateprobabilityDifference = (stateProbability - parentStateProbability)/divider;
                            styleBuffer.append("\t\t\t<width>"+(branchWidthConstant+(parentStateProbability+((a + 1)*stateprobabilityDifference))*branchWidthMultiplier)+"</width>\r");
                        } else {
                            styleBuffer.append("\t\t\t<width>"+branchWidth+"</width>\r");
                        }
                        if (useHeights){
                            styleBuffer.append("\t\t\t<color>"+"FF"+ ContinuousKML.getKMLColor((treeToExport.getHeight(node) + (a + 1) *
                                    ((treeToExport.getHeight(parentNode) - (treeToExport.getHeight(node)))/divider)),
                                    heightMinAndMax, startBranchColor, endBranchColor)+"</color>\r");
                        } else if (usePosterior){
                            styleBuffer.append("\t\t\t<color>"+"FF"+ ContinuousKML.getKMLColor(posteriorProb,
                                    posteriorMinAndMax, startBranchColor, endBranchColor)+"</color>\r");
                        } else {
                            styleBuffer.append("\t\t\t<color>"+"FF"+branchColor+"</color>\r");
                        }
                        styleBuffer.append("\t\t</BranchStyle>\r");
                        styleBuffer.append("\t</Style>\r");

                    }
                    branchesBuffer.append("\t</Folder>\r");
                }

            }
        }
    }

    public void writeLocationsKML() {

        for (int i = 0; i < stateNames.length; i++) {

            locationsBuffer.append("\t\t\t<Placemark>\r");
            locationsBuffer.append("\t\t\t\t<name>"+stateNames[i]+"</name>\r");

            locationsBuffer.append("\t\t\t\t<Point>\r");
            locationsBuffer.append("\t\t\t\t\t<altitudeMode>relativeToGround</altitudeMode>\r");
            locationsBuffer.append("\t\t\t\t\t<coordinates>"+stateCoordinates[i][1]+","+stateCoordinates[i][0]+","+"0</coordinates>\r");
            locationsBuffer.append("\t\t\t\t</Point>\r");

            locationsBuffer.append("\t\t\t</Placemark>\r");

        }
    }

    public void writeLineagesToCircles() {

        char rootState = 0;

        double rootHeight = (treeToExport.getHeight(treeToExport.getRootNode()));
        double delta = rootHeight/numberOfIntervals;

        double[][] numberOflineages = new double[(numberOfIntervals - 1)][stateNames.length + 1];

        for (int i = 0; i < (numberOfIntervals - 1); i ++ ) {

            numberOflineages[i][0] = rootHeight-((i+1)*delta);

        }

        for (int j = 0; j < (numberOfIntervals - 1); j ++ ) {
            for (int k = 0; k < (stateNames.length); k ++ ) {

                int numberOfLineagesOfState = 0;

                int nodeNumber = 0;
                for (Node node : treeToExport.getNodes()) {
                    nodeNumber ++;

                    if (!treeToExport.isRoot(node)) {

                         Node parentNode = treeToExport.getParent(node);
                         char state = ((String)node.getAttribute("state")).charAt(0);
                         char parentState = ((String)parentNode.getAttribute("state")).charAt(0);


                         if ((treeToExport.getHeight(node) <= numberOflineages[j][0]) && (treeToExport.getHeight(parentNode) > numberOflineages[j][0])) {

                            if ((state == parentState) && (String.valueOf(parentState).equals(states[k]))) {

                                numberOfLineagesOfState ++;

                            }

                         }


                    } else {
                        rootState = ((String)node.getAttribute("state")).charAt(0);
                    }
                }

                numberOflineages[j][k+1] = numberOfLineagesOfState;
            }
        }

        //print2DArray(numberOflineages, "numberOflineages.txt");
        // write the circle for the root
        double rootLat = 0;
        double rootLong = 0;

        for (int a = 0; a < stateCoordinates.length; a++ ) {
            if (String.valueOf(rootState).equals(states[a])) {
                rootLat =  stateCoordinates[a][0];
                rootLong = stateCoordinates[a][1];
            }
        }
        writeCircle(rootLat, rootLong, 36, radius, rootState, rootHeight, (rootHeight-delta), circleBuffer);
        styleBuffer.append("\t<Style id=\"circle_"+rootHeight+"_style\">\r");
        styleBuffer.append("\t\t<BranchStyle>\r\t\t\t<width>0.1</width>\r\t\t</BranchStyle>\r");
        styleBuffer.append("\t\t<PolyStyle>\r");
        styleBuffer.append("\t\t\t<color>"+"7F"+ ContinuousKML.getKMLColor(rootHeight,
                heightMinAndMax, startBranchColor, endBranchColor)+"</color>\r");
        styleBuffer.append("\t\t\t<outline>0</outline>\r");
        styleBuffer.append("\t\t</PolyStyle>\r");
        styleBuffer.append("\t</Style>\r");

        //write all other circles
        for (int o = 0; o < (numberOfIntervals - 1); o ++ ) {
            for (int p = 0; p < (stateNames.length); p ++ ) {

                if (numberOflineages[o][p+1] > 0) {
                    writeCircle(stateCoordinates[p][0], stateCoordinates[p][1], 36, radius*Math.sqrt(numberOflineages[o][p+1]), states[p].charAt(0), numberOflineages[o][0], (numberOflineages[o][0]-delta), circleBuffer);

                }
            }
            styleBuffer.append("\t<Style id=\"circle_"+numberOflineages[o][0]+"_style\">\r");
            styleBuffer.append("\t\t<BranchStyle>\r\t\t\t<width>0.1</width>\r\t\t</BranchStyle>\r");
            styleBuffer.append("\t\t<PolyStyle>\r");
            styleBuffer.append("\t\t\t<color>"+"7F"+ ContinuousKML.getKMLColor(numberOflineages[o][0],
                    heightMinAndMax, startBranchColor, endBranchColor)+"</color>\r");
            styleBuffer.append("\t\t\t<outline>0</outline>\r");
            styleBuffer.append("\t\t</PolyStyle>\r");
            styleBuffer.append("\t</Style>\r");

        }

    }

    public void compileBuffer(StringBuffer buffer) {

        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r");
        buffer.append("<kml xmlns=\"http://earth.google.com/kml/2.2\">\r");

        buffer.append("<Document>\r");

        buffer.append("\t<name>"+documentName+"</name>\r");
        buffer.append(styleBuffer);

        buffer.append("\t<Folder>\r");
        buffer.append("\t<name>branches</name>\r");
        buffer.append("\t<description>branches on which the state changes</description>\r");
        buffer.append(branchesBuffer);
        buffer.append("\t</Folder>\r");

        buffer.append("\t<Folder>\r");
        buffer.append("\t\t<name>Locations</name>\r");
        buffer.append("\t\t<description>Location states</description>\r");
        buffer.append(locationsBuffer);
        buffer.append("\t</Folder>\r");

        buffer.append("\t<Folder>\r");
        buffer.append("\t\t<name>circles</name>\r");
        buffer.append("\t\t<description>circles representing the number of lineages maintaining that state at any moment</description>\r");
        buffer.append(circleBuffer);
        buffer.append("\t</Folder>\r");

        buffer.append("</Document>\r");

        buffer.append("</kml>");

    }

    private static String[] convertToYearMonthDay(double fractionalDate) {

        String[] yearMonthDay = new String[3];

        int year = (int) fractionalDate;
        String yearString;

        if (year < 0) {
           yearMonthDay[0] = ""+year;
           yearMonthDay[1] = ""+0;
           yearMonthDay[2] = ""+0;
           return yearMonthDay;
        }

        else if (year < 10) {
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

    private static double getCoordinate(char state, String[] states, double[][] stateCoordinates, int latlong) {

        double coordinate = 0;

        for(int i = 0; i<states.length; i++) {
        	if (states[i].charAt(0) == state) {
                coordinate = stateCoordinates[i][latlong];
            }
        }

        return coordinate;
    }

    private static double getCoordinate1(char state, double[][] stateCoordinates, int latlong) {

        double coordinate = 0;

        for(int i = (int)('A'); i<stateCoordinates.length+(int)('A'); i++) {
        	//System.out.print(i+"\t"+(char)i+", ");
        	if ((char)i == state) {
                coordinate = stateCoordinates[i-(int)('A')][latlong];
            }
        }
        return coordinate;
    }

    private static double getPosteriorMin(RootedTree tree) {

        double min = 1;

        for (Node node : tree.getNodes()) {
            if (!(tree.isRoot(node) || tree.isExternal(node))) {
                if ((Double)node.getAttribute("posterior") < min) {
                    min = (Double)node.getAttribute("posterior");
                }
             }
        }

        return min;

    }
    //http://bbs.keyhole.com/ubb/showflat.php/Cat/0/Number/23634/page/vc/fpart/all/vc/1
    private void writeCircle(double centerLat, double centerLong, int numberOfPoints, double radius, char centerState, double beginDate, double endDate, StringBuffer buffer) {

        double lat1, long1, lat2, long2;
        double dlat, dlong, d_rad;
        double a, c, d;
        double delta_pts;
        double radial, lat_rad, dlon_rad, lon_rad;

        double degreeToRadian = Math.PI/180.0;

        String[] begin = convertToYearMonthDay(mostRecentDate - beginDate);
        String[] end = convertToYearMonthDay(mostRecentDate - endDate);

        // convert coordinates to radians
        lat1 = Math.toRadians(centerLat);
        long1 = Math.toRadians(centerLong);

        //Earth measures
        //Year Name	 a (meters) b (meters) 1/f Where Used
        //1980 International 6,378,137 6,356,752 298.257 Worldwide
        d = radius;
        d_rad = d/6378137;

        buffer.append("\t\t\t<Placemark>\r" +
                        "\t\t\t\t<name>"+centerState+"_"+beginDate+"_"+endDate+"</name>\r" +
               //         "<Style>\r" +
               //         "<geomColor>$geomColor1$geomColor2</geomColor>\r" +
               //         "<geomScale>$geomScale</geomScale></Style>\r" +
                        "\t\t\t\t<TimeSpan>\r");
                         if (ancient) {
                             buffer.append("\t\t\t\t\t<begin>"+(mostRecentDate - beginDate)+"</begin>\r");
                         } else {
                             buffer.append("\t\t\t\t\t<begin>"+begin[0]+"-"+begin[1]+"-"+begin[2]+"</begin>\r");
                         }
                         if (ancient) {
                             buffer.append("\t\t\t\t\t<end>"+(mostRecentDate - endDate)+"</end>\r");
                         } else {
                             buffer.append("\t\t\t\t\t<end>"+end[0]+"-"+end[1]+"-"+end[2]+"</end>\r");
                         }
        buffer.append("\t\t\t\t</TimeSpan>\r" +
                        "\t\t\t\t<styleUrl>#circle_"+beginDate+"_style</styleUrl>\r" +
                        "\t\t\t\t<Polygon>\r" +
                        "\t\t\t\t\t<altitudeMode>clampToGround</altitudeMode>\r" +
                        "\t\t\t\t\t<tessellate>1</tessellate>\r" +
                        "\t\t\t\t\t<outerBoundaryIs>\r" +
                        "\t\t\t\t\t\t<LinearRing>\r" +
                        "\t\t\t\t\t\t\t<coordinates>\r");

        // loop through the array and write path linestrings

        for(int i=0; i<=numberOfPoints; i++) {
            delta_pts = 360/(double)numberOfPoints;
            radial = Math.toRadians((double)i*delta_pts);

            //This algorithm is limited to distances such that dlon <pi/2
            lat_rad = Math.asin(Math.sin(lat1)* Math.cos(d_rad) + Math.cos(lat1)* Math.sin(d_rad)* Math.cos(radial));
            dlon_rad = Math.atan2(Math.sin(radial)* Math.sin(d_rad)* Math.cos(lat1), Math.cos(d_rad)- Math.sin(lat1)* Math.sin(lat_rad));
            lon_rad = ((long1 + dlon_rad + Math.PI) % (2*Math.PI)) - Math.PI;

            buffer.append("\t\t\t\t\t\t\t\t");
            buffer.append(Math.toDegrees(lon_rad) + ", ");
            buffer.append(Math.toDegrees(lat_rad) + ", 0");
            buffer.append('\r');

        }
        buffer.append("\t\t\t\t\t\t\t</coordinates>\r" +
                        "\t\t\t\t\t\t</LinearRing>\r" +
                        "\t\t\t\t\t</outerBoundaryIs>\r" +
                        "\t\t\t\t</Polygon>\r" +
                        "\t\t\t</Placemark>\r");


    }
    private static void print2DArray(double[][] array, String name) {
        try {
            PrintWriter outFile = new PrintWriter(new FileWriter(name), true);

            for (int i = 0; i < array.length; i++) {
                for (int j = 0; j < array[0].length; j++) {
                    outFile.print(array[i][j]+"\t");
                }
            outFile.println("");
            }
            outFile.close();

        } catch(IOException io) {
           System.err.print("Error writing to file: " + name);
        }
    }

}
