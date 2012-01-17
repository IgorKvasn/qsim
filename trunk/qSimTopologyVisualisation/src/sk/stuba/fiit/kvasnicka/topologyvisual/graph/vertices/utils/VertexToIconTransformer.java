package sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.utils;

import edu.uci.ics.jung.visualization.decorators.DefaultVertexIconTransformer;

import javax.swing.Icon;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;

/**
 * User: Igor Kvasnicka
 * Date: 9/2/11
 * Time: 2:20 PM
 */
public class VertexToIconTransformer<V extends TopologyVertex> extends DefaultVertexIconTransformer<V> {

    @Override
    public Icon transform(V v) {
        return v.getIcon();
    }
}
