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

package sk.stuba.fiit.kvasnicka.qsimsimulation.facade;

import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.OutputQueueManager;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLogUtils;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static sk.stuba.fiit.kvasnicka.TestUtils.initNetworkNode;

/**
 * @author Igor Kvasnicka
 */
public class SimulationFacadeTest {

    private SimulationFacade facade;
    private NetworkNode node1, node2;


    @Before
    public void before() {
        facade = new SimulationFacade();

        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueue q2 = new OutputQueue(50, "queue 2");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});
        OutputQueueManager outputQueueManager2 = new OutputQueueManager(new OutputQueue[]{q2});


        node1 = new Router("node1", null, outputQueueManager1, 10, 10, 10, 10, 100, 0, 0, null);
        node2 = new Router("node2", null, outputQueueManager2, 10, 10, 10, 10, 100, 0, 0, null);
        SimulationLogUtils simulationLogUtils = new SimulationLogUtils();
        initNetworkNode(node1, simulationLogUtils);
        initNetworkNode(node2, simulationLogUtils);

        SimulationRuleBean rule1 = new SimulationRuleBean("", node1, node2, 0, 0, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0);
        rule1.setRoute(Arrays.asList(node1, node2));
        facade.addSimulationRule(rule1);

        SimulationRuleBean rule2 = new SimulationRuleBean("", node1, node2, 0, 0, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0);
        rule2.setRoute(Arrays.asList(node1, node2));
        facade.addSimulationRule(rule2);

        SimulationRuleBean rule3 = new SimulationRuleBean("", node2, node1, 0, 0, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0);
        rule3.setRoute(Arrays.asList(node2, node1));
        facade.addSimulationRule(rule3);
    }

    /**
     * no simulation rules should be found
     */
    @Test
    public void testGetSimulRulesThatContainsNode_none() {

        OutputQueue q3 = new OutputQueue(50, "queue 3");
        OutputQueueManager outputQueueManager3 = new OutputQueueManager(new OutputQueue[]{q3});

        NetworkNode node3 = new Router("node3", null, outputQueueManager3, 10, 10, 10, 10, 100, 0, 0, null);
        List<SimulationRuleBean> rules = facade.getSimulRulesThatContainsNode(node3);
        assertNotNull(rules);
        assertEquals(0, rules.size());
    }

    @Test
    public void testGetSimulRulesThatContainsNode() {
        List<SimulationRuleBean> rules = facade.getSimulRulesThatContainsNode(node1);
        assertNotNull(rules);
        assertEquals(3, rules.size());
    }
}
