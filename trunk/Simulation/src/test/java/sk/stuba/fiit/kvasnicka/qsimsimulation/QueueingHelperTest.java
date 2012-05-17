package sk.stuba.fiit.kvasnicka.qsimsimulation;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.SwQueues;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.QueueingHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Fragment;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Igor Kvasnicka
 */
public class QueueingHelperTest {
    public QueueingHelperTest() {
    }


    /**
     * tests method that calculates, how many fragments is needed for a packet
     */
    @Test
    public void testCalculateFragmentSize() {

        int frSize = QueueingHelper.calculateFragmentSize(1, 2, 10, 5);
        assertEquals(5, frSize);

        int frSize2 = QueueingHelper.calculateFragmentSize(1, 2, 10, 11);
        assertEquals(10, frSize2);

        int frSize3 = QueueingHelper.calculateFragmentSize(2, 2, 10, 11);
        assertEquals(1, frSize3);

        int frSize4 = QueueingHelper.calculateFragmentSize(2, 2, 5, 9);
        assertEquals(4, frSize4);

        int frSize5 = QueueingHelper.calculateFragmentSize(1, 3, 5, 10);
        assertEquals(5, frSize5);


        try {
            QueueingHelper.calculateFragmentSize(2, 2, 10, 30);
            fail("this should throw exception, because I need more fragments for this packet - this is a problem in QueueingHelper.calculateNumberOfFragments()");
        } catch (IllegalStateException e) {
            //OK
        }

        try {
            QueueingHelper.calculateFragmentSize(3, 2, 10, 11);
            fail("this should throw exception, because fragment index is bigger than max fragment count");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void testcalculateNumberOfFragments() {
        int frCount1 = QueueingHelper.calculateNumberOfFragments(10, 6);
        assertEquals(2, frCount1);

        int frCount2 = QueueingHelper.calculateNumberOfFragments(10, 5);
        assertEquals(2, frCount2);

        int frCount3 = QueueingHelper.calculateNumberOfFragments(10, 11);
        assertEquals(1, frCount3);

        try {
            QueueingHelper.calculateNumberOfFragments(10, 0);
            fail("this should throw exception, because MTU must not be 0");
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            QueueingHelper.calculateNumberOfFragments(10, - 1);
            fail("this should throw exception, because MTU must not be negative");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void testCreateFragments() {
        //prepare - I need network nodes, so that means I have to initialise the whole topology,...


        QosMechanism qosMechanism = EasyMock.createMock(QosMechanism.class);


        SwQueues.QueueDefinition[] q = new SwQueues.QueueDefinition[1];
        q[0] = new SwQueues.QueueDefinition(50);
        SwQueues swQueues = new SwQueues(q);

        SwQueues.QueueDefinition[] q2 = new SwQueues.QueueDefinition[1];
        q2[0] = new SwQueues.QueueDefinition(50);
        SwQueues swQueues2 = new SwQueues(q2);

        EasyMock.expect(qosMechanism.classifyAndMarkPacket(EasyMock.anyObject(Packet.class))).andReturn(0).times(100);
        EasyMock.expect(qosMechanism.decitePacketsToMoveFromOutputQueue(EasyMock.anyObject(List.class), EasyMock.anyObject(SwQueues.class))).andAnswer(new IAnswer<List<Packet>>() {
            @Override
            public List<Packet> answer() throws Throwable {
                return (List<Packet>) EasyMock.getCurrentArguments()[0];
            }
        }).times(100);
        EasyMock.replay(qosMechanism);


        NetworkNode node1 = new Router("node1", qosMechanism, 2, swQueues, 10, 10, 10, 10);
        NetworkNode node2 = new Router("node2", qosMechanism, 2, swQueues2, 10, 10, 10, 10);


        Edge edge = new Edge(100, node1, node2, 10);
        edge.setLength(2);

        TopologyManager topologyManager = new TopologyManager(Arrays.asList(edge), Arrays.asList(node1, node2));
        node1.setTopologyManager(topologyManager);
        node2.setTopologyManager(topologyManager);

        node1.addRoute("node2", "node2");
        node2.addRoute("node1", "node1");

        SimulationTimer timer = EasyMock.createMock(SimulationTimer.class);
        EasyMock.expect(timer.getTopologyManager()).andReturn(topologyManager).times(100);
        EasyMock.replay(timer);

        PacketManager packetManager = new PacketManager(timer);

        Packet p1 = new Packet(10, node2, node1, packetManager, PacketTypeEnum.AUDIO_PACKET, 10);

        //test method... finally ... and test it on multiple test cases

        Fragment[] fragments = QueueingHelper.createFragments(p1, 10, node1, node2);
        assertEquals(1, fragments.length);

        fragments = QueueingHelper.createFragments(p1, 5, node1, node2);
        assertEquals(2, fragments.length);

        try {
            QueueingHelper.createFragments(p1, 0, node1, node2);
            fail("MTU must not be zero or negative");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }
}
