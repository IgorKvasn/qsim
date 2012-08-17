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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.OutputQueueManager;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.UsageStatistics;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Igor Kvasnicka
 */
@EqualsAndHashCode
@Getter

public class OutputQueue implements UsageStatistics {

    private int maxCapacity;
    private String queueLabel;
    private int qosNumber = - 1;
    private OutputQueueManager queueManager = null;
    private List<Packet> packets;

    /**
     * creates new QoS queue
     *
     * @param maxCapacity maximum capacity of this queue; all packets above this capacity will be dropped
     * @param queueLabel  user defined label for this queue - e.g.: "high priority"
     */
    public OutputQueue(int maxCapacity, String queueLabel) {
        this.maxCapacity = maxCapacity;
        this.queueLabel = queueLabel;
        this.packets = new LinkedList<Packet>();
    }


    public void setQosNumber(int qosNumber) {
        if (this.qosNumber == - 1) {
            this.qosNumber = qosNumber;
        } else {
            throw new IllegalStateException("QoS number is already set to: " + this.qosNumber + " new QoS number should be " + qosNumber);
        }
    }

    public void setQueueManager(OutputQueueManager queueManager) {
        if (this.queueManager == null) {
            this.queueManager = queueManager;
        } else {
            throw new IllegalStateException("OutputQueueManager is already set");
        }
    }

    /**
     * determines if there is space in output queue for one more packet
     *
     * @return
     */
    public boolean isAvailable() {
        return packets.size() != maxCapacity;
    }

    @Override
    public int getUsage() {
        return packets.size();
    }

    public boolean isEmpty() {
        return packets.isEmpty();
    }

    public void removePacket(Packet p) {
        if (! packets.remove(p)) {
            throw new IllegalStateException("Could not find packet in output queue - it cannot be deleted");
        }
    }

    public void addPacket(Packet p) {
        if (packets.size() == maxCapacity) {
            throw new IllegalStateException("output queue is already full - this should be taken care of, already");
        }
        packets.add(p);
    }
}
