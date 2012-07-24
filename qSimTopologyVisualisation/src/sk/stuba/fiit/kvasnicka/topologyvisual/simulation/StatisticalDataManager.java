/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.simulation;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ping.PingPacketDeliveredEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.ping.PingPacketDeliveredListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.pingrule.PingRuleEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.pingrule.PingRuleListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.simulationrule.SimulationRuleEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.simulationrule.SimulationRuleListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.packet.Packet;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

/**
 *
 * @author Igor Kvasnicka
 */
public final class StatisticalDataManager implements PingRuleListener, SimulationRuleListener, PingPacketDeliveredListener {

    private Map<String, StatisticalData> dataMap;

    public StatisticalDataManager(List<SimulationRuleBean> rules) {
        dataMap = new HashMap<String, StatisticalData>(rules.size() * 4 / 3);
        for (SimulationRuleBean rule : rules) {
            addData(rule);
        }
    }
    
    public Collection<StatisticalData> getStatisticalData(){
        return dataMap.values();
    }

    private void addData(SimulationRuleBean rule) {
        if (dataMap.containsKey(rule.getUniqueID())) {
            throw new IllegalStateException("such simulation rule already exists in StatisticalDataManager");
        }
        dataMap.put(rule.getUniqueID(), createStatisticalData(rule));
    }

    private void removeData(SimulationRuleBean rule) {
        if (!dataMap.containsKey(rule.getUniqueID())) {
            throw new IllegalStateException("no such simulation rule already exists in StatisticalDataManager");
        }
        dataMap.remove(rule.getUniqueID());

    }

    private StatisticalData createStatisticalData(SimulationRuleBean rule) {
        return new StatisticalData(rule);
    }

    private StatisticalData getStatisticalData(SimulationRuleBean rule) {
        if (rule == null) {
            throw new IllegalArgumentException("rule is NULL");
        }
        if (!dataMap.containsKey(rule.getUniqueID())) {
            throw new IllegalStateException("could not find simulation rule");
        }
        return dataMap.get(rule.getUniqueID());
    }

    private void packetDelivered(Packet packet) {
        StatisticalData statisticalData = getStatisticalData(packet.getSimulationRule());
        statisticalData.addDelay(packet.getSimulationTime() - packet.getCreationTime());
    }

    @Override
    public void pingRuleAdded(PingRuleEvent event) {
        addData(event.getRule());
    }

    @Override
    public void pingRuleRemoved(PingRuleEvent event) {
        removeData(event.getRule());
    }

    @Override
    public void simulationRuleAdded(SimulationRuleEvent event) {
        addData(event.getRule());
    }

    @Override
    public void simulationRuleRemoved(SimulationRuleEvent event) {
        removeData(event.getRule());
    }

    @Override
    public void packetDeliveredOccurred(PingPacketDeliveredEvent evt) {
        packetDelivered(evt.getPacket());
    }
}
