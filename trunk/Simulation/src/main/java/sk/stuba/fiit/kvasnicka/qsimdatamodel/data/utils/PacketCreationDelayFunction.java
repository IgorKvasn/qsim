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

package sk.stuba.fiit.kvasnicka.qsimdatamodel.data.utils;

import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.io.Serializable;

/**
 * @author Igo
 */
public abstract class PacketCreationDelayFunction implements Serializable {
    private static final long serialVersionUID = - 8172630266705558036L;

   @Getter
    protected double maxDelay;
    @Getter
    protected double period;

    protected PacketCreationDelayFunction(double maxDelay, double period) {
        if (maxDelay<0) throw new IllegalArgumentException("maxDelay must not be negative");
        if (period <= 0) throw new IllegalArgumentException("period must not be less or equal to 0");
        this.maxDelay = maxDelay;
        this.period = period;
    }

    /**
     * calculates packet creation delay
     * when you want to create function that gives repetitive results in time, it is advised to perform (simulationTime % period)
     * @return
     */
    public abstract double calculateDelay(SimulationRuleBean rule, double simulationTime);
}
