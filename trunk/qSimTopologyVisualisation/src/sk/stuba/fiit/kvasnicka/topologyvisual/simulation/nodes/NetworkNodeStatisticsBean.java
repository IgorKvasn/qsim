/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.simulation.nodes;

import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.Getter;
import lombok.Setter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.UsageStatistics;
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
    private TraceIdentifier rxTotalTrace;
    private Map<NetworkNode, TraceIdentifier> rxTraceMap; //key = the same as key in RX interface map
    private Map<NetworkNode, TraceIdentifier> txTraceMap; //key = the same as key in RX interface map
    private List<TraceIdentifier> outputTraceList; //the order is the same is specified in NetowrkNode's OutputQueueManager
    private TraceIdentifier txTotalTrace;
    private TraceIdentifier outputTotalTrace;
    private TraceIdentifier inputTrace;
    private TraceIdentifier processingTrace;
    private static Random random = new Random();
    private List<UsageStatistics> allUsages;

    public NetworkNodeStatisticsBean(NetworkNode node) {
        this.node = node;
        rxTraceMap = new HashMap<NetworkNode, TraceIdentifier>(node.getRxInterfaces().size() * 4 / 3);
        txTraceMap = new HashMap<NetworkNode, TraceIdentifier>(node.getTxInterfaces().size() * 4 / 3);
        outputTraceList = new LinkedList<TraceIdentifier>();
        allUsages = new LinkedList<UsageStatistics>();


        rxTotalTrace = new TraceIdentifier(createTrace(node.getName() + " - RX"));
        txTotalTrace = new TraceIdentifier(createTrace(node.getName() + " - TX"));
        outputTotalTrace = new TraceIdentifier(createTrace(node.getName() + " - Output queue"));
        inputTrace = new TraceIdentifier(createTrace(node.getName() + " - Input queue"));
        processingTrace = new TraceIdentifier(createTrace(node.getName() + " - Processing packets"));


    }

    private ITrace2D createTrace(String traceName) {
        ITrace2D trace = new Trace2DSimple();
        trace.setColor(generateRandomColor());
        trace.setName(traceName);
        return trace;
    }

    private Color generateRandomColor() {
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    @Override
    public void simulationTimerOccurred(SimulationTimerEvent ste) {
        double x = ste.getSimulationTime();

        //all RX buffers together
        rxTotalTrace.trace.addPoint(x, node.getAllRXBuffers().getUsage());

        // RX buffers
        for (Map.Entry<NetworkNode, TraceIdentifier> e : rxTraceMap.entrySet()) {
            e.getValue().trace.addPoint(x, node.getRxInterfaces().get(e.getKey()).getUsage());
        }

        //all TX buffers together
        txTotalTrace.trace.addPoint(x, node.getAllTXBuffers().getUsage());

        // TX buffers
        for (Map.Entry<NetworkNode, TraceIdentifier> e : txTraceMap.entrySet()) {
            e.getValue().trace.addPoint(x, node.getTxInterfaces().get(e.getKey()).getUsage());
        }

        //all output queues
        outputTotalTrace.trace.addPoint(x, node.getAllOutputQueues().getUsage());

        //output queues
        for (int i = 0; i < outputTraceList.size(); i++) {
            outputTraceList.get(i).trace.addPoint(x, node.getOutputQueues().getQueues()[i].getUsage());
        }

        //ptocessing packets
        processingTrace.trace.addPoint(x, node.getAllProcessingPackets().getUsage());
    }

    /**
     * returns all UsageStatistics for this network node
     *
     * @return
     */
    List<UsageStatistics> getUsages() {
        return allUsages;
    }

    @Getter
    public static class TraceIdentifier {

        private ITrace2D trace;
        @Setter
        private boolean visible;

        public TraceIdentifier(ITrace2D trace) {
            this.trace = trace;
            this.visible = false;
        }
    }
}
