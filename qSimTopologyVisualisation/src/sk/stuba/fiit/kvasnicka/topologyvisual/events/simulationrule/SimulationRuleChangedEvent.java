/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.events.simulationrule;

import java.util.EventObject;

/**
 *
 * @author Igor Kvasnicka
 */
public class SimulationRuleChangedEvent extends EventObject {

    public SimulationRuleChangedEvent(Object source) {
        super(source);
    }
}
