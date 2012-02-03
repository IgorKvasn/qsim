/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.routing;

import edu.uci.ics.jung.graph.AbstractGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.easymock.EasyMock;
import static org.junit.Assert.*;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.Topology;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.topologyvisual.exceptions.RoutingException;
import sk.stuba.fiit.kvasnicka.topologyvisual.facade.TopologyFacade;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.TopologyVertexFactory;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.RouterVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;

/**
 * None of these tests are testing finding the shortest path between two nodes.
 * This is done by JUNG library, so there is no need to test already tested.
 *
 * @author Igor Kvasnicka
 */
public class RoutingHelperTest extends NbTestCase {

    RoutingHelper helper;
    Topology topology;

    public RoutingHelperTest(String name) {
        super(name);
    }

    public void testOtherEndOfEdge() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        helper = new RoutingHelper();
        NetworkNode node1 = new Router("meno1");
        NetworkNode node2 = new Router("meno2");
        TopologyVertex vertex1 = new RouterVertex(node1);
        TopologyVertex vertex2 = new RouterVertex(node2);

        Edge edge = new Edge(node1, node2);
        TopologyEdge topolEdge = new TopologyEdge(edge, vertex1, vertex2);

        Method method = RoutingHelper.class.getDeclaredMethod("getOtherEndOfEdge", TopologyEdge.class, NetworkNode.class);
        method.setAccessible(true);
        NetworkNode otherNode = (NetworkNode) method.invoke(helper, topolEdge, node1);

