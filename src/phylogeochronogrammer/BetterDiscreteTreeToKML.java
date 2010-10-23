package phylogeochronogrammer;


import jebl.evolution.trees.RootedTree;
import jebl.evolution.io.TreeImporter;
import jebl.evolution.io.NexusImporter;
import jebl.evolution.io.ImportException;

import java.io.*;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: phil
 * Date: Oct 14, 2009
 * Time: 8:54:00 PM
 * To change this template use File | Settings | File Templates.
 */

public class BetterDiscreteTreeToKML {

    public static final String HELP = "help";
    public static final String ANNOTATION = "annotation";
    public static final String COORDINATES = "coordinates";
    public static final String TIMESCALER = "timescaler";
    public static final String MRSD = "mrsd";
    public static final String BWC = "bwc";
    public static final String BWM = "bwm";
    public static final String DIVIDER = "divider";
    public static final String USP = "usp";
    public static final String BW = "bw";
    public static final String BCUSE = "bcuse";
    public static final String BCOLOR = "bcolor";
    public static final String BSTARTCOLOR = "bstartcolor";
    public static final String BENDCOLOR = "bendcolor";
    public static final String ARCHBRANCH = "archbranch";
    public static final String ARCHHEIGHT = "archheight";
    public static final String ALTITUDE = "altitude";
    public static final String TEMP = "temp";
    public static final String CIRCLESEGMENTS = "circlesegments";
    public static final String RADIUS = "radius";
    public static final String AUTORADIUS = "autoradius";
    public static final String CIRCLEOP = "circleop";
    public static final String COORDSFORTAXA = "coordsfortaxa";
    public static final String SLICES = "slices";
    public static final String SLICEBW = "slicebw";
    public static final String SLICEMIDPOINT = "slicemidpoint";


    public static final String[] use = new String[] {"heights","posteriors"};
    public static final String[] arch = new String[] {"distance","time"};
    public static final String[] falseTrue = new String[] {"false","true"};
    private static final PrintStream progressStream = System.out;
    private static final String commandName = "phylogeo";

    public static void printUsage(Arguments arguments) {

        arguments.printUsage(commandName, "[<inputTree-file-name>] [<output-file-name>]"); //TODO: set this right
        progressStream.println();
        progressStream.println("  Example: " + commandName + " -coordinates coordinates.txt input.tre");
        progressStream.println();
    }

    private static int[] countLinesAndTokens(String coordinatesFileString){
        int lineCounter = 0;
        int tokenCounter = 0;
        int[] container = new int[2];
        try{
            BufferedReader reader1 = new BufferedReader(new FileReader(coordinatesFileString));
            String current1 = reader1.readLine();
            while (current1 != null && !reader1.equals("")) {
                lineCounter++;
                if (lineCounter == 1) {
                    StringTokenizer tokens = new StringTokenizer(current1);
                    while (tokens.hasMoreTokens()) {
                        tokenCounter++;
                        tokens.nextToken();
                    }
                }
                current1 = reader1.readLine();
            }

        } catch (IOException e) {
            System.err.println("Error reading " + coordinatesFileString);
            System.exit(1);
        }
        container[0] = lineCounter;
        container[1] = tokenCounter;
        return container;
    }

    private static void readLocationsCoordinates(String coordinatesFileString, String[][] locationsAndCoordinates){
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader(coordinatesFileString));
                String current2 = reader2.readLine();
                int counter2 = 0;
                while (current2 != null && !reader2.equals("")) {
                    StringTokenizer tokens2 = new StringTokenizer(current2);
                    for (int i = 0; i < locationsAndCoordinates[0].length; i++) {
                        locationsAndCoordinates[counter2][i] = tokens2.nextToken();
                        progressStream.print(locationsAndCoordinates[counter2][i]+"\t");
                    }
                    progressStream.print("\r");
                    counter2 ++;
                    current2 = reader2.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
    }

