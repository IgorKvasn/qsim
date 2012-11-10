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
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.nodes.NetworkNodeStatisticsBean.TraceIdentifier;

/**
 *
 * @author Igor Kvasnicka
 */
public class NetworkNodeStatsManager {

    private Map<NetworkNode, NetworkNodeStatisticsBean> statisticsBeans;
    private List<Usage2Trace> translateList = new java.util.LinkedList();
    private SimulationFacade simulationFacade;

    public NetworkNodeStatsManager(List<NetworkNode> networkNodeList, SimulationFacade simulationFacade) {
        this.simulationFacade = simulationFacade;
        statisticsBeans = new HashMap<NetworkNode, NetworkNodeStatisticsBean>();

        for (NetworkNode node : networkNodeList) {
            NetworkNodeStatisticsBean networkNodeStatisticsBean = new NetworkNodeStatisticsBean(node);
            simulationFacade.addSimulationTimerListener(networkNodeStatisticsBean);
            statisticsBeans.put(networkNodeStatisticsBean.getNode(), networkNodeStatisticsBean);

            translateList.add(new Usage2Trace(node.getAllOutputQueues(), networkNodeStatisticsBean.getOutputTotalTrace()));
            translateList.add(new Usage2Trace(node.getAllProcessingPackets(), networkNodeStatisticsBean.getProcessingTrace()));
            translateList.add(new Usage2Trace(node.getAllRXBuffers(), networkNodeStatisticsBean.getRxTotalTrace()));
            translateList.add(new Usage2Trace(node.getAllTXBuffers(), networkNodeStatisticsBean.getTxTotalTrace()));
            translateList.add(new Usage2Trace(node.getInputQueue(), networkNodeStatisticsBean.getInputTrace()));

            //todo init networkNodeStatisticsBean.getRxTraceMap() and TX            

        }
    }

    public List<ITrace2D> getAllTraces() {
        List<ITrace2D> list = new LinkedList<ITrace2D>();

        for (Usage2Trace usage : translateList) {
            list.add(usage.traceIdentifier.getTrace());
        }

        return list;
    }

    public void removeStatisticsListeners() {
        for (NetworkNodeStatisticsBean bean : statisticsBeans.values()) {
            simulationFacade.removeSimulationTimerListener(bean);
        }
    }

    public TraceIdentifier getTrace(UsageStatistics usageStatistics) {
        for (Usage2Trace usage2Trace : translateList) {
            if (usage2Trace.usageStatistics == usageStatistics) {//yes, I am comparing referencies, because that should be exact same object
                return usage2Trace.traceIdentifier;
            }
        }
        throw new IllegalStateException("Could not find TraceIdentifier - something went really wrong");
    }

    private class Usage2Trace {

        private UsageStatistics usageStatistics;
        private TraceIdentifier traceIdentifier;

        public Usage2Trace(UsageStatistics usageStatistics, TraceIdentifier traceIdentifier) {
            this.usageStatistics = usageStatistics;
            this.traceIdentifier = traceIdentifier;
        }
    }
}
