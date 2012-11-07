/*
 * This file is part of qSim.
 *
 * qSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * qSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with qSim.  If not, see <http://www.gnu.org/licenses/>.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges;

import lombok.Getter;
import lombok.Setter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;

/**
 * class that represents edge in the graphical topology
 *
 * @author Igor Kvasnicka
 */
//@EqualsAndHashCode(of = {"vertex1", "vertex2"})
public class TopologyEdge {

    @Getter
    @Setter
    private Edge edge;
    @Getter
    @Setter
    private TopologyVertex vertex1, vertex2;

    /**
     * used only during deserialisation - if you use this constructor, do not
     * forget to initialise vertex1 and vertex2 afterwards
     *
     * @param edge
     */
    public TopologyEdge(Edge edge) {
        this.edge = edge;//fixme init vertex1 and vertex2
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
