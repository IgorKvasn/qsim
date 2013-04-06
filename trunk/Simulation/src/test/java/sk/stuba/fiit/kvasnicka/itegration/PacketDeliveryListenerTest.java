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
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.utils.PacketCreationDelayFunction;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.utils.creationdelay.GaussNormalCreationDelay;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.log.SimulationLogEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.log.SimulationLogListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.packet.PacketDeliveredEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.packet.PacketDeliveredListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ping.PingPacketDeliveredEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ping.PingPacketDeliveredListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ruleactivation.SimulationRuleActivationListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.facade.SimulationFacade;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.LogCategory;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLogUtils;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PingManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.SimulationManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanismDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static sk.stuba.fiit.kvasnicka.TestUtils.getPropertyWithoutGetter;
import static sk.stuba.fiit.kvasnicka.TestUtils.initNetworkNode;
import static sk.stuba.fiit.kvasnicka.TestUtils.setWithoutSetter;

/**
 * testing PacketDeliveryListener and PingPacketDeliveryListener
 *
 * @author Igor Kvasnicka
 */
public class PacketDeliveryListenerTest {

    QosMechanismDefinition qosMechanism;
    NetworkNode node1, node2, node3;
    Edge edge1, edge2;
    SimulationManager simulationManager;
    int deliveredPackets, deliveredPingPackets;
    PacketCreationDelayFunction creation1;

    @Before
    public void before() {
        qosMechanism = EasyMock.createMock(QosMechanismDefinition.class);
        EasyMock.expect(qosMechanism.classifyAndMarkPacket(EasyMock.anyObject(NetworkNode.class), EasyMock.anyObject(Packet.class))).andReturn(0).times(100);
        EasyMock.expect(qosMechanism.decitePacketsToMoveFromOutputQueue(EasyMock.anyObject(NetworkNode.class), EasyMock.anyObject(Map.class))).andAnswer(new IAnswer<List<Packet>>() {
            @Override
            public List<Packet> answer() throws Throwable {
                if (((Map<Integer, List<Packet>>) EasyMock.getCurrentArguments()[1]).get(0) == null) {
                    return new LinkedList<Packet>();
                }
                return ((Map<Integer, List<Packet>>) EasyMock.getCurrentArguments()[1]).get(0);
            }
        }).times(100);
        EasyMock.replay(qosMechanism);

        creation1 = new GaussNormalCreationDelay(0, 1, 0, 1);

        node1 = new Router("node1", null, qosMechanism, 10, 10, null, 10, 10, 100, 0, 0);
        node2 = new Router("node2", null, qosMechanism, 10, 10, null, 10, 10, 100, 0, 0);
        node3 = new Router("node3", null, qosMechanism, 10, 10, null, 10, 10, 100, 0, 0);

        SimulationLogUtils simulationLogUtils = new SimulationLogUtils();

        initNetworkNode(node1, simulationLogUtils);
        initNetworkNode(node2, simulationLogUtils);
        initNetworkNode(node3, simulationLogUtils);

        edge1 = new Edge(100, 100, 2, 0, node1, node2);
        edge2 = new Edge(100, 100, 3, 0, node1, node2);
    }


    /**
     * send one ping packet
     */
    @Test
    public void testPingDeliveryListener() throws NoSuchFieldException, IllegalAccessException {
        deliveredPackets = 0;
        deliveredPingPackets = 0;

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, creation1, 1, 50, 0, Layer4TypeEnum.ICMP, IpPrecedence.IP_PRECEDENCE_0, null, 0, 0);
        rule.setRoute(Arrays.asList(node1, node2));

        SimulationFacade simulationFacade = new SimulationFacade();
        simulationFacade.addSimulationRule(rule);
        simulationFacade.initTimer(Arrays.asList(edge1), Arrays.asList(node1, node2));
        simulationFacade.startTimer();

        TestListenerClass testListenerClass = new TestListenerClass();

        rule.addPingPacketDeliveredListener(testListenerClass);
        rule.addPacketDeliveredListener(testListenerClass);

        SimulationTimer timer = (SimulationTimer) getPropertyWithoutGetter(SimulationFacade.class, simulationFacade, "timer");
        SimulationLogUtils logUtils = (SimulationLogUtils) getPropertyWithoutGetter(SimulationTimer.class, timer, "simulationLogUtils");
        logUtils.addSimulationLogListener(new SimulationLogListener() {
            @Override
            public void simulationLogOccurred(SimulationLogEvent evt) {
                if (evt.getSimulationLog().getCategory() == LogCategory.ERROR) {
                    fail("error during simulation: " + evt.getSimulationLog().getCause());
                }
            }
        });
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);

        assertTrue(timer.isEndOfSimulation());

        assertEquals(0, deliveredPackets);
        assertEquals(1, deliveredPingPackets);
    }


    @Test
    public void testDeliveryListener() throws NoSuchFieldException, IllegalAccessException {
        deliveredPackets = 0;
        deliveredPingPackets = 0;

        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge1), Arrays.asList(node1, node2), new SimulationLogUtils());
        SimulationLogUtils logUtils = (SimulationLogUtils) getPropertyWithoutGetter(SimulationTimer.class, timer, "simulationLogUtils");
        logUtils.addSimulationLogListener(new SimulationLogListener() {
            @Override
            public void simulationLogOccurred(SimulationLogEvent evt) {
                if (evt.getSimulationLog().getCategory() == LogCategory.ERROR) {
                    fail("error during simulation: " + evt.getSimulationLog().getCause());
                }
            }
        });
        simulationManager = new SimulationManager();
        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, creation1, 1, 50, 0, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0, null, 0, 0);
        rule.setRoute(Arrays.asList(node1, node2));

        TestListenerClass testListenerClass = new TestListenerClass();

        rule.addPingPacketDeliveredListener(testListenerClass);
        rule.addPacketDeliveredListener(testListenerClass);

        simulationManager.addSimulationRule(rule);


        setWithoutSetter(SimulationTimer.class, timer, "simulationManager", simulationManager);
        timer.startSimulationTimer(simulationManager, new PingManager(), new LinkedList<SimulationRuleActivationListener>());

        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);

        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);

        assertTrue(timer.isEndOfSimulation());

        assertEquals(1, deliveredPackets);
        assertEquals(0, deliveredPingPackets);
    }


    private class TestListenerClass implements PingPacketDeliveredListener, PacketDeliveredListener {

        @Override
        public void packetDeliveredOccurred(PacketDeliveredEvent evt) {
            deliveredPackets++;
        }

        @Override
        public void pingPacketDeliveredOccurred(PingPacketDeliveredEvent evt) {
            deliveredPingPackets++;
        }
    }
}

