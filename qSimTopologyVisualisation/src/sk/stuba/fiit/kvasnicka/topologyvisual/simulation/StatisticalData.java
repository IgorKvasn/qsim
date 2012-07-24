/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.simulation;

import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterFill;
import java.awt.Color;
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
public class StatisticalData {

    @Getter
    private SimulationRuleBean rule;
    private List<Double> delayList;
    @Getter
    private ITrace2D chartTrace;
    private transient javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();
    private static Random random = new Random();

    public StatisticalData(SimulationRuleBean rule) {
        this.rule = rule;
        delayList = new LinkedList<Double>();
        chartTrace = createTrace(rule, generateRandomColor());
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
        delayList.add(delay);
        chartTrace.addPoint(when,delay);
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
        double av = 0;
        for (double delay : delayList) {
            av += delay;
        }
        return av / delayList.size();
    }

    public double getMinDelay() {
        double min = Double.MAX_VALUE;
        for (double delay : delayList) {
            min = Math.min(min, delay);
        }
        return min;
    }

    public double getMaxDelay() {
        double max = Double.MIN_VALUE;
        for (double delay : delayList) {
            max = Math.max(max, delay);
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
