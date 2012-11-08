/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.transformation;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.dto.TopologyVertexSerialization;

/**
 *
 * @author Igor Kvasnicka
 */
public abstract class TopologyVertexToVertexXmlTransformation {

    public static TopologyVertexSerialization transformToSerializable(TopologyVertex vertex, AbstractLayout<TopologyVertex, TopologyEdge> layout) {
        if (layout == null) {
            throw new IllegalArgumentException("layout is NULL");
        }
        return new TopologyVertexSerialization(vertex, layout.getX(vertex), layout.getY(vertex));
    }
}
