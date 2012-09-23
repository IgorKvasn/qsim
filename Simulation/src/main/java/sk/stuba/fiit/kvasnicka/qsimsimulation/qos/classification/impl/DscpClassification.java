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

import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.PacketClassification;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp.DscpManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.ParameterException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.QosUtils;

import java.util.HashMap;

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

    /**
     * for a given queue number, finds DSCP value (AF11,AF12,EF,..)
     *
     * @param queueNumber
     * @return
     * @throws ArrayIndexOutOfBoundsException if queueNumber is below 0 or higher than max defined value
     */
    public static DscpValuesEnum findDscpValueByQueueNumber(int queueNumber) {
        if (queueNumber < 0) throw new ArrayIndexOutOfBoundsException("invalid queueNumber number: " + queueNumber);
        for (DscpValuesEnum dscpValuesEnum : DscpValuesEnum.values()) {
            if (dscpValuesEnum.getQosQueue() == queueNumber) {
                return dscpValuesEnum;
            }
        }
        throw new ArrayIndexOutOfBoundsException("invalid queueNumber number: " + queueNumber);
    }

    public enum DscpValuesEnum {
        BEST_EFFORT(0) {
            @Override
            public String getTextName() {
                return "Best effort";
            }
        },
        AF11(1),
        AF12(2),
        AF13(3),
        AF21(4),
        AF22(5),
        AF23(6),
        AF31(7),
        AF32(8),
        AF33(9),
        AF41(10),
        AF42(11),
        AF43(12),
        EF(13);
        @Getter
        private int qosQueue;

        private DscpValuesEnum(int qosQueue) {
            this.qosQueue = qosQueue;
        }

        public String getTextName() {
            return toString();
        }
    }
}
