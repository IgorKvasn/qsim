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
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanismDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.FifoScheduling;

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
public class FifoSchedulingTest {

    private PacketScheduling fifoScheduling;
    private NetworkNode node1;

    @Before
    public void before() {
        fifoScheduling = new FifoScheduling();
    }

    @Test
    public void testDecitePacketsToMoveFromOutputQueue() {
        QosMechanismDefinition qosMechanism = new QosMechanismDefinition(null, null,null,null, null);

        node1 = new Router("node1", null, qosMechanism, 200, 10, null, 10, 10, 100, 0, 0);

        final Packet p1 = new Packet(10, null, null, 0);
        final Packet p2 = new Packet(10, null, null, 0);
        final Packet p3 = new Packet(10, null, null, 0);

        Map<Integer, List<Packet>> outputPackets = new HashMap<Integer, List<Packet>>() {{
            put(0, Arrays.asList(p1, p2, p3));
        }};

        List<Packet> packetList = fifoScheduling.decitePacketsToMoveFromOutputQueue(node1, outputPackets);

        assertNotNull(packetList);
        assertEquals(outputPackets.get(0).size(), packetList.size());

        for (int i = 0; i < outputPackets.size(); i++) {
            assertTrue(outputPackets.get(0).get(i) == packetList.get(i));//it should be the exact same object, so I am comparing references
        }
    }

    @Test
    public void testDecitePacketsToMoveFromOutputQueue_multiple_queues() {
        QosMechanismDefinition qosMechanism = new QosMechanismDefinition(null, null,null, null, null);

        node1 = new Router("node1", null, qosMechanism, 200, 10, null, 10, 10, 100, 0, 0);

        Packet p1 = new Packet(10, null, null, 0);
        Packet p2 = new Packet(10, null, null, 0);
        Packet p3 = new Packet(10, null, null, 0);

        Map<Integer, List<Packet>> outputPackets = new HashMap<Integer, List<Packet>>();

        List<Packet> packetList = fifoScheduling.decitePacketsToMoveFromOutputQueue(node1, outputPackets);
        assertTrue(packetList.isEmpty());
    }
}
