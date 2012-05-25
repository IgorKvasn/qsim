package sk.stuba.fiit.kvasnicka.qsimdatamodel.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.SwQueues;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers.InputInterface;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers.OutputInterface;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.packet.PacketDeliveredEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ping.PingPacketDeliveredEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.NotEnoughBufferSpaceException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.PacketCrcErrorException;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    private static Logger logg = Logger.getLogger(NetworkNode.class);


    @Getter
    @Setter
    private String name;

    /**
     * defines rules for forbidden neigbours key= neigbour NetworkNode object
     * (Router, Computer,..) as class value = maximum connections (links) are
     * possible to create with this neigbour
     */
    @XmlTransient
    protected Map<Class, Integer> routingRules;
    @Getter
    private QosMechanism qosMechanism;


    @XmlTransient
    private List<Packet> processingPackets;

    private SwQueues swQueues;

    /**
     * map of all output interfaces - this is used as TX buffers
     */
    @XmlTransient
    @Getter
    private Map<NetworkNode, OutputInterface> txInterfaces;
    /**
     * map of all input interfaces - this is used as RX buffers
     */
    @XmlTransient
    @Getter
    private Map<NetworkNode, InputInterface> rxInterfaces;

    private int maxTxBufferSize;
    private int maxIntputQueueSize;


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
    @Getter
    private double tcpDelay;


    /**
     * this constructor is used only during deserialisation process
     */
    public NetworkNode() {
        routingRules = new HashMap<Class, Integer>();
        fillForbiddenRoutingRules(routingRules);
        processingPackets = new LinkedList<Packet>();
        txInterfaces = new HashMap<NetworkNode, OutputInterface>();
        rxInterfaces = new HashMap<NetworkNode, InputInterface>();
        inputQueue = new LinkedList<Packet>();
        outputQueue = new LinkedList<Packet>();
    }

    protected NetworkNode(String name, QosMechanism qosMechanism, SwQueues swQueues, int maxTxBufferSize, int maxIntputQueueSize, int maxProcessingPackets, double tcpDelay) {
        this();
        this.name = name;
        this.swQueues = swQueues;
        this.qosMechanism = qosMechanism;
        this.maxTxBufferSize = maxTxBufferSize;
        this.maxIntputQueueSize = maxIntputQueueSize;
        this.maxProcessingPackets = maxProcessingPackets;
        this.tcpDelay = tcpDelay;
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
        packet.setSimulationTime(packet.getSimulationTime() + DelayHelper.calculateProcessingDelay(this));
        processingPackets.add(packet);
    }

    /**
     * moves all packets that has been processed to output queue
     * some of the packets may be dropped due to queue size limit
     *
     * @param simulationTime current simulation time
     */

    public void movePacketsFromProcessingToOutputQueue(double simulationTime) {
        List<Packet> processedPackets;
        //iterate through packets in processing state and put processed packets to output queue
        while ((processedPackets = getProcessingFinishedPacket(simulationTime)) != null) {
            for (Iterator<Packet> iterator = processedPackets.iterator(); iterator.hasNext(); ) {
                Packet p = iterator.next();

                if (p.isPacketDelivered(this)) {//is packet delivered?
                    packetIsDelivered(p);
                    iterator.remove();
                    continue;
                }
                moveFromProcessingToOutputQueue(p);
            }
        }
    }


    /**
     * adds packets to output queue
     * this is done automatically when moving from processing
     *
     * @param packet packet to be added
     */
    private void moveFromProcessingToOutputQueue(Packet packet) {
        //classify and mark packet first
        packet.setQosQueue(qosMechanism.classifyAndMarkPacket(packet)); //todo toto ma bt este pred processingom, po prijati paketu, aby som zistil, ci to ma byt fast switching
        //add as much packets (fragments) as possible to TX buffer - packet can be put into TX only if there is enough space for ALL its fragments
        int mtu = topologyManager.findEdge(getName(), packet.getNextHopNetworkNode(this).getName()).getMtu();
        try {
            addToTxBuffer(packet, mtu);
        } catch (NotEnoughBufferSpaceException e) {
            try {
                addToOutputQueue(packet);
            } catch (NotEnoughBufferSpaceException e1) {
                logg.debug("no space left in output queue -> packet dropped");
                if (packet.getLayer4().isRetransmissionEnabled()) {
                    retransmittPacket(packet);
                }
            }
        }
    }

    private void addToOutputQueue(Packet packet) throws NotEnoughBufferSpaceException {
        //add packet to output queue
        if (! isOutputQueueAvailable(packet.getQosQueue())) {        //first check if there is enough space in output queue
            throw new NotEnoughBufferSpaceException("There is not enough space in output queue for packet with QoS queue: " + packet.getQosQueue());
        }
        packet.setTimeWhenCameToQueue(packet.getSimulationTime());
        outputQueue.add(packet);
    }

    /**
     * determines if there is enough space in QoS queue in output queue for this packet
     *
     * @param qosQueue number of qos queue where this packet belongs
     * @return true/false according to description
     */
    private boolean isOutputQueueAvailable(int qosQueue) {
        if (swQueues.getQueueUsedCapacity(qosQueue, outputQueue) + 1 > swQueues.getQueueMaxCapacity(qosQueue)) {
            return false;
        }
        return true;
    }

    /**
     * adds newly created packets to output queue
     *
     * @param packet packet to be added
     */
    public void addNewPacketsToOutputQueue(Packet packet) {
        moveFromProcessingToOutputQueue(packet);
    }


    /**
     * this method deals with packets that have been delivered
     *
     * @param packet packet that has been delivered
     */
    private void packetIsDelivered(Packet packet) {
        logg.debug("packet has been delivered to destination " + packet.getDestination() + " - it took " + (packet.getSimulationTime() - packet.getCreationTime()) + "msec");
        if (packet.getSimulationRule().isPing()) {
            packet.getSimulationRule().firePingPacketDeliveredEvent(new PingPacketDeliveredEvent(this, packet));
        } else {
            packet.getSimulationRule().firePacketDeliveredEvent(new PacketDeliveredEvent(this, packet));
        }
    }


    /**
     * returns all packets that have been successfully processed until current simulation time
     *
     * @param simulationTime current simulation time
     * @return null if no such packet found
     */
    public List<Packet> getProcessingFinishedPacket(double simulationTime) {
        List<Packet> result = new LinkedList<Packet>();

        for (Packet packet : processingPackets) {
            if (packet.getSimulationTime() <= simulationTime) {
                result.add(packet);
            }
        }
        if (result.isEmpty()) return null;

        //now remove these packets from processing                
        processingPackets.removeAll(result);

        return result;
    }


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
     * returns forbidden routes </p> key = type of NetworkNode that is forbidden
     * </p> value = maximum still allowed number of routes to "key" NetworkNode
     *
     * @return false if this route did not exist
     */
    public Map<Class, Integer> getRoutingRules() {
        return routingRules;
    }

    @Override
    public String toString() {
        return "NetworkNode{" + "name=" + name + '}';
    }


    public void moveFromOutputQueueToTxBuffer(double time) {
        List<Packet> eligiblePackets = getPacketsInOutputQueue(time);
        List<Packet> packetsToSend = qosMechanism.decitePacketsToMoveFromOutputQueue(eligiblePackets, swQueues);
        for (Packet p : packetsToSend) {
            int mtu = topologyManager.findEdge(getName(), p.getNextHopNetworkNode(this).getName()).getMtu();
            try {
                addToTxBuffer(p, mtu); //add packet to TX buffer
                outputQueue.remove(p); //remove packet from output queue
            } catch (NotEnoughBufferSpaceException e) {
                //there is not enough space in TX, so packet stays in output queue
                break;//no need to continue
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
        NetworkNode nextHop = packet.getNextHopNetworkNode(this);

        if (nextHop == null) throw new IllegalStateException("nextHop network node is NULL");

        //create fragments
        Fragment[] fragments = QueueingHelper.createFragments(packet, mtu, this, nextHop);


        if (! txInterfaces.containsKey(nextHop)) {
            txInterfaces.put(nextHop, new OutputInterface(maxTxBufferSize, this, nextHop, topologyManager));
        }

        OutputInterface txInterface = txInterfaces.get(nextHop);

        if (txInterface.getFragmentsCount() + fragments.length > txInterface.getMaxBufferSize()) {
            throw new NotEnoughBufferSpaceException("Not enough space in RX buffer");
        }

        for (Fragment f : fragments) {
            txInterfaces.get(nextHop).addFragment(f); //finally add fragment to output queue
        }
    }


    public void movePacketsToTheWire(double simulationTime) {
        for (OutputInterface outputInterface : txInterfaces.values()) {
            outputInterface.serialisePackets(simulationTime);
        }
    }


    /**
     * adds packet to RX buffer - if all fragments are present, a packet if put into input queue
     *
     * @param fragment fragment to add/receive
     */
    public void addToRxBuffer(Fragment fragment) {
        if (! rxInterfaces.containsKey(fragment.getFrom())) {
            Edge edge = topologyManager.findEdge(this.getName(), fragment.getFrom().getName());
            rxInterfaces.put(fragment.getFrom(), new InputInterface(edge, maxTxBufferSize));
        }
        Packet packet = null;
        try {
            packet = rxInterfaces.get(fragment.getFrom()).fragmentReceived(fragment);
        } catch (NotEnoughBufferSpaceException e) {
            logg.debug("no space left in TX buffer -> packet dropped");

            rxInterfaces.get(fragment.getFrom()).removeFragments(fragment.getFragmentID());
            if (fragment.getOriginalPacket().getLayer4().isRetransmissionEnabled()) {
                retransmittPacket(fragment.getOriginalPacket());
            }
            return;
        } catch (PacketCrcErrorException e) {
            logg.debug("packet has wrong CRC");
            if (e.getPacket().getLayer4().isRetransmissionEnabled()) {
                retransmittPacket(e.getPacket());
            }
            return;
        }
        if (packet != null) {//this was the last fragment to complete a whole packet - now I can place this packet into input queue
            if (isProcessingAvailable()) {//packet can be processed
                addPacketToProcessing(packet);
            } else {//there is no CPU left for this packet to be processed - it is placed into input queue
                packet.setTimeWhenCameToQueue(packet.getSimulationTime()); //packet.getSimulationTime() is a time, when packet was de-fragmented
                inputQueue.add(packet);
            }
        }
    }

    /**
     * tries to move as much packets as possible from input queue to processing
     */
    public void moveFromInputQueueToProcessing(double simulationTime) {
        for (Iterator<Packet> iterator = inputQueue.iterator(); iterator.hasNext(); ) {
            Packet packet = iterator.next();
            if (packet.getTimeWhenCameToQueue() <= simulationTime) {
                if (isProcessingAvailable()) {//is it possible to start processing this packet?
                    iterator.remove();
                    addPacketToProcessing(packet);
                } else {
                    return; //there is no reason to try to move other packets
                }
            }
        }
    }

    /**
     * packet should be transmitted again
     * packet will be send in time: packet.getSimulationTime() + current network node TCP timeout
     *
     * @param packet packet to send
     */
    public void retransmittPacket(Packet packet) {
        NetworkNode nodePrevious = packet.getPreviousHopNetworkNode(this);
        logg.debug("packet retransmission from node: " + nodePrevious);
        packet.setSimulationTime(packet.getSimulationTime() + nodePrevious.getTcpDelay());
        nodePrevious.moveFromProcessingToOutputQueue(packet);
    }

    /**
     * this method determines if packet can be processed or it must wait because there is no CPU left for it
     */
    private boolean isProcessingAvailable() {
        return processingPackets.size() != maxProcessingPackets;
    }


    /**
     * returns all packets that are currently processing
     *
     * @return
     */
    public List<Packet> getPacketsInProcessing() {
        List<Packet> list = new LinkedList<Packet>();

        for (Packet packet : processingPackets) {
            list.add(packet);
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
}
