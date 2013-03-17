/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.route;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.RouterVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;

/**
 *
 * @author Igor Kvasnicka
 */
public class RoutingHelperTest {

    @Test
    public void testCreateRouteFromEdgeList_1() {
        NetworkNode n1 = new Router("node1", null, null, 0, 0, null, 0, 0, 0, 0, 0);
        NetworkNode n2 = new Router("node2", null, null, 0, 0, null, 0, 0, 0, 0, 0);
        NetworkNode n3 = new Router("node3", null, null, 0, 0, null, 0, 0, 0, 0, 0);
        NetworkNode n4 = new Router("node4", null, null, 0, 0, null, 0, 0, 0, 0, 0);

        TopologyVertex v1 = new RouterVertex(n1);
        TopologyVertex v2 = new RouterVertex(n2);
        TopologyVertex v3 = new RouterVertex(n3);
        TopologyVertex v4 = new RouterVertex(n4);

        Edge ed1 = new Edge(0, 0, 0, 0, n1, n2);
        Edge ed2 = new Edge(0, 0, 0, 0, n2, n3);
        Edge ed3 = new Edge(0, 0, 0, 0, n3, n4);

        TopologyEdge e1 = new TopologyEdge(ed1, v1, v2);
        TopologyEdge e2 = new TopologyEdge(ed2, v2, v3);
        TopologyEdge e3 = new TopologyEdge(ed3, v3, v4);

        List<TopologyEdge> edges = Arrays.asList(e1, e2, e3);

        List<NetworkNode> createRouteFromEdgeList = RoutingHelper.createRouteFromEdgeList(n1, n4, edges);

        Assert.assertEquals(4, createRouteFromEdgeList.size());
        Assert.assertTrue(createRouteFromEdgeList.get(0).getName().equals(n1.getName()));
        Assert.assertTrue(createRouteFromEdgeList.get(1).getName().equals(n2.getName()));
        Assert.assertTrue(createRouteFromEdgeList.get(2).getName().equals(n3.getName()));
        Assert.assertTrue(createRouteFromEdgeList.get(3).getName().equals(n4.getName()));
    }

    @Test
    public void testCreateRouteFromEdgeList_2() {
        NetworkNode n1 = new Router("node1", null, null, 0, 0, null, 0, 0, 0, 0, 0);
        NetworkNode n2 = new Router("node2", null, null, 0, 0, null, 0, 0, 0, 0, 0);
        NetworkNode n3 = new Router("node3", null, null, 0, 0, null, 0, 0, 0, 0, 0);
        NetworkNode n4 = new Router("node4", null, null, 0, 0, null, 0, 0, 0, 0, 0);

        TopologyVertex v1 = new RouterVertex(n1);
        TopologyVertex v2 = new RouterVertex(n2);
        TopologyVertex v3 = new RouterVertex(n3);
        TopologyVertex v4 = new RouterVertex(n4);

        Edge ed1 = new Edge(0, 0, 0, 0, n1, n2);
        Edge ed2 = new Edge(0, 0, 0, 0, n2, n3);
        Edge ed3 = new Edge(0, 0, 0, 0, n3, n4);

        TopologyEdge e1 = new TopologyEdge(ed1, v1, v2);
        TopologyEdge e2 = new TopologyEdge(ed2, v2, v3);
        TopologyEdge e3 = new TopologyEdge(ed3, v3, v4);

        List<TopologyEdge> edges = Arrays.asList(e1, e3);

        try {
            List<NetworkNode> createRouteFromEdgeList = RoutingHelper.createRouteFromEdgeList(n1, n4, edges);
            Assert.fail("exception should be thrown, because route from edges is not correct");
        } catch (IllegalStateException e) {
            //OK
        }

    }

    @Test
    public void testCreateVerticesDataModelList() {
        NetworkNode n1 = new Router("node1", null, null, 0, 0, null, 0, 0, 0, 0, 0);
        NetworkNode n2 = new Router("node2", null, null, 0, 0, null, 0, 0, 0, 0, 0);
        NetworkNode n3 = new Router("node3", null, null, 0, 0, null, 0, 0, 0, 0, 0);
        NetworkNode n4 = new Router("node4", null, null, 0, 0, null, 0, 0, 0, 0, 0);

        TopologyVertex v1 = new RouterVertex(n1);
        TopologyVertex v2 = new RouterVertex(n2);
        TopologyVertex v3 = new RouterVertex(n3);
        TopologyVertex v4 = new RouterVertex(n4);

        List<NetworkNode> result = RoutingHelper.createVerticesDataModelList(Arrays.asList(v1, v2, v3, v4));

        Assert.assertEquals(4, result.size());
        Assert.assertTrue(result.get(0).getName().equals("node1"));
        Assert.assertTrue(result.get(1).getName().equals("node2"));
        Assert.assertTrue(result.get(2).getName().equals("node3"));
        Assert.assertTrue(result.get(3).getName().equals("node4"));

    }
}
