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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling;

import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.OutputQueueManager;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.ClassBasedWFQScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.ClassDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Igor Kvasnicka
 */
public class ClassBasedWFQSchedulingTest {
    private ClassBasedWFQScheduling classBasedWFQScheduling;
    private NetworkNode node1;

    @Test
    public void testConstructor_ok() {
        new ClassBasedWFQScheduling(new HashMap<String, Object>() {{
            put(ClassBasedWFQScheduling.CLASS_DEFINITIONS, new ClassDefinition[]{});
        }});
    }


    @Test
    public void testDecitePacketsToMoveFromOutputQueue_three_queues() {
        final ClassDefinition[] classDef = new ClassDefinition[2];
        classDef[0] = new ClassDefinition(0, 1);
        classDef[1] = new ClassDefinition(2);

        classBasedWFQScheduling = new ClassBasedWFQScheduling(new HashMap<String, Object>() {
            {
                put(ClassBasedWFQScheduling.CLASS_DEFINITIONS, classDef);
            }
        });


        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});
        QosMechanism qosMechanism = new QosMechanism(null, null, null);

        node1 = new Router("node1", qosMechanism, outputQueueManager1, 200, 10, 10, 10, 100, 0, 0);


        Packet p1 = new Packet(10, null, null, 0);
        p1.setQosQueue(0);
        Packet p2 = new Packet(10, null, null, 0);
        p2.setQosQueue(0);
        Packet p3 = new Packet(10, null, null, 0);
        p3.setQosQueue(1);
        Packet p4 = new Packet(10, null, null, 0);
        p4.setQosQueue(1);
        Packet p5 = new Packet(10, null, null, 0);
        p5.setQosQueue(2);
        Packet p6 = new Packet(10, null, null, 0);
        p6.setQosQueue(2);

        initRoute(p1, p2, p3, p4, p5, p6);

        List<List<Packet>> outputPackets = Arrays.asList(Arrays.asList(p1, p2), Arrays.asList(p3, p4), Arrays.asList(p5, p6)); //3 queues

        List<Packet> packetList = classBasedWFQScheduling.decitePacketsToMoveFromOutputQueue(node1, outputPackets);

        assertNotNull(packetList);
        assertEquals(6, packetList.size());


        assertTrue(packetList.get(0) == p1);
        assertTrue(packetList.get(1) == p3);
        assertTrue(packetList.get(2) == p2);
        assertTrue(packetList.get(3) == p4);
        assertTrue(packetList.get(4) == p5);
        assertTrue(packetList.get(5) == p6);
    }

    private void initRoute(Packet... packets) {
        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});
        QosMechanism qosMechanism = new QosMechanism(null, null, null);
        NetworkNode node2 = new Router("node1", qosMechanism, outputQueueManager1, 200, 10, 10, 10, 100, 0, 0);

        SimulationRuleBean simulationRuleBean = new SimulationRuleBean("", node1, node2, 1, 1, 100, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0);
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
