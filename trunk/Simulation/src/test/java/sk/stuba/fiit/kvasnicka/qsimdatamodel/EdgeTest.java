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

package sk.stuba.fiit.kvasnicka.qsimdatamodel;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers.RxBuffer;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers.TxBuffer;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.InputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.NotEnoughBufferSpaceException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLogUtils;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Fragment;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanismDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static sk.stuba.fiit.kvasnicka.TestUtils.getPropertyWithoutGetter;
import static sk.stuba.fiit.kvasnicka.TestUtils.initNetworkNode;
import static sk.stuba.fiit.kvasnicka.TestUtils.setWithoutSetter;

/**
 * @author Igor Kvasnicka
 */
public class EdgeTest {

    PacketManager packetManager;
    SimulationTimer timer;
    QosMechanismDefinition qosMechanism;
    double simulationTime;
    TopologyManager topologyManager;
    NetworkNode node1, node2;
    Edge edge;
    private final int MAX_TX_SIZE = 200;
    private final int MTU = 100;
    private final Layer4TypeEnum layer4 = Layer4TypeEnum.UDP;
    private long edgeSpeed = 100;

    @Before
    public void before() throws NotEnoughBufferSpaceException {
        simulationTime = 10L;


        qosMechanism = EasyMock.createMock(QosMechanismDefinition.class);

        EasyMock.expect(qosMechanism.classifyAndMarkPacket(EasyMock.anyObject(NetworkNode.class), EasyMock.anyObject(Packet.class))).andReturn(0).times(100);
        EasyMock.expect(qosMechanism.decitePacketsToMoveFromOutputQueue(EasyMock.anyObject(NetworkNode.class), EasyMock.anyObject(Map.class))).andAnswer(new IAnswer<List<Packet>>() {
            @Override
            public List<Packet> answer() throws Throwable {
                if (((Map<Integer, List<Packet>>) EasyMock.getCurrentArguments()[1]).get(0) == null) {
                    return new LinkedList<Packet>();
                }
                return ((Map<Integer, List<Packet>>) EasyMock.getCurrentArguments()[1]).get(0);
            }
        }).times(100);
        EasyMock.expect(qosMechanism.performActiveQueueManagement(EasyMock.anyObject(List.class), EasyMock.anyObject(Packet.class))).andReturn(true);

        EasyMock.replay(qosMechanism);


        node1 = new Router("node1", null, qosMechanism, MAX_TX_SIZE, 10, 50, 10, 2, 100, 0, 0);//max processing packets are set to 2
        node2 = new Router("node2", null, qosMechanism, MAX_TX_SIZE, 10, 50, 10, 2, 100, 0, 0);
        SimulationLogUtils simulationLogUtils = new SimulationLogUtils();
        initNetworkNode(node1, simulationLogUtils);
        initNetworkNode(node2, simulationLogUtils);

        edge = new Edge(edgeSpeed, MTU, 2, 0, node1, node2);

        topologyManager = new TopologyManager(Arrays.asList(edge), Arrays.asList(node1, node2));
        node1.setTopologyManager(topologyManager);
        node2.setTopologyManager(topologyManager);

        node1.setTopologyManager(topologyManager);
        node2.setTopologyManager(topologyManager);

        timer = EasyMock.createMock(SimulationTimer.class);
        EasyMock.expect(timer.getTopologyManager()).andReturn(topologyManager).times(100);
        EasyMock.replay(timer);

        packetManager = new PacketManager(timer);
    }

    /**
     * test preparation:
     * adds two packets on the wire
     * test:
     * moving packets from the wire to second network node
     * <p/>
     * all packets should end on the other network node in processing
     * these packets are all 1 fragment big and they are not put into input queue, because processing can handle it
     *
     * @throws NotEnoughBufferSpaceException
     */
    @Test
    public void testMoveFragmentsToNetworkNode() throws NotEnoughBufferSpaceException {
        //prepare - add two packets on the edge
        Packet p1 = new Packet(64, packetManager, null, 10);
        Packet p2 = new Packet(64, packetManager, null, 30);

        initRoute(p1, p2);

        node1.addToTxBuffer(p1, 100);
        node1.addToTxBuffer(p2, 100);

        TxBuffer outputInterface = node1.getTxInterfaces().get(node2);
        outputInterface.serialisePackets(50);     //adds two packets to the edge

        //test method
        edge.moveFragmentsToNetworkNode(100);

        //assert
        RxBuffer inputInterface = node2.getRxInterfaces().get(node1);
        assertNotNull(inputInterface);
        assertEquals(0, inputInterface.getNumberOfFragments()); //all fragments are put directly into processing
        assertNull(node2.getRxInterfaces().get(node2));
        InputQueue inputQueue = (InputQueue) getPropertyWithoutGetter(NetworkNode.class, node2, "inputQueue");
        assertTrue(inputQueue.isEmpty());//also input queue should be empty

        assertEquals(2, node2.getPacketsInProcessing().size());
    }

