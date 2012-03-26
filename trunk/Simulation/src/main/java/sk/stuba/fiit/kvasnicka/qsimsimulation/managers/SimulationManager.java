/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.qsimsimulation.managers;

import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationRuleBean;

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
