/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;

/**
 * This is an util class providing methods to manipulate with vertices.
 *
 * @author Igor Kvasnicka
 */
public abstract class VerticesUtil {

    public static final Color SELECTED_COLOR = Color.GREEN;
    public static final Color CHECKED_COLOR = Color.ORANGE;
    private static final int ALPHA = 0;
    private static final int RED = 1;
    private static final int GREEN = 2;
    private static final int BLUE = 3;
    private static final int HUE = 0;
    private static final int SATURATION = 1;
    private static final int BRIGHTNESS = 2;
    private static final int TRANSPARENT = 0;
    private static final Color COLOR_TO_REPLACE = new Color(178, 186, 157);
    private static final Color TRANSPARENT_COLOR = new Color(178, 186, 157, 0);

    /**
     * converts collection of TopologyVertex objects to a list of NetworkNodes
     *
     * @param col
     * @return
     */
    public static List<NetworkNode> convertTopologyVertexList2NetworkNodeList(Collection<TopologyVertex> col) {
        List<NetworkNode> list = new LinkedList<NetworkNode>();
        if (col == null) {
            return list;
        }
        for (TopologyVertex v : col) {
            list.add(v.getDataModel());
        }
        return list;
    }

    /**
     * retrieves names of all selected vertices
     *
     * @return
     */
    public static String getVerticesNames(Collection<TopologyVertex> vertices) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (TopologyVertex v : vertices) {
            if (i == vertices.size() - 1) {
                sb.append(v.getName());
                break;
            }
            i++;
            sb.append(v.getName()).append(", ");
        }
        return sb.toString();
    }

    /**
     * Changes color of image's border to specified color. All occurrence of
     * color RGB 178, 186, 157 will be replaced with new one.
     *
     * @param image image to deal with
     * @param replacement new color; null if new color is transparent
     * @return
     */
    public static BufferedImage changeColor(BufferedImage image, Color replacement) {
        if (replacement == null) {
            return changeColor(image, COLOR_TO_REPLACE, TRANSPARENT_COLOR);
        } else {
            return changeColor(image, COLOR_TO_REPLACE, replacement);
        }
    }

    /**
     * Changes color of image's border to specified color.
     *
     * @param image image to deal with
     * @param replacement new color
     * @param color to be replaced
     * @return
     */
    public static BufferedImage changeColor(BufferedImage image, Color original, Color replacement) {
        BufferedImage destImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = destImage.createGraphics();
        g.drawImage(image, null, 0, 0);
        g.dispose();

        int replacementRGB = replacement.getRGB();
        int originalRGB = original.getRGB();

        for (int i = 0; i < destImage.getWidth(); i++) {
            for (int j = 0; j < destImage.getHeight(); j++) {

                int destRGB = destImage.getRGB(i, j);

                if (matches(originalRGB, destRGB)) {
                    destImage.setRGB(i, j, replacementRGB);
                }
            }
        }

        return destImage;
    }

    private static int getNewPixelRGB(float[] replHSB, int destRGB) {
        float[] destHSB = getHSBArray(destRGB);

        return Color.HSBtoRGB(replHSB[HUE], replHSB[SATURATION], destHSB[BRIGHTNESS]);
    }

    private static boolean matches(int originalRGB, int destRGB) {
        float[] hsbMask = getHSBArray(originalRGB);
        float[] hsbDest = getHSBArray(destRGB);

        if (hsbMask[HUE] == hsbDest[HUE] && hsbMask[SATURATION] == hsbDest[SATURATION] && getRGBArray(destRGB)[ALPHA] != TRANSPARENT) {

            return true;
        }
        return false;
    }

    private static int[] getRGBArray(int rgb) {
        return new int[]{(rgb >> 24) & 0xff, (rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff};
    }

    private static float[] getHSBArray(int rgb) {
        int[] rgbArr = getRGBArray(rgb);
        return Color.RGBtoHSB(rgbArr[RED], rgbArr[GREEN], rgbArr[BLUE], null);
    }
}
