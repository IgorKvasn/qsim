/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.serialisation;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.AbstractGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.graphml.*;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import lombok.Getter;
import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;
import org.openide.util.Exceptions;
import sk.stuba.fiit.kvasnicka.topologyvisual.data.Edge;
import sk.stuba.fiit.kvasnicka.topologyvisual.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.TopologyVertexFactory;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageType;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.dto.EdgeDTO;

/**
 *
 * @author Igor Kvasnicka
 */
public class SerialisationHelper {

    private static Logger logg = Logger.getLogger(SerialisationHelper.class);
    /**
     * used to temporary store vertex position vertex position can be set after
     * Layout object is created and Layout is created after Jung file is loaded
     * - a kind of magic circle
     */
    private static Map<TopologyVertex, Point2D> vertexLocationMap = new HashMap<TopologyVertex, Point2D>();
    /**
     * key = vertex name <br> value = NetworkNode object loaded from DataModel
     * file
     */
    private static Map<String, NetworkNode> networkNodeVertexMap = new HashMap<String, NetworkNode>();
    private static Map<EdgeDescriptor, Edge> edgeMap = new HashMap<EdgeDescriptor, Edge>();

    public static DeserialisationResult loadSettings(File file) throws GraphIOException, IOException, JAXBException {
        if (file == null) {
            return null;
        }
        String fileString = readFileAsString(file);
        //first I need to load information about vertices and edges
        int beginData = fileString.indexOf("<xmlSerializationProxy>");
        int endData = fileString.indexOf("</xmlSerializationProxy>");
        if (beginData < 0 || endData < 0) {
            logg.error("File is corrupted - could not find data model.");
            return null;
        }
        String dataSettings = new String(fileString.substring(beginData, endData + "</xmlSerializationProxy>".length()));
        logg.debug("data model string created ");
        loadJaxb(dataSettings);
        logg.debug("data model loaded");

        //now I can load information about graph itself (JUNG data)
        int beginGraph = fileString.indexOf("<graphml");
        int endGraph = fileString.indexOf("</graphml>");
        if (beginGraph < 0 || endGraph < 0) {
            logg.error("File is corrupted - could not find JUNG data.");
          return null;
        }

        String jungSettings = new String(fileString.substring(beginGraph, endGraph + "</graphml>".length()));
        logg.debug("jung model string created ");
        return loadJung(jungSettings);
    }

    private static String readFileAsString(File file) throws java.io.IOException {
        byte[] buffer = new byte[(int) file.length()];
        BufferedInputStream f = null;
        try {
            f = new BufferedInputStream(new FileInputStream(file));
            f.read(buffer);
        } finally {
            if (f != null) {
                try {
                    f.close();
                } catch (IOException ignored) {
                }
            }
        }
        return new String(buffer);
    }

    private static void loadJaxb(String jaxbString) throws JAXBException, FileNotFoundException {
        JAXBContext context = JAXBContext.newInstance(XmlSerializationProxy.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        XmlSerializationProxy serProxy = (XmlSerializationProxy) unmarshaller.unmarshal(new StringReader(jaxbString));
        if (serProxy.getNumberOfVertices() != 0) {
            for (NetworkNode n : serProxy.getVertices()) {
                if (networkNodeVertexMap.containsKey(n.getName())) {
                    throw new IllegalStateException("Duplicate vertex name: " + n.getName());
                }
                networkNodeVertexMap.put(n.getName(), n);
            }
        }

        if (serProxy.getNumberOfEdges() != 0) {
            for (EdgeDTO eDTO : serProxy.getEdges()) {
                NetworkNode n1 = findVertexNetworkNode(eDTO.getNode1());
                NetworkNode n2 = findVertexNetworkNode(eDTO.getNode2());
                Edge e = new Edge(eDTO.getSpeed(), n1, n2);
                e.setLength(eDTO.getLength());
                EdgeDescriptor descr = new EdgeDescriptor(e.getNode1().getName(), e.getNode2().getName());
                if (edgeMap.containsKey(descr)) {
                    throw new IllegalStateException("Duplicate edge between: " + e.getNode1().getName() + " and " + e.getNode2().getName());
                }
                edgeMap.put(descr, e);
            }
        }

    }

    private static DeserialisationResult loadJung(String s) throws FileNotFoundException {
        final TopologyVertexFactory vFactory = new TopologyVertexFactory();
        Reader reader = new StringReader(s);

        Transformer<NodeMetadata, TopologyVertex> vtrans = new Transformer<NodeMetadata, TopologyVertex>() {

            @Override
            public TopologyVertex transform(NodeMetadata nmd) {
                String imageT = nmd.getProperty("imageType");
                String vName = nmd.getProperty("vertex_name");
                ImageType type = ImageType.valueOf(imageT);
                NetworkNode dataModel = findVertexNetworkNode(vName);
                //toto vracia null, lebo Topcomponent neexistuje a teda nie je selectnuty
                TopologyVertex v = vFactory.createVertex(type, dataModel);
                vertexLocationMap.put(v, new Point.Double(Double.parseDouble(nmd.getProperty("x")), Double.parseDouble(nmd.getProperty("y"))));
                return v;
            }
        };
        Transformer<EdgeMetadata, TopologyEdge> etrans = new Transformer<EdgeMetadata, TopologyEdge>() {

            @Override
            public TopologyEdge transform(EdgeMetadata emd) {
                String v1_name = emd.getProperty("vertex1_name");
                String v2_name = emd.getProperty("vertex2_name");

                TopologyEdge e = new TopologyEdge(findEdge(v1_name, v2_name));
                return e;
            }
        };
        Transformer<HyperEdgeMetadata, TopologyEdge> hetrans = new Transformer<HyperEdgeMetadata, TopologyEdge>() {

            @Override
            public TopologyEdge transform(HyperEdgeMetadata emd) {
                String v1_name = emd.getProperty("vertex1_name");
                String v2_name = emd.getProperty("vertex2_name");

                TopologyEdge e = new TopologyEdge(findEdge(v1_name, v2_name));
                return e;
            }
        };
        Transformer<GraphMetadata, UndirectedSparseGraph<TopologyVertex, TopologyEdge>> gtrans = new Transformer<GraphMetadata, UndirectedSparseGraph<TopologyVertex, TopologyEdge>>() {

            @Override
            public UndirectedSparseGraph<TopologyVertex, TopologyEdge> transform(GraphMetadata gmd) {
                return new UndirectedSparseGraph<TopologyVertex, TopologyEdge>();
            }
        };


        GraphMLReader2<UndirectedSparseGraph<TopologyVertex, TopologyEdge>, TopologyVertex, TopologyEdge> gmlr =
                new GraphMLReader2<UndirectedSparseGraph<TopologyVertex, TopologyEdge>, TopologyVertex, TopologyEdge>(
                reader,
                gtrans,
                vtrans,
                etrans,
                hetrans);
        UndirectedSparseGraph<TopologyVertex, TopologyEdge> readGraph;
        try {
            readGraph = gmlr.readGraph();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);

            return null;
        }
        logg.debug("jung loaded");
        return new DeserialisationResult(readGraph, vFactory);

    }

