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
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * this class represents software queues in network nodes
 *
 * @author Igor Kvasnicka
 */

public class OutputQueueManager implements Serializable {
    @Getter
    private List<OutputQueue> queues;
    private NetworkNode node;


    public OutputQueueManager(OutputQueue[] queues) {
        this.queues = Arrays.asList(queues);

        //finish output queues initialisation - each queue needs to know it qos number (order)
        for (int i = 0; i < queues.length; i++) {
            queues[i].setQosNumber(i);
            queues[i].initQueueManager(this);
        }
    }

    /**
     * when creating new output queue manager object
     * this will create parent-reference to NetworkNode that owns this manager
     *
     * @param node
     */
    public void initNode(NetworkNode node) {
        if (this.node == null) {
            this.node = node;
        } else {
            throw new IllegalStateException("network node for this OutputQueueManager has been already set to value: " + this.node.getName() + " and you are setting it to: " + node.getName());
        }
    }

    public NetworkNode getNode() {
        return node;
    }

    public int getQueueCount() {
        return queues.size();
    }

    /**
     * determines if there is enough space in QoS queue in output queue for this packet
     *
     * @param qosQueue number of qos queue where this packet belongs
     * @return true/false according to description
     */
    public boolean isOutputQueueAvailable(int qosQueue) {
        checkQosQueueNumberOk(qosQueue);

        return queues.get(qosQueue).isAvailable();
    }

    /**
     * retrieves all packets in all queues that are waiting in output queue within given time interval
     *
     * @param time current simulation time
     * @return returns packets in node's queue that came into queue within specified time interval;there are multiple output queues; result.get(1) will return list of packets in 2nd queue
     */
    public List<List<Packet>> getPacketsInOutputQueue(double time) {
        List<List<Packet>> list = new LinkedList<List<Packet>>();

        for (OutputQueue outputQueue : queues) {
            List<Packet> oneQueue = new LinkedList<Packet>();

            for (Packet packet : outputQueue.getPackets()) {
                if (packet.getTimeWhenCameToQueue() <= time) {
                    oneQueue.add(packet);
                }
            }
            list.add(oneQueue);
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
        checkQosQueueNumberOk(queueNumber);

        return queues.get(queueNumber).getMaxCapacity();
    }

    /**
     * returns used capacity of appropriate output queue
     *
     * @param queueNumber queue number
     * @return queue size
     */
    public int getQueueUsedCapacity(int queueNumber) {
        checkQosQueueNumberOk(queueNumber);

        return queues.get(queueNumber).getUsage();
    }

    /**
     * checks if where are no packets in output queue
     *
     * @return
     */
    public boolean isEmpty() {
        for (OutputQueue outputQueue : queues) {
            if (! outputQueue.isEmpty()) return false;
        }
        return true;
    }

    /**
     * removes packet from output queue
     *
     * @param p
     */
    public void removePacket(Packet p) {
        checkQosQueueNumberOk(p.getQosQueue());

        queues.get(p.getQosQueue()).removePacket(p);
    }

    /**
     * adds packet to appropriate output queue
     *
     * @param p
     */
    public void addPacket(Packet p) {
        checkQosQueueNumberOk(p.getQosQueue());


        //retrieve packets in output queue within time this packet arrives
        List<Packet> queue = getPacketsInOutputQueue(p.getSimulationTime()).get(p.getQosQueue());
        node.getQosMechanism().performActiveQueueManagement(queue, p);

        queues.get(p.getQosQueue()).addPacket(p);
    }

    private void checkQosQueueNumberOk(int qosNumber) {         //todo test
        if (qosNumber < 0) {
            throw new IllegalStateException("qos number is negative: " + qosNumber);
        }
        if (qosNumber >= queues.size()) {
            throw new IllegalArgumentException("Invalid queueNumber - max number of QoS queue: " + queues.size() + " (max allowed qos number is " + (queues.size() - 1) + "), but method argument was queue number " + qosNumber);
        }
    }

    /**
     * returns total number of packets in all output queues
     *
     * @return
     */
    public int getAllUsage() {
        int usage = 0;
        for (OutputQueue queue : queues) {
            usage += queue.getUsage();
        }
        return usage;
    }
}
