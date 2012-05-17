package sk.stuba.fiit.kvasnicka.qsimsimulation.packet;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketStateEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;

/**
 * @author Igor Kvasnicka
 */
@Getter
@EqualsAndHashCode
public class Packet {
    /**
     * state of the packet is determined by other ways, e.g. internal List where it is placed,...
     * also there are some new states, that are not in PacketStateEnum (INPUT_QUEUE,TX_BUFFER,...)
     */
    @Deprecated
    private PacketStateEnum state;
    private int packetSize;
    private NetworkNode destination;
    private PacketPosition position;
    /**
     * number of queue where this packet belongs
     * this number is calculated during "marking" phase
     * in general, high number means high priority
     * <p/>
     * may be null if packet has not yet been marked
     * <p/>
     * the lowest value is 1 (not 0)
     */
    @Setter
    private Integer qosQueue;
    private PacketManager packetManager;
    private PacketTypeEnum packetType;
    /**
     * simulation time when this packets changes its state
     */
    @Setter
    private double simulationTime;
    @Setter
    private double timeWhenCameToQueue;
    @Setter
    private double timeWhenCameToNetworkNode;


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
        this.packetManager = packetManager;
        this.packetType = packetType;
        this.simulationTime = creationTime;

        this.state = PacketStateEnum.OUPUT_QUEUE;
        this.position = new PacketPosition(source, state, this);
        this.qosQueue = - 1;// source.getQosMechanism().classifyAndMarkPacket(this);
        this.creationTime = creationTime;
    }


    /**
     * returns time needed to move to next state
     *
     * @return time needing to move to next packet state ; Long.MAX_VALUE if packet is in buffer and so time to next state cannot be calculated; Long.MIN_VALUE if packet is already delivered
     */
    public double calculateTimeToNextState() {
        if (state == null) throw new IllegalStateException("packet state is NULL");

        if (PacketStateEnum.OUPUT_QUEUE == state) {
            checkPositionInTheNetworkNode();
            return Double.MAX_VALUE;
        }

        if (PacketStateEnum.DELIVERED == state) {
            return Double.MIN_VALUE;
        }

        //depending on packet position
        switch (state) {
            case ON_THE_WIRE:
                checkPositionOnTheWire();
                return DelayHelper.calculatePropagationDelay(getPosition().getEdge());
            case SERIALISING_INPUT_START:
                checkPositionOnTheWire();
                return DelayHelper.calculateSerialisationDelay(getPosition().getEdge(), getPacketSize());
            case SERIALISING_OUTPUT_START:
                checkPositionOnTheWire();
                return DelayHelper.calculateSerialisationDelay(getPosition().getEdge(), getPacketSize());
            default:
                throw new IllegalStateException("unknown packet state: " + state);
        }
    }

    private void checkPositionOnTheWire() {
        if (! position.isOnTheWire()) {
            throw new IllegalStateException("packet is expected to be on the wire, but it is not - or PacketPosition is badly configured");
        }
    }

    private void checkPositionInTheNetworkNode() {
        if (position.isOnTheWire()) {
            throw new IllegalStateException("packet is expected to be in the NetworkNode, but it is not - or PacketPosition is badly configured");
        }
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

//    @Override
//    public long getDelay(TimeUnit unit) {
//        return unit.convert(timeWhenNextStateOccures - packetManager.getSimulationTime(), TimeUnit.MILLISECONDS);
//    }
//
//    @Override
//    public int compareTo(Delayed o) {
//        Packet oo = (Packet) o;
//        if (oo.timeWhenNextStateOccures > timeWhenNextStateOccures) return - 1;
//        if (oo.timeWhenNextStateOccures < timeWhenNextStateOccures) return 1;
//        return 0;
//    }
}
