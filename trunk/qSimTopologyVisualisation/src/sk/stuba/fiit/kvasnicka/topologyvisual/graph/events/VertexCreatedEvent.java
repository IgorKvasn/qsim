/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.graph.events;

import java.util.EventObject;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;

/**
 *
 * @author Igor Kvasnicka
 */
public class VertexCreatedEvent extends EventObject {

    private final TopologyVertex newVertex;

    public VertexCreatedEvent(Object source, TopologyVertex newVertex) {
        super(source);
        this.newVertex = newVertex;
    }

    public TopologyVertex getNewVertex() {
        return newVertex;
    }
}
