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

import lombok.Getter;
import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.UsageStatistics;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.QueueingHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Fragment;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Igor Kvasnicka
 */
public class TxBuffer implements UsageStatistics, Serializable {

    private static final Logger logg = Logger.getLogger(TxBuffer.class);
    private static final long serialVersionUID = 1758488306110030406L;

    private transient List<Fragment> fragments = new LinkedList<Fragment>();
    /**
     * determines when packet serialisation is done and next packet is ready to be serialised
     */
    private double serialisationEndTime = 0;

    @Getter
    private int maxBufferSize;

    @Getter
    private NetworkNode networknodeNextHop;

    private Edge edge;
    @Getter
    private String name;

    public TxBuffer(int maxBufferSize, NetworkNode currentNode, NetworkNode networknodeNextHop, TopologyManager topologyManager) {

        if (maxBufferSize == - 1) {
            this.maxBufferSize = Integer.MAX_VALUE;
        } else {
            this.maxBufferSize = maxBufferSize;
        }
        this.networknodeNextHop = networknodeNextHop;
        edge = topologyManager.findEdge(currentNode.getName(), networknodeNextHop.getName());
        name = currentNode.getName() + ": TX buffer - " + networknodeNextHop.getName();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        fragments = new LinkedList<Fragment>();
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

            if (fragment.getReceivedTime() > simulationTime) {//this fragment is not ready to serialise, yet
                if (logg.isDebugEnabled()) {
                    logg.debug("this fragment is not ready to serialise: " + fragment.getReceivedTime() + " " + simulationTime);
                }
                continue;
            }

            if (fragment.getReceivedTime() < serialisationEndTime) {//there is another serialisation going on
                fragment.setReceivedTime(serialisationEndTime);
            } else {
                serialisationEndTime = fragment.getReceivedTime();
            }

            int fragmentSize = QueueingHelper.calculateFragmentSize(fragment.getFragmentNumber(), QueueingHelper.calculateNumberOfFragments(fragment.getOriginalPacket().getPacketSize(), edge.getMtu()), edge.getMtu(), fragment.getOriginalPacket().getPacketSize());
            double serDelay = DelayHelper.calculateSerialisationDelay(fragment.getOriginalPacket(), edge, fragmentSize);
            if (serialisationEndTime + serDelay > simulationTime) { //there is no time left to serialise this packet
                if (logg.isDebugEnabled()) {
                    logg.debug("no time left to serialise this packet " + (serialisationEndTime + serDelay) + " " + simulationTime);
                }
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

    @Override
    public double getUsage() {
        return getFragmentsCount();
    }
}
