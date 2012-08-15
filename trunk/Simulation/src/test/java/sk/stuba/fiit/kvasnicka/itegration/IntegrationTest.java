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
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.OutputQueueManager;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ruleactivation.SimulationRuleActivationListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLogUtils;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PingManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.SimulationManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static sk.stuba.fiit.kvasnicka.TestUtils.initNetworkNode;

/**
 * @author Igor Kvasnicka
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DelayHelper.class)
public class IntegrationTest {
    QosMechanism qosMechanism;
    NetworkNode node1, node2, node3;
    Edge edge1, edge2;
    private SimulationManager simulationManager;

    @Before
    public void before() {

        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueue q2 = new OutputQueue(50, "queue 2");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});
        OutputQueueManager outputQueueManager2 = new OutputQueueManager(new OutputQueue[]{q2});


        qosMechanism = EasyMock.createMock(QosMechanism.class);
        EasyMock.expect(qosMechanism.classifyAndMarkPacket(EasyMock.anyObject(Packet.class))).andReturn(0).times(100);
        EasyMock.expect(qosMechanism.decitePacketsToMoveFromOutputQueue(EasyMock.anyObject(List.class), EasyMock.anyObject(OutputQueueManager.class))).andAnswer(new IAnswer<List<Packet>>() {
            @Override
            public List<Packet> answer() throws Throwable {
                return (List<Packet>) EasyMock.getCurrentArguments()[0];
            }
        }).times(100);
        EasyMock.replay(qosMechanism);

        node1 = new Router("node1", qosMechanism, outputQueueManager1, 10, 10, 10, 10, 100, 0, 0);
        node2 = new Router("node2", qosMechanism, outputQueueManager2, 10, 10, 10, 10, 100, 0, 0);
        node3 = new Router("node3", qosMechanism, outputQueueManager2, 10, 10, 10, 10, 100, 0, 0);

        SimulationLogUtils simulationLogUtils = new SimulationLogUtils();
        initNetworkNode(node1, simulationLogUtils);
        initNetworkNode(node2, simulationLogUtils);
        initNetworkNode(node3, simulationLogUtils);


        edge1 = new Edge(100, node1, node2);
        edge1.setMtu(100);
        edge1.setPacketErrorRate(0.0);
        edge1.setLength(2);

        edge2 = new Edge(100, node2, node3);
        edge2.setMtu(100);
        edge2.setPacketErrorRate(0.0);
        edge2.setLength(3);
//
//        topologyManager = new TopologyManager(Arrays.asList(edge1, edge2), Arrays.asList(node1, node2, node3));
//        node1.setTopologyManager(topologyManager);
//        node2.setTopologyManager(topologyManager);
//        node3.setTopologyManager(topologyManager);


//        node1.setRoute("node2", "node2");
//        node2.setRoute("node1", "node1");
    }

    /**
     * there are no simulation rules, so simulation timer should end after the first call
     */
    @Test
    public void testNosimulationRules() {

        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge1), Arrays.asList(node1, node2), new SimulationLogUtils());

        simulationManager = new SimulationManager();

        timer.startSimulationTimer(simulationManager, new PingManager(), new LinkedList<SimulationRuleActivationListener>());     //here timer is started, however JUnit cannot handle Timers, so I have to simulate timer scheduling (see lines below)

        timer.actionPerformed(null);

        assertFalse(timer.isRunning());
    }

    /**
     * simulate one packet
     */
    @Test
    public void testSinglePacketSimulation() throws NoSuchFieldException, IllegalAccessException {
        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge1), Arrays.asList(node1, node2), new SimulationLogUtils());

        simulationManager = new SimulationManager();
        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 1, 50, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, false);
        rule.setRoute(Arrays.asList(node1, node2));

        simulationManager.addSimulationRule(rule);

        timer.startSimulationTimer(simulationManager, new PingManager(), new LinkedList<SimulationRuleActivationListener>());     //here timer is started, however JUnit cannot handle Timers, so I have to simulate timer scheduling (see lines below)

        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);


        assertFalse(timer.isRunning());

        checkNoPacketsInTopology(timer);
    }


    /**
     * create multiple packets and see if all of them reach their destination
     */
    @Test
    public void testMultiplePacketsSimulation() throws NoSuchFieldException, IllegalAccessException {
        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge1), Arrays.asList(node1, node2), new SimulationLogUtils());

        simulationManager = new SimulationManager();
        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 2, 50, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, false);
        rule.setRoute(Arrays.asList(node1, node2));

        simulationManager.addSimulationRule(rule);

        timer.startSimulationTimer(simulationManager, new PingManager(), new LinkedList<SimulationRuleActivationListener>());     //here timer is started, however JUnit cannot handle Timers, so I have to simulate timer scheduling (see lines below)

        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);


        assertFalse(timer.isRunning());
        checkNoPacketsInTopology(timer);
    }


    /**
     * create multiple packets and see if all of them reach their destination
     */
    @Test
    public void testMultiplePacketsSimulation_simultaneous() throws NoSuchFieldException, IllegalAccessException {
        //all packets will be created at once - packet creation delay is set to 0
        PowerMock.mockStatic(DelayHelper.class);
        EasyMock.expect(DelayHelper.calculatePacketCreationDelay(EasyMock.anyObject(NetworkNode.class), EasyMock.anyInt(), EasyMock.anyObject(PacketTypeEnum.class))).andReturn(0.0).times(10);
        EasyMock.expect(DelayHelper.calculatePropagationDelay(EasyMock.anyObject(Edge.class))).andReturn(3.0).times(10);
        EasyMock.expect(DelayHelper.calculateProcessingDelay(EasyMock.anyObject(NetworkNode.class))).andReturn(1.0).times(10);
        EasyMock.expect(DelayHelper.calculateSerialisationDelay(EasyMock.anyObject(Edge.class), EasyMock.anyInt())).andReturn(0.2).times(10);

        PowerMock.replay(DelayHelper.class);

        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge1), Arrays.asList(node1, node2), new SimulationLogUtils());

        simulationManager = new SimulationManager();
        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 2, 50, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, false);
        rule.setRoute(Arrays.asList(node1, node2));
        simulationManager.addSimulationRule(rule);

        timer.startSimulationTimer(simulationManager, new PingManager(), new LinkedList<SimulationRuleActivationListener>());     //here timer is started, however JUnit cannot handle Timers, so I have to simulate timer scheduling (see lines below)

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


        assertFalse(timer.isRunning());
        checkNoPacketsInTopology(timer);
    }

    /**
     * simulate one packet
     */
    @Test
    public void testSimulationRuleActivationDelay() throws NoSuchFieldException, IllegalAccessException {
        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge1), Arrays.asList(node1, node2), new SimulationLogUtils());

        simulationManager = new SimulationManager();
        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 1, 50, 2, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, false);
        rule.setRoute(Arrays.asList(node1, node2));

        simulationManager.addSimulationRule(rule);

        timer.startSimulationTimer(simulationManager, new PingManager(), new LinkedList<SimulationRuleActivationListener>());     //here timer is started, however JUnit cannot handle Timers, so I have to simulate timer scheduling (see lines below)

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


        assertFalse(timer.isRunning());


        checkNoPacketsInTopology(timer);
    }


    /**
     * there are no simulation rules, so simulation timer should end after the first call
     *
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     * @throws NoSuchMethodException
     * @throws NoSuchMethodException
     * @throws java.lang.reflect.InvocationTargetException
     *
     */
    @Test
    public void testStopAndClearTimer() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge1), Arrays.asList(node1, node2), new SimulationLogUtils());

        simulationManager = new SimulationManager();
        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 2, 50, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, false);
        rule.setRoute(Arrays.asList(node1, node2));

        simulationManager.addSimulationRule(rule);

        timer.startSimulationTimer(simulationManager, new PingManager(), new LinkedList<SimulationRuleActivationListener>());     //here timer is started, however JUnit cannot handle Timers, so I have to simulate timer scheduling (see lines below)

        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.stopTimer();
        timer.clearSimulationData();

        assertFalse(timer.isRunning());
        checkNoPacketsInTopology(timer);
    }

    /**
     * this time route from source to destination consists of 3 network nodes
     */
    @Test
    public void test3Nodes() throws NoSuchFieldException, IllegalAccessException {
        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge1, edge2), Arrays.asList(node1, node2, node3), new SimulationLogUtils());

        simulationManager = new SimulationManager();
        SimulationRuleBean rule = new SimulationRuleBean("", node1, node3, 1, 50, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, false);
        rule.setRoute(Arrays.asList(node1, node2, node3));

        simulationManager.addSimulationRule(rule);

        timer.startSimulationTimer(simulationManager, new PingManager(), new LinkedList<SimulationRuleActivationListener>());     //here timer is started, however JUnit cannot handle Timers, so I have to simulate timer scheduling (see lines below)

        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);


        assertFalse(timer.isRunning());

        checkNoPacketsInTopology(timer);
    }

    /**
     * starts simulation timer with one simulation rule
     * after a few timer ticks, new rule will be added
     * <p/>
     * new rule should be activated, too
     */
    @Test
    public void testSimulRuleAdd() throws NoSuchFieldException, IllegalAccessException {
        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge1), Arrays.asList(node1, node2), new SimulationLogUtils());
        simulationManager = new SimulationManager();

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 1, 50, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, false);
        rule.setRoute(Arrays.asList(node1, node2));
        simulationManager.addSimulationRule(rule);


        timer.startSimulationTimer(simulationManager, new PingManager(), new LinkedList<SimulationRuleActivationListener>());     //here timer is started, however JUnit cannot handle Timers, so I have to simulate timer scheduling (see lines below)

        //perform 2 timer ticks
        timer.actionPerformed(null);


        //now create and add new simulation rule
        SimulationRuleBean rule2 = new SimulationRuleBean("", node1, node2, 1, 50, 1, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, false);
        rule2.setRoute(Arrays.asList(node1, node2));
        simulationManager.addSimulationRule(rule2);
        timer.actionPerformed(null);

        //continue
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);


        assertFalse("Simulation timer is still running", timer.isRunning());

        checkNoPacketsInTopology(timer);
    }


    private void checkNoPacketsInTopology(SimulationTimer timer) throws NoSuchFieldException, IllegalAccessException {
        Field privateStringField = SimulationTimer.class.getDeclaredField("packetManager");
        privateStringField.setAccessible(true);
        PacketManager packetManager = (PacketManager) privateStringField.get(timer);
        assertTrue(packetManager.checkNoPacketsInSimulation());
    }
}
