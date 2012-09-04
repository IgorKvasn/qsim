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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.impl;

import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.PacketClassificationInterf;

/**
 * @author Igor Kvasnicka
 */
public class DscpClassification implements PacketClassificationInterf {

    private static final long serialVersionUID = 7683972655898905067L;

    @Override
    public int classifyAndMarkPacket(NetworkNode networkNode, Packet packet) {
        if (networkNode == null) {
            throw new IllegalArgumentException("network node is NULL");
        }
        if (packet == null) {
            throw new IllegalArgumentException("packet is NULL");
        }
        if (networkNode.getDscpManager() == null) {
            throw new IllegalStateException("network node: " + networkNode.getName() + "is using DSCP classification, but no DSCP definitions have been made");
        }

        return networkNode.getDscpManager().determineMarkingByDscpDefinitions(packet);
    }
}
