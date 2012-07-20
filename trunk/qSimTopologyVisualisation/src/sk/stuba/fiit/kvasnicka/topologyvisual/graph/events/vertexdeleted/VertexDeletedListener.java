/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.vertexdeleted;

import java.util.EventListener;

/**
 *
 * @author Igor Kvasnicka
 */
public interface VertexDeletedListener extends EventListener {

    public void vertexDeletedOccurred(VertexDeletedEvent evt);
}
