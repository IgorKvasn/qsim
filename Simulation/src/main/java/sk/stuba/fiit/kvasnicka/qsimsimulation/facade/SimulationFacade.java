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

package sk.stuba.fiit.kvasnicka.qsimsimulation.facade;

import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.pingrule.PingRuleListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.simulationrule.SimulationRuleListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.SimulationManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PingManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.util.List;

/**
 * facade for all operations related to simulation
 *
 * @author Igor Kvasnicka
 */
public class SimulationFacade {

    private SimulationTimer timer;
    private SimulationManager simulationManager = new SimulationManager();
    private PingManager pingManager = new PingManager();

    /**
     * initialises simulation timer
     *
     * @param edgeList list of all edges
     * @param nodeList list of all vertices (network nodes)
     */
    public void initTimer(List<Edge> edgeList, List<NetworkNode> nodeList) {
        if (timer != null && timer.isRunning()) {
            throw new IllegalStateException("Starting timer: simulation timer is already running.");
        }
        timer = new SimulationTimer(edgeList, nodeList);
    }

    /**
     * creates and starts new simulation timer
     * default timer delay will be used
     *
     * @throws IllegalStateException when timer is already running
     */
    public void startTimer() {
        if (timer == null) {
            throw new IllegalStateException("Starting timer: simulation timer has not been initialised.");
        }
        if (timer.isRunning()) {
            throw new IllegalStateException("Starting timer: simulation timeris already running.");
        }
        timer.startSimulationTimer(simulationManager, pingManager);
    }

    /**
     * creates and starts new simulation timer
     *
     * @param initialSpeedUp timer delay speed up (permitted values: no less than 1)
     * @throws IllegalStateException when timer is already running
     */
    public void startTimer(double initialSpeedUp) {
        if (timer == null) {
            throw new IllegalStateException("Starting timer: simulation timer has not been initialised.");
        }
        if (timer.isRunning()) {
            throw new IllegalStateException("Starting timer: simulation timeris already running.");
        }
        timer.setTimerDelay(initialSpeedUp);
        startTimer();
    }

    /**
     * stops executing of a timer
     * it also clears topology from all simulation data (packets, fragments)
     */
    public void stopTimer() {
        if (timer == null) throw new IllegalStateException("Stopping timer: timer has not been started");
        timer.stopTimer();
        timer.clearSimulationData();
    }

    /**
     * pauses executing of a timer
     * if timer is already running, nothing happens
     *
     * @see #resumeTimer()
     */
    public void pauseTimer() {
        if (timer == null) throw new IllegalStateException("Puse timer: timer has not been started");
        timer.pauseTimer();
    }

    /**
     * resumes timer after it was paused
     * if timer is already paused, nothing happens
     *
     * @see #pauseTimer()
     */
    public void resumeTimer() {
        if (timer == null) throw new IllegalStateException("Resume timer: timer has not been started");
        timer.resumeTimer();
    }

    /**
     * changes timer delay
     * delay can be changes anytime - even when timer is running
     *
     * @param speedUp value from 1 to infinity - greater, the slower
     */
    public void setTimerDelay(double speedUp) {
        if (timer == null) throw new IllegalStateException("Change timer delay: timer has not been started");
        timer.setTimerDelay(speedUp);
    }

    /**
     * adds new simulation rule
     *
     * @param rule simulation rule to be added
     */
    public void addSimulationRule(SimulationRuleBean rule) {
        if (rule.isPing()) {
            addPingSimulationRule(rule);
        } else {
            addCommonSimulationRule(rule);
        }
    }

    private void addPingSimulationRule(SimulationRuleBean rule) {
        int repetitions = rule.getNumberOfPackets();
        rule.resetNumberOfPacketsToOne();
        simulationManager.addSimulationRule(rule);
        pingManager.addPing(rule, repetitions);
    }

    private void addCommonSimulationRule(SimulationRuleBean rule) {
        simulationManager.addSimulationRule(rule);
    }

    /**
     * returns list of all defined simulation rules
     * if timer is started, read-only (unmodifiable) list will be returned
     *
     * @return
     */
    public List<SimulationRuleBean> getSimulationRules() {
        if (simulationManager == null) throw new IllegalStateException("simulationManager is NULL");
        if (isTimerRunning()) return simulationManager.getRulesUnmodifiable();

        return simulationManager.getRulesModifiable();
    }

    /**
     * detects if simulation timer is running
     *
     * @return true if running
     */
    public boolean isTimerRunning() {
        if (timer == null) return false;
        return timer.isRunning();
    }

    public void addSimulationRuleListener(SimulationRuleListener listener) {
        simulationManager.addSimulationRuleListener(listener);
    }

    public void removeSimulationRuleListener(SimulationRuleListener listener) {
        simulationManager.removeSimulationRuleListener(listener);
    }

    public void addPingRuleListener(PingRuleListener listener) {
        pingManager.addPingRuleListener(listener);
    }

    public void removePingRuleListener(PingRuleListener listener) {
        pingManager.removePingRuleListener(listener);
    }
}
