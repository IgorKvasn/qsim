package sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import sk.stuba.fiit.kvasnicka.topologyvisual.data.Edge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;

/**
 * abstract class for all edge classes
 *
 * @author Igor Kvasnicka
 */
//@EqualsAndHashCode(of = {"vertex1", "vertex2"})
public class TopologyEdge {

    @Getter
    private Edge edge;
    @Getter
    private TopologyVertex vertex1, vertex2;

    /**
     * used only during deserialisation - if you use this constructor, do not
     * forget to initialise vertex1 and vertex2
     *
     * @param edge
     */
    public TopologyEdge(Edge edge) {
        this.edge = edge;
    }

    /**
     * creates new instance
     *
     * @param edge edge object that is being rendered
     */
    public TopologyEdge(Edge edge, TopologyVertex vertex1, TopologyVertex vertex2) {
        this.edge = edge;
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
    }

    public void setVertices(TopologyVertex vertex1, TopologyVertex vertex2) {
        if (this.vertex1 != null) {
            throw new IllegalStateException("vertex1 is already initialised - you have misused this method");
        }
        if (this.vertex2 != null) {
            throw new IllegalStateException("vertex2 is already initialised - you have misused this method");
        }
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
    }
}
