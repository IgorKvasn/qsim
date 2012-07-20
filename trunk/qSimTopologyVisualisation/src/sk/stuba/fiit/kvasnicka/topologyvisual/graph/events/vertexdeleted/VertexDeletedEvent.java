/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.vertexdeleted;

import java.util.Arrays;
import java.util.Collection;
import java.util.EventObject;
import lombok.Getter;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;

/**
 *
 * @author Igor Kvasnicka
 */
public class VertexDeletedEvent extends EventObject {

    @Getter
    private final Collection<TopologyVertex> deletedVertex;

    public VertexDeletedEvent(Object source, TopologyVertex deletedVertex) {
        super(source);
        this.deletedVertex = Arrays.asList(deletedVertex);
    }

    public VertexDeletedEvent(Object source, Collection<TopologyVertex> deletedVertex) {
        super(source);
        this.deletedVertex = deletedVertex;
    }
}
