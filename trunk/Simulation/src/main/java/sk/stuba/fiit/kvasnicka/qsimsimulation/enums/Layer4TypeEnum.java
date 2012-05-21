package sk.stuba.fiit.kvasnicka.qsimsimulation.enums;

/**
 * this enum defines whether packet is TCP or UDP packet
 *
 * @author Igor Kvasnicka
 */
public enum Layer4TypeEnum {
    TCP {
        @Override
        public boolean isRetransmissionEnabled() {
            return true;
        }
    }, UDP {
        @Override
        public boolean isRetransmissionEnabled() {
            return false;
        }
    };

    /**
     * what to do when packet is lost/wrong CRC?
     *
     * @return true when packet retransmission should be done (e.g. TCP protocol); false if nothing should be done (e.g. UDP protocol)
     */
    public abstract boolean isRetransmissionEnabled();
}
