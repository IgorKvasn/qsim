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
