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
package sk.stuba.fiit.kvasnicka.topologyvisual.topology;

import java.util.EnumSet;
import java.util.Set;
import sk.stuba.fiit.kvasnicka.topologyvisual.actions.buttons.ButtonEnum;

/**
 * describes in what state simulation currently is<br/>it also contains
 * information about toolbar button that should be <b>enabled</b> in particular
 * state
 *
 * @see ButtonEnum
 * @author Igor Kvasnicka
 */
public enum TopologyStateEnum {

    /**
     * no particular state - simulation has not been started or it is stopped
     */
    NOTHING {
        @Override
        public Set<ButtonEnum> getButtonsEnabled() {
            return EnumSet.of(ButtonEnum.CONFIGURE, ButtonEnum.RUN);
        }
    },
    /**
     * simulation is running
     */
    RUN {
        @Override
        public Set<ButtonEnum> getButtonsEnabled() {
            return EnumSet.of(ButtonEnum.STOP, ButtonEnum.PAUSE, ButtonEnum.NODE_STATS, ButtonEnum.SPEED, ButtonEnum.DROP_RATE);
        }
    },
    /**
     * simulation is temporary paused
     */
    PAUSED {
        @Override
        public Set<ButtonEnum> getButtonsEnabled() {
            return EnumSet.of(ButtonEnum.RUN, ButtonEnum.STOP, ButtonEnum.SPEED, ButtonEnum.DROP_RATE);
        }
    };

    /**
     * returns all buttons that should be enabled in this state
     *
     * @return
     */
    public abstract Set<ButtonEnum> getButtonsEnabled();

    public boolean isButtonEnabled(ButtonEnum button) {
        return getButtonsEnabled().contains(button);
    }
}
