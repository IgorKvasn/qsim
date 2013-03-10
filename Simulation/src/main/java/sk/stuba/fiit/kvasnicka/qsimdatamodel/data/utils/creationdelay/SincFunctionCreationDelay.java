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

package sk.stuba.fiit.kvasnicka.qsimdatamodel.data.utils.creationdelay;

import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.utils.PacketCreationDelayFunction;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

/**
 * sinc(x) = 1                     if x = 0,
 * sinc(x) = sin(x) / x            otherwise
 * <p/>
 * the result of the sinc(x) is is multiplied by "maxDelay" argument
 *
 * @author Igo
 */
public class SincFunctionCreationDelay extends PacketCreationDelayFunction {

    public SincFunctionCreationDelay(double maxDelay, double period) {
        super(maxDelay, period);
    }

    @Override
    public double calculateDelay(SimulationRuleBean rule, double simulationTime) {
        double time = simulationTime % period;
        if (time == 0) return maxDelay;
        return (Math.sin(time) / time) * maxDelay;
    }
}
