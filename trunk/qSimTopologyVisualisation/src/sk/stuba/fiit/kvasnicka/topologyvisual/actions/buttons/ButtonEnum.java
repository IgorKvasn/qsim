/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.actions.buttons;

import sk.stuba.fiit.kvasnicka.topologyvisual.topology.SimulationStateEnum;

/**
 * this enum specifies each button <br/> it is used to determine, what button is
 * to be active in particular simulation state
 *
 * @see SimulationStateEnum
 * @author Igor Kvasnicka
 */
public enum ButtonEnum {

    RUN,
    PAUSE,
    STOP,
    CONFIGURE
}
