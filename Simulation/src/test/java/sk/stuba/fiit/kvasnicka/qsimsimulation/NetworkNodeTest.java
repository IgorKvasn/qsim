package sk.stuba.fiit.kvasnicka.qsimsimulation;

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
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.NotEnoughBufferSpaceException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.SimulationManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DelayHelper.class)
public class NetworkNodeTest {

    public NetworkNodeTest() {
    }

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

    @Before
    public void before() {
        simulationTime = 10L;

        qosMechanism = EasyMock.createMock(QosMechanism.class);


        SwQueues.QueueDefinition[] q = new SwQueues.QueueDefinition[1];
        q[0] = new SwQueues.QueueDefinition(50);
        swQueues = new SwQueues(q);

        SwQueues.QueueDefinition[] q2 = new SwQueues.QueueDefinition[1];
        q2[0] = new SwQueues.QueueDefinition(50);
        swQueues2 = new SwQueues(q2);

        EasyMock.expect(qosMechanism.classifyAndMarkPacket(EasyMock.anyObject(Packet.class))).andReturn(0).times(100);
        EasyMock.expect(qosMechanism.decitePacketsToMoveFromOutputQueue(EasyMock.anyObject(List.class), EasyMock.anyObject(SwQueues.class))).andAnswer(new IAnswer<List<Packet>>() {
            @Override
            public List<Packet> answer() throws Throwable {
                return (List<Packet>) EasyMock.getCurrentArguments()[0];
            }
        }).times(100);
        EasyMock.replay(qosMechanism);


        node1 = new Router("node1", qosMechanism, 2, swQueues, MAX_TX_SIZE, 10, 10, 10);
        node2 = new Router("node2", qosMechanism, 2, swQueues2, MAX_TX_SIZE, 10, 10, 10);


        edge = new Edge(100, node1, node2, MTU);
        edge.setLength(2);

        topologyManager = new TopologyManager(Arrays.asList(edge), Arrays.asList(node1, node2));
        node1.setTopologyManager(topologyManager);
        node2.setTopologyManager(topologyManager);


        node1.addRoute("node2", "node2");
        node2.addRoute("node1", "node1");

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
        Packet p1 = new Packet(64, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10);
        Packet p2 = new Packet(64, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 30);


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
        Packet p1 = new Packet(150, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10);


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
        Packet p1 = new Packet(200, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10);

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
        node1 = new Router("node1", qosMechanism, 2, swQueues, 3, 10, 10, 10);
        node2 = new Router("node2", qosMechanism, 2, swQueues2, 0, 10, 10, 10);

        edge = new Edge(100, node1, node2, 100);
        edge.setLength(2);

        topologyManager = new TopologyManager(Arrays.asList(edge), Arrays.asList(node1, node2));
        node1.setTopologyManager(topologyManager);
        node2.setTopologyManager(topologyManager);


        node1.addRoute("node2", "node2");
        node2.addRoute("node1", "node1");

        //create packets
        Packet p1 = new Packet(200, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10);
        Packet p2 = new Packet(101, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 30);

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
        Packet p1 = new Packet(19000, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10);
        Packet p2 = new Packet(1000, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 30);

        PowerMock.mockStatic(DelayHelper.class);
        EasyMock.expect(DelayHelper.calculateProcessingDelay(node1)).andReturn(0.0).times(2); //there will be no processing delay
        PowerMock.replay(DelayHelper.class);

        //first add packets to processing
        node1.addPacketToProcessing(p1);
        node1.addPacketToProcessing(p2);

        //now here comes method I want to test
        node1.moveFromProcessingToOutputQueue(100);

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
        Packet p1 = new Packet(19000, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10);
        Packet p2 = new Packet(1001, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 30);

        PowerMock.mockStatic(DelayHelper.class);
        EasyMock.expect(DelayHelper.calculateProcessingDelay(node1)).andReturn(0.0).times(2); //there will be no processing delay
        PowerMock.replay(DelayHelper.class);

        //first add packets to processing
        node1.addPacketToProcessing(p1);
        node1.addPacketToProcessing(p2);

        //now here comes method I want to test
        node1.moveFromProcessingToOutputQueue(100);

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
     * creating new packets according to SimulationRuleBean
     */
    @Test
    public void testGeneratePackets() throws Exception {

        PowerMock.mockStatic(DelayHelper.class);
        EasyMock.expect(DelayHelper.calculatePacketCreationDelay(EasyMock.anyObject(NetworkNode.class), EasyMock.anyInt(), EasyMock.anyObject(PacketTypeEnum.class))).andReturn(SimulationTimer.TIME_QUANTUM / 2).times(2);
        PowerMock.replayAll();

        SimulationRuleBean rule = new SimulationRuleBean(node1, node2, 2, 50, true, 0, 3, PacketTypeEnum.AUDIO_PACKET);
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
}
