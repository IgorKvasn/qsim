/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.routing;

import edu.uci.ics.jung.algorithms.filters.FilterUtils;
import edu.uci.ics.jung.graph.AbstractGraph;
import java.util.*;
import org.apache.log4j.Logger;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
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
     * returns list of destination where is specified vertex able to route
     *
     * @return
     */
    public static List<NetworkNode> getAvailableDestinations(NetworkNode source) {
        if (source == null) {
            throw new IllegalArgumentException("source is NULL");
        }
        List<NetworkNode> result = new LinkedList<NetworkNode>();
        List<NetworkNode> toVisit = new LinkedList<NetworkNode>();//list of nodes that I need to visit
        Collection<NetworkNode> neighbours = new LinkedList<NetworkNode>();
        toVisit.addAll(convertStringList2NetworkNodeList(source.getAllDestinations()));
        while (!toVisit.isEmpty()) {//until there is still some node to visit
            int size = toVisit.size();
            neighbours.clear();
            for (NetworkNode node : toVisit) {
                if (result.contains(node) || node.equals(source)) { //node I am currently visiting was already processed
                    continue;
                }
                result.add(node);
                neighbours.addAll(getAvailableNextHops(node));//new nodes to visit                
            }
            //now, when all is done, I remove all nodes that I previously visited - this trick will save me from ConcurrentException
            for (int i = 0; i < size; i++) {
                toVisit.remove(0);
            }
            toVisit.addAll(neighbours);//now add new nodes

        }

        return result;
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

    public static boolean deleteRoute(TopologyEdge edge) {
        boolean bool1 = deleteRoute(edge.getVertex1(), edge.getVertex2());

        boolean bool2 = deleteRoute(edge.getVertex2(), edge.getVertex1());
        return bool1 || bool2;//at least one is true
    }

    /**
     * use only for edges (v1 and v2 are neighbours) otherwise no deletion
     * happens and this method will return false
     *
     * @param v1
     * @param v2
     * @return
     */
    private static boolean deleteRoute(TopologyVertex v1, TopologyVertex v2) {
        return v1.getDataModel().removeRoute(v2.getDataModel());

    }

    /**
     * deletes route
     *
     * @param from vertex that has got routing table I am interested in
     * @param to String that defines destination where I am routing
     * @return true if deletion was successful
     */
    public static boolean deleteRoute(TopologyVertex from, String to) {
        logg.debug("deleting route from " + from.getName() + " to destination " + to);
        NetworkNode toNode = findVertexByName(to);
        if (toNode == null) {
            return false;
        }
        return from.getDataModel().removeRoute(toNode);
    }

    public static boolean isDirectlyConnected(TopologyVertex from, String to) {
        if (to == null) {
            return false;
        }
        if (!from.getDataModel().containsRoute(to)) {
            return false;
        }
        //two verices are directly connected only if destination and next hop is the same
        NetworkNode nextHop = findVertexByName(from.getDataModel().getNextHopFromRoutingTable(to));

        return nextHop.getName().equals(to);
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

    /**
     * returns set of next hop vertices where is this vertex able to route
     *
     * @param source
     * @return
     */
    public static List<NetworkNode> getAvailableNextHops(NetworkNode source) {
        if (source == null) {
            throw new IllegalArgumentException("source is NULL");
        }
        Set<NetworkNode> result = new HashSet<NetworkNode>(convertStringList2NetworkNodeList(source.getAllDestinations()));//there may be more same next hops - this will find unique ones
        return Arrays.asList(result.toArray(new NetworkNode[result.size()]));
    }

    /**
     * this method is used when automatic routing is enabled it takes all
     * vertices and calculates the shortest paths between them old paths
     * (routes) will be forgotten
     */
    public void recalculateRoutes(Topology topology) {
        if (topology == null) {
            throw new IllegalArgumentException("topology is NULL");
        }
        logg.debug("recalculating routes....");
        List<RouterVertex> allRouters = topologyFacade.getAllRouters(); //ak budu nejake problemy s routovanim a switchmi, tak toto zmenit
        //first I delete all previous routing information        
        for (RouterVertex r : allRouters) {
            r.getDataModel().clearRoutingTable();
        }
        //now let's create new routes - remember, that all information about edges is stored in JUNG library,
        //so by previousely deleting routes I did not loose edges itself (themself..? anyway, here is the code: )
        for (RouterVertex r : allRouters) {
            try {
                setRouteSource(r);
            } catch (RoutingException ex) {
                // never happens, because I am iterating through routers only
            }
            for (RouterVertex rr : allRouters) {
                if (r.equals(rr)) { //comparing referencies, because I did not implement equals(), I know!! but it does not matter this time, because I am comparing the same elementes from the exact same List
                    continue;
                }
                createRoute(topology, rr);
            }
        }
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
     * each vertex is able to route to all his neighbours (and vice-versa) this
     * method creates routes to all neigbours of specified vertex
     */
    public static void createTwoWayRoute(NetworkNode node1, NetworkNode node2) {
        node1.addRoute(node2.getName(), node2.getName());
        node2.addRoute(node1.getName(), node1.getName());
    }

    /**
     * stores information about the first TopologyVertex that will be the source
     * of the route
     *
     * @param source source vertex
     * @throws RoutingException TopologyVertex is not a Router - only routers
     * can define routes
     */
    public void setRouteSource(TopologyVertex source) throws RoutingException {
        if (source == null) {
            throw new IllegalArgumentException("source is NULL");
        }

        if (!(source instanceof RouterVertex)) { //only routers can create routes - computers, nor switches cannot
            throw new RoutingException(NbBundle.getMessage(RoutingHelper.class, "routers.exception"));
        }

        this.routeSource = source;
    }

    /**
     * creates a route from the source vertex to destination
     *
     * @see
     * RoutingHelper#setRouteSource(sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex)
     * @param destination destination vertex
     * @param fixedVertices these vertices <b>must</b> be used in a final route
     * - probably user told so
     * @throws RoutingException destination is not a router or destination is
     * unreachable
     */
    @Deprecated
    public void createRoute(Topology topology, TopologyVertex destination, TopologyVertex... fixedVertices) {
        if (topology == null) {
            throw new IllegalArgumentException("topology is NULL");
        }
        if (destination == null) {
            throw new IllegalArgumentException("destination is NULL");
        }
        if (!destination.isRoutingAllowed()) {
            throw new IllegalStateException(NbBundle.getMessage(RoutingHelper.class, "routers.exception"));
        }

        if (routeSource.equals(destination)) {//no route will be created for a cyclic path
            return;
        }

        if (fixedVertices == null) {
            fixedVertices = new TopologyVertex[0];
        }
        List<TopologyEdge> path = new LinkedList<TopologyEdge>();
        List<TopologyVertex> fixed = new ArrayList(Arrays.asList(fixedVertices)); //Arrays.asList() returns read-only List


        if (fixed.isEmpty() || !fixed.get(0).equals(routeSource)) {//source is also a fixed vertex 
            fixed.add(0, routeSource);
        }

        if (!fixed.get(fixed.size() - 1).equals(destination)) {//destination is also a fixed vertex
            fixed.add(destination);
        }
        TopologyVertex vBegin, vEnd;

        for (int i = 0; i < fixed.size() - 1; i++) {
            vBegin = fixed.get(i);
            vEnd = fixed.get(i + 1);
//            List<TopologyEdge> shortestPath = topologyFacade.findShortestPath(topology.getG(), vBegin, vEnd);
//            path.addAll(shortestPath);
        }

        calculateRoute(routeSource, destination, path);

        printRoute(path);
    }

    /**
     * recalculates/recreates routing table of all TopologyNodes in route
     *
     * @param route
     * @param start the first TopologyVertex in the route - just to make my work
     * a little bit easy
     */
    public void calculateRoute(TopologyVertex start, TopologyVertex end, List<TopologyEdge> route) {
        if (start == null) {
            throw new IllegalArgumentException("start is NULL");
        }
        if (end == null) {
            throw new IllegalArgumentException("end is NULL");
        }
        if (route == null) {
            throw new IllegalArgumentException("route is NULL");
        }
        if (route.isEmpty()) {
            logg.debug("route: from " + start.getName() + " to " + end.getName() + " does not exist");
            return;
        }
        NetworkNode edgeBegin;
        NetworkNode destinationNode = end.getDataModel();

        //is the first vertex also in the first edge? maybe the List "route" has reverse ordering
        if (start.equals(route.get(0).getVertex1()) || start.equals(route.get(0).getVertex2())) {
            edgeBegin = start.getDataModel();//inicialisation    
        } else {
            edgeBegin = end.getDataModel();
        }

        for (TopologyEdge edge : route) {
            edgeBegin.addRoute(destinationNode.getName(), getOtherEndOfEdge(edge, edgeBegin).getName());
            edgeBegin = getOtherEndOfEdge(edge, edgeBegin); //the other end of edge is also beginning vertex for other edge

        }
    }

    private NetworkNode getOtherEndOfEdge(TopologyEdge topolEdge, NetworkNode node) {
        if (topolEdge == null) {
            throw new IllegalArgumentException("topolEdge is NULL");
        }
        if (node == null) {
            throw new IllegalArgumentException("node is NULL");
        }
        if (topolEdge.getVertex1().getDataModel().equals(node)) {
            return topolEdge.getVertex2().getDataModel();
        }
        if (topolEdge.getVertex2().getDataModel().equals(node)) {
            return topolEdge.getVertex1().getDataModel();
        }
        throw new IllegalStateException("Specified node " + node.getName() + " is not part of the edge: " + topolEdge.getVertex1().getName() + "<->" + topolEdge.getVertex2().getName());
    }

    private void printRoute(List<TopologyEdge> path) {
        StringBuilder sb = new StringBuilder("Routing path: ");
        for (TopologyEdge tEdge : path) {
            sb.append(tEdge.getVertex1()).append(":").append(tEdge.getVertex2()).append(" ");
        }
        logg.debug(sb.toString());
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
    public Collection<TopologyEdge> retrieveEdges(AbstractGraph<TopologyVertex, TopologyEdge> graph, TopologyVertex source, TopologyVertex destination, boolean distanceVector, TopologyVertex[] fixedVertices) throws RoutingException {
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
            fixedVertices = new TopologyVertex[0];
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
        List<TopologyVertex> fixed = new ArrayList(Arrays.asList(fixedVertices)); //Arrays.asList() returns read-only List
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
                throw new RoutingException(NbBundle.getMessage(RoutingHelper.class, "unreachable_vertex",vEnd.getName()));
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
