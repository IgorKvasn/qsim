package sk.stuba.fiit.kvasnicka.qsimsimulation.helpers;

import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Fragment;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.util.UUID;

/**
 * this helper contains methods with queueing involved
 *
 * @author Igor Kvasnicka
 */
public abstract class QueueingHelper {

    /**
     * calculates, how big [bytes] is a fragment - most fragments are as big as MTU, but the last one is smaller (in most cases)
     *
     * @param fragmentIndex counted from 1
     * @param fragmentCount total number of fragments
     * @param mtu           MTU
     * @param packetSize    total packet size
     * @return size of a single fragment [bytes]
     */
    public static int calculateFragmentSize(int fragmentIndex, int fragmentCount, int mtu, int packetSize) {
        if (fragmentIndex > fragmentCount) {
            throw new IllegalArgumentException("fragmentIndex is bigger than fragmentCount");
        }

        if ((fragmentCount == fragmentIndex) && (packetSize > fragmentCount * mtu)) {
            //this is the last fragment, but I need more fragments to send this packet
            throw new IllegalStateException("not enough fragments: packet size = " + packetSize + ", MTU = " + mtu + " calculated fragment count: " + fragmentCount);
        }

        if (packetSize < mtu) {
            return packetSize;
        }

        if (fragmentIndex != fragmentCount) {
            return mtu;
        }

        return packetSize % mtu;
    }

    /**
     * calculates, how many fragments will be created for a packet
     *
     * @param packetSize size of packet
     * @param mtu        maximum transfer unit - maximum size of a packet to be non-fragmented
     * @return number of fragments to be created
     */
    public static int calculateNumberOfFragments(int packetSize, int mtu) {
        if (mtu <= 0) throw new IllegalArgumentException("MTU is zero or negative");
        if (packetSize % mtu == 0) {
            return packetSize / mtu;
        }
        return (packetSize / mtu) + 1;
    }


    public static Fragment[] createFragments(Packet packet, int mtu, NetworkNode currentNode, NetworkNode nextHop) {
        Fragment[] fragments = new Fragment[QueueingHelper.calculateNumberOfFragments(packet.getPacketSize(), mtu)];
        String fragmentID = UUID.randomUUID().toString();
        for (int i = 0; i < fragments.length; i++) {
            fragments[i] = new Fragment(packet, i + 1, fragments.length, fragmentID, currentNode, nextHop);
            fragments[i].setReceivedTime(packet.getSimulationTime());
        }
        return fragments;
    }
}
