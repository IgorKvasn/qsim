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

/**
 * @author Igor Kvasnicka
 */

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.OutputQueueManager;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers.RxBuffer;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.InputQueue;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.PacketGenerator;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ruleactivation.SimulationRuleActivationListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.NotEnoughBufferSpaceException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.QueueingHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLogUtils;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PingManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.SimulationManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Fragment;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanismDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static sk.stuba.fiit.kvasnicka.TestUtils.getPropertyWithoutGetter;
import static sk.stuba.fiit.kvasnicka.TestUtils.initNetworkNode;
import static sk.stuba.fiit.kvasnicka.TestUtils.setWithoutSetter;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DelayHelper.class)
public class NetworkNodeTest {

    PacketManager packetManager;
    SimulationTimer timer;
    QosMechanismDefinition qosMechanism;
    double simulationTime;
    TopologyManager topologyManager;
    NetworkNode node1, node2;
    Edge edge;
    private final int MAX_TX_SIZE = 200;
    private final int MTU = 100;
    private final int MAX_OUTPUT_QUEUE_SIZE = 10;
    private static final int MAX_PROCESSING_PACKETS = 3;
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
        EasyMock.expect(qosMechanism.performActiveQueueManagement(EasyMock.anyObject(List.class), EasyMock.anyObject(Packet.class))).andReturn(true);
        EasyMock.expect(qosMechanism.performActiveQueueManagement(EasyMock.anyObject(List.class), EasyMock.anyObject(Packet.class))).andReturn(true);
        EasyMock.expect(qosMechanism.performActiveQueueManagement(EasyMock.anyObject(List.class), EasyMock.anyObject(Packet.class))).andReturn(true);
        EasyMock.expect(qosMechanism.performActiveQueueManagement(EasyMock.anyObject(List.class), EasyMock.anyObject(Packet.class))).andReturn(true);

        EasyMock.replay(qosMechanism);


        node1 = new Router("node1", null, qosMechanism, MAX_TX_SIZE, 10, 50, 10, MAX_PROCESSING_PACKETS, 100, 0, 0);
        node2 = new Router("node2", null, qosMechanism, MAX_TX_SIZE, 10, 50, 10, 10, 100, 0, 0);
        SimulationLogUtils simulationLogUtils = new SimulationLogUtils();
        initNetworkNode(node1, simulationLogUtils);
        initNetworkNode(node2, simulationLogUtils);


        edge = new Edge(100, MTU, 2, 0, node1, node2);

        topologyManager = new TopologyManager(Arrays.asList(edge), Arrays.asList(node1, node2));
        node1.setTopologyManager(topologyManager);
        node2.setTopologyManager(topologyManager);

        timer = EasyMock.createMock(SimulationTimer.class);
        EasyMock.expect(timer.getTopologyManager()).andReturn(topologyManager).times(100);
        EasyMock.replay(timer);

