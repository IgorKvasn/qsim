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
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.SwQueues;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers.OutputInterface;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.NotEnoughBufferSpaceException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Igor Kvasnicka
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DelayHelper.class)
public class OutputInterfaceTest {

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
     * serialises 2 packets from TX to the wire
     *
     * @throws NotEnoughBufferSpaceException
     */
    @Test
    public void serialisePackets() throws NotEnoughBufferSpaceException {
        //preparation
        PowerMock.mockStatic(DelayHelper.class);
        EasyMock.expect(DelayHelper.calculateSerialisationDelay(EasyMock.anyObject(Edge.class), EasyMock.anyInt())).andReturn(1.0).times(2);
        EasyMock.expect(DelayHelper.calculatePropagationDelay(EasyMock.anyObject(Edge.class))).andReturn(3.0).times(2);
        PowerMock.replay(DelayHelper.class);

        Packet p1 = new Packet(64, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10);
        Packet p2 = new Packet(64, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 30);

        node1.addToTxBuffer(p1, 100);
        node1.addToTxBuffer(p2, 100);

        //test method
        OutputInterface outputInterface = node1.getTxInterfaces().get(node2);
        outputInterface.serialisePackets(50);

        //assert - all packets should be on the wire
        assertEquals(0, outputInterface.getFragmentsCount());
        assertEquals(2, edge.getFragments().size());

        EasyMock.verify();
    }

    /**
     * serialises 1 packet to the wire, but there will be no time to serialise second packet
     *
     * @throws NotEnoughBufferSpaceException
     */
    @Test
    public void serialisePackets_not_enough_time() throws NotEnoughBufferSpaceException {
        //preparation
        PowerMock.mockStatic(DelayHelper.class);
        EasyMock.expect(DelayHelper.calculateSerialisationDelay(EasyMock.anyObject(Edge.class), EasyMock.anyInt())).andReturn(1.0).times(2);
        EasyMock.expect(DelayHelper.calculatePropagationDelay(EasyMock.anyObject(Edge.class))).andReturn(3.0).times(1);
        PowerMock.replay(DelayHelper.class);

        Packet p1 = new Packet(MTU / 2, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10);
        Packet p2 = new Packet(MTU / 2, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 11); //it is important to set packet creation time greater than first packet's plus serialisation delay

        node1.addToTxBuffer(p1, MTU);
        node1.addToTxBuffer(p2, MTU);

        //test method
        OutputInterface outputInterface = node1.getTxInterfaces().get(node2);
        outputInterface.serialisePackets(20);

        //assert - all packets should be on the wire
        assertEquals(1, outputInterface.getFragmentsCount());
        assertEquals(1, edge.getFragments().size());
        assertEquals(10 + 1 + 1 + 3, edge.getFragments().get(0).getReceivedTime(), 0.0);

        EasyMock.verify();
    }
}
