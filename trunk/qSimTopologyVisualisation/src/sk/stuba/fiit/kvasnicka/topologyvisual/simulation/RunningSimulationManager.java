/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.simulation;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import org.apache.log4j.Logger;

/**
 * manages all running simulations it only contains list of names of these
 * simulations - that's all. it does <b>not</b> control them
 * (stop/pause/play/....)
 *
 * @author Igor Kvasnicka
 */
public class RunningSimulationManager {

    private static Logger logg = Logger.getLogger(RunningSimulationManager.class);
    @Getter
    private Set<String> runningList;
    private static RunningSimulationManager INSTANCE;

    private RunningSimulationManager() {
        runningList = new HashSet<String>();
    }

    public void simulationStarted(String simulationName) {
        if (runningList.contains(simulationName)) {
            logg.error("this simulation seems to be running " + simulationName);
        }
        runningList.add(simulationName);
    }

    public void simulationEnded(String simulationName) {
        if (!runningList.contains(simulationName)) {
            logg.error("this simulation seems NOT to be running " + simulationName);
        }
        runningList.remove(simulationName);
    }

    public static RunningSimulationManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RunningSimulationManager();
        }
        return INSTANCE;
    }
}
