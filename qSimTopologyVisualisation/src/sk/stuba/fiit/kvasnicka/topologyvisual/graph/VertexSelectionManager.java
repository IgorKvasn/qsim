/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.graph;

import java.util.LinkedHashSet;
import java.util.List;
import lombok.Getter;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.VertexSelectionChangedEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.VertexSelectionChangedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.RouterVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;

/**
 *
 * @author Igor Kvasnicka
 */
@Deprecated
public class VertexSelectionManager { //todo this class is obsolete - now a default JUNG's pick behaviour is used to handle vertex selection

    @Getter
    private LinkedHashSet<TopologyVertex> selectedVertices = new LinkedHashSet<TopologyVertex>();

    /**
     * adds vertex to a list of all selected vertices <br>this method does not
     * actually visually select a vertex
     *
     * @param v
     */
    public void addSelectedVertex(TopologyVertex v) {
        selectedVertices.add(v);
        fireVertexCreatedEvent(new VertexSelectionChangedEvent(this));
    }

    /**
     * removes latest selected vertex from a list of all selected vertices
     * <br>this method does not actually visually deselect a vertex
     *
     * @param v
     */
    public void removeSelectedVertex(TopologyVertex v) {
        selectedVertices.remove(v);
        fireVertexCreatedEvent(new VertexSelectionChangedEvent(this));
    }

    /**
     * returns historically first vertex that selected of all currently selected
     * vertices if no vertices are selected, null will be returned
     *
     * @return first vertex or null
     */
    public TopologyVertex getFirstSelectedVertex() {
        for (TopologyVertex v : selectedVertices) {
            return v;//the first vertex will be returned
        }
        return null;
    }

    /**
     * returns historically first RouterVertex that selected of all currently
     * selected vertices if no RouterVertices are selected, null will be
     * returned
     *
     * @return first RouterVertex or null
     */
    public RouterVertex getFirstSelectedRouterVertex() {
        for (TopologyVertex v : selectedVertices) {
            if (v instanceof RouterVertex) {
                return (RouterVertex) v;//the first vertex will be returned
            }
        }
        return null;
    }
    private javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

    public void addVertexSelectionChangedListener(VertexSelectionChangedListener listener) {
        listenerList.add(VertexSelectionChangedListener.class, listener);
    }

    public void removeVertexSelectionChangedListener(VertexSelectionChangedListener listener) {
        listenerList.remove(VertexSelectionChangedListener.class, listener);
    }

    private synchronized void fireVertexCreatedEvent(VertexSelectionChangedEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == VertexSelectionChangedListener.class) {
                ((VertexSelectionChangedListener) listeners[i + 1]).vertexSelectionChangedOccurred(evt);
            }
        }
    }
}
