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

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;

/**
 * @author Igor Kvasnicka
 */
public class QosUtilsTest {

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
}
