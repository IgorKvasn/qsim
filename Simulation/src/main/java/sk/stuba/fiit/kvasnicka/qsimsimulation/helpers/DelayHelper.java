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

package sk.stuba.fiit.kvasnicka.qsimsimulation.helpers;

import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;

/**
 * here all fixed delays are calculated
 *
 * @author Igor Kvasnicka
 */
public abstract class DelayHelper {

    public static final double MIN_PROCESSING_DELAY = 0.5; //msec


    public static double calculateSerialisationDelay(Edge edge, int packetSize) {
        if (edge == null) throw new IllegalArgumentException("edge is NULL");
        return (double) packetSize / edge.getSpeed();
    }

    public static double calculatePropagationDelay(Edge edge) {
        if (edge == null) throw new IllegalArgumentException("edge is NULL");
        return edge.getLength() / (2.1 * Math.pow(10, 8));
    }

    public static double calculateProcessingDelay(NetworkNode networkNode) {
//        networkNode.getMinProcessingDelay()
//        networkNode.getMaxProcessingDelay()
        return 0.018;
    }

    /**
     * determines delay between packet creation
     * this delay is NOT QoS related
     *
     * @param node           network node where packets are being created
     * @param packetSize
     * @param packetTypeEnum
     * @return
     */
    public static double calculatePacketCreationDelay(NetworkNode node, int packetSize, PacketTypeEnum packetTypeEnum) {
        return 0.1;
    }
}
