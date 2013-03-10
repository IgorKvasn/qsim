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

import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

/**
 * @author Igo
 */
public abstract class PacketCreationDelayFunction {

    protected double maxDelay;
    protected double functionLength;

    protected PacketCreationDelayFunction(double maxDelay, double functionLength) {
        this.maxDelay = maxDelay;
        this.functionLength = functionLength;
    }

    /**
     * calculates packet creation delay
     * @return
     */
    public abstract double calculateDelay(SimulationRuleBean rule, double simulationTime);
}
