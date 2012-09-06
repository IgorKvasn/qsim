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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Fair-Queuing
 *
 * @author Igor Kvasnicka
 */
public class RoundRobinScheduling extends PacketScheduling {
    private static final long serialVersionUID = 8606581356591929660L;
    private transient int currentQueue = 0;

    @Override
    public List<Packet> decitePacketsToMoveFromOutputQueue(NetworkNode networkNode, Map<Integer, List<Packet>> outputQueuePackets) {
        if (outputQueuePackets == null) throw new IllegalArgumentException("outputQueuePackets is NULL");

        if (outputQueuePackets.isEmpty()) {//there are no output queues
            return Collections.emptyList();
        }

        List<Packet> packets = new LinkedList<Packet>();
        int packetNumber = 0;
        int numberOfQueues = outputQueuePackets.size();
        int inactiveQueue = 0;
        int startQueue = currentQueue;

        while (true) {

            List<Packet> queue = outputQueuePackets.get(currentQueue);

            if (queue.size() <= packetNumber) {       //this queue has no more packets left
                inactiveQueue++;
            } else {
                packets.add(queue.get(packetNumber));
            }

            currentQueue++;
            currentQueue %= numberOfQueues;
            if (currentQueue == startQueue) {//I have performed one round robin circle

                if (inactiveQueue == numberOfQueues) {
                    break;//there are no more packets in output queue
                } else {
                    inactiveQueue = 0;
                }

                packetNumber++;
            }
        }

        return packets;
    }
}
