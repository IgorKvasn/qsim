package sk.stuba.fiit.kvasnicka.qsimdatamodel.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.decorators.ProcessedPacketDecorator;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketStateEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * superclass of all nodes in topology (router/computer) all NetworkNodes are
 * "routable" - they all store information about routing
 *
 * @author Igor Kvasnicka
 */
@EqualsAndHashCode(of = {"name"})
@XmlSeeAlso({Router.class, Switch.class, Computer.class})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class NetworkNode implements Serializable {
    private static final Logger logg = Logger.getLogger(NetworkNode.class);

    @Getter
    private int markDelay;

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
    protected Map<Class, Integer> routingRules;
    @Getter
    public QosMechanism qosMechanism;  //author's note: toto nemoze by static, koho napadlo dat toto ako static?????


    @XmlTransient
    private List<ProcessedPacketDecorator> processingPackets;     //todo aj pocet processing paketov je obmedzeny vid: router_architecture.pdf

    @XmlTransient
    @Getter
    private QueueDefinition[] queues;

    @XmlTransient
    @Getter
    private Map<NetworkNode, OutputInterface> outputInterfaces;

    @Setter
    @Getter
    private TopologyManager topologyManager;


    /**
     * this constructor is used only during deserialisation process
     */
    public NetworkNode() {
        routes = new HashMap<String, String>();
        routingRules = new HashMap<Class, Integer>();
        fillForbiddenRoutingRules(routingRules);
        processingPackets = new LinkedList<ProcessedPacketDecorator>();
        outputInterfaces = new HashMap<NetworkNode, OutputInterface>();
    }

    protected NetworkNode(String name, QosMechanism qosMechanism, int markDelay, QueueDefinition[] queues) {
        this();
        this.name = name;
        this.queues = queues;
        this.markDelay = markDelay;
        this.qosMechanism = qosMechanism;
    }


    public int getQueueCount() {
        return queues.length;
    }

    /**
     * defines forbidden NetworkNodes that cannot be neighbours with this
     * NetworkNode e.g. Computer cannot be linked with Computer; Computer can be
     * linked with only 1 Router/Switch; etc. override this method to define a
     * new rule(s) By default there are no rules defined.
     */
    protected void fillForbiddenRoutingRules(Map<Class, Integer> routingRules) {
    }


    /**
     * processes given packet
     *
     * @param packet packet to be processed
     */
    public void addPacketToProcessing(Packet packet) {
        processingPackets.add(new ProcessedPacketDecorator(packet, packet.getTimeWhenNextStateOccures() + DelayHelper.calculateProcessingDelay(this), this));
    }


    /**
     * returns all packets that have been successfully processed until current simulation time
     *
     * @param simulationTime current simulation time
     * @return null if no such packet found
     */
    public List<ProcessedPacketDecorator> getProcessingFinishedPacket(double simulationTime) {
        List<ProcessedPacketDecorator> result = new LinkedList<ProcessedPacketDecorator>();

        for (ProcessedPacketDecorator decorator : processingPackets) {
            if (decorator.getTimeWhenProcessingFinished() <= simulationTime) {
                result.add(decorator);
            }
        }
        if (result.isEmpty()) return null;

        //now remove these packets from processing                
        processingPackets.removeAll(result);

        return result;
    }


    /**
     * retrieves number of packets of certain priority
     *
     * @param delayQueue packets associated with one output interface
     * @param priority   packet priority
     * @return number of packets
     */
    private int getSizeOfPacketsByPriority(List<Packet> delayQueue, int priority) {
        int size = 0;
        for (Packet p : delayQueue) {
            if (p.getQosQueue() == null) throw new IllegalStateException("packet is not marked");
            if (p.getQosQueue() == priority) size += p.getPacketSize();
        }
        return size;
    }

    @Deprecated
    public void checkOutputBuffersSize() {


        //podla router_switch_architecture-velmi_dobre\!\!\!\!.pdf nezalezi na velkosti paketov ale len na ich pocte

//        for (DelayQueue<Packet> delayQueue : outputInterfaces.values()) {  //iterate through all output interfaces
//
//            for (int queueNumber = 0; queueNumber < getQueueCount(); queueNumber++) {
//
//                int size = getSizeOfPacketsByPriority(delayQueue, queueNumber);
//                if (size > getQueueSize(queueNumber)) {
//                    performTailDrop(getPacketsByPriority(delayQueue, queueNumber), getQueueSize(queueNumber), size);
//                }
//            }
//        }
    }

//    private void performTailDrop(List<Packet> packets, int maxQueueSize, int actualQueueSize) {
//
//        //sort all packets by time they came to buffer
//        Collections.sort(packets, new Comparator<Packet>() {
//            @Override
//            public int compare(Packet o1, Packet o2) {
//                return ((Long) o1.getTimeWhenCameToBuffer()).compareTo(o2.getTimeWhenCameToBuffer());
//            }
//        });
//
//        for (Packet packet : packets) {
//            if (actualQueueSize <= maxQueueSize) break; //queue is now OK
//            actualQueueSize -= packet.getPacketSize();
//            dropPacketFromOutputQueue(packet);
//        }
//    }

//    /**
//     * this method is called whenever packet should be dropped
//     *
//     * @param packet packet to drop
//     */
//    private void dropPacketFromOutputQueue(Packet packet) {
//        logg.debug("packet has been dropped on network node: " + this.getName());
//        removePacketFromQueue(packet);
//        packet = null;
//    }


    /**
     * returns max size of appropriate output queue
     *
     * @param queueNumber queue number
     * @return queue size in Bytes
     */
    public int getQueueSize(int queueNumber) {
        if (queueNumber > queues.length) {
            throw new IllegalArgumentException("Invalid queueNumber: " + queueNumber);
        }
        QueueDefinition queueDefinition = queues[queueNumber];
        return queueDefinition.getMaxCapacity();
    }


    public void clearPackets() {
        processingPackets.clear();

        for (OutputInterface outputInterface : outputInterfaces.values()) {
            outputInterface.getPackets().clear();
            outputInterface.setSerialisationEndTime(0);//no serialisation is being done
        }
    }

    /**
     * only certain NetworkNode type as capable of QoS mechanisms
     *
     * @return true if this node uses some QoS mechanism
     */
    public abstract boolean isQosCapable(); //todo isQosCabale niekde pouzit :)

    /**
     * key=destination network node </p> value=next hop network node </p> to
     * add/remove routes use appropriate methods use this method only to
     * retrieve routing table
     *
     * @return <b>read-only</b> Map of routes
     * @see #addRoute(java.lang.String, String) addRoute
     * @see #removeRoute(NetworkNode)
     *      removeRoute
     * @see #clearRoutingTable() clearRoutingTable
     * @see #containsRoute(String)
     *      containsRoute
     * @see #getAllDestinations() getAllDestinations
     * @see #getNextHopFromRoutingTable(String)
     *      getNextHopFromRoutingTable
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
        if (! routes.containsKey(destination.getName())) {
            return false;
        }
        routes.remove(destination.getName());
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
            if (! destination.equals(routes.get(destination))) {
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
     * @param nextHop     vertex of next hop
     */
    public void addRoute(String destination, String nextHop) {
        routes.put(destination, nextHop);
    }

    @Override
    public String toString() {
        return "NetworkNode{" + "name=" + name + '}';
    }

    /**
     * finds all packets in given queue
     *
     * @param delayQueue  output queue
     * @param queueNumber number of desired queue; counted from 1
     * @return list of packets of certain priority
     */
    public List<Packet> getPacketsByPriority(List<Packet> delayQueue, int queueNumber) {
        List<Packet> packets = new LinkedList<Packet>();
        for (Packet p : delayQueue) {
            if (p.getQosQueue() == queueNumber) packets.add(p);
        }
        return packets;
    }


//    public void removePacketFromQueue(Packet packet) {
//        for (DelayQueue<Packet> delayQueue : outputInterfaces.values()) {
//            if (delayQueue.remove(packet)) return;//I do not need to continue iterating if packet has been removed
//        }
//    }

    /**
     * returns all packets that are in all output interfaces in all output queues of this network node
     *
     * @return
     */
    public List<Packet> getPacketsInOutputQueues() {
        List<Packet> packets = new LinkedList<Packet>();
        for (OutputInterface outputQueue : outputInterfaces.values()) {

            for (Packet p : outputQueue.getPackets()) {
                if (PacketStateEnum.OUPUT_BUFFER == p.getState()) {
                    packets.add(p);
                }
            }
        }
        return packets;
    }

    /**
     * tries to add packet to
     *
     * @param packet            packet to add
     * @param timeItCameToQueue simulation time, when packet came to output queue
     */

    public void addToOutputQueue(Packet packet, double timeItCameToQueue) {
        //everything is OK - I can put packet into queue now
        packet.setTimeWhenCameToBuffer(timeItCameToQueue);
        packet.setState(PacketStateEnum.OUPUT_BUFFER);

        NetworkNode nextHop = getNextHopNetworkNode(packet);
        if (! outputInterfaces.containsKey(nextHop)) {
            outputInterfaces.put(nextHop, new OutputInterface(nextHop));
        }
        outputInterfaces.get(nextHop).addPacket(packet); //finally add packet to output queue
    }

    public NetworkNode getNextHopNetworkNode(Packet packet) {
        if (! containsRoute(packet.getDestination().getName())) {
            throw new IllegalStateException("Invalid routing rule - unable to route packet to destination: " + packet.getDestination().getName() + " from node: " + this.getName());
        }
        String nextHop = getRoutes().get(packet.getDestination().getName());
        return topologyManager.findNetworkNode(nextHop);
    }

    public void removePacketFromProcessing(Packet packet) {
        for (ProcessedPacketDecorator packetDecorator : processingPackets) {
            if (packetDecorator.getPacket().equals(packet)) {
                processingPackets.remove(packetDecorator);
                return;
            }
        }
        throw new IllegalStateException("I was told to remove packet from processing queue, but no such packet found");
    }

    /**
     * returns all packets that are currently processing
     *
     * @return
     */
    public List<Packet> getPacketsInProcessing() {
        List<Packet> list = new LinkedList<Packet>();

        for (ProcessedPacketDecorator decorator : processingPackets) {
            list.add(decorator.getPacket());
        }
        return list;
    }

    /**
     * checks if there are no packets in this network node - processing or in the output interfaces
     *
     * @return
     */
    public boolean isEmpty() {
        if (! processingPackets.isEmpty()) return false;
        for (OutputInterface outputInterface : outputInterfaces.values()) {
            if (! outputInterface.getPackets().isEmpty()) return false;
        }
        return true;
    }


    /**
     * this class is used to define (output) queue
     */
    @Getter
    @EqualsAndHashCode
    public static class QueueDefinition {
        /**
         * where is this output queue headed to
         */
        @Setter
        private NetworkNode nextHop;
        private int maxCapacity;
        @Setter
        private int usedCapacity;

        public QueueDefinition(NetworkNode nextHop, int maxCapacity) {
            if (nextHop == null) throw new IllegalArgumentException("nextHop is NULL");
            this.maxCapacity = maxCapacity;
            this.nextHop = nextHop;
            this.usedCapacity = 0;
        }
    }

    public static class OutputInterface {
        @Getter
        private List<Packet> packets = new LinkedList<Packet>();
        /**
         * determines when packet serialisation is done and next packet is ready to be serialised
         */
        @Getter
        @Setter
        private double serialisationEndTime = 0;

        @Getter
        private NetworkNode networknodeNextHop;

        public OutputInterface(NetworkNode networknodeNextHop) {
            this.networknodeNextHop = networknodeNextHop;
        }

        public void addPacket(Packet packet) {
            packets.add(packet);
        }

        public void removePacket(Packet p) {
            packets.remove(p);
        }
    }
}