        assertNotNull(otherNode);
        assertEquals(node2, otherNode);
    }

    public void testCalculateRoute_Null_1() {
        helper = new RoutingHelper();

        NetworkNode node1 = new Router("meno1");
        NetworkNode node2 = new Router("meno2");

        TopologyVertex vertex1 = new RouterVertex(node1);
        TopologyVertex vertex2 = new RouterVertex(node2);

        Edge edge1 = new Edge(node1, node2);
        TopologyEdge topolEdge1 = new TopologyEdge(edge1, vertex1, vertex2);
        try {
            helper.calculateRoute(null, vertex1, Arrays.asList(topolEdge1));
            fail("exception not thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testCalculateRoute_Null_2() {
        helper = new RoutingHelper();

        NetworkNode node1 = new Router("meno1");
        NetworkNode node2 = new Router("meno2");

        TopologyVertex vertex1 = new RouterVertex(node1);
        TopologyVertex vertex2 = new RouterVertex(node2);

        Edge edge1 = new Edge(node1, node2);
        TopologyEdge topolEdge1 = new TopologyEdge(edge1, vertex1, vertex2);
        try {
            helper.calculateRoute(vertex1, vertex2, null);
            fail("exception not thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testCalculateRoute_Null_3() {
        helper = new RoutingHelper();

        NetworkNode node1 = new Router("meno1");
        NetworkNode node2 = new Router("meno2");

        TopologyVertex vertex1 = new RouterVertex(node1);
        TopologyVertex vertex2 = new RouterVertex(node2);

        Edge edge1 = new Edge(node1, node2);
        TopologyEdge topolEdge1 = new TopologyEdge(edge1, vertex1, vertex2);
        try {
            helper.calculateRoute(vertex1, null, Arrays.asList(topolEdge1));
            fail("exception not thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * a chain of vertices
     */
    public void testCalculateRoute_Simple() {
        helper = new RoutingHelper();
        NetworkNode node1 = new Router("meno1");
        NetworkNode node2 = new Router("meno2");
        NetworkNode node3 = new Router("meno3");
        NetworkNode node4 = new Router("meno4");
        TopologyVertex vertex1 = new RouterVertex(node1);
        TopologyVertex vertex2 = new RouterVertex(node2);
        TopologyVertex vertex3 = new RouterVertex(node3);
        TopologyVertex vertex4 = new RouterVertex(node4);

        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);
        Edge edge3 = new Edge(node3, node4);


        TopologyEdge topolEdge1 = new TopologyEdge(edge1, vertex1, vertex2);
        TopologyEdge topolEdge2 = new TopologyEdge(edge2, vertex2, vertex3);
        TopologyEdge topolEdge3 = new TopologyEdge(edge3, vertex3, vertex4);


        helper.calculateRoute(vertex1, vertex4, Arrays.asList(topolEdge1, topolEdge2, topolEdge3));

        assertTrue(node1.containsRoute(node4.getName()));
        assertEquals(1, node1.getAllDestinations().size());
        assertEquals(node2.getName(), node1.getNextHopFromRoutingTable(node4.getName()));

        assertTrue(node2.containsRoute(node4.getName()));
        assertEquals(1, node2.getAllDestinations().size());
        assertEquals(node3.getName(), node2.getNextHopFromRoutingTable(node4.getName()));

        assertTrue(node3.containsRoute(node4.getName()));
        assertEquals(1, node3.getAllDestinations().size());
        assertEquals(node4.getName(), node3.getNextHopFromRoutingTable(node4.getName()));

        assertEquals(0, node4.getAllDestinations().size());//destination node must not contain itself in the routing table - otherwise the cycle may be created
    }

    /**
     * 1. create a chain of vertices v1 -> v2 -> v3 2. create new chain of
     * vertices v1 -> v4 -> v3
     *
     * assert that the 2nd route is stored in routing tables
     */
    public void testCalculateRoute_Override() {
        helper = new RoutingHelper();
        NetworkNode node1 = new Router("meno1");
        NetworkNode node2 = new Router("meno2");
        NetworkNode node3 = new Router("meno3");
        NetworkNode node4 = new Router("meno4");
        TopologyVertex vertex1 = new RouterVertex(node1);
        TopologyVertex vertex2 = new RouterVertex(node2);
        TopologyVertex vertex3 = new RouterVertex(node3);
        TopologyVertex vertex4 = new RouterVertex(node4);

        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);
        Edge edge3 = new Edge(node1, node4);
        Edge edge4 = new Edge(node4, node3);



        //first route
        TopologyEdge topolEdge1 = new TopologyEdge(edge1, vertex1, vertex2);
        TopologyEdge topolEdge2 = new TopologyEdge(edge2, vertex2, vertex3);
        //second route
        TopologyEdge topolEdge3 = new TopologyEdge(edge3, vertex1, vertex4);
        TopologyEdge topolEdge4 = new TopologyEdge(edge4, vertex4, vertex3);


        //-------------first route-------------------
        helper.calculateRoute(vertex1, vertex3, Arrays.asList(topolEdge1, topolEdge2));

        assertTrue(node1.containsRoute(node3.getName()));
        assertEquals(1, node1.getAllDestinations().size());
        assertEquals(node2.getName(), node1.getNextHopFromRoutingTable(node3.getName()));

        assertTrue(node2.containsRoute(node3.getName()));
        assertEquals(1, node2.getAllDestinations().size());
        assertEquals(node3.getName(), node2.getNextHopFromRoutingTable(node3.getName()));

        assertEquals(0, node3.getAllDestinations().size());//destination node must not contain itself in the routing table - otherwise the cycle may be created


        //------------------second route-------------
        helper.calculateRoute(vertex1, vertex3, Arrays.asList(topolEdge3, topolEdge4));

        assertTrue(node1.containsRoute(node3.getName()));
        assertEquals(1, node1.getAllDestinations().size());
        assertEquals(node4.getName(), node1.getNextHopFromRoutingTable(node3.getName()));

        assertTrue(node2.containsRoute(node3.getName()));
        assertEquals(1, node2.getAllDestinations().size());
        assertEquals(node3.getName(), node2.getNextHopFromRoutingTable(node3.getName()));

        assertEquals(0, node3.getAllDestinations().size());//destination node must not contain itself in the routing table - otherwise the cycle may be created

    }

    /**
     * Ypsilon-shaped topology: v1 -> v2 -> v3 and v2 -> v4 1. calculate route
     * from v1 to v3 2. calculate route from v2 to v4
     *
     * assert that v2 is able to route to v3 and v4
     */
    public void testCalculateRoute_Multiple() {
        helper = new RoutingHelper();
        NetworkNode node1 = new Router("meno1");
        NetworkNode node2 = new Router("meno2");
        NetworkNode node3 = new Router("meno3");
        NetworkNode node4 = new Router("meno4");
        TopologyVertex vertex1 = new RouterVertex(node1);
        TopologyVertex vertex2 = new RouterVertex(node2);
        TopologyVertex vertex3 = new RouterVertex(node3);
        TopologyVertex vertex4 = new RouterVertex(node4);

        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);
        Edge edge3 = new Edge(node2, node4);

        TopologyEdge topolEdge1 = new TopologyEdge(edge1, vertex1, vertex2);
        TopologyEdge topolEdge2 = new TopologyEdge(edge2, vertex2, vertex3);
        TopologyEdge topolEdge3 = new TopologyEdge(edge3, vertex2, vertex4);


        //-------------first route-------------------
        helper.calculateRoute(vertex1, vertex3, Arrays.asList(topolEdge1, topolEdge2));

        //------------------second route-------------
        helper.calculateRoute(vertex2, vertex4, Arrays.asList(topolEdge3));

        assertEquals(2, vertex2.getDataModel().getAllDestinations().size());
        assertTrue(vertex2.getDataModel().containsRoute(vertex3.getDataModel().getName()));
        assertTrue(vertex2.getDataModel().containsRoute(vertex4.getDataModel().getName()));
    }

    /**
     * a chain of vertices, but I cannot reach destination algorithm run without
     * any problem - this is a matter of bad user input (maybe user wants this?)
     * this problem should be handled by method createRoute() because it is
     * creating the route after all...
     */
    public void testCalculateRoute_Error() {
        helper = new RoutingHelper();
        NetworkNode node1 = new Router("meno1");
        NetworkNode node2 = new Router("meno2");
        NetworkNode node3 = new Router("meno3");
        NetworkNode node4 = new Router("meno4");
        TopologyVertex vertex1 = new RouterVertex(node1);
        TopologyVertex vertex2 = new RouterVertex(node2);
        TopologyVertex vertex3 = new RouterVertex(node3);
        TopologyVertex vertex4 = new RouterVertex(node4);

        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);
        Edge edge3 = new Edge(node3, node4);


        TopologyEdge topolEdge1 = new TopologyEdge(edge1, vertex1, vertex2);
        TopologyEdge topolEdge2 = new TopologyEdge(edge2, vertex2, vertex3);


        helper.calculateRoute(vertex1, vertex4, Arrays.asList(topolEdge1, topolEdge2));

        assertTrue(node1.containsRoute(node4.getName()));
        assertEquals(1, node1.getAllDestinations().size());
        assertEquals(node2.getName(), node1.getNextHopFromRoutingTable(node4.getName()));

        assertTrue(node2.containsRoute(node4.getName()));
        assertEquals(1, node2.getAllDestinations().size());
        assertEquals(node3.getName(), node2.getNextHopFromRoutingTable(node4.getName()));

        assertEquals(0, node3.getAllDestinations().size());//the last node should route to destination, but destination is unreachable
    }

    /**
     * the test topology is the same as testCalculateRoute_Override(), but this
     * time I am testing calculating the route there will be no fixed vertices
     * this test does not assert anything - it only tests if algorithm passes
     *
     */
    public void testCreateRoute_normal_noFixedVertices() throws RoutingException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        helper = new RoutingHelper();
        topology = new Topology(null);

        NetworkNode node1 = new Router("meno1");
        NetworkNode node2 = new Router("meno2");
        NetworkNode node3 = new Router("meno3");
        NetworkNode node4 = new Router("meno4");
        TopologyVertex vertex1 = new RouterVertex(node1);
        TopologyVertex vertex2 = new RouterVertex(node2);
        TopologyVertex vertex3 = new RouterVertex(node3);
        TopologyVertex vertex4 = new RouterVertex(node4);

        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);
        Edge edge3 = new Edge(node1, node4);
        Edge edge4 = new Edge(node4, node3);

        //first route
        TopologyEdge topolEdge1 = new TopologyEdge(edge1, vertex1, vertex2);
        TopologyEdge topolEdge2 = new TopologyEdge(edge2, vertex2, vertex3);
        //second route
        TopologyEdge topolEdge3 = new TopologyEdge(edge3, vertex1, vertex4);
        TopologyEdge topolEdge4 = new TopologyEdge(edge4, vertex4, vertex3);


        Field topologyFacadeField = RoutingHelper.class.getDeclaredField("topologyFacade");
        topologyFacadeField.setAccessible(true);

        TopologyFacade facadeMock = EasyMock.createMock(TopologyFacade.class);

        EasyMock.expect(facadeMock.findShortestPath(topology.getG(), vertex1, vertex3, true)).andReturn(Arrays.asList(topolEdge1, topolEdge2));


        EasyMock.replay(facadeMock);

        topologyFacadeField.set(helper, facadeMock);

        helper.setRouteSource(vertex1);
        helper.createRoute(topology, vertex3);
    }

    /**
     * Ypsilon topology
     */
    public void ignoredGetAvailableDestinations() {
        topology = new Topology(null);

        NetworkNode source = new Router("source");
        NetworkNode a = new Router("a");
        NetworkNode b = new Router("b");
        NetworkNode c = new Router("c");

        source.addRoute(a.getName(), a.getName());
        source.addRoute(b.getName(), b.getName());
        b.addRoute(c.getName(), c.getName());


        List<NetworkNode> destinations = RoutingHelper.getAvailableDestinations(source);
        assertNotNull(destinations);
        assertEquals(3, destinations.size());
        assertTrue(destinations.contains(a));
        assertTrue(destinations.contains(b));
        assertTrue(destinations.contains(c));

    }

    /**
     * cyclic topology
     */
    public void ignoredGetAvailableDestinations_cycle() {
        topology = new Topology(null);

        NetworkNode source = new Router("source");
        NetworkNode a = new Router("a");
        NetworkNode b = new Router("b");
        NetworkNode c = new Router("c");

        source.addRoute(a.getName(), a.getName());
        source.addRoute(b.getName(), b.getName());
        b.addRoute(c.getName(), c.getName());
        c.addRoute(source.getName(), source.getName());


        List<NetworkNode> destinations = RoutingHelper.getAvailableDestinations(source);
        assertNotNull(destinations);
        assertEquals(3, destinations.size());
        assertTrue(destinations.contains(a));
        assertTrue(destinations.contains(b));
        assertTrue(destinations.contains(c));
    }

    public void testTwoWayRoute() {
        NetworkNode a = new Router("a");
        NetworkNode b = new Router("b");

        RoutingHelper.createTwoWayRoute(a, b);
        assertEquals(1, a.getAllDestinations().size());
        assertTrue(a.containsRoute(b.getName()));
        assertTrue(b.getName().equals(a.getNextHopFromRoutingTable(b.getName())));

        assertEquals(1, b.getAllDestinations().size());
        assertTrue(b.containsRoute(a.getName()));
        assertTrue(a.getName().equals(b.getNextHopFromRoutingTable(a.getName())));
    }

    public void testDirectlyConnected_Negative() {
        TopologyVertex from = new RouterVertex(new Router("router1"));
        TopologyVertex toVertex = new RouterVertex(new Router("router2"));
        String to = "router2";

        boolean result = RoutingHelper.isDirectlyConnected(from, to);
        assertFalse(result);
    }

    public void ignoredDirectlyConnected_Positive() throws Exception {
        topology = new Topology(null);

        Router r1 = new Router("router1");
        RouterVertex from = new RouterVertex(r1);
        String to = "router2";
        Router r2 = new Router(to);

        RoutingHelper.createTwoWayRoute(r1, r2);

        RouterVertex toVertex = new RouterVertex(r2);

        Field field = TopologyVertexFactory.class.getDeclaredField("vertexRouterList");
        field.setAccessible(true);
        List<RouterVertex> vertexRouterList = new LinkedList<RouterVertex>();
        vertexRouterList.add(toVertex);
        vertexRouterList.add(from);
        field.set(new TopologyVertexFactory(null), vertexRouterList);

        boolean result = RoutingHelper.isDirectlyConnected(from, to);
        assertTrue(result);
    }

    public void testRetrieveEdges_1() throws RoutingException {
        helper = new RoutingHelper();

        NetworkNode node1 = new Router("meno1");
        NetworkNode node2 = new Router("meno2");
        NetworkNode node3 = new Router("meno3");
        NetworkNode node4 = new Router("meno4");

        TopologyVertex vertex1 = new RouterVertex(node1);
        TopologyVertex vertex2 = new RouterVertex(node2);
        TopologyVertex vertex3 = new RouterVertex(node3);
        TopologyVertex vertex4 = new RouterVertex(node4);

        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);
        Edge edge3 = new Edge(node3, node4);

        TopologyEdge topolEdge1 = new TopologyEdge(edge1, vertex1, vertex2);
        TopologyEdge topolEdge2 = new TopologyEdge(edge2, vertex2, vertex3);
        TopologyEdge topolEdge3 = new TopologyEdge(edge3, vertex3, vertex4);

        AbstractGraph<TopologyVertex, TopologyEdge> graph = new UndirectedSparseGraph<TopologyVertex, TopologyEdge>();
        graph.addEdge(topolEdge1, vertex1, vertex2);
        graph.addEdge(topolEdge2, vertex2, vertex3);
        graph.addEdge(topolEdge3, vertex3, vertex4);
        Collection<TopologyEdge> retrieveEdges = helper.retrieveEdges(graph, vertex1, vertex4, true, new TopologyVertex[]{});
        assertEquals(3, retrieveEdges.size());
        TopologyEdge[] toArray = retrieveEdges.toArray(new TopologyEdge[retrieveEdges.size()]);
        assertEquals(topolEdge1, toArray[0]);
        assertEquals(topolEdge2, toArray[1]);
        assertEquals(topolEdge3, toArray[2]);
    }

    public void testRetrieveEdges_2() throws RoutingException {
        helper = new RoutingHelper();

        NetworkNode node1 = new Router("meno1");
        NetworkNode node2 = new Router("meno2");
        NetworkNode node3 = new Router("meno3");
        NetworkNode node4 = new Router("meno4");

        TopologyVertex vertex1 = new RouterVertex(node1);
        TopologyVertex vertex2 = new RouterVertex(node2);
        TopologyVertex vertex3 = new RouterVertex(node3);
        TopologyVertex vertex4 = new RouterVertex(node4);

        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);
        Edge edge3 = new Edge(node3, node4);

        TopologyEdge topolEdge1 = new TopologyEdge(edge1, vertex1, vertex2);
        TopologyEdge topolEdge2 = new TopologyEdge(edge2, vertex2, vertex3);
        TopologyEdge topolEdge3 = new TopologyEdge(edge3, vertex3, vertex4);

        AbstractGraph<TopologyVertex, TopologyEdge> graph = new UndirectedSparseGraph<TopologyVertex, TopologyEdge>();
        graph.addEdge(topolEdge1, vertex1, vertex2);
        graph.addEdge(topolEdge2, vertex2, vertex3);
        graph.addEdge(topolEdge3, vertex3, vertex4);
        Collection<TopologyEdge> retrieveEdges = helper.retrieveEdges(graph, vertex1, vertex4, true, new TopologyVertex[]{vertex3});
        assertEquals(3, retrieveEdges.size());
        TopologyEdge[] toArray = retrieveEdges.toArray(new TopologyEdge[retrieveEdges.size()]);
        assertEquals(topolEdge1, toArray[0]);
        assertEquals(topolEdge2, toArray[1]);
        assertEquals(topolEdge3, toArray[2]);
    }

    /**
     * create a circle and route between neighbour vertices - but also define
     * fixed vertex to make the route go longer way (around the circle)
     *
     * @throws RoutingException
     */
    public void testRetrieveEdges_3() throws RoutingException {
        helper = new RoutingHelper();

        NetworkNode node1 = new Router("meno1");
        NetworkNode node2 = new Router("meno2");
        NetworkNode node3 = new Router("meno3");
        NetworkNode node4 = new Router("meno4");

        TopologyVertex vertex1 = new RouterVertex(node1);
        TopologyVertex vertex2 = new RouterVertex(node2);
        TopologyVertex vertex3 = new RouterVertex(node3);
        TopologyVertex vertex4 = new RouterVertex(node4);

        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);
        Edge edge3 = new Edge(node3, node4);
        Edge edge4 = new Edge(node1, node4);

        TopologyEdge topolEdge1 = new TopologyEdge(edge1, vertex1, vertex2);
        TopologyEdge topolEdge2 = new TopologyEdge(edge2, vertex2, vertex3);
        TopologyEdge topolEdge3 = new TopologyEdge(edge3, vertex3, vertex4);
        TopologyEdge topolEdge4 = new TopologyEdge(edge4, vertex1, vertex4);


        AbstractGraph<TopologyVertex, TopologyEdge> graph = new UndirectedSparseGraph<TopologyVertex, TopologyEdge>();
        graph.addEdge(topolEdge1, vertex1, vertex2);
        graph.addEdge(topolEdge2, vertex2, vertex3);
        graph.addEdge(topolEdge3, vertex3, vertex4);
        graph.addEdge(topolEdge4, vertex1, vertex4);

        Collection<TopologyEdge> retrieveEdges = helper.retrieveEdges(graph, vertex1, vertex4, true, new TopologyVertex[]{vertex3});
        assertEquals(3, retrieveEdges.size());
        TopologyEdge[] toArray = retrieveEdges.toArray(new TopologyEdge[retrieveEdges.size()]);
        assertEquals(topolEdge1, toArray[0]);
        assertEquals(topolEdge2, toArray[1]);
        assertEquals(topolEdge3, toArray[2]);
    }

    /**
     * source is fixed vertex
     *
     * @throws RoutingException
     */
    public void testRetrieveEdges_source_fixed() {
        helper = new RoutingHelper();

        NetworkNode node1 = new Router("meno1");
        NetworkNode node2 = new Router("meno2");
        NetworkNode node3 = new Router("meno3");
        NetworkNode node4 = new Router("meno4");

        TopologyVertex vertex1 = new RouterVertex(node1);
        TopologyVertex vertex2 = new RouterVertex(node2);
        TopologyVertex vertex3 = new RouterVertex(node3);
        TopologyVertex vertex4 = new RouterVertex(node4);

        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);
        Edge edge3 = new Edge(node3, node4);

        TopologyEdge topolEdge1 = new TopologyEdge(edge1, vertex1, vertex2);
        TopologyEdge topolEdge2 = new TopologyEdge(edge2, vertex2, vertex3);
        TopologyEdge topolEdge3 = new TopologyEdge(edge3, vertex3, vertex4);

        AbstractGraph<TopologyVertex, TopologyEdge> graph = new UndirectedSparseGraph<TopologyVertex, TopologyEdge>();
        graph.addEdge(topolEdge1, vertex1, vertex2);
        graph.addEdge(topolEdge2, vertex2, vertex3);
        graph.addEdge(topolEdge3, vertex3, vertex4);
        Collection<TopologyEdge> retrieveEdges;
        try {
            retrieveEdges = helper.retrieveEdges(graph, vertex1, vertex4, true, new TopologyVertex[]{vertex1});
            fail("source must not be fixed");
        } catch (RoutingException ex) {
            //ok
        }
    }

    /**
     * destination is fixed vertex
     *
     * @throws RoutingException
     */
    public void testRetrieveEdges_destination_fixed() {
        helper = new RoutingHelper();

        NetworkNode node1 = new Router("meno1");
        NetworkNode node2 = new Router("meno2");
        NetworkNode node3 = new Router("meno3");
        NetworkNode node4 = new Router("meno4");

        TopologyVertex vertex1 = new RouterVertex(node1);
        TopologyVertex vertex2 = new RouterVertex(node2);
        TopologyVertex vertex3 = new RouterVertex(node3);
        TopologyVertex vertex4 = new RouterVertex(node4);

        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node2, node3);
        Edge edge3 = new Edge(node3, node4);

        TopologyEdge topolEdge1 = new TopologyEdge(edge1, vertex1, vertex2);
        TopologyEdge topolEdge2 = new TopologyEdge(edge2, vertex2, vertex3);
        TopologyEdge topolEdge3 = new TopologyEdge(edge3, vertex3, vertex4);

        AbstractGraph<TopologyVertex, TopologyEdge> graph = new UndirectedSparseGraph<TopologyVertex, TopologyEdge>();
        graph.addEdge(topolEdge1, vertex1, vertex2);
        graph.addEdge(topolEdge2, vertex2, vertex3);
        graph.addEdge(topolEdge3, vertex3, vertex4);
        Collection<TopologyEdge> retrieveEdges;
        try {
            retrieveEdges = helper.retrieveEdges(graph, vertex1, vertex4, true, new TopologyVertex[]{vertex4});
            fail("destination must not be fixed");
        } catch (RoutingException ex) {
            //ok
        }

    }
}
