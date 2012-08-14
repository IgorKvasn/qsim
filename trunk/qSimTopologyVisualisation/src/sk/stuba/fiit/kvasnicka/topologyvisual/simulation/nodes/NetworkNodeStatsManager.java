/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.simulation.nodes;

import info.monitorenter.gui.chart.ITrace2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimsimulation.facade.SimulationFacade;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.nodes.NetworkNodeStatisticsBean.TraceIdentifier;

/**
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

    public void removeStatisticsListeners() {
        for (NetworkNodeStatisticsBean bean : statisticsBeans.values()) {
            simulationFacade.removeSimulationTimerListener(bean);
        }
    }

    /**
     * returns trace for chart
     *
     * @param node
     * @param prop
     * @return
     */
    public TraceIdentifier getTrace(NetworkNode node, NetworkNodePropertyEnum prop) {
        if (!statisticsBeans.containsKey(node)) {
            throw new IllegalArgumentException("could not find netowrk node in NetworkNodeStatsManager: " + node.getName());
        }
        switch (prop) {
            case RX:
                return statisticsBeans.get(node).getRxTrace();
            case TX:
                return statisticsBeans.get(node).getTxTrace();
            case OUTPUT_BUFFER:
                return statisticsBeans.get(node).getOutputTrace();
            case INPUT_BUFFER:
                return statisticsBeans.get(node).getInputTrace();
            case PROCESSING:
                return statisticsBeans.get(node).getProcessingTrace();
            default:
                throw new IllegalStateException("unknown network node property enum: " + prop);
        }
    }
}
