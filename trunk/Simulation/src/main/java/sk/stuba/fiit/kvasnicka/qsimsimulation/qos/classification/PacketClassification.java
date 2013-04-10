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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification;

import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;

import java.util.HashMap;

/**
 * @author Igor Kvasnicka
 */
public abstract class PacketClassification implements QosMechanism {
    private static final long serialVersionUID = 5049685251835322761L;
    @Getter
    protected HashMap<String, Object> parameters;

    protected PacketClassification(HashMap<String, Object> parameters) {
        this.parameters = parameters;
    }

    protected PacketClassification() {

    }


    public abstract int classifyAndMarkPacket(NetworkNode networkNode, Packet packet);

    /**
     * identifies, if this QoS mechanism depends on some parameters that must be provided to properly configure mechanism
     *
     * @return
     */
    @Override
    public boolean hasParameters() {
        return parameters != null;
    }

    public enum Available {
        BEST_EFFORT(false),
        DSCP(true),
        FLOW_BASED(false),
        IP_PRECEDENCE(false),
        NONE(false);
        private boolean parameters;

        private Available(boolean hasParameters) {
            this.parameters = hasParameters;
        }

        public boolean hasParameters() {
            return parameters;
        }
    }
}
