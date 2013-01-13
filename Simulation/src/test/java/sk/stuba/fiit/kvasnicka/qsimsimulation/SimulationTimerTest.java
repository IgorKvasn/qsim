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

package sk.stuba.fiit.kvasnicka.qsimsimulation;

import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.TestUtils;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * @author Igor Kvasnicka
 */
public class SimulationTimerTest {
    private SimulationTimer timer;
    private int MIN_TIMER_DELAY;
    private int MAX_TIMER_DELAY;

    @Before
    public void before() {
        timer = new SimulationTimer(Collections.<Edge>emptyList(), Collections.<NetworkNode>emptyList(), null);
        MIN_TIMER_DELAY = (Integer) TestUtils.getPropertyWithoutGetter(SimulationTimer.class, timer, "MIN_TIMER_DELAY");
        MAX_TIMER_DELAY = (Integer) TestUtils.getPropertyWithoutGetter(SimulationTimer.class, timer, "MAX_TIMER_DELAY");
    }

    @Test
    public void testConvertTime_1() {
        int result = (Integer) TestUtils.callPrivateMethod(SimulationTimer.class, timer, "convertTime", new Class[]{double.class}, new Object[]{1});
        assertEquals(MAX_TIMER_DELAY, result);
    }

    @Test
    public void testConvertTime_2() {
        int result = (Integer) TestUtils.callPrivateMethod(SimulationTimer.class, timer, "convertTime", new Class[]{double.class}, new Object[]{2});
        assertEquals(MAX_TIMER_DELAY / 2, result);
    }

    @Test
    public void testConvertTime_0_5() {
        int result = (Integer) TestUtils.callPrivateMethod(SimulationTimer.class, timer, "convertTime", new Class[]{double.class}, new Object[]{0.5});
        assertEquals(MAX_TIMER_DELAY * 2, result);
    }


    @Test
    public void testConvertTime_max() {
        int result = (Integer) TestUtils.callPrivateMethod(SimulationTimer.class, timer, "convertTime", new Class[]{double.class}, new Object[]{10000});
        assertEquals(MIN_TIMER_DELAY, result);
    }

    @Test
    public void testConvertTime_21() {
        int result = (Integer) TestUtils.callPrivateMethod(SimulationTimer.class, timer, "convertTime", new Class[]{double.class}, new Object[]{10000});
        int result_20 = (Integer) TestUtils.callPrivateMethod(SimulationTimer.class, timer, "convertTime", new Class[]{double.class}, new Object[]{1000010000});

        assertEquals(result_20, result);
    }
}
