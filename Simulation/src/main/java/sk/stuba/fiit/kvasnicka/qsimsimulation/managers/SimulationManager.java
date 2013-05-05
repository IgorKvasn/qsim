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

import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.simulationrule.SimulationRuleEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.simulationrule.SimulationRuleListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Igor Kvasnicka
 */
public class SimulationManager {
    private static Logger logg = Logger.getLogger(SimulationManager.class);

    /**
     * all simulation rules defined for one topology
     */
    private List<SimulationRuleBean> rules;
    private transient javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();


    public SimulationManager() {
        rules = new LinkedList<SimulationRuleBean>();
    }

    /**
     * adds simulation rule into list of all simulation rules
     *
     * @param rule
     */
    public void addSimulationRule(SimulationRuleBean rule) {
        rules.add(rule);
        fireSimulationRuleAddedEvent(new SimulationRuleEvent(this, rule));
    }

    public void removeSimulationRule(SimulationRuleBean rule) {
        if (rules.remove(rule)) {
            fireSimulationRuleRemovedEvent(new SimulationRuleEvent(this, rule));
        } else {
            logg.warn("No matchin simulation rule found to be removed");
        }
    }

    /**
     * removes all simulation rules
     */
    public void removeAllSimulationRule() {
        rules.clear();
        fireSimulationRuleRemovedEvent(new SimulationRuleEvent(this,null));
    }

    /**
     * returns list of simulation rules that cannot by modified (read-only)
     *
     * @return
     */
    public List<SimulationRuleBean> getRulesUnmodifiable() {
        return Collections.unmodifiableList(rules);
    }

    /**
     * returns list of simulation rules
     *
     * @return
     */
    public List<SimulationRuleBean> getRulesModifiable() {
        return rules;
    }

    public void addSimulationRuleListener(SimulationRuleListener listener) {
        listenerList.add(SimulationRuleListener.class, listener);
    }

    public void removeSimulationRuleListener(SimulationRuleListener listener) {
        listenerList.remove(SimulationRuleListener.class, listener);
    }

    private void fireSimulationRuleAddedEvent(SimulationRuleEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i].equals(SimulationRuleListener.class)) {
                ((SimulationRuleListener) listeners[i + 1]).simulationRuleAdded(evt);
            }
        }
    }

    private void fireSimulationRuleRemovedEvent(SimulationRuleEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i].equals(SimulationRuleListener.class)) {
                ((SimulationRuleListener) listeners[i + 1]).simulationRuleRemoved(evt);
            }
        }
    }
}
