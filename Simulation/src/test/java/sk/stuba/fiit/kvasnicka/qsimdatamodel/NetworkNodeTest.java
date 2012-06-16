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
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.SwQueues;
import sk.stuba.fiit.kvasnicka.qsimsimulation.PacketGenerator;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.NotEnoughBufferSpaceException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.SimulationManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DelayHelper.class)
public class NetworkNodeTest {

    PacketManager packetManager;
    SimulationTimer timer;
    QosMechanism qosMechanism;
    double simulationTime;
    TopologyManager topologyManager;
    NetworkNode node1, node2;
    Edge edge;
    SwQueues swQueues, swQueues2;
    private final int MAX_TX_SIZE = 200;
    private final int MTU = 100;
    private final int MAX_OUTPUT_QUEUE_SIZE = 10;
    private static final int MAX_PROCESSING_PACKETS = 3;
    private final Layer4TypeEnum layer4 = Layer4TypeEnum.UDP;

    @Before
    public void before() {
        simulationTime = 10L;

        qosMechanism = EasyMock.createMock(QosMechanism.class);


        SwQueues.QueueDefinition[] q = new SwQueues.QueueDefinition[2];
        q[0] = new SwQueues.QueueDefinition(50, "queue 1");
        q[1] = new SwQueues.QueueDefinition(1, "queue 2");
        swQueues = new SwQueues(q);

        SwQueues.QueueDefinition[] q2 = new SwQueues.QueueDefinition[1];
        q2[0] = new SwQueues.QueueDefinition(50, "queue 1");
        swQueues2 = new SwQueues(q2);

        EasyMock.expect(qosMechanism.classifyAndMarkPacket(EasyMock.anyObject(Packet.class))).andReturn(0).times(100);
        EasyMock.expect(qosMechanism.decitePacketsToMoveFromOutputQueue(EasyMock.anyObject(List.class), EasyMock.anyObject(SwQueues.class))).andAnswer(new IAnswer<List<Packet>>() {
            @Override
            public List<Packet> answer() throws Throwable {
                return (List<Packet>) EasyMock.getCurrentArguments()[0];
            }
        }).times(100);
        EasyMock.replay(qosMechanism);


        node1 = new Router("node1", qosMechanism, swQueues, MAX_TX_SIZE, 10, 10, MAX_PROCESSING_PACKETS, 100, 0, 0);
        node2 = new Router("node2", qosMechanism, swQueues2, MAX_TX_SIZE, 10, 10, 10, 100, 0, 0);


        edge = new Edge(100, node1, node2);
        edge.setMtu(MTU);
        edge.setPacketErrorRate(0.0);
        edge.setLength(2);

        topologyManager = new TopologyManager(Arrays.asList(edge), Arrays.asList(node1, node2));
        node1.setTopologyManager(topologyManager);
        node2.setTopologyManager(topologyManager);


//        node1.addRoute("node2", "node2");
//        node2.addRoute("node1", "node1");

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
        Packet p1 = new Packet(64, layer4, packetManager, null, 10);
        Packet p2 = new Packet(64, layer4, packetManager, null, 30);

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
        Packet p1 = new Packet(150, layer4, packetManager, null, 10);

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
        Packet p1 = new Packet(200, layer4, packetManager, null, 10);

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
        node1 = new Router("node1", qosMechanism, swQueues, 3, 10, 10, 10, 100, 0, 0);
        node2 = new Router("node2", qosMechanism, swQueues2, 0, 10, 10, 10, 100, 0, 0);

        edge = new Edge(100, node1, node2);
        edge.setMtu(100);
        edge.setPacketErrorRate(0.0);
        edge.setLength(2);

        topologyManager = new TopologyManager(Arrays.asList(edge), Arrays.asList(node1, node2));
        node1.setTopologyManager(topologyManager);
        node2.setTopologyManager(topologyManager);


//        node1.addRoute("node2", "node2");
//        node2.addRoute("node1", "node1");

        //create packets
        Packet p1 = new Packet(200, layer4, packetManager, null, 10);
        Packet p2 = new Packet(101, layer4, packetManager, null, 30);

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
    }

