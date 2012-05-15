package sk.stuba.fiit.kvasnicka.qsimsimulation.helpers;

import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.PacketStateEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.util.LinkedList;
import java.util.List;

/**
 * this helper contains methods with queueing involved
 *
 * @author Igor Kvasnicka
 */
public class QueueingHelper {
    private final static Logger logg = Logger.getLogger(QueueingHelper.class);
    private PacketManager packetManager;
    private TopologyManager topologyManager;

    public QueueingHelper(PacketManager packetManager, TopologyManager topologyManager) {
        this.packetManager = packetManager;
        this.topologyManager = topologyManager;
    }


//    /**
//     * checks if there is enough space left in the output queue
//     *
//     * @param queueNumber queue number (packet priority)
//     * @param node        network node I am interested in
//     * @return true if packet can be added
//     */
//    private boolean isEnoughSpaceInOutputQueue(int queueNumber, NetworkNode node) {
//        if (queueNumber > node.getQueueCount()) {
//            throw new IllegalArgumentException("queueNumber is higher than maximum queue count: " + queueNumber + " > " + node.getQueueCount());
//        }
//        DelayQueue<Packet> packets = networkNodeMap.get(node);
//        int size = 0;
//        for (Packet p : packets) {
//            if ((PacketStateEnum.OUPUT_QUEUE == p.getState()) && (p.getQosQueue() == queueNumber)) {
//                size += p.getPacketSize();
//            }
//        }
//        if (size <= node.getQueueSize(queueNumber)) {
//            return true;
//        }
//        return false;
//    }


//    /**
//     * changes packet state of packets in input buffer and make them PROCESSING state
//     *
//     * @param simulationTime current simulation time
//     */
//    public void changePacketsStateInputBuffer(long simulationTime) {
//
//        for (NetworkNode node : networkNodeMap.keySet()) {
//            long startTime = simulationTime - SimulationTimer.TIME_QUANTUM; //previous time
//            long endTime = startTime + node.getPacketProcessingEndsTime();
//
//            //QoS method for queueing is called each time there is some CPU left to process packet
//            //so I have to iterate through all these times and call QoS queueing method
//
//            while (endTime > simulationTime) {
//                movePacketsFromOutputQueueInInterval(node, startTime, endTime);
//                startTime += node.getPacketProcessingEndsTime();
//                endTime += node.getPacketProcessingEndsTime();
//            }
//        }
//    }


    /**
     * postpones packet processing for some time
     *
     * @param packet           packet to handle
     * @param howLongToPospone time how long should packet be postponed
     */
    public void postponePacket(Packet packet, double howLongToPospone) {
        packet.setTimeWhenNextStateOccures(packet.getTimeWhenNextStateOccures() + howLongToPospone);
    }


    /**
     * empties output queues
     *
     * @param simulationTime current simulation time
     */
//    public void movePacketsFromOutputQueue(double simulationTime) {
//        for (NetworkNode node : packetManager.getNetworknodeList()) {
//            for (NetworkNode.OutputInterface outputInterface : node.getOutputInterfaces().values()) {
//
//                double serialisationEndTime = outputInterface.getSerialisationEndTime();
//
//                while (serialisationEndTime <= simulationTime) {
//                    boolean moved = movePacketsFromOutputQueueInInterval(node, outputInterface, serialisationEndTime);
//                    if (! moved) {
//                        break;
//                    }
//                    double next = outputInterface.getSerialisationEndTime();
//                    serialisationEndTime += next;
//                }
//            }
//        }
//    }
    private double previousTimeQuantum(double simulationTime) {
        return simulationTime - SimulationTimer.TIME_QUANTUM;
    }

    /**
     * moves packet from output interface to serialising state
     *
     * @param node
     * @param outputInterface
     * @param endTime
     * @return true if packet was moved; false if no such packet was found
     */
//    private boolean movePacketsFromOutputQueueInInterval(NetworkNode node, NetworkNode.OutputInterface outputInterface, double endTime) {
//        List<Packet> outputPackets = getPacketsInOutputQueue(outputInterface.getPackets(), endTime);
//        //todo tu urobit tail drop, ake je v queue prilis vela packetov a pretecie mi buffer - pouzijem networkNode.getPacketsByPriority
//        List<Packet> qosChoosen = node.getQosMechanism().decitePacketsToMoveFromOutputQueue(outputPackets);//these packets are eligible to change their state to PROCESSING
//
//        Packet p = findPacketToSend(qosChoosen);
//        if (p == null) {
//            return false;
//        }
//
//        p.setState(PacketStateEnum.SERIALISING_OUTPUT_START);
//        Edge newPosition = p.getPosition().getEdge();
//        newPosition.addPacket(p); //adds packet to the edge
//
//        p.setTimeWhenNextStateOccures(p.getTimeWhenNextStateOccures() + endTime + DelayHelper.calculateSerialisationDelay(newPosition, p.getPacketSize()));
//
//        outputInterface.setSerialisationEndTime(topologyManager.getSerialisationDelay(node, outputInterface.getNetworknodeNextHop(), p.getPacketSize()));
//
//        outputInterface.removePacket(p);//remove packet from output queue
//        return true;
//    }


    /**
     * retrieves all packets that are waiting in input buffer within given time interval
     *
     * @param packets all  packets in output queue
     * @param endTime end of time interval (included)
     * @return returns packets in node's queue that came into queue within specified time interval
     */
    private List<Packet> getPacketsInOutputQueue(List<Packet> packets, double endTime) {
        List<Packet> list = new LinkedList<Packet>();
        if (endTime == 0) {//there has not been any serialisation, yet
            list.addAll(packets);
            return list;
        }
        for (Packet packet : packets) {
            if (PacketStateEnum.OUPUT_QUEUE == packet.getState()) {
                if (packet.getTimeWhenCameToQueue() <= endTime) {
                    list.add(packet);
                }
            }
        }
        return list;
    }

    /**
     * ony one packet can be serialised on the wire
     * this method determines witch one is that - it is the packet that came first to the router
     *
     * @param packets list of packets I am reviewing
     * @return packet to serialise
     */
    private Packet findPacketToSend(List<Packet> packets) {//todo najdem vsetky pakety na poslanie do simulacneho casu (ktore som mal poslat) - tieto budu v HW queue a tie zacnem serializovat a vyhadzovat z HW queue
        double time = Double.MAX_VALUE;
        Packet packet = null;
        for (Packet p : packets) {
            if (p.getTimeWhenCameToNetworkNode() < time) {
                time = p.getTimeWhenCameToNetworkNode();
                packet = p;
            }
        }

        return packet;
    }
}
