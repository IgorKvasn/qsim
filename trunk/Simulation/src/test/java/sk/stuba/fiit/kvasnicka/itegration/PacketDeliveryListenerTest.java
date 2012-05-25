package sk.stuba.fiit.kvasnicka.itegration;


import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.SwQueues;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.packet.PacketDeliveredEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.packet.PacketDeliveredListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ping.PingPacketDeliveredEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ping.PingPacketDeliveredListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.SimulationManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * testing PacketDeliveryListener and PingPacketDeliveryListener
 *
 * @author Igor Kvasnicka
 */
public class PacketDeliveryListenerTest {

    QosMechanism qosMechanism;
    NetworkNode node1, node2, node3;
    Edge edge1, edge2;
    SimulationManager simulationManager;
    int deliveredPackets, deliveredPingPackets;

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

        node1 = new Router("node1", qosMechanism, swQueues, 10, 10, 10, 100);
        node2 = new Router("node2", qosMechanism, swQueues2, 10, 10, 10, 100);
        node3 = new Router("node3", qosMechanism, swQueues2, 10, 10, 10, 100);


        edge1 = new Edge(100, node1, node2, 100, 0.0);
        edge1.setLength(2);

        edge2 = new Edge(100, node2, node3, 100, 0.0);
        edge2.setLength(3);
    }


    /**
     * send one ping packet
     */
    @Test
    public void testPingDeliveryListener() throws NoSuchFieldException, IllegalAccessException {
        deliveredPackets = 0;
        deliveredPingPackets = 0;
        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge1), Arrays.asList(node1, node2));

        simulationManager = new SimulationManager();


        SimulationRuleBean rule = new SimulationRuleBean(node1, node2, 1, 50, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, true);
        rule.addRoute(Arrays.asList(node1, node2));

        timer.startSimulationTimer(simulationManager);
        timer.addPingSimulationRule(rule, 1);

        TestListenerClass testListenerClass = new TestListenerClass();

        rule.addPingPacketDeliveredListener(testListenerClass);
        rule.addPacketDeliveredListener(testListenerClass);

        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);

        assertFalse(timer.isRunning());

        assertEquals(0, deliveredPackets);
        assertEquals(1, deliveredPingPackets);
    }


    @Test
    public void testDeliveryListener() throws NoSuchFieldException, IllegalAccessException {
        deliveredPackets = 0;
        deliveredPingPackets = 0;

        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge1), Arrays.asList(node1, node2));

        simulationManager = new SimulationManager();
        SimulationRuleBean rule = new SimulationRuleBean(node1, node2, 1, 50, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, false);
        rule.addRoute(Arrays.asList(node1, node2));

        TestListenerClass testListenerClass = new TestListenerClass();

        rule.addPingPacketDeliveredListener(testListenerClass);
        rule.addPacketDeliveredListener(testListenerClass);

        simulationManager.addSimulationRule(rule);

        timer.startSimulationTimer(simulationManager);

        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);


        assertFalse(timer.isRunning());

        assertEquals(1, deliveredPackets);
        assertEquals(0, deliveredPingPackets);
    }


    private class TestListenerClass implements PingPacketDeliveredListener, PacketDeliveredListener {

        @Override
        public void packetDeliveredOccurred(PacketDeliveredEvent evt) {
            deliveredPackets++;
        }

        @Override
        public void packetDeliveredOccurred(PingPacketDeliveredEvent evt) {
            deliveredPingPackets++;
        }
    }
}
