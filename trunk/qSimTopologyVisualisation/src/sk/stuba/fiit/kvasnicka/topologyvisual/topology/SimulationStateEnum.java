/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public enum SimulationStateEnum {

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
            return EnumSet.of(ButtonEnum.STOP, ButtonEnum.PAUSE);
        }
    },
    /**
     * simulation is temporary paused
     */
    PAUSED {

        @Override
        public Set<ButtonEnum> getButtonsEnabled() {
            return EnumSet.of(ButtonEnum.RUN, ButtonEnum.STOP);
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