    private static NetworkNode findVertexNetworkNode(String name) {
        if (!networkNodeVertexMap.containsKey(name)) {
            throw new IllegalStateException("Could not find vetex with name: " + name + ". File being loaded is corrupted.");
        }
        return networkNodeVertexMap.get(name);
    }

    private static Edge findEdge(String vertex1, String vertex2) {
        EdgeDescriptor desc = new EdgeDescriptor(vertex1, vertex2);
        if (edgeMap.containsKey(desc)) {
            return edgeMap.get(desc);
        }
        throw new IllegalStateException("Could not find edge between vertices: " + vertex1 + " and " + vertex2 + ". File being loaded is corrupted.");
    }

    @Getter
    public static class DeserialisationResult {

        private AbstractGraph<TopologyVertex, TopologyEdge> g;
        private AbstractLayout<TopologyVertex, TopologyEdge> layout;
        private TopologyVertexFactory vFactory;

        public DeserialisationResult(AbstractGraph<TopologyVertex, TopologyEdge> g, TopologyVertexFactory vFactory) {
            this.vFactory = vFactory;
            this.g = g;
            layout = new StaticLayout<TopologyVertex, TopologyEdge>(g);

            for (TopologyVertex v : vertexLocationMap.keySet()) {
                layout.setLocation(v, vertexLocationMap.get(v));
            }

            normalizeEdges(g.getEdges());
        }

        /**
         * when TopologyEdge was created, special constructor was used. how it
         * is time to init fields that has been forgotten - vertex1 and vertex2
         *
         * @param edges
         */
        private void normalizeEdges(Collection<TopologyEdge> edges) {
            for (TopologyEdge edge : edges) {
                String v1 = edge.getEdge().getNode1().getName();
                String v2 = edge.getEdge().getNode2().getName();

                edge.setVertices(findTopologyVertex(v1), findTopologyVertex(v2));
            }
        }

        private TopologyVertex findTopologyVertex(String name) {
            Collection<TopologyVertex> vertices = g.getVertices();
            for (TopologyVertex v : vertices) {
                if (v.getName().equals(name)) {
                    return v;
                }
            }
            throw new IllegalStateException("Unknown vertex with name: " + name);
        }
    }

    private static class EdgeDescriptor {

        private String vertex1, vertex2;

        public EdgeDescriptor(String vertex1, String vertex2) {
            this.vertex1 = vertex1;
            this.vertex2 = vertex2;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final EdgeDescriptor other = (EdgeDescriptor) obj;
            if ((this.vertex1 == null) ? (other.vertex1 != null) : !this.vertex1.equals(other.vertex1)) {
                return false;
            }
            if ((this.vertex2 == null) ? (other.vertex2 != null) : !this.vertex2.equals(other.vertex2)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 41 * hash + (this.vertex1 != null ? this.vertex1.hashCode() : 0);
            hash = 41 * hash + (this.vertex2 != null ? this.vertex2.hashCode() : 0);
            return hash;
        }
    }
}
