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

package sk.stuba.fiit.kvasnicka.qsimdatamodel.data.utils.creationdelay;

import junit.framework.Assert;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.TestUtils;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLogUtils;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanismDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static sk.stuba.fiit.kvasnicka.TestUtils.initNetworkNode;

/**
 * @author Igo
 */
public class ConstantNoiseCreationDelayTest {
    PacketManager packetManager;
    SimulationTimer timer;
    QosMechanismDefinition qosMechanism;
    double simulationTime;
    TopologyManager topologyManager;
    NetworkNode node1, node2;
    Edge edge;
    private final int MAX_TX_SIZE = 200;
    private final int MTU = 100;
    private final int MAX_OUTPUT_QUEUE_SIZE = 10;
    private static final int MAX_PROCESSING_PACKETS = 3;
    private final Layer4TypeEnum layer4 = Layer4TypeEnum.UDP;
    private SimulationRuleBean simulationRuleBean;

    @Before
    public void before() {
        simulationTime = 10L;

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
        EasyMock.expect(qosMechanism.performActiveQueueManagement(EasyMock.anyObject(List.class), EasyMock.anyObject(Packet.class))).andReturn(true);
        EasyMock.expect(qosMechanism.performActiveQueueManagement(EasyMock.anyObject(List.class), EasyMock.anyObject(Packet.class))).andReturn(true);
        EasyMock.expect(qosMechanism.performActiveQueueManagement(EasyMock.anyObject(List.class), EasyMock.anyObject(Packet.class))).andReturn(true);
        EasyMock.expect(qosMechanism.performActiveQueueManagement(EasyMock.anyObject(List.class), EasyMock.anyObject(Packet.class))).andReturn(true);

        EasyMock.replay(qosMechanism);


        node1 = new Router("node1", null, qosMechanism, 10, 10, 50, 10, MAX_PROCESSING_PACKETS, 100, 0, 0);
        node2 = new Router("node2", null, qosMechanism, 10, 10, 50, 10, 10, 100, 0, 0);
        SimulationLogUtils simulationLogUtils = new SimulationLogUtils();
        initNetworkNode(node1, simulationLogUtils);
        initNetworkNode(node2, simulationLogUtils);


        edge = new Edge(100, MTU, 2, 0, node1, node2);

        topologyManager = new TopologyManager(Arrays.asList(edge), Arrays.asList(node1, node2));
        node1.setTopologyManager(topologyManager);
        node2.setTopologyManager(topologyManager);

        timer = EasyMock.createMock(SimulationTimer.class);
        EasyMock.expect(timer.getTopologyManager()).andReturn(topologyManager).times(100);
        EasyMock.replay(timer);

        packetManager = new PacketManager(timer);

        simulationRuleBean = new SimulationRuleBean("", node1, node2,null, 1, 1, 100, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0, null, 0, 0);
        simulationRuleBean.setRoute(Arrays.asList(node1, node2));
    }

    @Test
    public void testCreation_function_length() {
        double maxValue = 18.6;

        TestUtils.setWithoutSetter(SimulationRuleBean.class, simulationRuleBean, "packetCreationDelayFunction", new ConstantNoiseCreationDelay(maxValue, 0));

        double res = simulationRuleBean.getPacketCreationDelayFunction().calculateDelay(simulationRuleBean, 11);

        Assert.assertEquals(res, simulationRuleBean.getPacketCreationDelayFunction().calculateDelay(simulationRuleBean, 21));
        Assert.assertEquals(res, simulationRuleBean.getPacketCreationDelayFunction().calculateDelay(simulationRuleBean, 31));
    }

    @Test
    public void testCreation_maximum() {
        double maxValue = 18.6;
        double noise = 1;

        double res;
        TestUtils.setWithoutSetter(SimulationRuleBean.class, simulationRuleBean, "packetCreationDelayFunction", new ConstantNoiseCreationDelay(maxValue, noise));

        for (int i = 0; i < 50; i++) {
            res = simulationRuleBean.getPacketCreationDelayFunction().calculateDelay(simulationRuleBean, i);
            Assert.assertTrue(res <= maxValue + noise);
            Assert.assertTrue(res >= maxValue - noise);
        }
    }
}
