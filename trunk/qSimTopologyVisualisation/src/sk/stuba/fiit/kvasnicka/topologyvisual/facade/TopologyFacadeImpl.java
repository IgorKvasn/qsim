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
package sk.stuba.fiit.kvasnicka.topologyvisual.facade;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.AbstractGraph;
import java.util.Collection;
import java.util.List;
import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;
import org.openide.util.lookup.ServiceProvider;
import sk.stuba.fiit.kvasnicka.topologyvisual.PreferenciesHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.Topology;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.TopologyVertexFactory;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.ComputerVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.RouterVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.SwitchVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;

/**
 * @see TopologyFacade
 * @author Igor Kvasnicka
 */
@ServiceProvider(service = TopologyFacade.class)
public class TopologyFacadeImpl implements TopologyFacade {

    private static Logger logg = Logger.getLogger(TopologyFacadeImpl.class);
    private DijkstraShortestPath<TopologyVertex, TopologyEdge> dijkstra;

    @Override
    public Collection<TopologyVertex> getNeighbours(Topology topology, TopologyVertex vertex) {
        return topology.getG().getNeighbors(vertex);
    }

    @Override
    public List<TopologyEdge> findShortestPath( AbstractGraph<TopologyVertex,TopologyEdge> graph, TopologyVertex begin, TopologyVertex end, boolean distanceVector) {
        if (distanceVector) {//unweight dijkstra = distance vector routing protocol
            logg.debug("findShortestPath - unweight = distance vector");
            dijkstra = new DijkstraShortestPath(graph);
        } else {
            logg.debug("findShortestPath - weight = link state");
            dijkstra = new DijkstraShortestPath(graph, new Transformer<TopologyEdge, Double>() {

                @Override
                public Double transform(TopologyEdge edge) {
                    //higher bitrate means that this edge has smaller weight according to Dijkstra algorithm                    
                    return 1.0 / edge.getEdge().getSpeed();//this may produce some mathematical inaccuracy...
                }
            });
        }
        return dijkstra.getPath(begin, end);
    }

    @Override
    public List<RouterVertex> getAllRouters() {
        if (NetbeansWindowHelper.getInstance().getActiveTopology() == null) {
            throw new IllegalStateException("no topology selected");
        }
        return NetbeansWindowHelper.getInstance().getActiveTopology().getVertexFactory().getVertexRouterList();
    }

    @Override
    public List<ComputerVertex> getAllComputers() {
          if (NetbeansWindowHelper.getInstance().getActiveTopology() == null) {
            throw new IllegalStateException("no topology selected");
        }
        return NetbeansWindowHelper.getInstance().getActiveTopology().getVertexFactory().getVertexComputerList();
    }

    @Override
    public List<SwitchVertex> getAllSwitches() {
          if (NetbeansWindowHelper.getInstance().getActiveTopology() == null) {
            throw new IllegalStateException("no topology selected");
        }
        return NetbeansWindowHelper.getInstance().getActiveTopology().getVertexFactory().getVertexSwitchList();
    }

    @Override
    public List<TopologyVertex> getAllVertices() {
          if (NetbeansWindowHelper.getInstance().getActiveTopology() == null) {
            throw new IllegalStateException("no topology selected");
        }
        return NetbeansWindowHelper.getInstance().getActiveTopology().getVertexFactory().getAllVertices();
    }
}
