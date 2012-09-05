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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Igor Kvasnicka
 */
@EqualsAndHashCode
@Getter
public class OutputQueue implements UsageStatistics, Serializable {
    private static final long serialVersionUID = - 7880238363156637629L;
    private int maxCapacity;
    private int queueNumber;

    private OutputQueueManager queueManager;

    private transient List<Packet> packets;


    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        packets = new LinkedList<Packet>();
    }


    /**
     * creates new QoS queue
     *
     * @param maxCapacity  maximum capacity of this queue; all packets above this capacity will be dropped
     * @param queueManager output queue manager that owns this queue
     */
    public OutputQueue(int maxCapacity, OutputQueueManager queueManager, int queueNumber) {
        this.maxCapacity = maxCapacity;
        this.queueManager = queueManager;
        this.queueNumber = queueNumber;
        this.packets = new LinkedList<Packet>();
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
