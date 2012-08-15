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

package sk.stuba.fiit.kvasnicka.qsimsimulation.logs;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.OutputQueueManager;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.log.SimulationLogEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.log.SimulationLogListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ruleactivation.SimulationRuleActivationListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.facade.SimulationFacade;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PingManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.SimulationManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static sk.stuba.fiit.kvasnicka.TestUtils.getPropertyWithoutGetter;
import static sk.stuba.fiit.kvasnicka.TestUtils.initNetworkNode;

/**
 * @author Igor Kvasnicka
 */
public class SimulationLogUtilTest {
    private QosMechanism qosMechanism;
    private NetworkNode node1, node2;
    private Edge edge1;
    private SimulationManager simulationManager;
    private boolean logged;
    private ListenerClass listener;
    private SimulationLogUtils simulationLogUtils;

    @Before
    public void before() {

        logged = false;
        listener = new ListenerClass();
        simulationLogUtils = new SimulationLogUtils();

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

        initNetworkNode(node1, simulationLogUtils);
        initNetworkNode(node2, simulationLogUtils);


        edge1 = new Edge(100, node1, node2);
        edge1.setMtu(100);
        edge1.setPacketErrorRate(0.0);
        edge1.setLength(2);
    }

    @After
    public void after() {
        simulationLogUtils.removeSimulationLogListener(listener);
    }

    /**
     * 1. adds listener
     * 2. make a simple packet delivery (between 2 nodes - nothing fancy)
     * 3. event should be fired
     */
    @Test
    public void testSimulationLogListener() {
        //create and register listener

        simulationLogUtils.addSimulationLogListener(listener);

        //run simulation
        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge1), Arrays.asList(node1, node2), simulationLogUtils);
        simulationManager = new SimulationManager();
        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 1, 50, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, false);
        rule.setRoute(Arrays.asList(node1, node2));

        simulationManager.addSimulationRule(rule);

        timer.startSimulationTimer(simulationManager, new PingManager(), new LinkedList<SimulationRuleActivationListener>());

        timer.actionPerformed(null);
        timer.actionPerformed(null);
        timer.actionPerformed(null);

        assertTrue(logged);
    }

    /**
     * each simulation facade should have got separate SimulationLogUtils object (class instance)
     * also network nodes associated with each simulation facade should have got reference to its simulation facade
     */
    @Test
    public void testMultipleSimulationLogs() {
        NetworkNode node11 = new Router("node11", qosMechanism, null, 10, 10, 10, 10, 100, 0, 0);
        NetworkNode node12 = new Router("node12", qosMechanism, null, 10, 10, 10, 10, 100, 0, 0);
        NetworkNode node21 = new Router("node21", qosMechanism, null, 10, 10, 10, 10, 100, 0, 0);
        NetworkNode node22 = new Router("node22", qosMechanism, null, 10, 10, 10, 10, 100, 0, 0);


        Edge edge1 = new Edge(100, node11, node12);
        edge1.setMtu(100);
        edge1.setPacketErrorRate(0.0);
        edge1.setLength(2);

        Edge edge2 = new Edge(100, node21, node22);
        edge2.setMtu(100);
        edge2.setPacketErrorRate(0.0);
        edge2.setLength(2);

        SimulationFacade facade1 = new SimulationFacade();
        facade1.initTimer(Arrays.asList(edge1), Arrays.asList(node11, node12));

        SimulationFacade facade2 = new SimulationFacade();
        facade2.initTimer(Arrays.asList(edge2), Arrays.asList(node21, node22));

        SimulationLogUtils simulationLogUtils1 = (SimulationLogUtils) getPropertyWithoutGetter(SimulationFacade.class, facade1, "simulationLogUtils");
        SimulationLogUtils simulationLogUtils2 = (SimulationLogUtils) getPropertyWithoutGetter(SimulationFacade.class, facade2, "simulationLogUtils");

        //assert simulationLogUtils objects - they should be different for each facade
        assertFalse(simulationLogUtils1 == simulationLogUtils2); //that should be different objects

        //network nodes should have reference to appropriate simulationLogUtils
        assertTrue(getPropertyWithoutGetter(NetworkNode.class, node11, "simulLog") == simulationLogUtils1);
        assertTrue(getPropertyWithoutGetter(NetworkNode.class, node12, "simulLog") == simulationLogUtils1);

        assertTrue(getPropertyWithoutGetter(NetworkNode.class, node21, "simulLog") == simulationLogUtils2);
        assertTrue(getPropertyWithoutGetter(NetworkNode.class, node22, "simulLog") == simulationLogUtils2);
    }

    private class ListenerClass implements SimulationLogListener {

        @Override
        public void simulationLogOccurred(SimulationLogEvent evt) {
            logged = true;
        }
    }
}
