/*******************************************************************************
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
 ******************************************************************************/

package sk.stuba.fiit.kvasnicka.qsimsimulation.managers;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

/**
 * this class manages edges and nodes in the topology - it does not remove or add new edges/nodes
 * it only knows about all edges/nodes and help during routing process, etc.
 *
 * @author Igor Kvasnicka
 */
public class TopologyManager implements Serializable {

    private List<Edge> edgeList;
    private List<NetworkNode> nodeList;

    private transient WeakHashMap<CacheKey, Edge> edgeCache;


    public TopologyManager(List<Edge> edgeList, List<NetworkNode> nodeList) {
        this.nodeList = Collections.unmodifiableList(nodeList);
        this.edgeList = Collections.unmodifiableList(edgeList);

        edgeCache = new WeakHashMap<CacheKey, Edge>();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        edgeCache = new WeakHashMap<CacheKey, Edge>();
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

        CacheKey cacheKey1 = new CacheKey(node1, node2);
        if (edgeCache.containsKey(cacheKey1)) {
            return edgeCache.get(cacheKey1);
        }

        CacheKey cacheKey2 = new CacheKey(node2, node1);//different order of the nodes
        if (edgeCache.containsKey(cacheKey2)) {
            return edgeCache.get(cacheKey2);
        }

        for (Edge edge : edgeList) {
            if (edge.getNode1().getName().equals(node1) && edge.getNode2().getName().equals(node2)) {
                edgeCache.put(cacheKey1, edge);
                return edge;
            }
            if (edge.getNode2().getName().equals(node1) && edge.getNode1().getName().equals(node2)) {
                edgeCache.put(cacheKey1, edge);
                return edge;
            }
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

    @EqualsAndHashCode(of = {"node1", "node2"})
    private static final class CacheKey {
        private String node1, node2;

        private CacheKey(String node1, String node2) {
            this.node1 = node1;
            this.node2 = node2;
        }
    }
}
