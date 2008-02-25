package phylogeochronogrammer;

import jebl.evolution.io.NexusImporter;
import jebl.evolution.io.TreeImporter;
import jebl.evolution.io.ImportException;
import jebl.evolution.trees.Tree;
import jebl.evolution.trees.RootedTree;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: phil
 * Date: Feb 6, 2008
 * Time: 10:41:56 PM
 * To change this template use File | Settings | File Templates.
 */

public class NexusToKML {
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

        double height = 1000000;
        if (args.length > 1) {
            height = Double.parseDouble(args[1]);
        }

        System.out.println("plot height " + height);

        double date = 2000;
        if (args.length > 2) {
            date = Double.parseDouble(args[2]);
        }

        KMLexporter exporter = new KMLexporter(tree, args[0], height, date);

        try {
            BufferedWriter out1 = new BufferedWriter(new FileWriter(args[0]+".kml"));
            StringBuffer buffer = new StringBuffer();
            exporter.writeTreeToKML();
            exporter.compileBuffer(buffer);
            out1.write(buffer.toString());
            out1.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

    }
}
