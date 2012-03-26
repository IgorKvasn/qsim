package sk.stuba.fiit.kvasnicka.qsimsimulation.managers;

import org.apache.commons.lang.StringUtils;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;

import java.util.Collections;
import java.util.List;

/**
 * this class manages edges and nodes in the topology - it does not remove or add new edges/nodes
 * it only knows about all edges/nodes and help during routing process, etc.
 *
 * @author Igor Kvasnicka
 */
public class TopologyManager {
    private final List<Edge> edgeList;
    private final List<NetworkNode> nodeList;

    public TopologyManager(List<Edge> edgeList, List<NetworkNode> nodeList) {
        this.nodeList = Collections.unmodifiableList(nodeList);
        this.edgeList = Collections.unmodifiableList(edgeList);
    }

    /**
     * finds edge between two NetworkNodes.
     * the order of given nodes is not important: edge A-B is the same as B-A
     *
     * @param node1 name of the first node
     * @param node2 name of the second node
     * @return edge between them
     * @throws IllegalStateException if no such edge exists
     */
    public Edge findEdge(String node1, String node2) {
        if (StringUtils.isEmpty(node1)) {
            throw new IllegalArgumentException("node1 cannot be null or empty");
        }
        if (StringUtils.isEmpty(node2)) {
            throw new IllegalArgumentException("node2 cannot be null or empty");
        }
        for (Edge edge : edgeList) {
            if (edge.getNode1().getName().equals(node1) && edge.getNode2().getName().equals(node2)) return edge;
            if (edge.getNode2().getName().equals(node1) && edge.getNode1().getName().equals(node2)) return edge;
        }
        throw new IllegalStateException("unknown edge between " + node1 + " <-> " + node2);
    }

    /**
     * finds NetworkNode according to its name
     *
     * @param name
     * @return
     */
    public NetworkNode findNetworkNode(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        for (NetworkNode node : getNodeList()) {
            if (node.getName().equals(name)) return node;
        }

        throw new IllegalStateException("Network node not found with name: " + name);
    }

    /**
     * returns unmodifiable list of edges
     *
     * @return list of all edges
     */
    public List<Edge> getEdgeList() {
        return edgeList;
    }

    /**
     * returns unmodifiable list of network nodes
     *
     * @return list of all nodes
     */
    public List<NetworkNode> getNodeList() {
        return nodeList;
    }


    public double getSerialisationDelay(NetworkNode source, NetworkNode destination, int packetSize) {
        Edge edge = findEdge(source.getName(), destination.getName());
        return DelayHelper.calculateSerialisationDelay(edge, packetSize);
    }
}
