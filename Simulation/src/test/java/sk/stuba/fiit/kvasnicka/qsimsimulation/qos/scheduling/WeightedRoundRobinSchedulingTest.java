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
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.WeightedRoundRobinScheduling;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Igor Kvasnicka
 */
public class WeightedRoundRobinSchedulingTest {
    private WeightedRoundRobinScheduling weightedRoundRobinScheduling;
    private NetworkNode node1;

    @Before
    public void before() {

    }

    @Test
    public void testConstructor_null() {
        try {
            new WeightedRoundRobinScheduling(null);
            fail("no parameters - exception should be thrown");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void testConstructor_no_class_count_parameter() {
        try {
            new WeightedRoundRobinScheduling(new HashMap<String, Object>());
            fail("no class count parameter - exception should be thrown");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void testConstructor_wrong_parameter_value_type() {
        try {
            new WeightedRoundRobinScheduling(new HashMap<String, Object>() {{
                put(WeightedRoundRobinScheduling.QUEUES_WEIGHT, "test");
            }});
            fail("parameter class count should has integer value - exception should be thrown");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void testConstructor_ok() {
        new WeightedRoundRobinScheduling(new HashMap<String, Object>() {{
            put(WeightedRoundRobinScheduling.QUEUES_WEIGHT, new int[]{});
        }});
    }


    @Test
    public void testDecitePacketsToMoveFromOutputQueue_three_queues() {
        final int[] queueWeights = new int[]{1,2,1};


        weightedRoundRobinScheduling = new WeightedRoundRobinScheduling(new HashMap<String, Object>() {
            {
                put(WeightedRoundRobinScheduling.QUEUES_WEIGHT, queueWeights);
            }
        });

        QosMechanismDefinition qosMechanism = new QosMechanismDefinition(null, null, null, null);

        node1 = new Router("node1", null, qosMechanism, 200, 10, null, 10, 10, 100, 0, 0);

        final Packet p1 = new Packet(10, null, null, 0);
        final Packet p2 = new Packet(10, null, null, 0);
        final Packet p3 = new Packet(10, null, null, 0);
        final Packet p4 = new Packet(10, null, null, 0);
        final Packet p5 = new Packet(10, null, null, 0);
        final Packet p6 = new Packet(10, null, null, 0);
        Packet p7 = new Packet(10, null, null, 0);
        Packet p8 = new Packet(10, null, null, 0);
        Packet p9 = new Packet(10, null, null, 0);
        Packet p10 = new Packet(10, null, null, 0);

        Map<Integer, List<Packet>> outputPackets = new HashMap<Integer, List<Packet>>() {{
            put(0, Arrays.asList(p1, p2));
            put(1, Arrays.asList(p3, p4));
            put(2, Arrays.asList(p5, p6));
        }};

        List<Packet> packetList = weightedRoundRobinScheduling.decitePacketsToMoveFromOutputQueue(node1, outputPackets);

        assertNotNull(packetList);
        assertEquals(6, packetList.size());


        assertTrue(packetList.get(0) == p1);
        assertTrue(packetList.get(1) == p3);
        assertTrue(packetList.get(2) == p4);
        assertTrue(packetList.get(3) == p5);
        assertTrue(packetList.get(4) == p2);
        assertTrue(packetList.get(5) == p6);
    }
}
