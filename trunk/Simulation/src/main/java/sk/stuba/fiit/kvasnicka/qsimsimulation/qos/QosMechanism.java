package sk.stuba.fiit.kvasnicka.qsimsimulation.qos;

import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.util.List;

/**
 * here all the QoS stuff happens
 *
 * @author Igor Kvasnicka
 */


//todo urobit serializaciu do jaxb rovnako ako funguje netowrk node a router/switch/computer - @XmlSeeAlso


public interface QosMechanism {
    /**
     * marks given packet according to NetworkNode rules
     *
     * @param packet packet to mark
     * @return number of the QoS queue where this packet belongs
     * @see sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet#qosQueue here will be return value stored
     */

    int markPacket(Packet packet);

    /**
     * decides which packets should be moved from output queue
     *
     * @param outputQueuePackets all packets in all output queues (it does not matter what priority ar what queue are they in)
     * @return list of packets to send
     */
    List<Packet> decitePacketsToMoveFromOutputQueue(List<Packet> outputQueuePackets);
}
