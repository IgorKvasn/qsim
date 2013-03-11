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

package sk.stuba.fiit.kvasnicka.qsimdatamodel.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.OutputQueueManager;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.UsageStatistics;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers.RxBuffer;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers.TxBuffer;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.InputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.packet.PacketDeliveredEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ping.PingPacketDeliveredEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.NotEnoughBufferSpaceException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.PacketCrcErrorException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.RoutingException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.QueueingHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.LogCategory;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.LogSource;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimLog;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLog;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLogUtils;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Fragment;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanismDefinition;

import java.io.IOException;
import java.io.ObjectInputStream;
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

//todo Cav, future me: potom nezabudni, ze ak max size TX alebo RX je -1, tak sa to berie ako nekonecno - uz to je nakodene, len v GUI na to nezabudni ;)


@EqualsAndHashCode(of = {"name"})
public abstract class NetworkNode implements Serializable {

    private static Logger logg = Logger.getLogger(NetworkNode.class);
    private static final long serialVersionUID = 5329622657148825389L;
    @SimLog
    private transient SimulationLogUtils simulLog;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String description;

    /**
     * defines rules for forbidden neigbours key= neigbour NetworkNode object
     * (Router, Computer,..) as class value = maximum connections (links) are
     * possible to create with this neigbour
     */
    protected Map<Class, Integer> routingRules;
    @Getter
    private QosMechanismDefinition qosMechanism;


    private transient List<Packet> processingPackets;

    @Getter
    private OutputQueueManager outputQueueManager;

    /**
     * map of all output interfaces - this is used as TX buffers
     */
    @Getter
    private HashMap<NetworkNode, TxBuffer> txInterfaces;
    /**
     * map of all input interfaces - this is used as RX buffers
     */
    @Getter
    private HashMap<NetworkNode, RxBuffer> rxInterfaces;
    @Getter
    private int maxTxBufferSize;
    @Getter
    private int maxRxBufferSize;
    @Getter
    private InputQueue inputQueue;

    @Getter
    private TopologyManager topologyManager;
    /**
     * maximum number of simultaneously processed packets
     */
    @Getter
    private int maxProcessingPackets;
    @Getter
    private double tcpDelay;
    @Getter
    private double minProcessingDelay;
    @Getter
    private double maxProcessingDelay;
    @Getter
    private int maxIntputQueueSize;

    @Getter
    private transient UsageStatistics allRXBuffers;

    @Getter
    private transient UsageStatistics allTXBuffers;

    @Getter
    private transient UsageStatistics allOutputQueues;

    @Getter
    private transient UsageStatistics allProcessingPackets;

    /**
     * creates new network node
     *
     * @param name                 name (unique)
     * @param qosMechanism         set of QoS mechanism used
     * @param maxTxBufferSize      max TX size
     * @param maxRxBufferSize      max RX size
     * @param maxIntputQueueSize   max Input queue size
     * @param maxProcessingPackets max number of packets being concurrently processed
     * @param tcpDelay             TCP delay (time after retransmission happens when no ACK was retrieved)
     * @param minProcessingDelay   minimum time processing of one packet takes
     * @param maxProcessingDelay   maximum time processing of one packet takes
     */
    protected NetworkNode(String name, String description, QosMechanismDefinition qosMechanism, int maxTxBufferSize, int maxRxBufferSize, int maxOutputQueueSize, int maxIntputQueueSize, int maxProcessingPackets, double tcpDelay, double minProcessingDelay, double maxProcessingDelay) {
        this.description = description;
        this.name = name;
        this.outputQueueManager = new OutputQueueManager(maxOutputQueueSize);
        this.qosMechanism = qosMechanism;
        this.maxTxBufferSize = maxTxBufferSize;
        this.maxRxBufferSize = maxRxBufferSize;
        this.maxIntputQueueSize = maxIntputQueueSize;
        inputQueue = new InputQueue(maxIntputQueueSize, this);
        this.maxProcessingPackets = maxProcessingPackets;
        this.tcpDelay = tcpDelay;
        this.minProcessingDelay = minProcessingDelay;
        this.maxProcessingDelay = maxProcessingDelay;
        this.outputQueueManager.initNode(this);

        routingRules = new HashMap<Class, Integer>();
        fillForbiddenRoutingRules(routingRules);
        processingPackets = new LinkedList<Packet>();
        txInterfaces = new HashMap<NetworkNode, TxBuffer>();
        rxInterfaces = new HashMap<NetworkNode, RxBuffer>();

        allOutputQueues = new UsageStatistics() {
            @Override
            public double getUsage() {
                return getAllOutputQueueUsage();
            }
        };

        allRXBuffers = new UsageStatistics() {
            @Override
            public double getUsage() {
                return getRXUsage();
            }
        };

        allTXBuffers = new UsageStatistics() {
            @Override
            public double getUsage() {
                return getTXUsage();
            }
        };

        allProcessingPackets = new UsageStatistics() {
            @Override
            public double getUsage() {
                return getProcessingPackets();
            }
        };
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        processingPackets = new LinkedList<Packet>();

        allOutputQueues = new UsageStatistics() {
            @Override
            public double getUsage() {
                return getAllOutputQueueUsage();
            }
        };

        allRXBuffers = new UsageStatistics() {
            @Override
            public double getUsage() {
                return getRXUsage();
            }
        };

        allTXBuffers = new UsageStatistics() {
            @Override
            public double getUsage() {
                return getTXUsage();
            }
        };

        allProcessingPackets = new UsageStatistics() {
            @Override
            public double getUsage() {
                return getProcessingPackets();
            }
        };
    }

