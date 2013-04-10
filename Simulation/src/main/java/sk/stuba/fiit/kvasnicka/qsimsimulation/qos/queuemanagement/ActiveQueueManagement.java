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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.queuemanagement;

import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanism;

import java.util.HashMap;
import java.util.List;

/**
 * @author Igor Kvasnicka
 */
public abstract class ActiveQueueManagement implements QosMechanism {

    private static final long serialVersionUID = - 876488938244873514L;
    @Getter
    private HashMap<String, Object> parameters;

    public ActiveQueueManagement() {
    }

    public ActiveQueueManagement(HashMap<String, Object> parameters) {
        this.parameters = parameters;
    }

    public void setParameters(HashMap<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * active management for a single output queue
     * this method determines whether packet can be added to the queue
     *
     * @param queue     single output queue
     * @param newPacket packet to be added into queue
     */
    public abstract boolean manageQueue(List<Packet> queue, Packet newPacket);

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
        RED(true),
        WRED(true),
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
