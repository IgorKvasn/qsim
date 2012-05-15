package sk.stuba.fiit.kvasnicka.qsimsimulation.managers;

import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.QueueingHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Igor Kvasnicka
 */
public class PacketManager {
    private static final Logger logg = Logger.getLogger(PacketManager.class);
    /**
     * here are all packets in states PROCESSING
     */

    private SimulationTimer simulationTimer;//I need this to obtain current delay = time quantum
    private QueueingHelper queueingHelper;

    public PacketManager(SimulationTimer simulationTimer) {
        this.simulationTimer = simulationTimer;

        queueingHelper = new QueueingHelper(this, simulationTimer.getTopologyManager());
    }


    /**
     * returns list of all NetworkNodes
     *
     * @return
     */
    public List<NetworkNode> getNetworknodeList() {
        return simulationTimer.getTopologyManager().getNodeList();
    }

    /**
     * returns list of all Edges
     *
     * @return
     */
    public List<Edge> getEdgeList() {
        return simulationTimer.getTopologyManager().getEdgeList();
    }

    /**
     * returns actual simulation time
     *
     * @return current simulation time
     */
    public double getSimulationTime() {
        return simulationTimer.getSimulationTime();
    }

    /**
     * initializes newly created packets and adds them into proper DelayQueue
     * this method DOES NOT create packets
     *
     * @param source       source network node
     * @param packets      packet list to init
     * @param creationTime time when packets were created
     */
    public void initPackets(NetworkNode source, List<Packet> packets, double creationTime) {
        for (Packet p : packets) {
            source.addNewPacketsToOutputQueue(p, creationTime);
        }
    }


    /**
     * returns all packets that are about to change their state
     *
     * @param simulationTime
     * @return list of packets
     */
    public List<Packet> getAllPacketsOnTheWire(double simulationTime) {
        List<Packet> result = new LinkedList<Packet>();
        for (Edge edge : getEdgeList()) {
            result.addAll(getPackets(edge, simulationTime));
        }

        return result;
    }


    /**
     * returns all expired packets associated with given Edge
     *
     * @param edge           edge
     * @param simulationTime
     * @return list of packets
     */
    private Collection<Packet> getPackets(Edge edge, double simulationTime) {
        return edge.getPackets(simulationTime);
    }


//
//    /**
//     * <b>Deprecated:</b> this method is OK, but in fact it handles only packet sending...all other states are handled
//     * by NetworkNode itself
//     * <p/>
//     * changes state of the packet.
//     * this method is called when some state change actually happens
//     * it changes status only for packets that are not in PROCESSING or some of INPUT/OUTPUT BUFFER states
//     *
//     * @param packet         packet to change status
//     * @param simulationTime current simulation time
//     */
//    @Deprecated
//    private void changePacketState(Packet packet, double simulationTime) {
//        if (packet == null) {
//            throw new IllegalStateException("packet is NULL");
//        }
//        if (packet.getState() == null) {
//            throw new IllegalStateException("packet state is NULL");
//        }
//        if (packet.getState().isInBuffer() || packet.getState() == PacketStateEnum.PROCESSING) {//this method is not for you
//            logg.warn("method changePacketState() is dealing with packets in state " + packet.getState() + " - this is just wrong!");
//            return;
//        }
//        //move packet to next state and do not forget that I can move to several states within give time quantum
//        double timeToNextState = packet.getTimeWhenNextStateOccures();//because next state is NOW
//        while (timeToNextState <= simulationTime) {//I have got enough time to move to another state
//            PacketStateEnum nextState = packet.getNextState();
//            packet.setState(nextState);
//
//            //now use QoS mark method to determine network node buffer number (if packet just came into NetworkNode)
//            markPacketIfNeed(packet);
//
//            if (packet.getState() == PacketStateEnum.PROCESSING) {
//                break;
//            }
//            double next = packet.calculateTimeToNextState();
//
//            if (next == Double.MAX_VALUE) {  //packet is in output queue
//                logg.warn("packet is in output queue and yet I am processing it in changePacketState() method");
//                break;
//            }
//            if (next == Double.MIN_VALUE) {//packet is already delivered
//                logg.warn("packet is delivered");
//                return;
//            }
//
//
//            timeToNextState = timeToNextState + next;//this much time is until next state (that I may or may not reach)
//        }
//
//
//        if (PacketStateEnum.PROCESSING == packet.getState()) {//I have reached PROCESSING state
//            NetworkNode node = packet.getPosition().getNode();
//            packet.setTimeWhenNextStateOccures(timeToNextState + DelayHelper.calculateProcessingDelay(node));
//            packet.setTimeWhenCameToNetworkNode(timeToNextState);
//            node.addPacketToProcessing(packet);
//            return;
//        }
//
//        // I cannot process this packet in current time quantum, so packet is put into DelayQueue and it will be processed in the next time quantum
//        packet.setTimeWhenNextStateOccures(timeToNextState);
//    }


    /**
     * executes mark method in appropriate QoS mechanism
     * this happens only if packet han hot been already marked
     *
     * @param packet packet to mark
     */
    private void markPacketIfNeed(Packet packet) {
        if (packet == null) throw new IllegalArgumentException("packet is NULL");
        if (packet.getQosQueue() == null) {//packet has not been marked, yet

            if (packet.getPosition().getNode() == null) {
                throw new IllegalStateException("packet is not in the NetworkNode - cannot use QoS mechanism");
            }

            if (packet.getPosition().getNode().getQosMechanism() == null) {
                throw new IllegalStateException("NetworkNode has not set QoS mechanism");
            }

            int mark = packet.getPosition().getNode().getQosMechanism().classifyAndMarkPacket(packet);
            packet.setQosQueue(mark);
        }
    }


    /**
     * deletes all packets
     */
    public void clearAllPackets() {

        logg.debug("clearing all simulation data");
        for (NetworkNode node : simulationTimer.getTopologyManager().getNodeList()) {
            node.clearPackets();
        }

        for (Edge edge : simulationTimer.getTopologyManager().getEdgeList()) {
            edge.getAllPackets().clear();
        }
    }

    /**
     * checks whether there are no packets in the whole topology - this may indicate end of simulation (and end of the world, too)
     *
     * @return
     */
    public boolean checkNoPacketsInSimulation() {
        for (Edge e : getEdgeList()) {
            if (! e.getAllPackets().isEmpty()) return false;
        }

        for (NetworkNode networkNode : getNetworknodeList()) {
            if (! networkNode.isEmpty()) return false;
        }
        return true;
    }
}
