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

import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * this class represents software queues in network nodes
 *
 * @author Igor Kvasnicka
 */

public class OutputQueueManager implements Serializable {
    private static final long serialVersionUID = - 2867356876883962827L;
    private HashMap<Integer, OutputQueue> queues; //key=queue number, value = output queue
    private NetworkNode node;


    public OutputQueueManager(List<OutputQueue> outputQueues) {
        if (outputQueues == null) {
            this.queues = new HashMap<Integer, OutputQueue>();
        } else {
            this.queues = new HashMap<Integer, OutputQueue>(outputQueues.size() * 3 / 4);
            for (OutputQueue q : outputQueues) {
                queues.put(q.getQueueNumber(), q);
            }
        }
    }


    public Collection<OutputQueue> getQueues() {
        return queues.values();
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
        if (! queues.containsKey(qosQueue)) return true;

        return queues.get(qosQueue).isAvailable();
    }

    /**
     * retrieves all packets in all queues that are waiting in output queue within given time interval*
     *
     * @param time current simulation time
     * @return returns packets in node's queue that came into queue within specified time interval;there are multiple output queues;
     *         key = queue number; value = list of packets in queue
     */
    public Map<Integer, List<Packet>> getPacketsInOutputQueues(double time) {
        Map<Integer, List<Packet>> map = new HashMap<Integer, List<Packet>>();

        for (OutputQueue outputQueue : queues.values()) {
            List<Packet> oneQueue = new LinkedList<Packet>();

            for (Packet packet : outputQueue.getPackets()) {
                if (packet.getTimeWhenCameToQueue() <= time) {
                    oneQueue.add(packet);
                }
            }
            map.put(outputQueue.getQueueNumber(), oneQueue);
        }
        return map;
    }

    /**
     * returns used capacity of appropriate output queue
     *
     * @param queueNumber queue number
     * @return queue size
     */
    public double getQueueUsedCapacity(int queueNumber) {
        if (! queues.containsKey(queueNumber)) return 0;
        return queues.get(queueNumber).getUsage();
    }

    /**
     * checks if where are no packets in output queue
     *
     * @return
     */
    public boolean isEmpty() {
        for (OutputQueue outputQueue : queues.values()) {
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
        if (! queues.containsKey(p.getQosQueue())) {
            throw new IllegalArgumentException("Invalid queueNumber " + p.getQosQueue());
        }

        queues.get(p.getQosQueue()).removePacket(p);
    }

    /**
     * adds packet to appropriate output queue
     *
     * @param p packet to be added
     */
    public void addPacket(Packet p) {
        if (p.getQosQueue() == - 1) throw new IllegalStateException("packet has not been classified");

        //dynamically create new output queue if needed
        if (! queues.containsKey(p.getQosQueue())) {
            throw new IllegalStateException("output queue not defined for QoS queue: " + p.getQosQueue());
            //queues.put(p.getQosQueue(), new OutputQueue(maxCapacity, this, p.getQosQueue()));
        }

        //retrieve packets in output queue within time this packet arrives
        List<Packet> queue = getPacketsInOutputQueues(p.getSimulationTime()).get(p.getQosQueue());
        if (! node.getQosMechanism().performActiveQueueManagement(queue, p)) {
            //packet should be dropped
            if (p.getLayer4().isRetransmissionEnabled()) {
                node.retransmittPacket(p);
            } else {
                p.getSimulationRule().setCanCreateNewPacket(true); //in case this is a ICMP packet this allows to generate new packet on the src node
            }
        }

        queues.get(p.getQosQueue()).addPacket(p);
    }


    /**
     * returns total number of packets in all output queues
     *
     * @return
     */
    public int getAllUsage() {
        int usage = 0;
        for (OutputQueue queue : queues.values()) {
            usage += queue.getUsage();
        }
        return usage;
    }
}
