package sk.stuba.fiit.kvasnicka.qsimsimulation;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.SwQueues;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers.InputInterface;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.NotEnoughBufferSpaceException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.QueueingHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Fragment;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Igor Kvasnicka
 */
public class InputInterfaceTest {

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
        q[0] = new SwQueues.QueueDefinition(50, "queue 1");
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
     * test receiving fragments and holding them in RX buffer
     * in this test, no packet is being created
     * all fragments are from the same packet
     */
    @Test
    public void testFragmentReceived() throws NotEnoughBufferSpaceException {
        //prepare
        InputInterface inputInterface = new InputInterface(node1, 10);//I do not care about max RX size
        Packet p1 = new Packet(14, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10); //3 fragments will be created

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
    public void testFragmentReceived_multipacket() throws NotEnoughBufferSpaceException {
        //prepare
        InputInterface inputInterface = new InputInterface(node1, 10);//I do not care about max RX size
        Packet p1 = new Packet(14, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10); //3 fragments will be created
        Packet p2 = new Packet(16, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10); //4 fragments will be created


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
    public void testFragmentReceived_packet_created() throws NotEnoughBufferSpaceException {
        //prepare
        InputInterface inputInterface = new InputInterface(node1, 10);//I do not care about max RX size
        Packet p1 = new Packet(14, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10); //3 fragments will be created

        //test method
        Fragment[] fragments = QueueingHelper.createFragments(p1, 5, node2, node1); //MTU = 5
        //I am going to add all but one fragment into RX - I do not want a packet to be created
        assertNull(inputInterface.fragmentReceived(fragments[0]));
        assertNull(inputInterface.fragmentReceived(fragments[1]));
        Packet p = inputInterface.fragmentReceived(fragments[2]);//now a packet should be created
        assertNotNull(p);

        assertTrue(p == p1); //these should be EXACT objects, so I am comparing references


        //assert - there should be no fragments in RX buffer, neither input queue or processing
        assertEquals(0, inputInterface.getNumberOfFragments());

        List<Packet> outputQueue = (List<Packet>) getPropertyWithoutGetter(NetworkNode.class, node2, "inputQueue");
        assertNotNull(outputQueue);
        assertEquals(0, outputQueue.size());

        assertEquals(0, node2.getPacketsInProcessing().size());//there should be 1 packet in processing, because input queue is empty
    }


    /**
     * test receiving fragments and holding them in RX buffer
     * in this test, a packet  will be created
     * all fragments are from the same packet
     * each packet has got different received time - when a new packet is created, it should have got simulation time of last fragment's "received time"
     */
    @Test
    public void testFragmentReceived_packet_created__simulation_time() throws NotEnoughBufferSpaceException {
        //prepare
        InputInterface inputInterface = new InputInterface(node1, 10);//I do not care about max RX size
        Packet p1 = new Packet(14, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10); //3 fragments will be created

        //test method
        Fragment[] fragments = QueueingHelper.createFragments(p1, 5, node2, node1); //MTU = 5
        fragments[0].setReceivedTime(11);
        fragments[1].setReceivedTime(22);
        fragments[2].setReceivedTime(33);
        //I am going to add all but one fragment into RX - I do not want a packet to be created
        assertNull(inputInterface.fragmentReceived(fragments[0]));
        assertNull(inputInterface.fragmentReceived(fragments[1]));
        Packet p = inputInterface.fragmentReceived(fragments[2]);//now a packet should be created
        assertNotNull(p);

        assertTrue(p == p1); //these should be EXACT objects, so I am comparing references
        assertEquals(fragments[2].getReceivedTime(), p.getSimulationTime(), 0.0);

        //assert - there should be no fragments in RX buffer, neither input queue or processing
        assertEquals(0, inputInterface.getNumberOfFragments());

        List<Packet> outputQueue = (List<Packet>) getPropertyWithoutGetter(NetworkNode.class, node2, "inputQueue");
        assertNotNull(outputQueue);
        assertEquals(0, outputQueue.size());

        assertEquals(0, node2.getPacketsInProcessing().size());//there should be 1 packet in processing, because input queue is empty
    }


    /**
     * test receiving fragments and holding them in RX buffer
     * in this test, a packet  will be created
     * there will be fragments from 2 packets
     * fragments even came in the wrong order, but tested method should handle it
     */
    @Test
    public void testFragmentReceived_packet_created__multiple_packets() throws NotEnoughBufferSpaceException {
        //prepare
        InputInterface inputInterface = new InputInterface(node1, 10);//I do not care about max RX size
        Packet p1 = new Packet(14, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10); //3 fragments will be created
        Packet p2 = new Packet(9, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10); //2 fragments will be created

        //test method
        Fragment[] fragments1 = QueueingHelper.createFragments(p1, 5, node2, node1); //MTU = 5
        Fragment[] fragments2 = QueueingHelper.createFragments(p2, 5, node2, node1); //MTU = 5

        //I am going to add all but one fragment into RX - I do not want a packet to be created
        assertNull(inputInterface.fragmentReceived(fragments1[1]));//1. packet
        assertNull(inputInterface.fragmentReceived(fragments2[0]));//2. packet
        assertNull(inputInterface.fragmentReceived(fragments1[0]));//1. packet

        Packet pp = inputInterface.fragmentReceived(fragments2[1]); //2. packet; now a packet should be created
        assertNotNull(pp);

        Packet p = inputInterface.fragmentReceived(fragments1[2]);//1. packet; now a packet should be created
        assertNotNull(p);

        assertTrue(p == p1); //these should be EXACT objects, so I am comparing references
        assertTrue(pp == p2); //these should be EXACT objects, so I am comparing references


        //assert - there should be no fragments in RX buffer, neither input queue or processing
        assertEquals(0, inputInterface.getNumberOfFragments());

        List<Packet> outputQueue = (List<Packet>) getPropertyWithoutGetter(NetworkNode.class, node2, "inputQueue");
        assertNotNull(outputQueue);
        assertEquals(0, outputQueue.size());

        assertEquals(0, node2.getPacketsInProcessing().size());//there should be 1 packet in processing, because input queue is empty
    }

    /**
     * test receiving fragments and holding them in RX buffer
     * in this test, no packet is being created
     * the purpose of this test is to test overflowing RX buffer
     */
    @Test
    public void testFragmentReceived_overflow() {
        //prepare
        int MAX_RX_SIZE = 2;
        InputInterface inputInterface = new InputInterface(node1, MAX_RX_SIZE);//max 2 fragments in RX
        Packet p1 = new Packet(14, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10); //3 fragments will be created

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

        //assert - there should be 2 fragments in RX buffer
        assertEquals(MAX_RX_SIZE, inputInterface.getNumberOfFragments());
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
