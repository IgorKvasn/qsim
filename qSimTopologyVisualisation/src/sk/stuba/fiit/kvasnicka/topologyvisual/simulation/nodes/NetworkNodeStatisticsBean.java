/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.simulation.nodes;

import info.monitorenter.gui.chart.ITrace2D;
import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.timer.SimulationTimerEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.timer.SimulationTimerListener;

/**
 * collects all statistics about one NetworkNode
 *
 * @author Igor Kvasnicka
 */
@Getter
public class NetworkNodeStatisticsBean implements SimulationTimerListener {

    private NetworkNode node;
    private ITrace2D rxTrace;
    private ITrace2D txTrace;
    private ITrace2D outputTrace;
    private ITrace2D inputTrace;
    private ITrace2D processingTrace;

    public NetworkNodeStatisticsBean(NetworkNode node) {
        this.node = node;
        //todo init traces
    }

    @Override
    public void simulationTimerOccurred(SimulationTimerEvent ste) {
        //todo add data from node to traces
    }
}
