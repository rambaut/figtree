/*
 * DiversityContinuousColourDecorator.java
 *
 * Copyright (C) 2006-2014 Andrew Rambaut
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package figtree.treeviewer.decorators;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;
import jebl.util.Attributable;

import java.awt.*;

/**
 * This draws a continuous colour spectrum to show diversity across the tree.
 *
 * @author Andrew Rambaut
 * @version $Id$
 *
 * $HeadURL$
 *
 * $LastChangedBy$
 * $LastChangedDate$
 * $LastChangedRevision$
 */
public class DiversityContinuousColourDecorator extends ColourDecorator {

    public DiversityContinuousColourDecorator() throws NumberFormatException {
        super(null);
    }

    public DiversityContinuousColourDecorator(String attribute, String settings) {
        super(attribute);

        setup(settings);
    }

    public void setup(String settings) {
        if (!settings.startsWith("{") || !settings.endsWith("}")) {
            throw new IllegalArgumentException("DiversityContinuousColourDecorator settings string not in correct format");
        }

        String[] parts1 = settings.substring(1, settings.length() - 1).split("}[, ]+");
        if (parts1.length != 2) {
            throw new IllegalArgumentException("DiversityContinuousColourDecorator settings string not in correct format");
        }
        String[] parts2 = parts1[1].split("[, ]+");
        if (parts2.length != 7) {
            throw new IllegalArgumentException("DiversityContinuousColourDecorator settings string not in correct format");
        }

        try {
            hueLower = Float.parseFloat(parts2[0]);
            hueUpper = Float.parseFloat(parts2[1]);
            saturationLower = Float.parseFloat(parts2[2]);
            saturationUpper = Float.parseFloat(parts2[3]);
            brightnessLower = Float.parseFloat(parts2[4]);
            brightnessUpper = Float.parseFloat(parts2[5]);
            reverseHue = Boolean.parseBoolean(parts2[6]);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("HSBContinuousColourDecorator settings string not in correct format");
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("HSBContinuousColourDecorator settings string not in correct format");
        }
    }

    public void setup(float hueUpper, float hueLower,
                      float saturationUpper, float saturationLower,
                      float brightnessUpper, float brightnessLower,
                      boolean reverseHue) {
        this.hueUpper = hueUpper;
        this.hueLower = hueLower;
        this.saturationUpper = saturationUpper;
        this.saturationLower = saturationLower;
        this.brightnessUpper = brightnessUpper;
        this.brightnessLower = brightnessLower;
        this.reverseHue = reverseHue;
    }

    public void setTree(RootedTree tree) {
        this.tree = tree;
        double[] maxValue = { 0 };
        traverseTree((RootedTree)tree, ((RootedTree) tree).getRootNode(), 0.0, 1.0, maxValue);
        for (Node node : tree.getNodes()) {
            double value = (Double)node.getAttribute("@hue");
            node.setAttribute("@hue", value / maxValue[0]);
        }
    }

    private double traverseTree(RootedTree tree, Node node, double lower, double upper, double[] maxValue) {
        double value;

        if (tree.isExternal(node)) {
            value = (upper + lower) / 2;
        } else {
            value = 0.0;
            double range = upper - lower;

            int n = tree.getChildren(node).size();
            double[] counts = new double[n];

            int i = 0;
            double count = 0.0;
            for (Node child : tree.getChildren(node)) {
                double c = countTips(tree, child);
                count += c;
                counts[i] = c;
                i++;
            }

            double l = lower;
            i = 0;
            for (Node child : tree.getChildren(node)) {
                double prop = range * (counts[i] / count);
                double u = l + prop;
                value += traverseTree(tree, child, l, u, maxValue);
                l = u;
                i++;
            }
            value /= n;
        }

//        value *= tree.getLength(node);
        node.setAttribute("@hue", value);

        if (value > maxValue[0]) {
            maxValue[0] = value;
        }

        return value;
    }

    private double countTips(RootedTree tree, Node node) {
        double count;

        if (tree.isExternal(node)) {
            count = 1;
        } else {
            count = 0;
            for (Node child : tree.getChildren(node)) {
                count += countTips(tree, child);
            }
        }

        return count;
    }

    @Override
    public void setItem(Object item) {
        if (item instanceof Attributable) {
            hue = (Double)((Attributable) item).getAttribute("@hue");

            paint = getColourForValue(item);
        }
    }

    @Override
    protected Color getColourForValue(Object value) {
        return Color.getHSBColor(getHue((float)hue), getSaturation((float)0.5), getBrightness((float)0.5));
    }

    private float getHue(float value) {
        if (reverseHue) {
            return hueUpper - ((hueUpper - hueLower) * value);
        }

        return ((hueUpper - hueLower) * value) + hueLower;
    }

    private float getSaturation(float value) {
        return ((saturationUpper - saturationLower) * value) + saturationLower;
    }

    private float getBrightness(float value) {
        return ((brightnessUpper - brightnessLower) * value) + brightnessLower;
    }

    public float getHueUpper() {
        return hueUpper;
    }

    public void setHueUpper(float hueUpper) {
        this.hueUpper = hueUpper;
    }

    public float getHueLower() {
        return hueLower;
    }

    public void setHueLower(float hueLower) {
        this.hueLower = hueLower;
    }

    public float getSaturationUpper() {
        return saturationUpper;
    }

    public void setSaturationUpper(float saturationUpper) {
        this.saturationUpper = saturationUpper;
    }

    public float getSaturationLower() {
        return saturationLower;
    }

    public void setSaturationLower(float saturationLower) {
        this.saturationLower = saturationLower;
    }

    public float getBrightnessUpper() {
        return brightnessUpper;
    }

    public void setBrightnessUpper(float brightnessUpper) {
        this.brightnessUpper = brightnessUpper;
    }

    public float getBrightnessLower() {
        return brightnessLower;
    }

    public void setBrightnessLower(float brightnessLower) {
        this.brightnessLower = brightnessLower;
    }

    public boolean isReverseHue() {
        return reverseHue;
    }

    public void setReverseHue(boolean reverseHue) {
        this.reverseHue = reverseHue;
    }

    /**
     * Create a string representation suitable for writing to a text file
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(hueLower);
        sb.append(",");
        sb.append(hueUpper);
        sb.append(",");
        sb.append(saturationLower);
        sb.append(",");
        sb.append(saturationUpper);
        sb.append(",");
        sb.append(brightnessLower);
        sb.append(",");
        sb.append(brightnessUpper);
        sb.append(",");
        sb.append(reverseHue);
        sb.append("}");
        return sb.toString();
    }

    private RootedTree tree;
    private double hue;

    private float hueUpper = 1.0F;
    private float hueLower = 0.0F;
    private float saturationUpper = 0.6F;
    private float saturationLower = 0.6F;
    private float brightnessUpper = 0.8F;
    private float brightnessLower = 0.4F;

    private boolean reverseHue = false;

}
