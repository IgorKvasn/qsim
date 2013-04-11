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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling;

import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Igor Kvasnicka
 */
public abstract class PacketScheduling implements QosMechanism {
    private static final long serialVersionUID = - 7724658919812873308L;
    @Getter
    protected HashMap<String, Object> parameters;

    public PacketScheduling() {
    }

    public PacketScheduling(HashMap<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean hasParameters() {
        return parameters != null;
    }

    /**
     * decides ORDER of packets that will be moved from output queue to TX buffer
     * note that this method DOES NOT remove packets from output queue
     *
     * @param networkNode
     * @param outputQueuePackets map of packets in output queues; key = output queue number; value = list of packets
     * @return
     */
    public abstract List<Packet> decitePacketsToMoveFromOutputQueue(NetworkNode networkNode, Map<Integer, List<Packet>> outputQueuePackets);


    public enum Available {
        WFQ(false, true),
        FIFO(false, false),
        PRIORITY_QUEUEING(false, false),
        ROUND_ROBIN(false, false),
        WEIGHTED_ROUND_ROBIN(true, false);

        private boolean parameters;
        @Getter
        private boolean flowBased;

        private Available(boolean hasParameters, boolean flowBased) {
            this.parameters = hasParameters;
            this.flowBased = flowBased;
        }

        public boolean hasParameters() {
            return parameters;
        }
    }
}
