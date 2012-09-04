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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils;

import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.OutputQueueManager;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Igor Kvasnicka
 */
public class ClassificationUtilTest {

    @Test
    public void testIsClassificationRuleApplied_destination_IN_list() throws ClassificationException {

        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueue q2 = new OutputQueue(50, "queue 2");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});
        OutputQueueManager outputQueueManager2 = new OutputQueueManager(new OutputQueue[]{q2});

        NetworkNode node1 = new Router("node1", null, outputQueueManager1, 10, 10, 10, 10, 100, 0, 0, null);
        NetworkNode node2 = new Router("node2", null, outputQueueManager2, 10, 10, 10, 10, 100, 0, 0, null);

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 1, 50, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0);
        rule.setRoute(Arrays.asList(node1, node2));

        Packet packet = new Packet(10, null, rule, 10);

        boolean result = ClassificationUtil.isClassificationRuleApplied("destination(['a','node2','c','d'])", packet);
        assertTrue(result);

        boolean result2 = ClassificationUtil.isClassificationRuleApplied("destination(['a','b','c','d'])", packet);
        assertFalse(result2);
    }

    @Test
    public void testIsClassificationRuleApplied_destination_single() throws ClassificationException {
        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueue q2 = new OutputQueue(50, "queue 2");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});
        OutputQueueManager outputQueueManager2 = new OutputQueueManager(new OutputQueue[]{q2});

        NetworkNode node1 = new Router("node1", null, outputQueueManager1, 10, 10, 10, 10, 100, 0, 0, null);
        NetworkNode node2 = new Router("node2", null, outputQueueManager2, 10, 10, 10, 10, 100, 0, 0, null);

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 1, 50, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0);
        rule.setRoute(Arrays.asList(node1, node2));

        Packet packet = new Packet(10, null, rule, 10);

        boolean result = ClassificationUtil.isClassificationRuleApplied("destination('node2')", packet);
        assertTrue(result);

        boolean result2 = ClassificationUtil.isClassificationRuleApplied("destination('a')", packet);
        assertFalse(result2);
    }

    @Test
    public void testIsClassificationRuleApplied_destination_negation() throws ClassificationException {

        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueue q2 = new OutputQueue(50, "queue 2");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});
        OutputQueueManager outputQueueManager2 = new OutputQueueManager(new OutputQueue[]{q2});

        NetworkNode node1 = new Router("node1", null, outputQueueManager1, 10, 10, 10, 10, 100, 0, 0, null);
        NetworkNode node2 = new Router("node2", null, outputQueueManager2, 10, 10, 10, 10, 100, 0, 0, null);

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 1, 50, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0);
        rule.setRoute(Arrays.asList(node1, node2));

        Packet packet = new Packet(10, null, rule, 10);

        boolean result = ClassificationUtil.isClassificationRuleApplied("notdestination(['a','node2','c','d'])", packet);
        assertFalse(result);

        boolean result2 = ClassificationUtil.isClassificationRuleApplied("notdestination(['a','b','c','d'])", packet);
        assertTrue(result2);

        boolean result3 = ClassificationUtil.isClassificationRuleApplied("notdestination('a')", packet);
        assertTrue(result3);
    }

    @Test
    public void testIsClassificationRuleApplied_source_IN_list() throws ClassificationException {

        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueue q2 = new OutputQueue(50, "queue 2");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});
        OutputQueueManager outputQueueManager2 = new OutputQueueManager(new OutputQueue[]{q2});

        NetworkNode node1 = new Router("node1", null, outputQueueManager1, 10, 10, 10, 10, 100, 0, 0, null);
        NetworkNode node2 = new Router("node2", null, outputQueueManager2, 10, 10, 10, 10, 100, 0, 0, null);

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 1, 50, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0);
        rule.setRoute(Arrays.asList(node1, node2));

        Packet packet = new Packet(10, null, rule, 10);

        boolean result = ClassificationUtil.isClassificationRuleApplied("source(['a','node1','c','d'])", packet);
        assertTrue(result);

        boolean result2 = ClassificationUtil.isClassificationRuleApplied("source(['a','b','c','d'])", packet);
        assertFalse(result2);
    }

    @Test
    public void testIsClassificationRuleApplied_source_single() throws ClassificationException {

        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueue q2 = new OutputQueue(50, "queue 2");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});
        OutputQueueManager outputQueueManager2 = new OutputQueueManager(new OutputQueue[]{q2});

        NetworkNode node1 = new Router("node1", null, outputQueueManager1, 10, 10, 10, 10, 100, 0, 0, null);
        NetworkNode node2 = new Router("node2", null, outputQueueManager2, 10, 10, 10, 10, 100, 0, 0, null);

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 1, 50, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0);
        rule.setRoute(Arrays.asList(node1, node2));

        Packet packet = new Packet(10, null, rule, 10);

        boolean result = ClassificationUtil.isClassificationRuleApplied("source('node1')", packet);
        assertTrue(result);

        boolean result2 = ClassificationUtil.isClassificationRuleApplied("source('a')", packet);
        assertFalse(result2);
    }

    @Test
    public void testIsClassificationRuleApplied_source_negation() throws ClassificationException {

        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueue q2 = new OutputQueue(50, "queue 2");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});
        OutputQueueManager outputQueueManager2 = new OutputQueueManager(new OutputQueue[]{q2});

        NetworkNode node1 = new Router("node1", null, outputQueueManager1, 10, 10, 10, 10, 100, 0, 0, null);
        NetworkNode node2 = new Router("node2", null, outputQueueManager2, 10, 10, 10, 10, 100, 0, 0, null);

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 1, 50, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0);
        rule.setRoute(Arrays.asList(node1, node2));

        Packet packet = new Packet(10, null, rule, 10);

        boolean result = ClassificationUtil.isClassificationRuleApplied("notsource(['a','node1','c','d'])", packet);
        assertFalse(result);

        boolean result2 = ClassificationUtil.isClassificationRuleApplied("notsource ( ['a','b','c','d' ] ) ", packet);
        assertTrue(result2);

        boolean result3 = ClassificationUtil.isClassificationRuleApplied("notsource('a')", packet);
        assertTrue(result3);
    }

    @Test
    public void testIsClassificationRuleApplied_packet_size() throws ClassificationException {

        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueue q2 = new OutputQueue(50, "queue 2");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});
        OutputQueueManager outputQueueManager2 = new OutputQueueManager(new OutputQueue[]{q2});

        NetworkNode node1 = new Router("node1", null, outputQueueManager1, 10, 10, 10, 10, 100, 0, 0, null);
        NetworkNode node2 = new Router("node2", null, outputQueueManager2, 10, 10, 10, 10, 100, 0, 0, null);

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 1, 50, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0);
        rule.setRoute(Arrays.asList(node1, node2));

        Packet packet = new Packet(10, null, rule, 10);

        boolean result = ClassificationUtil.isClassificationRuleApplied("size = 50", packet);
        assertTrue(result);

        boolean result2 = ClassificationUtil.isClassificationRuleApplied("size != 50", packet);
        assertFalse(result2);
    }

    @Test
    public void testIsClassificationRuleApplied_packet_size_compare() throws ClassificationException {

        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueue q2 = new OutputQueue(50, "queue 2");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});
        OutputQueueManager outputQueueManager2 = new OutputQueueManager(new OutputQueue[]{q2});

        NetworkNode node1 = new Router("node1", null, outputQueueManager1, 10, 10, 10, 10, 100, 0, 0, null);
        NetworkNode node2 = new Router("node2", null, outputQueueManager2, 10, 10, 10, 10, 100, 0, 0, null);

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 1, 50, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0);
        rule.setRoute(Arrays.asList(node1, node2));

        Packet packet = new Packet(10, null, rule, 10);

        boolean result = ClassificationUtil.isClassificationRuleApplied("size >= 50", packet);
        assertTrue(result);

        boolean result2 = ClassificationUtil.isClassificationRuleApplied("size < 50", packet);
        assertFalse(result2);
    }

    @Test
    public void testIsClassificationRuleApplied_multiple_conditions() throws ClassificationException {

        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueue q2 = new OutputQueue(50, "queue 2");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});
        OutputQueueManager outputQueueManager2 = new OutputQueueManager(new OutputQueue[]{q2});

        NetworkNode node1 = new Router("node1", null, outputQueueManager1, 10, 10, 10, 10, 100, 0, 0, null);
        NetworkNode node2 = new Router("node2", null, outputQueueManager2, 10, 10, 10, 10, 100, 0, 0, null);

        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 1, 50, 0, PacketTypeEnum.AUDIO_PACKET, Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0);
        rule.setRoute(Arrays.asList(node1, node2));

        Packet packet = new Packet(10, null, rule, 10);

        boolean result = ClassificationUtil.isClassificationRuleApplied("(size = 50) AND (source('node1'))", packet);
        assertTrue(result);

        boolean result2 = ClassificationUtil.isClassificationRuleApplied("(size != 50) AND (source('node1'))", packet);
        assertFalse(result2);

        boolean result3 = ClassificationUtil.isClassificationRuleApplied("((size != 50) AND (source('node1'))) OR packetType='AUDIO'", packet);
        assertTrue(result3);

        boolean result4 = ClassificationUtil.isClassificationRuleApplied("((size != 50) AND (source('node1'))) OR packetType='AUDIO' AND  layer4 !='TCP'", packet);
        assertTrue(result4);
    }
}
