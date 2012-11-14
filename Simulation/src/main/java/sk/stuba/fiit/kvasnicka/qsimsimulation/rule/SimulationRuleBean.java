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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.qsimsimulation.rule;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.packet.PacketDeliveredEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.packet.PacketDeliveredListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ping.PingPacketDeliveredEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ping.PingPacketDeliveredListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.RoutingException;

import javax.swing.event.EventListenerList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * this bean represents user definition of a simulation rule
 * this bean has got equals/hash code properly overridden, so it can be a key in hash map/set
 *
 * @author Igor Kvasnicka
 */
@Getter
@EqualsAndHashCode(of = "uniqueID")
public class SimulationRuleBean {

    private final String uniqueID = UUID.randomUUID().toString();

    @Setter
    private boolean active;
    private NetworkNode source;
    private NetworkNode destination;
    /**
     * simulation time when this rule became active
     */
    @Setter
    private double activationTime; //(msec)
    /**
     * number of packets yet to create - this field changes
     *
     * @see #decreaseNumberOfPackets()
     */
    private int numberOfPackets;
    private int packetSize;

    /**
     * here is a "routing table" for this simulation rule
     * this means, that multiple simulation rules may have contradictory routing,
     * e.g: A-B-C-D and A-X-C-Y-D (the same source, destination and one network node in the middle)
     * this also allows to create new simulation rules at runtime (e.g. "ping") without affecting other routing tables
     */
    private Map<NetworkNode, NetworkNode> routes;//key=current network node; value=next hop network node
    private Layer4TypeEnum layer4Type;
    @Getter
    private List<NetworkNode> route;
    @Getter
    private String name;
    @Getter
    private IpPrecedence ipPrecedence;
    @Getter
    private int srcPort;
    @Getter
    private int destPort;
    @Getter
    @Setter
    /**
     * this is a flag that is used mainly (only) for ping - ping simulation rule may be active, but when a packet did not finish its round-trip, no new packet
     * can be created
     */
    private boolean canCreateNewPacket = true;
    /**
     * list of all who wants to be notified, when packet is delivered
     * especially useful when waiting for a ping to be delivered
     */
    private transient javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

    /**
     * creates new simulation rule
     * all arguments are self-explained
     * <p/>
     * do not forget to set route
     * <p/>
     *
     * @param source
     * @param destination
     * @param numberOfPackets
     * @param packetSize
     * @param activeDelay
     * @param srcPort
     * @param destPort
     * @see #setRoute(java.util.List)
     */
    public SimulationRuleBean(String name, NetworkNode source, NetworkNode destination, int numberOfPackets, int packetSize, double activeDelay, Layer4TypeEnum layer4Type, IpPrecedence ipPrecedence, int srcPort, int destPort) {
        this.name = name;
        this.activationTime = activeDelay;
        this.layer4Type = layer4Type;
        this.ipPrecedence = ipPrecedence;
        this.srcPort = srcPort;
        this.destPort = destPort;
        this.active = false;
        this.source = source;
        this.destination = destination;
        this.numberOfPackets = numberOfPackets;
        this.packetSize = packetSize;
        routes = new HashMap<NetworkNode, NetworkNode>();
    }

    /**
     * determines, if it is a ping simulation rule
     *
     * @return
     */
    public boolean isPing() {
        return layer4Type == Layer4TypeEnum.ICMP;
    }

    public void decreaseNumberOfPackets() {
        if (numberOfPackets == - 1) return; //-1 is infinity
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
        return numberOfPackets == 0;
    }

    /**
     * forces simulation rule to send on 1 packet
     */
    public void resetNumberOfPacketsToOne() {
        if (numberOfPackets == - 1) return;
        numberOfPackets = 1;
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
    public void setRoute(List<NetworkNode> route) {
        if (route == null) throw new IllegalArgumentException("route is NULL");
        if (route.size() < 2) {
            throw new IllegalArgumentException("route must consist of at least 2 network nodes: source and destination; this route is long: " + route.size());
        }
        this.route = route;

        routes.clear();

        //routes from node1 to node2....
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
    public NetworkNode getPreviousHopFromRoutingTable(NetworkNode currentNode) throws RoutingException {
        if (! routes.containsValue(currentNode)) {
            throw new RoutingException("cannot find previous network node for: " + currentNode);
        }
        for (NetworkNode node : routes.keySet()) {
            if (routes.get(node).equals(currentNode)) {//this is the route I came here
                return node;
            }
        }
        throw new IllegalStateException("node I am looking for is in the routing table, but I cannot find it: " + currentNode);
    }

    public void addPingPacketDeliveredListener(PingPacketDeliveredListener listener) {
        listenerList.add(PingPacketDeliveredListener.class, listener);
    }

    public void removePingPacketDeliveredListener(PingPacketDeliveredListener listener) {
        listenerList.remove(PingPacketDeliveredListener.class, listener);
    }

    public void firePingPacketDeliveredEvent(PingPacketDeliveredEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i].equals(PingPacketDeliveredListener.class)) {
                ((PingPacketDeliveredListener) listeners[i + 1]).pingPacketDeliveredOccurred(evt);
            }
        }
    }

    public void addPacketDeliveredListener(PacketDeliveredListener listener) {
        listenerList.add(PacketDeliveredListener.class, listener);
    }

    public void removePacketDeliveredListener(PacketDeliveredListener listener) {
        listenerList.remove(PacketDeliveredListener.class, listener);
    }

    public void firePacketDeliveredEvent(PacketDeliveredEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i].equals(PacketDeliveredListener.class)) {
                ((PacketDeliveredListener) listeners[i + 1]).packetDeliveredOccurred(evt);
            }
        }
    }


    /**
     * removes all delivery listeners
     */
    public void removeAllDeliveryListeners() {
        listenerList = new EventListenerList(); //listenerList cannot remove all listeners, so I simply create a new object instead...
    }
}
