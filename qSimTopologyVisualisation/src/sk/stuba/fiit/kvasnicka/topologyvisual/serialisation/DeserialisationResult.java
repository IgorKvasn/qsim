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
package sk.stuba.fiit.kvasnicka.topologyvisual.serialisation;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.AbstractGraph;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import sk.stuba.fiit.kvasnicka.topologyvisual.PreferenciesHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.TopologyVertexFactory;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;

/**
 *
 * @author Igor Kvasnicka
 */
@Getter
@Setter
public class DeserialisationResult {

    private AbstractGraph<TopologyVertex, TopologyEdge> g;
    private AbstractLayout<TopologyVertex, TopologyEdge> layout;
    private TopologyVertexFactory vFactory;
    private String name, description;
    private boolean distanceVectorRouting;

    /**
     * empty constructor used when it is a new file
     */
    public DeserialisationResult() {
        distanceVectorRouting = PreferenciesHelper.isRoutingDistanceProtocol();
    }

    /**
     * this constructor is used during deserialisation
     *
     * @param g
     * @param vFactory
     * @param name
     * @param description
     */
    public DeserialisationResult(AbstractGraph<TopologyVertex, TopologyEdge> g, TopologyVertexFactory vFactory, Map<TopologyVertex, Point2D> vertexLocationMap, String name, String description, boolean distanceVectorRouting) {
        this.name = name;
        this.description = description;
        this.vFactory = vFactory;
        this.g = g;
        layout = new StaticLayout<TopologyVertex, TopologyEdge>(g);

        for (TopologyVertex v : vertexLocationMap.keySet()) {
            layout.setLocation(v, vertexLocationMap.get(v));
        }

        normalizeEdges(g.getEdges());
        this.distanceVectorRouting = distanceVectorRouting;
    }

    /**
     * used when no JUNG data is provided
     *
     * @param name
     * @param description
     */
    public DeserialisationResult(String name, String description,boolean distanceVectorRouting) {
        this.name = name;
        this.description = description;
        this.distanceVectorRouting = distanceVectorRouting;
    }

    /**
     * tells if JUNG graph was deserialised
     *
     * @return
     */
    public boolean isJungLoaded() {
        return g != null && vFactory != null;
    }

    /**
     * when TopologyEdge was created, special constructor was used. how it is
     * time to init fields that has been forgotten - vertex1 and vertex2
     *
     * @param edges
     */
    private void normalizeEdges(Collection<TopologyEdge> edges) {
        for (TopologyEdge edge : edges) {
            String v1 = edge.getEdge().getNode1().getName();
            String v2 = edge.getEdge().getNode2().getName();

            edge.setVertices(findTopologyVertex(v1), findTopologyVertex(v2));
        }
    }

    private TopologyVertex findTopologyVertex(String name) {
        Collection<TopologyVertex> vertices = g.getVertices();
        for (TopologyVertex v : vertices) {
            if (v.getName().equals(name)) {
                return v;
            }
        }
        throw new IllegalStateException("Unknown vertex with name: " + name);
    }

    public static class EdgeDescriptor {

        private String vertex1, vertex2;

        public EdgeDescriptor(String vertex1, String vertex2) {
            this.vertex1 = vertex1;
            this.vertex2 = vertex2;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final EdgeDescriptor other = (EdgeDescriptor) obj;
            if ((this.vertex1 == null) ? (other.vertex1 != null) : !this.vertex1.equals(other.vertex1)) {
                return false;
            }
            if ((this.vertex2 == null) ? (other.vertex2 != null) : !this.vertex2.equals(other.vertex2)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 41 * hash + (this.vertex1 != null ? this.vertex1.hashCode() : 0);
            hash = 41 * hash + (this.vertex2 != null ? this.vertex2.hashCode() : 0);
            return hash;
        }
    }
}