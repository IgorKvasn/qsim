package sk.stuba.fiit.kvasnicka.qsimsimulation.events.ping;

import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.util.EventObject;

/**
 * @author Igor Kvasnicka
 */
public class PingPacketDeliveredEvent extends EventObject {
    @Getter
    private Packet packet;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public PingPacketDeliveredEvent(Object source, Packet packet) {
        super(source);
        this.packet = packet;
    }
}