        packetManager = new PacketManager(timer);
    }

    /**
     * both packets should be in the TX
     *
     * @throws Exception
     */
    @Test
    public void testAddToTxBuffer() throws Exception {
        Packet p1 = new Packet(64, packetManager, null, 10);
        Packet p2 = new Packet(64, packetManager, null, 30);

        initRoute(p1, p2);

        node1.addToTxBuffer(p1, 100);
        node1.addToTxBuffer(p2, 100);


        assertNotNull(node1.getTxInterfaces());
        assertNotNull(node1.getTxInterfaces().get(node2));
        assertEquals(2, node1.getTxInterfaces().get(node2).getFragmentsCount()); //there should be no packets on the wire yet
    }


    /**
     * adds one packet to TX  - but size of this packet is bigger than MTU (2 fragments needing)
     * so there should be 2 fragments in TX
     *
     * @throws Exception
     */
    @Test
    public void testAddToTxBuffer_multifragment() throws Exception {
        Packet p1 = new Packet(150, packetManager, null, 10);

        initRoute(p1);

        node1.addToTxBuffer(p1, 100);


        assertNotNull(node1.getTxInterfaces());
        assertNotNull(node1.getTxInterfaces().get(node2));
        assertEquals(2, node1.getTxInterfaces().get(node2).getFragmentsCount()); //there should be no packets on the wire yet
    }

    /**
     * adds one packet to TX  - but size of this packet is bigger than MTU (2 fragments needing)
     * so there should be 2 fragments in TX
     * <p/>
     * the same as test before, but now size of packet modulo MTU is zero
     * (the result should be the same as test before)
     *
     * @throws Exception
     */
    @Test
    public void testAddToTxBuffer_multifragment_2() throws Exception {
        Packet p1 = new Packet(200, packetManager, null, 10);

        initRoute(p1);

        node1.addToTxBuffer(p1, 100);


        assertNotNull(node1.getTxInterfaces());
        assertNotNull(node1.getTxInterfaces().get(node2));
        assertEquals(2, node1.getTxInterfaces().get(node2).getFragmentsCount()); //there should be no packets on the wire yet
    }


    /**
     * adds 2 packets to TX - the first one fits into TX perfectly, the second one is too big for TX,
     * so it should be placed into output queue - however this is a test for TX not for output queue,
     * so this test is successful when adding second packet into TX throws the NotEnoughSpaceException
     *
     * @throws Exception
     */
    @Test
    public void testAddToTxBuffer_overflow() throws Exception {
        //redefine nodes, to make maxTxSize smaller number
        node1 = new Router("node1", null, qosMechanism, 3, 10, 50, 10, 10, 100, 0, 0);
        node2 = new Router("node2", null, qosMechanism, 0, 10, 50, 10, 10, 100, 0, 0);
        SimulationLogUtils simulationLogUtils = new SimulationLogUtils();
        initNetworkNode(node1, simulationLogUtils);
        initNetworkNode(node2, simulationLogUtils);

        edge = new Edge(100, 100, 2, 0, node1, node2);

        topologyManager = new TopologyManager(Arrays.asList(edge), Arrays.asList(node1, node2));
        node1.setTopologyManager(topologyManager);
        node2.setTopologyManager(topologyManager);


        //create packets
        Packet p1 = new Packet(200, packetManager, null, 10);
        Packet p2 = new Packet(101, packetManager, null, 30);

        initRoute(p1, p2);

        //adds them into TX
        node1.addToTxBuffer(p1, 100);
        try {
            node1.addToTxBuffer(p2, 100);
            fail("the second packet is placed into TX - this should not happen");
        } catch (NotEnoughBufferSpaceException e) {
            //OK
        }

        //there should be 2 fragments in TX
        assertNotNull(node1.getTxInterfaces());
        assertNotNull(node1.getTxInterfaces().get(node2));
        assertEquals(2, node1.getTxInterfaces().get(node2).getFragmentsCount()); //there should be no packets on the wire yet
        assertEquals(0, edge.getFragments().size());
    }

    /**
     * adding packets to output queue - all packets will be added to TX, because there is enough space
     */
    @Test
    public void testMoveFromProcessingToOutputQueue() {
        //create packets
        Packet p1 = new Packet(19000, packetManager, null, 10);
        Packet p2 = new Packet(1000, packetManager, null, 30);

        initRoute(p1, p2);

        PowerMock.mockStatic(DelayHelper.class);
        EasyMock.expect(DelayHelper.calculateProcessingDelay(node1)).andReturn(0.0).times(2); //there will be no processing delay
        PowerMock.replay(DelayHelper.class);

        //first add packets to processing
        node1.addPacketToProcessing(p1);
        node1.addPacketToProcessing(p2);

        //now here comes method I want to test
        node1.movePacketsFromProcessingToOutputQueue(100);

        //both packets should be moved to TX buffer (because there IS enough space in TX - no output queue is needed)
        assertNotNull(node1.getTxInterfaces());
        assertNotNull(node1.getTxInterfaces().get(node2));
        int fragments = node1.getTxInterfaces().get(node2).getFragmentsCount();
        assertEquals(p1.getPacketSize() / MTU + p2.getPacketSize() / MTU, fragments);
    }


    /**
     * adding packets to output queue - the first packet will be added to TX, but there will be not enough space for the second packet
     * so it will be added to output queue
     */
    @Test
    public void testMoveFromProcessingToOutputQueue_overflow() {
        //create packets
        Packet p1 = new Packet(19000, packetManager, null, 10);
        Packet p2 = new Packet(1001, packetManager, null, 30);

        p1.setQosQueue(0);
        p2.setQosQueue(0);


        initRoute(p1, p2);

        PowerMock.mockStatic(DelayHelper.class);
        EasyMock.expect(DelayHelper.calculateProcessingDelay(node1)).andReturn(0.0).times(2); //there will be no processing delay
        PowerMock.replay(DelayHelper.class);

        //first add packets to processing
        node1.addPacketToProcessing(p1);
        node1.addPacketToProcessing(p2);

        //now here comes method I want to test
        node1.movePacketsFromProcessingToOutputQueue(100);

        //only the first packet should be moved to TX buffer
        assertNotNull(node1.getTxInterfaces());
        assertNotNull(node1.getTxInterfaces().get(node2));
        int fragments = node1.getTxInterfaces().get(node2).getFragmentsCount();
        assertEquals(p1.getPacketSize() / MTU, fragments);

        //the second packet has got not enough space in TX, so it will be put into output queue
        OutputQueueManager outputQueue = node1.getOutputQueueManager();
        assertNotNull(outputQueue);
        assertEquals(1, outputQueue.getAllUsage());
    }

    /**
     * adds 2 packets to processing
     */
    @Test
    public void testAddPacketToProcessing() {

        //prepare processing delay calculation
        PowerMock.mockStatic(DelayHelper.class);
        EasyMock.expect(DelayHelper.calculateProcessingDelay(node1)).andReturn(10.0).times(2);
        PowerMock.replay(DelayHelper.class);

        //create packets
        Packet p1 = new Packet(100, packetManager, null, 10);
        Packet p2 = new Packet(200, packetManager, null, 30);

        initRoute(p1, p2);

        //add to processing
        node1.addPacketToProcessing(p1);
        node1.addPacketToProcessing(p2);

        //assert
        List<Packet> processed = node1.getProcessingFinishedPacket(40);
        assertNotNull(processed);
        assertEquals(2, processed.size());
    }


    /**
     * adds 2 packets to processing
     * the first one will be processed within given simulation time
     */
    @Test
    public void testAddPacketToProcessing_processingDelay() {

        //prepare processing delay calculation
        PowerMock.mockStatic(DelayHelper.class);
        EasyMock.expect(DelayHelper.calculateProcessingDelay(node1)).andReturn(10.0).times(2);
        PowerMock.replay(DelayHelper.class);

        //create packets
        Packet p1 = new Packet(100, packetManager, null, 10);
        Packet p2 = new Packet(200, packetManager, null, 30);

        initRoute(p1, p2);

        //add to processing
        node1.addPacketToProcessing(p1);
        node1.addPacketToProcessing(p2);

        //assert
        List<Packet> processed = node1.getProcessingFinishedPacket(20);
        assertNotNull(processed);
        assertEquals(1, processed.size());
    }

    /**
     * moves 2 packets from output queue to TX
     */
    @Test
    public void testMoveFromOutputQueueToTxBuffer() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //preparation
        Packet p1 = new Packet(MTU, packetManager, null, 10);//note that every packet is one 1 fragment big
        Packet p2 = new Packet(MTU, packetManager, null, 30);

        initRoute(p1, p2);

        p1.setQosQueue(qosMechanism.classifyAndMarkPacket(node1, p1));
        p2.setQosQueue(qosMechanism.classifyAndMarkPacket(node1, p2));

        //add packets directly to output queue - NOT to output buffer

        Method privateStringMethod = NetworkNode.class.getDeclaredMethod("addToOutputQueue", Packet.class);
        privateStringMethod.setAccessible(true);
        privateStringMethod.invoke(node1, p1);
        privateStringMethod.invoke(node1, p2);


        //pre-test check: there should be 0 fragments in TX and 2 packets in output queue
        assertEquals(0, node1.getTxInterfaces().get(node2).getFragmentsCount());

        OutputQueueManager outputQueue = node1.getOutputQueueManager();
        assertNotNull(outputQueue);
        assertEquals(2, outputQueue.getAllUsage());


        //test method
        node1.moveFromOutputQueueToTxBuffer(40);

        //both packets should be in TX queue
        assertNotNull(node1.getTxInterfaces().get(node2));
        int fragments = node1.getTxInterfaces().get(node2).getFragmentsCount();
        assertEquals(2, fragments);
    }

    /**
     * moves 1 packet from output queue to TX, the other one must leave in output queue,
     * because there is not enough space in TX
     */
    @Test
    public void testMoveFromOutputQueueToTxBuffer_overflow() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //preparation
        Packet p1 = new Packet(MTU, packetManager, null, 10);//note that every packet is one 1 fragment big
        Packet p2 = new Packet(MTU, packetManager, null, 30);
        Packet p3 = new Packet(MTU * (MAX_TX_SIZE - 1), packetManager, null, 30);//this packet is very big - it will be put into TX

        initRoute(p1, p2, p3);

        p1.setQosQueue(qosMechanism.classifyAndMarkPacket(node1, p1));
        p2.setQosQueue(qosMechanism.classifyAndMarkPacket(node1, p2));

        //add packets directly to output queue - NOT to output buffer
        Method privateStringMethod = NetworkNode.class.getDeclaredMethod("addToOutputQueue", Packet.class);
        privateStringMethod.setAccessible(true);
        privateStringMethod.invoke(node1, p1);
        privateStringMethod.invoke(node1, p2);

        //also put some fragments into TX to make space just for one extra fragment
        privateStringMethod = NetworkNode.class.getDeclaredMethod("addToTxBuffer", Packet.class, int.class);
        privateStringMethod.setAccessible(true);
        privateStringMethod.invoke(node1, p3, MTU);


        //pre-test check: there should be 0 fragments in TX and 2 packets in output queue
        assertNotNull(node1.getTxInterfaces().get(node2));
        int fragments = node1.getTxInterfaces().get(node2).getFragmentsCount();
        assertEquals(MAX_TX_SIZE - 1, fragments);


        OutputQueueManager outputQueue = node1.getOutputQueueManager();
        assertNotNull(outputQueue);
        assertEquals(2, outputQueue.getAllUsage());


        //test method
        node1.moveFromOutputQueueToTxBuffer(40);

        //one packets should be in TX queue and one should left in output queue

        outputQueue = node1.getOutputQueueManager();
        assertNotNull(outputQueue);
        assertEquals(1, outputQueue.getAllUsage());

        assertNotNull(node1.getTxInterfaces().get(node2));
        fragments = node1.getTxInterfaces().get(node2).getFragmentsCount();
        assertEquals(MAX_TX_SIZE, fragments);
    }

    /**
     * adds new packets to output queue
     * both packets should be placed in TX buffer, because it is empty
     */
    @Test
    public void testAddNewPacketsToOutputQueue() {
        //preparation
        Packet p1 = new Packet(MTU, packetManager, null, 10);
        Packet p2 = new Packet(MTU, packetManager, null, 30);

        initRoute(p1, p2);

        //test method
        node1.addNewPacketsToOutputQueue(p1);
        node1.addNewPacketsToOutputQueue(p2);

        //assert
        OutputQueueManager outputQueue = node1.getOutputQueueManager();
        assertNotNull(outputQueue);
        assertEquals(0, outputQueue.getAllUsage());

        assertNotNull(node1.getTxInterfaces().get(node2));
        int fragments = node1.getTxInterfaces().get(node2).getFragmentsCount();
        assertEquals(2, fragments);
    }

    /**
     * adds new packets to output queue
     * one packet should be placed in TX buffer and the second one should wait in output queue
     */
    @Test
    public void testAddNewPacketsToOutputQueue_overflow() {
        //preparation
        Packet p1 = new Packet(MTU * (MAX_TX_SIZE - 1), packetManager, null, 10);
        Packet p2 = new Packet(MTU * 2, packetManager, null, 30);

        p1.setQosQueue(0);
        p2.setQosQueue(0);

        initRoute(p1, p2);

        //test method
        node1.addNewPacketsToOutputQueue(p1);
        node1.addNewPacketsToOutputQueue(p2);

        //assert
        OutputQueueManager outputQueue = node1.getOutputQueueManager();
        assertNotNull(outputQueue);
        assertEquals(1, outputQueue.getAllUsage());

        assertNotNull(node1.getTxInterfaces().get(node2));
        int fragments = node1.getTxInterfaces().get(node2).getFragmentsCount();
        assertEquals(MAX_TX_SIZE - 1, fragments);
    }

    /**
     * adds packets to processing
     */
    @Test
    public void testMoveFromInputQueueToProcessing() throws NoSuchFieldException, IllegalAccessException {
        //prepare some packets into input queue
        Packet p1 = new Packet(10, packetManager, null, 10);
        Packet p2 = new Packet(10, packetManager, null, 30);

        InputQueue inputQueueSetter = new InputQueue(100, node1);

        List<Packet> list = new LinkedList<Packet>(Arrays.asList(p1, p2));

        Field f = InputQueue.class.getDeclaredField("inputQueue");
        f.setAccessible(true);
        f.set(inputQueueSetter, list);

        Field fnode = NetworkNode.class.getDeclaredField("inputQueue");
        fnode.setAccessible(true);
        fnode.set(node1, inputQueueSetter);

        //test method
        node1.moveFromInputQueueToProcessing(40);

        //assert - both packets should be in processing
        assertEquals(2, node1.getPacketsInProcessing().size());

        InputQueue inputQueue = (InputQueue) getPropertyWithoutGetter(NetworkNode.class, node1, "inputQueue");
        assertNotNull(inputQueue);
        assertTrue(inputQueue.isEmpty());
    }

    /**
     * adds packets to processing, but there should be max 3 packets in processing and I will try to put there 5 packets
     * that means 2 packets should be left in input queue
     */
    @Test
    public void testMoveFromInputQueueToProcessing_overflow() throws NoSuchFieldException, IllegalAccessException {
        //prepare some packets into input queue
        Packet p1 = new Packet(10, packetManager, null, 10);
        Packet p2 = new Packet(10, packetManager, null, 30);
        Packet p3 = new Packet(10, packetManager, null, 30);
        Packet p4 = new Packet(10, packetManager, null, 30);
        Packet p5 = new Packet(10, packetManager, null, 30);

        List<Packet> list = new LinkedList<Packet>(Arrays.asList(p1, p2, p3, p4, p5));

        InputQueue inputQueueSetter = new InputQueue(100, node1);

        Field f = InputQueue.class.getDeclaredField("inputQueue");
        f.setAccessible(true);
        f.set(inputQueueSetter, list);

        Field fnode = NetworkNode.class.getDeclaredField("inputQueue");
        fnode.setAccessible(true);
        fnode.set(node1, inputQueueSetter);

        //test method
        node1.moveFromInputQueueToProcessing(40);

        //assert - both packets should be in processing
        assertEquals(MAX_PROCESSING_PACKETS, node1.getPacketsInProcessing().size());
        InputQueue inputQueue = (InputQueue) getPropertyWithoutGetter(NetworkNode.class, node1, "inputQueue");
        assertNotNull(inputQueue);
        assertEquals(2, inputQueue.getUsage(), 0);
    }

    /**
     * adds two packets into output queue (NOT TX buffer)
     */
    @Test
    public void testAddToOutputQueue() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Packet p1 = new Packet(10, packetManager, null, 10);
        Packet p2 = new Packet(10, packetManager, null, 30);

        p1.setQosQueue(qosMechanism.classifyAndMarkPacket(node1, p1));
        p2.setQosQueue(qosMechanism.classifyAndMarkPacket(node1, p2));

        //test
        Method privateStringMethod = NetworkNode.class.getDeclaredMethod("addToOutputQueue", Packet.class);
        privateStringMethod.setAccessible(true);
        privateStringMethod.invoke(node1, p1);
        privateStringMethod.invoke(node1, p2);

        //assert
        OutputQueueManager outputQueue = node1.getOutputQueueManager();
        assertNotNull(outputQueue);
        assertEquals(2, outputQueue.getAllUsage());
    }

    /**
     * adds 3 packets into output queue (NOT TX buffer)
     * one packet will be placed in output queue, the other one will be dropped because there will be no space left for him
     * the third packet however will be added, because it has go different QoS queue number
     */
    @Test
    public void testAddToOutputQueue_overflow() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Packet p1 = new Packet(10, packetManager, null, 10);
        Packet p2 = new Packet(10, packetManager, null, 30);
        Packet p3 = new Packet(10, packetManager, null, 30);


        p1.setQosQueue(1);
        p2.setQosQueue(0);
        p3.setQosQueue(1);

        //test
        Method privateStringMethod = NetworkNode.class.getDeclaredMethod("addToOutputQueue", Packet.class);
        privateStringMethod.setAccessible(true);
        privateStringMethod.invoke(node1, p1);
        privateStringMethod.invoke(node1, p2);

        //now both queues should be created, so I will change max queue size in queue number 1 (where next packet will be added)
        node1.getOutputQueueManager().getQueues().toArray();

        HashMap<Integer, OutputQueue> queues = (HashMap<Integer, OutputQueue>) getPropertyWithoutGetter(OutputQueueManager.class, node1.getOutputQueueManager(), "queues");
        OutputQueue outputQueue_1 = queues.get(1);
        setWithoutSetter(OutputQueue.class, outputQueue_1, "maxCapacity", 1);

        //now add another packet - it should be dropped
        try {
            privateStringMethod.invoke(node1, p3);
            fail("buffer NotEnoughBufferSpaceException should be thrown");
        } catch (InvocationTargetException e) {
            //seems ok, so far
            if (e.getCause() instanceof NotEnoughBufferSpaceException) {
                //ok
            } else {
                e.printStackTrace();
                fail("exception was thrown, but it is not NotEnoughBufferSpaceException");
            }
        }


        //assert
        OutputQueueManager outputQueue = node1.getOutputQueueManager();
        assertNotNull(outputQueue);
        assertEquals(2, outputQueue.getAllUsage());
    }

    /**
     * creating new packets according to SimulationRuleBean
     */
    @Test
    public void testGeneratePackets() throws Exception {

        PowerMock.mockStatic(DelayHelper.class);
        EasyMock.expect(DelayHelper.calculatePacketCreationDelay(EasyMock.anyObject(NetworkNode.class), EasyMock.anyInt())).andReturn(SimulationTimer.TIME_QUANTUM / 2).times(2);
        PowerMock.replayAll();

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 2, 50, 0, layer4, IpPrecedence.IP_PRECEDENCE_0, 0, 0);
        rule.setRoute(Arrays.asList(node1, node2));

        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge), Arrays.asList(node1, node2), new SimulationLogUtils());

        SimulationManager simulationManager = new SimulationManager();
        simulationManager.addSimulationRule(rule);

        setWithoutSetter(SimulationTimer.class, timer, "simulationManager", simulationManager);

        timer.startSimulationTimer(simulationManager, new PingManager(), new LinkedList<SimulationRuleActivationListener>()); //need to init all the stuff


        Field privateStringField = SimulationTimer.class.getDeclaredField("packetGenerator");
        privateStringField.setAccessible(true);
        PacketGenerator generator = (PacketGenerator) privateStringField.get(timer);

        generator.generatePackets(0, SimulationTimer.TIME_QUANTUM);
        generator.generatePackets(SimulationTimer.TIME_QUANTUM, SimulationTimer.TIME_QUANTUM);  //I need 2 time quantums

        PowerMock.verifyAll();

        assertNotNull(node1.getTxInterfaces());
        assertNotNull(node1.getTxInterfaces().get(node2));
        int fragments = node1.getTxInterfaces().get(node2).getFragmentsCount();
        assertEquals(2, fragments);
    }

    /**
     * one fragment was dropped
     * this is a test to check if no packet is created and the rest of the fragments are removed after the last fragment came
     *
     * @throws Exception
     */
    @Test
    public void testAddToRxBuffer_overflow_fragments_remove() throws Exception {
        //redefine nodes, to make maxTxSize smaller number
        node1 = new Router("node1", null, qosMechanism, 3, 1, 50, 10, 10, 100, 0, 0);
        node2 = new Router("node2", null, qosMechanism, 0, 3, 50, 10, 10, 100, 0, 0);
        SimulationLogUtils simulationLogUtils = new SimulationLogUtils();
        initNetworkNode(node1, simulationLogUtils);
        initNetworkNode(node2, simulationLogUtils);

        edge = new Edge(100, 100, 2, 0, node1, node2);

        topologyManager = new TopologyManager(Arrays.asList(edge), Arrays.asList(node1, node2));
        node1.setTopologyManager(topologyManager);
        node2.setTopologyManager(topologyManager);


        //create packets
        Packet p1 = new Packet(200, packetManager, null, 10);
        Packet p2 = new Packet(101, packetManager, null, 30);

        initRoute(p1, p2);

        Fragment[] fragments1 = QueueingHelper.createFragments(p1, 10, node1, node2);


        node2.addToRxBuffer(fragments1[0]);//adds first fragment to RX
        assertEquals(1, node2.getRxInterfaces().get(node1).getNumberOfFragments());
        setWithoutSetter(RxBuffer.class, node2.getRxInterfaces().get(node1), "maxRxSize", 1);//temporary change maxRxSize to simulate buffer is full

        node2.addToRxBuffer(fragments1[1]); //tries to add fragment to RX, but it should be dropped
        assertEquals(1, node2.getRxInterfaces().get(node1).getNumberOfFragments());
        setWithoutSetter(RxBuffer.class, node2.getRxInterfaces().get(node1), "maxRxSize", 1000); //all the rest of the fragments will be added


        for (int i = 2; i < fragments1.length; i++) {
            node2.addToRxBuffer(fragments1[i]);
        }

        //all fragments should be removed
        assertEquals(0, node2.getRxInterfaces().get(node1).getNumberOfFragments());
        assertTrue(node2.getInputQueue().isEmpty());
        assertEquals(0, node2.getProcessingPackets());
    }

    /**
     * last fragment was dropped
     * this is a test to check if no packet is created and the rest of the fragments are removed after the last fragment came
     *
     * @throws Exception
     */
    @Test
    public void testAddToRxBuffer_overflow_fragments_remove_last_fragment_dropped() throws Exception {

        node1 = new Router("node1", null, qosMechanism, 3, 300, 50, 10, 10, 100, 0, 0);
        node2 = new Router("node2", null, qosMechanism, 0, 300, 50, 10, 10, 100, 0, 0);
        SimulationLogUtils simulationLogUtils = new SimulationLogUtils();
        initNetworkNode(node1, simulationLogUtils);
        initNetworkNode(node2, simulationLogUtils);

        edge = new Edge(100, 100, 2, 0, node1, node2);

        topologyManager = new TopologyManager(Arrays.asList(edge), Arrays.asList(node1, node2));
        node1.setTopologyManager(topologyManager);
        node2.setTopologyManager(topologyManager);


        //create packets
        Packet p1 = new Packet(200, packetManager, null, 10);
        Packet p2 = new Packet(101, packetManager, null, 30);

        initRoute(p1, p2);

        Fragment[] fragments1 = QueueingHelper.createFragments(p1, 10, node1, node2);

        for (int i = 0; i < fragments1.length - 1; i++) {
            node2.addToRxBuffer(fragments1[i]);
        }
        assertEquals(fragments1.length - 1, node2.getRxInterfaces().get(node1).getNumberOfFragments());

        setWithoutSetter(RxBuffer.class, node2.getRxInterfaces().get(node1), "maxRxSize", fragments1.length - 1);//temporary change maxRxSize to simulate buffer is full

        node2.addToRxBuffer(fragments1[fragments1.length - 1]);//adds last  fragment to RX

        //all fragments should be removed
        assertEquals(0, node2.getRxInterfaces().get(node1).getNumberOfFragments());
        assertTrue(node2.getInputQueue().isEmpty());
        assertEquals(0, node2.getProcessingPackets());
    }


    private void initRoute(Packet... packets) {
        SimulationRuleBean simulationRuleBean = new SimulationRuleBean("", node1, node2, 1, 1, 100, Layer4TypeEnum.TCP, IpPrecedence.IP_PRECEDENCE_0, 0, 0);
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
