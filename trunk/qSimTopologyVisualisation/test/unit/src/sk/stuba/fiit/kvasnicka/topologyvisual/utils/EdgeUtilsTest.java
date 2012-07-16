/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.utils;

import org.junit.Assert;
import org.junit.Before;
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
public class EdgeUtilsTest {

    TopologyEdge edge, edge_equal_1, edge_equal_2, edge_not_equal_1, edge_not_equal_2;

    @Before
    public void before() {
        NetworkNode n1 = new Router("node1", null, null, 0, 0, 0, 0, 0, 0, 0);
        NetworkNode n2 = new Router("node2", null, null, 0, 0, 0, 0, 0, 0, 0);
        NetworkNode n3 = new Router("node3", null, null, 0, 0, 0, 0, 0, 0, 0);
        NetworkNode n4 = new Router("node4", null, null, 0, 0, 0, 0, 0, 0, 0);

        TopologyVertex v1 = new RouterVertex(n1);
        TopologyVertex v2 = new RouterVertex(n2);
        TopologyVertex v3 = new RouterVertex(n3);
        TopologyVertex v4 = new RouterVertex(n4);

        edge = new TopologyEdge(new Edge(n1, n2), v1, v2);
        edge_equal_1 = new TopologyEdge(new Edge(n1, n2), v1, v2);
        edge_equal_2 = new TopologyEdge(new Edge(n2, n1), v2, v1);
        edge_not_equal_1 = new TopologyEdge(new Edge(n2, n3), v2, v3);
        edge_not_equal_2 = new TopologyEdge(new Edge(n4, n3), v4, v3);
    }

    @Test
    public void testIsEdgesEqual() {
        Assert.assertTrue(EdgeUtils.isEdgesEqual(edge, edge_equal_1));
        Assert.assertTrue(EdgeUtils.isEdgesEqual(edge, edge_equal_2));
        
        Assert.assertFalse(EdgeUtils.isEdgesEqual(edge, edge_not_equal_1));
        Assert.assertFalse(EdgeUtils.isEdgesEqual(edge, edge_not_equal_2));
    }
}