    public static double[] parseVariableLengthDoubleArray(String inString) throws Arguments.ArgumentException {

        List<Double> returnList = new ArrayList<Double>();
        StringTokenizer st = new StringTokenizer(inString,",");
        while(st.hasMoreTokens()) {
            try {
                returnList.add(Double.parseDouble(st.nextToken()));
            } catch (NumberFormatException e) {
                throw new Arguments.ArgumentException();
            }

        }

        if (returnList.size()>0) {
            double[] doubleArray = new double[returnList.size()];
            for(int i=0; i<doubleArray.length; i++)
                doubleArray[i] = returnList.get(i);
            return doubleArray;
        }
        return null;
    }

    private static RootedTree readTree(String inString) throws Arguments.ArgumentException {

        RootedTree tree;
        try {
            TreeImporter importer = new NexusImporter(new FileReader(inString));
            tree = (RootedTree)importer.importNextTree();
        } catch (ImportException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return tree;
    }

    public static void main(String[] args) throws Arguments.ArgumentException {

        String inputFileName = null;
        String outputFileName = null;
        RootedTree tree = null;
        String[][] locations = null;

        String stateAnnotation = "state";

        //in case trees are scaled in other time units
        double timeScaler = 1;

        double branchWidthConstant = 2.0;   // the width of branches will be stateProbability*branchWidthMultiplier+branchWidthConstant
        double branchWidthMultiplier = 5.0;

        double divider = 100; // this is to chop up the branches of the surface tree in 'divider' segments
        boolean useStateProbability = true; // use state probabilities for branch width
        double branchWidth = 10.0; // branch width if posterior probabilities are not used
        boolean usePosterior = false; // use posterior probabilities to color branch
        boolean useHeights = true; // use heights (time) to color branches
        String startBranchColor = "FF00FF"; //red: 0000FF green: 00FF00 magenta: FF00FF white: FFFFFF yellow: 00FFFF cyan: FFFF00
        String endBranchColor = "FFFF00";
        String branchColor = "ffffff"; // branch color if color range based on rates is not used
        boolean arcBranches = true; // branches are arcs with heights proportional to the distance between locations
        boolean arcTimeHeight = false; // the height of the arcs is proportional to the time the branch spans, by default arch-heights are proportional to the distance between locations
        double altitudeFactor = 100; // this is the factor with which to multiply the time of the branch to get the altitude for that branch in the surface Tree
        boolean temporary = false;

        double mostRecentDate = 2009;  // required to convert heights to calendar dates

        //circles
        int numberOfIntervals = 100;
        //boolean autoRadius = false;
        double radius = 0;
        String circleOpacity = "8F";

        //extra coordinates for some taxa
        boolean coordinatesForTaxa = false;
        String[][] taxaCoordinates = null;

        //tree slices for google maps, requires treeHeight in exporterString: e.g., exporterString.writeTreeToKML(0.025);
        //TODO: have this outputed to a different file (what happens now)?
        boolean makeTreeSlices = false;
        double[] sliceTimes = null;
        double treeSliceBranchWidth = 3;
        boolean showBranchAtMidPoint = false; // shows complete branch for slice if time is more recent than the branch's midpoint


        Arguments arguments = new Arguments(
                new Arguments.Option[]{
                        new Arguments.StringOption(COORDINATES, "coordinate file", "specifies a tab-delimited file with coordinates for the locations"),
                        new Arguments.StringOption(ANNOTATION, "location state annotation string", "specifies the string used for location state annotation [default=state]"),
                        new Arguments.RealOption(TIMESCALER,"specifies the scaling factor by which to rescale time [default=1]"),
                        new Arguments.RealOption(MRSD,"specifies the most recent sampling data in fractional years to rescale time [default=2009]"),
                        new Arguments.RealOption(BWC,"specifies the branch width constant [default=2]"),
                        new Arguments.RealOption(BWM,"specifies the branch width multiplier [default=5]"),
                        new Arguments.StringOption(USP, falseTrue, false,
                                "use state probabilities for branch width [default = true]"),
                        new Arguments.StringOption(BCUSE, use, false,
                                "use heights or posterior probabilities for branch colors [default = heights]"),
                        new Arguments.RealOption(BW,"specifies the branch width if posterior probabilities are not used [default=10]"),
                        new Arguments.IntegerOption(DIVIDER,"specifies in how many segments at branch should be chopped up [default=50]"),
                        new Arguments.StringOption(BSTARTCOLOR, "branch start color", "specifies a starting color for the branches [default=FF00FF]"),
                        new Arguments.StringOption(BENDCOLOR, "branch end color", "specifies an end color for the branches [default=FFFF00]"),
                        new Arguments.StringOption(BCOLOR, "branch color", "specifies a branch color if color range based on rates is not used [default=ffffff]"),
                        new Arguments.StringOption(ARCHBRANCH, falseTrue, false,
                                "use arcs for the branches [default = true], by default arc-heights are proportional to the distance between locations"),
                        new Arguments.StringOption(ARCHHEIGHT, arch, false,
                                "use time or distance for arch heights [default = no arcs]"),
                        new Arguments.RealOption(ALTITUDE,"specifies the altitudefactor for the branches [default=1000]"),
                        new Arguments.StringOption(TEMP, falseTrue, false,
                                "display branches only temporary [default=false"),
                        new Arguments.IntegerOption(CIRCLESEGMENTS,"specifies the number of segments to construct circles [default=100]"),
                        new Arguments.IntegerOption(RADIUS,"specifies the radiusfactor for the circles [default='autoradius']"),
                        new Arguments.StringOption(CIRCLEOP, "circle opacity", "sets the opacity of the circles [default=8F]"),
                        new Arguments.Option(HELP, "option to print this message"),
                        new Arguments.StringOption(COORDSFORTAXA, "file with taxa coords", "specifies a file with additional coordinates for particular taxa"),
                        new Arguments.RealOption(SLICEBW,"specifies the branch width for tree slices [default=3]"),
                        new Arguments.StringOption(SLICES,"time","specifies a slice time-list [default=none]"),
                        new Arguments.StringOption(SLICEMIDPOINT, falseTrue, false,
                                "shows complete branch for sliced tree if time is more recent than the branch's midpoint [default=false"),
                });

        try {
            arguments.parseArguments(args);
        } catch (Arguments.ArgumentException ae) {
            progressStream.println(ae);
            printUsage(arguments);
            System.exit(1);
        }

        if (arguments.hasOption(HELP)) {
            printUsage(arguments);
            System.exit(0);
        }

        try {

            String coordinatesFileString = arguments.getStringOption(COORDINATES);
            //System.out.println(coordinatesFileString);
            if (coordinatesFileString != null) {
                // count lines in locations file and tokens per line
                int counts[] = countLinesAndTokens(coordinatesFileString);
                //System.out.println(counts[0]+"\t"+counts[1]);
                //read in locations
                locations = new String[counts[0]][counts[1]];
                readLocationsCoordinates(coordinatesFileString,locations);
            } else {
                progressStream.print("no coordinates for taxa??");
                System.exit(1);
            }

            if (arguments.hasOption(MRSD)) {
                mostRecentDate = arguments.getRealOption(MRSD);
            }

            if (arguments.hasOption(TIMESCALER)) {
                timeScaler = arguments.getRealOption(TIMESCALER);
            }

            if (arguments.hasOption(DIVIDER)) {
                divider = arguments.getRealOption(DIVIDER);
            }

            if (arguments.hasOption(ALTITUDE)) {
                altitudeFactor = arguments.getRealOption(ALTITUDE);
            }

            String stateAnnotationString = arguments.getStringOption(ANNOTATION);
            if (stateAnnotationString != null){
                stateAnnotation = stateAnnotationString;
            }

            String useStateProbString = arguments.getStringOption(USP);
            if (useStateProbString != null && useStateProbString.compareToIgnoreCase("posteriors") != 0)
                useStateProbability = false;

            String useColorString = arguments.getStringOption(BCUSE);
            if (useColorString != null && useColorString.compareToIgnoreCase("posteriors") == 0) {
                usePosterior = true;
                useHeights = false;
            }
            if (useColorString != null && useColorString.compareToIgnoreCase("heights") == 0) {
                useHeights =  true;
                usePosterior = false;
            }

            if (arguments.hasOption(BWC)) {
                branchWidthConstant = arguments.getRealOption(BWC);
            }

            if (arguments.hasOption(BWM)) {
                branchWidthMultiplier = arguments.getRealOption(BWM);
            }

            if (arguments.hasOption(BW)) {
                branchWidth = arguments.getRealOption(BW);
            }

            String color1String = arguments.getStringOption(BSTARTCOLOR);
            if (color1String != null) {
                startBranchColor = color1String;
            }

            String color2String = arguments.getStringOption(BENDCOLOR);
            if (color2String != null) {
                endBranchColor = color2String;
            }

            String colorString = arguments.getStringOption(BCOLOR);
            if (colorString != null) {
                branchColor = colorString;
            }

            String archString = arguments.getStringOption(ARCHBRANCH);
            if (archString != null && archString.compareToIgnoreCase("false") == 0)
                arcBranches = false;

            String archHeightString = arguments.getStringOption(ARCHHEIGHT);
            if (archHeightString != null && archHeightString.compareToIgnoreCase("time") == 0) {
                arcTimeHeight = true;
            }

            String tempString = arguments.getStringOption(TEMP);
            if (tempString != null && tempString.compareToIgnoreCase("true") == 0)
                temporary = true;

            if (arguments.hasOption(CIRCLESEGMENTS)) {
                numberOfIntervals = arguments.getIntegerOption(CIRCLESEGMENTS);
            }

            if (arguments.hasOption(RADIUS)) {
                radius = arguments.getIntegerOption(RADIUS);
            }

            String circleOpacityString = arguments.getStringOption(CIRCLEOP);
            if (circleOpacityString != null) {
                circleOpacity = circleOpacityString;
            }

            //read in extra taxon locations
            String taxaCoordinatesFileString = arguments.getStringOption(COORDSFORTAXA);
            if (taxaCoordinatesFileString != null) {
                coordinatesForTaxa = true;
                progressStream.println("\radditional taxa locations:");
                // count lines in locations file and tokens per line
                int[] counts = countLinesAndTokens(taxaCoordinatesFileString);
                //read in locations
                taxaCoordinates = new String[counts[0]][counts[1]];
                readLocationsCoordinates(taxaCoordinatesFileString,taxaCoordinates);
            }

            String sliceString = arguments.getStringOption(SLICES);
            if (sliceString != null) {
                makeTreeSlices = true;
                sliceTimes = parseVariableLengthDoubleArray(sliceString);
            }

            if (arguments.hasOption(SLICEBW)) {
                treeSliceBranchWidth = arguments.getRealOption(SLICEBW);
            }

            String midpointString = arguments.getStringOption(SLICEMIDPOINT);
            if (midpointString != null && midpointString.compareToIgnoreCase("true") == 0)
                showBranchAtMidPoint = true;

        } catch (Arguments.ArgumentException e) {
            progressStream.println(e);
            printUsage(arguments);
            System.exit(-1);
        }

        final String[] args2 = arguments.getLeftoverArguments();

        outputFileName = args2[0]+".kml";
        switch (args2.length) {
            case 0:
                printUsage(arguments);
                System.exit(1);
            case 2:
                outputFileName = args2[1];
                // fall to
            case 1:
                inputFileName = args2[0];
                tree = readTree(inputFileName);
                break;
            default: {
                System.err.println("Unknown option: " + args2[2]);
                System.err.println();
                printUsage(arguments);
                System.exit(1);
            }
        }

        DiscreteKMLString exporterString = new DiscreteKMLString(tree, stateAnnotation, locations, inputFileName, mostRecentDate, timeScaler, divider, branchWidthConstant, branchWidthMultiplier, useStateProbability, branchWidth, startBranchColor, endBranchColor, branchColor, useHeights, usePosterior, arcBranches, arcTimeHeight, altitudeFactor, temporary, numberOfIntervals, radius, circleOpacity, coordinatesForTaxa, taxaCoordinates, makeTreeSlices);

        try {
            BufferedWriter out1 = new BufferedWriter(new FileWriter(outputFileName));
            StringBuffer buffer = new StringBuffer();

            if (makeTreeSlices) {
                for (int i = 0; i < sliceTimes.length; i++)
                exporterString.writeTreeToKML(sliceTimes[i], treeSliceBranchWidth, showBranchAtMidPoint);
            } else {
                exporterString.writeTreeToKML();
                exporterString.writeLineagesToCircles();
            }
            exporterString.writeLocationsKML();
            exporterString.compileBuffer(buffer);
            out1.write(buffer.toString());
            out1.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

    }

}
