package sk.stuba.fiit.kvasnicka.qsimsimulation.managers;

import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;

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

    public PacketManager(SimulationTimer simulationTimer) {
        this.simulationTimer = simulationTimer;
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
     */
    public void initPackets(NetworkNode source, List<Packet> packets) {
        for (Packet p : packets) {
            source.addNewPacketsToOutputQueue(p);
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
            edge.getFragments().clear();
        }
    }

    /**
     * checks whether there are no packets in the whole topology - this may indicate end of simulation (and end of the world, too)
     *
     * @return
     */
    public boolean checkNoPacketsInSimulation() {
        for (Edge e : getEdgeList()) {
            if (! e.getFragments().isEmpty()) return false;
        }

        for (NetworkNode networkNode : getNetworknodeList()) {
            if (! networkNode.isEmpty()) return false;
        }
        return true;
    }
}
