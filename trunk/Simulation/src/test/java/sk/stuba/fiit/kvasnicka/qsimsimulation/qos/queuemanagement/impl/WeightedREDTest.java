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

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.PacketClassification;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.impl.IpPrecedenceClassification;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.ClassDefinition;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Igor Kvasnicka
 */
public class WeightedREDTest {
    WeightedRED wred;
    Packet packet1, packet2;
    PacketClassification packetClassification;

    @Before
    public void before() {

        packet1 = new Packet(15, null, null, 1);
        packet1.setQosQueue(0, 2);
        packet1.setMarking(IpPrecedence.IP_PRECEDENCE_0, null);

        packet2 = new Packet(15, null, null, 1);
        packet2.setMarking(IpPrecedence.IP_PRECEDENCE_2, null);
        packet2.setQosQueue(1, 2);

        ClassDefinition classDefinition1 = new ClassDefinition(Arrays.asList(IpPrecedence.IP_PRECEDENCE_0), null, "name1");
        ClassDefinition classDefinition2 = new ClassDefinition(Arrays.asList(IpPrecedence.IP_PRECEDENCE_1), null, "name2");

        packetClassification = EasyMock.createMock(IpPrecedenceClassification.class);
        EasyMock.expect(packetClassification.convertClassificationToQueue(EasyMock.anyObject(List.class), EasyMock.anyObject(List.class))).andAnswer(new IAnswer<List<Integer>>() {
            @Override
            public List<Integer> answer() throws Throwable {
                List<IpPrecedence> list = (List<IpPrecedence>) EasyMock.getCurrentArguments()[0];
                if (list.contains(IpPrecedence.IP_PRECEDENCE_0)) {
                    return Arrays.asList(1);
                } else {
                    return Arrays.asList(0);
                }
            }
        }).times(4);

        EasyMock.replay(packetClassification);

        classDefinition1.setClassification(packetClassification);
        classDefinition2.setClassification(packetClassification);


        final WeightedRED.WredDefinition[] defs = new WeightedRED.WredDefinition[2];
        defs[0] = new WeightedRED.WredDefinition(classDefinition1, .02, .2, .1, .9);
        defs[1] = new WeightedRED.WredDefinition(classDefinition2, .2, .2, .1, .9);

        wred = new WeightedRED(new HashMap<String, Object>() {{
            put(WeightedRED.WRED_DEFINITION, defs);
        }});
    }

    @Test
    public void testManageQueue() throws Exception {
        List<Packet> queue = new LinkedList<Packet>();
        for (int i = 0; i < 16; i++) {
            Packet p = new Packet(150, null, null, 1);
            p.setQosQueue(0, 2);
            queue.add(p);
        }

        assertFalse(wred.manageQueue(queue, packet1));//over maximum threshold

        List<Packet> queue2 = new LinkedList<Packet>();
        assertTrue(wred.manageQueue(queue2, packet2));//below minimum threshold
    }

    @Test
    public void testManageQueue_wrong_queue() {
        List<Packet> queue2 = new LinkedList<Packet>();
        Packet p = new Packet(15, null, null, 1);
        p.setQosQueue(10, 100);
        try {
            wred.manageQueue(queue2, p);
            fail("undefined queue number - exception should be thrown");
        } catch (IllegalStateException e) {
            //ok
        }
    }

    @Test
    public void testManageQueue_default_red_definition() {
        ClassDefinition classDefinition1 = new ClassDefinition(Arrays.asList(IpPrecedence.IP_PRECEDENCE_0), null, "name1");
        ClassDefinition classDefinition2 = new ClassDefinition(Arrays.asList(IpPrecedence.IP_PRECEDENCE_1), null, "");
        classDefinition1.setClassification(packetClassification);
        classDefinition2.setClassification(packetClassification);


        final WeightedRED.WredDefinition[] defs = new WeightedRED.WredDefinition[2];
        defs[0] = new WeightedRED.WredDefinition(classDefinition1, .02, .2, .1, .9);
        defs[1] = new WeightedRED.WredDefinition(classDefinition2, .2, .2, .1, .9);

        wred = new WeightedRED(new HashMap<String, Object>() {{
            put(WeightedRED.WRED_DEFINITION, defs);
        }});


        List<Packet> queue2 = new LinkedList<Packet>();
        Packet p = new Packet(15, null, null, 1);
        p.setQosQueue(10, 100);
        wred.manageQueue(queue2, p);//all I care about is if no exception is thrown
    }
}