    public void setTopologyManager(TopologyManager topologyManager) {
        this.topologyManager = topologyManager;
        //also create RX and TX buffers
        initTxBuffers();
        initRxBuffers();
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
     * returns number of packets in output queue
     *
     * @return
     */
    public int getAllOutputQueueUsage() {
        return outputQueueManager.getAllUsage();
    }

    /**
     * returns number of packets in input queue
     *
     * @return
     */
    public double getInputQueueUsage() {
        return inputQueue.getUsage();
    }

    /**
     * returns number of packets in RX (all RX buffers)
     *
     * @return
     */
    public int getRXUsage() {
        int usage = 0;
        for (RxBuffer in : rxInterfaces.values()) {
            usage += in.getNumberOfFragments();
        }
        return usage;
    }

    /**
     * retuns sum of all RX queue sizes
     *
     * @return
     */
    public int getMaxRxSizeTotal() {
        return maxRxBufferSize * rxInterfaces.size();
    }

    /**
     * retuns sum of all TX queue sizes
     *
     * @return
     */
    public int getMaxTxSizeTotal() {
        return maxTxBufferSize * txInterfaces.size();
    }

    /**
     * returns number of packets in TX (all TX buffers)
     *
     * @return
     */
    public int getTXUsage() {
        int usage = 0;
        for (TxBuffer out : txInterfaces.values()) {
            usage += out.getFragmentsCount();
        }
        return usage;
    }

    /**
     * returns number of packets being processed
     *
     * @return
     */
    public int getProcessingPackets() {
        return processingPackets.size();
    }

    /**
     * returns maximum number of packets that can be stored in all output queues (all QoS queues)
     *
     * @return
     */
    public int getMaxOutputQueueSize() {
        return outputQueueManager.getQueueCount() * outputQueueManager.getMaxCapacity();
    }

    /**
     * creates TX buffers
     */
    private void initTxBuffers() {
        List<Edge> edgesList = topologyManager.findEdgesWithNode(this);
        for (Edge edge : edgesList) {
            NetworkNode oppositeNode = edge.findOppositeNetworkNode(this);
            txInterfaces.put(oppositeNode, new TxBuffer(maxTxBufferSize, this, oppositeNode, topologyManager));
        }
    }

    /**
     * creates RX buffers
     */
    private void initRxBuffers() {
        List<Edge> edgesList = topologyManager.findEdgesWithNode(this);
        for (Edge edge : edgesList) {
            NetworkNode oppositeNode = edge.findOppositeNetworkNode(this);
            rxInterfaces.put(oppositeNode, new RxBuffer(edge, maxRxBufferSize, this));
        }
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
        //add as much packets (fragments) as possible to TX buffer - packet can be put into TX only if there is enough space for ALL its fragments
        int mtu = topologyManager.findEdge(getName(), packet.getNextHopNetworkNode(this).getName()).getMtu();
        try {
            addToTxBuffer(packet, mtu);
        } catch (NotEnoughBufferSpaceException e) {
            try {
                addToOutputQueue(packet);
                if (Layer4TypeEnum.TCP == packet.getLayer4()) {
                    nodeCongestedNot(packet);
                }
            } catch (NotEnoughBufferSpaceException e1) {
                if (logg.isDebugEnabled()) {
                    logg.debug("no space left in output queue -> packet dropped");
                }
                simulLog.log(new SimulationLog(LogCategory.INFO, "No space left in output queue -> packet dropped", getName(), LogSource.VERTEX, packet.getSimulationTime()));
                if (packet.getLayer4().isRetransmissionEnabled()) {
                    retransmittPacket(packet);
                } else {
                    packet.getSimulationRule().setCanCreateNewPacket(true); //in case this is a ICMP packet this allows to generate new packet on the src node
                }
                if (Layer4TypeEnum.TCP == packet.getLayer4()) {
                    nodeCongested(packet);
                }
            }
        }
    }

    private void addToOutputQueue(Packet packet) throws NotEnoughBufferSpaceException {
        //add packet to output queue
        if (! outputQueueManager.isOutputQueueAvailable(packet.getQosQueue())) {        //first check if there is enough space in output queue
            throw new NotEnoughBufferSpaceException("There is not enough space in output queue for packet with QoS queue: " + packet.getQosQueue());
        }
        packet.setTimeWhenCameToQueue(packet.getSimulationTime());
        outputQueueManager.addPacket(packet);
    }


    /**
     * adds newly created packets to output queue
     *
     * @param packet packet to be added
     */
    public void addNewPacketsToOutputQueue(Packet packet) {
        packet.setQosQueue(qosMechanism.classifyAndMarkPacket(this, packet));//new packet has to be classified
        moveFromProcessingToOutputQueue(packet);
    }


    /**
     * this method deals with packets that have been delivered
     *
     * @param packet packet that has been delivered
     */
    private void packetIsDelivered(Packet packet) {
        if (logg.isDebugEnabled()) {
            logg.debug("packet has been delivered to destination " + packet.getDestination() + " - it took " + (packet.getSimulationTime() - packet.getCreationTime()) + " msec");
        }
        if (packet.getSimulationRule().isPing()) {
            simulLog.log(new SimulationLog(LogCategory.INFO, "Ping packet delivered in: " + (packet.getSimulationTime() - packet.getCreationTime()) + " msec", packet.getSimulationRule().getSource().getName(), LogSource.VERTEX, packet.getSimulationTime()));
            packet.getSimulationRule().firePingPacketDeliveredEvent(new PingPacketDeliveredEvent(this, packet));
        } else {
            simulLog.log(new SimulationLog(LogCategory.INFO, "Packet has been delivered", packet.getSimulationRule().getSource().getName(), LogSource.VERTEX, packet.getSimulationTime()));
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

        for (TxBuffer outputInterface : txInterfaces.values()) {
            outputInterface.reset();
        }

        for (RxBuffer inputInterface : rxInterfaces.values()) {
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
        return name;
    }

    /**
     * moves as much packets as possible from output queue to TX buffer
     * it moves only packets that are eligible (according to their simulation time)
     * and if TX is full, it stops moving packets
     *
     * @param time current simulation time
     */
    public void moveFromOutputQueueToTxBuffer(double time) {
        Map<Integer, List<Packet>> eligiblePackets = outputQueueManager.getPacketsInOutputQueues(time);
        List<Packet> packetsToSend = qosMechanism.decitePacketsToMoveFromOutputQueue(this, eligiblePackets);
        for (Packet p : packetsToSend) {
            int mtu = topologyManager.findEdge(getName(), p.getNextHopNetworkNode(this).getName()).getMtu();
            try {
                addToTxBuffer(p, mtu); //add packet to TX buffer
                outputQueueManager.removePacket(p); //remove packet from output queue
            } catch (NotEnoughBufferSpaceException e) {
                //there is not enough space in TX, so packet stays in output queue
                break;//no need to continue
            }
        }
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
            throw new IllegalStateException("could not find TX buffer on node: " + getName() + " for opposide node: " + nextHop.getName());
        }

        TxBuffer txInterface = txInterfaces.get(nextHop);

        if (txInterface.getFragmentsCount() + fragments.length > txInterface.getMaxBufferSize()) {
            throw new NotEnoughBufferSpaceException("Not enough space in RX buffer");
        }

        for (Fragment f : fragments) {
            txInterfaces.get(nextHop).addFragment(f); //finally add fragment to output queue
        }
    }


    public void movePacketsToTheWire(double simulationTime) {
        for (TxBuffer outputInterface : txInterfaces.values()) {
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
            throw new IllegalStateException("could not find RX buffer on node: " + getName() + " for opposide node: " + fragment.getFrom().getName());
        }
        Packet packet = null;
        try {
            packet = rxInterfaces.get(fragment.getFrom()).fragmentReceived(fragment);
        } catch (NotEnoughBufferSpaceException e) {
            if (logg.isDebugEnabled()) {
                logg.debug("no space left in RX buffer -> packet dropped");
            }

            simulLog.log(new SimulationLog(LogCategory.INFO, "No space left in TX buffer -> packet dropped", getName(), LogSource.VERTEX, fragment.getReceivedTime()));

            if (fragment.getOriginalPacket().getLayer4().isRetransmissionEnabled()) {
                retransmittPacket(fragment.getOriginalPacket());
            } else {
                fragment.getOriginalPacket().getSimulationRule().setCanCreateNewPacket(true); //in case this is a ICMP packet this allows to generate new packet on the src node
            }

            return;
        } catch (PacketCrcErrorException e) {
            if (logg.isDebugEnabled()) {
                logg.debug("packet has wrong CRC");
            }

            simulLog.log(new SimulationLog(LogCategory.INFO, "Wrong packet CRC.", getName(), LogSource.VERTEX, fragment.getReceivedTime()));

            if (e.getPacket().getLayer4().isRetransmissionEnabled()) {
                retransmittPacket(e.getPacket());
            } else {
                if (fragment.getOriginalPacket().getSimulationRule().isPing()) {
                    fragment.getOriginalPacket().getSimulationRule().setCanCreateNewPacket(true); //in case this is a ICMP packet this allows to generate new packet on the src node
                }
            }
            return;
        }

        if (packet != null) {//this was the last fragment to complete a whole packet - now I can place this packet into input queue
            //mark and classify packet right after it was received
            packet.setQosQueue(qosMechanism.classifyAndMarkPacket(this, packet));

            if (isProcessingAvailable()) {//packet can be processed
                if (Layer4TypeEnum.TCP == packet.getLayer4()) {
                    nodeCongestedNot(packet);
                }
                addPacketToProcessing(packet);
            } else {//there is no CPU left for this packet to be processed - it is placed into input queue
                packet.setTimeWhenCameToQueue(packet.getSimulationTime()); //packet.getSimulationTime() is a time, when packet was de-fragmented
                if (inputQueue.isAvailable()) {
                    if (Layer4TypeEnum.TCP == packet.getLayer4()) {
                        nodeCongestedNot(packet);
                    }
                    inputQueue.addPacket(packet);
                } else {    //there is not enough space in input queue - packet is dropped
                    if (logg.isDebugEnabled()) {
                        logg.debug("no space left in input queue -> packet dropped");
                    }
                    simulLog.log(new SimulationLog(LogCategory.INFO, "No space left in input queue -> packet dropped", getName(), LogSource.VERTEX, packet.getSimulationTime()));
                    if (packet.getLayer4().isRetransmissionEnabled()) {
                        retransmittPacket(packet);
                    } else {
                        fragment.getOriginalPacket().getSimulationRule().setCanCreateNewPacket(true); //in case this is a ICMP packet this allows to generate new packet on the src node
                    }
                    if (Layer4TypeEnum.TCP == packet.getLayer4()) {
                        nodeCongested(packet);
                    }
                }
            }
        }
    }


    /**
     * tries to move as much packets as possible from input queue to processing
     */
    public void moveFromInputQueueToProcessing(double simulationTime) {
        for (Iterator<Packet> iterator = inputQueue.getInputQueue().iterator(); iterator.hasNext(); ) {
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
        NetworkNode nodePrevious = null;
        try {
            nodePrevious = packet.getPreviousHopNetworkNode(this);
            packet.setSimulationTime(packet.getSimulationTime() + nodePrevious.getTcpDelay());
            if (logg.isDebugEnabled()) {
                logg.debug("packet retransmission from node: " + nodePrevious + " started at: " + packet.getSimulationTime());
            }
            nodePrevious.moveFromProcessingToOutputQueue(packet);
        } catch (RoutingException e) {
            logg.warn("routing exception: " + e.getMessage());
        }
    }

    /**
     * called when network node is congested - either input/output queue or TX/RX are full
     * it may seem that this method is called every time packet is retransmitted, but it is not true - when packet CRC is wrong, this method is not called
     *
     * @param packet packet that was dropped because of congestion
     * @see #nodeCongestedNot(sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet)
     */
    private void nodeCongested(Packet packet) {
        NetworkNode nodePrevious = null;
        try {
            nodePrevious = packet.getPreviousHopNetworkNode(this);
            Edge edge = topologyManager.findEdge(this.getName(), nodePrevious.getName());
            edge.decreaseSpeed(packet.getSimulationRule(), packet.getSimulationTime() + nodePrevious.getTcpDelay());
        } catch (RoutingException e) {
            logg.warn("routing exception: " + e.getMessage());
        }
    }

    /**
     * node is no more congested
     *
     * @param packet
     * @see #nodeCongested(sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet)
     */
    private void nodeCongestedNot(Packet packet) {
        NetworkNode nodePrevious = null;
        try {
            nodePrevious = packet.getPreviousHopNetworkNode(this);
            Edge edge = topologyManager.findEdge(this.getName(), nodePrevious.getName());
            edge.increaseSpeed(packet.getSimulationRule());
        } catch (RoutingException e) {
            logg.warn("routing exception: " + e.getMessage());
        }
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
        for (TxBuffer outputInterface : txInterfaces.values()) {
            if (! outputInterface.isEmpty()) return false;
        }
        for (RxBuffer inputInterface : rxInterfaces.values()) {
            if (! inputInterface.isEmpty()) return false;
        }
        if (! inputQueue.isEmpty()) return false;
        if (! outputQueueManager.isEmpty()) return false;
        return true;
    }
}
