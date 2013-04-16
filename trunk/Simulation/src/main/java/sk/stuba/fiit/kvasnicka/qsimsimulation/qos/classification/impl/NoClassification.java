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
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.PacketClassification;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp.DscpValuesEnum;

import java.util.Collections;
import java.util.List;

/**
 * uses previously classified packet settings
 *
 * @author Igor Kvasnicka
 */
public class NoClassification extends PacketClassification {


    private static final long serialVersionUID = - 1182135936949875862L;

    @Override
    public int classifyAndMarkPacket(NetworkNode networkNode, Packet packet) {
        if (packet.getQosQueue() == - 1) {
            return 0;
        }
        return packet.getQosQueue();
    }

    @Override
    public List<Integer> convertClassificationToQueue(List<IpPrecedence> ipPrecedenceList, List<DscpValuesEnum> dscpValuesEnums) {
        return Collections.<Integer>emptyList();
    }
}
