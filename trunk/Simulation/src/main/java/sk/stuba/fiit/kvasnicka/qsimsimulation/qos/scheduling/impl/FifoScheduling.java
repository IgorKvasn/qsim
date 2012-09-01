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

import java.util.List;

/**
 * can be applied only if there is only one queue
 *
 * @author Igor Kvasnicka
 */
public class FifoScheduling extends PacketScheduling {

    private static final long serialVersionUID = - 5010671441508269651L;

    public FifoScheduling() {
    }

    @Override
    public List<Packet> decitePacketsToMoveFromOutputQueue(NetworkNode node, List<List<Packet>> outputQueuePackets) {
        if (node == null) throw new IllegalArgumentException("node is NULL");
        if (outputQueuePackets == null) throw new IllegalArgumentException("outputQueuePackets is NULL");

        if (outputQueuePackets.isEmpty()) {//there are no output queues??? are you serious????
            throw new IllegalStateException("no output queues defined - cannot perform packet scheduling");
        }

        if (outputQueuePackets.size() != 1) {         //there must be only one queue, otherwise FIFI scheduling cannot be applied
            throw new IllegalStateException("FIFO scheduling can be applied only if network node has only 1 output queue, network node: " + node.getName() + " has " + outputQueuePackets.size() + " queues");
        }
        return outputQueuePackets.get(0);
    }
}
