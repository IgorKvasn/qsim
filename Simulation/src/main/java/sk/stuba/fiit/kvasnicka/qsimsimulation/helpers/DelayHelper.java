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

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math.analysis.interpolation.UnivariateRealInterpolator;
import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.LogCategory;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.LogSource;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLog;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLogUtil;

/**
 * here all delays are calculated
 *
 * @author Igor Kvasnicka
 */
public abstract class DelayHelper {
    private static UnivariateRealInterpolator interpolator = new LinearInterpolator();

    private static Logger logg = Logger.getLogger(DelayHelper.class);
    public static final double MIN_PROCESSING_DELAY = 0.5; //msec     //todo change to 0.018 or possibly another value - 0.5 msec is simply too big
    public static final double PACKET_CREATION_DELAY = 0.1;


    public static double calculateSerialisationDelay(Edge edge, int packetSize) {
        if (edge == null) throw new IllegalArgumentException("edge is NULL");
        return (double) packetSize / edge.getSpeed();
    }

    public static double calculatePropagationDelay(Edge edge) {
        if (edge == null) throw new IllegalArgumentException("edge is NULL");
        return edge.getLength() / (2.1 * Math.pow(10, 8));
    }

    /**
     * processing delay is a variable delay calculated depending in number of packets being processed by CPU right now
     * before simulation started, user entered minimum and maximum value for processing delay and maximum number of packets being simultaneously processed
     * <p/>
     * processing delay is interpolated according these values
     *
     * @param networkNode
     * @return
     */
    public static double calculateProcessingDelay(NetworkNode networkNode) {  //todo cache all iterpolation objects so that they need not to be initialised over and over
        UnivariateRealFunction function = null;
        try {
            function = interpolator.interpolate(new double[]{0.0, networkNode.getMaxProcessingPackets()}, new double[]{networkNode.getMinProcessingDelay(), networkNode.getMaxProcessingDelay()});
            double interpolationX = networkNode.getPacketsInProcessing().size();
            return function.value(interpolationX);
        } catch (MathException e) {
            logg.error(e);
            SimulationLogUtil.getInstance().log(new SimulationLog(LogCategory.ERROR, "Unable to calculate processing delay: " + e.getMessage(), networkNode.getName(), LogSource.UNKNOWN, - 1));
            return MIN_PROCESSING_DELAY;
        }
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
        return PACKET_CREATION_DELAY;
    }
}
