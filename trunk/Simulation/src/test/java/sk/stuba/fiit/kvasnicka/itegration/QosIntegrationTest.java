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

import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.packet.PacketDeliveredEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.packet.PacketDeliveredListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ruleactivation.SimulationRuleActivationListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLogUtils;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PingManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.SimulationManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanismDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.impl.FlowBasedClassification;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.queuemanagement.impl.RandomEarlyDetection;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.RoundRobinScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static sk.stuba.fiit.kvasnicka.TestUtils.initNetworkNode;

/**
 * @author Igor Kvasnicka
 */
public class QosIntegrationTest {
    private NetworkNode node1, node2, node3;
    private Edge edge1, edge2;
    private SimulationManager simulationManager;
    int packetDelivered;

    @Before
    public void before() {
        packetDelivered = 0;

        QosMechanismDefinition qosMechanism1 = new QosMechanismDefinition(null,new RoundRobinScheduling(), new FlowBasedClassification(), new RandomEarlyDetection(new HashMap<String, Object>() {{
            put(RandomEarlyDetection.EXPONENTIAL_WEIGHT_FACTOR, .02);
            put(RandomEarlyDetection.MAX_PROBABILITY, .9);
            put(RandomEarlyDetection.MAX_THRESHOLD, .2);
            put(RandomEarlyDetection.MIN_THRESHOLD, .1);
        }}));

        QosMechanismDefinition qosMechanism2 = new QosMechanismDefinition(null,new RoundRobinScheduling(), new FlowBasedClassification(), new RandomEarlyDetection(new HashMap<String, Object>() {{
            put(RandomEarlyDetection.EXPONENTIAL_WEIGHT_FACTOR, .02);
            put(RandomEarlyDetection.MAX_PROBABILITY, .9);
            put(RandomEarlyDetection.MAX_THRESHOLD, .2);
            put(RandomEarlyDetection.MIN_THRESHOLD, .1);
        }}));

        QosMechanismDefinition qosMechanism3 = new QosMechanismDefinition(null,new RoundRobinScheduling(), new FlowBasedClassification(), new RandomEarlyDetection(new HashMap<String, Object>() {{
            put(RandomEarlyDetection.EXPONENTIAL_WEIGHT_FACTOR, .02);
            put(RandomEarlyDetection.MAX_PROBABILITY, .9);
            put(RandomEarlyDetection.MAX_THRESHOLD, .2);
            put(RandomEarlyDetection.MIN_THRESHOLD, .1);
        }}));


        node1 = new Router("node1", null, qosMechanism1, 10, 10, 50, 10, 10, SimulationTimer.TIME_QUANTUM * 3 / 2, 0, 0);
        node2 = new Router("node2", null, qosMechanism2, 10, 10, 50, 10, 10, SimulationTimer.TIME_QUANTUM * 3 / 2, 0, 0);
        node3 = new Router("node3", null, qosMechanism3, 10, 10, 50, 10, 10, SimulationTimer.TIME_QUANTUM * 3 / 2, 0, 0);

        SimulationLogUtils simulationLogUtils = new SimulationLogUtils();
        initNetworkNode(node1, simulationLogUtils);
        initNetworkNode(node2, simulationLogUtils);
        initNetworkNode(node3, simulationLogUtils);


        edge1 = new Edge(100, 100, 2, 0, node1, node2);
        edge2 = new Edge(100, 100, 3, 0, node2, node3);
    }

    /**
     * this time route from source to destination consists of 3 network nodes
     */
    @Test
    public void test3Nodes() throws NoSuchFieldException, IllegalAccessException {
        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge1, edge2), Arrays.asList(node1, node2, node3), new SimulationLogUtils());

        simulationManager = new SimulationManager();
        SimulationRuleBean rule = new SimulationRuleBean("", node1, node3, 1, 50, 0,  Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0, 0, 0);
        rule.setRoute(Arrays.asList(node1, node2, node3));

        simulationManager.addSimulationRule(rule);

        TestListenerClass testListenerClass = new TestListenerClass();
        rule.addPacketDeliveredListener(testListenerClass);

        timer.startSimulationTimer(simulationManager, new PingManager(), new LinkedList<SimulationRuleActivationListener>());     //here timer is started, however JUnit cannot handle Timers, so I have to simulate timer scheduling (see lines below)

        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);


        assertFalse(timer.isRunning());
        assertEquals(1, packetDelivered);


        checkNoPacketsInTopology(timer);
    }

    private void checkNoPacketsInTopology(SimulationTimer timer) throws NoSuchFieldException, IllegalAccessException {
        Field privateStringField = SimulationTimer.class.getDeclaredField("packetManager");
        privateStringField.setAccessible(true);
        PacketManager packetManager = (PacketManager) privateStringField.get(timer);
        assertTrue(packetManager.checkNoPacketsInSimulation());
    }

    private class TestListenerClass implements PacketDeliveredListener {

        @Override
        public void packetDeliveredOccurred(PacketDeliveredEvent evt) {
            packetDelivered++;
        }
    }
}
