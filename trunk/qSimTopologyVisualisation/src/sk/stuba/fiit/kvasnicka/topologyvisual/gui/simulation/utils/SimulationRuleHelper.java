/*
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
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.utils;

import sk.stuba.fiit.kvasnicka.qsimsimulation.facade.SimulationFacade;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.SimulationData;

/**
 *
 * @author Igor Kvasnicka
 */
public class SimulationRuleHelper {

    /**
     * creates new simulation rule
     *
     * @return simulation rule
     */
    private static SimulationRuleBean createSimulationRule(SimulationData.Data data) {
        SimulationRuleBean rule = new SimulationRuleBean(data.getName(), data.getSourceVertex().getDataModel(), data.getDestinationVertex().getDataModel(), data.getPacketCount(), data.getPacketSize(), data.getActivationDelay(), data.getLayer4protocol(), data.getIpPrecedence(), data.getDscpValuesEnum(), data.getSrcPort(), data.getDestPort());
        return rule;
    }

    /**
     * creates new simulation rule
     *
     * @param data
     */
    public static SimulationRuleBean newSimulationRule(SimulationFacade simulationFacade, SimulationData.Data data) {
        if (simulationFacade == null) {
            throw new IllegalArgumentException("simulation facade is NULL");
        }

        if (data == null) {
            throw new IllegalArgumentException("simulation rule data is NULL");
        }

        SimulationRuleBean rule = createSimulationRule(data);
        return rule;
    }
}
