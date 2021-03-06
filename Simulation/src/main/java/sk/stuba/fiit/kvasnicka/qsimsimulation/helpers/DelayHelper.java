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
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

/**
 * here all delays are calculated
 *
 * @author Igor Kvasnicka
 */
public abstract class DelayHelper {
    private static UnivariateRealInterpolator interpolator = new LinearInterpolator();

    private static Logger logg = Logger.getLogger(DelayHelper.class);
    public static final double MIN_PROCESSING_DELAY = 0.5; //msec
    public static final double PACKET_CREATION_DELAY = 0.1;


    public static double calculateSerialisationDelay(Packet packet, Edge edge, int packetSize) {
        if (edge == null) throw new IllegalArgumentException("edge is NULL");
        return (double) packetSize / edge.getSpeed(packet);
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
    public static double calculateProcessingDelay(NetworkNode networkNode) {
        UnivariateRealFunction function = null;
        try {
            function = interpolator.interpolate(new double[]{0.0, networkNode.getMaxProcessingPackets()}, new double[]{networkNode.getMinProcessingDelay(), networkNode.getMaxProcessingDelay()});
            double interpolationX = networkNode.getPacketsInProcessing().size();
            return function.value(interpolationX);
        } catch (MathException e) {
            logg.error(e);
            return MIN_PROCESSING_DELAY;
        }
    }

    /**
     * determines delay between packet creation
     * this delay is NOT QoS related
     *
     * @param rule       simulation rule that is creating new packets
     * @param packetSize
     * @return
     */
    public static double calculatePacketCreationDelay(SimulationRuleBean rule, int packetSize, double simulationTime) {
        if (rule==null){
            throw new IllegalArgumentException("simulation rule is NULL");
        }

        if (rule.getSource()==null){
            throw new IllegalArgumentException("src network node is NULL");
        }

        if (rule.getPacketCreationDelayFunction() == null) {
            throw new IllegalArgumentException("packet creation delay function is NULL");
        }

        return rule.getPacketCreationDelayFunction().calculateDelay(rule, simulationTime);
    }
}
