package sk.stuba.fiit.kvasnicka.qsimsimulation.packet;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;

/**
 * @author Igor Kvasnicka
 */
@Getter
@EqualsAndHashCode
public class Packet {

    private int packetSize;
    private NetworkNode source;
    private NetworkNode destination;
    /**
     * number of queue where this packet belongs
     * this number is calculated during "marking" phase
     * in general, high number means high priority
     * <p/>
     * may be null if packet has not yet been marked
     * <p/>
     * the lowest value is 0
     */
    @Setter
    private int qosQueue;
    @Getter
    private Layer4TypeEnum layer4;

    private PacketManager packetManager;
    /**
     * simulation time when this packets changes its state
     */

    private double simulationTime;
    @Setter
    private double timeWhenCameToQueue;


    private final double creationTime;
    private final SimulationRuleBean simulationRule;


    /**
     * creates new Packet object
     *
     * @param size          size in Bytes
     * @param destination   where is this packet headed to
     * @param source        NetworkNode that created this packet
     * @param layer4        TCP/IP layer 4 protocol
     * @param packetManager reference to packet manager class
     * @param creationTime  simulation time, when this packet changes its state
     */
    public Packet(int size, NetworkNode destination, NetworkNode source, Layer4TypeEnum layer4, PacketManager packetManager, SimulationRuleBean simulationRule, double creationTime) {
        this.packetSize = size;
        this.destination = destination;
        this.source = source;
        this.layer4 = layer4;
        this.packetManager = packetManager;
        this.simulationRule = simulationRule;
        this.simulationTime = creationTime;

        this.qosQueue = - 1;
        this.creationTime = creationTime;
    }

    public void setSimulationTime(double simulationTime) {
        if (this.simulationTime > simulationTime) {
            throw new IllegalStateException("new packet simulation time is lower then current simulation time: " + this.simulationTime + ">" + simulationTime);
        }
        this.simulationTime = simulationTime;
    }

    /**
     * returns a type of packet - audio, video, data, ...
     *
     * @return type of packet
     */
    public PacketTypeEnum getPacketType() {
        return simulationRule.getPacketTypeEnum();
    }

    /**
     * answers the question: Is this packet finally delivered to his destination?
     *
     * @param currentNode NetworkNode where is this packet now; may be null (e.g. if packet is placed on the edge)
     * @return is packet in final destination?
     */
    public boolean isPacketDelivered(NetworkNode currentNode) {
        if (currentNode == null) return false;
        if (currentNode.equals(getDestination())) {
            return true;
        }
        return false;
    }


    public NetworkNode getNextHopNetworkNode(NetworkNode currentNode) {
        if (simulationRule == null) {
            throw new IllegalStateException("simulaiton rule for this packet is NULL - it has not been properly initialised");
        }
        return simulationRule.getNextHopFromRoutingTable(currentNode);
    }
}
