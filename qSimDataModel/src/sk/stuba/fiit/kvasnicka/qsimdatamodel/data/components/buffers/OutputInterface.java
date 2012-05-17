package sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers;

import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.QueueingHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Fragment;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Igor Kvasnicka
 */
public class OutputInterface {

    private List<Fragment> fragments = new LinkedList<Fragment>();
    /**
     * determines when packet serialisation is done and next packet is ready to be serialised
     */
    private double serialisationEndTime = 0;

    @Getter
    private int maxBufferSize;

    @Getter
    private NetworkNode networknodeNextHop;

    private Edge edge;

    public OutputInterface(int maxBufferSize, NetworkNode currentNode, NetworkNode networknodeNextHop, TopologyManager topologyManager) {

        if (maxBufferSize == - 1) {
            this.maxBufferSize = Integer.MAX_VALUE;
        } else {
            this.maxBufferSize = maxBufferSize;
        }
        this.networknodeNextHop = networknodeNextHop;
        edge = topologyManager.findEdge(currentNode.getName(), networknodeNextHop.getName());
    }

    public void addFragment(Fragment packet) {
        fragments.add(packet);
    }

    /**
     * returns number of fragments placed int this TX
     *
     * @return
     */
    public int getFragmentsCount() {
        return fragments.size();
    }

    public void reset() {
        serialisationEndTime = 0;
        fragments.clear();
    }

    public boolean isEmpty() {
        return fragments.isEmpty();
    }

    /**
     * serialises as many packets as possible
     * if time after serialisation is complete is bigger than current simulation time, sending quits
     */
    public void serialisePackets(double simulationTime) {
        for (Iterator<Fragment> iterator = fragments.iterator(); iterator.hasNext(); ) {   //iterate through all the fragments in TX
            Fragment fragment = iterator.next();
            if (serialisationEndTime >= fragment.getOriginalPacket().getSimulationTime()) {//this fragment will be ready to send after serialisation ends
                continue;
            }
            if (serialisationEndTime == 0) {//this is the first packet to serialise
                serialisationEndTime = fragment.getOriginalPacket().getSimulationTime();
            }

            int fragmentSize = QueueingHelper.calculateFragmentSize(fragment.getFragmentNumber(), QueueingHelper.calculateNumberOfFragments(fragment.getOriginalPacket().getPacketSize(), edge.getMtu()), edge.getMtu(), fragment.getOriginalPacket().getPacketSize());
            double serDelay = DelayHelper.calculateSerialisationDelay(edge, fragmentSize);
            if (serialisationEndTime + serDelay > simulationTime) { //there is no time left to serialise this packet
                continue;
            }
            serialisationEndTime += serDelay;

            double propagationDelay = DelayHelper.calculatePropagationDelay(edge);
            fragment.setReceivedTime(serialisationEndTime + serDelay + propagationDelay);
            //remove fragment from TX
            iterator.remove();
            //add fragment to the edge
            edge.addFragment(fragment);
        }
    }
}
