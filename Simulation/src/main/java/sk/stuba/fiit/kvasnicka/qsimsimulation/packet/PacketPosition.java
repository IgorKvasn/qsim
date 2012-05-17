package sk.stuba.fiit.kvasnicka.qsimsimulation.packet;

import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketStateEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;

/**
 * this class specifies, where is packet placed - on the wire (Edge object) or in the NetworkNode
 *
 * @author Igor Kvasnicka
 */
@Getter
@Deprecated
public class PacketPosition {
    private Edge edge;
    private NetworkNode node;
    private NetworkNode edgeNodeFrom;//start of the edge
    private PacketStateEnum state;
    private final Packet packet;

    public PacketPosition(NetworkNode sourceNode, PacketStateEnum state, Packet packet) {
        this.node = sourceNode;
        this.state = state;
        this.packet = packet;
        this.edgeNodeFrom = sourceNode;
    }


    public boolean isOnTheWire() {
        return edge != null;
    }

    /**
     * returns the second end of the edge
     *
     * @param edge edge itself
     * @param node this is one end of the edge
     * @return the other end of the edge
     */
    private NetworkNode getOtherEndOfEdge(Edge edge, NetworkNode node) {
        if (edge == null) {
            throw new IllegalArgumentException("topolEdge is NULL");
        }
        if (node == null) {
            throw new IllegalArgumentException("node is NULL");
        }
        if (edge.getNode1().equals(node)) {
            return edge.getNode2();
        }
        if (edge.getNode2().equals(node)) {
            return edge.getNode1();
        }
        throw new IllegalStateException("Specified node " + node.getName() + " is not part of the edge: " + edge.getNode1() + "<->" + edge.getNode2());
    }


    private Edge getEdgeByRouting(NetworkNode currentNode, Packet packet, TopologyManager topologyManager) {
        if (! currentNode.containsRoute(packet.getDestination().getName())) {
            throw new IllegalStateException("Invalid routing rule - unable to route packet to destination: " + packet.getDestination() + " from node: " + currentNode);
        }

        String nextHop = currentNode.getRoutes().get(packet.getDestination().getName());
        return topologyManager.findEdge(nextHop, currentNode.getName());
    }
}