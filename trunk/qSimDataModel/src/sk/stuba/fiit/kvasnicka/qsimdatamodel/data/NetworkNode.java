package sk.stuba.fiit.kvasnicka.qsimdatamodel.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * superclass of all nodes in topology (router/computer) all NetworkNodes are
 * "routable" - they all store information about routing
 *
 * @author Igor Kvasnicka
 */
@EqualsAndHashCode(of = {"name"})
@XmlSeeAlso({Router.class, Switch.class, Computer.class})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class NetworkNode implements Serializable{

    @Getter
    @Setter
    private String name;
    private HashMap<String, String> routes;//key=destination network node name; value=next hop network node name    
    /**
     * defines rules for forbidden neigbours key= neigbour NetworkNode object
     * (Router, Computer,..) as class value = maximum connections (links) are
     * possible to create with this neigbour
     */
    @XmlTransient
    protected static Map<Class, Integer> routingRules;

    /**
     * this constructor is used only during deserialisation process
     */
    public NetworkNode() {
        routes = new HashMap<String, String>();
        routingRules = new HashMap<Class, Integer>();
        fillForbiddenRoutingRules(routingRules);
    }

    protected NetworkNode(String name) {
        this();
        this.name = name;

    }

    /**
     * defines forbidden NetworNodes that cannot be neighbours with this
     * NetworkNode e.g. Computer cannot be linked with Computer; Computer can be
     * linked with only 1 Router/Switch; etc. override this method to define a
     * new rule(s) By default there are no rules defined.
     */
    protected void fillForbiddenRoutingRules(Map<Class, Integer> routingRules) {
    }

    /**
     * key=destination network node </p> value=next hop network node </p> to
     * add/remove routes use appropriate methods use this method only to
     * retrieve routing table
     *
     * @return <b>read-only</b> Map of routes
     * @see #addRoute(sk.stuba.fiit.kvasnicka.topologyvisual.data.NetworkNode,
     * sk.stuba.fiit.kvasnicka.topologyvisual.data.NetworkNode) addRoute
     * @see
     * #removeRoute(sk.stuba.fiit.kvasnicka.topologyvisual.data.NetworkNode)
     * removeRoute
     * @see #clearRoutingTable() clearRoutingTable
     * @see
     * #containsRoute(sk.stuba.fiit.kvasnicka.topologyvisual.data.NetworkNode)
     * containsRoute
     * @see #getAllDestinations() getAllDestinations
     * @see
     * #getNextHopFromRoutingTable(sk.stuba.fiit.kvasnicka.topologyvisual.data.NetworkNode)
     * getNextHopFromRoutingTable
     */
    public Map<String, String> getRoutes() {
        return Collections.unmodifiableMap(routes);
    }

    /**
     * returns forbidden routes </p> key = type of NetworkNode that is forbidden
     * </p> value = maximum still allowed number of routes to "key" NetworkNode
     *
     * @return false if this route did not exist
     */
    public Map<Class, Integer> getRoutingRules() {
        return routingRules;
    }

    public boolean removeRoute(NetworkNode destination) {
        if (destination == null) {
            throw new IllegalArgumentException("destination is NULL");
        }
        if (!routes.containsKey(destination)) {
            return false;
        }
        routes.remove(destination);
        return true;
    }

    /**
     * read-only collection of destinations
     *
     * @return
     */
    public Collection<String> getAllDestinations() {
        return getRoutes().values();
    }

    /**
     * clears the routing table however directly connected routes MUST be
     * persisted
     */
    public void clearRoutingTable() {
        List<String> toDelete = new LinkedList<String>();

        for (String destination : routes.keySet()) {
            if (!destination.equals(routes.get(destination))) {
                toDelete.add(destination);
            }
        }

        for (String deleteMe : toDelete) {
            routes.remove(deleteMe);
        }

    }

    public boolean containsRoute(String destination) {
        if (destination == null) {
            throw new IllegalArgumentException("destination is NULL");
        }
        return routes.containsKey(destination);
    }

    public String getNextHopFromRoutingTable(String destination) {
        if (destination == null) {
            throw new IllegalArgumentException("destination is NULL");
        }
        return routes.get(destination);
    }

    /**
     * adds new routing rule (new route) to routing table
     *
     * @param destination destination (final) vertex
     * @param nextHop vertex of next hop
     */
    public void addRoute(String destination, String nextHop) {
        routes.put(destination, nextHop);
    }

    @Override
    public String toString() {
        return "NetworkNode{" + "name=" + name + '}';
    }
}
