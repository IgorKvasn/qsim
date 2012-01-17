package sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.utils;

import edu.uci.ics.jung.visualization.FourPassImageShaper;
import edu.uci.ics.jung.visualization.LayeredIcon;
import edu.uci.ics.jung.visualization.decorators.VertexIconShapeTransformer;
import org.apache.log4j.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;

/**
 * @author Igor Kvasnicka
 */
public class MyVertexIconShapeTransformer<V extends TopologyVertex> extends VertexIconShapeTransformer<V> {
    private static Logger logg = Logger.getLogger(MyVertexIconShapeTransformer.class);

    /**
     * creates new instance
     */
    public MyVertexIconShapeTransformer() {
        super(null);
    }

    @Override
    public Shape transform(V v) {
        if (v == null) {
            logg.warn("vertex is null - this should not happen");
            return null;
        }
        Icon icon =  v.getIcon();

        if (icon != null && icon instanceof LayeredIcon) {
            Image image = ((ImageIcon) icon).getImage();
            Shape shape = shapeMap.get(image);
            if (shape == null) {
                shape = FourPassImageShaper.getShape(image, 30);
                if (shape.getBounds().getWidth() > 0 && shape.getBounds().getHeight() > 0) {
                    // don't cache a zero-sized shape, wait for the image
                    // to be ready
                    int width = image.getWidth(null);
                    int height = image.getHeight(null);
                    AffineTransform transform = AffineTransform.getTranslateInstance(- width / 2, - height / 2);
                    shape = transform.createTransformedShape(shape);
                    shapeMap.put(image, shape);
                }
            }
            return shape;
        }
        return null;
    }
}