    /**
     * adding packets to output queue - all packets will be added to TX, because there is enough space
     */
    @Test
    public void testMoveFromProcessingToOutputQueue() {
        //create packets
        Packet p1 = new Packet(19000, layer4, packetManager, null, 10);
        Packet p2 = new Packet(1000, layer4, packetManager, null, 30);

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
        Packet p1 = new Packet(19000, layer4, packetManager, null, 10);
        Packet p2 = new Packet(1001, layer4, packetManager, null, 30);

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
        List<Packet> outputQueue = (List<Packet>) getPropertyWithoutGetter(NetworkNode.class, node1, "outputQueue");
        assertNotNull(outputQueue);
        assertEquals(1, outputQueue.size());
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
        Packet p1 = new Packet(100, layer4, packetManager, null, 10);
        Packet p2 = new Packet(200, layer4, packetManager, null, 30);

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
        Packet p1 = new Packet(100, layer4, packetManager, null, 10);
        Packet p2 = new Packet(200, layer4, packetManager, null, 30);

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
        Packet p1 = new Packet(MTU, layer4, packetManager, null, 10);//note that every packet is one 1 fragment big
        Packet p2 = new Packet(MTU, layer4, packetManager, null, 30);

        initRoute(p1, p2);

        p1.setQosQueue(qosMechanism.classifyAndMarkPacket(p1));
        p2.setQosQueue(qosMechanism.classifyAndMarkPacket(p2));

        //add packets directly to output queue - NOT to output buffer
        Method privateStringMethod = NetworkNode.class.getDeclaredMethod("addToOutputQueue", Packet.class);
        privateStringMethod.setAccessible(true);
        privateStringMethod.invoke(node1, p1);
        privateStringMethod.invoke(node1, p2);


        //pre-test check: there should be 0 fragments in TX and 2 packets in output queue
        assertNull(node1.getTxInterfaces().get(node2));

        List<Packet> outputQueue = (List<Packet>) getPropertyWithoutGetter(NetworkNode.class, node1, "outputQueue");
        assertNotNull(outputQueue);
        assertEquals(2, outputQueue.size());


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
        Packet p1 = new Packet(MTU, layer4, packetManager, null, 10);//note that every packet is one 1 fragment big
        Packet p2 = new Packet(MTU, layer4, packetManager, null, 30);
        Packet p3 = new Packet(MTU * (MAX_TX_SIZE - 1), layer4, packetManager, null, 30);//this packet is very big - it will be put into TX

        initRoute(p1, p2, p3);

        p1.setQosQueue(qosMechanism.classifyAndMarkPacket(p1));
        p2.setQosQueue(qosMechanism.classifyAndMarkPacket(p2));

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


        List<Packet> outputQueue = (List<Packet>) getPropertyWithoutGetter(NetworkNode.class, node1, "outputQueue");
        assertNotNull(outputQueue);
        assertEquals(2, outputQueue.size());


        //test method
        node1.moveFromOutputQueueToTxBuffer(40);

        //one packets should be in TX queue and one should left in output queue

        outputQueue = (List<Packet>) getPropertyWithoutGetter(NetworkNode.class, node1, "outputQueue");
        assertNotNull(outputQueue);
        assertEquals(1, outputQueue.size());

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
        Packet p1 = new Packet(MTU, layer4, packetManager, null, 10);
        Packet p2 = new Packet(MTU, layer4, packetManager, null, 30);

        initRoute(p1, p2);

        //test method
        node1.addNewPacketsToOutputQueue(p1);
        node1.addNewPacketsToOutputQueue(p2);

        //assert
        List<Packet> outputQueue = (List<Packet>) getPropertyWithoutGetter(NetworkNode.class, node1, "outputQueue");
        assertNotNull(outputQueue);
        assertEquals(0, outputQueue.size());

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
        Packet p1 = new Packet(MTU * (MAX_TX_SIZE - 1), layer4, packetManager, null, 10);
        Packet p2 = new Packet(MTU * 2, layer4, packetManager, null, 30);

        initRoute(p1, p2);

        //test method
        node1.addNewPacketsToOutputQueue(p1);
        node1.addNewPacketsToOutputQueue(p2);

        //assert
        List<Packet> outputQueue = (List<Packet>) getPropertyWithoutGetter(NetworkNode.class, node1, "outputQueue");
        assertNotNull(outputQueue);
        assertEquals(1, outputQueue.size());

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
        Packet p1 = new Packet(10, layer4, packetManager, null, 10);
        Packet p2 = new Packet(10, layer4, packetManager, null, 30);

        List<Packet> list = new LinkedList<Packet>(Arrays.asList(p1, p2));

        Field f = NetworkNode.class.getDeclaredField("inputQueue");
        f.setAccessible(true);
        f.set(node1, list);

        //test method
        node1.moveFromInputQueueToProcessing(40);

        //assert - both packets should be in processing
        assertEquals(2, node1.getPacketsInProcessing().size());

        List<Packet> inputQueue = (List<Packet>) getPropertyWithoutGetter(NetworkNode.class, node1, "inputQueue");
        assertNotNull(inputQueue);
        assertEquals(0, inputQueue.size());
    }

