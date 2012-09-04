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
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLogUtils;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
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
 * tests congestion behaviour on output queues
 *
 * @author Igor Kvasnicka
 */
public class TcpCongestionOutputQueueTest {
    PacketManager packetManager;
    SimulationTimer timer;
    QosMechanism qosMechanism;
    double simulationTime;
    TopologyManager topologyManager;
    NetworkNode node1, node2, node3;
    Edge edge1, edge2;
    private final int MAX_TX_SIZE = 0;
    private final int MTU = 100;
    private static final int MAX_PROCESSING_PACKETS = 3;
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


        EasyMock.replay(qosMechanism);


        node1 = new Router("node1", qosMechanism, outputQueueManager1, MAX_TX_SIZE, 10, 10, MAX_PROCESSING_PACKETS, 100, 0, 0, null);
        node2 = new Router("node2", qosMechanism, outputQueueManager2, MAX_TX_SIZE, 10, 10, 10, 100, 0, 0, null);
        node3 = new Router("node3", qosMechanism, outputQueueManager3, MAX_TX_SIZE, 10, 10, 10, 100, 0, 0, null);

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
    public void testMoveFromProcessingToOutputQueue_overflow() {
        //create packets
        Packet p1 = new Packet(19000, packetManager, null, 10);
        p1.setQosQueue(0);
        Packet p2 = new Packet(1001, packetManager, null, 30);
        p2.setQosQueue(0);
        Packet p3 = new Packet(1001, packetManager, null, 330);//this creation time is important, because this is time when edge congestion happens
        p3.setQosQueue(0);
        Packet p4 = new Packet(1001, packetManager, null, 350);
        p4.setQosQueue(0);
        Packet p5 = new Packet(1001, packetManager, null, 550);//this creation time is important, because this is time when edge congestion happens
        p5.setQosQueue(0);

        initRoute(p1, p2, p3, p4, p5);

        //first add packets to processing
        node2.addPacketToProcessing(p1);
        node2.addPacketToProcessing(p2);

        //now here comes method I want to test
        node2.movePacketsFromProcessingToOutputQueue(100);

        //the second packet should be dropped
        OutputQueueManager outputQueue = node2.getOutputQueueManager();
        assertNotNull(outputQueue);
        assertEquals(1, outputQueue.getAllUsage()); //only one packet in output queue

        //get edge speed in some time in the future (edge speed is changed due to congestion with some delay - TCP timer delay)
        long speed = edge1.getSpeed(p3);
        assertEquals(edge1.getMaxSpeed() / 2, speed);

        //add another packet - should be dropped, because of congestion
        node2.addPacketToProcessing(p4);
        node2.movePacketsFromProcessingToOutputQueue(500);

        outputQueue = node2.getOutputQueueManager();
        assertNotNull(outputQueue);
        assertEquals(1, outputQueue.getAllUsage()); //only one packet in output queue

        //assert edge speed decrease
        speed = edge1.getSpeed(p5);
        assertEquals(edge1.getMaxSpeed() / 4, speed);


        //edge congestion set should be empty
        TreeSet set = (TreeSet) getPropertyWithoutGetter(Edge.class, edge1, "congestedInfoSet");
        assertNotNull(set);
        assertTrue(set.isEmpty());
    }

