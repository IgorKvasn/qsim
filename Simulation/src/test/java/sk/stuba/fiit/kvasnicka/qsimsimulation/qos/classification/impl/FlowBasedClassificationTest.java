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
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.PacketClassification;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author Igor Kvasnicka
 */
public class FlowBasedClassificationTest {
    PacketClassification classification;
    NetworkNode node1;
    Packet packet;

    @Before
    public void before() {
        classification = new FlowBasedClassification();
        node1 = new Router("node1", null, null, 100, 10, 50, 10, 10, 100, 0, 0);

        packet = new Packet(14, null, null, 10);
        initRoute(packet);
    }

    @Test
    public void testClassifyAndMarkPacket() throws Exception {

        Packet packet1 = new Packet(14, null, null, 10);

        NetworkNode node2 = new Router("node2", null, null, 100, 10, 50, 10, 10, 100, 0, 0);
        SimulationRuleBean simulationRuleBean = new SimulationRuleBean("", node1, node2, 1, 1, 100, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, null, 1, 2);
        simulationRuleBean.setRoute(Arrays.asList(node1, node2));

        Field f = null;
        try {
            f = Packet.class.getDeclaredField("simulationRule");
            f.setAccessible(true);
            f.set(packet1, simulationRuleBean);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        Packet packet2 = new Packet(14, null, null, 10);

        NetworkNode node3 = new Router("node3", null, null, 100, 10, 50, 10, 10, 100, 0, 0);
        SimulationRuleBean simulationRuleBean2 = new SimulationRuleBean("", node1, node3, 1, 1, 100, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, null, 1, 2);
        simulationRuleBean2.setRoute(Arrays.asList(node1, node3));

        Field f2 = null;
        try {
            f2 = Packet.class.getDeclaredField("simulationRule");
            f2.setAccessible(true);
            f2.set(packet2, simulationRuleBean2);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        Packet packet3 = new Packet(14, null, null, 10);
        SimulationRuleBean simulationRuleBean3 = new SimulationRuleBean("", node2, node1, 1, 1, 100, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, null, 3, 2);
        simulationRuleBean3.setRoute(Arrays.asList(node1, node2));

        Field f3 = null;
        try {
            f3 = Packet.class.getDeclaredField("simulationRule");
            f3.setAccessible(true);
            f3.set(packet3, simulationRuleBean3);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        Packet packet4 = new Packet(14, null, null, 10);

        Field f4 = null;
        try {
            f4 = Packet.class.getDeclaredField("simulationRule");
            f4.setAccessible(true);
            f4.set(packet4, simulationRuleBean);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        assertEquals(0, classification.classifyAndMarkPacket(node1, packet));
        assertEquals(1, classification.classifyAndMarkPacket(node1, packet1));
        assertEquals(2, classification.classifyAndMarkPacket(node1, packet2));
        assertEquals(1, classification.classifyAndMarkPacket(node1, packet4));
    }

    private void initRoute(Packet... packets) {
        NetworkNode node2 = new Router("node2", null, null, 100, 10, 50, 10, 10, 100, 0, 0);
        SimulationRuleBean simulationRuleBean = new SimulationRuleBean("", node1, node2, 1, 1, 100, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, null, 10, 11);
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
