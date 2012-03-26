package sk.stuba.fiit.kvasnicka.qsimsimulation;

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
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketStateEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
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
import static org.junit.Assert.assertTrue;

/**
 * @author Igor Kvasnicka
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DelayHelper.class)

public class PacketManagerTest {
    PacketManager packetManager;
    SimulationTimer timer;
    QosMechanism qosMechanism;
    double simulationTime;
    TopologyManager topologyManager;
    NetworkNode node1, node2;
    Edge edge;


    @Before
    public void before() {
        simulationTime = 10L;

        qosMechanism = EasyMock.createMock(QosMechanism.class);
        EasyMock.expect(qosMechanism.markPacket(EasyMock.anyObject(Packet.class))).andReturn(0).times(100);
        EasyMock.expect(qosMechanism.decitePacketsToMoveFromOutputQueue(EasyMock.anyObject(List.class))).andAnswer(new IAnswer<List<Packet>>() {
            @Override
            public List<Packet> answer() throws Throwable {
                return (List<Packet>) EasyMock.getCurrentArguments()[0];
            }
        }).times(100);
        EasyMock.replay(qosMechanism);


        node1 = new Router("node1", qosMechanism, 0, 2, new NetworkNode.QueueDefinition[]{null});
        node2 = new Router("node2", qosMechanism, 0, 2, new NetworkNode.QueueDefinition[]{null});

        node1.getQueues()[0] = new NetworkNode.QueueDefinition(node2, 50);
        node2.getQueues()[0] = new NetworkNode.QueueDefinition(node1, 50);


        edge = new Edge(100, node1, node2);
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

    @Test
    public void testInitPackets() throws Exception {
        //--------prepare

        Packet p1 = new Packet(10, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, simulationTime);
        Packet p2 = new Packet(10, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, simulationTime);

        //------run
        packetManager.initPackets(node1, Arrays.asList(p1, p2), simulationTime);

        //-------assert
        List<Packet> out1 = node1.getPacketsInOutputQueues();
        assertNotNull(out1);
        assertEquals(2, out1.size());
        assertTrue(((out1.get(0) == p1) && (out1.get(1) == p2)) || ((out1.get(0) == p2) && (out1.get(1) == p1)));
        assertEquals(simulationTime, out1.get(0).getTimeWhenNextStateOccures(), 0);
        assertEquals(simulationTime, out1.get(1).getTimeWhenNextStateOccures(), 0);

        assertEquals(PacketStateEnum.OUPUT_BUFFER, out1.get(0).getState());
        assertEquals(PacketStateEnum.OUPUT_BUFFER, out1.get(1).getState());

        List<Packet> out2 = node2.getPacketsInOutputQueues();
        assertNotNull(out2);
        assertTrue(out2.isEmpty());
    }

    /**
     * one packet is in network node, the second one is on the wire
     *
     * @throws Exception
     */
    @Test
    public void testGetAllPacketsOnTheWire() throws Exception {

        Packet p1 = new Packet(64, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10);
        Packet p2 = new Packet(64, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 30);

        p1.setState(PacketStateEnum.SERIALISING_OUTPUT_START); //to init "edge" field in PacketPosition
        p2.setState(PacketStateEnum.SERIALISING_OUTPUT_START);

        p1.setState(PacketStateEnum.OUPUT_BUFFER);

        node1.addToOutputQueue(p1, 0);
        //initial assert that everything is set properly
        assertTrue(p1.getPosition().getNode() != null);
        assertTrue(p2.getPosition().getEdge() != null);
        p2.getPosition().getEdge().addPacket(p2);

        List<Packet> list = packetManager.getAllPacketsOnTheWire(Double.MAX_VALUE);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(PacketStateEnum.SERIALISING_OUTPUT_START, list.get(0).getState());
    }

    /**
     * iterates through all the states possible
     *
     * @throws Exception
     */
    @Test
    public void testChangePacketsState() throws Exception {

        Packet p1 = new Packet(64, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10);
        p1.setState(PacketStateEnum.SERIALISING_OUTPUT_START);
        p1.setTimeWhenNextStateOccures(10);
        edge.addPacket(p1);

        List<Packet> packets = packetManager.getAllPacketsOnTheWire(Double.MAX_VALUE);
        assertEquals(1, packets.size()); //just to be sure - this is supposed to be in other test

        packetManager.changePacketsState(packets, 100);

        assertEquals(PacketStateEnum.PROCESSING, p1.getState());
    }

    /**
     * there is no time to iterate through all states
     *
     * @throws Exception
     */
    @Test
    public void testChangePacketsState_1() throws Exception {

        Packet p1 = new Packet(64, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10);
        p1.setState(PacketStateEnum.SERIALISING_OUTPUT_START);
        edge.addPacket(p1);

        List<Packet> packets = packetManager.getAllPacketsOnTheWire(Double.MAX_VALUE);
        assertEquals(1, packets.size()); //just to be sure - this is supposed to be in other test

        packetManager.changePacketsState(packets, 10);

        assertEquals(PacketStateEnum.ON_THE_WIRE, p1.getState());
    }

    /**
     * 2 packets in network node - one came too late to be moved
     *
     * @throws Exception
     */
    @Test
    public void testMovePacketsFromOutputQueue() throws Exception {
        //prepare

        //the second packet came to the network node AFTER simulation time, so it should be ignored (because in fact is is not there yet)

        Packet p1 = new Packet(64, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10);
        Packet p2 = new Packet(64, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 30);

        p1.setState(PacketStateEnum.SERIALISING_OUTPUT_START); //to init "edge" field in PacketPosition
        p2.setState(PacketStateEnum.SERIALISING_OUTPUT_START);

        p1.setState(PacketStateEnum.OUPUT_BUFFER);
        p2.setState(PacketStateEnum.OUPUT_BUFFER);

        node1.addToOutputQueue(p1, 0);
        node1.addToOutputQueue(p2, 50);

        //run method
        packetManager.movePacketsFromOutputQueue(10);

        //assert
        List<Packet> output = node1.getPacketsInOutputQueues();
        assertNotNull(output);
        assertEquals(1, output.size());

        assertEquals(1, edge.getPackets(Double.MAX_VALUE).size());
        assertEquals(PacketStateEnum.SERIALISING_OUTPUT_START, edge.getPackets(Double.MAX_VALUE).toArray(new Packet[0])[0].getState());
    }


    /**
     * 2 packets in network node - both of them should be moved
     *
     * @throws Exception
     */
    @Test
    public void testMovePacketsFromOutputQueue_2() throws Exception {
        //prepare

        Packet p1 = new Packet(64, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10);
        Packet p2 = new Packet(64, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 30);

        p1.setState(PacketStateEnum.SERIALISING_OUTPUT_START); //to init "edge" field in PacketPosition
        p2.setState(PacketStateEnum.SERIALISING_OUTPUT_START);

        p1.setState(PacketStateEnum.OUPUT_BUFFER);
        p2.setState(PacketStateEnum.OUPUT_BUFFER);

        node1.addToOutputQueue(p1, 0);
        node1.addToOutputQueue(p2, 50);

        //run method
        packetManager.movePacketsFromOutputQueue(100);

        //assert
        List<Packet> output = node1.getPacketsInOutputQueues();
        assertNotNull(output);
        assertEquals(0, output.size());

        assertEquals(2, edge.getPackets(Double.MAX_VALUE).size());
        assertEquals(PacketStateEnum.SERIALISING_OUTPUT_START, edge.getPackets(Double.MAX_VALUE).toArray(new Packet[0])[0].getState());
        assertEquals(PacketStateEnum.SERIALISING_OUTPUT_START, edge.getPackets(Double.MAX_VALUE).toArray(new Packet[0])[1].getState());
    }

    /**
     * 2 packets in network node - ony one should be moved - the second packet has got no time left in this quantum
     *
     * @throws Exception
     */
    @Test
    public void testMovePacketsFromOutputQueue_3() throws Exception {
        //prepare

        Packet p1 = new Packet(100064, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10);   //this macket consumes all the time quantum
        Packet p2 = new Packet(64, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 30);

        p1.setState(PacketStateEnum.SERIALISING_OUTPUT_START); //to init "edge" field in PacketPosition
        p2.setState(PacketStateEnum.SERIALISING_OUTPUT_START);

        p1.setState(PacketStateEnum.OUPUT_BUFFER);
        p2.setState(PacketStateEnum.OUPUT_BUFFER);

        node1.addToOutputQueue(p1, 0);
        node1.addToOutputQueue(p2, 50);

        //run method
        packetManager.movePacketsFromOutputQueue(100);

        //assert
        List<Packet> output = node1.getPacketsInOutputQueues();
        assertNotNull(output);
        assertEquals(1, output.size());

        assertEquals(1, edge.getPackets(Double.MAX_VALUE).size());
        assertEquals(PacketStateEnum.SERIALISING_OUTPUT_START, edge.getPackets(Double.MAX_VALUE).toArray(new Packet[0])[0].getState());
    }


    @Test
    /**
     * there are no packets to move to output queue
     */
    public void testMoveProcessedPackets_empty() throws Exception {
        //prepare


        Packet p1 = new Packet(30, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10);
        Packet p2 = new Packet(30, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10);

        p1.setState(PacketStateEnum.SERIALISING_OUTPUT_START); //to init "edge" field in PacketPosition
        p2.setState(PacketStateEnum.SERIALISING_OUTPUT_START);

        p1.setState(PacketStateEnum.PROCESSING);
        p2.setState(PacketStateEnum.PROCESSING);


        node1.addPacketToProcessing(p1);
        node1.addPacketToProcessing(p2);

        //run method
        packetManager.moveProcessedPackets(5);

        //assert
        List<Packet> list = node1.getPacketsInOutputQueues();
        assertNotNull(list);
        assertEquals(0, list.size());
        List<Packet> proc = node1.getPacketsInProcessing();
        assertNotNull(proc);
        assertEquals(2, proc.size());
    }

    @Test
    /**
     * 1 packet (out of 2) should be moved to output queue
     */
    public void testMoveProcessedPackets() throws Exception {
        //prepare
        Packet p1 = new Packet(30, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10);
        Packet p2 = new Packet(30, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 50);

        p1.setState(PacketStateEnum.SERIALISING_OUTPUT_START); //to init "edge" field in PacketPosition
        p2.setState(PacketStateEnum.SERIALISING_OUTPUT_START);


        p1.setState(PacketStateEnum.PROCESSING);
        p2.setState(PacketStateEnum.PROCESSING);

        node1.addPacketToProcessing(p1);
        node1.addPacketToProcessing(p2);

        //run method
        packetManager.moveProcessedPackets(25);

        //assert
        List<Packet> list = node1.getPacketsInOutputQueues();
        assertNotNull(list);
        assertEquals(1, list.size());
        List<Packet> proc = node1.getPacketsInProcessing();
        assertNotNull(proc);
        assertEquals(1, proc.size());
    }

    @Test
    /**
     * all packets in processing should be moved to output queue
     */
    public void testMoveProcessedPackets_all() throws Exception {
        //prepare
        Packet p1 = new Packet(30, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10);
        Packet p2 = new Packet(30, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 50);

        p1.setState(PacketStateEnum.SERIALISING_OUTPUT_START); //to init "edge" field in PacketPosition
        p2.setState(PacketStateEnum.SERIALISING_OUTPUT_START);


        p1.setState(PacketStateEnum.PROCESSING);
        p2.setState(PacketStateEnum.PROCESSING);

        node1.addPacketToProcessing(p1);
        node1.addPacketToProcessing(p2);

        //run method
        packetManager.moveProcessedPackets(125);

        //assert
        List<Packet> list = node1.getPacketsInOutputQueues();
        assertNotNull(list);
        assertEquals(2, list.size());
        List<Packet> proc = node1.getPacketsInProcessing();
        assertNotNull(proc);
        assertEquals(0, proc.size());
    }


    /**
     * here comes some real deal:
     * first create some packets that are placed into PROCESSING STATE (they should be moved to the next state in the current timer quantum)
     * then call moveProcessed() to move them to output queue
     * after that call moveFromOutputQueue
     * at the end - the first packet should be in SERIALISING_OUTPUT state and the other one is still waiting in the OUTPUT_QUEUE state
     */
    @Test
    public void testMoveProcessed_and_MoveFromOutputQueue() {

        //prepare
        Packet p1 = new Packet(30000, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10);
        Packet p2 = new Packet(30000, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 50);

        p1.setState(PacketStateEnum.SERIALISING_OUTPUT_START); //to init "edge" field in PacketPosition
        p2.setState(PacketStateEnum.SERIALISING_OUTPUT_START);


        p1.setState(PacketStateEnum.PROCESSING);
        p2.setState(PacketStateEnum.PROCESSING);

        node1.addPacketToProcessing(p1);
        node1.addPacketToProcessing(p2);

        //run method
        packetManager.moveProcessedPackets(100);      //I want all the packets to move to Output queue
        packetManager.movePacketsFromOutputQueue(100);

        //ath the end - the first packet should be in SERIALISING_OUTPUT state and the other one is still waiting in the OUTPUT_QUEUE state

        assertEquals(PacketStateEnum.SERIALISING_OUTPUT_START, p1.getState());
        assertEquals(PacketStateEnum.OUPUT_BUFFER, p2.getState());
    }

    /**
     * similar to testMoveProcessed_and_MoveFromOutputQueue but now, there is enough time to serialise both packets
     * so one packet is ON_THE_WIRE and the second one is SERIALISON_OUTPUT
     * <p/>
     * here comes some real deal:
     * first create some packets that are placed into PROCESSING STATE (they should be moved to the next state in the current timer quantum)
     * then call moveProcessed() to move them to output queue
     * after that call moveFromOutputQueue
     * at the end - the first packet should be in SERIALISING_OUTPUT state and the other one is still waiting in the OUTPUT_QUEUE state
     */
    @Test
    public void testMoveProcessed_and_MoveFromOutputQueue_2() {

        //prepare
        Packet p1 = new Packet(30, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10);
        Packet p2 = new Packet(30, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 50);

        p1.setState(PacketStateEnum.SERIALISING_OUTPUT_START); //to init "edge" field in PacketPosition
        p2.setState(PacketStateEnum.SERIALISING_OUTPUT_START);


        p1.setState(PacketStateEnum.PROCESSING);
        p2.setState(PacketStateEnum.PROCESSING);

        node1.addPacketToProcessing(p1);
        node1.addPacketToProcessing(p2);

        //run method
        packetManager.moveProcessedPackets(500);      //I want all the packets to move to Output queue
        packetManager.movePacketsFromOutputQueue(500);

        //at the end - the first packet should be in SERIALISING_OUTPUT state and the other one is still waiting in the OUTPUT_QUEUE state

        assertEquals(PacketStateEnum.SERIALISING_OUTPUT_START, p1.getState()); //well, this packet should be in ON_THE_WIRE, but packetManager.changePacketStates() has not been called, so its state was not changed
        assertEquals(PacketStateEnum.SERIALISING_OUTPUT_START, p2.getState());
    }

    /**
     * tests whether packet is detected to be delivered
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Test
    public void testDelivered() throws NoSuchFieldException, IllegalAccessException {
        Packet p1 = new Packet(30, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10);
        p1.setState(PacketStateEnum.SERIALISING_OUTPUT_START);
        p1.setState(PacketStateEnum.PROCESSING);
        node2.addPacketToProcessing(p1);

        packetManager.moveProcessedPackets(100);

        assertEquals(PacketStateEnum.DELIVERED, p1.getState());
        assertTrue(node2.getPacketsInOutputQueues().isEmpty());
        assertTrue(node2.getPacketsInProcessing().isEmpty());
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

        PowerMock.verifyAll();
        assertEquals(2, node1.getPacketsInOutputQueues().size());
    }
}


//todo vytovir sa len tolko paketov, kolkokrat sa zaobla metoda generatePacketsFromSimulRule - cize ak sa vytvori viac peketov v jednom time quante, tak sa realne vytvori len 1 paket !!