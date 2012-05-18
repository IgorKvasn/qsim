package sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers;

import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.NotEnoughBufferSpaceException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Fragment;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Igor Kvasnicka
 */
public class InputInterface {
    private static final Logger logg = Logger.getLogger(InputInterface.class);

    //what newtork node is on the other end of the wire
    private NetworkNode networkNodeFrom;
    private int maxTxSize;
    /**
     * key = fragmentID
     * value = number of fragment already received
     */
    private Map<String, Integer> fragmentMap;

    public InputInterface(NetworkNode networkNodeFrom, int maxTxSize) {
        if (maxTxSize == - 1) {
            this.maxTxSize = - 1;
        } else {
            this.maxTxSize = maxTxSize;
        }
        this.networkNodeFrom = networkNodeFrom;

        fragmentMap = new HashMap<String, Integer>();
    }

    /**
     * this method is called whenever fragment is received
     *
     * @param fragment received fragment
     * @return reference to Packet objekt when all fragments are received; null if there are some fragments to be received
     */
    public Packet fragmentReceived(Fragment fragment) throws NotEnoughBufferSpaceException {
        if (! fragmentMap.containsKey(fragment.getFragmentID())) {//this is the first fragment I received

            if (fragment.getFragmentCountTotal() == 1) {//there is only one fragment
                fragment.getOriginalPacket().setSimulationTime(fragment.getReceivedTime());
                return fragment.getOriginalPacket();
            }

            fragmentMap.put(fragment.getFragmentID(), 1);
            return null;
        }
        int recievedFragments = fragmentMap.get(fragment.getFragmentID());

        if (getNumberOfFragments() == maxTxSize) {//there is not enough space - tail drop
            throw new NotEnoughBufferSpaceException("Not enough space in RX buffer");
        }

        if (recievedFragments + 1 == fragment.getFragmentCountTotal()) { //fragment I've just received is the last one - the packet is compete
            fragmentMap.remove(fragment.getFragmentID());
            fragment.getOriginalPacket().setSimulationTime(fragment.getReceivedTime());
            return fragment.getOriginalPacket();
        }

        fragmentMap.put(fragment.getFragmentID(), recievedFragments + 1);
        return null;
    }

    public int getNumberOfFragments() {
        int numberOfFragments = 0;
        for (Integer fragmentCount : fragmentMap.values()) {
            numberOfFragments += fragmentCount;
        }
        return numberOfFragments;
    }

    public void clear() {
        fragmentMap.clear();
    }

    public boolean isEmpty() {
        return fragmentMap.isEmpty();
    }
}
