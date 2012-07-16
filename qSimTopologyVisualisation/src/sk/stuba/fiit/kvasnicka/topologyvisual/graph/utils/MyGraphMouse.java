package sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils;

/**
 * @author Igor Kvasnicka
 */
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.*;
import java.awt.event.MouseEvent;
import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.VertexCreatedEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.VertexCreatedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.Topology;

public class MyGraphMouse extends DefaultModalGraphMouse<TopologyVertex, TopologyEdge> {

    private static Logger logg = Logger.getLogger(MyGraphMouse.class);
    private TopologyVertexFactory vertexFactory;
    private Topology topology;

    /**
     * creates new instance
     *
     * @param rc render context
     * @param vertexFactory vertex factory to create new vertices
     */
    public MyGraphMouse(RenderContext<TopologyVertex, TopologyEdge> rc, TopologyVertexFactory vertexFactory, Topology topology) {
        this.vertexFactory = vertexFactory;
        this.topology = topology;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        if (!vertexFactory.canCreateVertex()) { //no vertex should be created
            return;

        }
        createVertex(e);
        topology.getTopolElementTopComponent().deselectAction();
    }

    /**
     * creates new vertex on the position where user clicked
     *
     * @param e
     */
    private void createVertex(MouseEvent e) {
        VisualizationViewer<TopologyVertex, TopologyEdge> vv = (VisualizationViewer<TopologyVertex, TopologyEdge>) e.getSource();
        Graph<TopologyVertex, TopologyEdge> graph = vv.getModel().getGraphLayout().getGraph();
        // create a new vertex
        TopologyVertex newVertex = vertexFactory.create();
        if (newVertex == null) {
            return; //user hit cancel button so he does not want to create vertex after all
        }
        graph.addVertex(newVertex);
        Layout<TopologyVertex, TopologyEdge> layout = vv.getModel().getGraphLayout();
        layout.setLocation(newVertex, vv.getRenderContext().getMultiLayerTransformer().inverseTransform(e.getPoint()));
        fireVertexCreatedEvent(new VertexCreatedEvent(this, newVertex));
        vv.repaint();
    }

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
