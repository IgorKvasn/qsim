/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.qsimsimulation;

import lombok.Getter;
import lombok.Setter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * this bean represents user definition of a simulation rule
 *
 * @author Igor Kvasnicka
 */
@Getter
public class SimulationRuleBean {
    @Setter
    private boolean active;
    private int repeat;//-1 if infinity;
    private PacketTypeEnum packetTypeEnum;
    private NetworkNode source;
    private NetworkNode destination;
    /**
     * simulation time when this rule became active
     */
    private double activationTime; //(msec)
    /**
     * number of packets yet to create - this field changes
     *
     * @see #decreaseNumberOfPackets()
     */
    private int numberOfPackets;
    private int packetSize;

    /**
     * true if this rule is automatic = it starts when simulation starts
     * manual (non-automatic) rules starts when user says so
     */
    private boolean automatic;
    /**
     * here is a "routing table" for this simulation rule
     * this means, that multiple simulation rules may have contradictory routing,
     * e.g: A-B-C-D and A-X-C-Y-D (the same source, destination and one network node in the middle)
     * this also allows to create new simulation rules at runtime (e.g. "ping") without affecting other routing tables
     */
    private Map<NetworkNode, NetworkNode> routes;//key=current network node; value=next hop network node
    private Layer4TypeEnum layer4Type;
    private boolean ping;

    /**
     * creates new simulation rule
     * all arguments are self-explained
     *
     * @param source
     * @param destination
     * @param numberOfPackets
     * @param packetSize
     * @param automatic
     * @param activeDelay
     * @param repeat          -1 if infinity
     * @param packetTypeEnum
     */
    public SimulationRuleBean(NetworkNode source, NetworkNode destination, int numberOfPackets, int packetSize, boolean automatic, double activeDelay, int repeat, PacketTypeEnum packetTypeEnum, Layer4TypeEnum layer4Type, boolean ping) {
        this.activationTime = activeDelay;
        this.repeat = repeat;
        this.packetTypeEnum = packetTypeEnum;
        this.layer4Type = layer4Type;
        this.ping = ping;
        this.active = false;
        this.source = source;
        this.destination = destination;
        this.numberOfPackets = numberOfPackets;
        this.packetSize = packetSize;
        this.automatic = automatic;
        routes = new HashMap<NetworkNode, NetworkNode>();
    }

    /**
     * creates new simulation rule
     * all arguments are self-explained
     *
     * @param source
     * @param destination
     * @param numberOfPackets
     * @param packetSize
     * @param automatic
     * @param activeDelay
     * @param repeat          -1 if infinity
     * @param packetTypeEnum
     */
    public SimulationRuleBean(NetworkNode source, NetworkNode destination, int numberOfPackets, int packetSize, boolean automatic, double activeDelay, int repeat, PacketTypeEnum packetTypeEnum, Layer4TypeEnum layer4Type) {
        this.activationTime = activeDelay;
        this.repeat = repeat;
        this.packetTypeEnum = packetTypeEnum;
        this.layer4Type = layer4Type;
        this.ping = false;
        this.active = false;
        this.source = source;
        this.destination = destination;
        this.numberOfPackets = numberOfPackets;
        this.packetSize = packetSize;
        this.automatic = automatic;
        routes = new HashMap<NetworkNode, NetworkNode>();
    }

    /**
     * each rule has set number of repetitions. this method assures that rule is used exact number of times
     * it decreases repeat number of repeat count - when it reaches 0 simulation rule is set to "finished"
     */
    public void decreaseRuleRepetition() {
        if (repeat <= 0) return;//so when rule is finished (repeat=0) or if it is set to infinity (repeat=-1)
        repeat--;
    }

    public void decreaseNumberOfPackets() {
        numberOfPackets--;
    }

    /**
     * sometimes simulation rule needs more time quantums to be finished
     *
     * @param timeQuantum
     */
    public void increaseActivationTime(double timeQuantum) {
        activationTime += timeQuantum;
    }

    public boolean isFinished() {
        return repeat == 0 && numberOfPackets == 0;
    }


    /**
     * clears the routing table however directly connected routes MUST be
     * persisted
     */
    public void clearRoutingTable() {
        routes.clear();
    }


    /**
     * adds new routing rule (new route) to routing table
     * route must consist of at least 2 network nodes: source and destination
     *
     * @param route list of network nodes in which packet will be routed
     */
    public void addRoute(List<NetworkNode> route) {
        if (route == null) throw new IllegalArgumentException("route is NULL");
        if (route.size() < 2) {
            throw new IllegalArgumentException("route must consist of at least 2 network nodes: source and destination; this route is long: " + route.size());
        }

        for (int i = 0; i < route.size() - 1; i++) {
            routes.put(route.get(i), route.get(i + 1));
        }
    }

    public NetworkNode getNextHopFromRoutingTable(NetworkNode currentNode) {
        if (! routes.containsKey(currentNode)) {
            throw new IllegalStateException("cannot find route for destination: " + getDestination() + " from " + currentNode);
        }
        return routes.get(currentNode);
    }

    /**
     * finds network node that was previous hop
     *
     * @param currentNode network node, where packet is currently placed
     * @return previous network node
     */
    public NetworkNode getPreviousHopFromRoutingTable(NetworkNode currentNode) {
        if (! routes.containsValue(currentNode)) {
            throw new IllegalStateException("cannot find previous network node for: " + currentNode);
        }
        for (NetworkNode node : routes.keySet()) {
            if (routes.get(node).equals(currentNode)) {//this is the route I came here
                return node;
            }
        }
        throw new IllegalStateException("node I am looking for is in the routing table, but I cannot find it: " + currentNode);
    }
}
