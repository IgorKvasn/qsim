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
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.log.SimulationLogListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.pingrule.PingRuleListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.simulationrule.SimulationRuleListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLogUtil;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.PingManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.SimulationManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.util.LinkedList;
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

    /**
     * removes simulation rules
     *
     * @param rule
     */
    public void removeSimulationRule(SimulationRuleBean rule) {
        if (rule.isPing()) {
            pingManager.removePing(rule);
        } else {
            simulationManager.removeSimulationRule(rule);
        }
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
     * returns list of all defined ping simulation rules
     *
     * @return
     */
    public List<SimulationRuleBean> getPingSimulationRules() {
        if (pingManager == null) throw new IllegalStateException("pingManager is NULL");
        return pingManager.getPingSimulationRules();
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

    /**
     * returns list of all simulation or ping rules that contains specified NetworkNode in their route
     *
     * @param node NetworkNode to search for
     * @return
     */
    public List<SimulationRuleBean> getSimulRulesThatContainsNode(NetworkNode node) {
        List<SimulationRuleBean> simulRules = getRulesThatContainsNode(simulationManager.getRulesUnmodifiable(), node);
        List<SimulationRuleBean> pingRules = getRulesThatContainsNode(pingManager.getPingSimulationRules(), node);

        simulRules.addAll(pingRules);

        return simulRules;
    }

    private List<SimulationRuleBean> getRulesThatContainsNode(List<SimulationRuleBean> rules, NetworkNode node) {
        if (node == null || rules == null) return new LinkedList<SimulationRuleBean>();
        List<SimulationRuleBean> result = new LinkedList<SimulationRuleBean>();
        for (SimulationRuleBean rule : rules) {
            if (rule.getRoute().contains(node)) {
                result.add(rule);
            }
        }
        return result;
    }


    /**
     * register for adding or removing new simulation rule
     *
     * @param listener
     */
    public void addSimulationRuleListener(SimulationRuleListener listener) {
        simulationManager.addSimulationRuleListener(listener);
    }

    public void removeSimulationRuleListener(SimulationRuleListener listener) {
        simulationManager.removeSimulationRuleListener(listener);
    }

    /**
     * register for adding or removing new ping simulation rule
     *
     * @param listener
     */
    public void addPingRuleListener(PingRuleListener listener) {
        pingManager.addPingRuleListener(listener);
    }

    public void removePingRuleListener(PingRuleListener listener) {
        pingManager.removePingRuleListener(listener);
    }

    /**
     * register for simulation logs, e.g. packet delivery, topology errors/informations, etc.
     */
    public void addSimulationLogListener(SimulationLogListener l) {
        SimulationLogUtil.getInstance().addSimulationLogListener(l);
    }

    /**
     * removes listener for simulation logs, e.g. packet delivery, topology errors/informations, etc.
     */
    public void removeSimulationLogListener(SimulationLogListener l) {
        SimulationLogUtil.getInstance().removeSimulationLogListener(l);
    }
}

