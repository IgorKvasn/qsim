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
import org.powermock.api.easymock.PowerMock;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.OutputQueueManager;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ruleactivation.SimulationRuleActivationListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLogUtils;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PingManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.SimulationManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static sk.stuba.fiit.kvasnicka.TestUtils.initNetworkNode;
import static sk.stuba.fiit.kvasnicka.TestUtils.setWithoutSetter;

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
        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueue q2 = new OutputQueue(50, "queue 2");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});
        OutputQueueManager outputQueueManager2 = new OutputQueueManager(new OutputQueue[]{q2});


        qosMechanism = EasyMock.createMock(QosMechanism.class);
        EasyMock.expect(qosMechanism.classifyAndMarkPacket(EasyMock.anyObject(NetworkNode.class), EasyMock.anyObject(Packet.class))).andReturn(0).times(100);
        EasyMock.expect(qosMechanism.decitePacketsToMoveFromOutputQueue(EasyMock.anyObject(NetworkNode.class), EasyMock.anyObject(List.class))).andAnswer(new IAnswer<List<Packet>>() {
            @Override
            public List<Packet> answer() throws Throwable {
                return ((List<List<Packet>>) EasyMock.getCurrentArguments()[1]).get(0);
            }
        }).times(100);
        EasyMock.replay(qosMechanism);

        node1 = new Router("node1", qosMechanism, outputQueueManager1, 10, 10, 10, 10, 2.1, 0, 0, null);
        node2 = new Router("node2", qosMechanism, outputQueueManager2, 10, 10, 10, 10, 2.1, 0, 0, null);

        SimulationLogUtils simulationLogUtils = new SimulationLogUtils();

        initNetworkNode(node1, simulationLogUtils);
        initNetworkNode(node2, simulationLogUtils);

        edge = PowerMock.createPartialMock(Edge.class, "getPacketErrorRate");
        EasyMock.expect(edge.getPacketErrorRate()).andReturn(5.0).times(1);
        EasyMock.expect(edge.getPacketErrorRate()).andReturn(0.0).times(2);
        PowerMock.replay(edge);

        setWithoutSetter(Edge.class, edge, "fragments", new LinkedList<Packet>());
        setWithoutSetter(Edge.class, edge, "maxSpeed", 1000);
        setWithoutSetter(Edge.class, edge, "length", 20);
        setWithoutSetter(Edge.class, edge, "node1", node1);
        setWithoutSetter(Edge.class, edge, "node2", node2);
        setWithoutSetter(Edge.class, edge, "mtu", 1000);
        setWithoutSetter(Edge.class, edge, "speedMap", new HashMap<SimulationRuleBean, Long>());
        setWithoutSetter(Edge.class, edge, "congestedInfoSet", new TreeSet<Object>());
    }

    /**
     * simulate one TCP packet - this means that packet retransmission should occur
     */
    @Test
    public void testSinglePacketSimulation_TCP() throws Exception {

        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge), Arrays.asList(node1, node2), new SimulationLogUtils());

        simulationManager = new SimulationManager();
        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 1, 50, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.TCP, IpPrecedence.IP_PRECEDENCE_0, 0, 0);
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

    private void checkNoPacketsInTopology(SimulationTimer timer) throws NoSuchFieldException, IllegalAccessException {
        Field privateStringField = SimulationTimer.class.getDeclaredField("packetManager");
        privateStringField.setAccessible(true);
        PacketManager packetManager = (PacketManager) privateStringField.get(timer);
        assertTrue(packetManager.checkNoPacketsInSimulation());
    }
}
