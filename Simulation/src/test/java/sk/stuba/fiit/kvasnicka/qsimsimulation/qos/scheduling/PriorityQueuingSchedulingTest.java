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
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.OutputQueueManager;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.PriorityQueuingScheduling;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Igor Kvasnicka
 */
public class PriorityQueuingSchedulingTest {
    private PriorityQueuingScheduling roundRobinScheduling;
    private NetworkNode node1;

    @Before
    public void before() {
        roundRobinScheduling = new PriorityQueuingScheduling();
    }

    @Test
    public void testDecitePacketsToMoveFromOutputQueue_one_queue() {
        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});
        QosMechanism qosMechanism = new QosMechanism(null, null, null);

        node1 = new Router("node1", qosMechanism, outputQueueManager1, 200, 10, 10, 10, 100, 0, 0, null);

        Packet p1 = new Packet(10, null, null, 0);
        Packet p2 = new Packet(10, null, null, 0);
        Packet p3 = new Packet(10, null, null, 0);

        List<List<Packet>> outputPackets = Arrays.asList(Arrays.asList(p1, p2, p3));

        List<Packet> packetList = roundRobinScheduling.decitePacketsToMoveFromOutputQueue(node1, outputPackets);

        assertNotNull(packetList);
        assertEquals(outputPackets.get(0).size(), packetList.size());

        for (int i = 0; i < packetList.size(); i++) {
            assertTrue(outputPackets.get(0).get(i) == packetList.get(i));//it should be the exact same object, so I am comparing references
        }
    }


    @Test
    public void testDecitePacketsToMoveFromOutputQueue_multiple_queues() {
        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});
        QosMechanism qosMechanism = new QosMechanism(null, null, null);

        node1 = new Router("node1", qosMechanism, outputQueueManager1, 200, 10, 10, 10, 100, 0, 0, null);

        Packet p1 = new Packet(10, null, null, 0);
        Packet p2 = new Packet(10, null, null, 0);
        Packet p3 = new Packet(10, null, null, 0);
        Packet p4 = new Packet(10, null, null, 0);

        List<List<Packet>> outputPackets = Arrays.asList(Arrays.asList(p1, p2), Arrays.asList(p3, p4));

        List<Packet> packetList = roundRobinScheduling.decitePacketsToMoveFromOutputQueue(node1, outputPackets);

        assertNotNull(packetList);
        assertEquals(outputPackets.get(0).size() + outputPackets.get(1).size(), packetList.size());

        assertTrue(outputPackets.get(1).get(0) == packetList.get(0));   //p3
        assertTrue(outputPackets.get(1).get(1) == packetList.get(1));   //p4
        assertTrue(outputPackets.get(0).get(0) == packetList.get(2));   //p1
        assertTrue(outputPackets.get(0).get(1) == packetList.get(3));   //p2
    }

    @Test
    public void testDecitePacketsToMoveFromOutputQueue_no_queues() {
        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});
        QosMechanism qosMechanism = new QosMechanism(null, null, null);

        node1 = new Router("node1", qosMechanism, outputQueueManager1, 200, 10, 10, 10, 100, 0, 0, null);

        Packet p1 = new Packet(10, null, null, 0);
        Packet p2 = new Packet(10, null, null, 0);
        Packet p3 = new Packet(10, null, null, 0);

        List<List<Packet>> outputPackets = new LinkedList<List<Packet>>();
        try {
            List<Packet> packetList = roundRobinScheduling.decitePacketsToMoveFromOutputQueue(node1, outputPackets);
            fail("exception should be thrown");
        } catch (IllegalStateException e) {
            //OK
        }
    }
}
