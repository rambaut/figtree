package phylogeochronogrammer;

import jebl.evolution.trees.RootedTree;
import jebl.evolution.io.TreeImporter;
import jebl.evolution.io.NexusImporter;
import jebl.evolution.io.ImportException;

import java.io.*;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: phil
 * Date: May 17, 2008
 * Time: 10:32:33 AM
 * To change this template use File | Settings | File Templates.
 *
 * usage: inputTreeFile locationDegrees date 
 */
public class DiscreteTreeToKML {
    public static void main(String[] args) {

        boolean locationString = true;
        String annotation = "state";

        //read tree
        String inputTreeFile = args[0];
        RootedTree tree;
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

        // count lines in locations file and tokens per line
        int counter1 = 0; // counts the lines in the file with locations and their coordinates
        int tokenCounter = 0; //counts the colums in the same file
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader(args[1]));
            String current1 = reader1.readLine();
            while (current1 != null && !reader1.equals("") && !reader1.equals("\n") && !reader1.equals("\r")) {
                counter1++;
                if (counter1 == 1) {
                    StringTokenizer tokens = new StringTokenizer(current1);
                    while (tokens.hasMoreTokens()) {
                        tokenCounter++;
                        tokens.nextToken();
                    }
                }
                current1 = reader1.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        //read in locations
        String[][] locations = new String[counter1][tokenCounter];
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(args[1]));
            String current2 = reader2.readLine();
            int counter2 = 0;
            while (current2 != null && !reader2.equals("") && !reader2.equals("\n") && !reader2.equals("\r")) {
                StringTokenizer tokens2 = new StringTokenizer(current2);
                for (int i = 0; i < tokenCounter; i++) {
                    locations[counter2][i] = tokens2.nextToken();
                    System.out.print(locations[counter2][i]+"\t");
                }
                System.out.print("\r");
                counter2 ++;
                current2 = reader2.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        //read in date
        double date = 2000;
        if (args.length > 2) {
            date = Double.parseDouble(args[2]);
        }

        //read in branch colors
        String startBranchColor = "FF00FF";
        String endBranchColor = "FFFF00";
        double timeScaler = 1;
        if (args.length > 3) {
            startBranchColor = args[3];
        }
        if (args.length > 4) {
            endBranchColor = args[4];
        }
        if (args.length > 5) {
            timeScaler = Double.parseDouble(args[5]);
        }

        //read in extra taxon locations
        String[][] taxaLocations = null;
        if (args.length > 6) {
            int counter3 = 0; // counts the lines in the file with locations and there coordinates
            int tokenCounter2 = 0; //counts the colums in the same file
            try {
                BufferedReader reader3 = new BufferedReader(new FileReader(args[6]));
                String current3 = reader3.readLine();
                while (current3 != null && !reader3.equals("")) {
                    counter3++;
                    if (counter3 == 1) {
                        StringTokenizer tokens3 = new StringTokenizer(current3);
                        while (tokens3.hasMoreTokens()) {
                            tokenCounter2++;
                            tokens3.nextToken("\t");
                        }
                    }
                    current3 = reader3.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            taxaLocations = new String[counter3][tokenCounter2];
            System.out.println("\radditional taxa locations:");
            try {
                BufferedReader reader4 = new BufferedReader(new FileReader(args[6]));
                String current4 = reader4.readLine();
                int counter4 = 0;
                while (current4 != null && !reader4.equals("")) {
                    StringTokenizer tokens4 = new StringTokenizer(current4);
                    for (int i = 0; i < tokenCounter; i++) {
                        taxaLocations[counter4][i] = tokens4.nextToken();
                        System.out.print(taxaLocations[counter4][i]+"\t");
                    }
                    System.out.print("\r");
                    counter4 ++;
                    current4 = reader4.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }


        DiscreteKMLString exporterString = null;
        DiscreteKML exporterState = null;
        if (args.length > 4) {
            exporterString = new DiscreteKMLString(tree, annotation, args[0], date, locations,  startBranchColor, endBranchColor, timeScaler);
            if (args.length > 7) {
                exporterString = new DiscreteKMLString(tree, annotation, args[0], date, locations,  startBranchColor, endBranchColor, timeScaler, taxaLocations);
            }
        } else {
            if (locationString) {
                exporterString = new DiscreteKMLString(tree, annotation, args[0], date, locations);
            } else {
                exporterState = new DiscreteKML(tree, args[0], date, locations);
            }
         }

        //a different class is used denpending on the location being a string or character (we should really treat it as a string from now on)

        if (locationString) {
            try {
                BufferedWriter out1 = new BufferedWriter(new FileWriter(args[0]+".kml"));
                StringBuffer buffer = new StringBuffer();
                exporterString.writeTreeToKML();
                //exporterString.writeTreeToKML(0.025);
                exporterString.writeLocationsKML();
                exporterString.writeLineagesToCircles();
                exporterString.compileBuffer(buffer);
                out1.write(buffer.toString());
                out1.close();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            try {
                BufferedWriter out1 = new BufferedWriter(new FileWriter(args[0]+".kml"));
                StringBuffer buffer = new StringBuffer();
                exporterState.writeTreeToKML();
                exporterState.writeLocationsKML();
                exporterState.writeLineagesToCircles();
                exporterState.compileBuffer(buffer);
                out1.write(buffer.toString());
                out1.close();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

        }

    }

}
