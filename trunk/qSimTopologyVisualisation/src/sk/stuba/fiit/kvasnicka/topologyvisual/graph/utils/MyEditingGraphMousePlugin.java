/*
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
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.EditingGraphMousePlugin;
import org.apache.commons.collections15.Factory;

import java.awt.event.MouseEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.VertexCreatedEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.VertexCreatedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;

/**
 * @author Igor Kvasnicka
 */
@Deprecated
public class MyEditingGraphMousePlugin extends EditingGraphMousePlugin<TopologyVertex, TopologyEdge> {

    /**
     * creates instance of my implementation of EditingGraphMousePlugin
     *
     * @param vertexFactory vertex factory
     * @param edgeFactory edge factory
     */
    public MyEditingGraphMousePlugin(Factory<TopologyVertex> vertexFactory, Factory<TopologyEdge> edgeFactory) {
        super(vertexFactory, edgeFactory);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (checkModifiers(e)) {
            @SuppressWarnings({"unchecked"})
            final VisualizationViewer<TopologyVertex, TopologyEdge> vv = (VisualizationViewer<TopologyVertex, TopologyEdge>) e.getSource();
            GraphElementAccessor<TopologyVertex, TopologyEdge> pickSupport = vv.getPickSupport();
            if (pickSupport != null) {
                Graph<TopologyVertex, TopologyEdge> graph = vv.getModel().getGraphLayout().getGraph();
                // set default edge type
                if (graph instanceof DirectedGraph) {
                    edgeIsDirected = EdgeType.DIRECTED;
                } else {
                    edgeIsDirected = EdgeType.UNDIRECTED;
                }
                // make a new vertex
                TopologyVertex newVertex = vertexFactory.create();
                e.consume();
                if (newVertex == null) {
                    return;//user hit cancel button so he does not want to create vertex after all
                }
                graph.addVertex(newVertex);
                Layout<TopologyVertex, TopologyEdge> layout = vv.getModel().getGraphLayout();
                layout.setLocation(newVertex, vv.getRenderContext().getMultiLayerTransformer().inverseTransform(e.getPoint()));
                fireVertexCreatedEvent(new VertexCreatedEvent(this, newVertex));
            }
            vv.repaint();
        }
    }
    private javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

    public void addVertexCreatedListener(VertexCreatedListener listener) {
        listenerList.add(VertexCreatedListener.class, listener);
    }

    public void removeVertexCreatedListener(VertexCreatedListener listener) {
        listenerList.remove(VertexCreatedListener.class, listener);
    }

    private void fireVertexCreatedEvent(VertexCreatedEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == VertexCreatedListener.class) {
                ((VertexCreatedListener) listeners[i + 1]).vertexCreatedOccurred(evt);
            }
        }
    }
}
