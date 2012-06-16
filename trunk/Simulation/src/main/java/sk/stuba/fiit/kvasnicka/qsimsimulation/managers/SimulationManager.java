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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.qsimsimulation.managers;

import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Igor Kvasnicka
 */
public class SimulationManager {
    /**
     * all simulation rules defined for one topology
     */
    private List<SimulationRuleBean> rules;

    public SimulationManager() {
        rules = new LinkedList<SimulationRuleBean>();
    }

    public void addSimulationRule(SimulationRuleBean rule) {
        rules.add(rule);
    }

    /**
     * returns list of simulation rules that cannot by modified (rad-only)
     *
     * @return
     */
    public List<SimulationRuleBean> getRulesUnmodifiable() {
        return Collections.unmodifiableList(rules);
    }
}
