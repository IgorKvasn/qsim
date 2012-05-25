package sk.stuba.fiit.kvasnicka.qsimsimulation;

import lombok.Getter;
import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.SimulationManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.ping.PingManager;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Simulation timer that awakes and do all the simulation stuff.
 * Simple javax.swing.Timer is used.
 *
 * @author Igor Kvasnicka
 */
public class SimulationTimer implements ActionListener {
    private static Logger logg = Logger.getLogger(SimulationTimer.class);
    public static final double TIME_QUANTUM = DelayHelper.MIN_PROCESSING_DELAY + .1; //or also known as "timer delay" :) [msec]
    private Timer timer;
    @Getter
    private PacketGenerator packetGenerator;
    @Getter
    private PingManager pingManager;
    @Getter
    private double simulationTime;
    private SimulationManager simulationManager;
    @Getter
    private PacketManager packetManager;
    @Getter
    private TopologyManager topologyManager;
    private static final int MIN_TIMER_DELAY = 10; //[msec]
    private static final int MAX_TIMER_DELAY = 1000;//[msec]

    public SimulationTimer(List<Edge> edgeList, List<NetworkNode> nodeList) {
        topologyManager = new TopologyManager(edgeList, nodeList);
    }

    /**
     * starts the timer
     *
     * @param simulationManager reference to SimulationManager object
     */
    public void startSimulationTimer(SimulationManager simulationManager) {
        if (timer != null && timer.isRunning()) {
            throw new IllegalStateException("Simulation timer is already running.");
        }
        if (packetManager != null) { //simulation has been started some time before
            packetManager.clearAllPackets();//clean-up all packets
        }
        packetManager = new PacketManager(this);
        pingManager = new PingManager();
        this.simulationManager = simulationManager;
        packetGenerator = new PacketGenerator(simulationManager.getRulesUnmodifiable(), this);

        for (NetworkNode node : getTopologyManager().getNodeList()) {
            node.setTopologyManager(topologyManager);
        }

        timer = new Timer(convertTime(1), this);
        if (logg.isDebugEnabled()) {
            logg.debug("starting simulation timer");
        }
        timer.start();
    }

    public void addPingSimulationRule(SimulationRuleBean rule, int repetitions) {
        simulationManager.addSimulationRule(rule);
        pingManager.addPing(rule, repetitions);
    }

    public void addSimulationrule(SimulationRuleBean rule) {
        simulationManager.addSimulationRule(rule);
    }


    /**
     * cancels simulation timer
     *
     * @see #clearSimulationData()
     */
    public void stopTimer() {
        if (timer == null) throw new IllegalStateException("stopping timer: timer is NULL");
        if (logg.isDebugEnabled()) {
            logg.debug("stopping simulation timer");
        }
        timer.stop();
        for (SimulationRuleBean rule : simulationManager.getRulesUnmodifiable()) {
            rule.removeAllDeliveryListeners();
        }
    }

    /**
     * pauses simulation timer
     */
    public void pauseTimer() {
        if (timer == null) throw new IllegalStateException("pause timer: timer is NULL");
        if (logg.isDebugEnabled()) {
            logg.debug("pause simulation timer");
        }
        timer.stop();
    }

    public void resumeTimer() {
        if (timer == null) throw new IllegalStateException("resume timer: timer is NULL");
        if (logg.isDebugEnabled()) {
            logg.debug("resuming simulation timer");
        }
        timer.start();
    }

    /**
     * clears all data left after the simulation process
     * use this method only when timer is stopped
     */
    public void clearSimulationData() {
        if (timer == null) throw new IllegalStateException("clear simulation data: timer is NULL");
        if (timer.isRunning()) {
            throw new IllegalStateException("cannot clear simulation data - simulation timer is running");
        }
        packetManager.clearAllPackets();
    }

    /**
     * this method is called when timer awakes
     *
     * @param event
     */
    public void actionPerformed(ActionEvent event) {
        try {

            simulationTime += TIME_QUANTUM;//increase simulation clock

            //-------generate new packets in all network nodes
            //new packets are created into output buffer, so generatePackets() should be called before emptying output buffers
            packetGenerator.generatePackets(simulationTime, TIME_QUANTUM);

            for (Edge edge : packetManager.getEdgeList()) {
                edge.moveFragmentsToNetworkNode(simulationTime);
            }

            //from input queue to processing
            for (NetworkNode node : packetManager.getNetworknodeList()) {
                node.moveFromInputQueueToProcessing(simulationTime);
            }

            //from processing to output queue
            for (NetworkNode node : packetManager.getNetworknodeList()) {
                node.movePacketsFromProcessingToOutputQueue(simulationTime);
            }

            //from output queue to TX buffer
            for (NetworkNode node : packetManager.getNetworknodeList()) {
                node.moveFromOutputQueueToTxBuffer(simulationTime);
            }

            //from TX buffer to the wire and to the RX buffer on the next-hop network node
            for (NetworkNode node : packetManager.getNetworknodeList()) {
                node.movePacketsToTheWire(simulationTime);
            }

            //check if there is nothing more to simulate
            if (isEndOfSimulation()) {
                if (logg.isDebugEnabled()) {
                    logg.debug("there is nothing left to simulate");
                }
                timer.stop();
            }
        } catch (Exception e) {
            //just to make it fail-safe catch all possible problems
            logg.error("Error during timer execution", e);
        }
    }


    public boolean isRunning() {
        return timer.isRunning();
    }

    /**
     * simulation may be done when all these conditions are fulfilled:
     * 1. there are no unfinished simulation rules (none is active or scheduled to start)
     * 2. all packets are in DELIVERED state = that means: no packets on the edges and no packets in the network nodes
     *
     * @return
     */
    private boolean isEndOfSimulation() {
        //first condition
        for (SimulationRuleBean sRule : simulationManager.getRulesUnmodifiable()) {
            if (! sRule.isFinished()) return false;
        }
        if (! packetManager.checkNoPacketsInSimulation()) {
            return false;
        }

        return true;
    }

    /**
     * sets delay of the timer
     * this method is called from GUI when user decides to speed up/slow down simulation
     *
     * @param speedUp no float or double speed up allowed; also no negative or less then 1
     */
    public void setTimerDelay(double speedUp) {
        if (speedUp < 1) throw new IllegalArgumentException("speedUp must not be less then 1");
        timer.setDelay(convertTime(speedUp));
    }

    private int convertTime(double speedUp) {
        long l = Math.round((TIME_QUANTUM * 1000) * speedUp);

        if (l < MIN_TIMER_DELAY) return MIN_TIMER_DELAY;
        if (l > Integer.MAX_VALUE) return MAX_TIMER_DELAY;

        return (int) l;
    }
}
