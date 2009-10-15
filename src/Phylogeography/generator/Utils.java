package phylogeography.generator;

import jebl.evolution.graphs.Node;

import java.awt.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class Utils {
    public static String getKMLDate(double fractionalDate) {

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


      public static int getIntegerNodeAttribute(Node node, String attributeName) {
          if (node.getAttribute(attributeName) == null) {
              throw new RuntimeException("Attribute, " + attributeName + ", missing from node");
          }
          return (Integer)node.getAttribute(attributeName);
      }

      public static int getIntegerNodeAttribute(Node node, String attributeName, int defaultValue) {
          if (node.getAttribute(attributeName) == null) {
              return defaultValue;
          }
          return (Integer)node.getAttribute(attributeName);
      }

      public static double getDoubleNodeAttribute(Node node, String attributeName) {
          if (node.getAttribute(attributeName) == null) {
              throw new RuntimeException("Attribute, " + attributeName + ", missing from node");
          }
          return (Double)node.getAttribute(attributeName);
      }

      public static double getDoubleNodeAttribute(Node node, String attributeName, double defaultValue) {
          if (node.getAttribute(attributeName) == null) {
              return defaultValue;
          }
          return (Double)node.getAttribute(attributeName);
      }

      public static Object[] getArrayNodeAttribute(Node node, String attributeName) {
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

      public static Color getBlendedColor(float proportion, Color startColor, Color endColor) {
          proportion = Math.max(proportion, 0.0F);
          proportion = Math.min(proportion, 1.0F);
          float[] start = startColor.getRGBColorComponents(null);
          float[] end = endColor.getRGBColorComponents(null);

          float[] color = new float[start.length];
          for (int i = 0; i < start.length; i++) {
              color[i] = start[i] + ((end[i] - start[i]) * proportion);
          }

          return new Color(color[0], color[1], color[2]);
      }

}
