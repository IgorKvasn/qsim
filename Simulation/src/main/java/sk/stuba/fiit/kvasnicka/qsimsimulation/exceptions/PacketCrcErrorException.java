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

package sk.stuba.fiit.kvasnicka.qsimsimulation.exceptions;

import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

/**
 * received packet has got a wrong CRC - it will be dropped and maybe a request for retransmission
 *
 * @author Igor Kvasnicka
 */
public class PacketCrcErrorException extends Exception {
    @Getter
    private transient Packet packet;

    /**
     * creates new exception of this type
     *
     * @param packet packet that is wrong
     */
    public PacketCrcErrorException(Packet packet) {
        super();
        this.packet = packet;
    }
}