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

import java.util.HashMap;

/**
 * @author Igor Kvasnicka
 */
public class WeightedREDTest {
    WeightedRED wred;

    @Before
    public void before() {

    }

    @Test
    public void testManageQueue_parameters() throws Exception {
        wred = new WeightedRED(new HashMap<String, Object>() {{
            put(WeightedRED.WRED_DEFINITION, new WeightedRED.WredDefinition[]{});
        }});
    }

    //todo finish
}
