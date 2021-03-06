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

package sk.stuba.fiit.kvasnicka.qsimsimulation.buffers;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers.RxBuffer;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.InputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.NotEnoughBufferSpaceException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.PacketCrcErrorException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.QueueingHelper;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static sk.stuba.fiit.kvasnicka.TestUtils.getPropertyWithoutGetter;
import static sk.stuba.fiit.kvasnicka.TestUtils.initNetworkNode;

/**
 * @author Igor Kvasnicka
 */
public class RxBufferTest {

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

    @Before
    public void before() {
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
        EasyMock.replay(qosMechanism);


        node1 = new Router("node1", null, qosMechanism, MAX_TX_SIZE, 10, null, 10, 10, 100, 0, 0);
        node2 = new Router("node2", null, qosMechanism, MAX_TX_SIZE, 10, null, 10, 10, 100, 0, 0);
        SimulationLogUtils simulationLogUtils = new SimulationLogUtils();
        initNetworkNode(node1, simulationLogUtils);
        initNetworkNode(node2, simulationLogUtils);


        edge = new Edge(100, MTU, 2, 0, node1, node2);

        topologyManager = new TopologyManager(Arrays.asList(edge), Arrays.asList(node1, node2));
        node1.setTopologyManager(topologyManager);
        node2.setTopologyManager(topologyManager);

//
//        node1.setRoute("node2", "node2");
//        node2.setRoute("node1", "node1");

        timer = EasyMock.createMock(SimulationTimer.class);
        EasyMock.expect(timer.getTopologyManager()).andReturn(topologyManager).times(100);
        EasyMock.replay(timer);

        packetManager = new PacketManager(timer);
    }

    /**
     * test receiving fragments and holding them in RX buffer
     * in this test, no packet is being created
     * all fragments are from the same packet
     */
    @Test
    public void testFragmentReceived() throws NotEnoughBufferSpaceException, PacketCrcErrorException {
        //prepare
        RxBuffer inputInterface = new RxBuffer(edge, 10, node1);//I do not care about max RX size
        Packet p1 = new Packet(14, packetManager, null, 10); //3 fragments will be created
        initRoute(p1);

        //test method
        Fragment[] fragments = QueueingHelper.createFragments(p1, 5, node2, node1); //MTU = 5
        //I am going to add all but one fragment into RX - I do not want a packet to be created
        for (int i = 0, fragmentsLength = fragments.length - 1; i < fragmentsLength; i++) {
            Fragment fragment = fragments[i];
            inputInterface.fragmentReceived(fragment);
        }

        //assert - there should be 2 fragments in RX buffer
        assertEquals(fragments.length - 1, inputInterface.getNumberOfFragments());
    }

    /**
     * test receiving fragments and holding them in RX buffer
     * in this test, no packet is being created
     * there will be fragments from 2 packets
     */
    @Test
    public void testFragmentReceived_multipacket() throws NotEnoughBufferSpaceException, PacketCrcErrorException {
        //prepare
        RxBuffer inputInterface = new RxBuffer(edge, 10, node1);//I do not care about max RX size
        Packet p1 = new Packet(14, packetManager, null, 10); //3 fragments will be created
        Packet p2 = new Packet(16, packetManager, null, 10); //4 fragments will be created
        initRoute(p1, p2);


        //test method
        //adding fragments of the first packet
        Fragment[] fragments = QueueingHelper.createFragments(p1, 5, node2, node1); //MTU = 5
        //I am going to add all but one fragment into RX - I do not want a packet to be created
        for (int i = 0, fragmentsLength = fragments.length - 1; i < fragmentsLength; i++) {
            Fragment fragment = fragments[i];
            inputInterface.fragmentReceived(fragment);
        }

        //adding fragments of the first packet
        Fragment[] fragments2 = QueueingHelper.createFragments(p2, 5, node2, node1); //MTU = 5
        //I am going to add all but one fragment into RX - I do not want a packet to be created
        for (int i = 0, fragmentsLength = fragments2.length - 1; i < fragmentsLength; i++) {
            Fragment fragment = fragments2[i];
            inputInterface.fragmentReceived(fragment);
        }

        //assert - there should be 2 fragments in RX buffer
        assertEquals(fragments2.length - 1 + fragments.length - 1, inputInterface.getNumberOfFragments());
    }

    /**
     * test receiving fragments and holding them in RX buffer
     * in this test, a packet  will be created
     * all fragments are from the same packet
     */
    @Test
    public void testFragmentReceived_packet_created() throws NotEnoughBufferSpaceException, PacketCrcErrorException {
        //prepare
        RxBuffer inputInterface = new RxBuffer(edge, 10, node1);//I do not care about max RX size
        Packet p1 = new Packet(14, packetManager, null, 10); //3 fragments will be created
        initRoute(p1);

        //test method
        Fragment[] fragments = QueueingHelper.createFragments(p1, 5, node2, node1); //MTU = 5
        //I am going to add all but one fragment into RX - I do not want a packet to be created
        assertNull(inputInterface.fragmentReceived(fragments[0]));
        assertNull(inputInterface.fragmentReceived(fragments[1]));
        Packet p = inputInterface.fragmentReceived(fragments[2]);//now a packet should be created
        initRoute(p);
        assertNotNull(p);

        assertTrue(p == p1); //these should be EXACT objects, so I am comparing references


        //assert - there should be no fragments in RX buffer, neither input queue or processing
        assertEquals(0, inputInterface.getNumberOfFragments());

        InputQueue inputQueue = (InputQueue) getPropertyWithoutGetter(NetworkNode.class, node2, "inputQueue");
        assertNotNull(inputQueue);
        assertTrue(inputQueue.isEmpty());

        assertEquals(0, node2.getPacketsInProcessing().size());//there should be 1 packet in processing, because input queue is empty
    }


