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

/**
 * @author Igor Kvasnicka
 */
public class WeightedRED extends ActiveQueueManagement {


    private static final long serialVersionUID = - 1483265147363980794L;
    public static final String WRED_DEFINITION = "WRED_DEFINITION";
    /**
     * each queue hsa its own RED algorithm
     * key = queue number
     * value = RED algorithm
     */
    private transient Map<Integer, RandomEarlyDetection> reds = null;


    public WeightedRED(HashMap<String, Object> parameters) {
        super(parameters);

        try {
            QosUtils.checkParameter(parameters, WredDefinition[].class, WRED_DEFINITION);
        } catch (ParameterException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public boolean manageQueue(List<Packet> queue, Packet newPacket) {

        if (reds == null) {
            initRedMap();
        }

        if (newPacket == null) {
            throw new IllegalArgumentException("packet is NULL");
        }
        if (! reds.containsKey(newPacket.getQosQueue())) {
            throw new IllegalStateException("no WRED definition for queue: " + newPacket.getQosQueue());
        }

        if (reds.get(newPacket.getQosQueue()) == null) {
            throw new IllegalStateException("RED algorithm was not defined for QoS queue: " + newPacket.getQosQueue());
        }

        return reds.get(newPacket.getQosQueue()).manageQueue(queue, newPacket);
    }

    private void initRedMap() {
        WredDefinition[] definitions = (WredDefinition[]) getParameters().get(WRED_DEFINITION);

        reds = new HashMap<Integer, RandomEarlyDetection>();

        //create RED algorithms for each queue
        for (final WredDefinition def : definitions) {

            if (reds.containsKey(def.qosNumber)) {
                throw new IllegalStateException("duplicate queue definition for queue: " + def.qosNumber);
            }

            RandomEarlyDetection red = new RandomEarlyDetection(new HashMap<String, Object>() {
                {
                    put(RandomEarlyDetection.EXPONENTIAL_WEIGHT_FACTOR, def.exponentialWeightFactor);
                    put(RandomEarlyDetection.MAX_PROBABILITY, def.maxProbability);
                    put(RandomEarlyDetection.MAX_THRESHOLD, def.maxThreshold);
                    put(RandomEarlyDetection.MIN_THRESHOLD, def.minThreshold);
                }

                private static final long serialVersionUID = 2244842184862300699L;
            });

            reds.put(def.qosNumber, red);
        }
    }


    public static class WredDefinition {

        private int qosNumber;
        private double exponentialWeightFactor;
        private double maxThreshold;
        private double minThreshold;
        private double maxProbability;

        public WredDefinition(int qosNumber, double exponentialWeightFactor, double maxThreshold, double minThreshold, double maxProbability) {
            this.qosNumber = qosNumber;
            this.exponentialWeightFactor = exponentialWeightFactor;
            this.maxThreshold = maxThreshold;
            this.minThreshold = minThreshold;
            this.maxProbability = maxProbability;
        }
    }
}
