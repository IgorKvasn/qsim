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

package sk.stuba.fiit.kvasnicka.qsimsimulation.events.ping;

import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.util.EventObject;

/**
 * @author Igor Kvasnicka
 */
public class PingPacketDeliveredEvent extends EventObject {
    @Getter
    private transient Packet packet;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public PingPacketDeliveredEvent(Object source, Packet packet) {
        super(source);
        this.packet = packet;
    }
}
