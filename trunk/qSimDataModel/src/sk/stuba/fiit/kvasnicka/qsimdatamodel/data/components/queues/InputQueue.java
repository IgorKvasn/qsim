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
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.UsageStatistics;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Igor Kvasnicka
 */
public class InputQueue implements UsageStatistics {
    @Getter
    private List<Packet> inputQueue;
    private int maxSize;

    public InputQueue() {
        this.maxSize = - 1;
        inputQueue = new LinkedList<Packet>();
    }

    @Override
    public int getUsage() {
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

    public void setMaxSize(int maxIntputQueueSize) {
        if (this.maxSize == - 1) {
            this.maxSize = maxIntputQueueSize;
        } else {
            throw new IllegalStateException("max input qeueue size is already set to value: " + maxSize + "; proposed new value is " + maxIntputQueueSize);
        }
    }

    public void addPacket(Packet packet) {
        if (packet == null) throw new IllegalArgumentException("packet is NULL");
        if (maxSize == - 1) throw new IllegalStateException("input queue max size is not set");

        if (inputQueue.size() == maxSize) {
            throw new IllegalStateException("input queue is full - this should be already taken care of");
        }
        inputQueue.add(packet);
    }

    public boolean isAvailable() {
        return inputQueue.size() != maxSize;
    }
}
//
//if (Layer4TypeEnum.TCP == fragment.getOriginalPacket().getLayer4()) {
//           nodeCongested(fragment.getOriginalPacket());
//       }
