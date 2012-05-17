package sk.stuba.fiit.kvasnicka.qsimdatamodel.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.SwQueues;
import sk.stuba.fiit.kvasnicka.qsimsimulation.decorators.ProcessedPacketDecorator;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.NotEnoughBufferSpaceException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.QueueingHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Fragment;
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
import java.util.UUID;

/**
 * superclass of all nodes in topology (router/computer) all NetworkNodes are
 * "routable" - they all store information about routing
 *
 * @author Igor Kvasnicka
 */

//todo porozmyslat, co ma byt JAXB transient
//todo Cav, future me: potom nezabudni, ze ak max size TX alebo RX je -1, tak sa to berie ako nekonecno - uz to je nakodene, len v GUI na to nezabudni ;)


//todo kde bude volany RED/WRED ?

@EqualsAndHashCode(of = {"name"})
@XmlSeeAlso({Router.class, Switch.class, Computer.class})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class NetworkNode implements Serializable {
    private static final Logger logg = Logger.getLogger(NetworkNode.class);


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
    public QosMechanism qosMechanism;


    @XmlTransient
    private List<ProcessedPacketDecorator> processingPackets;

    private SwQueues swQueues;

    @XmlTransient
    @Getter
    private Map<NetworkNode, OutputInterface> txInterfaces;
    /**
     * map of all input interfaces - this is used as RX buffers
     */
    private Map<NetworkNode, InputInterface> rxInterfaces;

    private int maxTxBufferSize;
    private int maxIntputQueueSize;
    private int maxOutputQueueSize;


    private List<Packet> inputQueue;
    /**
     * all packets in output queue
     * if you are looking for QoS queues, they are <b>defined</b> on SwQueues
     */
    private List<Packet> outputQueue;


    @Setter
    @Getter
    private TopologyManager topologyManager;
    /**
     * maximum number of simultaneously processed packets
     */
    private int maxProcessingPackets;


    /**
     * this constructor is used only during deserialisation process
     */
    public NetworkNode() {
        routes = new HashMap<String, String>();
        routingRules = new HashMap<Class, Integer>();
        fillForbiddenRoutingRules(routingRules);
        processingPackets = new LinkedList<ProcessedPacketDecorator>();
        txInterfaces = new HashMap<NetworkNode, OutputInterface>();
        rxInterfaces = new HashMap<NetworkNode, InputInterface>();
        inputQueue = new LinkedList<Packet>();
        outputQueue = new LinkedList<Packet>();
    }

    protected NetworkNode(String name, QosMechanism qosMechanism, SwQueues swQueues, int maxTxBufferSize, int maxIntputQueueSize, int maxOutputQueueSize, int maxProcessingPackets) {
        this();
        this.name = name;
        this.swQueues = swQueues;
        this.qosMechanism = qosMechanism;
        this.maxTxBufferSize = maxTxBufferSize;
        this.maxIntputQueueSize = maxIntputQueueSize;
        this.maxOutputQueueSize = maxOutputQueueSize;
        this.maxProcessingPackets = maxProcessingPackets;
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
     * moves all packets that has been processed to output queue
     * some of the packets may be dropped due to queue size limit
     *
     * @param simulationTime current simulation time
     */

    public void moveFromProcessingToOutputQueue(double simulationTime) {
        List<ProcessedPacketDecorator> processedPackets;
        //iterate through packets in processing state and put processed packets to output queue
        while ((processedPackets = getProcessingFinishedPacket(simulationTime)) != null) {
            for (ProcessedPacketDecorator p : processedPackets) {

                if (p.getPacket().isPacketDelivered(this)) {//is packet delivered?
                    packetIsDelivered(p.getPacket());
                    processedPackets.remove(p);
                    continue;
                }
                moveFromProcessingToOutputQueue(p.getPacket(), p.getTimeWhenProcessingFinished());
            }
        }
    }


    /**
     * adds packets to output queue
     * this is done automatically when moving from processing
     *
     * @param packet packet to be added
     * @param time   time, when this happens
     */
    private void moveFromProcessingToOutputQueue(Packet packet, double time) {
        //classify and mark packet first
        packet.setQosQueue(qosMechanism.classifyAndMarkPacket(packet)); //todo toto ma bt este pred processingom, po prijati paketu, aby som zistil, ci to ma byt fast switching
        //add as much packets (fragments) as possible to TX buffer - packet can be put into TX only if there is enough space for ALL its fragments
        int mtu = topologyManager.findEdge(getName(), getNextHopNetworkNode(packet).getName()).getMtu();
        try {
            addToTxBuffer(packet, mtu);
        } catch (NotEnoughBufferSpaceException e) {
            addToOutputQueue(packet, time);
        }
    }

    private void addToOutputQueue(Packet packet, double time) {
        //add packet to output queue
        if (isOutputQueueAvailable(packet.getQosQueue())) {        //first check if there is enough space in output queue
            logg.debug("no space left in output queue -> packet dropped");     //todo retransmisia: tu pridat paket do nejakej fronty v predoslom network node, aby znovu poslal paket - ale iba, ak je to TCP
            return;
        }
        packet.setTimeWhenCameToQueue(time);
        outputQueue.add(packet);
    }

    /**
     * determines if there is enough space in QoS queue in output queue for this packet
     *
     * @param qosQueue number of qos queue where this packet belongs
     * @return
     */
    private boolean isOutputQueueAvailable(int qosQueue) {
        if (swQueues.getQueueUsedCapacity(qosQueue) + 1 <= swQueues.getQueueMaxCapacity(qosQueue)) {
            return false;
        }
        return true;
    }

    /**
     * adds newly created packets to output queue
     *
     * @param packet packet to be added
     * @param time   time when this happens
     */
    public void addNewPacketsToOutputQueue(Packet packet, double time) {
        moveFromProcessingToOutputQueue(packet, time);
    }


    /**
     * this method deals with packets that have been delivered
     *
     * @param packet packet that has been delivered
     */
    private void packetIsDelivered(Packet packet) {
        logg.debug("packet has been delivered to destination " + packet.getDestination() + " - it took " + (packet.getTimeWhenNextStateOccures() - packet.getCreationTime()) + "msec");
        packet = null;
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


    public void clearPackets() {
        processingPackets.clear();

        for (OutputInterface outputInterface : txInterfaces.values()) {
            outputInterface.reset();
        }

        for (InputInterface inputInterface : rxInterfaces.values()) {
            inputInterface.clear();
        }
    }

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


    /**
     * returns all packets that are in all output interfaces in all output queues of this network node
     *
     * @return
     */
//    public List<Packet> getPacketsInOutputQueues() {
//        List<Packet> packets = new LinkedList<Packet>();
//        for (OutputInterface outputQueue : outputInterfaces.values()) {
//
//            for (Packet p : outputQueue.get()) {
//                packets.add(p);
//            }
//        }
//        return packets;
//    }
    public void moveFromOutputQueueToTxBuffer(double time) {
        List<Packet> eligiblePackets = getPacketsInOutputQueue(time);
        List<Packet> packetsToSend = qosMechanism.decitePacketsToMoveFromOutputQueue(eligiblePackets, swQueues);
        for (Packet p : packetsToSend) {
            int mtu = topologyManager.findEdge(getName(), getNextHopNetworkNode(p).getName()).getMtu();
            try {
                addToTxBuffer(p, mtu);
            } catch (NotEnoughBufferSpaceException e) {
                addToOutputQueue(p, time);
            }
        }
    }

    /**
     * retrieves all packets that are waiting in input buffer within given time interval
     *
     * @param time current simulation time
     * @return returns packets in node's queue that came into queue within specified time interval
     */
    private List<Packet> getPacketsInOutputQueue(double time) {
        List<Packet> list = new LinkedList<Packet>();

        for (Packet packet : outputQueue) {
            if (packet.getTimeWhenCameToQueue() <= time) {
                list.add(packet);
            }
        }
        return list;
    }

    /**
     * adds packet to TX buffer
     *
     * @param packet packet to add
     * @param mtu    maximum transfer unit
     * @throws NotEnoughBufferSpaceException if there is not enough space in TX for all fragments of specified packet
     */
    public void addToTxBuffer(Packet packet, int mtu) throws NotEnoughBufferSpaceException {
        NetworkNode nextHop = getNextHopNetworkNode(packet);

        //create fragments
        Fragment[] fragments = createFragments(packet, mtu, nextHop);


        if (! txInterfaces.containsKey(nextHop)) {
            txInterfaces.put(nextHop, new OutputInterface(maxTxBufferSize, this, nextHop, topologyManager));
        }

        OutputInterface txInterface = txInterfaces.get(nextHop);

        if (txInterface.getFragmentsCount() + fragments.length > txInterface.getMaxBufferSize()) {
            throw new NotEnoughBufferSpaceException();
        }

        for (Fragment f : fragments) {
            txInterfaces.get(nextHop).addFragment(f); //finally add fragment to output queue
        }
    }

    /**
     * determines if there is enough space in TX buffer for given number of fragments
     *
     * @param nextHopNetworkNode next hop NetworkNode
     * @param newFragmentSize    fragments I want to put into TX buffer
     * @return true/false according to method description
     */
    private boolean isTxAvailable(NetworkNode nextHopNetworkNode, int newFragmentSize) {

        if (! txInterfaces.containsKey(nextHopNetworkNode)) {//create Tx buffer if not exists
            txInterfaces.put(nextHopNetworkNode, new OutputInterface(maxTxBufferSize, this, nextHopNetworkNode, topologyManager));
        }

        int currentFragmentSize = txInterfaces.get(nextHopNetworkNode).getFragmentsCount();
        if (currentFragmentSize + newFragmentSize > txInterfaces.get(nextHopNetworkNode).getMaxBufferSize()) {
            return false;
        }
        return true;
    }


    private Fragment[] createFragments(Packet packet, int mtu, NetworkNode nextHop) {
        Fragment[] fragments = new Fragment[QueueingHelper.calculateNumberOfFragments(packet.getPacketSize(), mtu)];
        String fragmentID = UUID.randomUUID().toString();
        for (int i = 0; i < fragments.length; i++) {
            fragments[i] = new Fragment(packet, fragments.length, fragmentID, this, nextHop);
        }
        return fragments;
    }

    public void movePacketsToTheWire(double simulationTime) {
        for (OutputInterface outputInterface : txInterfaces.values()) {
            outputInterface.sendPackets(simulationTime);
        }
    }


    /**
     * adds packet to RX buffer - if all fragments are present, a packet if put into input queue
     *
     * @param fragment     fragment to add/receive
     * @param timeReceived simulation time, when fragment came to the network node
     */
    public void addToRxBuffer(Fragment fragment, double timeReceived) {

        //todo ziaden tail drop tu nie je - ak sa fragment nezmesti, tak by som ho mal dropnut - posielam prilis rychlo a vela fragmentov
        //todo  pouzit NotEnoughBufferSpaceException
        if (! rxInterfaces.containsKey(fragment.getFrom())) {
            rxInterfaces.put(fragment.getFrom(), new InputInterface(fragment.getFrom(), maxTxBufferSize));
        }
        Packet packet = rxInterfaces.get(fragment.getFrom()).fragmentReceived(fragment);
        if (packet != null) {//this was the last fragment to complete a whole packet - now I can place this packet into input queue
            if (isProcessingAvailable()) {//packet can be processed
                addPacketToProcessing(packet);
            } else {//there is no CPU left for this packet to be processed - it is placed into input queue
                packet.setTimeWhenCameToQueue(timeReceived);
                inputQueue.add(packet);
            }
        }
    }

    /**
     * tries to move as much packets as possible from input queue to processing
     */
    public void moveFromInputQueueToProcessing(double simulationTime) {

        for (Packet packet : inputQueue) {
            if (packet.getTimeWhenCameToQueue() <= simulationTime) {
                if (isProcessingAvailable()) {//is it possible to start processing this packet?
                    inputQueue.remove(packet);
                    addPacketToProcessing(packet);
                } else {
                    return; //there is no reason to try to move other packets
                }
            }
        }
    }

    /**
     * this method determines if packet can be processed or it must wait because there is no CPU left for it
     */
    private boolean isProcessingAvailable() {
        return processingPackets.size() != maxProcessingPackets;
    }


    public NetworkNode getNextHopNetworkNode(Packet packet) {
        if (! containsRoute(packet.getDestination().getName())) {
            throw new IllegalStateException("Invalid routing rule - unable to route packet to destination: " + packet.getDestination().getName() + " from node: " + this.getName());
        }
        String nextHop = getRoutes().get(packet.getDestination().getName());
        return topologyManager.findNetworkNode(nextHop);
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
        for (OutputInterface outputInterface : txInterfaces.values()) {
            if (! outputInterface.isEmpty()) return false;
        }
        for (InputInterface inputInterface : rxInterfaces.values()) {
            if (! inputInterface.isEmpty()) return false;
        }
        return true;
    }

    //todo z input a output interface urobit zvlast triedy - nie inner triedy
    public static class OutputInterface {

        private List<Fragment> fragments = new LinkedList<Fragment>();
        /**
         * determines when packet serialisation is done and next packet is ready to be serialised
         */
        private double serialisationEndTime = 0;

        @Getter
        private int maxBufferSize;

        @Getter
        private NetworkNode networknodeNextHop;

        private Edge edge;

        public OutputInterface(int maxBufferSize, NetworkNode currentNode, NetworkNode networknodeNextHop, TopologyManager topologyManager) {

            if (maxBufferSize == - 1) {
                this.maxBufferSize = Integer.MAX_VALUE;
            } else {
                this.maxBufferSize = maxBufferSize;
            }
            this.networknodeNextHop = networknodeNextHop;
            edge = topologyManager.findEdge(currentNode.getName(), networknodeNextHop.getName());
        }

        public void addFragment(Fragment packet) {
            fragments.add(packet);
        }

        /**
         * returns number of fragments placed int this TX
         *
         * @return
         */
        public int getFragmentsCount() {
            return fragments.size();
        }

        public void reset() {
            serialisationEndTime = 0;
            fragments.clear();
        }

        public boolean isEmpty() {
            return fragments.isEmpty();
        }

        /**
         * serialises as many packets as possible
         * if time after serialisation is complete is bigger than current simulation time, sending quits
         */
        public void sendPackets(double simulationTime) {
            for (int i = 0, fragmentsSize = fragments.size(); i < fragmentsSize; i++) {//iterate through all the fragments in TX
                Fragment fragment = fragments.get(i);

                int fragmentSize = QueueingHelper.calculateFragmentSize(i + 1, QueueingHelper.calculateNumberOfFragments(fragment.getOriginalPacket().getPacketSize(), edge.getMtu()), edge.getMtu(), fragment.getOriginalPacket().getPacketSize());
                double serDelay = DelayHelper.calculateSerialisationDelay(edge,fragmentSize);
                if (serialisationEndTime + serDelay > simulationTime) { //there is no time left to serialise this packet
                    break;
                }
                serialisationEndTime += serDelay;

                double propagationDelay = DelayHelper.calculatePropagationDelay(edge);
                fragment.setSimulationTime(serialisationEndTime + serDelay + propagationDelay);
                //remove fragment from TX
                fragments.remove(fragment);//critical kvoli tomuto to otestuj!!! mazem z Listu pocas toho, ako ho iterujem - iterujem cez index, takze by to mohlo byt OK
                i--;
                //add fragment to the edge
                edge.addFragment(fragment);
            }
        }
    }

    private class InputInterface {

        //what newtork node is on the other end of the wire
        private NetworkNode networkNodeFrom;
        private int maxTxSize;
        /**
         * key = fragmentID
         * value = number of fragment already received
         */
        private Map<String, Integer> fragmentMap;

        private InputInterface(NetworkNode networkNodeFrom, int maxTxSize) {
            if (maxTxSize == - 1) {
                this.maxTxSize = - 1;
            } else {
                this.maxTxSize = maxTxSize;
            }
            this.networkNodeFrom = networkNodeFrom;

            fragmentMap = new HashMap<String, Integer>();
        }

        /**
         * this method is called whenever fragment is received
         *
         * @param fragment received fragment
         * @return reference to Packet objekt when all fragments are received; null if there are some fragments to be received
         */
        public Packet fragmentReceived(Fragment fragment) {
            if (! fragmentMap.containsKey(fragment.getFragmentID())) {//this is the first fragment I received

                if (fragment.getFragmentCountTotal() == 1) {//there is only one fragment
                    return fragment.getOriginalPacket();
                }

                fragmentMap.put(fragment.getFragmentID(), 1);
                return null;
            }
            int recievedFragments = fragmentMap.get(fragment.getFragmentID());

            if (getNumberOfFragments() == maxTxSize) {//there is not enough space - tail drop
                logg.debug("no spaceleft in TX buffer -> packet dropped"); //todo retransmisia; pouzit NotEnoughBufferSpaceException
                return null;
            }

            if (recievedFragments + 1 == fragment.getFragmentCountTotal()) { //fragment I've just received is the last one
                fragmentMap.remove(fragment.getFragmentID());
                return fragment.getOriginalPacket();
            }

            fragmentMap.put(fragment.getFragmentID(), recievedFragments + 1);
            return null;
        }

        public int getNumberOfFragments() {
            int numberOfFragments = 0;
            for (Integer fragmentCount : fragmentMap.values()) {
                numberOfFragments += fragmentCount;
            }
            return numberOfFragments;
        }

        public void clear() {
            fragmentMap.clear();
        }

        public boolean isEmpty() {
            return fragmentMap.isEmpty();
        }
    }
}
