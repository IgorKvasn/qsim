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
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.ParameterException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.QosUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Igor Kvasnicka
 */
public class WeightedRoundRobinScheduling extends PacketScheduling {

    private static final long serialVersionUID = - 4165594410407914293L;

    private transient int currentQueue;//currently processed queue
    public static final String QUEUES_WEIGHT = "queues_weight";

    public WeightedRoundRobinScheduling(HashMap<String, Object> parameters) {
        super(parameters);
        try {
            QosUtils.checkParameter(parameters, int[].class, QUEUES_WEIGHT);
            int[] queueWeights = (int[]) parameters.get(QUEUES_WEIGHT);

            for (int q : queueWeights) {
                if (q <= 0) throw new ParameterException("One of the queue weights is negative or zero.");
            }
        } catch (ParameterException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public List<Packet> decitePacketsToMoveFromOutputQueue(NetworkNode networkNode, Map<Integer, List<Packet>> outputQueuePackets) {
        if (outputQueuePackets == null) throw new IllegalArgumentException("outputQueuePackets is NULL");

        if (outputQueuePackets.isEmpty()) {//there are no output queues
            return Collections.emptyList();
        }


        int[] queueWeights = (int[]) parameters.get(QUEUES_WEIGHT);

        List<Packet> packets = new LinkedList<Packet>();
        int numberOfQueues = outputQueuePackets.size();
        int inactiveQueue = 0;
        int startQueue = currentQueue;
        int[] lastPacket = new int[queueWeights.length];//index of last processed packet in each queue
        Arrays.fill(lastPacket, 0);//init

        while (true) {

            List<Packet> queue = outputQueuePackets.get(currentQueue);

            if (queue.size() <= lastPacket[currentQueue]) {       //this queue has no more packets left
                inactiveQueue++;
            } else {
                for (int i = 0; i < queueWeights[currentQueue]; i++) {
                    if (queue.size() <= lastPacket[currentQueue]) {
                        break;//queue is empty
                    }
                    try {
                        packets.add(queue.get(lastPacket[currentQueue]));
                        lastPacket[currentQueue]++;
                    } catch (Exception e) {
                        break;//this should not happen - it should be taken care of in previous if-statement
                    }
                }
            }

            currentQueue++;
            currentQueue %= numberOfQueues;
            if (currentQueue == startQueue) {//I have performed one round robin circle

                if (inactiveQueue == numberOfQueues) {
                    break;//there are no more packets in output queue
                } else {
                    inactiveQueue = 0;
                }

            }
        }

        return packets;
    }
}
