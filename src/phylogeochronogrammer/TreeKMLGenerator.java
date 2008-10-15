package phylogeochronogrammer;

import jebl.evolution.trees.RootedTree;
import jebl.evolution.graphs.Node;
import org.jdom.Element;

import java.util.List;

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
    }

    public Element generate(RootedTree tree, List<TreeDefinition> definitions) {

        Element element = new Element("trees");

        for (TreeDefinition definition : definitions) {
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
                        }

                    }



                        styleBuffer.append("\t<Style id=\"surfaceTreeBranch"+ nodeNumber +"_part"+(a+1)+"_style\">\r");
                        styleBuffer.append("\t\t<LineStyle>\r");
                        if (usePosterior_ST) {
                            if (treeToExport.isExternal(node)) {
                                styleBuffer.append("\t\t\t<width>"+(branchWidthConstant+branchWidthMultiplier)+"</width>\r");
                            }  else {
                                Double posterior = (Double)node.getAttribute("posterior");
                                styleBuffer.append("\t\t\t<width>"+(branchWidthConstant+posterior*branchWidthMultiplier)+"</width>\r");
                            }
                        } else {
                            styleBuffer.append("\t\t\t<width>"+branchWidth_ST+"</width>\r");
                        }
                        if (useHeights_ST){
                            styleBuffer.append("\t\t\t<color>"+"FF"+getKMLColor((treeToExport.getHeight(node) + (a + 1) *
                                    ((treeToExport.getHeight(parentNode) - (treeToExport.getHeight(node)))/divider)),
                                    heightMinAndMax, startBranchColor_ST, endBranchColor_ST)+"</color>\r");
                        } else {
                            styleBuffer.append("\t\t\t<color>"+"FF"+branchColor_ST+"</color>\r");
                        }
                        styleBuffer.append("\t\t</LineStyle>\r");
                        styleBuffer.append("\t</Style>\r");

                    }
                    surfaceTreeBuffer.append("\t</Folder>\r");

                    //this writes placemarks on the surface for the taxa, and writes out projections from the tips to the surface
                    if (treeToExport.isExternal(node)) {

                        taxaBuffer.append("\t\t\t<Placemark>\r");

                        taxaBuffer.append("\t\t\t<visibility>"+visibility_taxa+"</visibility>\r");

                        taxaBuffer.append("\t\t\t\t<name>"+treeToExport.getTaxon(node).getName()+"</name>\r");

                        taxaBuffer.append("\t\t\t\t<Point>\r");
                        taxaBuffer.append("\t\t\t\t\t<altitudeMode>relativeToGround</altitudeMode>\r");
                        taxaBuffer.append("\t\t\t\t\t<coordinates>"+longitude+","+latitude+","+"0</coordinates>\r");
                        taxaBuffer.append("\t\t\t\t</Point>\r");

                        taxaBuffer.append("\t\t\t</Placemark>\r");

                        if (treeToExport.getHeight(node) > 0) {

                            projectionsBuffer.append("\t\t\t<LineString>\r");
                            projectionsBuffer.append("\t\t\t\t<altitudeMode>relativeToGround</altitudeMode>\r");

                            projectionsBuffer.append("\t\t\t\t<coordinates>\r");
                            projectionsBuffer.append("\t\t\t\t\t"+longitude+","+latitude+","+altitude+"\r");
                            projectionsBuffer.append("\t\t\t\t\t"+longitude+","+latitude+",0\r");
                            projectionsBuffer.append("\t\t\t\t</coordinates>\r");

                            projectionsBuffer.append("\t\t\t</LineString>\r");
                        }

                        styleBuffer.append("\t<Style id=\"rectangleTreeBranch"+ nodeNumber +"_style\">\r");
                        styleBuffer.append("\t\t<LineStyle>\r");
                        if (usePosterior_RT) {
                            styleBuffer.append("\t\t\t<width>"+(branchWidthConstant+branchWidthMultiplier)+"</width>\r");
                        } else {
                            styleBuffer.append("\t\t\t<width>"+branchWidth_RT+"</width>\r");
                        }
                        if (useRates_RT) {
                            styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_RT)+
                                    getKMLColor((Double)node.getAttribute("rate"), rateMinMaxMedian, startBranchColor_RT,
                                            endBranchColor_RT)+"</color>\r");
                        } else {
                            styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_RT)+branchColor_RT+"</color>\r");
                        }
                        styleBuffer.append("\t\t</LineStyle>\r");
                        styleBuffer.append("\t</Style>\r");

                        styleBuffer.append("\t<Style id=\"triangleTreeBranch"+ nodeNumber +"_style\">\r");
                        styleBuffer.append("\t\t<LineStyle>\r");
                        if (usePosterior_TT) {
                            styleBuffer.append("\t\t\t<width>"+(branchWidthConstant+branchWidthMultiplier)+"</width>\r");
                        } else {
                            styleBuffer.append("\t\t\t<width>"+branchWidth_TT+"</width>\r");
                        }
                        if (useRates_TT) {
                            styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_TT)+
                                    getKMLColor((Double)node.getAttribute("rate"), rateMinMaxMedian, startBranchColor_TT,
                                            endBranchColor_TT)+"</color>\r");
                        } else {
                            styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_TT)+branchColor_TT+"</color>\r");
                        }
                        styleBuffer.append("\t\t</LineStyle>\r");
                        styleBuffer.append("\t</Style>\r");

                        // line style for surface external branches, based on posterior
                        styleBuffer.append("\t<Style id=\"surfaceTreeBranch"+ nodeNumber +"_style\">\r");
                        styleBuffer.append("\t\t<LineStyle>\r");
                        if (usePosterior_ST) {
                            styleBuffer.append("\t\t\t<width>"+(branchWidthConstant+branchWidthMultiplier)+"</width>\r");
                        } else {
                            styleBuffer.append("\t\t\t<width>"+branchWidth_ST+"</width>\r");
                        }
                        if (useHeights_ST){
                            styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_ST)+getKMLColor(treeToExport.getHeight(node),
                                    heightMinAndMax, startBranchColor_ST, endBranchColor_ST)+"</color>\r");
                        } else {
                            styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_ST)+branchColor_ST+"</color>\r");
                        }
                        styleBuffer.append("\t\t</LineStyle>\r");
                        styleBuffer.append("\t</Style>\r");

                    }  else {

                        if (contoursAndNotSpades) {
                            appendContour(contourBuffer, treeToExport, node, nodeNumber, plotHeight, mostRecentDate, traitName, latitudeName, longitudeName, false, iniVisi_contours);
                            appendContour(groundContourBuffer, treeToExport, node, nodeNumber, plotHeight, mostRecentDate, traitName, latitudeName, longitudeName, true, iniVisi_groundContours);
                        } else {
                            appendSpade(spadeBuffer, treeToExport, node, nodeNumber, plotHeight, mostRecentDate, latitudeName, longitudeName, false, iniVisi_spades);
                            appendSpade(groundSpadeBuffer, treeToExport, node, nodeNumber, plotHeight, mostRecentDate, latitudeName, longitudeName, true, iniVisi_groundSpades);
                            appendDiamond(diamondBuffer, treeToExport, node, nodeNumber, plotHeight, latitudeName, longitudeName, iniVisi_diamonds);
                        }

                        Double posterior = (Double)node.getAttribute("posterior");

                        // line style for internal rectangleTree branches, based on posterior
                        styleBuffer.append("\t<Style id=\"rectangleTreeBranch"+ nodeNumber +"_style\">\r");
                        styleBuffer.append("\t\t<LineStyle>\r");
                        if (usePosterior_RT) {
                            styleBuffer.append("\t\t\t<width>"+(branchWidthConstant+posterior*branchWidthMultiplier)+"</width>\r");
                        } else {
                            styleBuffer.append("\t\t\t<width>"+branchWidth_RT+"</width>\r");
                        }
                        if (useRates_RT) {
                            styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_RT)+getKMLColor((Double)node.getAttribute("rate"),rateMinMaxMedian,
                                    startBranchColor_RT, endBranchColor_RT)+"</color>\r");
                        } else {
                            styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_RT)+branchColor_RT+"</color>\r");
                        }
                        styleBuffer.append("\t\t</LineStyle>\r");
                        styleBuffer.append("\t</Style>\r");

                        // line style for internal nodes, based on posterior
                        styleBuffer.append("\t<Style id=\"triangleTreeBranch"+ nodeNumber +"_style\">\r");
                        styleBuffer.append("\t\t<LineStyle>\r");
                        if (usePosterior_TT) {
                            styleBuffer.append("\t\t\t<width>"+(branchWidthConstant+posterior*branchWidthMultiplier)+"</width>\r");
                        } else {
                            styleBuffer.append("\t\t\t<width>"+branchWidth_TT+"</width>\r");
                        }
                        if (useRates_TT) {
                            styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_TT)+getKMLColor((Double)node.getAttribute("rate"),rateMinMaxMedian,
                                    startBranchColor_TT, endBranchColor_TT)+"</color>\r");
                        } else {
                            styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_TT)+branchColor_TT+"</color>\r");
                        }
                        styleBuffer.append("\t\t</LineStyle>\r");
                        styleBuffer.append("\t</Style>\r");

                        // line style for surface internal branches, based on posterior
                        styleBuffer.append("\t<Style id=\"surfaceTreeBranch"+ nodeNumber +"_style\">\r");
                        styleBuffer.append("\t\t<LineStyle>\r");
                        if (usePosterior_ST) {
                            styleBuffer.append("\t\t\t<width>"+(branchWidthConstant+posterior*branchWidthMultiplier)+"</width>\r");
                        } else {
                            styleBuffer.append("\t\t\t<width>"+branchWidth_ST+"</width>\r");
                        }
                        if (useHeights_ST){
                            styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_ST)+getKMLColor(treeToExport.getHeight(node),
                                    heightMinAndMax, startBranchColor_ST, endBranchColor_ST)+"</color>\r");
                        } else {
                            styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_ST)+branchColor_ST+"</color>\r");
                        }
                        styleBuffer.append("\t\t</LineStyle>\r");
                        styleBuffer.append("\t</Style>\r");

                        // spade style for internal nodes
                        styleBuffer.append("\t<Style id=\"spade"+ nodeNumber +"_style\">\r");
                        styleBuffer.append("\t\t<LineStyle>\r");
                        styleBuffer.append("\t\t\t<width>1.5</width>\r");
                        if (useHeights_spades) {
                            styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_spades)+getKMLColor(treeToExport.getHeight(node),
                                    heightMinAndMax, startSpadeColor, endSpadeColor)+"</color>\r");
                        }  else {
                            styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_spades)+spadeColor+"</color>\r");
                        }
                        styleBuffer.append("\t\t</LineStyle>\r");
                        styleBuffer.append("\t\t<PolyStyle>\r");
                        if (useHeights_spades) {
                            styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_spades)+getKMLColor(treeToExport.getHeight(node),
                                    heightMinAndMax, startSpadeColor, endSpadeColor)+"</color>\r");
                        }  else {
                            styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_spades)+spadeColor+"</color>\r");
                        }
                        styleBuffer.append("\t\t</PolyStyle>\r");
                        styleBuffer.append("\t</Style>\r");

                        // ground spade style for internal nodes
                        styleBuffer.append("\t<Style id=\"groundSpade"+ nodeNumber +"_style\">\r");
                        styleBuffer.append("\t\t<LineStyle>\r");
                        styleBuffer.append("\t\t\t<width>0.5</width>\r");
                        styleBuffer.append("\t\t</LineStyle>\r");
                        styleBuffer.append("\t\t<PolyStyle>\r");
                        if (useHeights_groundSpades) {
                            styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_groundSpades)+getKMLColor(treeToExport.getHeight(node),
                                    heightMinAndMax, startgroundSpadeColor, endgroundSpadeColor)+"</color>\r");
                        } else {
                            styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_groundSpades)+groundSpadeColor+"</color>\r");
                        }
                        styleBuffer.append("\t\t\t<outline>0</outline>\r");
                        styleBuffer.append("\t\t</PolyStyle>\r");
                        styleBuffer.append("\t</Style>\r");
                    }

                    // write a ancestral branch to the root with length that is a precentage of the total height
                }  else {

                    rectangleTreeBuffer.append("\t\t<Placemark>\r");
                    triangleTreeBuffer.append("\t\t<Placemark>\r");

                    // by putting a timeSpan on this root branch, this will be the oldest time. So, everything else will appear later in the time animation. (if not, the root spade would already be there)
                    double date = mostRecentDate - (treeToExport.getHeight(node) + (fraction*treeToExport.getHeight(node)));
                    String[] yearMonthDay = convertToYearMonthDay(date);
                    rectangleTreeBuffer.append("\t\t\t<TimeSpan>\r");
                    triangleTreeBuffer.append("\t\t\t<TimeSpan>\r");
                    rectangleTreeBuffer.append("\t\t\t\t<begin>"+yearMonthDay[0]+"-"+yearMonthDay[1]+"-"+yearMonthDay[2]+"</begin>\r");
                    triangleTreeBuffer.append("\t\t\t\t<begin>"+yearMonthDay[0]+"-"+yearMonthDay[1]+"-"+yearMonthDay[2]+"</begin>\r");
                    rectangleTreeBuffer.append("\t\t\t</TimeSpan>\r");
                    triangleTreeBuffer.append("\t\t\t</TimeSpan>\r");

                    rectangleTreeBuffer.append("\t\t\t<visibility>"+visibility_RT+"</visibility>\r");
                    triangleTreeBuffer.append("\t\t\t<visibility>"+visibility_TT+"</visibility>\r");
                    rectangleTreeBuffer.append("\t\t\t<name>rectangleTreeBranch"+ nodeNumber +"</name>\r");
                    triangleTreeBuffer.append("\t\t\t<name>triangleTreeBranch"+ nodeNumber +"</name>\r");
                    rectangleTreeBuffer.append("\t\t\t<styleUrl>#rectangleTreeBranch"+ nodeNumber +"_style</styleUrl>\r");
                    triangleTreeBuffer.append("\t\t\t<styleUrl>#triangleTreeBranch"+ nodeNumber +"_style</styleUrl>\r");
                    rectangleTreeBuffer.append("\t\t\t<LineString>\r");
                    triangleTreeBuffer.append("\t\t\t<LineString>\r");
                    rectangleTreeBuffer.append("\t\t\t\t<altitudeMode>relativeToGround</altitudeMode>\r");
                    triangleTreeBuffer.append("\t\t\t\t<altitudeMode>relativeToGround</altitudeMode>\r");
                    rectangleTreeBuffer.append("\t\t\t\t<coordinates>\r");
                    triangleTreeBuffer.append("\t\t\t\t<coordinates>\r");

                    rectangleTreeBuffer.append("\t\t\t\t\t"+longitude+","+latitude+","+altitude+"\r");
                    triangleTreeBuffer.append("\t\t\t\t\t"+longitude+","+latitude+","+altitude+"\r");
                    rectangleTreeBuffer.append("\t\t\t\t\t"+longitude+","+latitude+","+(altitude+(fraction*altitude))+"\r");
                    triangleTreeBuffer.append("\t\t\t\t\t"+longitude+","+latitude+","+(altitude+(fraction*altitude))+"\r");

                    rectangleTreeBuffer.append("\t\t\t\t</coordinates>\r");
                    triangleTreeBuffer.append("\t\t\t\t</coordinates>\r");
                    rectangleTreeBuffer.append("\t\t\t</LineString>\r");
                    triangleTreeBuffer.append("\t\t\t</LineString>\r");
                    rectangleTreeBuffer.append("\t\t</Placemark>\r");
                    triangleTreeBuffer.append("\t\t</Placemark>\r");

                    if (contoursAndNotSpades) {
                        appendContour(contourBuffer, treeToExport, node, nodeNumber, plotHeight, mostRecentDate, traitName, latitudeName, longitudeName, false, iniVisi_contours);
                        appendContour(groundContourBuffer, treeToExport, node, nodeNumber, plotHeight, mostRecentDate, traitName, latitudeName, longitudeName, true, iniVisi_groundContours);
                    } else {
                        appendSpade(spadeBuffer, treeToExport, node, nodeNumber, plotHeight, mostRecentDate, latitudeName, longitudeName, false, iniVisi_spades);
                        appendSpade(groundSpadeBuffer, treeToExport, node, nodeNumber, plotHeight, mostRecentDate, latitudeName, longitudeName, true, iniVisi_groundSpades);
                        appendDiamond(diamondBuffer, treeToExport, node, nodeNumber, plotHeight, latitudeName, longitudeName, iniVisi_diamonds);
                    }

                    // style for the rectangle root branch
                    styleBuffer.append("\t<Style id=\"rectangleTreeBranch"+ nodeNumber +"_style\">\r");
                    styleBuffer.append("\t\t<LineStyle>\r");
                    if (usePosterior_RT) {
                        styleBuffer.append("\t\t\t<width>"+(branchWidthConstant+branchWidthMultiplier)+"</width>\r");
                    } else {
                        styleBuffer.append("\t\t\t<width>"+branchWidth_RT+"</width>\r");
                    }
                    if (useRates_RT){
                        // the root branch gets the color for the median rate
                        styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_RT)+getKMLColor(rateMinMaxMedian[2],
                                rateMinMaxMedian, startBranchColor_RT, endBranchColor_RT)+"</color>\r");
                    } else {
                        styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_RT)+branchColor_RT+"</color>\r");
                    }
                    styleBuffer.append("\t\t</LineStyle>\r");
                    styleBuffer.append("\t</Style>\r");

                    // style for the triangle root branch
                    styleBuffer.append("\t<Style id=\"triangleTreeBranch"+ nodeNumber +"_style\">\r");
                    styleBuffer.append("\t\t<LineStyle>\r");
                    if (usePosterior_RT) {
                        styleBuffer.append("\t\t\t<width>"+(branchWidthConstant+branchWidthMultiplier)+"</width>\r");
                    } else {
                        styleBuffer.append("\t\t\t<width>"+branchWidth_TT+"</width>\r");
                    }
                    if (useRates_TT){
                        // the root branch gets the color for the median rate
                        styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_TT)+getKMLColor(rateMinMaxMedian[2],
                                rateMinMaxMedian, startBranchColor_TT, endBranchColor_TT)+"</color>\r");
                    } else {
                        styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_TT)+branchColor_TT+"</color>\r");
                    }
                    styleBuffer.append("\t\t</LineStyle>\r");
                    styleBuffer.append("\t</Style>\r");

                    // style for the root spade based on height
                    styleBuffer.append("\t<Style id=\"spade"+ nodeNumber +"_style\">\r");
                    styleBuffer.append("\t\t<LineStyle>\r");
                    styleBuffer.append("\t\t\t<width>1.5</width>\r");
                    if (useHeights_spades) {
                        styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_spades)+getKMLColor(treeToExport.getHeight(node),
                                heightMinAndMax, startSpadeColor, endSpadeColor)+"</color>\r");
                    }  else {
                        styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_spades)+spadeColor+"</color>\r");
                    }
                    styleBuffer.append("\t\t</LineStyle>\r");
                    styleBuffer.append("\t\t<PolyStyle>\r");
                    if (useHeights_spades) {
                        styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_spades)+getKMLColor(treeToExport.getHeight(node),
                                heightMinAndMax, startSpadeColor, endSpadeColor)+"</color>\r");
                    }  else {
                        styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_spades)+spadeColor+"</color>\r");
                    }
                    styleBuffer.append("\t\t</PolyStyle>\r");
                    styleBuffer.append("\t</Style>\r");
                    // ground spade style for root
                    styleBuffer.append("\t<Style id=\"groundSpade"+ nodeNumber +"_style\">\r");
                    styleBuffer.append("\t\t<LineStyle>\r");
                    styleBuffer.append("\t\t\t<width>0.5</width>\r");
                    styleBuffer.append("\t\t</LineStyle>\r");
                    styleBuffer.append("\t\t<PolyStyle>\r");
                    if (useHeights_groundSpades) {
                        styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_groundSpades)+getKMLColor(treeToExport.getHeight(node),
                                heightMinAndMax, startgroundSpadeColor, endgroundSpadeColor)+"</color>\r");
                    } else {
                        styleBuffer.append("\t\t\t<color>"+getOpacity(opacity_groundSpades)+groundSpadeColor+"</color>\r");
                    }
                    styleBuffer.append("\t\t\t<outline>0</outline>");
                    styleBuffer.append("\t\t</PolyStyle>\r");
                    styleBuffer.append("\t</Style>\r");
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

    public void compileBuffer(StringBuffer buffer) {

        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r");
        buffer.append("<kml xmlns=\"http://earth.google.com/kml/2.2\">\r");

        buffer.append("<Document>\r");

        buffer.append("\t<name>"+documentName+"</name>\r");
        buffer.append(styleBuffer);

        buffer.append("\t<Style id=\"diamondStyle\">\r");
        buffer.append("\t\t<LineStyle>\r");
        buffer.append("\t\t\t<width>0.5</width>\r");
        buffer.append("\t\t</LineStyle>\r");
        buffer.append("\t\t<PolyStyle>\r");
        buffer.append("\t\t\t<color>7d00ffff</color>\r");
        buffer.append("\t\t\t<fill>0</fill>\r");
        buffer.append("\t\t</PolyStyle>\r");
        buffer.append("\t</Style>\r");

        buffer.append("\t<Folder>\r");
        buffer.append("\t<name>triangle tree</name>\r");
        buffer.append("\t<description>tree out of surface with node heights proportional to time</description>\r");
        buffer.append(triangleTreeBuffer);
        buffer.append("\t</Folder>\r");

        buffer.append("\t<Folder>\r");
        buffer.append("\t<name>rectangle tree</name>\r");
        buffer.append("\t<description>tree out of surface with branch lengths (and node heights) proportional to time</description>\r");

        buffer.append(rectangleTreeBuffer);
        buffer.append("\t</Folder>\r");

        buffer.append("\t<Folder>\r");
        buffer.append("\t<name>surface tree</name>\r");
        buffer.append("\t<description>tree on the surface interconnecting inferred and sampled locations</description>\r");
        buffer.append(surfaceTreeBuffer);
        buffer.append("\t</Folder>\r");

        int visibility_projections;
        if (iniVisi_projections) { visibility_projections = 1; } else { visibility_projections = 0; }

        buffer.append("\t<Placemark>\r");
        buffer.append("\t\t<name>projections</name>\r");
        buffer.append("\t\t<description>projections from tips to surface</description>\r");
        buffer.append("\t\t<visibility>"+visibility_projections+"</visibility>\r");
        buffer.append("\t\t<MultiGeometry>\r");
        buffer.append(projectionsBuffer);
        buffer.append("\t\t</MultiGeometry>\r");
        buffer.append("\t</Placemark>\r");

        buffer.append("\t<Folder>\r");
        buffer.append("\t\t<name>Taxon labels</name>\r");
        buffer.append("\t\t<description>Taxon Labels</description>\r");
        buffer.append(taxaBuffer);
        buffer.append("\t</Folder>\r");

        //writes a folder with credible intervals in the form of polygons
        buffer.append("\t<Folder>\r");
        buffer.append("\t<name>polygon HPDs</name>\r");
        buffer.append("\t<description>various polygons to represent credible intervals</description>\r");

        if (contoursAndNotSpades) {

            buffer.append("\t\t<Folder>\r");
            buffer.append("\t\t\t<name>contours HPDs</name>\r");
            buffer.append("\t\t\t<description>contour HPDs for internal nodes</description>\r");
            buffer.append(contourBuffer);
            buffer.append("\t\t</Folder>\r");

            buffer.append("\t\t<Folder>\r");
            buffer.append("\t\t\t<name>surface contour HPDs</name>\r");
            buffer.append("\t\t\t<description>contour HPDs for internal nodes projected on the surface</description>\r");
            buffer.append(groundContourBuffer);
            buffer.append("\t\t</Folder>\r");

        } else {

            buffer.append("\t\t<Folder>\r");
            buffer.append("\t\t\t<name>spade HPDs</name>\r");
            buffer.append("\t\t\t<description>longitude and latitude HPDs for internal nodes</description>\r");
            buffer.append(spadeBuffer);
            buffer.append("\t\t</Folder>\r");

            buffer.append("\t\t<Folder>\r");
            buffer.append("\t\t\t<name>surface spade HPDs</name>\r");
            buffer.append("\t\t\t<description>longitude and latitude HPDs for internal nodes projected on the surface</description>\r");
            buffer.append(groundSpadeBuffer);
            buffer.append("\t\t</Folder>\r");


            buffer.append("\t\t<Folder>\r");
            buffer.append("\t\t\t<name>diamond HPDs</name>\r");
            buffer.append("\t\t\t<description>longitude and latitude and altitude HPDs for internal nodes</description>\r");
            buffer.append(diamondBuffer);
            buffer.append("\t\t</Folder>\r");

        }

        buffer.append("\t\t</Folder>\r");

        buffer.append("</Document>\r");

        buffer.append("</kml>");


    }

    private static void appendSpade(StringBuffer buffer, RootedTree tree, Node node, int nodeNumber, double plotHeight, double mostRecentDate, String latitudeName, String longitudeName, boolean groundSpade, boolean initialVisibility) {

        int visibility;
        if (initialVisibility) {visibility = 1; } else { visibility = 0; }

        double scaleFactor = plotHeight/tree.getHeight(tree.getRootNode());
        Double longitude = (Double)node.getAttribute(longitudeName);
        Double latitude = (Double)node.getAttribute(latitudeName);
        double altitude = (tree.getHeight(node)*scaleFactor);
        String altitudeMode;
        if (groundSpade) {
            altitude = 0;
            altitudeMode = "clampToGround";
        } else {
            altitudeMode = "relativeToGround";
        }

        Object[] longitudeHPDs = (Object[])node.getAttribute(longitudeName+"_95%_HPD");
        Object[] latitudeHPDs = (Object[])node.getAttribute(latitudeName+"_95%_HPD");

        buffer.append("\t\t<Placemark>\r");

        buffer.append("\t\t\t<visibility>"+visibility+"</visibility>\r");

        buffer.append("\t\t<TimeSpan>\r");
        double date = mostRecentDate - tree.getHeight(node);
        String[] yearMonthDay = convertToYearMonthDay(date);
        buffer.append("\t\t\t<begin>"+yearMonthDay[0]+"-"+yearMonthDay[1]+"-"+yearMonthDay[2]+"</begin>\r");
        buffer.append("\t\t</TimeSpan>\r");

        if (groundSpade) {
            buffer.append("\t\t<styleUrl>#groundSpade"+nodeNumber+"_style</styleUrl>\r");
        }   else {
            buffer.append("\t\t<styleUrl>#spade"+nodeNumber+"_style</styleUrl>\r");
        }
        buffer.append("\t\t<Polygon>\r");
        buffer.append("\t\t\t<altitudeMode>"+altitudeMode+"</altitudeMode>\r");
        if (groundSpade) {
            buffer.append("\t\t\t<tessellate>1</tessellate>\r");
        }
        buffer.append("\t\t\t<outerBoundaryIs>\r");
        buffer.append("\t\t\t\t<LinearRing>\r");
        buffer.append("\t\t\t\t\t<coordinates>\r");

        buffer.append("\t\t\t\t\t"+longitude+","+latitudeHPDs[1]+","+altitude+"\r");
        buffer.append("\t\t\t\t\t"+longitudeHPDs[1]+","+latitude+","+altitude+"\r");
        buffer.append("\t\t\t\t\t"+longitude+","+latitudeHPDs[0]+","+altitude+"\r");
        buffer.append("\t\t\t\t\t"+longitudeHPDs[0]+","+latitude+","+altitude+"\r");
        buffer.append("\t\t\t\t\t"+longitude+","+latitudeHPDs[1]+","+altitude+"\r");

        buffer.append("\t\t\t\t\t</coordinates>\r");
        buffer.append("\t\t\t\t</LinearRing>\r");
        buffer.append("\t\t\t</outerBoundaryIs>\r");
        buffer.append("\t\t</Polygon>\r");
        buffer.append("\t\t</Placemark>\r");

    }

    private static void appendContour(StringBuffer buffer, RootedTree tree, Node node, int nodeNumber, double plotHeight, double mostRecentDate, String latLongName, String latitudeName, String longitudeName, boolean groundContour, boolean initialVisibility) {

        int visibility;
        if (initialVisibility) {visibility = 1; } else { visibility = 0; }

        double scaleFactor = plotHeight/tree.getHeight(tree.getRootNode());
        double altitude = (tree.getHeight(node)*scaleFactor);
        String altitudeMode;
        if (groundContour) {
            altitude = 0;
            altitudeMode = "clampToGround";
        } else {
            altitudeMode = "relativeToGround";
        }

        int modality = ((Integer)node.getAttribute(latLongName+"_95%HPD_modality")).intValue();

        for (int x = 0; x < modality; x++) {
            Object[] longitudeHPDs = (Object[])node.getAttribute(longitudeName+"_95%HPD_"+(x + 1));
            Object[] latitudeHPDs = (Object[])node.getAttribute(latitudeName+"_95%HPD_"+(x + 1));

            buffer.append("\t\t<Placemark>\r");

            buffer.append("\t\t\t<visibility>"+visibility+"</visibility>\r");

            buffer.append("\t\t<TimeSpan>\r");
            double date = mostRecentDate - tree.getHeight(node);
            String[] yearMonthDay = convertToYearMonthDay(date);
            buffer.append("\t\t\t<begin>"+yearMonthDay[0]+"-"+yearMonthDay[1]+"-"+yearMonthDay[2]+"</begin>\r");
            buffer.append("\t\t</TimeSpan>\r");

            if (groundContour) {
                buffer.append("\t\t<styleUrl>#groundSpade"+nodeNumber+"_style</styleUrl>\r");
            }   else {
                buffer.append("\t\t<styleUrl>#spade"+nodeNumber+"_style</styleUrl>\r");
            }
            buffer.append("\t\t<Polygon>\r");
            buffer.append("\t\t\t<altitudeMode>"+altitudeMode+"</altitudeMode>\r");
            if (groundContour) {
                buffer.append("\t\t\t<tessellate>1</tessellate>\r");
            }
            buffer.append("\t\t\t<outerBoundaryIs>\r");
            buffer.append("\t\t\t\t<LinearRing>\r");
            buffer.append("\t\t\t\t\t<coordinates>\r");

            for (int y = 0; y < longitudeHPDs.length; y++) {

                buffer.append("\t\t\t\t\t"+longitudeHPDs[y]+","+latitudeHPDs[y]+","+altitude+"\r");

            }

            buffer.append("\t\t\t\t\t</coordinates>\r");
            buffer.append("\t\t\t\t</LinearRing>\r");
            buffer.append("\t\t\t</outerBoundaryIs>\r");
            buffer.append("\t\t</Polygon>\r");
            buffer.append("\t\t</Placemark>\r");
        }
    }

    private static void appendDiamond(StringBuffer buffer, RootedTree tree, Node node, int nodeNumber, double plotHeight, String latitudeName, String longitudeName, boolean initialVisibility) {

        int visibility;
        if (initialVisibility) { visibility = 1; } else { visibility = 0; }

        double scaleFactor = plotHeight/tree.getHeight(tree.getRootNode());
        Double longitude = (Double)node.getAttribute(longitudeName);
        Double latitude = (Double)node.getAttribute(latitudeName);
        double altitude = (tree.getHeight(node)*scaleFactor);

        Object[] longitudeHPDs = (Object[])node.getAttribute(longitudeName+"_95%_HPD");
        Object[] latitudeHPDs = (Object[])node.getAttribute(latitudeName+"_95%_HPD");
        Object[] heightHPDs = (Object[])node.getAttribute("height_95%_HPD");

        double longitudeHPDlower = (Double)longitudeHPDs[0];
        double longitudeHPDupper = (Double)longitudeHPDs[1];

        double latitudeHPDlower = (Double)latitudeHPDs[0];
        double latitudeHPDupper = (Double)latitudeHPDs[1];

        double heightHPDlower = (Double)heightHPDs[0];
        double altitudeHPDlower = heightHPDlower*scaleFactor;
        double heightHPDupper = (Double)heightHPDs[1];
        double altitudeHPDupper = heightHPDupper* scaleFactor;

        buffer.append("\t\t<Folder>\r");
        buffer.append("\t\t\t<name>node"+nodeNumber+"_diamond</name>\r");

        buffer.append("\t\t\t<Placemark>\r");
        buffer.append("\t\t\t<visibility>"+visibility+"</visibility>\r");
        buffer.append("\t\t\t<styleUrl>#diamondStyle</styleUrl>\r");
        buffer.append("\t\t\t<Polygon>\r");
        buffer.append("\t\t\t\t<altitudeMode>relativeToGround</altitudeMode>\r");
        buffer.append("\t\t\t\t<outerBoundaryIs>\r");

        buffer.append("\t\t\t\t\t<LinearRing>\r");
        buffer.append("\t\t\t\t\t\t<coordinates>\r");

        buffer.append("\t\t\t\t\t\t"+longitude+","+latitude+","+altitudeHPDlower+"\r");
        buffer.append("\t\t\t\t\t\t"+longitudeHPDlower+","+latitudeHPDupper+","+altitude+"\r");
        buffer.append("\t\t\t\t\t\t"+longitudeHPDlower+","+latitudeHPDlower+","+altitude+"\r");
        buffer.append("\t\t\t\t\t\t"+longitude+","+latitude+","+altitudeHPDlower+"\r");

        buffer.append("\t\t\t\t\t\t</coordinates>\r");
        buffer.append("\t\t\t\t\t</LinearRing>\r");
        buffer.append("\t\t\t\t</outerBoundaryIs>\r");
        buffer.append("\t\t\t</Polygon>\r");
        buffer.append("\t\t\t</Placemark>\r");

        buffer.append("\t\t\t<Placemark>\r");
        buffer.append("\t\t\t<visibility>0</visibility>\r");
        buffer.append("\t\t\t<styleUrl>#diamondStyle</styleUrl>\r");
        buffer.append("\t\t\t<Polygon>\r");
        buffer.append("\t\t\t\t<altitudeMode>relativeToGround</altitudeMode>\r");
        buffer.append("\t\t\t\t<outerBoundaryIs>\r");

        buffer.append("\t\t\t\t\t<LinearRing>\r");
        buffer.append("\t\t\t\t\t\t<coordinates>\r");

        buffer.append("\t\t\t\t\t\t"+longitudeHPDlower+","+latitudeHPDupper+","+altitude+"\r");
        buffer.append("\t\t\t\t\t\t"+longitudeHPDlower+","+latitudeHPDlower+","+altitude+"\r");
        buffer.append("\t\t\t\t\t\t"+longitude+","+latitude+","+altitudeHPDupper+"\r");
        buffer.append("\t\t\t\t\t\t"+longitudeHPDlower+","+latitudeHPDupper+","+altitude+"\r");

        buffer.append("\t\t\t\t\t\t</coordinates>\r");
        buffer.append("\t\t\t\t\t</LinearRing>\r");
        buffer.append("\t\t\t\t</outerBoundaryIs>\r");
        buffer.append("\t\t\t</Polygon>\r");
        buffer.append("\t\t\t</Placemark>\r");

        buffer.append("\t\t\t<Placemark>\r");
        buffer.append("\t\t\t<visibility>0</visibility>\r");
        buffer.append("\t\t\t<styleUrl>#diamondStyle</styleUrl>\r");
        buffer.append("\t\t\t<Polygon>\r");
        buffer.append("\t\t\t\t<altitudeMode>relativeToGround</altitudeMode>\r");
        buffer.append("\t\t\t\t<outerBoundaryIs>\r");

        buffer.append("\t\t\t\t\t<LinearRing>\r");
        buffer.append("\t\t\t\t\t\t<coordinates>\r");

        buffer.append("\t\t\t\t\t\t"+longitudeHPDlower+","+latitudeHPDupper+","+altitude+"\r");
        buffer.append("\t\t\t\t\t\t"+longitude+","+latitude+","+altitudeHPDupper+"\r");
        buffer.append("\t\t\t\t\t\t"+longitudeHPDupper+","+latitudeHPDupper+","+altitude+"\r");
        buffer.append("\t\t\t\t\t\t"+longitudeHPDlower+","+latitudeHPDupper+","+altitude+"\r");

        buffer.append("\t\t\t\t\t\t</coordinates>\r");
        buffer.append("\t\t\t\t\t</LinearRing>\r");
        buffer.append("\t\t\t\t</outerBoundaryIs>\r");
        buffer.append("\t\t\t</Polygon>\r");
        buffer.append("\t\t\t</Placemark>\r");


        buffer.append("\t\t\t<Placemark>\r");
        buffer.append("\t\t\t<visibility>0</visibility>\r");
        buffer.append("\t\t\t<styleUrl>#diamondStyle</styleUrl>\r");
        buffer.append("\t\t\t<Polygon>\r");
        buffer.append("\t\t\t\t<altitudeMode>relativeToGround</altitudeMode>\r");
        buffer.append("\t\t\t\t<outerBoundaryIs>\r");

        buffer.append("\t\t\t\t\t<LinearRing>\r");
        buffer.append("\t\t\t\t\t\t<coordinates>\r");

        buffer.append("\t\t\t\t\t\t"+longitudeHPDlower+","+latitudeHPDupper+","+altitude+"\r");
        buffer.append("\t\t\t\t\t\t"+longitudeHPDupper+","+latitudeHPDupper+","+altitude+"\r");
        buffer.append("\t\t\t\t\t\t"+longitude+","+latitude+","+altitudeHPDlower+"\r");
        buffer.append("\t\t\t\t\t\t"+longitudeHPDlower+","+latitudeHPDupper+","+altitude+"\r");

        buffer.append("\t\t\t\t\t\t</coordinates>\r");
        buffer.append("\t\t\t\t\t</LinearRing>\r");
        buffer.append("\t\t\t\t</outerBoundaryIs>\r");
        buffer.append("\t\t\t</Polygon>\r");
        buffer.append("\t\t\t</Placemark>\r");

        buffer.append("\t\t\t<Placemark>\r");
        buffer.append("\t\t\t<visibility>0</visibility>\r");
        buffer.append("\t\t\t<styleUrl>#diamondStyle</styleUrl>\r");
        buffer.append("\t\t\t<Polygon>\r");
        buffer.append("\t\t\t\t<altitudeMode>relativeToGround</altitudeMode>\r");
        buffer.append("\t\t\t\t<outerBoundaryIs>\r");

        buffer.append("\t\t\t\t\t<LinearRing>\r");
        buffer.append("\t\t\t\t\t\t<coordinates>\r");

        buffer.append("\t\t\t\t\t\t"+longitudeHPDupper+","+latitudeHPDupper+","+altitude+"\r");
        buffer.append("\t\t\t\t\t\t"+longitudeHPDupper+","+latitudeHPDlower+","+altitude+"\r");
        buffer.append("\t\t\t\t\t\t"+longitude+","+latitude+","+altitudeHPDupper+"\r");
        buffer.append("\t\t\t\t\t\t"+longitudeHPDupper+","+latitudeHPDupper+","+altitude+"\r");

        buffer.append("\t\t\t\t\t\t</coordinates>\r");
        buffer.append("\t\t\t\t\t</LinearRing>\r");
        buffer.append("\t\t\t\t</outerBoundaryIs>\r");
        buffer.append("\t\t\t</Polygon>\r");
        buffer.append("\t\t\t</Placemark>\r");

        buffer.append("\t\t\t<Placemark>\r");
        buffer.append("\t\t\t<visibility>0</visibility>\r");
        buffer.append("\t\t\t<styleUrl>#diamondStyle</styleUrl>\r");
        buffer.append("\t\t\t<Polygon>\r");
        buffer.append("\t\t\t\t<altitudeMode>relativeToGround</altitudeMode>\r");
        buffer.append("\t\t\t\t<outerBoundaryIs>\r");

        buffer.append("\t\t\t\t\t<LinearRing>\r");
        buffer.append("\t\t\t\t\t\t<coordinates>\r");

        buffer.append("\t\t\t\t\t\t"+longitudeHPDupper+","+latitudeHPDupper+","+altitude+"\r");
        buffer.append("\t\t\t\t\t\t"+longitudeHPDupper+","+latitudeHPDlower+","+altitude+"\r");
        buffer.append("\t\t\t\t\t\t"+longitude+","+latitude+","+altitudeHPDlower+"\r");
        buffer.append("\t\t\t\t\t\t"+longitudeHPDupper+","+latitudeHPDupper+","+altitude+"\r");

        buffer.append("\t\t\t\t\t\t</coordinates>\r");
        buffer.append("\t\t\t\t\t</LinearRing>\r");
        buffer.append("\t\t\t\t</outerBoundaryIs>\r");
        buffer.append("\t\t\t</Polygon>\r");
        buffer.append("\t\t\t</Placemark>\r");

        buffer.append("\t\t\t<Placemark>\r");
        buffer.append("\t\t\t<visibility>0</visibility>\r");
        buffer.append("\t\t\t<styleUrl>#diamondStyle</styleUrl>\r");
        buffer.append("\t\t\t<Polygon>\r");
        buffer.append("\t\t\t\t<altitudeMode>relativeToGround</altitudeMode>\r");
        buffer.append("\t\t\t\t<outerBoundaryIs>\r");

        buffer.append("\t\t\t\t\t<LinearRing>\r");
        buffer.append("\t\t\t\t\t\t<coordinates>\r");

        buffer.append("\t\t\t\t\t\t"+longitudeHPDlower+","+latitudeHPDlower+","+altitude+"\r");
        buffer.append("\t\t\t\t\t\t"+longitudeHPDupper+","+latitudeHPDlower+","+altitude+"\r");
        buffer.append("\t\t\t\t\t\t"+longitude+","+latitude+","+altitudeHPDupper+"\r");
        buffer.append("\t\t\t\t\t\t"+longitudeHPDlower+","+latitudeHPDlower+","+altitude+"\r");

        buffer.append("\t\t\t\t\t\t</coordinates>\r");
        buffer.append("\t\t\t\t\t</LinearRing>\r");
        buffer.append("\t\t\t\t</outerBoundaryIs>\r");
        buffer.append("\t\t\t</Polygon>\r");
        buffer.append("\t\t\t</Placemark>\r");

        buffer.append("\t\t\t<Placemark>\r");
        buffer.append("\t\t\t<visibility>0</visibility>\r");
        buffer.append("\t\t\t<styleUrl>#diamondStyle</styleUrl>\r");
        buffer.append("\t\t\t<Polygon>\r");
        buffer.append("\t\t\t\t<altitudeMode>relativeToGround</altitudeMode>\r");
        buffer.append("\t\t\t\t<outerBoundaryIs>\r");

        buffer.append("\t\t\t\t\t<LinearRing>\r");
        buffer.append("\t\t\t\t\t\t<coordinates>\r");

        buffer.append("\t\t\t\t\t"+longitudeHPDlower+","+latitudeHPDlower+","+altitude+"\r");
        buffer.append("\t\t\t\t\t"+longitudeHPDupper+","+latitudeHPDlower+","+altitude+"\r");
        buffer.append("\t\t\t\t\t"+longitude+","+latitude+","+altitudeHPDlower+"\r");
        buffer.append("\t\t\t\t\t"+longitudeHPDlower+","+latitudeHPDlower+","+altitude+"\r");

        buffer.append("\t\t\t\t\t\t</coordinates>\r");
        buffer.append("\t\t\t\t\t</LinearRing>\r");
        buffer.append("\t\t\t\t</outerBoundaryIs>\r");
        buffer.append("\t\t\t</Polygon>\r");
        buffer.append("\t\t\t</Placemark>\r");

        buffer.append("\t\t</Folder>\r");

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