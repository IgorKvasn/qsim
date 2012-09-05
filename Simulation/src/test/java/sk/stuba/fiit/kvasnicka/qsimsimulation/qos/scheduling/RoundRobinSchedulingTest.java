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
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.RoundRobinScheduling;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Igor Kvasnicka
 */
public class RoundRobinSchedulingTest {
    private RoundRobinScheduling roundRobinScheduling;
    private NetworkNode node1;

    @Before
    public void before() {
        roundRobinScheduling = new RoundRobinScheduling();
    }

    @Test
    public void testDecitePacketsToMoveFromOutputQueue_one_queue() {
        QosMechanism qosMechanism = new QosMechanism(null, null, null);

        node1 = new Router("node1", qosMechanism, 200, 10, 50, 10, 10, 100, 0, 0, null);

        final Packet p1 = new Packet(10, null, null, 0);
        final Packet p2 = new Packet(10, null, null, 0);
        final Packet p3 = new Packet(10, null, null, 0);

        Map<Integer, List<Packet>> outputPackets = new HashMap<Integer, List<Packet>>() {{
            put(0, Arrays.asList(p1, p2, p3));
        }};

        List<Packet> packetList = roundRobinScheduling.decitePacketsToMoveFromOutputQueue(node1, outputPackets);

        assertNotNull(packetList);
        assertEquals(outputPackets.get(0).size(), packetList.size());

        for (int i = 0; i < packetList.size(); i++) {
            assertTrue(outputPackets.get(0).get(i) == packetList.get(i));//it should be the exact same object, so I am comparing references
        }

        assertEquals(0, getCurrentQueue(roundRobinScheduling));
    }

    @Test
    public void testDecitePacketsToMoveFromOutputQueue_multiple_queues() {
        QosMechanism qosMechanism = new QosMechanism(null, null, null);

        node1 = new Router("node1", qosMechanism, 200, 10, 50, 10, 10, 100, 0, 0, null);

        final Packet p1 = new Packet(10, null, null, 0);
        final Packet p2 = new Packet(10, null, null, 0);
        final Packet p3 = new Packet(10, null, null, 0);

        Map<Integer, List<Packet>> outputPackets = new HashMap<Integer, List<Packet>>() {{
            put(0, Arrays.asList(p1, p2));
            put(1, Arrays.asList(p3));
        }};

        List<Packet> packetList = roundRobinScheduling.decitePacketsToMoveFromOutputQueue(node1, outputPackets);

        assertNotNull(packetList);
        assertEquals(outputPackets.get(0).size() + outputPackets.get(1).size(), packetList.size());

        assertTrue(outputPackets.get(0).get(0) == packetList.get(0));  //p1
        assertTrue(outputPackets.get(1).get(0) == packetList.get(1));  //p3
        assertTrue(outputPackets.get(0).get(1) == packetList.get(2));  //p2

        assertEquals(0, getCurrentQueue(roundRobinScheduling));
    }

    @Test
    public void testDecitePacketsToMoveFromOutputQueue_multiple_queues_multiple_calls() {
        QosMechanism qosMechanism = new QosMechanism(null, null, null);

        node1 = new Router("node1", qosMechanism, 200, 10, 50, 10, 10, 100, 0, 0, null);

        final Packet p1 = new Packet(10, null, null, 0);
        final Packet p2 = new Packet(10, null, null, 0);
        final Packet p3 = new Packet(10, null, null, 0);

        Map<Integer, List<Packet>> outputPackets = new HashMap<Integer, List<Packet>>() {{
            put(0, Arrays.asList(p1, p2));
            put(1, Arrays.asList(p3));
        }};

        TestUtils.setWithoutSetter(RoundRobinScheduling.class, roundRobinScheduling, "currentQueue", 1);//starting with queue no. 1

        List<Packet> packetList = roundRobinScheduling.decitePacketsToMoveFromOutputQueue(node1, outputPackets);


        assertNotNull(packetList);
        assertEquals(outputPackets.get(0).size() + outputPackets.get(1).size(), packetList.size());

        assertTrue(outputPackets.get(1).get(0) == packetList.get(0));//p3
        assertTrue(outputPackets.get(0).get(0) == packetList.get(1));//p1
        assertTrue(outputPackets.get(0).get(1) == packetList.get(2));//p2

        assertEquals(1, getCurrentQueue(roundRobinScheduling));
    }


    @Test
    public void testDecitePacketsToMoveFromOutputQueue_empty_queues_1() {
        QosMechanism qosMechanism = new QosMechanism(null, null, null);

        node1 = new Router("node1", qosMechanism, 200, 10, 50, 10, 10, 100, 0, 0, null);

        final Packet p1 = new Packet(10, null, null, 0);
        final Packet p2 = new Packet(10, null, null, 0);

        Map<Integer, List<Packet>> outputPackets = new HashMap<Integer, List<Packet>>() {{
            put(0, Arrays.asList(p1, p2));
            put(1, new LinkedList<Packet>()); //second queue is empty
        }};


        List<Packet> packetList = roundRobinScheduling.decitePacketsToMoveFromOutputQueue(node1, outputPackets);


        assertNotNull(packetList);
        assertEquals(outputPackets.get(0).size() + outputPackets.get(1).size(), packetList.size());

        assertTrue(outputPackets.get(0).get(0) == packetList.get(0));//p1
        assertTrue(outputPackets.get(0).get(1) == packetList.get(1));//p2

        assertEquals(0, getCurrentQueue(roundRobinScheduling));
    }


    @Test
    public void testDecitePacketsToMoveFromOutputQueue_empty_queues_2() {
        QosMechanism qosMechanism = new QosMechanism(null, null, null);

        node1 = new Router("node1", qosMechanism, 200, 10, 50, 10, 10, 100, 0, 0, null);


        Map<Integer, List<Packet>> outputPackets = new HashMap<Integer, List<Packet>>();

        try {
            List<Packet> packetList = roundRobinScheduling.decitePacketsToMoveFromOutputQueue(node1, outputPackets);
            fail("exception should be thrown");
        } catch (IllegalStateException e) {
            //OK
        }
    }


    private int getCurrentQueue(RoundRobinScheduling round) {
        return (Integer) TestUtils.getPropertyWithoutGetter(RoundRobinScheduling.class, round, "currentQueue");
    }
}
