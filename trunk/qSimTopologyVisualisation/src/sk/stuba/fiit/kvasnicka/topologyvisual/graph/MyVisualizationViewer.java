/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.graph;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.MouseListenerTranslator;
import java.awt.event.MouseListener;
import java.util.Map;
import java.util.WeakHashMap;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;

/**
 *
 * @author Igor Kvasnicka
 */
public class MyVisualizationViewer extends VisualizationViewer<TopologyVertex, TopologyEdge> {

    private Map<GraphMouseListener<TopologyVertex>, MouseListenerTranslator<TopologyVertex, TopologyEdge>> mapping;

    public MyVisualizationViewer(Layout<TopologyVertex, TopologyEdge> layout) {
        super(layout);
        mapping = new WeakHashMap<GraphMouseListener<TopologyVertex>, MouseListenerTranslator<TopologyVertex, TopologyEdge>>();
    }

    @Override
    public void addGraphMouseListener(GraphMouseListener<TopologyVertex> gel) {
        MouseListenerTranslator<TopologyVertex, TopologyEdge> mouseListenerTranslator = new MouseListenerTranslator<TopologyVertex, TopologyEdge>(gel, this);
        mapping.put(gel, mouseListenerTranslator);
        addMouseListener(mouseListenerTranslator);

    }

    public void removeGraphMouseListener(GraphMouseListener<TopologyVertex> gel) {
        if (!mapping.containsKey(gel)) {
            return;
        }
        MouseListener l = mapping.get(gel);
        removeMouseListener(l);
    }
}
