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

package sk.stuba.fiit.kvasnicka.qsimsimulation.managers;

import org.junit.Before;
import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * @author Igor Kvasnicka
 */
public class SimulationManagerTest {
    private SimulationManager simulationManager;
    private NetworkNode node1, node2;


    @Before
    public void before() {
        simulationManager = new SimulationManager();

        node1 = new Router("node1", null, null, 10, 10, 50, 10, 10, 100, 0, 0);
        node2 = new Router("node2", null, null, 10, 10, 50, 10, 10, 100, 0, 0);
    }

    @Test
    public void testPingAdd() {
        SimulationRuleBean rule = new SimulationRuleBean("", node1, node2, 0, 0, 0,  Layer4TypeEnum.UDP, IpPrecedence.IP_PRECEDENCE_0, null,  0, 0);
        simulationManager.addSimulationRule(rule);

        assertNotNull(simulationManager.getRulesUnmodifiable());
        assertEquals(1, simulationManager.getRulesModifiable().size());
    }

    @Test
    public void testPingRemove() {

        //preparation
        testPingAdd(); //first add some ping simulation rule
        SimulationRuleBean rule = simulationManager.getRulesUnmodifiable().get(0);//there cannot be any NullPointerEx, because it was checked in the method above

        //test
        simulationManager.removeSimulationRule(rule);//remove the rul

        //assert
        assertNotNull(simulationManager.getRulesUnmodifiable());
        assertEquals(0, simulationManager.getRulesUnmodifiable().size());
    }
}
