/*******************************************************************************
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
 ******************************************************************************/

package sk.stuba.fiit.kvasnicka.qsimsimulation.managers;

import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Igor Kvasnicka
 */
public class TopologyManagerTest {
    private NetworkNode node1, node2, node3, node4;
    private Edge edge_12, edge_23;
    private TopologyManager topologyManager;

    @Before
    public void before() {
        node1 = new Router("node1", null, null,null, 0, 10, 50, 10, 2, 100, 0, 0);
        node2 = new Router("node2", null, null,null, 0, 10, 50, 10, 2, 100, 0, 0);
        node3 = new Router("node3", null, null,null, 0, 10, 50, 10, 2, 100, 0, 0);
        node4 = new Router("node4", null, null,null, 0, 10, 50, 10, 2, 100, 0, 0);


        edge_12 = new Edge(0, 1, 2, 0, node1, node2);
        edge_23 = new Edge(0, 1, 2, 0, node2, node3);


        topologyManager = new TopologyManager(Arrays.asList(edge_12, edge_23), Arrays.asList(node1, node2, node3, node4));
    }

    @Test
    public void testFindEdgesWithNode_null() {
        try {
            topologyManager.findEdgesWithNode(null);
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void testFindEdgesWithNode() {
        List<Edge> result = topologyManager.findEdgesWithNode(node2);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0) == edge_12 || result.get(0) == edge_23);     //it should be exactly the same object
        assertTrue(result.get(1) == edge_12 || result.get(1) == edge_23);    //it should be exactly the same object
        assertTrue(result.get(0) != result.get(1));                          //previous asserts may be true, it both list items are the same
    }

    @Test
    public void testFindEdgesWithNode_nothing_found() {
        List<Edge> result = topologyManager.findEdgesWithNode(node4);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testFindNetworkNode_null() {
        try {
            topologyManager.findNetworkNode(null);
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void testFindNetworkNode() {
        NetworkNode result = topologyManager.findNetworkNode(node1.getName());
        assertTrue(result == node1); //it should be exactly the same object
    }

    @Test
    public void testFindNetworkNode_unknown_node() {
        NetworkNode newNode = new Router("this is a new node", null, null,null, 1, 10, 50, 10, 2, 100, 0, 0);
        try {
            topologyManager.findNetworkNode(newNode.getName());
            fail("IllegalStateException should be thrown");
        } catch (IllegalStateException e) {
            //OK
        }
    }

    @Test
    public void testFindEdge_null() {
        try {
            topologyManager.findEdge(null, null);
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException e) {
            //OK
        }
        try {
            topologyManager.findEdge(node1.getName(), null);
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException e) {
            //OK
        }
        try {
            topologyManager.findEdge(null, node2.getName());
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void testFindEdge() {
        Edge edge = topologyManager.findEdge(node1.getName(), node2.getName());
        assertNotNull(edge);
        assertTrue(edge == edge_12);   //it should be exactly the same object

        //now try it in a reverse order
        Edge edge2 = topologyManager.findEdge(node2.getName(), node1.getName());
        assertNotNull(edge2);
        assertTrue(edge2 == edge_12);   //it should be exactly the same object
    }


    /**
     * run findEdge method twice - test if the second result is OK, too
     */
    @Test
    public void testFindEdge_caching_test() {
        Edge edge = topologyManager.findEdge(node1.getName(), node2.getName());
        assertNotNull(edge);
        assertTrue(edge == edge_12);   //it should be exactly the same object

        Edge edge2 = topologyManager.findEdge(node1.getName(), node2.getName());
        assertNotNull(edge2);
        assertTrue(edge2 == edge_12);   //it should be exactly the same object
    }

    @Test
    public void testFindEdge_not_found() {
        try {
            topologyManager.findEdge(node1.getName(), node3.getName());
            fail("IllegalStateException should be thrown");
        } catch (IllegalStateException e) {
            //OK

        }
    }

    @Test
    public void testFindEdge_not_found_new_node() {
        NetworkNode newNode = new Router("this is a new node", null, null,null, 1, 10, 50, 10, 2, 100, 0, 0);

        try {
            topologyManager.findEdge(newNode.getName(), node2.getName());
            fail("IllegalStateException should be thrown");
        } catch (IllegalStateException e) {
            //OK

        }
    }
}
