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
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import sk.stuba.fiit.kvasnicka.topologyvisual.PreferenciesHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.TopologyVertexFactory;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.SimulationData;

/**
 *
 * @author Igor Kvasnicka
 */
@Getter
@Setter
public class DeserialisationResult {

    private AbstractGraph<TopologyVertex, TopologyEdge> g;
    private AbstractLayout<TopologyVertex, TopologyEdge> layout;
    private String name, description;
    private boolean distanceVectorRouting;
    private TopologyVertexFactory vertexFactory;
    private List<SimulationData.Data> simulRulesData;

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
    public DeserialisationResult(AbstractGraph<TopologyVertex, TopologyEdge> g, StaticLayout<TopologyVertex, TopologyEdge> layout, TopologyVertexFactory vertexFactory, Map<TopologyVertex, Point2D> vertexLocationMap, String name, String description, boolean distanceVectorRouting, List<SimulationData.Data> simulRulesData) {
        this.name = name;
        this.description = description;
        this.g = g;
        this.layout = layout;
        this.distanceVectorRouting = distanceVectorRouting;
        this.vertexFactory = vertexFactory;
        this.simulRulesData = simulRulesData;
    }

    /**
     * used when no JUNG data is provided
     *
     * @param name
     * @param description
     */
    public DeserialisationResult(String name, String description, boolean distanceVectorRouting) {
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
        return g != null && vertexFactory != null;
    }
}