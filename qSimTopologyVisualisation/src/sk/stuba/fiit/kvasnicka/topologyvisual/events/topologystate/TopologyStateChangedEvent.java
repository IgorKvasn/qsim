/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.events.topologystate;

import java.util.EventObject;
import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.TopologyStateEnum;

/**
 * topology state has changed
 *
 * @author Igor Kvasnicka
 */
@Getter
public class TopologyStateChangedEvent extends EventObject {

    private TopologyStateEnum newState;

    public TopologyStateChangedEvent(Object source, TopologyStateEnum newState) {
        super(source);
        this.newState = newState;

    }
}
