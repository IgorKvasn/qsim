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

package sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues;

import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.UsageStatistics;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Igor Kvasnicka
 */
public class InputQueue implements UsageStatistics, Serializable {
    private static final long serialVersionUID = 6065064185201761330L;
    @Getter
    private transient List<Packet> inputQueue;
    private int maxSize = - 1;
    private NetworkNode node;

    public InputQueue(int maxSize, NetworkNode networkNode) {
        this.maxSize = maxSize;
        this.node = networkNode;
        inputQueue = new LinkedList<Packet>();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        inputQueue = new LinkedList<Packet>();
    }


    @Override
    public double getUsage() {
        return inputQueue.size();
    }

    /**
     * checks if there are no packets in input queue
     *
     * @return
     */
    public boolean isEmpty() {
        return inputQueue.isEmpty();
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void addPacket(Packet packet) {
        if (packet == null) throw new IllegalArgumentException("packet is NULL");
        if (maxSize == - 1) throw new IllegalStateException("input queue max size is not set");

        if (inputQueue.size() == maxSize) {
            throw new IllegalStateException("input queue is full - this should be already taken care of");
        }

        if (! node.getQosMechanism().performActiveQueueManagement(inputQueue, packet)) {
            //packet should be dropped
            if (packet.getLayer4().isRetransmissionEnabled()) {
                node.retransmittPacket(packet);
            } else {
                packet.getSimulationRule().setCanCreateNewPacket(true); //in case this is a ICMP packet this allows to generate new packet on the src node
            }
        }

        inputQueue.add(packet);
    }

    public boolean isAvailable() {
        return inputQueue.size() != maxSize;
    }
}