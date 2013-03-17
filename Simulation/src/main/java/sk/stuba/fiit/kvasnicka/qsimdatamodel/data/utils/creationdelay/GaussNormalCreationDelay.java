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

import lombok.Getter;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.utils.PacketCreationDelayFunction;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

/**
 * @author Igo
 */
public class GaussNormalCreationDelay extends PacketCreationDelayFunction {
    private static final long serialVersionUID = - 8806656614913165161L;
    private transient NormalDistribution distribution;
    @Getter
    private double mean;
    @Getter
    private double standardDistribution;

    /**
     * recommended values:
     * mean = 0.0
     * standard deviation = 1.0
     */
    public GaussNormalCreationDelay(double maxDelay, double functionLength, double mean, double standardDistribution) {
        super(maxDelay, functionLength);
        this.mean = mean;
        this.standardDistribution = standardDistribution;
        distribution = new NormalDistributionImpl(mean, standardDistribution);
    }


    private void readObject(java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException {
        stream.defaultReadObject();

        distribution = new NormalDistributionImpl(mean, standardDistribution);
    }


    @Override
    public double calculateDelay(SimulationRuleBean rule, double simulationTime) {
        double time = simulationTime % period;
        return distribution.density(time) * maxDelay * Math.sqrt(2 * Math.PI); //it must be doubled by sqrt(2*pi), because max value of Gauss's normal distribution is 1/sqrt(2*pi)
    }
}
