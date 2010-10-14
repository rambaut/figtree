package phylogeochronogrammer;

import jebl.evolution.trees.RootedTree;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: phil
 * Date: May 16, 2008
 * Time: 11:30:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class DiscreteKMLString {

    public static final String STATE = "state";

    //input related variables
    RootedTree treeToExport;
    String documentName;
    String[] stateNames;
    double[][] stateCoordinates;
    double timeScaler = 1;

    double branchWidthConstant = 2.0;   // the width of branches will be stateProbability*branchWidthMultiplier+branchWidthConstant
    double branchWidthMultiplier = 5.0;

    double divider = 50; // this is to chop up the branches of the surface tree in 'divider' segments
    //divider = (2*(int)(distance/25)); --> this is further down overriding the usual divider
    boolean useStateProbability = true; // use state probabilities for branch width
    double branchWidth = 10.0; // branch width if posterior probabilities are not used
    boolean usePosterior = false; // use posterior probabilities to color branch
    boolean useHeights = true; // use heights (time) to color branches
    String startBranchColor = "FF00FF"; //red: 0000FF green: 00FF00 magenta: FF00FF white: FFFFFF yellow: 00FFFF cyan: FFFF00
    String endBranchColor = "FFFF00";
    String branchColor = "ffffff"; // branch color if color range based on rates is not used
    boolean arcBranches = false; // branches are arcs with heights proportional to the distance between locations
    boolean arcTimeHeight = false; // the height of the arcs is proportional to the time the branch spans, by default archeights are proportional to the distance between locations
    double altitudeFactor = 1000; // this is the factor with which to multiply the time of the branch to get the altitude for that branch in the surface Tree
    boolean temporary = false;

    double mostRecentDate;  // required to convert heights to calendar dates
    boolean ancient = false;

    double[] posteriorMinAndMax = new double[2];  // used to calibrate the color range for branches
    double[] heightMinAndMax = new double[2];   // used to calibrate the color range for branches

    //circles
    int numberOfIntervals = 100;
    boolean autoRadius = false;
    double radius = 40000;
    String circleOpacity = "8F";

    //some taxa may have different coordinates than its state
    boolean coordinatesForTaxa = false;
    String[] taxaNames;
    double[][] taxaCoordinates;

    //treeSlices
    //String treeSliceBranchColor = "FFFF00"; //red: 0000FF green: 00FF00 magenta: FF00FF white: FFFFFF yellow: 00FFFF cyan: FFFF00
    double treeSliceBranchWidth = 3.5;
    boolean showBranchAtMidPoint = false; // shows complete branch for slice if time is more recent than the branch's midpoint

    //everything is written to separate buffers, and than collected in structured KML document by compileBuffer
    StringBuffer branchesBuffer = new StringBuffer();
    StringBuffer circleBuffer = new StringBuffer();
    StringBuffer locationsBuffer = new StringBuffer();
    StringBuffer styleBuffer = new StringBuffer();
    StringBuffer treeSliceBuffer = new StringBuffer();

    private static final PrintStream progressStream = System.out;

    public DiscreteKMLString(){
    }

    public DiscreteKMLString(RootedTree tree, String name, double date, String[][] locations){
        treeToExport = tree;
        documentName = name;
        mostRecentDate = date;
        double[][] locationCoordinates = new double[locations.length][2];
        String[] locationNames = new String[locations.length];

        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double minLong = Double.MAX_VALUE;
        double maxLong = -Double.MAX_VALUE;

        getTaxaNamesCoordinates(locations, locationNames,locationCoordinates);
//        overides default radius
//        if (autoRadius) {
//            radius = 100*Math.abs(maxLat-minLat)*Math.abs(maxLong-minLong);
//       }

        stateCoordinates = locationCoordinates;
        stateNames = locationNames;

        if (mostRecentDate - (treeToExport.getHeight(treeToExport.getRootNode())*timeScaler) < 0) {
            ancient = true;
        }
    }

    public DiscreteKMLString(RootedTree tree, String name, double date, String[][] stateLocations, String startBranchColor, String endBranchColor, double timeScaler){
        treeToExport = tree;
        documentName = name;
        mostRecentDate = date;
        double[][] stateLocationCoordinates = new double[stateLocations.length][2];
        String[] stateLocationNames = new String[stateLocations.length];
        //extra arrays for taxa coordinates

        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double minLong = Double.MAX_VALUE;
        double maxLong = -Double.MAX_VALUE;

        getTaxaNamesCoordinates(stateLocations, stateLocationNames,stateLocationCoordinates);
//        get taxa coordinates and names, and turn the boolean to true
        coordinatesForTaxa = false;

        //overides default radius
        if (autoRadius) {
            radius = 100*Math.abs(maxLat-minLat)*Math.abs(maxLong-minLong);
        }

        stateCoordinates = stateLocationCoordinates;
        stateNames = stateLocationNames;
        this.startBranchColor = startBranchColor;
        this.endBranchColor = endBranchColor;
        this.timeScaler = timeScaler;

        if (mostRecentDate - (treeToExport.getHeight(treeToExport.getRootNode())*timeScaler) < 0) {
            ancient = true;
        }
    }

    public DiscreteKMLString(RootedTree tree, String name, double date, String[][] stateLocations, String startBranchColor, String endBranchColor, double timeScaler, String[][] taxaLocations){
        treeToExport = tree;
        documentName = name;
        mostRecentDate = date;
        double[][] stateLocationCoordinates = new double[stateLocations.length][2];
        String[] stateLocationNames = new String[stateLocations.length];
        //extra arrays for taxa coordinates
        double[][] taxaLocationCoordinates = new double[taxaLocations.length][2];
        String[] taxaLocationNames = new String[taxaLocations.length];

        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double minLong = Double.MAX_VALUE;
        double maxLong = -Double.MAX_VALUE;

        //get taxa coordinates and names, and turn the boolean to true
        getTaxaNamesCoordinates(taxaLocations, taxaLocationNames,taxaLocationCoordinates);
        coordinatesForTaxa = true;

        //overides default radius
//        if (autoRadius) {
//            radius = 100*Math.abs(maxLat-minLat)*Math.abs(maxLong-minLong);
//        }

        stateCoordinates = stateLocationCoordinates;
        stateNames = stateLocationNames;
        taxaCoordinates = taxaLocationCoordinates;
        taxaNames = taxaLocationNames;
        this.startBranchColor = startBranchColor;
        this.endBranchColor = endBranchColor;
        this.timeScaler = timeScaler;
        //System.out.println(timeScaler);
        //System.out.println(startBranchColor);
        //System.out.println(endBranchColor);

        if (mostRecentDate - (treeToExport.getHeight(treeToExport.getRootNode())*timeScaler) < 0) {
            ancient = true;
        }
    }

    public DiscreteKMLString(RootedTree tree, String[][] stateLocations, String name, double date, double timeScaler, double divider, double branchWidthConstant, double branchWidthMultiplier, boolean useStateProbability, double branchWidth, String startBranchColor, String endBranchColor, String branchColor, boolean useHeights, boolean usePosterior, boolean arcBranches, boolean arcTimeHeight, double altitudeFactor, boolean temporary, int numberOfIntervals, double radius, String circleOpacity, boolean coordinatesForTaxa, String[][] taxaCoordinates, boolean makeTreeSlices) {
        treeToExport = tree;
        documentName = name;
        mostRecentDate = date;
        double[][] stateLocationCoordinates = new double[stateLocations.length][2];
        String[] stateLocationNames = new String[stateLocations.length];
        //extra arrays for taxa coordinates
        double[][] taxaLocationCoordinates = null;
        String[] taxaLocationNames = null;
        if (coordinatesForTaxa) {
            taxaLocationCoordinates = new double[taxaCoordinates.length][2];
            taxaLocationNames = new String[taxaCoordinates.length];
        }

        double[] minMaxLatLong = getMinMaxLatLong(stateLocations, stateLocationNames,stateLocationCoordinates);
        getTaxaNamesCoordinates(stateLocations, stateLocationNames,stateLocationCoordinates);
        //get taxa coordinates and names, and turn the boolean to true
        if (coordinatesForTaxa) {
            getTaxaNamesCoordinates(taxaCoordinates, taxaLocationNames,taxaLocationCoordinates);
        }

        stateCoordinates = stateLocationCoordinates;
        stateNames = stateLocationNames;
        this.taxaCoordinates = taxaLocationCoordinates;
        taxaNames = taxaLocationNames;
        this.startBranchColor = startBranchColor;
        this.endBranchColor = endBranchColor;
        this.timeScaler = timeScaler;

        if (mostRecentDate - (treeToExport.getHeight(treeToExport.getRootNode())*timeScaler) < 0) {
            ancient = true;
        }

        this.divider = divider;
        this.branchWidthConstant = branchWidthConstant;
        this.branchWidthMultiplier = branchWidthMultiplier;
        this.branchWidth = branchWidth;
        this.useStateProbability = useStateProbability;
        this.startBranchColor = startBranchColor;
        this.endBranchColor = endBranchColor;
        this.branchColor = branchColor;
        this.useHeights = useHeights;
        this.usePosterior = usePosterior;
        this.arcBranches = arcBranches;
        this.arcTimeHeight = arcTimeHeight;
        this.altitudeFactor = altitudeFactor;
        this.temporary = temporary;
        this.numberOfIntervals = numberOfIntervals;
        if (radius <= 0) {
            this.radius = radius = 10*Math.abs(minMaxLatLong[1]-minMaxLatLong[0])*Math.abs(minMaxLatLong[3]-minMaxLatLong[2]);
        } else {
            this.radius = radius;
        }
        this.circleOpacity = circleOpacity;

        //System.out.println(useHeights+"\t"+startBranchColor+"\t"+endBranchColor);

        if (!makeTreeSlices) {
            progressStream.println("\rtime attributes:");
            String[] yearMonthDay = convertToYearMonthDay(mostRecentDate);

            progressStream.println("\tmost recent sampling date = "+mostRecentDate+" ("+yearMonthDay[0]+"-"+yearMonthDay[1]+"-"+yearMonthDay[2]+"), timeScaler = "+timeScaler);
            progressStream.println("\rbranch attributes:" );
            progressStream.println("\tbranch segments = "+this.divider);
            if (useStateProbability) {
                progressStream.println("\tbranch width = branchWidthConstant("+this.branchWidthConstant+") + branchWithMultiplier("+this.branchWidthMultiplier+") +  * state probability");
            }  else {
                progressStream.println("\tbranch width = "+this.branchWidth);
            }
            if ( this.useHeights || this.usePosterior) {
                if (this.startBranchColor.equals(this.endBranchColor)){
                    progressStream.println("\tbranch color = "+this.startBranchColor);
                }
                if (this.useHeights) {
                    progressStream.println("\tusing heights for color");
                } else if (this.usePosterior) {
                    progressStream.println("\tusing posteriors for color");
                }
                progressStream.println("\t\tbranch color range = "+this.startBranchColor+" - "+this.endBranchColor);
            }   else {
                progressStream.println("\tbranch color = "+this.branchColor);
            }
            if (this.arcBranches) {
                progressStream.println("\tusing branch archs");
                if (this.arcTimeHeight){
                    progressStream.println("\t\tarch height proportional to the time elapsed the branch");
                } else {
                    progressStream.println("\t\tarch height proportional to the distance represented by the state change along the branch");

                }
                progressStream.println("\t\t arch height mulitplier = "+altitudeFactor);
            }
            if (this.temporary) {
                progressStream.println("\tbranches shown temporarily");
            }
            progressStream.println("\rlocation circle attributes:" );
            progressStream.println("\tcircle radius = "+ this.radius);
            progressStream.println("\tcircle opacity = "+ this.circleOpacity);
            progressStream.println("\tnumber of segments to represent the circle = "+ this.numberOfIntervals);
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
            String state = ((((String)node.getAttribute(STATE)).replaceAll("\"","")).replaceAll(" ","")).trim();

            //in case the location state is a concatenation of other states (occurs when they get the same posterior prob)
            if (state.contains("+")) {
                state = state.substring(0,state.indexOf("+"));
            }


            if (!treeToExport.isRoot(node)) {

                Node parentNode = treeToExport.getParent(node);

                // test to see node has the attribute
                Object testAttribute = parentNode.getAttribute(STATE);
                if (testAttribute == null) {
                    System.err.print("An internal node has no state attribute; make sure to set the posterior probability limit to 0 when annotating an MCC tree in TreeAnnotator!");        
                }

                String parentState = ((((String)parentNode.getAttribute(STATE)).replaceAll("\"","")).replaceAll(" ","")).trim();

                if (parentState.contains("+")) {
                    parentState = parentState.substring(0,parentState.indexOf('+'));
                }

                boolean considerTaxonCoordinateForThisNode = false;
                if (coordinatesForTaxa) {
                    if (treeToExport.isExternal(node)) {
                        if (taxonHasSeparateCoordinate(treeToExport.getTaxon(node),taxaNames)){
                            considerTaxonCoordinateForThisNode = true;
                        }
                    }
                }

                if (!(state.toLowerCase()).equals(parentState.toLowerCase()) || considerTaxonCoordinateForThisNode) {

                    double stateProbability = (Double)node.getAttribute("state.prob");
                    double latitude = getCoordinate(state, stateNames, stateCoordinates, 0);
                    double longitude = getCoordinate(state, stateNames, stateCoordinates, 1);
                    double posteriorProb = 1;
                    if (!treeToExport.isExternal(node)) {
                        posteriorProb = (Double)node.getAttribute("posterior");
                    } else {
                        if (considerTaxonCoordinateForThisNode) {
                            latitude  = getTaxaCoordinate(treeToExport.getTaxon(node), taxaNames, taxaCoordinates, 0);
                            longitude  = getTaxaCoordinate(treeToExport.getTaxon(node), taxaNames, taxaCoordinates, 1);
                        }
                    }

                    if ((latitude == 0) && (longitude == 0)) {
                        System.err.println(state+" has no coordinate??");
                    }

                    double parentStateProbability = (Double)parentNode.getAttribute("state.prob");
                    double parentLatitude = getCoordinate(parentState, stateNames, stateCoordinates, 0);
                    double parentLongitude = getCoordinate(parentState, stateNames, stateCoordinates, 1);

                    //System.out.println(latitude+"\t"+parentLatitude+"\t"+longitude+"\t"+parentLongitude);

                    // distance used for chopping up and altitude
                    double distance = (3958*Math.PI*Math.sqrt((parentLatitude-latitude)*(parentLatitude-latitude)+Math.cos(parentLatitude/57.29578)*Math.cos(latitude/57.29578)*(parentLongitude-longitude)*(parentLongitude-longitude))/180);
                    double maxAltitude;
                    if (arcTimeHeight) {
                        maxAltitude = (treeToExport.getHeight(parentNode) - treeToExport.getHeight(node))*altitudeFactor;
                    } else {
                        maxAltitude = distance*altitudeFactor;
                    }
                    // check if we have to go through the 180
                    boolean longitudeBreak = longitudeBreak(longitude,parentLongitude);
                    double latitudeDifference = parentLatitude - latitude;
                    double longitudeDifference;
                    if (!longitudeBreak) {
                        longitudeDifference = parentLongitude - longitude;
                    } else {
                        if (parentLongitude < 0){
                            longitudeDifference = (180+parentLongitude) + (180-longitude);
                        } else {
                            longitudeDifference = (180-parentLongitude) + (180+longitude);
                        }                       
                    }

                    branchesBuffer.append("\t<Folder>\r");

                    branchesBuffer.append("\t\t\t<name>branch"+ nodeNumber +"</name>\r");
                    //System.out.println("branch "+nodeNumber+"\t+state+"+"\t"+latitude+"\t"+longitude+"\t"+parentLatitude+"\t"+parentLongitude);

                    //divider dependent on distance
                    //divider = (2*(int)(distance/25));

                    double currentLongitude1 = 0;
                    double currentLongitude2 = 0;
                    for (int a = 0; a < divider; a ++) {

                        branchesBuffer.append("\t\t<Placemark>\r");

                        branchesBuffer.append("\t\t\t<name>branch"+ nodeNumber +"_part"+(a+1)+"</name>\r");

                        branchesBuffer.append("\t\t\t<TimeSpan>\r");
                        //convert height of the branch segment to a real date (based on th date for the most recent sample)
                        double date = mostRecentDate - ((treeToExport.getHeight(node)*timeScaler) + (a + 1) *
                            (((treeToExport.getHeight(parentNode)*timeScaler) - ((treeToExport.getHeight(node))*timeScaler))/divider));
//used to make branches dissapear over time
                        double endDate = mostRecentDate - ((treeToExport.getHeight(node)*timeScaler) - (divider-(a + 1)) *
                            (((treeToExport.getHeight(parentNode)*timeScaler) - ((treeToExport.getHeight(node))*timeScaler))/divider));
                        if (endDate > mostRecentDate) {
                            endDate = mostRecentDate;
                        }
                        String[] yearMonthDay = convertToYearMonthDay(date);
                        String[] endYearMonthDay = convertToYearMonthDay(endDate);
                        //System.out.println(yearMonthDay[0]+"\t"+date+"\t"+mostRecentDate+"\t"+(treeToExport.getHeight(node) + (a + 1) * ((treeToExport.getHeight(parentNode) - (treeToExport.getHeight(node)))/divider))+"\t"+date);
                        if (ancient) {
                            branchesBuffer.append("\t\t\t\t<begin>"+Math.round(date)+"</begin>\r");
                        } else {
                            branchesBuffer.append("\t\t\t\t<begin>"+yearMonthDay[0]+"-"+yearMonthDay[1]+"-"+yearMonthDay[2]+"</begin>\r");
                        }

                        if (temporary) {
                            if (ancient) {
                                branchesBuffer.append("\t\t\t\t<end>"+Math.round(endDate)+"</end>\r");
                            } else {
                                branchesBuffer.append("\t\t\t\t<end>"+endYearMonthDay[0]+"-"+endYearMonthDay[1]+"-"+endYearMonthDay[2]+"</end>\r");
                            }
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
                        styleBuffer.append("\t\t<LineStyle>\r");
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
                        styleBuffer.append("\t\t</LineStyle>\r");
                        styleBuffer.append("\t</Style>\r");

                    }
                    branchesBuffer.append("\t</Folder>\r");
                }

            }
        }
    }

    public void writeTreeToKML(double time, double treeSliceBranchWidth, boolean showBranchAtMidPoint) {

        treeSliceBuffer.append("\t<Folder>\r");
        treeSliceBuffer.append("\t\t\t<name>tree"+ time +"</name>\r");

        int nodeNumber = 0;

        posteriorMinAndMax[1] = 1;
        posteriorMinAndMax[0] = getPosteriorMin(treeToExport);
        heightMinAndMax[0] = 0;
        heightMinAndMax[1] = treeToExport.getHeight(treeToExport.getRootNode());

        for (Node node : treeToExport.getNodes()) {
            nodeNumber++;
            String state = ((((String)node.getAttribute(STATE)).replaceAll("\"","")).replaceAll(" ","")).trim();

            //in case the location state is a concatenation of other states (occurs when they get the same posterior prob)
            if (state.contains("+")) {
                state = state.substring(0,state.indexOf("+"));
            }


            if (!treeToExport.isRoot(node)) {

                Node parentNode = treeToExport.getParent(node);
                String parentState = ((((String)parentNode.getAttribute(STATE)).replaceAll("\"","")).replaceAll(" ","")).trim();

                if (parentState.contains("+")) {
                    parentState = parentState.substring(0,parentState.indexOf('+'));
                }

                if (!(state.toLowerCase()).equals(parentState.toLowerCase())) {

                    double latitude = getCoordinate(state, stateNames, stateCoordinates, 0);
                    double longitude = getCoordinate(state, stateNames, stateCoordinates, 1);
                    //System.out.println(latitude+"\t"+longitude);
                    if ((latitude == 0) && (longitude == 0)) {
                        System.err.println(state+" has no coordinate??");
                    }
                    double nodeHeight = treeToExport.getHeight(node);

                    double parentLatitude = getCoordinate(parentState, stateNames, stateCoordinates, 0);
                    double parentLongitude = getCoordinate(parentState, stateNames, stateCoordinates, 1);
                    double parentHeight = treeToExport.getHeight(treeToExport.getParent(node))
                            ;

                    boolean longitudeBreak = longitudeBreak(longitude,parentLongitude);
                    //System.out.println(latitude+"\t"+parentLatitude+"\t"+longitude+"\t"+parentLongitude);

                    if ((parentHeight > time) && (nodeHeight <= time)) {
                        //extrapolate lat/long

                        if (!showBranchAtMidPoint) {

                            latitude = parentLatitude + (latitude-parentLatitude)*((parentHeight-time)/(parentHeight-nodeHeight));

                            if (longitudeBreak) {
                                if (longitude > 0) {
                                    double currentLongitude = parentLongitude - ((180-longitude)+(180+parentLongitude))*((parentHeight-time)/(parentHeight-nodeHeight));

                                    if (currentLongitude < -180) {
                                       longitude = (180+(180+currentLongitude));
                                       //System.out.print("break1"+currentLongitude+"\t"+longitude);
                                    } else {
                                       longitude = currentLongitude;
                                       //System.out.print("break2"+currentLongitude+"\t"+longitude);
                                    }
                                } else {
                                   double currentLongitude = parentLongitude + ((180-parentLongitude)+(180+longitude))*((parentHeight-time)/(parentHeight-nodeHeight));
                                    if (currentLongitude > 180) {
                                        longitude = (-180-(180-currentLongitude));
                                        //System.out.print("break3"+currentLongitude+"\t"+longitude);
                                     } else {
                                        longitude = currentLongitude;
                                        //System.out.print("break4"+longitude);
                                     }
                                }
                                //System.out.print("\t"+state+"_"+longitude+"\t"+parentState+"\r");
                            } else {
                                longitude = parentLongitude + (longitude-parentLongitude)*((parentHeight-time)/(parentHeight-nodeHeight));
                            }
                        }
                    }

                    if (((parentHeight*timeScaler > time) && !(showBranchAtMidPoint)) || (showBranchAtMidPoint && (time < ((nodeHeight+((parentHeight-nodeHeight)/2.0))*timeScaler)))) {
                        treeSliceBuffer.append("\t\t<Placemark>\r");
                        treeSliceBuffer.append("\t\t\t<name>branch"+ nodeNumber +"_"+parentState+"_"+state+"</name>\r");
                        //style
                        treeSliceBuffer.append("\t\t\t<styleUrl>#tree"+time+"branch"+nodeNumber+"_style</styleUrl>\r");
                        // branchesBuffer.append("\t\t\t<styleUrl>#surfaceTreeBranch"+nodeNumber+"_style</styleUrl>\r");
                        treeSliceBuffer.append("\t\t\t<LineString>\r");
                        treeSliceBuffer.append("\t\t\t\t<altitudeMode>clampToGround</altitudeMode>\r");
                        treeSliceBuffer.append("\t\t\t\t<coordinates>\r");
                        treeSliceBuffer.append("\t\t\t\t\t"+longitude+","+latitude+",0\r");
                        treeSliceBuffer.append("\t\t\t\t\t"+parentLongitude+","+parentLatitude+",0\r");
                        treeSliceBuffer.append("\t\t\t\t</coordinates>\r");
                        treeSliceBuffer.append("\t\t\t</LineString>\r");

                        treeSliceBuffer.append("\t\t</Placemark>\r");
                        styleBuffer.append("\t<Style id=\"tree"+ time +"branch"+ nodeNumber +"_style\">\r");
                        styleBuffer.append("\t\t<LineStyle>\r");
                        styleBuffer.append("\t\t\t<width>"+treeSliceBranchWidth+"</width>\r");
                        styleBuffer.append("\t\t\t<color>"+"FF"+ ContinuousKML.getKMLColor((nodeHeight+((parentHeight-nodeHeight)/2.0)),
                                    heightMinAndMax, startBranchColor, endBranchColor)+"</color>\r");
                        styleBuffer.append("\t\t</LineStyle>\r");
                        styleBuffer.append("\t</Style>\r");
                    }
                }

            }
        }
        treeSliceBuffer.append("\t</Folder>\r");
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

        String rootState = null;

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
                         String state = (((String)node.getAttribute(STATE)).replaceAll("\"","")).trim();
                         String parentState = (((String)parentNode.getAttribute(STATE)).replaceAll("\"","")).trim();


                         if ((treeToExport.getHeight(node) <= numberOflineages[j][0]) && (treeToExport.getHeight(parentNode) > numberOflineages[j][0])) {

                            if ((state.equals(parentState)) && (parentState.equals(stateNames[k]))) {

                                numberOfLineagesOfState ++;

                            }

                         }


                    } else {
                        rootState = (((String)node.getAttribute(STATE)).replaceAll("\"","")).trim();
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
            if (rootState.equals(stateNames[a])) {
                rootLat =  stateCoordinates[a][0];
                rootLong = stateCoordinates[a][1];
            }
        }
        writeCircle(rootLat, rootLong, 36, radius, rootState, rootHeight*timeScaler, (rootHeight-delta)*timeScaler, circleBuffer);
        styleBuffer.append("\t<Style id=\"circle_"+rootHeight*timeScaler+"_style\">\r");
        styleBuffer.append("\t\t<LineStyle>\r\t\t\t<width>0.1</width>\r\t\t</LineStyle>\r");
        styleBuffer.append("\t\t<PolyStyle>\r");
        styleBuffer.append("\t\t\t<color>"+circleOpacity+ ContinuousKML.getKMLColor(rootHeight,
                heightMinAndMax, startBranchColor, endBranchColor)+"</color>\r");
        styleBuffer.append("\t\t\t<outline>0</outline>\r");
        styleBuffer.append("\t\t</PolyStyle>\r");
        styleBuffer.append("\t</Style>\r");

        //write all other circles
        for (int o = 0; o < (numberOfIntervals - 1); o ++ ) {
            for (int p = 0; p < (stateNames.length); p ++ ) {

                if (numberOflineages[o][p+1] > 0) {
                    writeCircle(stateCoordinates[p][0], stateCoordinates[p][1], 36, radius*Math.sqrt(numberOflineages[o][p+1]), stateNames[p], numberOflineages[o][0]*timeScaler, (numberOflineages[o][0]-delta)*timeScaler, circleBuffer);

                }
            }
            styleBuffer.append("\t<Style id=\"circle_"+numberOflineages[o][0]*timeScaler+"_style\">\r");
            styleBuffer.append("\t\t<LineStyle>\r\t\t\t<width>0.1</width>\r\t\t</LineStyle>\r");
            styleBuffer.append("\t\t<PolyStyle>\r");
            styleBuffer.append("\t\t\t<color>"+circleOpacity+ ContinuousKML.getKMLColor(numberOflineages[o][0],
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

        buffer.append("\t<Folder>\r");
        buffer.append("\t\t<name>treeSlices</name>\r");
        buffer.append("\t\t<description>tree slices for particular times</description>\r");
        buffer.append(treeSliceBuffer);
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

        int month = ((int) (12.0 * fractionalMonth)) + 1;
        String monthString;

        if (month < 10) {
            monthString = "0"+month;
        } else {
            monthString = ""+month;
        }

        yearMonthDay[1] = monthString;

        int day = ((int) Math.round(30*((12*fractionalMonth +1)- month)) + 1);
        String dayString;

        if (day < 10) {
            dayString = "0"+day;
        } else {
            dayString = ""+day;
        }

        yearMonthDay[2] = dayString;

        return yearMonthDay;

    }

    private static double getCoordinate(String state, String[] locations, double[][] stateCoordinates, int latlong) {

        double coordinate = 0;

        for(int i = 0; i<locations.length; i++) {
        	if (locations[i].equals(state)) {
                coordinate = stateCoordinates[i][latlong];
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
    private void writeCircle(double centerLat, double centerLong, int numberOfPoints, double radius, String centerState, double beginDate, double endDate, StringBuffer buffer) {

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
                             buffer.append("\t\t\t\t\t<begin>"+Math.round(mostRecentDate - beginDate)+"</begin>\r");
                         } else {
                             buffer.append("\t\t\t\t\t<begin>"+begin[0]+"-"+begin[1]+"-"+begin[2]+"</begin>\r");
                         }
                         if (ancient) {
                             buffer.append("\t\t\t\t\t<end>"+Math.round(mostRecentDate - endDate)+"</end>\r");
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

    private double[] getMinMaxLatLong( String[][] locations, String[] locationNames, double[][] locationCoordinates) {

        double[] minMaxLatLong =  new double[4];
        minMaxLatLong[0] = Double.MAX_VALUE;
        minMaxLatLong[1] = -Double.MAX_VALUE;
        minMaxLatLong[2] = Double.MAX_VALUE;
        minMaxLatLong[3] = -Double.MAX_VALUE;


        for (int i = 0; i < locations.length; i++) {
            locationCoordinates[i][0] = Double.parseDouble(locations[i][(locations[0].length - 2)]);
            if (locationCoordinates[i][0] < minMaxLatLong[0])  {
                minMaxLatLong[0] = locationCoordinates[i][0];
            }
            if (locationCoordinates[i][0] > minMaxLatLong[1])  {
                minMaxLatLong[1] = locationCoordinates[i][0];
            }
            locationCoordinates[i][1] = Double.parseDouble(locations[i][(locations[0].length - 1)]);
            if (locationCoordinates[i][1] < minMaxLatLong[2])  {
                minMaxLatLong[2] = locationCoordinates[i][1];
            }
            if (locationCoordinates[i][1] > minMaxLatLong[3])  {
                minMaxLatLong[3] = locationCoordinates[i][1];
            }

        }
        return minMaxLatLong;
    }

    private void getTaxaNamesCoordinates(String[][] locations, String[] locationNames, double[][] locationCoordinates) {

        for (int i = 0; i < locations.length; i++) {
            locationNames[i] = locations[i][(locations[0].length - 3)];  //in a three column file, the name is the first element, so length -3
            locationCoordinates[i][0] = Double.parseDouble(locations[i][(locations[0].length - 2)]);
            locationCoordinates[i][1] = Double.parseDouble(locations[i][(locations[0].length - 1)]);
        }
    }

    private static double getTaxaCoordinate(Taxon taxon, String[] locations, double[][] coordinates, int latOrLong) {

        double coordinate = 0;

        for(int i = 0; i<locations.length; i++) {
        	if ((locations[i].toLowerCase()).equals((taxon.getName()).toLowerCase())) {
                coordinate = coordinates[i][latOrLong];
            }
        }

        return coordinate;
    }

    private static boolean taxonHasSeparateCoordinate(Taxon taxon, String[] locations){

        boolean hasCoordinate = false;

        for(int i = 0; i<locations.length; i++) {
        	if ((locations[i].toLowerCase()).equals((taxon.getName()).toLowerCase())) {
                hasCoordinate = true;
            }
        }
        return hasCoordinate;
    }
    private static boolean longitudeBreak(double longitude, double parentLongitude){

        boolean longitudeBreak = false;
        double trialDistance = 0;
        double longitudeDifference = parentLongitude - longitude;

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
        return longitudeBreak;
    }
}
