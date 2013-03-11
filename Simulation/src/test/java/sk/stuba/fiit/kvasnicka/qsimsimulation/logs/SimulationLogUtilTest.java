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
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.log.SimulationLogEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.log.SimulationLogListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ruleactivation.SimulationRuleActivationListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.facade.SimulationFacade;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PingManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.SimulationManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanismDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static sk.stuba.fiit.kvasnicka.TestUtils.getPropertyWithoutGetter;
import static sk.stuba.fiit.kvasnicka.TestUtils.initNetworkNode;

/**
 * @author Igor Kvasnicka
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DelayHelper.class)
public class SimulationLogUtilTest {
    private QosMechanismDefinition qosMechanism;
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

        PowerMock.mockStatic(DelayHelper.class);
        EasyMock.expect(DelayHelper.calculatePacketCreationDelay(EasyMock.anyObject(SimulationRuleBean.class), EasyMock.anyInt(), EasyMock.anyDouble())).andReturn(0.0).times(10);
        EasyMock.expect(DelayHelper.calculateSerialisationDelay(EasyMock.anyObject(Packet.class), EasyMock.anyObject(Edge.class), EasyMock.anyInt())).andReturn(0.0).times(10);
        EasyMock.expect(DelayHelper.calculatePropagationDelay( EasyMock.anyObject(Edge.class))).andReturn(0.0).times(10);


        PowerMock.replay(DelayHelper.class);


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

        node1 = new Router("node1", null, qosMechanism, 10, 10, 50, 10, 10, 100, 0, 0);
        node2 = new Router("node2", null, qosMechanism, 10, 10, 50, 10, 10, 100, 0, 0);

        initNetworkNode(node1, simulationLogUtils);
        initNetworkNode(node2, simulationLogUtils);


        edge1 = new Edge(100, 100, 0, 2, node1, node2);
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
        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, null, 1, 50, 0, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0, null, 0, 0);
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

        NetworkNode node11 = new Router("node11", null, qosMechanism, 10, 10, 50, 10, 10, 100, 0, 0);
        NetworkNode node12 = new Router("node12", null, qosMechanism, 10, 10, 50, 10, 10, 100, 0, 0);
        NetworkNode node21 = new Router("node21", null, qosMechanism, 10, 10, 50, 10, 10, 100, 0, 0);
        NetworkNode node22 = new Router("node22", null, qosMechanism, 10, 10, 50, 10, 10, 100, 0, 0);


        Edge edge1 = new Edge(100, 100, 2, 0, node11, node12);
        Edge edge2 = new Edge(100, 100, 2, 0, node21, node22);

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
