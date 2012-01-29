/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.serialisation;

import edu.uci.ics.jung.graph.Graph;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.Topology;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.topologyvisual.PreferenciesHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.TopologyVertexFactory;
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

    @XmlTransient
    private static final Logger logg = Logger.getLogger(XmlSerializationProxy.class);
    private int numberOfVertices;
    private ArrayList<NetworkNode> vertices;
    private int numberOfEdges;
    private ArrayList<EdgeDTO> edges;
    private String topologyName, topologyDescription;
    private Boolean distanceVectorRouting;

    /**
     * initializes serialisation proxy - fills it with actual data that are
     * about to be saved
     */
    public void prepareProxy(TopologyVertexFactory vertexFactory, Graph topologyGraph, String name, String description, boolean distanceVectorRouting) {
        //saving JUNG topology
        if (vertexFactory != null && topologyGraph != null) {
            this.vertices = getNetworkNodes(vertexFactory);
            this.numberOfVertices = vertices.size();

            this.edges = getDataModelEdges(topologyGraph);
            this.numberOfEdges = edges.size();
        }

        //saving topology information
        this.topologyName = name;
        this.topologyDescription = description;
        this.distanceVectorRouting = distanceVectorRouting;
    }

    public boolean isDistanceVectorRouting() {
        if (distanceVectorRouting == null) {
            logg.debug("distance vector elemtent not found in XML - using default as user told me so: " + PreferenciesHelper.isRoutingDistanceProtocol());
            distanceVectorRouting = PreferenciesHelper.isRoutingDistanceProtocol();
        }
        return distanceVectorRouting.booleanValue();
    }

    private ArrayList<NetworkNode> getNetworkNodes(TopologyVertexFactory vertexFactory) {
        List<TopologyVertex> allVertices = vertexFactory.getAllVertices();
        ArrayList<NetworkNode> list = new ArrayList<NetworkNode>(allVertices.size());
        for (TopologyVertex v : allVertices) {
            list.add(v.getDataModel());
        }
        return list;
    }

    private ArrayList<EdgeDTO> getDataModelEdges(Graph topologyGraph) {
        Collection<TopologyEdge> edgesCol = topologyGraph.getEdges();

        ArrayList<EdgeDTO> list = new ArrayList<EdgeDTO>(edgesCol.size());
        for (TopologyEdge e : edgesCol) {
            list.add(EdgeTransform.transformToDTO(e.getEdge()));
        }
        return list;
    }
}
