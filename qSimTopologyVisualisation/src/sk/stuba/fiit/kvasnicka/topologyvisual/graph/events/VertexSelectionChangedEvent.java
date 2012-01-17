/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.graph.events;

import java.util.EventObject;

/**
 *
 * @author Igor Kvasnicka
 */
public class VertexSelectionChangedEvent extends EventObject {

    public VertexSelectionChangedEvent(Object source) {
        super(source);
    }
}
