/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.simulation;

import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

/**
 * here are collected statistical data for one simulation rule
 *
 * @author Igor Kvasnicka
 */
public class StatisticalData {

    @Getter
    private SimulationRuleBean rule;
    private List<Double> delayList;

    public StatisticalData(SimulationRuleBean rule) {
        this.rule = rule;
        delayList = new LinkedList<Double>();
    }

    public String getSimulationruleId() {
        return rule.getUniqueID();
    }

    public void addDelay(double delay) {
        if (delay < 0) {
            throw new IllegalStateException("packet delay is negative - something is wrong with simulation engine...");
        }
        delayList.add(delay);
    }

    public double calculateAvarageDelay() {
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
}
