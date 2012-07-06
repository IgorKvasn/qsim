package sk.stuba.fiit.kvasnicka.topologyvisual.resources;

import edu.uci.ics.jung.visualization.LayeredIcon;
import java.awt.*;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.net.URL;
import java.util.HashMap;
import lombok.EqualsAndHashCode;
import org.openide.util.ImageUtilities;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.VerticesUtil;

/**
 * images are cached, so that when image is to be loaded, previously loaded
 * image is being used this is meant to save some heap space, but in fact I
 * doubt it is a significant save
 *
 * User: Igor Kvasnicka Date: 9/2/11 Time: 12:28 PM
 */
public class ImageResourceHelper {

    private static HashMap<ImageKey, ImageIcon> cache = new HashMap<ImageKey, ImageIcon>();

    /**
     * loads image from jar depending on path specified as parameter this image
     * will be used to display vertex
     * <p/>
     * this method uses cache, so that vertex image is loaded into memory just
     * once
     * <p/>
     * <b>Note: this method does not scale image</b>
     *
     * @param imgType image to load
     * @return Image instance
     * @param border color of a border to be applied; null if no border
     */
    public static Icon loadImageVertex(ImageType imgType, Color border) {
        return new LayeredIcon(loadImageVertexAsImage(imgType, border));
    }

    /**
     * loads image and returns its representation as Image object
     *
     * @param imgType
     * @param border color of the border; null if no border should be used
     * @return
     */
    public static Image loadImageVertexAsImage(ImageType imgType, Color border) {

        String imgPath = imgType.getResourcePath();


        ImageKey imageKey = new ImageKey(imgPath, true, border, 0, 0);
        if (!cache.containsKey(imageKey)) {
            cache.put(imageKey, new ImageIcon(VerticesUtil.changeColor(toBufferedImage(loadImage(imgType).getImage()), border)));
        }
        return cache.get(imageKey).getImage();
    }

    // This method returns a buffered image with the contents of an image
    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();

        // Determine if the image has transparent pixels; for this method's
        // implementation, see Determining If an Image Has Transparent Pixels
        boolean hasAlpha = hasAlpha(image);

        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
            if (hasAlpha) {
                transparency = Transparency.BITMASK;
            }

            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(
                    image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }

        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha) {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }

        // Copy image to buffered image
        Graphics g = bimage.createGraphics();

        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bimage;
    }

    public static boolean hasAlpha(Image image) {
        // If buffered image, the color model is readily available
        if (image instanceof BufferedImage) {
            BufferedImage bimage = (BufferedImage) image;
            return bimage.getColorModel().hasAlpha();
        }

        // Use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
        PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
        }

        // Get the image's color model
        ColorModel cm = pg.getColorModel();
        return cm.hasAlpha();
    }

    /**
     * loads image from jar depending on path specified as parameter
     * <p/>
     * <b>note:</b> this method is not cached
     *
     * @param imgType image to load
     * @return Image instance
     */
    public static ImageIcon loadImage(ImageType imgType) {
        return loadImage(imgType.getResourcePath());
    }

    /**
     * loads image from jar depending on path specified as parameter
     * <p/>
     * <b>note:</b> this method is not cached
     *
     * @param resourcePath path to the image inside of jar
     * @return Image instance
     */
    public static ImageIcon loadImage(String resourcePath) {
        if (resourcePath == null) {
            throw new IllegalArgumentException("resourcePath is NULL");
        }
        URL url = ImageResourceHelper.class.getResource(resourcePath);
        if (url == null) {
            throw new IllegalStateException("resource file " + resourcePath + " not found");
        }
        return new ImageIcon(url);
    }

    /**
     * loads image from jar depending on path specified as parameter
     *
     * @param resourcePath path to the image inside of jar
     * @param width width of loaded image
     * @param height height of loaded image
     * @return Image image instance
     */
    public static ImageIcon loadImage(String resourcePath, int width, int height) {
        if (resourcePath == null) {
            throw new IllegalArgumentException("resourcePath is NULL");
        }
        return scale(loadImage(resourcePath), width, height);
    }

    private static ImageIcon scale(ImageIcon original, int width, int height) {
        if (original == null) {
            throw new IllegalArgumentException("original image is NULL");
        }
        Image img = original.getImage();
        Image newimg = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(newimg);
    }

    @EqualsAndHashCode
    private static class ImageKey {

        private boolean originalSize;
        private int width;
        private int height;
        private String resourcePath;
        private Color border;

        private ImageKey(String resourcePath, boolean originalSize, Color border, int width, int height) {
            this.originalSize = originalSize;
            this.border = border;
            this.width = width;
            this.height = height;
            this.resourcePath = resourcePath;
        }
    }
}
