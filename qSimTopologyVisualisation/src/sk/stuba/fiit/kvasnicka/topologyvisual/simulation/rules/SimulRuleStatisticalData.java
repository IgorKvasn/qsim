/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.simulation.rules;

import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.topologyvisual.events.statisticaldata.StatisticalDataChangedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.events.statisticaldata.StatisticalDataEvent;

/**
 * here are collected statistical data for one simulation rule
 *
 * @author Igor Kvasnicka
 */
public class SimulRuleStatisticalData {

    @Getter
    private SimulationRuleBean rule;
    @Getter
    private List<Point2D.Double> delayList;
    @Getter
    private ITrace2D chartTrace;
    private transient javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();
    private static Random random = new Random();

    public SimulRuleStatisticalData(SimulationRuleBean rule) {
        this.rule = rule;
        delayList = new LinkedList<Point2D.Double>();
        chartTrace = createTrace(rule, generateRandomColor());
        chartTrace.setName(rule.getName());
    }

    public String getSimulationruleId() {
        return rule.getUniqueID();
    }

    /**
     * "trace" is object that stores point on the chart
     *
     * @param traceColor color to be rendered
     * @return
     */
    private ITrace2D createTrace(SimulationRuleBean rule, Color traceColor) {

        ITrace2D trace = new Trace2DSimple();
        trace.setColor(traceColor);
        trace.setName(rule.getName());
        //  trace.setTracePainter(new TracePainterFill(chart));       

        return trace;
    }

    private Color generateRandomColor() {
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    /**
     * a new ping packet has been delivered
     *
     * @param delay RTT of the ping
     * @param when when was ping delivered (simulation time)
     */
    public void addDelay(double delay, double when) {
        if (delay < 0) {
            throw new IllegalStateException("packet delay is negative - something is wrong with simulation engine...");
        }
        delayList.add(new Point2D.Double(when,delay));
        chartTrace.addPoint(when, delay);
//        DecimalFormat df = new DecimalFormat("#.##");//round to two decimal places
//        chartTrace.addPoint(when, Double.valueOf(df.format(delay)));
        fireStatisticalDataChangeEvent(new StatisticalDataEvent(this, rule, delay, when));
    }

    /**
     * returns number of statistical data collected
     *
     * @return
     */
    public int getStatisticalDataCount() {
        return delayList.size();
    }

    /**
     * calculates average delay
     *
     * @return
     */
    public double calculateAverageDelay() {
        if (delayList.isEmpty()) {
            return Double.NaN;
        }
        double av = 0;
        for (Point2D.Double delay : delayList) {
            av += delay.y;
        }
        return av / delayList.size();
    }

    public double getMinDelay() {
        if (delayList.isEmpty()) {
            return Double.NaN;
        }
        double min = Double.MAX_VALUE;
        for (Point2D.Double delay : delayList) {
            min = Math.min(min, delay.y);
        }
        return min;
    }

    public double getMaxDelay() {
        if (delayList.isEmpty()) {
            return Double.NaN;
        }
        double max = Double.MIN_VALUE;
        for (Point2D.Double delay : delayList) {
            max = Math.max(max, delay.y);
        }
        return max;
    }

    public void addStatisticalDataChangedListener(StatisticalDataChangedListener listener) {
        listenerList.add(StatisticalDataChangedListener.class, listener);
    }

    public void removeStatisticalDataChangedListener(StatisticalDataChangedListener listener) {
        listenerList.remove(StatisticalDataChangedListener.class, listener);
    }

    private void fireStatisticalDataChangeEvent(StatisticalDataEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i].equals(StatisticalDataChangedListener.class)) {
                ((StatisticalDataChangedListener) listeners[i + 1]).statisticalDataChangeOccured(evt);
            }
        }
    }
}
