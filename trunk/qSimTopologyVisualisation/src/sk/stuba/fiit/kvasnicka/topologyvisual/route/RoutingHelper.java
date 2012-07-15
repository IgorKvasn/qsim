/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.route;

/**
 *
 * @author Igor Kvasnicka
 */
import edu.uci.ics.jung.algorithms.filters.FilterUtils;
import edu.uci.ics.jung.graph.AbstractGraph;
import java.util.*;
import org.apache.log4j.Logger;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.Topology;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.topologyvisual.exceptions.RoutingException;
import sk.stuba.fiit.kvasnicka.topologyvisual.facade.TopologyFacade;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.TopologyVertexFactory;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.RouterVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;

/**
 * This helper is used to handle routes. It does not store these routes, it is
 * active only during creation and manipulation with routes. The routes itself
 * are stored in NetworkNode objects (Router, Switch, Computer objects).
 *
 * @author Igor Kvasnicka
 */
@NbBundle.Messages({
    "fixed_vertixes=Source or destination node must not be in fixed nodes list",
    "unreachable_vertex=Unable to find valid path to vertex \"{0}\""
})
public class RoutingHelper {

    private static Logger logg = Logger.getLogger(RoutingHelper.class);
    private TopologyVertex routeSource;
    private static TopologyFacade topologyFacade;

    public RoutingHelper() {
        topologyFacade = findTopologyFacade();
    }

    /**
     * extracts data model from all TopologyVertex objects
     *
     * @param route
     * @return
     */
    public static List<NetworkNode> createVerticesDataModelList(List<TopologyVertex> route) {
        if (route == null) {
            throw new IllegalArgumentException("vertices list is null");
        }

        List<NetworkNode> list = new LinkedList<NetworkNode>();
        for (TopologyVertex v : route) {
            list.add(v.getDataModel());
        }
        return list;
    }

    /**
     *
     * route specified by consequent topology edges creates route specified as
     * list of NetworkNodes
     *
     * @param route
     * @return
     */
    public static List<NetworkNode> createRouteFromEdgeList(NetworkNode start, NetworkNode end, List<TopologyEdge> route) {
        List<NetworkNode> routeNodes = new LinkedList<NetworkNode>();
        NetworkNode previousNode = start;
        routeNodes.add(start);

        for (TopologyEdge edge : route) {
            NetworkNode nextNode = getNextNetworkNode(previousNode, edge.getEdge());
            routeNodes.add(nextNode);
            previousNode = nextNode;
        }

        //check if last NetworkNode is the same as "end" parameter
        if (!routeNodes.get(routeNodes.size() - 1).getName().equals(end.getName())) {
            throw new IllegalStateException("creating route: last network node should be " + end + ", but it is " + routeNodes.get(route.size() - 1));
        }

        return routeNodes;
    }

    /**
     * for a given edge returns the other end of the edge
     *
     * @param node1 first end of the edge
     * @param edge the edge itself
     * @return the second end of the edge
     */
    private static NetworkNode getNextNetworkNode(NetworkNode node1, Edge edge) {
        if (edge.getNode1().getName().equals(node1.getName())) {
            return edge.getNode2();
        }
        if (edge.getNode2().getName().equals(node1.getName())) {
            return edge.getNode1();
        }
        throw new IllegalStateException("NetworkNode " + node1 + " is not part of the edge between nodes: " + edge.getNode1() + " <-> " + edge.getNode2());
    }

    private static List<NetworkNode> convertStringList2NetworkNodeList(Collection<String> col) {
        if (col == null || col.isEmpty()) {
            return new LinkedList<NetworkNode>();
        }
        List<NetworkNode> list = new ArrayList<NetworkNode>(col.size());
        for (String vertexName : col) {
            list.add(findVertexByName(vertexName));
        }
        return list;
    }

    public static int getNumberOfNeighboursByType(Topology topology, TopologyVertex v, Class type) {
        Collection<TopologyVertex> neighbors = topologyFacade.getNeighbours(topology, v);
        int count = 0;
        for (TopologyVertex vertex : neighbors) {
            if (type.isInstance(vertex.getDataModel())) {
                count++;
            }
        }
        return count;
    }

    /**
     * finds vertex by its name<br> it is guaranteed that each vertex has got
     * unique name<br>finds vertex in current topology
     *
     * @param vertexName
     * @return
     */
    public static NetworkNode findVertexByName(String vertexName) {
        if (NetbeansWindowHelper.getInstance().getActiveTopology() == null) {
            throw new IllegalStateException("No TopComponent was selected");
        }
        return findVertexByName(vertexName, NetbeansWindowHelper.getInstance().getActiveTopology().getVertexFactory());
    }

    /**
     * finds vertex by its name<br> it is guaranteed that each vertex has got
     * unique name<br>finds vertex in provided topology (specified by
     * VertexTopologyFactory object)
     *
     * @param vertexName
     * @return
     */
    public static NetworkNode findVertexByName(String vertexName, TopologyVertexFactory factory) {
        if (vertexName == null) {
            throw new IllegalArgumentException("vertexName is NULL");
        }
        for (TopologyVertex v : factory.getAllVertices()) {
            if (vertexName.equals(v.getName())) {
                return v.getDataModel();
            }
        }
        throw new IllegalStateException("Vertex named: [" + vertexName + "] could not be found.");
    }

