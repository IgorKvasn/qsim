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
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.OutputQueueManager;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ruleactivation.SimulationRuleActivationListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLogUtils;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PingManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.SimulationManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static sk.stuba.fiit.kvasnicka.TestUtils.initNetworkNode;

/**
 * @author Igor Kvasnicka
 */
public class TcpCongestionTest {
    QosMechanism qosMechanism;
    NetworkNode node1, node2, node3;
    Edge edge1;
    private SimulationManager simulationManager;

    @Before
    public void before() {

        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueue q2 = new OutputQueue(50, "queue 2");
        OutputQueue q3 = new OutputQueue(50, "queue 3");

        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});
        OutputQueueManager outputQueueManager2 = new OutputQueueManager(new OutputQueue[]{q2});
        OutputQueueManager outputQueueManager3 = new OutputQueueManager(new OutputQueue[]{q3});


        qosMechanism = EasyMock.createMock(QosMechanism.class);
        EasyMock.expect(qosMechanism.classifyAndMarkPacket(EasyMock.anyObject(NetworkNode.class), EasyMock.anyObject(Packet.class))).andReturn(0).times(100);
        EasyMock.expect(qosMechanism.decitePacketsToMoveFromOutputQueue(EasyMock.anyObject(NetworkNode.class), EasyMock.anyObject(List.class))).andAnswer(new IAnswer<List<Packet>>() {
            @Override
            public List<Packet> answer() throws Throwable {
                return ((List<List<Packet>>) EasyMock.getCurrentArguments()[1]).get(0);
            }
        }).times(100);
        EasyMock.replay(qosMechanism);

        node1 = new Router("node1", qosMechanism, outputQueueManager1, 10, 10, 10, 10, 100, 0, 0);
        node2 = new Router("node2", qosMechanism, outputQueueManager2, 10, 10, 10, 10, 100, 0, 0);
        node3 = new Router("node3", qosMechanism, outputQueueManager3, 10, 10, 10, 10, 100, 0, 0);

        SimulationLogUtils simulationLogUtils = new SimulationLogUtils();
        initNetworkNode(node1, simulationLogUtils);
        initNetworkNode(node2, simulationLogUtils);
        initNetworkNode(node3, simulationLogUtils);


        edge1 = new Edge(100, 100, 2, 0, node1, node2);
    }


    /**
     * simulate one packet and test retransmission because of output buffer owerflow
     */
    @Test
    public void testSinglePacketSimulation_retransmission_output_buffer_overflow() throws NoSuchFieldException, IllegalAccessException {
        SimulationTimer timer = new SimulationTimer(Arrays.asList(edge1), Arrays.asList(node1, node2), new SimulationLogUtils());


        simulationManager = new SimulationManager();
        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 1, 50, .05, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.TCP, false);
        rule.setRoute(Arrays.asList(node1, node2));

        simulationManager.addSimulationRule(rule);

        timer.startSimulationTimer(simulationManager, new PingManager(), new LinkedList<SimulationRuleActivationListener>());

        timer.actionPerformed(null);

        timer.actionPerformed(null);
        timer.actionPerformed(null);

        timer.actionPerformed(null);

        timer.actionPerformed(null);

        assertFalse(timer.isRunning());
    }
}
