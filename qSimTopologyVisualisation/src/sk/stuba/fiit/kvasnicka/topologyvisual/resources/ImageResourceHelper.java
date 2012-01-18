package sk.stuba.fiit.kvasnicka.topologyvisual.resources;

import edu.uci.ics.jung.visualization.LayeredIcon;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.net.URL;
import java.util.HashMap;

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
     */
    public static Icon loadImageVertex(ImageType imgType, boolean selected) {
        return new LayeredIcon(loadImageVertexAsImage(imgType, selected));
    }

    public static Image loadImageVertexAsImage(ImageType imgType, boolean selected) {
        String imgPath = selected ? imgType.getSelectePath() : imgType.getResourcePath();
        ImageKey imageKey = new ImageKey(imgPath, true, 0, 0);
        if (!cache.containsKey(imageKey)) {
            cache.put(imageKey, loadImage(imgPath));
        }
        return cache.get(imageKey).getImage();
    }

    public static Image loadCheckedImageVertexAsImage(ImageType imgType) {
        String imgPath = imgType.getCheckedPath();
        ImageKey imageKey = new ImageKey(imgPath, true, 0, 0);
        if (!cache.containsKey(imageKey)) {
            cache.put(imageKey, loadImage(imgPath));
        }
        return cache.get(imageKey).getImage();
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
            throw new NullPointerException("resource file not found");
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

    private static class ImageKey {

        private boolean originalSize;
        private int width;
        private int height;
        private String resourcePath;

        private ImageKey(String resourcePath, boolean originalSize, int width, int height) {
            this.originalSize = originalSize;
            this.width = width;
            this.height = height;
            this.resourcePath = resourcePath;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ImageKey imageKey = (ImageKey) o;

            if (height != imageKey.height) {
                return false;
            }
            if (originalSize != imageKey.originalSize) {
                return false;
            }
            if (width != imageKey.width) {
                return false;
            }
            if (!resourcePath.equals(imageKey.resourcePath)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = (originalSize ? 1 : 0);
            result = 31 * result + width;
            result = 31 * result + height;
            result = 31 * result + resourcePath.hashCode();
            return result;
        }
    }
}
