/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.events;

import java.util.EventListener;

/**
 * used to listen for changes of active topology top component
 *
 * @author Igor Kvasnicka
 */
public interface TopologyWindowChangeListener extends EventListener {

    public void topologyWindowChangeOccurred(TopologyWindowChangeEvent evt);
}
