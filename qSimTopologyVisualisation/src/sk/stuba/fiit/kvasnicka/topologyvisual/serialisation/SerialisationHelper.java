/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.serialisation;

import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.graphml.*;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;
import org.openide.util.Exceptions;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.TopologyVertexFactory;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageType;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.DeserialisationResult.EdgeDescriptor;
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
    private Map<TopologyVertex, Point2D> vertexLocationMap = new HashMap<TopologyVertex, Point2D>();
    /**
     * key = vertex name <br> value = NetworkNode object loaded from DataModel
     * file
     */
    private Map<String, NetworkNode> networkNodeVertexMap = new HashMap<String, NetworkNode>();
    private Map<EdgeDescriptor, Edge> edgeMap = new HashMap<EdgeDescriptor, Edge>();
    private String topologyName, topologyDescription;

    /**
     * reads file being loaded
     *
     * @param file
     * @return
     * @throws GraphIOException
     * @throws IOException
     * @throws JAXBException
     */
    public DeserialisationResult loadSettings(File file) throws GraphIOException, IOException, JAXBException {
        if (file == null) {
            return null;
        }
        String fileString = readFileAsString(file);
        //first I need to load information about vertices and edges
        int beginData = fileString.indexOf("<xmlSerializationProxy>");
        int endData = fileString.indexOf("</xmlSerializationProxy>");
        if (beginData < 0 || endData < 0) { //data model MUST be present either file contains JUNG graph or not
            logg.error("File is corrupted - could not find data model.");
            return null;
        }
        String dataSettings = new String(fileString.substring(beginData, endData + "</xmlSerializationProxy>".length()));
        loadJaxb(dataSettings);

        //now I can load information about graph itself (JUNG data)
        int beginGraph = fileString.indexOf("<graphml");
        int endGraph = fileString.indexOf("</graphml>");
        String jungSettings = "";
        if (beginGraph < 0 || endGraph < 0) {
            logg.info("could not find JUNG data.");
        } else {
            jungSettings = new String(fileString.substring(beginGraph, endGraph + "</graphml>".length()));
        }
        return loadJung(jungSettings);
    }

    private String readFileAsString(File file) throws java.io.IOException {
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

    private void loadJaxb(String jaxbString) throws JAXBException, FileNotFoundException {
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
        topologyName = serProxy.getTopologyName();
        topologyDescription = serProxy.getTopologyDescription();
    }

    private DeserialisationResult loadJung(String s) throws FileNotFoundException {
        if (s.isEmpty()) {//no JUNG xml to be deserialised
            logg.debug("no JUNG data found = no topology graph - it is OK");
            return new DeserialisationResult(topologyName, topologyDescription);
        }
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
        return new DeserialisationResult(readGraph, vFactory, vertexLocationMap, topologyName, topologyDescription);
    }

    private NetworkNode findVertexNetworkNode(String name) {
        if (!networkNodeVertexMap.containsKey(name)) {
            throw new IllegalStateException("Could not find vetex with name: " + name + ". File being loaded is corrupted.");
        }
        return networkNodeVertexMap.get(name);
    }

    private Edge findEdge(String vertex1, String vertex2) {
        EdgeDescriptor desc = new EdgeDescriptor(vertex1, vertex2);
        if (edgeMap.containsKey(desc)) {
            return edgeMap.get(desc);
        }
        throw new IllegalStateException("Could not find edge between vertices: " + vertex1 + " and " + vertex2 + ". File being loaded is corrupted.");
    }
}
