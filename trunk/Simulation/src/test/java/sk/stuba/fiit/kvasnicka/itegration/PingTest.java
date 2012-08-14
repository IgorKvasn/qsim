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

package sk.stuba.fiit.kvasnicka.itegration;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.SwQueues;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.log.SimulationLogEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.log.SimulationLogListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.facade.SimulationFacade;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static sk.stuba.fiit.kvasnicka.TestUtils.getPropertyWithoutGetter;

/**
 * @author Igor Kvasnicka
 */
public class PingTest {
    int packetsDelivered;
    QosMechanism qosMechanism;
    NetworkNode node1, node2, node3;
    Edge edge1, edge2;
    ListenerClass listener;

    @Before
    public void before() {

        listener = new ListenerClass();
        packetsDelivered = 0;


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

        node1 = new Router("node1", qosMechanism, swQueues, 10, 10, 10, 10, 100, 0, 0);
        node2 = new Router("node2", qosMechanism, swQueues2, 10, 10, 10, 10, 100, 0, 0);
        node3 = new Router("node3", qosMechanism, swQueues2, 10, 10, 10, 10, 100, 0, 0);


        edge1 = new Edge(100000000, node1, node2);
        edge1.setMtu(100);
        edge1.setPacketErrorRate(0.0);
        edge1.setLength(2000);


        edge2 = new Edge(100, node2, node3);
        edge2.setMtu(100);
        edge2.setPacketErrorRate(0.0);
        edge2.setLength(3);
    }


    /**
     * simulate one packet
     */
    @Test
    public void testSinglePacketSimulation() throws NoSuchFieldException, IllegalAccessException {

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 1, 50, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, true);
        rule.setRoute(Arrays.asList(node1, node2));

        SimulationFacade simulationFacade = new SimulationFacade();
        simulationFacade.addSimulationRule(rule);
        simulationFacade.initTimer(Arrays.asList(edge1), Arrays.asList(node1, node2));
        SimulationTimer timer = (SimulationTimer) getPropertyWithoutGetter(SimulationFacade.class, simulationFacade, "timer");
        simulationFacade.addSimulationLogListener(listener);

        simulationFacade.startTimer();


        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);

        assertFalse(timer.isRunning());
        assertEquals(1, packetsDelivered);
    }

    /**
     * this time route from source to destination consists of 3 network nodes
     */
    @Test
    public void testSinglePacket_3Nodes() throws NoSuchFieldException, IllegalAccessException {

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node3, 1, 50, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, true);
        rule.setRoute(Arrays.asList(node1, node2, node3));

        SimulationFacade simulationFacade = new SimulationFacade();
        simulationFacade.addSimulationRule(rule);
        simulationFacade.initTimer(Arrays.asList(edge1, edge2), Arrays.asList(node1, node2, node3));
        SimulationTimer timer = (SimulationTimer) getPropertyWithoutGetter(SimulationFacade.class, simulationFacade, "timer");
        simulationFacade.addSimulationLogListener(listener);

        simulationFacade.startTimer();

        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);

        assertFalse(timer.isRunning());
        assertEquals(1, packetsDelivered);
    }


    /**
     * this ping goes on and on
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Test
    public void testSinglePacketSimulation_infinitePing() throws NoSuchFieldException, IllegalAccessException {
        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, - 1, 50, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, true);    //notice this -1
        rule.setRoute(Arrays.asList(node1, node2));

        SimulationFacade simulationFacade = new SimulationFacade();
        simulationFacade.addSimulationRule(rule);
        simulationFacade.initTimer(Arrays.asList(edge1), Arrays.asList(node1, node2));
        simulationFacade.startTimer();

        SimulationTimer timer = (SimulationTimer) getPropertyWithoutGetter(SimulationFacade.class, simulationFacade, "timer");

        //simulate many timer ticks
        for (int i = 0; i < 15; i++) {
            timer.actionPerformed(null);
        }


        assertTrue(timer.isRunning());    //timer has not yet finished
    }

    private class ListenerClass implements SimulationLogListener {

        @Override
        public void simulationLogOccurred(SimulationLogEvent evt) {
            if (evt.getSimulationLog().getCause().equals("Starting simulation timer")) return;
            if (evt.getSimulationLog().getCause().equals("Nothing to simulate - simulation stopped")) return;

            if (evt.getSimulationLog().getCause().startsWith("Ping packet delivered in:")) {
                packetsDelivered++;
            } else {
                fail("Bad simulation log message: " + evt.getSimulationLog().getCause());
            }
        }
    }
}