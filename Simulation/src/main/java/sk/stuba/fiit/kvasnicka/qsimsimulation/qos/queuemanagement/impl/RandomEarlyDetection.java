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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.queuemanagement.impl;

import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.queuemanagement.ActiveQueueManagement;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Igor Kvasnicka
 */
public class RandomEarlyDetection extends ActiveQueueManagement {

    public static final String EXPONENTIAL_WEIGHT_FACTOR = "exponential_weight_factor";
    private static final long serialVersionUID = 1566117576697050626L;
    /**
     * map of previous average queue sizes
     * key = queue number (packet.getQosQueue)
     * value = average queue size
     */
    private transient Map<Integer, Double> previousAverageQueueSize;

    public RandomEarlyDetection(Map<String, Object> parameters) {
        super(parameters);
        if (parameters == null) throw new IllegalArgumentException("no parameters defined - parameter Map is NULL");
        if (! parameters.containsKey(EXPONENTIAL_WEIGHT_FACTOR)) {
            throw new IllegalArgumentException("EXPONENTIAL_WEIGHT_FACTOR was not defined in parameters");
        }
        if (! (parameters.get(EXPONENTIAL_WEIGHT_FACTOR) instanceof Double)) {
            throw new IllegalArgumentException("EXPONENTIAL_WEIGHT_FACTOR parameter must has Double as value - actual value of defined parameter is " + parameters.get(EXPONENTIAL_WEIGHT_FACTOR).getClass());
        }
    }

    @Override
    public boolean manageQueue(List<Packet> queue, Packet newPacket) {
        if (previousAverageQueueSize == null) {
            previousAverageQueueSize = new TreeMap<Integer, Double>();
        }
        double averageQueueSize = calculateAverageQueueSize((Double) parameters.get(EXPONENTIAL_WEIGHT_FACTOR), newPacket.getQosQueue(), queue.size());

        return false;
    }

    /**
     * Pb = PMAX * ( (AVG - MINthres) / (MAXthres - MINthres) )
     * where:
     * PMAX = max probability of packet drop (when AVG = MAXthres)
     * AVG = average queue size
     * MINthres = minumum treshold
     *
     * @return
     */
    private double calculatePacketDropProbability() {
        throw new UnsupportedOperationException("not yet implemented"); //CRITICAL finish this
    }

    /**
     * AVGk = (1-w) * AVGk-1 + w*q
     * where:
     * w = exponential weight factor
     * q = current queue size
     * AVGk-1 = previous average queue size (it is not a math formula: AVGk - 1)
     *
     * @return
     */
    private double calculateAverageQueueSize(double weightFactor, int queueNumber, int queueSize) {
        double result = (1 - weightFactor) * getPreviousQueueSize(queueNumber) + weightFactor * queueSize;
        previousAverageQueueSize.put(queueNumber, result);
        return result;
    }

    private double getPreviousQueueSize(int queue) {
        if (previousAverageQueueSize.containsKey(queue)) {
            return previousAverageQueueSize.get(queue);
        }
        return 0;
    }
}
