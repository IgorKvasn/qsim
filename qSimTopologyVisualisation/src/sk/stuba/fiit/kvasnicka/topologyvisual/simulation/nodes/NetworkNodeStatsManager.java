/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.simulation.nodes;

import info.monitorenter.gui.chart.ITrace2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.UsageStatistics;
import sk.stuba.fiit.kvasnicka.qsimsimulation.facade.SimulationFacade;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.nodes.NetworkNodeStatisticsBean.TraceIdentifier;

/**
 * manages all NetworkNodeStatisticsBean objects
 *
 * @author Igor Kvasnicka
 */
public class NetworkNodeStatsManager {

    private Map<NetworkNode, NetworkNodeStatisticsBean> statisticsBeans;
    private SimulationFacade simulationFacade;

    public NetworkNodeStatsManager(List<NetworkNode> networkNodeList, SimulationFacade simulationFacade) {
        this.simulationFacade = simulationFacade;
        statisticsBeans = new HashMap<NetworkNode, NetworkNodeStatisticsBean>();

        for (NetworkNode node : networkNodeList) {
            NetworkNodeStatisticsBean networkNodeStatisticsBean = new NetworkNodeStatisticsBean(node);
            simulationFacade.addSimulationTimerListener(networkNodeStatisticsBean);
            statisticsBeans.put(networkNodeStatisticsBean.getNode(), networkNodeStatisticsBean);
        }
    }

    public List<ITrace2D> getAllTraces() {
        List<ITrace2D> list = new LinkedList<ITrace2D>();

        for (NetworkNodeStatisticsBean statBean : statisticsBeans.values()) {
            for (TraceIdentifier trace : statBean.getAllTraceIdentifiers()) {
                list.add(trace.getTrace());
            }
        }

        return list;
    }

    public void removeStatisticsListeners() {
        for (NetworkNodeStatisticsBean bean : statisticsBeans.values()) {
            simulationFacade.removeSimulationTimerListener(bean);
        }
    }

    /**
     * finds TraceIdentifier that represents specified usage statistics
     *
     * @param usageStatistics
     * @return
     */
    public TraceIdentifier getTrace(UsageStatistics usageStatistics) {
        for (NetworkNodeStatisticsBean statBean : statisticsBeans.values()) {
            for (TraceIdentifier trace : statBean.getAllTraceIdentifiers()) {
                if (trace.getUsageStatistics() == usageStatistics) {//yes, I am comparing referencies, because that should be exact same object
                    return trace;
                }
            }
        }
        throw new IllegalStateException("Could not find TraceIdentifier - something went really wrong");
    }
}
