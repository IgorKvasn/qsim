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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Igor Kvasnicka
 */
public final class QosUtils {

    private QosUtils() {

    }

    /**
     * check whether parameter map contains desired parameter
     * checks for:
     * parameter map must not be null
     * map must contain desired parameter key (that is not null)
     * type of parameter must be desired one
     *
     * @param map            parameter map
     * @param parameterClass Class of parameter (Integer, Double,...)
     * @param parameterKey   key to the parameter map
     * @throws ParameterException something is wrong with parameter
     */
    public static void checkParameter(Map<String, Object> map, Class parameterClass, String parameterKey) throws ParameterException {

        if (map == null) throw new ParameterException("no parameters defined - parameter Map is NULL");

        if (! map.containsKey(parameterKey)) {
            throw new ParameterException(parameterKey + " was not defined in parameters");
        }

        if (map.get(parameterKey) == null) {//this checks, if value is not null
            throw new ParameterException("parameter " + parameterKey + " has got NULL as value");
        }

        if (! parameterClass.isInstance(map.get(parameterKey))) {
            throw new ParameterException(parameterKey + " parameter must has " + parameterClass.getSimpleName() + " as value - actual value of defined parameter is " + map.get(parameterKey).getClass().getSimpleName());
        }
    }

    /**
     * one queue cannot be in multiple classes
     *
     * @throws ParameterException if queue is in multiple classes
     */
    public static void checkClassDefinition(ClassDefinition[] classes) throws ParameterException {
        if (classes == null) {
            throw new IllegalArgumentException("class definitions is NULL; this should be already taken care of");
        }
        Set<Integer> set = new TreeSet<Integer>();
        for (ClassDefinition def : classes) {
            if (def == null) throw new ParameterException("class definition is NULL");
            for (int queue : def.getQueueNumbers()) {
                if (set.contains(queue)) {
                    throw new ParameterException("Queue: " + queue + " is multiple classes");
                }
                set.add(queue);
            }
        }
    }
//
//    public static QosMechanismDefinition createQosMechanism(PacketClassification.Available classificationEnum, PacketScheduling.Available schedulingEnum, ActiveQueueManagement.Available queueManagementEnum) {
//        if (classificationEnum == null) throw new IllegalArgumentException("QoS classification not defined");
//        if (schedulingEnum == null) throw new IllegalArgumentException("QoS packet scheduling not defined");
//        if (queueManagementEnum == null) throw new IllegalArgumentException("QoS active queue management not defined");
//
//        PacketScheduling packetScheduling;
//        switch (schedulingEnum) {
//            case CB_WFQ:
//                packetScheduling = new C
//                break;
//            case FIFO:
//                break;
//            case PRIORITY_QUEUEING:
//                break;
//            case ROUND_ROBIN:
//                break;
//            case WEIGHTED_ROUND_ROBIN:
//                break;
//            case WFQ:
//                break;
//            default:
//                throw new IllegalStateException("unknown packet scheduling mechanism enum: " + schedulingEnum);
//        }
//
//        PacketClassification packetClassification;
//        ActiveQueueManagement activeQueueManagement;
//        return new QosMechanismDefinition(packetScheduling, packetClassification, activeQueueManagement);
//    }
}
