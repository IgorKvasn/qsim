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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.impl;

import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.TestUtils;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.OutputQueueManager;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.PacketClassificationInterf;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.lang.reflect.Field;
import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 * @author Igor Kvasnicka
 */
public class IpPrecedenceClassificationTest {
    PacketClassificationInterf classification;
    NetworkNode node1;
    Packet packet;

    @Before
    public void before() {
        classification = new IpPrecedenceClassification();
        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});

        node1 = new Router("node1", null, outputQueueManager1, 100, 10, 10, 10, 100, 0, 0, null);

        packet = new Packet(14, null, null, 10);
        initRoute(packet);
    }

    @Test
    public void testClassifyAndMarkPacket_precedence_not_defined() {
        try {
            classification.classifyAndMarkPacket(node1, packet);
            fail("IP precedence not defined in pakcet - exception should be thrown");
        } catch (IllegalStateException e) {
            //OK
        }
    }

    @Test
    public void testClassifyAndMarkPacket() {
        IpPrecedence precedence = IpPrecedence.IP_PRECEDENCE_2;

        TestUtils.setWithoutSetter(SimulationRuleBean.class, packet.getSimulationRule(), "ipPrecedence", precedence);

        int queue = classification.classifyAndMarkPacket(node1, packet);
        assertEquals(precedence.getIpPrecedence(), queue);
    }

    private void initRoute(Packet... packets) {
        NetworkNode node2;
        OutputQueue q2 = new OutputQueue(50, "queue 2");
        OutputQueueManager outputQueueManager2 = new OutputQueueManager(new OutputQueue[]{q2});


        node2 = new Router("node2", null, outputQueueManager2, 100, 10, 10, 10, 100, 0, 0, null);
        SimulationRuleBean simulationRuleBean = new SimulationRuleBean("", node1, node2, 1, 1, 100, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, null, 0, 0);
        simulationRuleBean.setRoute(Arrays.asList(node1, node2));

        for (Packet p : packets) {
            Field f = null;
            try {
                f = Packet.class.getDeclaredField("simulationRule");
                f.setAccessible(true);
                f.set(p, simulationRuleBean);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
