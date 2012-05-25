package sk.stuba.fiit.kvasnicka.qsimsimulation.events.ping;

import java.util.EventListener;

/**
 * @author Igor Kvasnicka
 */
public interface PingPacketDeliveredListener extends EventListener {
    /**
     * ping packet has been delivered
     *
     * @param evt event that occured
     */
    public void packetDeliveredOccurred(PingPacketDeliveredEvent evt);
}


