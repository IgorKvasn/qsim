package sk.stuba.fiit.kvasnicka.qsimsimulation.packet;

import lombok.Getter;
import lombok.Setter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;

/**
 * If a packet is too big, fragments are created
 *
 * @author Igor Kvasnicka
 */


//moze to byt takto: po sieti sa bude posielat Fragment, ktory bude mat fragmentID a pocetFragmentov
//kazdy fragment ma referenciu na povodny paket
//ked v NetworkNode som dostal vsetky fragmenty, vezmem paket z referencie fragmentu a pracujem s nim v network node

//todo namiesto objektov Fragment len simulovat posielanie - vzdy, kedy by mal prist fragment, tak sa len zvysi nejaky counter v input interface
@Getter
public class Fragment {
    private final Packet originalPacket;

    /**
     * how many fragments are there in total for one packet
     */
    private final int fragmentCountTotal;
    /**
     * this is ID of fragments - all fragments with the same ID were created from the same packet
     */
    private final String fragmentID;
    /**
     * network node that created and sent this fragment
     * note, that this is not the original sender of this "packet", but it is a next-hop node (or precisely: previous-hop)
     */
    private final NetworkNode from;
    /**
     * netowrk node that this fragment is headed to
     * this is not a final destination of a fragment/packet
     * it is just a next-hop network node
     */
    private final NetworkNode to;
    /**
     * time fragment is serialised on the next-hop network node
     */
    @Setter
    private double simulationTime;

    public Fragment(Packet originalPacket, int fragmentCountTotal, String fragmentID, NetworkNode from, NetworkNode to) {
        this.originalPacket = originalPacket;
        this.fragmentCountTotal = fragmentCountTotal;
        this.fragmentID = fragmentID;
        this.from = from;
        this.to = to;
    }
}
