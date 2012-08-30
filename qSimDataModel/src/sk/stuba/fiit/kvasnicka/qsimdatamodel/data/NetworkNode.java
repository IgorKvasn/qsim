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
    @SimLog
    @XmlTransient
    private SimulationLogUtils simulLog;

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
    private QosMechanism qosMechanism;


    @XmlTransient
    private List<Packet> processingPackets;
    @Getter
    private OutputQueueManager outputQueues;

    /**
     * map of all output interfaces - this is used as TX buffers
     */
    @XmlTransient
    @Getter
    private Map<NetworkNode, TxBuffer> txInterfaces;
    /**
     * map of all input interfaces - this is used as RX buffers
     */
    @XmlTransient
    @Getter
    private Map<NetworkNode, RxBuffer> rxInterfaces;
    @Getter
    private int maxTxBufferSize;
    @Getter
    private int maxRxBufferSize;
    @Getter
    private InputQueue inputQueue;

    @Setter
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
    private UsageStatistics allRXBuffers;
    @Getter
    private UsageStatistics allTXBuffers;
    @Getter
    private UsageStatistics allOutputQueues;
    @Getter
    private UsageStatistics allProcessingPackets;


    /**
     * this constructor is used only during deserialisation process
     */
    public NetworkNode() {
        routingRules = new HashMap<Class, Integer>();
        fillForbiddenRoutingRules(routingRules);
        processingPackets = new LinkedList<Packet>();
        txInterfaces = new HashMap<NetworkNode, TxBuffer>();
        rxInterfaces = new HashMap<NetworkNode, RxBuffer>();
        inputQueue = new InputQueue();

        allOutputQueues = new UsageStatistics() {
            @Override
            public int getUsage() {
                return getAllOutputQueueUsage();
            }
        };

        allRXBuffers = new UsageStatistics() {
            @Override
            public int getUsage() {
                return getRXUsage();
            }
        };

        allTXBuffers = new UsageStatistics() {
            @Override
            public int getUsage() {
                return getTXUsage();
            }
        };

        allProcessingPackets = new UsageStatistics() {
            @Override
            public int getUsage() {
                return getProcessingPackets();
            }
        };
    }

    protected NetworkNode(String name, QosMechanism qosMechanism, OutputQueueManager swQueues, int maxTxBufferSize, int maxRxBufferSize, int maxIntputQueueSize, int maxProcessingPackets, double tcpDelay, double minProcessingDelay, double maxProcessingDelay) {
        this();
        this.name = name;
        this.outputQueues = swQueues;
        this.qosMechanism = qosMechanism;
        this.maxTxBufferSize = maxTxBufferSize;
        this.maxRxBufferSize = maxRxBufferSize;
        inputQueue.setMaxSize(maxIntputQueueSize);
        this.maxProcessingPackets = maxProcessingPackets;
        this.tcpDelay = tcpDelay;
        this.minProcessingDelay = minProcessingDelay;
        this.maxProcessingDelay = maxProcessingDelay;
        outputQueues.setNode(this);
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
        return outputQueues.getAllUsage();
    }

    /**
     * returns number of packets in input queue
     *
     * @return
     */
    public int getInputQueueUsage() {
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
        int total = 0;
        for (int i = 0; i < outputQueues.getQueueCount(); i++) {
            total += outputQueues.getQueueMaxCapacity(i);
        }
        return total;
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
        packet.setQosQueue(qosMechanism.classifyAndMarkPacket(this, packet)); //todo toto ma bt este pred processingom, po prijati paketu, aby som zistil, ci to ma byt fast switching
        //add as much packets (fragments) as possible to TX buffer - packet can be put into TX only if there is enough space for ALL its fragments
        int mtu = topologyManager.findEdge(getName(), packet.getNextHopNetworkNode(this).getName()).getMtu();
        try {
            addToTxBuffer(packet, mtu);
        } catch (NotEnoughBufferSpaceException e) {
            try {
                addToOutputQueue(packet);
            } catch (NotEnoughBufferSpaceException e1) {
                if (logg.isDebugEnabled()) {
                    logg.debug("no space left in output queue -> packet dropped");
                }
                simulLog.log(new SimulationLog(LogCategory.INFO, "No space left in output queue -> packet dropped", getName(), LogSource.VERTEX, packet.getSimulationTime()));
                if (packet.getLayer4().isRetransmissionEnabled()) {
                    retransmittPacket(packet);
                }
                if (Layer4TypeEnum.TCP == packet.getLayer4()) {
                    nodeCongested(packet);   //todo test
                }
            }
        }
    }

    private void addToOutputQueue(Packet packet) throws NotEnoughBufferSpaceException {
        //add packet to output queue
        if (! outputQueues.isOutputQueueAvailable(packet.getQosQueue())) {        //first check if there is enough space in output queue
            throw new NotEnoughBufferSpaceException("There is not enough space in output queue for packet with QoS queue: " + packet.getQosQueue());
        }
        packet.setTimeWhenCameToQueue(packet.getSimulationTime());
        outputQueues.addPacket(packet);
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
        return "NetworkNode{" + "name=" + name + '}';
    }

    /**
     * moves as much packets as possible from output queue to TX buffer
     * it moves only packets that are eligible (according to their simulation time)
     * and if TX is full, it stops moving packets
     *
     * @param time current simulation time
     */
    public void moveFromOutputQueueToTxBuffer(double time) {
        List<List<Packet>> eligiblePackets = outputQueues.getPacketsInOutputQueue(time);
        List<Packet> packetsToSend = qosMechanism.decitePacketsToMoveFromOutputQueue(this, eligiblePackets);
        for (Packet p : packetsToSend) {
            int mtu = topologyManager.findEdge(getName(), p.getNextHopNetworkNode(this).getName()).getMtu();
            try {
                addToTxBuffer(p, mtu); //add packet to TX buffer
                outputQueues.removePacket(p); //remove packet from output queue
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
            txInterfaces.put(nextHop, new TxBuffer(maxTxBufferSize, this, nextHop, topologyManager));
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
            Edge edge = topologyManager.findEdge(this.getName(), fragment.getFrom().getName());
            rxInterfaces.put(fragment.getFrom(), new RxBuffer(edge, maxRxBufferSize));
        }
        Packet packet = null;
        try {
            packet = rxInterfaces.get(fragment.getFrom()).fragmentReceived(fragment);
        } catch (NotEnoughBufferSpaceException e) {
            if (logg.isDebugEnabled()) {
                logg.debug("no space left in TX buffer -> packet dropped");
            }

            simulLog.log(new SimulationLog(LogCategory.INFO, "No space left in TX buffer -> packet dropped", getName(), LogSource.VERTEX, fragment.getReceivedTime()));

            if (fragment.getOriginalPacket().getLayer4().isRetransmissionEnabled()) {
                retransmittPacket(fragment.getOriginalPacket());
            }

            return;
        } catch (PacketCrcErrorException e) {
            if (logg.isDebugEnabled()) {
                logg.debug("packet has wrong CRC");
            }

            simulLog.log(new SimulationLog(LogCategory.INFO, "Wrong packet CRC.", getName(), LogSource.VERTEX, fragment.getReceivedTime()));

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
                if (inputQueue.isAvailable()) {
                    inputQueue.addPacket(packet);
                } else {    //there is not enough space in input queue - packet is dropped
                    if (logg.isDebugEnabled()) {
                        logg.debug("no space left in input queue -> packet dropped");
                    }
                    simulLog.log(new SimulationLog(LogCategory.INFO, "No space left in input queue -> packet dropped", getName(), LogSource.VERTEX, packet.getSimulationTime()));
                    if (packet.getLayer4().isRetransmissionEnabled()) {
                        retransmittPacket(packet);
                    }
                    if (Layer4TypeEnum.TCP == packet.getLayer4()) {
                        nodeCongested(packet);   //todo test
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
    private void retransmittPacket(Packet packet) {//todo test - najprv nech je paket chybny a potom je ok
        NetworkNode nodePrevious = packet.getPreviousHopNetworkNode(this);
        if (logg.isDebugEnabled()) {
            logg.debug("packet retransmission from node: " + nodePrevious);
        }
        packet.setSimulationTime(packet.getSimulationTime() + nodePrevious.getTcpDelay());
        nodePrevious.moveFromProcessingToOutputQueue(packet);
    }

    /**
     * called when network node is congested - either input/output queue or TX/RX are full
     * it may seem that this method is called every time packet is retransmitted, but it is not true - when packet CRC is wrong, this method is not called
     *
     * @param packet packet that was dropped because of congestion
     */
    private void nodeCongested(Packet packet) {
        NetworkNode nodePrevious = packet.getPreviousHopNetworkNode(this);
        Edge edge = topologyManager.findEdge(this.getName(), nodePrevious.getName());
        edge.decreaseSpeed(packet.getSimulationRule(), packet.getSimulationTime() + nodePrevious.getTcpDelay());
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
        if (! outputQueues.isEmpty()) return false;
        return true;
    }
}
