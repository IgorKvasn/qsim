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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos;

import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.OutputQueueManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.util.List;

/**
 * here all the QoS stuff happens
 *
 * @author Igor Kvasnicka
 */


//todo urobit serializaciu do jaxb rovnako ako funguje netowrk node a router/switch/computer - @XmlSeeAlso


public interface QosMechanism {
    /**
     * marks given packet according to NetworkNode rules
     *
     * @param packet packet to mark
     * @return number of the QoS queue where this packet belongs
     * @see sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet#qosQueue here will be return value stored
     */

    int classifyAndMarkPacket(Packet packet);

    /**
     * decides which packets should be moved from output queue
     *
     * @param outputQueuePackets all packets in all output queues (it does not matter what priority ar what queue are they in)
     * @param swQueues           definition of QoS queues
     * @return list of packets to send
     */
    List<Packet> decitePacketsToMoveFromOutputQueue(List<Packet> outputQueuePackets, OutputQueueManager swQueues);
}