    /**
     * test receiving fragments and holding them in RX buffer
     * in this test, a packet  will be created
     * all fragments are from the same packet
     * each packet has got different received time - when a new packet is created, it should have got simulation time of last fragment's "received time"
     */
    @Test
    public void testFragmentReceived_packet_created__simulation_time() throws NotEnoughBufferSpaceException, PacketCrcErrorException {
        //prepare
        RxBuffer inputInterface = new RxBuffer(edge, 10, node1);//I do not care about max RX size
        Packet p1 = new Packet(14, packetManager, null, 10); //3 fragments will be created
        initRoute(p1);

        //test method
        Fragment[] fragments = QueueingHelper.createFragments(p1, 5, node2, node1); //MTU = 5
        fragments[0].setReceivedTime(11);
        fragments[1].setReceivedTime(22);
        fragments[2].setReceivedTime(33);
        //I am going to add all but one fragment into RX - I do not want a packet to be created
        assertNull(inputInterface.fragmentReceived(fragments[0]));
        assertNull(inputInterface.fragmentReceived(fragments[1]));
        Packet p = inputInterface.fragmentReceived(fragments[2]);//now a packet should be created
        initRoute(p);
        assertNotNull(p);

        assertTrue(p == p1); //these should be EXACT objects, so I am comparing references
        assertEquals(fragments[2].getReceivedTime(), p.getSimulationTime(), 0.0);

        //assert - there should be no fragments in RX buffer, neither input queue or processing
        assertEquals(0, inputInterface.getNumberOfFragments());

        InputQueue inputQueue = (InputQueue) getPropertyWithoutGetter(NetworkNode.class, node2, "inputQueue");
        assertNotNull(inputQueue);
        assertTrue(inputQueue.isEmpty());

        assertEquals(0, node2.getPacketsInProcessing().size());//there should be 1 packet in processing, because input queue is empty
    }


    /**
     * test receiving fragments and holding them in RX buffer
     * in this test, a packet  will be created
     * there will be fragments from 2 packets
     * fragments even came in the wrong order, but tested method should handle it
     */
    @Test
    public void testFragmentReceived_packet_created__multiple_packets() throws NotEnoughBufferSpaceException, PacketCrcErrorException {
        //prepare
        RxBuffer inputInterface = new RxBuffer(edge, 10, node1);//I do not care about max RX size
        Packet p1 = new Packet(14, packetManager, null, 10); //3 fragments will be created
        Packet p2 = new Packet(9, packetManager, null, 10); //2 fragments will be created
        initRoute(p1, p2);

        //test method
        Fragment[] fragments1 = QueueingHelper.createFragments(p1, 5, node2, node1); //MTU = 5
        Fragment[] fragments2 = QueueingHelper.createFragments(p2, 5, node2, node1); //MTU = 5

        //I am going to add all but one fragment into RX - I do not want a packet to be created
        assertNull(inputInterface.fragmentReceived(fragments1[1]));//1. packet
        assertNull(inputInterface.fragmentReceived(fragments2[0]));//2. packet
        assertNull(inputInterface.fragmentReceived(fragments1[0]));//1. packet

        Packet pp = inputInterface.fragmentReceived(fragments2[1]); //2. packet; now a packet should be created
        initRoute(pp);
        assertNotNull(pp);

        Packet p = inputInterface.fragmentReceived(fragments1[2]);//1. packet; now a packet should be created
        initRoute(p);
        assertNotNull(p);

        assertTrue(p == p1); //these should be EXACT objects, so I am comparing references
        assertTrue(pp == p2); //these should be EXACT objects, so I am comparing references


        //assert - there should be no fragments in RX buffer, neither input queue or processing
        assertEquals(0, inputInterface.getNumberOfFragments());

        InputQueue inputQueue = (InputQueue) getPropertyWithoutGetter(NetworkNode.class, node2, "inputQueue");
        assertNotNull(inputQueue);
        assertTrue(inputQueue.isEmpty());

        assertEquals(0, node2.getPacketsInProcessing().size());//there should be 1 packet in processing, because input queue is empty
    }

    /**
     * test receiving fragments and holding them in RX buffer
     * in this test, no packet is being created
     * the purpose of this test is to test overflowing RX buffer
     */
    @Test
    public void testFragmentReceived_overflow() throws PacketCrcErrorException {
        //prepare
        int MAX_RX_SIZE = 2;
        RxBuffer inputInterface = new RxBuffer(edge, MAX_RX_SIZE, node1);//max 2 fragments in RX
        Packet p1 = new Packet(14, packetManager, null, 10); //3 fragments will be created
        initRoute(p1);

        //test method
        Fragment[] fragments = QueueingHelper.createFragments(p1, 5, node2, node1); //MTU = 5
        //I am going to add all but one fragment into RX - I do not want a packet to be created

        try {
            inputInterface.fragmentReceived(fragments[0]);
            inputInterface.fragmentReceived(fragments[1]);
        } catch (NotEnoughBufferSpaceException e) {
            fail("buffer overflow - this should not happen, yet");
        }
        try {
            inputInterface.fragmentReceived(fragments[2]);//this should not pass
            fail("RX buffer overflow should happen and it did not");
        } catch (NotEnoughBufferSpaceException e) {
            //OK
        }

        //assert - all fragments should be removed, because all were received, although only 2 were not dropped
        assertEquals(0, inputInterface.getNumberOfFragments());
    }

    private void initRoute(Packet... packets) {
        SimulationRuleBean simulationRuleBean = new SimulationRuleBean("", node1, node2, null,1, 1, 100, layer4, IpPrecedence.IP_PRECEDENCE_0, null,  0, 0);
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
