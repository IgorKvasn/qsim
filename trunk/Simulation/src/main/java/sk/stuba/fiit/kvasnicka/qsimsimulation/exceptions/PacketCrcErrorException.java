package sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions;

import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

/**
 * received packet has got a wrong CRC - it will be dropped and maybe a request for retransmission
 *
 * @author Igor Kvasnicka
 */
public class PacketCrcErrorException extends Exception {
    @Getter
    private Packet packet;

    /**
     * creates new exception of this type
     *
     * @param packet packet that is wrong
     */
    public PacketCrcErrorException(Packet packet) {
        super();
        this.packet = packet;
    }
}