    /**
     * 2 packets will be dropped because of congestion and then as many packets as needed will be used to restore max speed
     * this method also tests, if edge speed cannot by more than max
     */
    @Test
    public void testMoveFromProcessingToOutputQueue_congestion_and_recovery() {
        //create packets
        Packet p1 = new Packet(19000, packetManager, null, 10);
        Packet p2 = new Packet(1001, packetManager, null, 30);
        Packet p3 = new Packet(1001, packetManager, null, 330);//this creation time is important, because this is time when edge congestion happens
        Packet p4 = new Packet(1001, packetManager, null, 350);
        Packet p5 = new Packet(1001, packetManager, null, 550);//this creation time is important, because this is time when edge congestion happens
        Packet p6 = new Packet(1001, packetManager, null, 350);
        Packet p7 = new Packet(1001, packetManager, null, 350);
        Packet p8 = new Packet(1001, packetManager, null, 350);
        Packet p9 = new Packet(1001, packetManager, null, 350);
        Packet p10 = new Packet(1001, packetManager, null, 350);

        p1.setQosQueue(0);
        p2.setQosQueue(0);
        p3.setQosQueue(0);
        p4.setQosQueue(0);
        p5.setQosQueue(0);
        p6.setQosQueue(0);
        p7.setQosQueue(0);
        p8.setQosQueue(0);
        p9.setQosQueue(0);
        p10.setQosQueue(0);


        initRoute(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10);

        //first add packets to processing
        node2.addPacketToProcessing(p1);
        node2.addPacketToProcessing(p2);

        //now here comes method I want to test
        node2.movePacketsFromProcessingToOutputQueue(100);

        //the second packet should be dropped
        OutputQueueManager outputQueue = node2.getOutputQueueManager();
        assertNotNull(outputQueue);
        assertEquals(1, outputQueue.getAllUsage()); //only one packet in output queue

        //get edge speed in some time in the future (edge speed is changed due to congestion with some delay - TCP timer delay)
        long speed = edge1.getSpeed(p3);
        assertEquals(edge1.getMaxSpeed() / 2, speed);

        //add another packet - should be dropped, because of congestion
        node2.addPacketToProcessing(p4);
        node2.movePacketsFromProcessingToOutputQueue(500);

        outputQueue = node2.getOutputQueueManager();
        assertNotNull(outputQueue);
        assertEquals(1, outputQueue.getAllUsage()); //only one packet in output queue

        //assert edge speed decrease
        long decreaseSpeed = edge1.getSpeed(p5);
        assertEquals(edge1.getMaxSpeed() / 4, decreaseSpeed);


        //edge congestion set should be empty
        TreeSet set = (TreeSet) getPropertyWithoutGetter(Edge.class, edge1, "congestedInfoSet");
        assertNotNull(set);
        assertTrue(set.isEmpty());

        //----------- now increase edge speed by enlarging output queue and adding packets to it
        setWithoutSetter(OutputQueue.class, q2, "maxCapacity", 100);
        node2.addPacketToProcessing(p6);
        node2.movePacketsFromProcessingToOutputQueue(660);

        outputQueue = node2.getOutputQueueManager();
        assertNotNull(outputQueue);
        assertEquals(2, outputQueue.getAllUsage()); //packet should be added to output queue

        //assert edge speed decrease
        speed = edge1.getSpeed(p5);
        double speed_increment = (Double) getPropertyWithoutGetter(Edge.class, edge1, "EDGE_SPEED_INCREMENT");

        assertEquals(Math.round(decreaseSpeed * speed_increment), speed);

        node2.addPacketToProcessing(p7);
        node2.addPacketToProcessing(p8);
        node2.addPacketToProcessing(p9);
        node2.movePacketsFromProcessingToOutputQueue(660);

        speed = edge1.getSpeed(p5);
        assertEquals(edge1.getMaxSpeed(), speed); //max speed should be restored

        //add one more packet
        node2.addPacketToProcessing(p10);
        node2.movePacketsFromProcessingToOutputQueue(660);

        speed = edge1.getSpeed(p5); //speed should not be above max speed
        assertEquals(edge1.getMaxSpeed(), speed); //max speed should be restored
    }

