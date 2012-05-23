package sk.stuba.fiit.kvasnicka.qsimsimulation.buffers;

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
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.NotEnoughBufferSpaceException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;

import java.lang.reflect.Field;
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
    private final Layer4TypeEnum layer4 = Layer4TypeEnum.UDP;

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


        node1 = new Router("node1", qosMechanism, swQueues, MAX_TX_SIZE, 10, 10, 100);
        node2 = new Router("node2", qosMechanism, swQueues2, MAX_TX_SIZE, 10, 10, 100);


        edge = new Edge(100, node1, node2, MTU, 0.0);
        edge.setLength(2);

        topologyManager = new TopologyManager(Arrays.asList(edge), Arrays.asList(node1, node2));
        node1.setTopologyManager(topologyManager);
        node2.setTopologyManager(topologyManager);

//
//        node1.addRoute("node2", "node2");
//        node2.addRoute("node1", "node1");

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
    public void testSerialisePackets() throws NotEnoughBufferSpaceException {
        //preparation
        PowerMock.mockStatic(DelayHelper.class);
        EasyMock.expect(DelayHelper.calculateSerialisationDelay(EasyMock.anyObject(Edge.class), EasyMock.anyInt())).andReturn(1.0).times(2);
        EasyMock.expect(DelayHelper.calculatePropagationDelay(EasyMock.anyObject(Edge.class))).andReturn(3.0).times(2);
        PowerMock.replay(DelayHelper.class);

        Packet p1 = new Packet(64, node2, node1, layer4, packetManager, null, 10);
        Packet p2 = new Packet(64, node2, node1, layer4, packetManager, null, 30);

        initRoute(p1, p2);

        node1.addToTxBuffer(p1, 100);
        node1.addToTxBuffer(p2, 100);

        //test method
        OutputInterface outputInterface = node1.getTxInterfaces().get(node2);
        outputInterface.serialisePackets(50);

        //assert - all packets should be on the wire
        assertEquals(0, outputInterface.getFragmentsCount());
        assertEquals(2, edge.getFragments().size());
        assertEquals(10 + 1 + 1 + 3, edge.getFragments().get(0).getReceivedTime(), 0.0);
        assertEquals(30 + 1 + 1 + 3, edge.getFragments().get(1).getReceivedTime(), 0.0);

        EasyMock.verify();
    }

    /**
     * serialises 2 packets to the wire and the third one is out of simulation time
     * I do not care about timing now
     *
     * @throws NotEnoughBufferSpaceException
     */
    @Test
    public void testSerialisePackets_not_enough_time() throws NotEnoughBufferSpaceException {
        //preparation
        PowerMock.mockStatic(DelayHelper.class);
        EasyMock.expect(DelayHelper.calculateSerialisationDelay(EasyMock.anyObject(Edge.class), EasyMock.anyInt())).andReturn(1.0).times(2);
        EasyMock.expect(DelayHelper.calculatePropagationDelay(EasyMock.anyObject(Edge.class))).andReturn(3.0).times(2);
        PowerMock.replay(DelayHelper.class);

        Packet p1 = new Packet(MTU / 2, node2, node1, layer4, packetManager, null, 10);//OK
        Packet p2 = new Packet(MTU / 2, node2, node1, layer4, packetManager, null, 12); //OK;packet will be ready to serialise after previous serialisation ends
        Packet p3 = new Packet(MTU / 2, node2, node1, layer4, packetManager, null, 50); //packet will be ready to serialise some after simulation time

        initRoute(p1, p2, p3);

        node1.addToTxBuffer(p1, MTU);
        node1.addToTxBuffer(p2, MTU);
        node1.addToTxBuffer(p3, MTU);

        //test method
        OutputInterface outputInterface = node1.getTxInterfaces().get(node2);
        outputInterface.serialisePackets(20);

        //assert - 2 packets should left in TX and 2 packets should be moved on the edge
        assertEquals(1, outputInterface.getFragmentsCount());
        assertEquals(2, edge.getFragments().size());


        EasyMock.verify();
    }


    /**
     * serialises 1 packet to the wire, but the second packet have to wait until the second will be serialised
     * multiple are serialised within given time quantum
     *
     * @throws NotEnoughBufferSpaceException
     */
    @Test
    public void testSerialisePackets_not_enough_time_2() throws NotEnoughBufferSpaceException {
        //preparation
        PowerMock.mockStatic(DelayHelper.class);
        EasyMock.expect(DelayHelper.calculateSerialisationDelay(EasyMock.anyObject(Edge.class), EasyMock.anyInt())).andReturn(5.0).times(2);
        EasyMock.expect(DelayHelper.calculatePropagationDelay(EasyMock.anyObject(Edge.class))).andReturn(3.0).times(2);
        PowerMock.replay(DelayHelper.class);

        Packet p1 = new Packet(MTU / 2, node2, node1, layer4, packetManager, null, 10);
        Packet p2 = new Packet(MTU / 2, node2, node1, layer4, packetManager, null, 12);

        initRoute(p1, p2);

        node1.addToTxBuffer(p1, MTU);
        node1.addToTxBuffer(p2, MTU);

        //test method
        OutputInterface outputInterface = node1.getTxInterfaces().get(node2);
        outputInterface.serialisePackets(50);

        //assert - all packets should be on the wire
        assertEquals(0, outputInterface.getFragmentsCount());
        assertEquals(2, edge.getFragments().size());

        assertEquals(10 + 5 + 5 + 3, edge.getFragments().get(0).getReceivedTime(), 0.0);//first packet
        //explanation: packet will come at sim. time 10, it will be 2 times serialised and once propagated


        assertEquals((10 + 5) + 5 + 5 + 3, edge.getFragments().get(1).getReceivedTime(), Double.MIN_VALUE);//second packet
        //explanation: the first packet will be serialised at 10+5 (simulationTime+serialisationTime) - then it will be 2 times serialised and propagated

        EasyMock.verify();
    }

    /**
     * first packet came - it starts to be serialised (serialisationEndTime = 15)
     * second packet came before first - it has to wait it will bi serialised at 15 + 5 (15 is time when first pacet is finished)
     * third packet came after the second packet is serialised - it should not wait, so it will be serialised at 25 + 5 (25 is time when it came)
     *
     * @throws NotEnoughBufferSpaceException
     */
    @Test
    public void testSerialisePackets_complicated() throws NotEnoughBufferSpaceException {
        //preparation
        PowerMock.mockStatic(DelayHelper.class);
        EasyMock.expect(DelayHelper.calculateSerialisationDelay(EasyMock.anyObject(Edge.class), EasyMock.anyInt())).andReturn(5.0).times(4);
        EasyMock.expect(DelayHelper.calculatePropagationDelay(EasyMock.anyObject(Edge.class))).andReturn(3.0).times(3);
        PowerMock.replay(DelayHelper.class);

        Packet p1 = new Packet(MTU / 2, node2, node1, layer4, packetManager, null, 10);
        Packet p2 = new Packet(MTU / 2, node2, node1, layer4, packetManager, null, 9);
        Packet p3 = new Packet(MTU / 2, node2, node1, layer4, packetManager, null, 25);

        initRoute(p1, p2, p3);


        node1.addToTxBuffer(p1, MTU);
        node1.addToTxBuffer(p2, MTU);
        node1.addToTxBuffer(p3, MTU);

        //test method
        OutputInterface outputInterface = node1.getTxInterfaces().get(node2);
        outputInterface.serialisePackets(50);

        //assert - all packets should be on the wire
        assertEquals(0, outputInterface.getFragmentsCount());
        assertEquals(3, edge.getFragments().size());

        assertEquals(23, edge.getFragments().get(0).getReceivedTime(), 0.0);
        assertEquals(28, edge.getFragments().get(1).getReceivedTime(), 0.0);
        assertEquals(38, edge.getFragments().get(2).getReceivedTime(), 0.0);
    }


    /**
     * run serialise method twice
     *
     * @throws NotEnoughBufferSpaceException
     */
    @Test
    public void testSerialisePackets_complicated_2() throws NotEnoughBufferSpaceException {
        //preparation
        PowerMock.mockStatic(DelayHelper.class);
        EasyMock.expect(DelayHelper.calculateSerialisationDelay(EasyMock.anyObject(Edge.class), EasyMock.anyInt())).andReturn(5.0).times(4);
        EasyMock.expect(DelayHelper.calculatePropagationDelay(EasyMock.anyObject(Edge.class))).andReturn(3.0).times(3);
        PowerMock.replay(DelayHelper.class);

        Packet p1 = new Packet(MTU / 2, node2, node1, layer4, packetManager, null, 10);
        Packet p2 = new Packet(MTU / 2, node2, node1, layer4, packetManager, null, 9);
        Packet p3 = new Packet(MTU / 2, node2, node1, layer4, packetManager, null, 25);

        initRoute(p1, p2, p3);


        node1.addToTxBuffer(p1, MTU);
        node1.addToTxBuffer(p2, MTU);
        node1.addToTxBuffer(p3, MTU);

        //test method
        OutputInterface outputInterface = node1.getTxInterfaces().get(node2);
        outputInterface.serialisePackets(15);
        outputInterface.serialisePackets(30);


        //assert - all packets should be on the wire
        assertEquals(0, outputInterface.getFragmentsCount());
        assertEquals(3, edge.getFragments().size());

        assertEquals(23, edge.getFragments().get(0).getReceivedTime(), 0.0);
        assertEquals(28, edge.getFragments().get(1).getReceivedTime(), 0.0);
        assertEquals(38, edge.getFragments().get(2).getReceivedTime(), 0.0);
    }


    /**
     * run serialise method twice - this time the third packet will net be serialised, because there is no time left
     *
     * @throws NotEnoughBufferSpaceException
     */
    @Test
    public void testSerialisePackets_complicated_3() throws NotEnoughBufferSpaceException {
        //preparation
        PowerMock.mockStatic(DelayHelper.class);
        EasyMock.expect(DelayHelper.calculateSerialisationDelay(EasyMock.anyObject(Edge.class), EasyMock.anyInt())).andReturn(5.0).times(4);
        EasyMock.expect(DelayHelper.calculatePropagationDelay(EasyMock.anyObject(Edge.class))).andReturn(3.0).times(3);
        PowerMock.replay(DelayHelper.class);

        Packet p1 = new Packet(MTU / 2, node2, node1, layer4, packetManager, null, 10);
        Packet p2 = new Packet(MTU / 2, node2, node1, layer4, packetManager, null, 9);
        Packet p3 = new Packet(MTU / 2, node2, node1, layer4, packetManager, null, 25);

        initRoute(p1, p2, p3);


        node1.addToTxBuffer(p1, MTU);
        node1.addToTxBuffer(p2, MTU);
        node1.addToTxBuffer(p3, MTU);

        //test method
        OutputInterface outputInterface = node1.getTxInterfaces().get(node2);
        outputInterface.serialisePackets(15);
        outputInterface.serialisePackets(29); //notice this simulation time - if it was 30, third packet would be serialised


        //assert - all packets should be on the wire
        assertEquals(1, outputInterface.getFragmentsCount());
        assertEquals(2, edge.getFragments().size());

        assertEquals(23, edge.getFragments().get(0).getReceivedTime(), 0.0);
        assertEquals(28, edge.getFragments().get(1).getReceivedTime(), 0.0);
    }

    private void initRoute(Packet... packets) {
        SimulationRuleBean simulationRuleBean = new SimulationRuleBean(node1, node2, 1, 1, true, 10, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP);
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
