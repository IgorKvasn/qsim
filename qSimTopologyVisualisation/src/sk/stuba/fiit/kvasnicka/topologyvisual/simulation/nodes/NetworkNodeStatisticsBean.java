/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.simulation.nodes;

import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import java.awt.Color;
import java.util.Random;
import lombok.Getter;
import lombok.Setter;
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
    private TraceIdentifier rxTrace;
    private TraceIdentifier txTrace;
    private TraceIdentifier outputTrace;
    private TraceIdentifier inputTrace;
    private TraceIdentifier processingTrace;
    private static Random random = new Random();

    public NetworkNodeStatisticsBean(NetworkNode node) {
        this.node = node;

        rxTrace = new TraceIdentifier(createTrace(node.getName() + " - RX"));
        txTrace = new TraceIdentifier(createTrace(node.getName() + " - TX"));
        outputTrace = new TraceIdentifier(createTrace(node.getName() + " - Output queue"));
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
        rxTrace.trace.addPoint(x, node.getRXUsage());
        txTrace.trace.addPoint(x, node.getTXUsage());
        outputTrace.trace.addPoint(x, node.getOutputQueueUsage());
        inputTrace.trace.addPoint(x, node.getInputQueueUsage());
        processingTrace.trace.addPoint(x, node.getProcessingPackets());
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
