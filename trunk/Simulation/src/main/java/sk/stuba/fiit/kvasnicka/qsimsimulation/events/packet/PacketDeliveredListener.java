package sk.stuba.fiit.kvasnicka.qsimsimulation.events.packet;

import java.util.EventListener;

/**
 * @author Igor Kvasnicka
 */
public interface PacketDeliveredListener extends EventListener {
    /**
     * ping packet has been delivered
     *
     * @param evt event that occured
     */
    public void packetDeliveredOccurred(PacketDeliveredEvent evt);
}


