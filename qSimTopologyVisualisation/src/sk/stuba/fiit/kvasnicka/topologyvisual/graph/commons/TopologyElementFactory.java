package sk.stuba.fiit.kvasnicka.topologyvisual.graph.commons;

import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.utils.DialogHandler;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.ComputerVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.RouterVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.SwitchVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.PaletteActionEnum;

/**
 * all factory methods that have something to do with creating new elements in topology (vertices - such as routers or PCs; edges,...)
 *
 * @author Igor Kvasnicka
 */
public abstract class TopologyElementFactory {

    /**
     * creates TopologyVertex object depending on user selection in TaskPane
     *
     * @param action what users selects in TaskPane
     * @param node   network node associated with this Vertex
     * @return new TopologyVertex object
     */
    public static TopologyVertex createVertex(PaletteActionEnum action, NetworkNode node) {
        if (action == null) {
            throw new IllegalArgumentException("TaskPaneAction is NULL");
        }
        switch (action) {
            case NEW_VERTEX_PC:
                return new ComputerVertex(node);
            case NEW_VERTEX_SWITCH:
                return new SwitchVertex(node);
            case NEW_VERTEX_ROUTER:
                return new RouterVertex(node);

            default:
                throw new IllegalArgumentException("action not defined for creating new vertex " + action);
        }
    }

    /**
     * creates Edge object depending on user selection in TaskPane
     *
     * @param action        what users selects in TaskPane
     * @param dialogHandler handler for creating dialogs (EdgeConfigurationDialog will be shown in this method)
     * @return new Edge object (not TOpologyEdge)
     */
    public static Edge createEdge(PaletteActionEnum action, DialogHandler dialogHandler, NetworkNode node1, NetworkNode node2) {                
        if (action == null) {
            throw new IllegalArgumentException("TaskPaneAction is NULL");
        }
        Edge edge = null;
        switch (action) {
            case NEW_EDGE_ETHERNET:
                edge = new Edge(10485760, node1, node2);
                break;
            case NEW_EDGE_FAST_ETHERNET:
                edge = new Edge(104857600, node1, node2);
                break;
            case NEW_EDGE_GIGA_ETHERNET:
                edge = new Edge(1048576000, node1, node2);

                break;
            case NEW_EDGE_CUSTOM:
                edge = new Edge(node1, node2);
                break;
            default:
                throw new IllegalArgumentException("action not defined for creating new edge " + action);
        }

        return dialogHandler.showEdgeConfigurationDialog(edge);
    }
}
