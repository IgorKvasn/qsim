package sk.stuba.fiit.kvasnicka.qsimsimulation.decorators;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

/**
 * this object is used when packet is being processed
 *
 * @author Igor Kvasnicka
 */
@Getter
@EqualsAndHashCode
public class ProcessedPacketDecorator implements Comparable<ProcessedPacketDecorator> {
    private final Packet packet;
    private final double timeWhenProcessingFinished;

    public ProcessedPacketDecorator(Packet packet, double timeWhenProcessingFinished, NetworkNode node) {
        if (packet == null) throw new IllegalArgumentException("packet is NULL");
        if (node == null) throw new IllegalArgumentException("node is NULL");
        this.packet = packet;
        this.timeWhenProcessingFinished = timeWhenProcessingFinished;
    }

    @Override
    public int compareTo(ProcessedPacketDecorator o) {
        if (getTimeWhenProcessingFinished() > getTimeWhenProcessingFinished()) return - 1;
        if (getTimeWhenProcessingFinished() < getTimeWhenProcessingFinished()) return 1;
        return 0;
    }
}
