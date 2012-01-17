/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.graph.events;

import java.util.EventListener;

/**
 *
 * @author Igor Kvasnicka
 */
public interface VertexCreatedListener extends EventListener {

    public void vertexCreatedOccurred(VertexCreatedEvent evt);
}
