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
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.PacketClassification;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.lang.reflect.Field;
import java.util.Arrays;

import static junit.framework.Assert.assertEquals;

/**
 * @author Igor Kvasnicka
 */
public class NoClassificationTest {
    PacketClassification classification;
    NetworkNode node1;
    Packet packet;

    @Before
    public void before() {
        classification = new NoClassification();

        node1 = new Router("node1", null, null, 100, 10, null, 10, 10, 100, 0, 0);

        packet = new Packet(14, null, null, 10);
        initRoute(packet);
    }

    @Test
    public void testClassifyAndMarkPacket() {
        int QOS_MARKING = 4;
        packet.setQosQueue(QOS_MARKING,10);
        assertEquals(QOS_MARKING, classification.classifyAndMarkPacket(node1, packet));
    }

    private void initRoute(Packet... packets) {
        NetworkNode node2 = new Router("node2", null, null, 100, 10, null, 10, 10, 100, 0, 0);
        SimulationRuleBean simulationRuleBean = new SimulationRuleBean("", node1, node2, null,1, 1, 100, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0, null, 0, 0);
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
