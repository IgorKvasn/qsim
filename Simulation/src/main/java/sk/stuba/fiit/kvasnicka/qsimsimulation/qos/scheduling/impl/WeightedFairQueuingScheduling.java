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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl;

import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.PacketScheduling;

import java.util.LinkedList;
import java.util.List;

/**
 * to put all the theory in one sentence: smaller packets has got higher priority
 *
 * @author Igor Kvasnicka
 */
public class WeightedFairQueuingScheduling extends PacketScheduling {


    @Override
    public List<Packet> decitePacketsToMoveFromOutputQueue(NetworkNode networkNode, List<List<Packet>> outputQueuePackets) {
        if (outputQueuePackets == null) throw new IllegalArgumentException("outputQueuePackets is NULL");

        if (outputQueuePackets.isEmpty()) {//there are no output queues??? are you serious????
            throw new IllegalStateException("no output queues defined - cannot perform packet scheduling");
        }

        List<Packet> result = new LinkedList<Packet>();
        List<List<Packet>> outputQueuePacketsCopy = outputQueueMakeCopy(outputQueuePackets);
        List<Packet> firstPackets = new LinkedList<Packet>();//here are first packet from all queues

        for (; ; ) {
            for (List<Packet> queue : outputQueuePacketsCopy) {
                if (queue.isEmpty()) {//no more packets in this queue
                    continue;
                }
                firstPackets.add(queue.get(0));
            }

            if (firstPackets.isEmpty()) {//there are no more packets in output queues
                break;
            }
            List<Packet> toSchedule = findSmallestPacket(firstPackets);

            removePackets(toSchedule, outputQueuePacketsCopy);//remove them from output queue so they no longer will be first
            result.addAll(toSchedule);
            firstPackets.clear();
        }

        return result;
    }

    /**
     * packets will be removed from output queues (note, that this is just a copy of output queue)
     *
     * @param toSchedule
     * @param outputQueue
     */
    private void removePackets(List<Packet> toSchedule, List<List<Packet>> outputQueue) {
        for (Packet p : toSchedule) {
            if (p.getQosQueue() == - 1) {
                throw new IllegalStateException("packet has not been marked - how is this possible????");
            }
            if (! outputQueue.get(p.getQosQueue()).remove(p)) {
                throw new IllegalStateException("unable to remove packet from output queue - it was not found there");
            }
        }
    }

    /**
     * finds packet that has the smallest size
     * however there can be multiple packets with the same, smallest size - this method will return all of these packets
     *
     * @param packets
     * @return
     */
    private List<Packet> findSmallestPacket(List<Packet> packets) {
        List<Packet> smallest = new LinkedList<Packet>();
        for (Packet p : packets) {
            if (smallest.isEmpty()) {
                smallest.add(p);
                continue;
            }

            if (smallest.get(0).getPacketSize() == p.getPacketSize()) {//another packet with this (smallest) size
                smallest.add(p);
            }
            if (smallest.get(0).getPacketSize() > p.getPacketSize()) {//I have found smaller packet
                smallest.clear();
                smallest.add(p);
            }
        }
        return smallest;
    }

    private List<List<Packet>> outputQueueMakeCopy(List<List<Packet>> outputQueuePackets) {
        List<List<Packet>> result = new LinkedList<List<Packet>>();
        for (List<Packet> q : outputQueuePackets) {
            List<Packet> list = new LinkedList<Packet>();
            list.addAll(q);
            result.add(list);
        }
        return result;
    }
}
