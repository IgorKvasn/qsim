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

package sk.stuba.fiit.kvasnicka.qsimsimulation.events.timer;

import lombok.Getter;

import java.util.EventObject;

/**
 * this event occurs at the end of simulation timer tick
 * event contains information about simulation time that just passed
 *
 * @author Igor Kvasnicka
 */
public class SimulationTimerEvent extends EventObject {
    @Getter
    private double simulationTime;

    /**
     * Constructs a prototypical Event.
     *
     * @param source         The object on which the Event initially occurred.
     * @param simulationTime current simulation time
     * @throws IllegalArgumentException if source is null.
     */
    public SimulationTimerEvent(Object source, double simulationTime) {
        super(source);
        this.simulationTime = simulationTime;
    }
}