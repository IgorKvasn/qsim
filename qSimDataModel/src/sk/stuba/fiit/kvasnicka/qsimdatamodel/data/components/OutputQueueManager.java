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

package sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components;

import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.util.LinkedList;
import java.util.List;

/**
 * this class represents software queues in network nodes
 *
 * @author Igor Kvasnicka
 */

//todo cela trieda ma byt JAXB serializovatelna

public class OutputQueueManager {
    @Getter
    private OutputQueue[] queues;

    /**
     * all packets in output queue
     * if you are looking for QoS queues, they are <b>defined</b> on OutputQueueManager
     */
    @Getter
    private List<Packet> outputQueue;

    public OutputQueueManager(OutputQueue[] queues) {
        this.queues = new OutputQueue[queues.length];
        System.arraycopy(queues, 0, this.queues, 0, queues.length); //http://pmd.sourceforge.net/rules/java/sunsecure.html - ArrayIsStoredDirectly

        //finish output queues initialisation - each queue needs to know it qos number (order)
        for (int i = 0; i < queues.length; i++) {
            queues[i].setQosNumber(i);
            queues[i].setQueueManager(this);
        }

        outputQueue = new LinkedList<Packet>();
    }

    public int getQueueCount() {
        return queues.length;
    }

    /**
     * determines if there is enough space in QoS queue in output queue for this packet
     *
     * @param qosQueue number of qos queue where this packet belongs
     * @return true/false according to description
     */
    public boolean isOutputQueueAvailable(int qosQueue) {
        if (getQueueUsedCapacity(qosQueue, getOutputQueue()) + 1 > getQueueMaxCapacity(qosQueue)) {
            return false;
        }
        return true;
    }

    /**
     * retrieves all packets that are waiting in input buffer within given time interval
     *
     * @param time current simulation time
     * @return returns packets in node's queue that came into queue within specified time interval
     */
    public List<Packet> getPacketsInOutputQueue(double time) {
        List<Packet> list = new LinkedList<Packet>();

        for (Packet packet : outputQueue) {
            if (packet.getTimeWhenCameToQueue() <= time) {
                list.add(packet);
            }
        }
        return list;
    }


    /**
     * returns max size of appropriate output queue
     *
     * @param queueNumber queue number
     * @return queue size
     */
    public int getQueueMaxCapacity(int queueNumber) {
        if (queueNumber > queues.length) {
            throw new IllegalArgumentException("Invalid queueNumber: " + queueNumber);
        }
        OutputQueue queueDefinition = queues[queueNumber];
        return queueDefinition.getMaxCapacity();
    }

    /**
     * returns used capacity of appropriate output queue
     *
     * @param queueNumber queue number
     * @param outputQueue all packets in output queue - regardless of QoS queue number (use NetowrkNode.getOutputQueue() to retrieve these packets)
     * @return queue size
     */
    public int getQueueUsedCapacity(int queueNumber, List<Packet> outputQueue) {
        if (queueNumber >= queues.length) {
            throw new IllegalArgumentException("Invalid queueNumber - max number of QoS queue: " + queues.length + " (max allowed qos number is " + (queues.length - 1) + "), but method argument was queue number " + queueNumber);
        }
        if (queueNumber == - 1) {
            throw new IllegalStateException("This packet has not been marked and classified - QoS queue number is -1 (default value)");
        }
        int size = 0;
        for (Packet p : outputQueue) {
            if (p.getQosQueue() == - 1) throw new IllegalStateException("packet is not marked");
            if (p.getQosQueue() == queueNumber) size++;
        }
        return size;
    }

    /**
     * checks if where are no packets in output queue
     *
     * @return
     */
    public boolean isEmpty() {
        return outputQueue.isEmpty();
    }
}
