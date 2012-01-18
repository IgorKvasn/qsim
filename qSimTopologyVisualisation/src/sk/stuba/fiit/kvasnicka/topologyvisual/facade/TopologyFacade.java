/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.facade;

import java.util.Collection;
import java.util.List;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.TopologyCreation;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.ComputerVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.RouterVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.SwitchVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;

/**
 * public interface for communication between modules
 *
 * @author Igor Kvasnicka
 */
public interface TopologyFacade {

    /**
     * returns all neighbours to specified vertex
     *
     * @param vertex
     * @return
     */
    public Collection<TopologyVertex> getNeighbours(TopologyCreation topology, TopologyVertex vertex);

    /**
     * finds the shortest path between two vertices the algorithm used is
     * Dijkstra algorithm for finding the shortest path
     *
     * @param begin source vertex
     * @param end destination vertex
     * @return
     */
    public List<TopologyEdge> findShortestPath(TopologyCreation topology, TopologyVertex begin, TopologyVertex end);

    /**
     * returns list of all topology routers
     *
     * @return
     */
    public List<RouterVertex> getAllRouters();

    /**
     * returns list of all topology PCs
     *
     * @return
     */
    public List<ComputerVertex> getAllComputers();

    /**
     * returns list of all topology Switches
     *
     * @return
     */
    public List<SwitchVertex> getAllSwitches();

    public List<TopologyVertex> getAllVertices();
}
