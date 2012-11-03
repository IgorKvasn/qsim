/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.transformation;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.ComputerVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.RouterVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.SwitchVertex;
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
        return new TopologyVertexSerialization(vertex.getDataModel(), layout.getX(vertex), layout.getY(vertex), vertex.getImageType());
    }

    public static TopologyVertex transFormSerializable(TopologyVertexSerialization vertex,AbstractLayout<TopologyVertex, TopologyEdge> layout) {
        if (layout == null) {
            throw new IllegalArgumentException("layout is NULL");
        }
        TopologyVertex v = null;

        switch (vertex.getImageType()) {
            case TOPOLOGY_VERTEX_COMPUTER:
                v = new ComputerVertex(vertex.getNode());
                break;
            case TOPOLOGY_VERTEX_ROUTER:
                v = new RouterVertex(vertex.getNode());
                break;
            case TOPOLOGY_VERTEX_SWITCH:
                v = new SwitchVertex(vertex.getNode());
                break;
            default:
                throw new IllegalArgumentException("unknown vertex type for image: " + vertex.getImageType());
        }
        layout.setLocation(v, vertex.getX(), vertex.getY());
        return v;
    }
}
