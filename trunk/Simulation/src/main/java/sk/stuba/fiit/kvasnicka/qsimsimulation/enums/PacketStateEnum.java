package sk.stuba.fiit.kvasnicka.qsimsimulation.enums;

/**
 * all states that packet can be in
 * order is important
 * it is ordered from the state when packet is sent to state when packet is done
 *
 * @author Igor Kvasnicka
 */
@Deprecated //see Packet object for explanation - "field state"
public enum PacketStateEnum {
    TX_BUFFER,
    /**
     * packet is placed in output buffer
     */
    OUPUT_QUEUE,
    /**
     * packet starts to be serialised
     */
    SERIALISING_OUTPUT_START,
    /**
     * packet is transmitting over wire
     */
    ON_THE_WIRE,
    /**
     * packet starts to be received
     */
    SERIALISING_INPUT_START,

    /**
     * packet is in the network node and it starts to be processed
     */
    PROCESSING,
    /**
     * final state
     */
    DELIVERED;

    public PacketStateEnum getNext() {
        return this.ordinal() < PacketStateEnum.values().length - 1 ? PacketStateEnum.values()[this.ordinal() + 1] : null;
    }

    public boolean isInBuffer() {
        return OUPUT_QUEUE == this;
    }
}
