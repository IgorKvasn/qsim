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

package sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.utils;

import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.PacketScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.ClassBasedWFQScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.FifoScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.PriorityQueuingScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.RoundRobinScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.WeightedFairQueuingScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.WeightedRoundRobinScheduling;

import java.util.HashMap;

/**
 * factory that creates packet scheduling algorithm implementation
 *
 * @author Igor Kvasnicka
 */
public final class PacketSchedulingFactory {
    private PacketSchedulingFactory() {

    }

    /**
     * creates PacketScheduling implementation object according to specified PacketSchedulingImplEnum
     *
     * @param schedulingEnum type of packet scheduling to be created
     * @param params         some packet scheduling implementations require some parameters; may be null if no parameters are needed
     * @return PacketScheduling implementation
     */
    public static PacketScheduling createPacketScheduling(PacketSchedulingImplEnum schedulingEnum, HashMap<String, Object> params) {
        switch (schedulingEnum) {
            case ClassBasedWFQ:
                return new ClassBasedWFQScheduling(params);
            case FIFO:
                return new FifoScheduling();
            case PriorityQueue:
                return new PriorityQueuingScheduling();
            case RoundRobin:
                return new RoundRobinScheduling();
            case WeightedFairQueuing:
                return new WeightedFairQueuingScheduling();
            case WeightedRoundRobin:
                return new WeightedRoundRobinScheduling(params);
            default:
                throw new IllegalArgumentException("unknown packet scheduling algorithm: " + schedulingEnum.name());
        }
    }
}
