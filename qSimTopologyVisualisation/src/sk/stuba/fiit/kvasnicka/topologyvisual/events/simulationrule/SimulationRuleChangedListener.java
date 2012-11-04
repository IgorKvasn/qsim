/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.events.simulationrule;

import java.util.EventListener;

/**
 * there has been some change in simulation rules definition (user has
 * created/deleted or modified a rule)
 *
 * @author Igor Kvasnicka
 */
public interface SimulationRuleChangedListener extends EventListener {

    void simulationRuleChangedOccured(SimulationRuleChangedEvent evt);
}