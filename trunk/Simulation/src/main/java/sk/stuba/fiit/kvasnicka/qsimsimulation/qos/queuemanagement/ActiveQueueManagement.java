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

import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Igor Kvasnicka
 */
public abstract class ActiveQueueManagement {
    protected Map<String, Object> parameters = new HashMap<String, Object>();

    public ActiveQueueManagement() {
    }

    public ActiveQueueManagement(Map<String, Object> parameters) {
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
}
