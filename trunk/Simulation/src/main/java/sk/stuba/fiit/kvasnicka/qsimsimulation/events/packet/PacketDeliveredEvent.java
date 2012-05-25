package sk.stuba.fiit.kvasnicka.qsimsimulation.events.packet;

import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.util.EventObject;

/**
 * @author Igor Kvasnicka
 */
public class PacketDeliveredEvent extends EventObject {
    @Getter
    private Packet packet;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public PacketDeliveredEvent(Object source, Packet packet) {
        super(source);
        this.packet = packet;
    }
}