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

package sk.stuba.fiit.kvasnicka.itegration.congestion;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.OutputQueueManager;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.QueueingHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLogUtils;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Fragment;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static sk.stuba.fiit.kvasnicka.TestUtils.getPropertyWithoutGetter;
import static sk.stuba.fiit.kvasnicka.TestUtils.initNetworkNode;
import static sk.stuba.fiit.kvasnicka.TestUtils.setWithoutSetter;

/**
 * this is a single and simple test of input queue & congestion avoidance mechanism to see if it is enabled on input queue
 * <p/>
 * if you are looking for tests of congestion-avoidance as-is, see TcpCongestionOutputQueueTest file
 *
 * @author Igor Kvasnicka
 */
public class TcpCongestionInputQueueTest {
    PacketManager packetManager;
    SimulationTimer timer;
    QosMechanism qosMechanism;
    double simulationTime;
    TopologyManager topologyManager;
    NetworkNode node1, node2, node3;
    Edge edge1, edge2;
    private final int MAX_TX_SIZE = 0;
    private final int MTU = 100;
    private OutputQueueManager outputQueueManager1, outputQueueManager2, outputQueueManager3;
    private OutputQueue q2;

    @Before
    public void before() {
        simulationTime = 10L;

        qosMechanism = EasyMock.createMock(QosMechanism.class);


        OutputQueue q1 = new OutputQueue(10, "queue 1");
        OutputQueue q11 = new OutputQueue(10, "queue 11");
        q2 = new OutputQueue(1, "queue 2");
        OutputQueue q3 = new OutputQueue(10, "queue 3");

        outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1, q11});
        outputQueueManager2 = new OutputQueueManager(new OutputQueue[]{q2});
        outputQueueManager3 = new OutputQueueManager(new OutputQueue[]{q3});

        EasyMock.expect(qosMechanism.classifyAndMarkPacket(EasyMock.anyObject(NetworkNode.class), EasyMock.anyObject(Packet.class))).andReturn(0).times(100);
        EasyMock.expect(qosMechanism.decitePacketsToMoveFromOutputQueue(EasyMock.anyObject(NetworkNode.class), EasyMock.anyObject(List.class))).andAnswer(new IAnswer<List<Packet>>() {
            @Override
            public List<Packet> answer() throws Throwable {
                return ((List<List<Packet>>) EasyMock.getCurrentArguments()[1]).get(0);
            }
        }).times(100);
        qosMechanism.performActiveQueueManagement(EasyMock.anyObject(List.class), EasyMock.anyObject(Packet.class));
        qosMechanism.performActiveQueueManagement(EasyMock.anyObject(List.class), EasyMock.anyObject(Packet.class));
        qosMechanism.performActiveQueueManagement(EasyMock.anyObject(List.class), EasyMock.anyObject(Packet.class));
        qosMechanism.performActiveQueueManagement(EasyMock.anyObject(List.class), EasyMock.anyObject(Packet.class));
        qosMechanism.performActiveQueueManagement(EasyMock.anyObject(List.class), EasyMock.anyObject(Packet.class));
        qosMechanism.performActiveQueueManagement(EasyMock.anyObject(List.class), EasyMock.anyObject(Packet.class));
        qosMechanism.performActiveQueueManagement(EasyMock.anyObject(List.class), EasyMock.anyObject(Packet.class));
        qosMechanism.performActiveQueueManagement(EasyMock.anyObject(List.class), EasyMock.anyObject(Packet.class));
        qosMechanism.performActiveQueueManagement(EasyMock.anyObject(List.class), EasyMock.anyObject(Packet.class));
        qosMechanism.performActiveQueueManagement(EasyMock.anyObject(List.class), EasyMock.anyObject(Packet.class));


        EasyMock.replay(qosMechanism);


        node1 = new Router("node1", qosMechanism, outputQueueManager1, MAX_TX_SIZE, 10, 1, 0, 100, 0, 0);//notice, that 0 packets should be in processing - all will be placed into input queue
        node2 = new Router("node2", qosMechanism, outputQueueManager2, MAX_TX_SIZE, 10, 1, 0, 100, 0, 0);
        node3 = new Router("node3", qosMechanism, outputQueueManager3, MAX_TX_SIZE, 10, 1, 0, 100, 0, 0);

        SimulationLogUtils simulationLogUtils = new SimulationLogUtils();
        initNetworkNode(node1, simulationLogUtils);
        initNetworkNode(node2, simulationLogUtils);
        initNetworkNode(node3, simulationLogUtils);


        edge1 = new Edge(100, MTU, 2, 0, node1, node2);
        edge2 = new Edge(100, MTU, 2, 0, node2, node3);

        topologyManager = new TopologyManager(Arrays.asList(edge1, edge2), Arrays.asList(node1, node2));
        node1.setTopologyManager(topologyManager);
        node2.setTopologyManager(topologyManager);

        timer = EasyMock.createMock(SimulationTimer.class);
        EasyMock.expect(timer.getTopologyManager()).andReturn(topologyManager).times(100);
        EasyMock.replay(timer);

        packetManager = new PacketManager(timer);
    }


    /**
     * adding packets to output queue - the first packet will be added to TX, but there will be not enough space for the second packet
     * so it will be added to output queue
     */
    @Test
    public void testMoveFromRxToInputQueue_overflow() {
        //create packets
        Packet p1 = new Packet(100, Layer4TypeEnum.TCP, packetManager, null, 10);
        Packet p2 = new Packet(100, Layer4TypeEnum.TCP, packetManager, null, 30);
        Packet p3 = new Packet(100, Layer4TypeEnum.TCP, packetManager, null, 330);//this creation time is important, because this is time when edge congestion happens
        Packet p4 = new Packet(100, Layer4TypeEnum.TCP, packetManager, null, 350);
        Packet p5 = new Packet(100, Layer4TypeEnum.TCP, packetManager, null, 850);//this creation time is important, because this is time when edge congestion happens
        Packet p6 = new Packet(100, Layer4TypeEnum.TCP, packetManager, null, 890);
        Packet p7 = new Packet(100, Layer4TypeEnum.TCP, packetManager, null, 900);
        Packet p8 = new Packet(100, Layer4TypeEnum.TCP, packetManager, null, 950);
        initRoute(p1, p2, p3, p4, p5, p6, p7, p8);

        Fragment[] f1 = QueueingHelper.createFragments(p1, 20, node1, node2);

        //first add packet to input queue - this should be OK
        for (Fragment f : f1) {
            node2.addToRxBuffer(f);
        }

        assertEquals(1, node2.getInputQueueUsage());

        Fragment[] f2 = QueueingHelper.createFragments(p2, 20, node1, node2);
        //this packet will be dropped - indicating congestion
        for (Fragment f : f2) {
            node2.addToRxBuffer(f);
        }
        assertEquals(1, node2.getInputQueueUsage());//still one packet

        //get edge speed in some time in the future (edge speed is changed due to congestion with some delay - TCP timer delay)
        long speed = edge1.getSpeed(p3);
        assertEquals(edge1.getMaxSpeed() / 2, speed);

        Fragment[] f3 = QueueingHelper.createFragments(p4, 20, node1, node2);
        //this packet will be dropped - indicating congestion
        for (Fragment f : f3) {
            node2.addToRxBuffer(f);
        }


        //assert edge speed decrease
        speed = edge1.getSpeed(p5);
        assertEquals(edge1.getMaxSpeed() / 4, speed);


        //edge congestion set should be empty
        TreeSet set = (TreeSet) getPropertyWithoutGetter(Edge.class, edge1, "congestedInfoSet");
        assertNotNull(set);
        assertTrue(set.isEmpty());


        //===========TCP congestion recovery============

        setWithoutSetter(NetworkNode.class, node2, "maxProcessingPackets", 100);

        Fragment[] f4 = QueueingHelper.createFragments(p6, 20, node1, node2);
        for (Fragment f : f4) {
            node2.addToRxBuffer(f);
        }


        Fragment[] f5 = QueueingHelper.createFragments(p7, 20, node1, node2);
        for (Fragment f : f5) {
            node2.addToRxBuffer(f);
        }

        for (Fragment f : f5) {
            node2.addToRxBuffer(f);
        }

        for (Fragment f : f5) {
            node2.addToRxBuffer(f);
        }

        speed = edge1.getSpeed(p8);
        assertEquals(edge1.getMaxSpeed(), speed);   //at this point I should reach max Speed
    }


    private void initRoute(Packet... packets) {
        SimulationRuleBean simulationRuleBean = new SimulationRuleBean("", node1, node3, 1, 1, 100, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.TCP);
        simulationRuleBean.setRoute(Arrays.asList(node1, node2, node3));

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


    private void initRoute2(Packet... packets) {
        SimulationRuleBean simulationRuleBean = new SimulationRuleBean("", node1, node3, 1, 1, 100, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.TCP);
        simulationRuleBean.setRoute(Arrays.asList(node1, node2, node3));

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
