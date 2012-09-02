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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.queuemanagement.impl;

import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Igor Kvasnicka
 */
public class RandomEarlyDetectionTest {
    RandomEarlyDetection red;
    Packet packet;

    @Before
    public void before() {
        packet = new Packet(15, null, null, null, 1);
        packet.setQosQueue(0);

        red = new RandomEarlyDetection(new HashMap<String, Object>() {{
            put(RandomEarlyDetection.EXPONENTIAL_WEIGHT_FACTOR, .02);
            put(RandomEarlyDetection.MAX_PROBABILITY, .9);
            put(RandomEarlyDetection.MAX_THRESHOLD, .2);
            put(RandomEarlyDetection.MIN_THRESHOLD, .1);
        }});
    }

    @Test
    public void testManageQueue_over_threshold() throws Exception {
        List<Packet> queue = new LinkedList<Packet>();
        for (int i = 0; i < 16; i++) {
            Packet p = new Packet(150, null, null, null, 1);
            p.setQosQueue(0);
            queue.add(p);
        }

        assertFalse(red.manageQueue(queue, packet));
    }

    @Test
    public void testManageQueue_below_threshold() throws Exception {
        assertTrue(red.manageQueue(new LinkedList<Packet>(), packet));
    }

    @Test
    public void testManageQueue_probability() throws Exception {
        List<Packet> queue = new LinkedList<Packet>();
        for (int i = 0; i < 6; i++) {
            Packet p = new Packet(150, null, null, null, 1);
            p.setQosQueue(0);
            queue.add(p);
        }

        //just run the method to see if no exception is thrown
        //there is no way how to test it, because it is randomized
        red.manageQueue(queue, packet);
    }

    @Test
    public void testManageQueue_multiple_calls() throws Exception {
        assertTrue(red.manageQueue(new LinkedList<Packet>(), packet));
        assertTrue(red.manageQueue(new LinkedList<Packet>(), packet));
    }
}