    /**
     * just like previous test, but this time, there are some packets in processing, so test packets
     * should be put into input queue - however, there is one space left in processing, so only one packet should be in input queue
     *
     * @throws NotEnoughBufferSpaceException
     */
    @Test
    public void testMoveFragmentsToNetworkNode_multifragment() throws NotEnoughBufferSpaceException {
        //prepare - add two packets on the edge
        Packet p1 = new Packet(64, packetManager, null, 10);
        Packet p2 = new Packet(64, packetManager, null, 30);
        Packet p3 = new Packet(64, packetManager, null, 30);

        initRoute(p1, p2, p3);

        node1.addToTxBuffer(p1, 100);
        node1.addToTxBuffer(p2, 100);

        TxBuffer outputInterface = node1.getTxInterfaces().get(node2);
        outputInterface.serialisePackets(50);     //adds two packets to the edge
        node2.addPacketToProcessing(p3); //some packets are already processing

        //test method
        edge.moveFragmentsToNetworkNode(100);

        //assert
        RxBuffer inputInterface = node2.getRxInterfaces().get(node1);
        assertNotNull(inputInterface);
        assertEquals(0, inputInterface.getNumberOfFragments()); //all fragments are put directly into processing
        assertNull(node2.getRxInterfaces().get(node2));
        InputQueue inputQueue = (InputQueue) getPropertyWithoutGetter(NetworkNode.class, node2, "inputQueue");
        assertEquals(1, inputQueue.getUsage(), 0);

        assertEquals(2, node2.getPacketsInProcessing().size());
    }

    @Test
    public void testEdgeUsage() {
        List<Fragment> fragments = new LinkedList<Fragment>();
        fragments.add(new Fragment(null, 0, 0, 50, null, null, null));


        setWithoutSetter(Edge.class, edge, "maxSpeed", 100);
        setWithoutSetter(Edge.class, edge, "fragments", fragments);

        double usage = edge.getUsage();
        assertEquals(83.33, usage, 0.1);
    }


    @Test
    public void testEdgeUsage_2() {
        List<Fragment> fragments = new LinkedList<Fragment>();
        fragments.add(new Fragment(null, 0, 0, 1500, null, null, null));


        setWithoutSetter(Edge.class, edge, "maxSpeed", 100000);
        setWithoutSetter(Edge.class, edge, "fragments", fragments);

        double usage = edge.getUsage();
        assertEquals(2.5, usage, 0);
    }

    @Test
    public void testFindOppositeNetworkNode() {
        assertTrue(node2.getName().equals(edge.findOppositeNetworkNode(node1).getName()));
        assertTrue(node1.getName().equals(edge.findOppositeNetworkNode(node2).getName()));
    }

    @Test
    public void testFindOppositeNetworkNode_null_argument() {
        try {
            edge.findOppositeNetworkNode(null);
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void testFindOppositeNetworkNode_unknown_node() {
        try {
            NetworkNode node3 = new Router("new node that is not placed on the edge", null, qosMechanism, MAX_TX_SIZE, 10, 50, 10, 2, 100, 0, 0);

            edge.findOppositeNetworkNode(node3);
            fail("IllegalStateException should be thrown");
        } catch (IllegalStateException e) {
            //OK
        }
    }

    @Test
    public void testContainsNode() {
        assertTrue(edge.containsNode(node1));
        assertTrue(edge.containsNode(node2));
    }

    @Test
    public void testContainsNode_unknown_node() {
        NetworkNode node3 = new Router("new node that is not placed on the edge", null, qosMechanism, MAX_TX_SIZE, 10, 50, 10, 2, 100, 0, 0);

        assertFalse(edge.containsNode(node3));
    }

    @Test
    public void testContainsNode_null_argument() {
        try {
            edge.containsNode(null);
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    private void initRoute(Packet... packets) {
        SimulationRuleBean simulationRuleBean = new SimulationRuleBean("", node1, node2, 1, 1, 10, layer4, IpPrecedence.IP_PRECEDENCE_0, 0, 0);
        simulationRuleBean.setRoute(Arrays.asList(node1, node2));

        for (Packet p : packets) {
            Field f = null;
            try {
                f = Packet.class.getDeclaredField("simulationRule");
                f.setAccessible(true);
                f.set(p, simulationRuleBean);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
