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
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.FifoScheduling;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Igor Kvasnicka
 */
public class FifoSchedulingTest {

    private PacketScheduling fifoScheduling;
    private NetworkNode node1;

    @Before
    public void before() {
        fifoScheduling = new FifoScheduling();
    }

    @Test
    public void testDecitePacketsToMoveFromOutputQueue() {
        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1});
        QosMechanism qosMechanism = new QosMechanism();

        node1 = new Router("node1", qosMechanism, outputQueueManager1, 200, 10, 10, 10, 100, 0, 0);

        Packet p1 = new Packet(10, Layer4TypeEnum.UDP, null, null, 0);
        Packet p2 = new Packet(10, Layer4TypeEnum.UDP, null, null, 0);
        Packet p3 = new Packet(10, Layer4TypeEnum.UDP, null, null, 0);

        List<List<Packet>> outputPackets = Arrays.asList(Arrays.asList(p1, p2, p3));

        List<Packet> packetList = fifoScheduling.decitePacketsToMoveFromOutputQueue(node1, outputPackets);

        assertNotNull(packetList);
        assertEquals(outputPackets.get(0).size(), packetList.size());

        for (int i = 0; i < outputPackets.size(); i++) {
            assertTrue(outputPackets.get(0).get(i) == packetList.get(i));//it should be the exact same object, so I am comparing references
        }
    }

    @Test
    public void testDecitePacketsToMoveFromOutputQueue_multiple_queues() {
        OutputQueue q1 = new OutputQueue(50, "queue 1");
        OutputQueue q2 = new OutputQueue(50, "queue 2");
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{q1, q2});
        QosMechanism qosMechanism = new QosMechanism();

        node1 = new Router("node1", qosMechanism, outputQueueManager1, 200, 10, 10, 10, 100, 0, 0);

        Packet p1 = new Packet(10, Layer4TypeEnum.UDP, null, null, 0);
        Packet p2 = new Packet(10, Layer4TypeEnum.UDP, null, null, 0);
        Packet p3 = new Packet(10, Layer4TypeEnum.UDP, null, null, 0);

        List<List<Packet>> outputPackets = Arrays.asList();
        try {
            List<Packet> packetList = fifoScheduling.decitePacketsToMoveFromOutputQueue(node1, outputPackets);
            fail("IllegalStateException should be thrown");
        } catch (IllegalStateException e) {
            //OK
        }
    }

    @Test
    public void testDecitePacketsToMoveFromOutputQueue_no_queue() {
        OutputQueueManager outputQueueManager1 = new OutputQueueManager(new OutputQueue[]{});
        QosMechanism qosMechanism = new QosMechanism();

        node1 = new Router("node1", qosMechanism, outputQueueManager1, 200, 10, 10, 10, 100, 0, 0);

        Packet p1 = new Packet(10, Layer4TypeEnum.UDP, null, null, 0);
        Packet p2 = new Packet(10, Layer4TypeEnum.UDP, null, null, 0);
        Packet p3 = new Packet(10, Layer4TypeEnum.UDP, null, null, 0);

        List<List<Packet>> outputPackets = Arrays.asList(Arrays.asList(p1, p2, p3), Arrays.asList(p1, p2));

        try {
            List<Packet> packetList = fifoScheduling.decitePacketsToMoveFromOutputQueue(node1, outputPackets);
            fail("IllegalStateException should be thrown");
        } catch (IllegalStateException e) {
            //OK
        }
    }
}
