package sk.stuba.fiit.kvasnicka.qsimsimulation.packet;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
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

    private PacketManager packetManager;
    private PacketTypeEnum packetType;
    /**
     * simulation time when this packets changes its state
     */

    private double simulationTime;
    @Setter
    private double timeWhenCameToQueue;


    private final double creationTime;


    /**
     * creates new Packet object
     *
     * @param size          size in Bytes
     * @param destination   where is this packet headed to
     * @param source        NetworkNode that created this packet
     * @param packetManager reference to packet manager class
     * @param packetType    type of the packet depending on packet rule configuration (user's choice)
     * @param creationTime  simulation time, when this packet changes its state
     */
    public Packet(int size, NetworkNode destination, NetworkNode source, PacketManager packetManager, PacketTypeEnum packetType, double creationTime) {
        this.packetSize = size;
        this.destination = destination;
        this.source = source;
        this.packetManager = packetManager;
        this.packetType = packetType;
        this.simulationTime = creationTime;

        this.qosQueue = - 1;// source.getQosMechanism().classifyAndMarkPacket(this);
        this.creationTime = creationTime;
    }

    public void setSimulationTime(double simulationTime) {
        if (this.simulationTime > simulationTime) {
            throw new IllegalStateException("new packet simulation time is lower then current simulation time: " + this.simulationTime + ">" + simulationTime);
        }
        this.simulationTime = simulationTime;
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
}
