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

import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.queuemanagement.ActiveQueueManagement;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.ClassDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.ParameterException;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.QosUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Igor Kvasnicka
 */
public class WeightedRED extends ActiveQueueManagement {


    private static final long serialVersionUID = - 1483265147363980794L;
    public static final String WRED_DEFINITION = "WRED_DEFINITION";
    private static final String DEFAULT_RED_DEFINITION = "";
    /**
     * each queue has its own RED algorithm; if queue number is null, it is a default RED
     * key = QoS class name
     * value = RED algorithm
     */
    private transient Map<String, RandomEarlyDetection> reds = null;

    private transient WredDefinition[] wredDefinitions = null;


    public WeightedRED(HashMap<String, Object> parameters) {
        super(parameters);

        try {
            QosUtils.checkParameter(parameters, WredDefinition[].class, WRED_DEFINITION);
        } catch (ParameterException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean manageQueue(List<Packet> queue, Packet newPacket) {

        if (reds == null) {
            initRedMap();
        }
        if (wredDefinitions == null) {
            initClasses();
        }

        if (newPacket == null) {
            throw new IllegalArgumentException("packet is NULL");
        }
        ClassDefinition qosClass = getClassNumber(newPacket);

        if (qosClass == null) {//could not find red definition for packet with this classification

            if (reds.containsKey(DEFAULT_RED_DEFINITION)) {//is a default definition configured?
                return reds.get(DEFAULT_RED_DEFINITION).manageQueue(queue, newPacket);
            }

            throw new IllegalStateException("no WRED definition for queue: " + newPacket.getQosQueue() + " - also: no default WRED definition configured");
        }

        if (reds.get(qosClass.getName()) == null) {
            throw new IllegalStateException("RED algorithm was not defined for QoS class: " + qosClass.getName());
        }

        return reds.get(qosClass.getName()).manageQueue(queue, newPacket);
    }

    private void initClasses() {
        wredDefinitions = (WredDefinition[]) getParameters().get(WRED_DEFINITION);
    }

    /**
     * for a given packet determines, which QoS class it belongs to
     *
     * @param packet packet
     * @return null if no such class found
     */
    private ClassDefinition getClassNumber(Packet packet) {
        if (packet.getQosQueue() == - 1) return null;
        for (WredDefinition def : wredDefinitions) {
            if (def.getQosClass().getQueueNumbers().contains(packet.getQosQueue())) return def.getQosClass();
        }
        return null;
    }

    private void initRedMap() {
        WredDefinition[] definitions = (WredDefinition[]) getParameters().get(WRED_DEFINITION);

        reds = new HashMap<String, RandomEarlyDetection>();

        //create RED algorithms for each queue
        for (final WredDefinition def : definitions) {

            if (reds.containsKey(def.getQosClass().getName())) {
                throw new IllegalStateException("duplicate QoS class definition for queue: " + def.getQosClass().getName());
            }

            RandomEarlyDetection red = new RandomEarlyDetection(new WRedToRed(def));
            reds.put(def.getQosClass().getName(), red);
        }
    }

    @Getter
    public static class WredDefinition implements Serializable {

        private static final long serialVersionUID = - 2963226506330225270L;

        private ClassDefinition qosClass;
        private double exponentialWeightFactor;
        private double maxThreshold;
        private double minThreshold;
        private double maxProbability;

        public WredDefinition(ClassDefinition qosClass, double exponentialWeightFactor, double maxThreshold, double minThreshold, double maxProbability) {
            this.qosClass = qosClass;
            this.exponentialWeightFactor = exponentialWeightFactor;
            this.maxThreshold = maxThreshold;
            this.minThreshold = minThreshold;
            this.maxProbability = maxProbability;
        }
    }

    private static class WRedToRed extends HashMap<String, Object> {

        private static final long serialVersionUID = 2244842184862300699L;

        public WRedToRed(WredDefinition def) {
            put(RandomEarlyDetection.EXPONENTIAL_WEIGHT_FACTOR, def.exponentialWeightFactor);
            put(RandomEarlyDetection.MAX_PROBABILITY, def.maxProbability);
            put(RandomEarlyDetection.MAX_THRESHOLD, def.maxThreshold);
            put(RandomEarlyDetection.MIN_THRESHOLD, def.minThreshold);
        }
    }
}
