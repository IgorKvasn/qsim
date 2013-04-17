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
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.ParameterException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.QosUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Igor Kvasnicka
 */
public class RandomEarlyDetection extends ActiveQueueManagement {

    private static final long serialVersionUID = - 162267636428656655L;

    public static final String EXPONENTIAL_WEIGHT_FACTOR = "exponential_weight_factor";
    public static final String MIN_THRESHOLD = "min_threshlod";
    public static final String MAX_THRESHOLD = "max_threshlod";
    public static final String MAX_PROBABILITY = "max_probability";


    /**
     * map of previous average queue sizes
     * key = queue number (packet.getQosQueue)
     * value = average queue size
     */
    private transient Map<Integer, Double> previousAverageQueueSize;

    public RandomEarlyDetection(HashMap<String, Object> parameters) {
        super(parameters);

        try {
            QosUtils.checkParameter(parameters, Double.class, EXPONENTIAL_WEIGHT_FACTOR);
            QosUtils.checkParameter(parameters, Double.class, MIN_THRESHOLD);
            QosUtils.checkParameter(parameters, Double.class, MAX_THRESHOLD);
            QosUtils.checkParameter(parameters, Double.class, MAX_PROBABILITY);
        } catch (ParameterException e) {
            throw new IllegalStateException(e);
        }

        if ((Double) parameters.get(EXPONENTIAL_WEIGHT_FACTOR) > 1) {
         //   throw new IllegalArgumentException("EXPONENTIAL_WEIGHT_FACTOR must not be greater than 1");
        }
    }

    @Override
    public boolean manageQueue(List<Packet> queue, Packet newPacket) {
        HashMap<String, Object> parameters = getParameters();
        if (newPacket == null) {
            throw new IllegalStateException("packet is NULL");
        }
        if (newPacket.getQosQueue() == - 1) {
            throw new IllegalStateException("unknown qos queue for packet: " + newPacket.getQosQueue());
        }
        if (queue == null) throw new IllegalArgumentException("queue is NULL");

        if (previousAverageQueueSize == null) {
            previousAverageQueueSize = new TreeMap<Integer, Double>();
        }

        double averageQueueSize = calculateAverageQueueSize((Double) parameters.get(EXPONENTIAL_WEIGHT_FACTOR), newPacket.getQosQueue(), queue.size());

        double threshMin = (Double) parameters.get(MIN_THRESHOLD);
        double threshMax = (Double) parameters.get(MAX_THRESHOLD);

        if (averageQueueSize <= threshMin)
            return true;//no packet dropping
        if (averageQueueSize > threshMax)
            return false;//everything is dropped

        double pmax = (Double) parameters.get(MAX_PROBABILITY);
        double q = (Double) parameters.get(EXPONENTIAL_WEIGHT_FACTOR);

        double pb = pmax * (averageQueueSize - threshMin) / (threshMax - threshMin);
        double probability = pb / (1 - q * pb);
        if (probability > 1) {
            throw new IllegalStateException("drop probability is more then 1 - somethong is wrong with your formula; probability = " + probability);
        }

        if (Math.random() <= probability) return true;
        return false;
    }

    /**
     * calculates and stores current average queue size
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
