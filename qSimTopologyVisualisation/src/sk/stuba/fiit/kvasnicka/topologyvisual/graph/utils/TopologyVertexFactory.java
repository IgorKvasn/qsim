package sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.apache.commons.collections15.Factory;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.gui.TopologyVisualisation;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.commons.TopologyElementFactory;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.ComputerVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.RouterVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.SwitchVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageType;

/**
 * Factory to create new vertices
 *
 * @author Igor Kvasnicka
 */
public class TopologyVertexFactory implements Factory<TopologyVertex> {

    private static Logger logg = Logger.getLogger(TopologyVertexFactory.class);
    private List<RouterVertex> vertexRouterList = new LinkedList<RouterVertex>();
    private List<ComputerVertex> vertexComputerList = new LinkedList<ComputerVertex>();
    private List<SwitchVertex> vertexSwitchList = new LinkedList<SwitchVertex>();
    private TopologyVisualisation topolElementTopComponent;

    /**
     * creates new instance
     *
     * @param mainFrame reference to MainFrame
     */
    public TopologyVertexFactory(TopologyVisualisation topolElementTopComponent) {
        this.topolElementTopComponent = topolElementTopComponent;
    }

    public TopologyVertexFactory() {
    }

    public void setTopolElementTopComponent(TopologyVisualisation topolElementTopComponent) {
        this.topolElementTopComponent = topolElementTopComponent;
    }

    /**
     * true when user wants to create <b>some</b> vertex (I don't know what type
     * of vertex, yet)
     */
    public boolean canCreateVertex() {
        if (topolElementTopComponent.getSelectedAction() == null) {//no action is selected = no topology vertex should be created
            return false;
        }
        return true;
    }

    @Override
    public TopologyVertex create() {
        if (topolElementTopComponent == null) {
            throw new IllegalStateException("topolElementTopComponent is NULL");
        }

        try {
            NetworkNode node;
            switch (topolElementTopComponent.getSelectedAction()) {
                case NEW_VERTEX_ROUTER:
                    node = topolElementTopComponent.getDialogHandler().showRouterConfigurationDialog();
                    break;
                case NEW_VERTEX_PC:
                    node = topolElementTopComponent.getDialogHandler().showComputerConfigurationDialog();
                    break;
                case NEW_VERTEX_SWITCH:
                    node = topolElementTopComponent.getDialogHandler().showSwitchConfigurationDialog();
                    break;

                default:
                    throw new IllegalStateException("undefined action for creating new vertex " + topolElementTopComponent.getSelectedAction());
            }
            TopologyVertex vertex = TopologyElementFactory.createVertex(topolElementTopComponent.getSelectedAction(), node);
            addVertexToList(vertex);
            return vertex;
        } catch (IllegalStateException e) {//user hit cancel button
            logg.debug(e);
            return null;
        } finally {
            topolElementTopComponent.getTopologyElementCreator().cancelAction();
        }
    }

    /**
     * removed vertex from lists of vertices
     *
     * @param vertex
     */
    public void deleteVertex(TopologyVertex vertex) {
        if (vertex == null) {
            return;
        }
        if (vertex instanceof RouterVertex) {
            vertexRouterList.remove((RouterVertex) vertex);
            return;
        }
        if (vertex instanceof ComputerVertex) {
            vertexComputerList.remove((ComputerVertex) vertex);
            return;
        }
        if (vertex instanceof SwitchVertex) {
            vertexSwitchList.remove((SwitchVertex) vertex);
            return;
        }

        throw new IllegalArgumentException("vertex " + vertex + " is unknown type, so cannot be deleted");
    }

    /**
     * returns list of all routers it is guaranteed, that elements are not null
     *
     * @return
     */
    public List<RouterVertex> getVertexRouterList() {
        return vertexRouterList;
    }

    /**
     * returns list of all computers it is guaranteed, that elements are not
     * null
     *
     * @return
     */
    public List<ComputerVertex> getVertexComputerList() {
        return vertexComputerList;
    }

    /**
     * returns list of all switches it is guaranteed, that elements are not null
     *
     * @return
     */
    public List<SwitchVertex> getVertexSwitchList() {
        return vertexSwitchList;
    }

    /**
     * returns list of all vertices sorted by their name it is guaranteed, that
     * elements are not null
     *
     * @return
     */
    public List<TopologyVertex> getAllVertices() {
        ArrayList<TopologyVertex> list = new ArrayList<TopologyVertex>(vertexComputerList.size() + vertexRouterList.size() + vertexSwitchList.size());
        list.addAll(vertexComputerList);
        list.addAll(vertexRouterList);
        list.addAll(vertexSwitchList);

        Collections.sort(list, new VertexComparator());
        return list;
    }

    public boolean isVertexNameUnique(String name) {
        List<TopologyVertex> allVertices = getAllVertices();
        for (TopologyVertex v : allVertices) {
            if (v.getName().equals(name)) {
                return false;
            }
        }
        return true;
    }

    /**
     * creates topology vertex according to its data model representation <br/>
     * this comes handy when loading topology vertices from file
     *
     * @param imageType
     * @param dataModel
     * @return
     */
    public TopologyVertex createVertex(ImageType imageType, NetworkNode dataModel) {
        TopologyVertex v;
        switch (imageType) {
            case TOPOLOGY_VERTEX_COMPUTER:
                v = new ComputerVertex(dataModel);
                break;
            case TOPOLOGY_VERTEX_ROUTER:
                v = new RouterVertex(dataModel);
                break;
            case TOPOLOGY_VERTEX_SWITCH:
                v = new SwitchVertex(dataModel);
                break;
            default:
                throw new IllegalStateException("unknown ImageType during deserialisation: " + imageType);
        }

        addVertexToList(v);
        return v;
    }

    private void addVertexToList(TopologyVertex vertex) {
        if (vertex == null) {
            throw new IllegalArgumentException("vertex is NULL");
        }
        if (vertex instanceof RouterVertex) {
            vertexRouterList.add((RouterVertex) vertex);
        } else if (vertex instanceof ComputerVertex) {
            vertexComputerList.add((ComputerVertex) vertex);
        } else if (vertex instanceof SwitchVertex) {
            vertexSwitchList.add((SwitchVertex) vertex);
        } else {
            throw new IllegalStateException("unknown vertex type");
        }
    }

    private static class VertexComparator implements Comparator<TopologyVertex> {

        @Override
        public int compare(TopologyVertex o1, TopologyVertex o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
}
