/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.events.statisticaldata;

import java.util.EventObject;
import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

/**
 * new ping packet has been delivered (hurray!)
 *
 * this event contains information about RTT of the ping and when was the packet
 * delivered - these two values will be used in the chart
 *
 * @author Igor Kvasnicka
 */
@Getter
public class StatisticalDataEvent extends EventObject {

    private final SimulationRuleBean rule;
    private final double rtt;
    private final double when;

    public StatisticalDataEvent(Object source, SimulationRuleBean rule, double rtt, double when) {
        super(source);
        this.rule = rule;
        this.rtt = rtt;
        this.when = when;
    }
}
