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
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.AbstractGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.io.GraphIOException;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.TopologyVertexFactory;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.dto.TopologyVertexSerialization;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.transformation.TopologyVertexToVertexXmlTransformation;

/**
 *
 * @author Igor Kvasnicka
 */
public class SerialisationHelper {

    private static Logger logg = Logger.getLogger(SerialisationHelper.class);

    /**
     * reads file being loaded
     *
     * @param file
     * @return
     * @throws GraphIOException
     * @throws IOException
     * @throws JAXBException
     */
    public DeserialisationResult loadSettings(File file) throws GraphIOException, IOException, ClassNotFoundException {
        if (file == null) {
            return null;
        }
        DeserialisationResult result = new DeserialisationResult();
        String fileString = readFileAsString(file);
        SerializationProxy serProxy = SerializationProxy.serializeFromString(fileString);

        List<TopologyVertexSerialization> vertices = serProxy.getVertices();

        AbstractGraph<TopologyVertex, TopologyEdge> g = new UndirectedSparseGraph<TopologyVertex, TopologyEdge>();
        AbstractLayout<TopologyVertex, TopologyEdge> layout = new StaticLayout<TopologyVertex, TopologyEdge>(g);

        List<TopologyVertex> verticesTopology = addVerticesToGraph(vertices, g, layout);
        addEdgesToGraph(serProxy.getEdges(), g, verticesTopology);


        result.setDescription(serProxy.getTopologyDescription());
        result.setName(serProxy.getTopologyName());
        result.setDistanceVectorRouting(serProxy.isDistanceVectorRouting());
        result.setG(g);
        result.setLayout(layout);
        result.setVertexFactory(new TopologyVertexFactory(verticesTopology));
        logg.debug("file loaded: " + file.getAbsolutePath());
        return result;
    }

    private void addEdgesToGraph(List<Edge> edges, AbstractGraph<TopologyVertex, TopologyEdge> g, List<TopologyVertex> vertices) {
        for (Edge e : edges) {
            g.addEdge(new TopologyEdge(e), findVertexByName(vertices, e.getNode1().getName()), findVertexByName(vertices, e.getNode2().getName()));
        }
    }

    private TopologyVertex findVertexByName(List<TopologyVertex> vertices, String name) {
        for (TopologyVertex v : vertices) {
            if (v.getName().equals(name)) {
                return v;
            }
        }
        throw new IllegalStateException("could not find vertex with name: " + name);
    }

    private List<TopologyVertex> addVerticesToGraph(List<TopologyVertexSerialization> vertices, AbstractGraph<TopologyVertex, TopologyEdge> g, AbstractLayout<TopologyVertex, TopologyEdge> layout) {
        List<TopologyVertex> result = new LinkedList<TopologyVertex>();

        for (TopologyVertexSerialization t : vertices) {
            TopologyVertex vertex = TopologyVertexToVertexXmlTransformation.transFormSerializable(t, layout);
            g.addVertex(vertex);
            result.add(vertex);
        }

        return result;
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
}
