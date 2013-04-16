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

import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.TestUtils;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanismDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.WeightedFairQueuingScheduling;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Igor Kvasnicka
 */
public class WeightedFairQueuingSchedulingTest {
    private WeightedFairQueuingScheduling weightedFairQueuingScheduling;
    private NetworkNode node1;

    @Before
    public void before() {
        weightedFairQueuingScheduling = new WeightedFairQueuingScheduling();
    }

    @Test
    public void testFindSmallestPacket_1() {
        Packet p1 = new Packet(10, null, null, 0);
        p1.setQosQueue(0,1);
        Packet p2 = new Packet(16, null, null, 0);
        p2.setQosQueue(0,1);
        Packet p3 = new Packet(12, null, null, 0);
        p3.setQosQueue(0,1);

        List<Packet> list = Arrays.asList(p1, p2, p3);
        List<Packet> smallest = (List<Packet>) TestUtils.callPrivateMethod(WeightedFairQueuingScheduling.class, weightedFairQueuingScheduling, "findSmallestPacket", new Class[]{List.class}, new Object[]{list});

        assertNotNull(smallest);
        assertEquals(1, smallest.size());
        assertTrue(smallest.get(0) == p1);
    }

    @Test
    public void testFindSmallestPacket_same_size() {
        Packet p1 = new Packet(10, null, null, 0);
        p1.setQosQueue(0,1);
        Packet p2 = new Packet(16, null, null, 0);
        p2.setQosQueue(0,1);
        Packet p3 = new Packet(10, null, null, 0);
        p3.setQosQueue(0,1);

        List<Packet> list = Arrays.asList(p1, p2, p3);
        List<Packet> smallest = (List<Packet>) TestUtils.callPrivateMethod(WeightedFairQueuingScheduling.class, weightedFairQueuingScheduling, "findSmallestPacket", new Class[]{List.class}, new Object[]{list});

        assertNotNull(smallest);
        assertEquals(2, smallest.size());
        assertTrue(smallest.get(0) == p1);
        assertTrue(smallest.get(1) == p3);
    }

    @Test
    /**
     * for one queue it has got no effect
     */
    public void testDecitePacketsToMoveFromOutputQueue_one_queue() {
        QosMechanismDefinition qosMechanism = new QosMechanismDefinition(null, null, null, null);

        node1 = new Router("node1", null, qosMechanism, 200, 10, null, 10, 10, 100, 0, 0);

        final Packet p1 = new Packet(10, null, null, 0);
        p1.setQosQueue(0,1);
        final Packet p2 = new Packet(16, null, null, 0);
        p2.setQosQueue(0,1);
        final Packet p3 = new Packet(12, null, null, 0);
        p3.setQosQueue(0,1);

        Map<Integer, List<Packet>> outputPackets = new HashMap<Integer, List<Packet>>() {{
            put(0, Arrays.asList(p1, p2, p3));
        }};

        List<Packet> packetList = weightedFairQueuingScheduling.decitePacketsToMoveFromOutputQueue(node1, outputPackets);

        assertNotNull(packetList);
        assertEquals(outputPackets.get(0).size(), packetList.size());

        assertTrue(packetList.get(0) == p1);
        assertTrue(packetList.get(1) == p2);
        assertTrue(packetList.get(2) == p3);
    }

    @Test
    public void testDecitePacketsToMoveFromOutputQueue_multiple_queues() {
        QosMechanismDefinition qosMechanism = new QosMechanismDefinition(null, null, null, null);

        node1 = new Router("node1", null, qosMechanism, 200, 10, null, 10, 10, 100, 0, 0);

        final Packet p1 = new Packet(10, null, null, 0);
        p1.setQosQueue(0,1);
        final Packet p2 = new Packet(16, null, null, 0);
        p2.setQosQueue(0,1);
        final Packet p3 = new Packet(12, null, null, 0);
        p3.setQosQueue(0,1);

        final Packet p4 = new Packet(8, null, null, 0);
        p4.setQosQueue(1,2);
        final Packet p5 = new Packet(15, null, null, 0);
        p5.setQosQueue(1,2);


        Map<Integer, List<Packet>> outputPackets = new HashMap<Integer, List<Packet>>() {{
            put(0, Arrays.asList(p1, p2, p3));
            put(1, Arrays.asList(p4, p5));
        }};

        List<Packet> packetList = weightedFairQueuingScheduling.decitePacketsToMoveFromOutputQueue(node1, outputPackets);

        assertNotNull(packetList);
        assertEquals(5, packetList.size());

        assertTrue(packetList.get(0) == p4);
        assertTrue(packetList.get(1) == p1);
        assertTrue(packetList.get(2) == p5);
        assertTrue(packetList.get(3) == p2);
        assertTrue(packetList.get(4) == p3);
    }
}
