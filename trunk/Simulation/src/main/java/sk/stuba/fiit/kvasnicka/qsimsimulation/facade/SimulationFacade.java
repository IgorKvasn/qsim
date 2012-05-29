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
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.qsimsimulation.SimulationTimer;
import sk.stuba.fiit.kvasnicka.qsimsimulation.managers.SimulationManager;

import java.util.List;

/**
 * facade for all operations related to simulation
 *
 * @author Igor Kvasnicka
 */
public class SimulationFacade {

    private SimulationTimer timer;

    /**
     * creates and starts new simulation timer
     * default timer delay will be used
     *
     * @param edgeList          list of all edges
     * @param nodeList          list of all vertices (network nodes)
     * @param simulationManager simulation timer associated with this simulation
     * @throws IllegalStateException when timer is already running
     */
    public void startTimer(List<Edge> edgeList, List<NetworkNode> nodeList, SimulationManager simulationManager) {
        if (timer != null && timer.isRunning()) {
            throw new IllegalStateException("Starting timer: simulation timer is already running.");
        }
        timer = new SimulationTimer(edgeList, nodeList);
        timer.startSimulationTimer(simulationManager);
    }

    /**
     * creates and starts new simulation timer
     *
     * @param edgeList          list of all edges
     * @param nodeList          list of all vertices (network nodes)
     * @param simulationManager simulation timer associated with this simulation
     * @param initialSpeedUp    timer delay speed up (permitted values: no less than 1)
     * @throws IllegalStateException when timer is already running
     */
    public void startTimer(List<Edge> edgeList, List<NetworkNode> nodeList, SimulationManager simulationManager, double initialSpeedUp) {
        timer = new SimulationTimer(edgeList, nodeList);
        timer.setTimerDelay(initialSpeedUp);
        timer.startSimulationTimer(simulationManager);
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
        if (timer == null) throw new IllegalStateException("Adding simulation rule: timer has not been started");
        timer.addSimulationrule(rule);
    }

    /**
     * adds new simulation rule for ping
     *
     * @param rule        simulation rule
     * @param pingRepeats number of ping packets that will be sent; packets are send one after another - not all at once
     */
    public void addPingRule(SimulationRuleBean rule, int pingRepeats) {
        if (timer == null) throw new IllegalStateException("Adding ping rule: timer has not been started");
        timer.addPingSimulationRule(rule, pingRepeats);
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
}
