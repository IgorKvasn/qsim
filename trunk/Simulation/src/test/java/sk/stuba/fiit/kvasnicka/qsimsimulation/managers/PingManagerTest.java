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

package sk.stuba.fiit.kvasnicka.qsimsimulation.managers;

import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.OutputQueueManager;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.util.Arrays;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static sk.stuba.fiit.kvasnicka.TestUtils.getPropertyWithoutGetter;
import static sk.stuba.fiit.kvasnicka.TestUtils.setWithoutSetter;

/**
 * @author Igor Kvasnicka
 */
public class PingManagerTest {
    private PingManager pingManager;
    private NetworkNode node1, node2;


    @Before
    public void before() {
        pingManager = new PingManager();

        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueue q2 = new OutputQueue(50, "queue 2");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});
        OutputQueueManager outputQueueManager2 = new OutputQueueManager(new OutputQueue[]{q2});


        node1 = new Router("node1", null, outputQueueManager1, 10, 10, 10, 10, 100, 0, 0);
        node2 = new Router("node2", null, outputQueueManager2, 10, 10, 10, 10, 100, 0, 0);
    }

    @Test
    public void testPingAdd() {
        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 0, 0, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.ICMP, IpPrecedence.IP_PRECEDENCE_0);
        setWithoutSetter(SimulationRuleBean.class, rule, "route", Arrays.asList(node1, node2));
        pingManager.addPing(rule, 1);

        assertNotNull(getPropertyWithoutGetter(PingManager.class, pingManager, "pingDefinitions"));
        Map pingDefinitions = (Map) getPropertyWithoutGetter(PingManager.class, pingManager, "pingDefinitions");
        assertEquals(1, pingDefinitions.size());

        assertNotNull(pingManager.getPingSimulationRules());
        assertEquals(1, pingManager.getPingSimulationRules().size());
    }

    @Test
    public void testPingRemove() {

        //preparation
        testPingAdd(); //first add some ping simulation rule
        SimulationRuleBean rule = pingManager.getPingSimulationRules().get(0);//there cannot be any NullPointerEx, because it was checked in the method above

        //test
        pingManager.removePing(rule);//remove the rul

        //assert
        assertNotNull(getPropertyWithoutGetter(PingManager.class, pingManager, "pingDefinitions"));
        Map pingDefinitions = (Map) getPropertyWithoutGetter(PingManager.class, pingManager, "pingDefinitions");
        assertEquals(0, pingDefinitions.size());

        assertNotNull(pingManager.getPingSimulationRules());
        assertEquals(0, pingManager.getPingSimulationRules().size());
    }
}
