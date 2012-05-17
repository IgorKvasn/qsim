package sk.stuba.fiit.kvasnicka.qsimsimulation.helpers;

import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Fragment;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.util.UUID;

/**
 * this helper contains methods with queueing involved
 *
 * @author Igor Kvasnicka
 */
public class QueueingHelper {
    private final static Logger logg = Logger.getLogger(QueueingHelper.class);
    private PacketManager packetManager;
    private TopologyManager topologyManager;

    public QueueingHelper(PacketManager packetManager, TopologyManager topologyManager) {
        this.packetManager = packetManager;
        this.topologyManager = topologyManager;
    }


    /**
     * calculates, how big [bytes] is a fragment - most fragments are as big as MTU, but the last one is smaller (in most cases)
     *
     * @param fragmentIndex counted from 1
     * @param fragmentCount total number of fragments
     * @param mtu           MTU
     * @param packetSize    total packet size
     * @return size of a single fragment [bytes]
     */
    public static int calculateFragmentSize(int fragmentIndex, int fragmentCount, int mtu, int packetSize) { //todo presunut do queuing helper ako static metodu
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


//    /**
//     * checks if there is enough space left in the output queue
//     *
//     * @param queueNumber queue number (packet priority)
//     * @param node        network node I am interested in
//     * @return true if packet can be added
//     */
//    private boolean isEnoughSpaceInOutputQueue(int queueNumber, NetworkNode node) {
//        if (queueNumber > node.getQueueCount()) {
//            throw new IllegalArgumentException("queueNumber is higher than maximum queue count: " + queueNumber + " > " + node.getQueueCount());
//        }
//        DelayQueue<Packet> packets = networkNodeMap.get(node);
//        int size = 0;
//        for (Packet p : packets) {
//            if ((PacketStateEnum.OUPUT_QUEUE == p.getState()) && (p.getQosQueue() == queueNumber)) {
//                size += p.getPacketSize();
//            }
//        }
//        if (size <= node.getQueueSize(queueNumber)) {
//            return true;
//        }
//        return false;
//    }


//    /**
//     * changes packet state of packets in input buffer and make them PROCESSING state
//     *
//     * @param simulationTime current simulation time
//     */
//    public void changePacketsStateInputBuffer(long simulationTime) {
//
//        for (NetworkNode node : networkNodeMap.keySet()) {
//            long startTime = simulationTime - SimulationTimer.TIME_QUANTUM; //previous time
//            long endTime = startTime + node.getPacketProcessingEndsTime();
//
//            //QoS method for queueing is called each time there is some CPU left to process packet
//            //so I have to iterate through all these times and call QoS queueing method
//
//            while (endTime > simulationTime) {
//                movePacketsFromOutputQueueInInterval(node, startTime, endTime);
//                startTime += node.getPacketProcessingEndsTime();
//                endTime += node.getPacketProcessingEndsTime();
//            }
//        }
//    }


    public static Fragment[] createFragments(Packet packet, int mtu, NetworkNode currentNode, NetworkNode nextHop) {
        Fragment[] fragments = new Fragment[QueueingHelper.calculateNumberOfFragments(packet.getPacketSize(), mtu)];
        String fragmentID = UUID.randomUUID().toString();
        for (int i = 0; i < fragments.length; i++) {
            fragments[i] = new Fragment(packet, i + 1, fragments.length, fragmentID, currentNode, nextHop);
        }
        return fragments;
    }
}