    /**
     * decrease edge speed to minimum, but no less (that is the point of the test)
     */
    @Test
    public void testMoveFromProcessingToOutputQueue_congestion_minimum() {
        //create packets
        Packet p1 = new Packet(19000, packetManager, null, 10);
        Packet p2 = new Packet(1001, packetManager, null, 30);
        Packet p3 = new Packet(1001, packetManager, null, 330);
        Packet p4 = new Packet(1001, packetManager, null, 350);
        Packet p5 = new Packet(1001, packetManager, null, 550);
        Packet p6 = new Packet(1001, packetManager, null, 350);
        Packet p7 = new Packet(1001, packetManager, null, 350);
        Packet p8 = new Packet(1001, packetManager, null, 350);
        Packet p9 = new Packet(1001, packetManager, null, 350);
        Packet p10 = new Packet(1001, packetManager, null, 750);

        p1.setQosQueue(0);
        p2.setQosQueue(0);
        p3.setQosQueue(0);
        p4.setQosQueue(0);
        p5.setQosQueue(0);
        p6.setQosQueue(0);
        p7.setQosQueue(0);
        p8.setQosQueue(0);
        p9.setQosQueue(0);
        p10.setQosQueue(0);

        initRoute(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10);

        long min_speed = (Long) getPropertyWithoutGetter(Edge.class, edge1, "MIN_SPEED");

        //first add packets to processing
        node2.addPacketToProcessing(p1);
        node2.addPacketToProcessing(p2);
        node2.addPacketToProcessing(p4);
        node2.addPacketToProcessing(p5);
        node2.addPacketToProcessing(p6);
        node2.addPacketToProcessing(p7);
        node2.addPacketToProcessing(p8);

        node2.movePacketsFromProcessingToOutputQueue(660);

        TreeSet congestedInfoSet = (TreeSet) getPropertyWithoutGetter(Edge.class, edge1, "congestedInfoSet");
        assertNotNull(congestedInfoSet);
        assertEquals(3, congestedInfoSet.size());

        long speed = edge1.getSpeed(p10);
        assertEquals(min_speed, speed); //how I should reach minimum edge speed

        node2.addPacketToProcessing(p9); //add 1 more packet - speed must not be less than min speed constant
        node2.movePacketsFromProcessingToOutputQueue(960);

        speed = edge1.getSpeed(p10);
        assertEquals(min_speed, speed);

        assertNotNull(congestedInfoSet);
        assertEquals(0, congestedInfoSet.size());
    }


    /**
     * there are two simulation rules each of them should have got different speeds
     */
    @Test
    public void testMoveFromProcessingToOutputQueue_overflow_two_rules() {
        //create packets
        Packet p1 = new Packet(19000, packetManager, null, 10);//this packet will fill up output queue

        Packet p2 = new Packet(1001, packetManager, null, 10);
        Packet p3 = new Packet(1001, packetManager, null, 30);
        Packet p4 = new Packet(1001, packetManager, null, 330);//this creation time is important, because this is time when edge congestion happens
        Packet p5 = new Packet(1001, packetManager, null, 350);
        Packet p6 = new Packet(1001, packetManager, null, 580);//this creation time is important, because this is time when edge congestion happens

        p1.setQosQueue(0);
        p2.setQosQueue(0);
        p3.setQosQueue(0);
        p4.setQosQueue(0);
        p5.setQosQueue(0);
        p6.setQosQueue(0);


        initRoute(p1, p2, p3, p4);
        initRoute2(p5, p6);

        //first add packets to processing
        node2.addPacketToProcessing(p1);
        node2.addPacketToProcessing(p2);
        node2.addPacketToProcessing(p3);

        //now here comes method I want to test
        node2.movePacketsFromProcessingToOutputQueue(100);

        //the second packet should be dropped
        OutputQueueManager outputQueue = node2.getOutputQueueManager();
        assertNotNull(outputQueue);
        assertEquals(1, outputQueue.getAllUsage()); //only one packet in output queue

        //get edge speed in some time in the future (edge speed is changed due to congestion with some delay - TCP timer delay)
        long speed = edge1.getSpeed(p4);
        assertEquals(edge1.getMaxSpeed() / 4, speed); //-------------------speed for first simul rule should be one quoter

        //add another packet - should be dropped, because of congestion
        node2.addPacketToProcessing(p5);
        node2.movePacketsFromProcessingToOutputQueue(500);

        outputQueue = node2.getOutputQueueManager();
        assertNotNull(outputQueue);
        assertEquals(1, outputQueue.getAllUsage()); //only one packet in output queue

        //assert edge speed decrease
        speed = edge1.getSpeed(p6);
        assertEquals(edge1.getMaxSpeed() / 2, speed); //-------------------speed for first simul rule should be one half


        //edge congestion set should be empty
        TreeSet set = (TreeSet) getPropertyWithoutGetter(Edge.class, edge1, "congestedInfoSet");
        assertNotNull(set);
        assertTrue(set.isEmpty());
    }


    private void initRoute(Packet... packets) {
        SimulationRuleBean simulationRuleBean = new SimulationRuleBean("", node1, node3, 1, 1, 100, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.TCP, IpPrecedence.IP_PRECEDENCE_0, 0, 0);
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
        SimulationRuleBean simulationRuleBean = new SimulationRuleBean("", node1, node3, 1, 1, 100, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.TCP, IpPrecedence.IP_PRECEDENCE_0, 0, 0);
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
