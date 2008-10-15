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


        int counter1 = 0; // counts the lines in the file with locations and there coordinates
        int tokenCounter = 0; //counts the colums in teh same file
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader(args[1]));
            String current1 = reader1.readLine();
            while (current1 != null && !reader1.equals("")) {
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

        String[][] locations = new String[counter1][tokenCounter];

        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(args[1]));
            String current2 = reader2.readLine();
            int counter2 = 0;
            while (current2 != null && !reader2.equals("")) {
                StringTokenizer tokens = new StringTokenizer(current2);
                for (int i = 0; i < tokenCounter; i++) {
                    locations[counter2][i] = tokens.nextToken();
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


        double date = 2000;
        if (args.length > 2) {
            date = Double.parseDouble(args[2]);
        }

        DiscreteKML exporter = new DiscreteKML(tree, args[0], date, locations);

        try {
            BufferedWriter out1 = new BufferedWriter(new FileWriter(args[0]+".kml"));
            StringBuffer buffer = new StringBuffer();
            exporter.writeTreeToKML();
            exporter.writeLocationsKML();
            exporter.writeLineagesToCircles();
            exporter.compileBuffer(buffer);
            out1.write(buffer.toString());
            out1.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

    }

}