    private static TopologyFacade findTopologyFacade() throws IllegalStateException {
        Collection<? extends TopologyFacade> lookupAll = Lookup.getDefault().lookupAll(TopologyFacade.class);
        if (lookupAll.isEmpty()) {
            throw new IllegalStateException("No topology facade found");
        }
        if (lookupAll.size() > 1) {
            logg.warn("Multiple instancies of Topologyfacade found - the first one will be used.");
        }
        return lookupAll.iterator().next();
    }

    /**
     * finds all edges from one vertex to the last vertex specified in vararg
     * (the second parameter). These edges connects all vertices (beginning with
     * source) in other specified in method parameters. The first vertex
     * (source) is mandatory.
     *
     * @param source first vertex
     * @param fixedPoints other vertices that must be included in the way (order
     * is important; the last vertex is "destination")
     */
    public static List<TopologyEdge> retrieveEdges(AbstractGraph<TopologyVertex, TopologyEdge> graph, TopologyVertex source, TopologyVertex destination, boolean distanceVector, List<TopologyVertex> fixedVertices) throws RoutingException {
        if (graph == null) {
            throw new IllegalArgumentException("graph is NULL");
        }
        if (source == null) {
            throw new IllegalArgumentException("source is NULL");
        }
        if (destination == null) {
            throw new IllegalArgumentException("destination is NULL");
        }
        if (!source.isRoutingAllowed()) {
            throw new IllegalStateException(NbBundle.getMessage(RoutingHelper.class, "routers.exception" + " " + source.getName()));
        }
        if (fixedVertices == null) {
            fixedVertices = new LinkedList<TopologyVertex>();
        }

        //check if source or destination is not fixed
        for (TopologyVertex v : fixedVertices) {
            if (source.equals(v)) {
                throw new RoutingException(NbBundle.getMessage(RoutingHelper.class, "fixed_vertixes"));
            }
            if (destination.equals(v)) {
                throw new RoutingException(NbBundle.getMessage(RoutingHelper.class, "fixed_vertixes"));
            }
            if (v == null) {
                throw new IllegalStateException("fixed vertex is NULL");
            }
        }

        //lets go
        List<TopologyEdge> path = new LinkedList<TopologyEdge>();
        List<TopologyVertex> fixed = new ArrayList(fixedVertices); //make a copy
        fixed.add(0, source);
        fixed.add(destination);

        //for each fixed pair of vertices (source and destination are fixed, too now) I create a subgraph
        //that contains only non-fixed vertices and these two fixed vertices
        //then I calculate shortest path between them
        //note, that I take two sucessors from fixed list - e.g. fixed[i] and fixed[i+1]

        TopologyVertex vBegin, vEnd;
        for (int i = 0; i < fixed.size() - 1; i++) {
            vBegin = fixed.get(i);
            vEnd = fixed.get(i + 1);

            List<TopologyVertex> vertices = new ArrayList<TopologyVertex>(graph.getVertices());
            vertices.removeAll(fixed);//only non-fixed vertices
            vertices.add(vBegin);//and two vertices I am lookung path between
            vertices.add(vEnd);

            AbstractGraph<TopologyVertex, TopologyEdge> subGraph = FilterUtils.createInducedSubgraph(vertices, graph);
            List<TopologyEdge> shortestPath = topologyFacade.findShortestPath(subGraph, vBegin, vEnd, distanceVector);
            if (shortestPath.isEmpty()) {
                throw new RoutingException(NbBundle.getMessage(RoutingHelper.class, "unreachable_vertex", vEnd.getName()));
            }
            path.addAll(shortestPath);
        }

        return path;
    }

    /**
     * checks route for cycles
     *
     * @param edges
     * @return false if no cycles were found; true otherwise
     */
    public boolean checkRouteForCycle(Collection<TopologyEdge> edges) {
        //each vertex should be used at most twice
        //the first time it is end of edge, the second time it is edge start
        //there must be two verteices with only one repetition - source and destination vertex
        //weeel, I sould use Boolean instead of Integer, but with int it is more readable don't you think?
        Map<TopologyVertex, Integer> used = new HashMap<TopologyVertex, Integer>(1 + (int) (edges.size() / 0.75));
        for (TopologyEdge edge : edges) {

            if (used.containsKey(edge.getVertex1())) {
                if (used.get(edge.getVertex1()) == 2) {//it has been used for 2 times already
                    return false;
                } else {
                    used.put(edge.getVertex1(), 2);
                }
            } else {
                used.put(edge.getVertex1(), 1);
            }

            if (used.containsKey(edge.getVertex2())) {
                if (used.get(edge.getVertex2()) == 2) {//it has been used for 2 times already
                    return false;
                } else {
                    used.put(edge.getVertex2(), 2);
                }
            } else {
                used.put(edge.getVertex2(), 1);
            }
        }
        return true;
    }
}