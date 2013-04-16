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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.PacketClassification;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.impl.IpPrecedenceClassification;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;

/**
 * @author Igor Kvasnicka
 */
public class QosUtilsTest {

    PacketClassification packetClassification;

    @Before
    public void before() {
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
    }


    @Test
    public void testCheckParameter_ok() {
        Map<String, Object> map = new HashMap<String, Object>();
        String key = "this is a test key";
        map.put(key, 4);

        try {
            QosUtils.checkParameter(map, Integer.class, key);
        } catch (ParameterException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCheckParameter_map_null() {
        String key = "this is a test key";

        try {
            QosUtils.checkParameter(null, Integer.class, key);
            fail("map is null - exception should be thrown");
        } catch (ParameterException e) {
            //OK
        }
    }

    @Test
    public void testCheckParameter_key_missing() {
        Map<String, Object> map = new HashMap<String, Object>();
        String key = "this is a test key";
        map.put(key, 4);

        try {
            QosUtils.checkParameter(map, Integer.class, "another key");
            fail("map does not countain desired key - exception should be thrown");
        } catch (ParameterException e) {
            //OK
        }
    }

    @Test
    public void testCheckParameter_wrong_class_value() {
        Map<String, Object> map = new HashMap<String, Object>();
        String key = "this is a test key";
        map.put(key, 4);

        try {
            QosUtils.checkParameter(map, String.class, key);
            fail("value is wrong type - exception should be thrown");
        } catch (ParameterException e) {
            //OK
        }
    }

    @Test
    public void testCheckParameter_value_null() {
        Map<String, Object> map = new HashMap<String, Object>();
        String key = "this is a test key";
        map.put(key, null);

        try {
            QosUtils.checkParameter(map, Integer.class, key);
            fail("value is null - exception should be thrown");
        } catch (ParameterException e) {
            //OK
        }
    }

    @Test
    public void testClassDefinition_definition_null() throws Exception {
        ClassDefinition[] classes = new ClassDefinition[2];
        try {
            QosUtils.checkClassDefinition(classes);
            fail("class definition is null - exception should be thrown");
        } catch (ParameterException e) {
            //OK
        }
    }

    @Test
    public void testClassDefinition_ok() throws Exception {


        ClassDefinition[] classes = new ClassDefinition[2];
        ClassDefinition classDefinition1 = new ClassDefinition(Arrays.asList(IpPrecedence.IP_PRECEDENCE_0), null, "name1");
        ClassDefinition classDefinition2 = new ClassDefinition(Arrays.asList(IpPrecedence.IP_PRECEDENCE_1, IpPrecedence.IP_PRECEDENCE_2), null, "name2");

        classDefinition1.setClassification(packetClassification);
        classDefinition2.setClassification(packetClassification);

        QosUtils.checkClassDefinition(new ClassDefinition[]{classDefinition1, classDefinition2});
    }

    @Test
    public void testClassDefinition_wrong() throws Exception {
        ClassDefinition classDefinition1 = new ClassDefinition(Arrays.asList(IpPrecedence.IP_PRECEDENCE_0), null, "name1");
        ClassDefinition classDefinition2 = new ClassDefinition(Arrays.asList(IpPrecedence.IP_PRECEDENCE_0, IpPrecedence.IP_PRECEDENCE_2), null, "name2");

        classDefinition1.setClassification(packetClassification);
        classDefinition2.setClassification(packetClassification);

        try {
            QosUtils.checkClassDefinition(new ClassDefinition[]{classDefinition1, classDefinition2});
            fail("parameter exception should be thrown - one queue is in multiple classes");
        } catch (ParameterException e) {
            //OK
        }
    }
}
