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

import lombok.Setter;
import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.PacketClassificationInterf;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.queuemanagement.ActiveQueueManagement;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.PacketScheduling;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * here all the QoS stuff happens
 *
 * @author Igor Kvasnicka
 */


public class QosMechanism implements Serializable {
    private static Logger logg = Logger.getLogger(QosMechanism.class);
    private static final long serialVersionUID = - 2761566613214031952L;
    @Setter
    private PacketScheduling packetScheduling;
    @Setter
    private PacketClassificationInterf packetClassification;
    @Setter
    private ActiveQueueManagement activeQueueManagement;

    public QosMechanism(PacketScheduling packetScheduling, PacketClassificationInterf packetClassification, ActiveQueueManagement activeQueueManagement) {
        this.packetScheduling = packetScheduling;
        this.packetClassification = packetClassification;
        this.activeQueueManagement = activeQueueManagement;
    }

    /**
     * marks given packet according to NetworkNode rules
     *
     * @param networkNode network node that is calling this method
     * @param packet      packet to mark
     * @return number of the QoS queue where this packet belongs
     * @see sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet#qosQueue here will be return value stored
     */

    public int classifyAndMarkPacket(NetworkNode networkNode, Packet packet) {
        logg.debug("classifyAndMarkPacket - node " + networkNode.getName());

        if (packetClassification == null) throw new IllegalStateException("packetClassification is NULL");
        return packetClassification.classifyAndMarkPacket(networkNode, packet);
    }

    /**
     * decides which packets should be moved from output queue
     * it does not remove packets from output queue!!!
     *
     * @param networkNode        network node that is calling this method
     * @param outputQueuePackets all packets in all output queues (it does not matter what priority ar what queue are they in)
     * @return list of packets to send
     */
    public List<Packet> decitePacketsToMoveFromOutputQueue(NetworkNode networkNode, Map<Integer, List<Packet>> outputQueuePackets) {
        logg.debug("decitePacketsToMoveFromOutputQueue - " + networkNode.getName());

        if (packetScheduling == null) throw new IllegalStateException("packetScheduling is NULL");
        return packetScheduling.decitePacketsToMoveFromOutputQueue(networkNode, outputQueuePackets);
    }

    /**
     * performs active queue management over specified output queue
     * this method determines whether packet can be added to the queue
     *
     * @param queue     single output queue
     * @param newPacket packet to be added
     * @return true if packet can be added into queue; false if it should be dropped
     */
    public boolean performActiveQueueManagement(List<Packet> queue, Packet newPacket) {
        logg.debug("performActiveQueueManagement");

        if (activeQueueManagement == null) throw new IllegalStateException("activeQueueManagement is NULL");
        if (newPacket == null) throw new IllegalStateException("newPacket is NULL");
        return activeQueueManagement.manageQueue(queue, newPacket);
    }
}
