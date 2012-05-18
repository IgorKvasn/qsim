package sk.stuba.fiit.kvasnicka.qsimsimulation;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.SwQueues;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
    SwQueues swQueues, swQueues2;


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


        node1 = new Router("node1", qosMechanism, 2, swQueues, 200, 10, 10, 10);
        node2 = new Router("node2", qosMechanism, 2, swQueues2, 200, 10, 10, 10);


        edge = new Edge(100, node1, node2, 100);
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
        assertNotNull(node1.getTxInterfaces());
        assertNotNull(node1.getTxInterfaces().get(node2));
        int fragments = node1.getTxInterfaces().get(node2).getFragmentsCount();
        assertEquals(2, fragments);
    }
}