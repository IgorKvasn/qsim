package sk.stuba.fiit.kvasnicka.itegration;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.SwQueues;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.SimulationManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Igor Kvasnicka
 */
public class EdgeErrorTest {

    QosMechanism qosMechanism;
    TopologyManager topologyManager;
    NetworkNode node1, node2;
    Edge edge;
    private SimulationManager simulationManager;

    @Before
    public void before() {
        SwQueues.QueueDefinition[] q = new SwQueues.QueueDefinition[1];
        q[0] = new SwQueues.QueueDefinition(50, "queue 1");
        SwQueues swQueues = new SwQueues(q);

        SwQueues.QueueDefinition[] q2 = new SwQueues.QueueDefinition[1];
        q2[0] = new SwQueues.QueueDefinition(50, "queue 1");
        SwQueues swQueues2 = new SwQueues(q2);


        qosMechanism = EasyMock.createMock(QosMechanism.class);
        EasyMock.expect(qosMechanism.classifyAndMarkPacket(EasyMock.anyObject(Packet.class))).andReturn(0).times(100);
        EasyMock.expect(qosMechanism.decitePacketsToMoveFromOutputQueue(EasyMock.anyObject(List.class), EasyMock.anyObject(SwQueues.class))).andAnswer(new IAnswer<List<Packet>>() {
            @Override
            public List<Packet> answer() throws Throwable {
                return (List<Packet>) EasyMock.getCurrentArguments()[0];
            }
        }).times(100);
        EasyMock.replay(qosMechanism);

        node1 = new Router("node1", qosMechanism, swQueues, 10, 10, 10, 2.1);
        node2 = new Router("node2", qosMechanism, swQueues2, 10, 10, 10, 2.1);


//        edge = new Edge(100, node1, node2, 100, 0.0);
//        edge.setLength(2);

        edge = PowerMock.createPartialMock(Edge.class, "getPacketErrorRate");
        EasyMock.expect(edge.getPacketErrorRate()).andReturn(5.0).times(1);
        EasyMock.expect(edge.getPacketErrorRate()).andReturn(0.0).times(2);
        PowerMock.replay(edge);

        setWithoutSetter(Edge.class, edge, "fragments", new LinkedList<Packet>());
        setWithoutSetter(Edge.class, edge, "speed", 1000);
        setWithoutSetter(Edge.class, edge, "length", 20);
        setWithoutSetter(Edge.class, edge, "node1", node1);
        setWithoutSetter(Edge.class, edge, "node2", node2);
        setWithoutSetter(Edge.class, edge, "mtu", 1000);
    }

    /**
     * simulate one TCP packet - this means that packet retransmission should occur
     */
    @Test
    public void testSinglePacketSimulation_TCP() throws NoSuchFieldException, IllegalAccessException {


        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge), Arrays.asList(node1, node2));

        simulationManager = new SimulationManager();
        SimulationRuleBean rule = new SimulationRuleBean(node1, node2, 1, 50, true, 0, 1, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.TCP);
        rule.addRoute(Arrays.asList(node1, node2));

        simulationManager.addSimulationRule(rule);

        timer.startSimulationTimer(simulationManager);     //here timer is started, however JUnit cannot handle Timers, so I have to simulate timer scheduling (see lines below)

        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);

        assertFalse(timer.isRunning());

        checkNoPacketsInTopology(timer);
    }

    private void checkNoPacketsInTopology(SimulationTimer timer) throws NoSuchFieldException, IllegalAccessException {
        Field privateStringField = SimulationTimer.class.getDeclaredField("packetManager");
        privateStringField.setAccessible(true);
        PacketManager packetManager = (PacketManager) privateStringField.get(timer);
        assertTrue(packetManager.checkNoPacketsInSimulation());
    }

    private void setWithoutSetter(Class c, Object o, String field, Object value) {

        Field f = null;
        try {
            f = c.getDeclaredField(field);
            f.setAccessible(true);
            f.set(o, value);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
