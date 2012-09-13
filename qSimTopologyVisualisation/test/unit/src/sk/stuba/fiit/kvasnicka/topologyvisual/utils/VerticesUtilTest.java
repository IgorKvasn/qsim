/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.utils;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.RouterVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;

/**
 *
 * @author Igor Kvasnicka
 */
public class VerticesUtilTest {

    private TopologyVertex v1, v2, v3;

    @Before
    public void before() {
        NetworkNode n1 = new Router("node1", null, null, 0, 0, 0, 0, 0, 0, 0, 0);
        NetworkNode n2 = new Router("node2", null, null, 0, 0, 0, 0, 0, 0, 0, 0);
        NetworkNode n3 = new Router("node3", null, null, 0, 0, 0, 0, 0, 0, 0, 0);


        v1 = new RouterVertex(n1);
        v2 = new RouterVertex(n2);
        v3 = new RouterVertex(n3);
    }

    @Test
    public void testGetVerticesNames_one_vertex() {
        String result = VerticesUtil.getVerticesNames(Arrays.asList(v1));
        Assert.assertTrue("node1".equals(result));
    }

    @Test
    public void testGetVerticesNames_two_vertex() {
        String result = VerticesUtil.getVerticesNames(Arrays.asList(v1, v2));
        Assert.assertTrue("node1, node2".equals(result));
    }

    @Test
    public void testGetVerticesNames_multiple_vertex() {
        String result = VerticesUtil.getVerticesNames(Arrays.asList(v1, v2, v3));
        Assert.assertTrue("node1, node2, node3".equals(result));
    }
}
