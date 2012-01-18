/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.facade;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import java.util.Collection;
import java.util.List;
import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;
import org.openide.util.lookup.ServiceProvider;
import sk.stuba.fiit.kvasnicka.topologyvisual.PreferenciesHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.TopologyCreation;
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
    public Collection<TopologyVertex> getNeighbours(TopologyCreation topology, TopologyVertex vertex) {
        return topology.getGraph().getNeighbors(vertex);
    }

    @Override
    public List<TopologyEdge> findShortestPath(TopologyCreation topology, TopologyVertex begin, TopologyVertex end) {
        if (PreferenciesHelper.isAutomaticRoutingDistanceProtocol()) {//unweight dijkstra
            logg.debug("findShortestPath - unweight");
            dijkstra = new DijkstraShortestPath(topology.getGraph());
        } else {
            logg.debug("findShortestPath - weight");
            dijkstra = new DijkstraShortestPath(topology.getGraph(), new Transformer<TopologyEdge, Double>() {

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
        if (NetbeansWindowHelper.getInstance().getActiveTopComponentTopology() == null) {
            throw new IllegalStateException("no topology selected");
        }
        return NetbeansWindowHelper.getInstance().getActiveTopComponentTopology().getVertexFactory().getVertexRouterList();
    }

    @Override
    public List<ComputerVertex> getAllComputers() {
          if (NetbeansWindowHelper.getInstance().getActiveTopComponentTopology() == null) {
            throw new IllegalStateException("no topology selected");
        }
        return NetbeansWindowHelper.getInstance().getActiveTopComponentTopology().getVertexFactory().getVertexComputerList();
    }

    @Override
    public List<SwitchVertex> getAllSwitches() {
          if (NetbeansWindowHelper.getInstance().getActiveTopComponentTopology() == null) {
            throw new IllegalStateException("no topology selected");
        }
        return NetbeansWindowHelper.getInstance().getActiveTopComponentTopology().getVertexFactory().getVertexSwitchList();
    }

    @Override
    public List<TopologyVertex> getAllVertices() {
          if (NetbeansWindowHelper.getInstance().getActiveTopComponentTopology() == null) {
            throw new IllegalStateException("no topology selected");
        }
        return NetbeansWindowHelper.getInstance().getActiveTopComponentTopology().getVertexFactory().getAllVertices();
    }
}
