/*******************************************************************************
 * This file is part of qSim.
 *
 * qSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * qSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with qSim.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.buffers;

import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.NotEnoughBufferSpaceException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions.PacketCrcErrorException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Fragment;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Igor Kvasnicka
 */
public class InputInterface {
    private static Logger logg = Logger.getLogger(InputInterface.class);

    /**
     * edge that is connected to this intput interface
     */
    private Edge edge;
    private int maxTxSize;
    /**
     * key = fragmentID
     * value = number of fragment already received
     */
    private Map<String, Integer> fragmentMap;

    public InputInterface(Edge edge, int maxTxSize) {
        if (maxTxSize == - 1) {
            this.maxTxSize = - 1;
        } else {
            this.maxTxSize = maxTxSize;
        }

        this.edge = edge;
        fragmentMap = new HashMap<String, Integer>();
    }

    /**
     * this method is called whenever fragment is received
     *
     * @param fragment received fragment
     * @return reference to Packet objekt when all fragments are received; null if there are some fragments to be received
     */
    public Packet fragmentReceived(Fragment fragment) throws NotEnoughBufferSpaceException, PacketCrcErrorException {
        if (! fragmentMap.containsKey(fragment.getFragmentID())) {//this is the first fragment I received

            if (fragment.getFragmentCountTotal() == 1) {//there is only one fragment
                if (! isPacketCrcOK()) {
                    throw new PacketCrcErrorException(fragment.getOriginalPacket());
                }
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
            if (! isPacketCrcOK()) {
                throw new PacketCrcErrorException(fragment.getOriginalPacket());
            }

            return fragment.getOriginalPacket();
        }

        fragmentMap.put(fragment.getFragmentID(), recievedFragments + 1);
        return null;
    }

    private boolean isPacketCrcOK() {
        if (Math.random() <= edge.getPacketErrorRate()) {
            if (logg.isDebugEnabled()) {
                logg.debug("packet is CRC wrong");
            }
            return false;
        }
        return true;
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

    /**
     * removes all fragments with the same fragmentID
     *
     * @param fragmentID fragment's ID
     */
    public void removeFragments(String fragmentID) {
        if (! fragmentMap.containsKey(fragmentID)) {//this should not happen
            return;
        }
        fragmentMap.remove(fragmentID);
    }
}
