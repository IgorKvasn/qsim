/*
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
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.actions.buttons;

import sk.stuba.fiit.kvasnicka.topologyvisual.topology.TopologyStateEnum;

/**
 * this enum specifies each button <br/> it is used to determine, what button is
 * to be active in particular simulation state
 *
 * @see TopologyStateEnum
 * @author Igor Kvasnicka
 */
public enum ButtonEnum {

    RUN,
    PAUSE,
    STOP,
    CONFIGURE,
    NODE_STATS
}