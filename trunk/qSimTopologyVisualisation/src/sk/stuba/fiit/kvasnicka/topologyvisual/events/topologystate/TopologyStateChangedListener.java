/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.events.topologystate;

import java.util.EventListener;

/**
 *
 * @author Igor Kvasnicka
 */
public interface TopologyStateChangedListener extends EventListener {

    void topologyStateChangeOccured(TopologyStateChangedEvent event);
}
