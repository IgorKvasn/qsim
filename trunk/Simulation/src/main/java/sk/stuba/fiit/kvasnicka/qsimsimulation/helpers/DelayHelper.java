package sk.stuba.fiit.kvasnicka.qsimsimulation.helpers;

import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketTypeEnum;

/**
 * here all fixed delays are calculated
 *
 * @author Igor Kvasnicka
 */
public abstract class DelayHelper {

    private static final Logger logg = Logger.getLogger(DelayHelper.class);

    public static final double MIN_PROCESSING_DELAY = 0.5; //msec


    public static double calculateSerialisationDelay(Edge edge, int packetSize) {                //todo toto vsetko mockovat!!!!!!!!!
//        logg.debug("serialisable delay");
        if (edge == null) throw new IllegalArgumentException("edge is NULL");
        return (double) packetSize / edge.getSpeed();
    }

    public static double calculatePropagationDelay(Edge edge) {
//        logg.debug("propagation delay");
        if (edge == null) throw new IllegalArgumentException("edge is NULL");
        return edge.getLength() / (2.1 * Math.pow(10, 8));
    }

    public static double calculateProcessingDelay(NetworkNode networkNode) {
//        logg.debug("processing delay");
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
//        logg.debug("packet creation");
        return 0.1;
    }
}
