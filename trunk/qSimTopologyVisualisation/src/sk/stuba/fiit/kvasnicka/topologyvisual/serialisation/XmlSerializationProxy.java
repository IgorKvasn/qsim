/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.serialisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.TopologyCreation;
import sk.stuba.fiit.kvasnicka.topologyvisual.data.Edge;
import sk.stuba.fiit.kvasnicka.topologyvisual.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.dto.EdgeDTO;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.transform.EdgeTransform;

/**
 * Serialisation proxy pattern. two main things are being serialised by this
 * proxy: <p>1. </br>vertices - TopologyVertex and underlying NetworkNode
 * </br>edges - TopologyEdge and underlying Edge</p><p> 2. settings (mainly
 * routing settings - I doubt there will be something more in the future, but
 * who knows? I don't.)</p><p>JUNG vertices will be serialised separately using
 * JUNGs build-in serialisation capabilities</p>
 *
 * @author Igor Kvasnicka
 */
@Getter
@Setter
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlSerializationProxy {

    private int numberOfVertices;
    private ArrayList<NetworkNode> vertices;
    private int numberOfEdges;
    private ArrayList<EdgeDTO> edges;

    /**
     * used when deserialisation process
     */
    public XmlSerializationProxy() {
    }

    /**
     * used when serialising
     *
     * @param topology
     */
    public XmlSerializationProxy(TopologyCreation topology) {
        if (topology == null) {
            throw new IllegalArgumentException("topology is NULL");
        }
        prepareJungProxy();
    }

    private void prepareJungProxy() {
        this.vertices = getNetworkNodes();
        this.numberOfVertices = vertices.size();

        this.edges = getDataModelEdges();
        this.numberOfEdges = edges.size();

    }

    private ArrayList<NetworkNode> getNetworkNodes() {
        List<TopologyVertex> allVertices = NetbeansWindowHelper.getInstance().getActiveTopComponentTopology().getVertexFactory().getAllVertices();
        ArrayList<NetworkNode> list = new ArrayList<NetworkNode>(allVertices.size());
        for (TopologyVertex v : allVertices) {
            list.add(v.getDataModel());
        }
        return list;
    }

    private ArrayList<EdgeDTO> getDataModelEdges() {
        Collection<TopologyEdge> edgesCol = NetbeansWindowHelper.getInstance().getActiveTopComponentTopology().getGraph().getEdges();

        ArrayList<EdgeDTO> list = new ArrayList<EdgeDTO>(edgesCol.size());
        for (TopologyEdge e : edgesCol) {
            list.add(EdgeTransform.transformToDTO(e.getEdge()));
        }
        return list;
    }
}
