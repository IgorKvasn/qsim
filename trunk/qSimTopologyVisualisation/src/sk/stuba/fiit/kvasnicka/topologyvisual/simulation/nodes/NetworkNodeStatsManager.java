/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.simulation.nodes;

import java.util.LinkedList;
import java.util.List;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.facade.SimulationFacade;

/**
 *
 * @author Igor Kvasnicka
 */
public class NetworkNodeStatsManager {

    private List<NetworkNodeStatisticsBean> statisticsBeans;
    private SimulationFacade simulationFacade;

    public NetworkNodeStatsManager(List<NetworkNode> networkNodeList, SimulationFacade simulationFacade) {
        this.simulationFacade = simulationFacade;
        statisticsBeans = new LinkedList<NetworkNodeStatisticsBean>();

        for (NetworkNode node : networkNodeList) {
            NetworkNodeStatisticsBean networkNodeStatisticsBean = new NetworkNodeStatisticsBean(node);
            simulationFacade.addSimulationTimerListener(networkNodeStatisticsBean);
            statisticsBeans.add(networkNodeStatisticsBean);
        }
    }

    public void removeStatisticsListeners() {
        for (NetworkNodeStatisticsBean bean : statisticsBeans) {
            simulationFacade.removeSimulationTimerListener(bean);
        }
    }
}
