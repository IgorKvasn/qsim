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

package sk.stuba.fiit.kvasnicka.qsimsimulation;

import lombok.Getter;
import org.apache.log4j.Logger;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ruleactivation.SimulationRuleActivationListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.timer.SimulationTimerEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.timer.SimulationTimerListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.LogCategory;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.LogSource;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLog;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLogUtils;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PacketManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PingManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.SimulationManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.TopologyManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

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
    public static final int MILIS_IN_SECOND = 1000;
    public static final int NANOS_IN_MILIS = 1000;
    public static final double DEFAULT_SIMULATION_SPEED_UP = 1;
    private Timer timer;
    @Getter
    private PacketGenerator packetGenerator;
    @Getter
    private PingManager pingManager;
    @Getter
    private double simulationTime;
    @Getter
    private SimulationManager simulationManager;
    @Getter
    private PacketManager packetManager;
    @Getter
    private TopologyManager topologyManager;

    private static final double MIN_SPEED_UP = 0.5;
    private static final double MAX_SPEED_UP = 20;

    private static final int MIN_TIMER_DELAY = 5; //[msec]
    private static final int MAX_TIMER_DELAY = 1000;//[msec]

    private javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();
    private SimulationLogUtils simulationLogUtils;
    @Getter
    private double speedUp = 1;
    private boolean doSimulation = false;


    public SimulationTimer(List<Edge> edgeList, List<NetworkNode> nodeList, SimulationLogUtils simulationLogUtils) {
        this.simulationLogUtils = simulationLogUtils;
        topologyManager = new TopologyManager(edgeList, nodeList);
        packetManager = new PacketManager(this);

        for (NetworkNode node : getTopologyManager().getNodeList()) {
            node.setTopologyManager(topologyManager);
        }
    }

    /**
     * starts the timer
     */
    public void startSimulationTimer(SimulationManager simulationManager, PingManager pingManager, List<SimulationRuleActivationListener> listenerToBeAdded) {
        if (simulationManager == null) {
            throw new IllegalStateException("simulation manager is NULL");
        }

        if (timer != null && timer.isRunning()) {
            throw new IllegalStateException("Simulation timer is already running.");
        }

        if (packetManager != null) { //simulation has been started some time before
            packetManager.clearAllPackets();//clean-up all packets
        }

        this.simulationManager = simulationManager;
        this.pingManager = pingManager;

        packetGenerator = new PacketGenerator(simulationManager.getRulesUnmodifiable(), this);


        //adds listeners
        for (SimulationRuleActivationListener listener : listenerToBeAdded) {
            packetGenerator.addSimulationRuleActivationListener(listener);
        }
        listenerToBeAdded.clear();


        timer = new Timer(convertTime(SimulationTimer.DEFAULT_SIMULATION_SPEED_UP), this);
        if (logg.isDebugEnabled()) {
            logg.debug("starting simulation timer");
        }
        simulationLogUtils.log(new SimulationLog(LogCategory.INFO, "Starting simulation timer", SimulationLogUtils.SOURCE_GENERAL, LogSource.UNKNOWN, 0));
        timer.start();
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

        simulationLogUtils.log(new SimulationLog(LogCategory.INFO, "Stopping simulation timer", SimulationLogUtils.SOURCE_GENERAL, LogSource.UNKNOWN, simulationTime));

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

        simulationLogUtils.log(new SimulationLog(LogCategory.INFO, "Pausing simulation timer", SimulationLogUtils.SOURCE_GENERAL, LogSource.UNKNOWN, simulationTime));


        timer.stop();
    }

    public void resumeTimer() {
        if (timer == null) throw new IllegalStateException("resume timer: timer is NULL");
        if (logg.isDebugEnabled()) {
            logg.debug("resuming simulation timer");
        }

        simulationLogUtils.log(new SimulationLog(LogCategory.INFO, "Resuming simulation timer", SimulationLogUtils.SOURCE_GENERAL, LogSource.UNKNOWN, simulationTime));

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
        doSimulation = !doSimulation;

        if (!doSimulation){ //only every other timer tick will make simulation active
            fireSimulationTimerEvent(new SimulationTimerEvent(this, simulationTime));
            return;
        }
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
                simulationLogUtils.log(new SimulationLog(LogCategory.INFO, "Nothing to simulate - simulation stopped", SimulationLogUtils.SOURCE_GENERAL, LogSource.UNKNOWN, simulationTime));
            }
            fireSimulationTimerEvent(new SimulationTimerEvent(this, simulationTime));
        } catch (Exception e) {
            //just to make it fail-safe catch all possible problems
            logg.error("Error during timer execution", e);
            simulationLogUtils.log(new SimulationLog(LogCategory.ERROR, "Unknown error: " + e.getLocalizedMessage(), SimulationLogUtils.SOURCE_GENERAL, LogSource.UNKNOWN, - 1));
        }
    }


    public boolean isRunning() {
        if (timer == null) return false;
        return timer.isRunning();
    }

    /**
     * simulation may be done when all these conditions are fulfilled:
     * 1. there are no unfinished simulation rules (none is active or scheduled to start)
     * 2. all packets are in DELIVERED state = that means: no packets on the edges and no packets in the network nodes
     *
     * @return
     */
    public boolean isEndOfSimulation() {
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
     * @param speedUp no float or double speed up allowed; also no negative or equal to 0
     */
    public void setTimerDelay(double speedUp) {
        if (speedUp <= 0) return;
        this.speedUp = speedUp;
        timer.setDelay(convertTime(speedUp));
    }

    /**
     * converts speed-up contant to msec that will be used in java.util.Timer delay
     * note, that higher the speed-up constant, lower the delay and vice-versa
     *
     * @param speedUp
     * @return
     */
    private int convertTime(double speedUp) {
        if (speedUp < MIN_SPEED_UP) return MAX_TIMER_DELAY;
        if (speedUp > MAX_SPEED_UP) return MIN_TIMER_DELAY;

        int result = safeLongToInt(Math.round(MAX_TIMER_DELAY / speedUp));
        if (result == 0) return MIN_TIMER_DELAY;
        return result;
    }

    private static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        if (l > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) l;
    }

    public void addSimulationTimerListener(SimulationTimerListener listener) {
        listenerList.add(SimulationTimerListener.class, listener);
    }

    public void removeSimulationTimerListener(SimulationTimerListener listener) {
        listenerList.remove(SimulationTimerListener.class, listener);
    }

    private void fireSimulationTimerEvent(SimulationTimerEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i].equals(SimulationTimerListener.class)) {
                ((SimulationTimerListener) listeners[i + 1]).simulationTimerOccurred(evt);
            }
        }
    }
}