    /**
     * adds packets to processing, but there should be max 3 packets in processing and I will try to put there 5 packets
     * that means 2 packets should be left in input queue
     */
    @Test
    public void testMoveFromInputQueueToProcessing_overflow() throws NoSuchFieldException, IllegalAccessException {
        //prepare some packets into input queue
        Packet p1 = new Packet(10, layer4, packetManager, null, 10);
        Packet p2 = new Packet(10, layer4, packetManager, null, 30);
        Packet p3 = new Packet(10, layer4, packetManager, null, 30);
        Packet p4 = new Packet(10, layer4, packetManager, null, 30);
        Packet p5 = new Packet(10, layer4, packetManager, null, 30);

        List<Packet> list = new LinkedList<Packet>(Arrays.asList(p1, p2, p3, p4, p5));

        Field f = NetworkNode.class.getDeclaredField("inputQueue");
        f.setAccessible(true);
        f.set(node1, list);

        //test method
        node1.moveFromInputQueueToProcessing(40);

        //assert - both packets should be in processing
        assertEquals(MAX_PROCESSING_PACKETS, node1.getPacketsInProcessing().size());
        List<Packet> inputQueue = (List<Packet>) getPropertyWithoutGetter(NetworkNode.class, node1, "inputQueue");
        assertNotNull(inputQueue);
        assertEquals(2, inputQueue.size());
    }

    /**
     * adds two packets into output queue (NOT TX buffer)
     */
    @Test
    public void testAddToOutputQueue() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Packet p1 = new Packet(10, layer4, packetManager, null, 10);
        Packet p2 = new Packet(10, layer4, packetManager, null, 30);

        p1.setQosQueue(qosMechanism.classifyAndMarkPacket(p1));
        p2.setQosQueue(qosMechanism.classifyAndMarkPacket(p2));

        //test
        Method privateStringMethod = NetworkNode.class.getDeclaredMethod("addToOutputQueue", Packet.class);
        privateStringMethod.setAccessible(true);
        privateStringMethod.invoke(node1, p1);
        privateStringMethod.invoke(node1, p2);

        //assert
        List<Packet> inputQueue = (List<Packet>) getPropertyWithoutGetter(NetworkNode.class, node1, "outputQueue");
        assertNotNull(inputQueue);
        assertEquals(2, inputQueue.size());
    }

    /**
     * adds 3 packets into output queue (NOT TX buffer)
     * one packet will be placed in output queue, the other one will be dropped because there will be no space left for him
     * the third packet however will be added, because it has go different QoS queue number
     */
    @Test
    public void testAddToOutputQueue_overflow() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Packet p1 = new Packet(10, layer4, packetManager, null, 10);
        Packet p2 = new Packet(10, layer4, packetManager, null, 30);
        Packet p3 = new Packet(10, layer4, packetManager, null, 30);


        p1.setQosQueue(1);
        p2.setQosQueue(0);
        p3.setQosQueue(1);

        //test
        Method privateStringMethod = NetworkNode.class.getDeclaredMethod("addToOutputQueue", Packet.class);
        privateStringMethod.setAccessible(true);
        privateStringMethod.invoke(node1, p1);
        privateStringMethod.invoke(node1, p2);
        try {
            privateStringMethod.invoke(node1, p3);
            fail("buffer NotEnoughBufferSpaceException should be thrown");
        } catch (InvocationTargetException e) {
            //seems ok
            if (e.getCause() instanceof NotEnoughBufferSpaceException) {
                //ok
            } else {
                e.printStackTrace();
                fail("exception was thrown, but it is not NotEnoughBufferSpaceException");
            }
        }


        //assert
        List<Packet> inputQueue = (List<Packet>) getPropertyWithoutGetter(NetworkNode.class, node1, "outputQueue");
        assertNotNull(inputQueue);
        assertEquals(2, inputQueue.size());
    }

    /**
     * creating new packets according to SimulationRuleBean
     */
    @Test
    public void testGeneratePackets() throws Exception {

        PowerMock.mockStatic(DelayHelper.class);
        EasyMock.expect(DelayHelper.calculatePacketCreationDelay(EasyMock.anyObject(NetworkNode.class), EasyMock.anyInt(), EasyMock.anyObject(PacketTypeEnum.class))).andReturn(SimulationTimer.TIME_QUANTUM / 2).times(2);
        PowerMock.replayAll();

        SimulationRuleBean rule = new SimulationRuleBean(node1, node2, 2, 50, 0, null, Layer4TypeEnum.UDP, false);
        rule.addRoute(Arrays.asList(node1, node2));

        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge), Arrays.asList(node1, node2));

        SimulationManager simulationManager = new SimulationManager();
        simulationManager.addSimulationRule(rule);
        timer.startSimulationTimer(simulationManager); //need to init all the stuff


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


    private Object getPropertyWithoutGetter(Class klass, Object bean, String field) {
        Field f = null;
        try {
            f = klass.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(bean);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initRoute(Packet... packets) {
        SimulationRuleBean simulationRuleBean = new SimulationRuleBean(node1, node2, 1, 1, 100, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, false);
        simulationRuleBean.addRoute(Arrays.asList(node1, node2));

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
