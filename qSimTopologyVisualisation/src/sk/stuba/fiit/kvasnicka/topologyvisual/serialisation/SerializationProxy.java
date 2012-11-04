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
package sk.stuba.fiit.kvasnicka.topologyvisual.serialisation;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.graph.AbstractGraph;
import edu.uci.ics.jung.graph.Graph;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.topologyvisual.PreferenciesHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.TopologyVertexFactory;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.dto.TopologyVertexSerialization;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.transformation.TopologyVertexToVertexXmlTransformation;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.SimulationData;

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
public class SerializationProxy implements Serializable {

    private static final long serialVersionUID = -403250971215465050L;
    private static final transient Logger logg = Logger.getLogger(SerializationProxy.class);
    private ArrayList<Edge> edges;
    private String topologyName, topologyDescription;
    private Boolean distanceVectorRouting;
    private ArrayList<TopologyVertexSerialization> vertices;
    private ArrayList<SimulationData.Data> simulRulesData;

    public static SerializationProxy serializeFromString(String s) throws IOException, ClassNotFoundException {
        byte[] data = Base64.decodeBase64(s);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return (SerializationProxy) o;
    }

    public static String serializetoString(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return new String(Base64.encodeBase64String(baos.toByteArray()));
    }

    /**
     * initializes serialisation proxy - fills it with actual data that are
     * about to be saved
     */
    public void prepareProxy(TopologyVertexFactory vertexFactory, AbstractGraph<TopologyVertex, TopologyEdge> graph, AbstractLayout<TopologyVertex, TopologyEdge> layout, String name, String description, boolean distanceVectorRouting, List<SimulationData.Data> simulRulesData) {
        //saving JUNG topology
        if (vertexFactory != null && graph != null) {
            this.vertices = getNetworkNodes(vertexFactory, layout);
            this.edges = getDataModelEdges(graph);
        }

        //saving topology information
        this.topologyName = name;
        this.topologyDescription = description;
        this.distanceVectorRouting = distanceVectorRouting;
        this.simulRulesData = new ArrayList<SimulationData.Data>(simulRulesData);
    }

    public boolean isDistanceVectorRouting() {
        if (distanceVectorRouting == null) {
            logg.debug("distance vector elemtent not found in XML - using default as user told me so: " + PreferenciesHelper.isRoutingDistanceProtocol());
            distanceVectorRouting = PreferenciesHelper.isRoutingDistanceProtocol();
        }
        return distanceVectorRouting.booleanValue();
    }

    private ArrayList<TopologyVertexSerialization> getNetworkNodes(TopologyVertexFactory vertexFactory, AbstractLayout<TopologyVertex, TopologyEdge> layout) {
        List<TopologyVertex> allVertices = vertexFactory.getAllVertices();
        ArrayList<TopologyVertexSerialization> list = new ArrayList<TopologyVertexSerialization>(allVertices.size());
        for (TopologyVertex v : allVertices) {
            list.add(TopologyVertexToVertexXmlTransformation.transformToSerializable(v, layout));
        }
        return list;
    }

    private ArrayList<Edge> getDataModelEdges(Graph topologyGraph) {
        Collection<TopologyEdge> edgesCol = topologyGraph.getEdges();

        ArrayList<Edge> list = new ArrayList<Edge>(edgesCol.size());
        for (TopologyEdge e : edgesCol) {
            list.add(e.getEdge());
        }
        return list;
    }
}
