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
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp.DscpManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp.DscpValuesEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.ParameterException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.QosUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Igor Kvasnicka
 */
public class DscpClassification extends PacketClassification {

    private static final long serialVersionUID = 7683972655898905067L;
    public static final String DSCP_DEFINITIONS = "dscp_definitions";

    public DscpClassification(HashMap<String, Object> parameters) {
        super(parameters);
        try {
            QosUtils.checkParameter(parameters, DscpManager.class, DSCP_DEFINITIONS);
        } catch (ParameterException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int classifyAndMarkPacket(NetworkNode networkNode, Packet packet) {
        if (networkNode == null) {
            throw new IllegalArgumentException("network node is NULL");
        }
        if (packet == null) {
            throw new IllegalArgumentException("packet is NULL");
        }
        DscpManager dscpManager = (DscpManager) parameters.get(DSCP_DEFINITIONS);
        return dscpManager.determineMarkingByDscpDefinitions(packet);
    }

    @Override
    public List<Integer> convertClassificationToQueue(List<IpPrecedence> ipPrecedenceList, List<DscpValuesEnum> dscpValuesEnums) {
        List<Integer> result = new LinkedList<Integer>();
        DscpManager dscpManager = (DscpManager) parameters.get(DSCP_DEFINITIONS);
        for (DscpValuesEnum dscp : dscpValuesEnums) {
            try {
                result.add(dscpManager.convertDscpToQueue(dscp));
            } catch (Exception e) {
                result.add(0);
            }
        }
        return result;
    }

//    /**
//     * for a given queue number, finds DSCP value (AF11,AF12,EF,..)
//     *
//     * @param queueNumber
//     * @return
//     * @throws ArrayIndexOutOfBoundsException if queueNumber is below 0 or higher than max defined value
//     */
//    public static DscpValuesEnum findDscpValueByQueueNumber(int queueNumber) {
//        if (queueNumber < 0) throw new ArrayIndexOutOfBoundsException("invalid queueNumber number: " + queueNumber);
//        for (DscpValuesEnum dscpValuesEnum : DscpValuesEnum.values()) {
//            if (dscpValuesEnum.getQosQueue() == queueNumber) {
//                return dscpValuesEnum;
//            }
//        }
//        throw new ArrayIndexOutOfBoundsException("invalid queueNumber number: " + queueNumber);
//    }
}
